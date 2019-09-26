package leomrlima.consumer;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import leomrlima.common.kafka.Bootstrap;
import leomrlima.common.kafka.KafkaConnection;
import leomrlima.common.redis.RedisDatabase;

import static spark.Spark.*;


public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static void main(String[] args) {
		String appId = System.getenv("CONSUMER_NAME");
		if (appId == null) {
			logger.warn("CONSUMER_NAME environment variable is null, please set it to something meaningul!");
			appId = UUID.randomUUID().toString();
		}
		String groupId = System.getenv("CONSUMER_GROUP");
		if (groupId == null) {
			logger.warn("CONSUMER_GROUP environment variable is null, please set it to something meaningul!");
			groupId = UUID.randomUUID().toString();
		}

		logger.debug("Consumer {} / {} started", appId, groupId);

		// set up Kafka access
		Bootstrap bootstrap = new Bootstrap(appId, groupId);
		KafkaConnection connection = new KafkaConnection("deviceEvents", bootstrap);

		// set up Redis access
		RedisDatabase<DeviceStatus> deviceStatusDb = new RedisDatabase<DeviceStatus>("leomrlima",
				DeviceStatus::getDeviceId);
		
		// set up REST access
		get("/status/:deviceId", (req, res) -> {
			logger.info("Info requested for {}", req.params(":deviceId"));
			Optional<DeviceStatus> dvc = deviceStatusDb.get(req.params(":deviceId"));
			if (dvc.isPresent()) {
				return gson.toJson(dvc.get());
			} else {
				res.status(404);
				return "Not found";
			}
		});

		// set up event processing for infinity
		while (true) {
			connection.poll(Duration.ofDays(1), (event) -> {
				logger.info("New record: {}", event);
				switch (event.type) {
				case CONNECTED:
					if (!deviceStatusDb.keyExists(event.deviceId)) {
						logger.warn("Bogus event {} detected for {}", event.type, event.deviceId);
					} else {
						Optional<DeviceStatus> deviceStatus = deviceStatusDb.get(event.deviceId);
						if (!deviceStatus.isPresent()) {
							logger.info("Bogus event detected for {}", event.deviceId);
						} else {
							deviceStatus.get().status = "Connected via " + event.gatewayId;
							deviceStatusDb.set(deviceStatus.get());
							logger.info("Device Status updated: {}", deviceStatus.get());
						}
					}
					break;
				case DISCONNECTED:
					if (!deviceStatusDb.keyExists(event.deviceId)) {
						logger.warn("Bogus event {} detected for {}", event.type, event.deviceId);
					} else {
						Optional<DeviceStatus> deviceStatus = deviceStatusDb.get(event.deviceId);
						if (!deviceStatus.isPresent()) {
							logger.info("Bogus event detected for {}", event.deviceId);
						} else {
							deviceStatus.get().status = "Disconnected";
							deviceStatusDb.set(deviceStatus.get());
							logger.info("Device Status updated: {}", deviceStatus.get());
						}
					}
					break;
				case SEND_DATA:
				case SENT_DATA:
					if (!deviceStatusDb.keyExists(event.deviceId)) {
						logger.warn("Bogus event {} detected for {}", event.type, event.deviceId);
					} else {
						Optional<DeviceStatus> deviceStatus = deviceStatusDb.get(event.deviceId);
						if (!deviceStatus.isPresent()) {
							logger.info("Bogus event detected for {}", event.deviceId);
						} else {
							deviceStatus.get().byteCount += event.payload.length;
							deviceStatusDb.set(deviceStatus.get());
							logger.info("Device Status updated: {}", deviceStatus.get());
						}
					}
					break;
				case DELETED:
					if (!deviceStatusDb.keyExists(event.deviceId)) {
						logger.warn("Bogus registration deletion detected for {}", event.deviceId);
					} else {
						Optional<DeviceStatus> deviceStatus = deviceStatusDb.getAndDelete(event.deviceId);
						logger.info("Device Status deleted: {}", deviceStatus);
					}
					break;
				case REGISTERED:
					if (deviceStatusDb.keyExists(event.deviceId)) {
						logger.warn("Duplicate registration detected for {}", event.deviceId);
					} else {
						DeviceStatus deviceStatus = new DeviceStatus();
						deviceStatus.deviceId = event.deviceId;
						deviceStatusDb.set(deviceStatus);
						logger.info("New Device Status registered: {}", deviceStatus);
					}
					break;
				default:
					break;
				}
			});
		}
	}

}
