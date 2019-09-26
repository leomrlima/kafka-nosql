package leomrlima.consumer2;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.aerogear.kafka.cdi.annotation.Consumer;
import org.aerogear.kafka.cdi.annotation.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;
import jakarta.nosql.mapping.keyvalue.KeyValueTemplate;
import leomrlima.common.event.DeviceEvent;
import leomrlima.consumer2.config.Config;

@ApplicationScoped
@KafkaConfig(bootstrapServers = "kafka:9092")
public class ListenerService {

	private static Logger logger = LoggerFactory.getLogger(ListenerService.class);

	@Inject
	@Database(value = DatabaseType.KEY_VALUE, provider = BucketManagerProducer.STATUS_BUCKET)
	private KeyValueTemplate template;

	@PostConstruct
	private void init() {
		logger.info("Listener started");
	}
	
	@Consumer(topics = Config.TOPIC, groupId = Config.GROUP_ID)
	public void receiver(final JsonObject message) {
		final DeviceEvent event = JsonUtils.fromJson(message, DeviceEvent.class);
		logger.info("New record: {}", event);
		Optional<DeviceStatus> deviceStatus = template.get(event.deviceId, DeviceStatus.class);
		logger.info("Status found?", deviceStatus.isPresent());

		switch (event.type) {
		case CONNECTED:
			if (!deviceStatus.isPresent()) {
				logger.info("Bogus event detected for {}", event.deviceId);
			} else {
				deviceStatus.get().setStatus("Connected via " + event.gatewayId);
				template.put(deviceStatus.get());
				logger.info("Device Status updated: {}", deviceStatus.get());
			}
			break;
		case DISCONNECTED:
			if (!deviceStatus.isPresent()) {
				logger.info("Bogus event detected for {}", event.deviceId);
			} else {
				deviceStatus.get().setStatus("Disconnected");
				template.put(deviceStatus.get());
				logger.info("Device Status updated: {}", deviceStatus.get());
			}
			break;
		case SEND_DATA:
		case SENT_DATA:
			if (!deviceStatus.isPresent()) {
				logger.info("Bogus event detected for {}", event.deviceId);
			} else {
				deviceStatus.get().setByteCount(deviceStatus.get().getByteCount() + event.payload.length);
				template.put(deviceStatus.get());
				logger.info("Device Status updated: {}", deviceStatus.get());
			}
			break;
		case DELETED:
			if (!deviceStatus.isPresent()) {
				logger.warn("Bogus registration deletion detected for {}", event.deviceId);
			} else {
				template.delete(event.deviceId);
				logger.info("Device Status deleted: {}", deviceStatus);
			}
			break;
		case REGISTERED:
			if (deviceStatus.isPresent()) {
				logger.warn("Duplicate registration detected for {}", event.deviceId);
			} else {
				DeviceStatus newDeviceStatus = new DeviceStatus(event.deviceId, "New", 0);
				template.put(newDeviceStatus);
				logger.info("New Device Status registered: {}", newDeviceStatus);
			}
			break;
		default:
			break;
		}
	}
}
