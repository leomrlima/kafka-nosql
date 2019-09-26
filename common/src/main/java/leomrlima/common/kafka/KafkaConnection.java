package leomrlima.common.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import leomrlima.common.event.DeviceEvent;

public class KafkaConnection {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConnection.class);
	
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private final Serializer<DeviceEvent> serializer = new Serializer<DeviceEvent>() {

		@Override
		public void configure(Map<String, ?> configs, boolean isKey) {
			// NOOP
		}

		@Override
		public byte[] serialize(String topic, DeviceEvent data) {
			return gson.toJson(data).getBytes();
		}

		@Override
		public void close() {
			// NOOP
		}

	};

	private final Deserializer<DeviceEvent> deserializer = new Deserializer<DeviceEvent>() {

		@Override
		public void configure(Map<String, ?> configs, boolean isKey) {
			// NOOP
		}

		@Override
		public DeviceEvent deserialize(String topic, byte[] data) {
			return gson.fromJson(new String(data), DeviceEvent.class);
		}

		@Override
		public void close() {
			// NOOP
		}

	};

	private KafkaProducer<String, DeviceEvent> producer;

	private KafkaConsumer<String, DeviceEvent> consumer;
	
	private Bootstrap bootstrap;

	private final String topic;

	public KafkaConnection(String topic, Bootstrap bootstrap) {
		this.bootstrap = Objects.requireNonNull(bootstrap);
		this.topic = Objects.requireNonNull(topic);
	}

	public synchronized void fire(DeviceEvent value) {
		if (producer == null) {
			producer = bootstrap.startProducer(new StringSerializer(), serializer);
		}
		logger.info("Fire {}: {}", value.getClass().getSimpleName(), value);
		producer.send(new ProducerRecord<>(topic, value.deviceId, value),
				(metadata, exception) -> logger.debug("Event sent with metadata {}", metadata, exception));
	}

	public synchronized void poll(Duration timeout, Consumer<DeviceEvent> recordConsumer) {
		if (consumer == null) {
			consumer = bootstrap.startConsumer(new StringDeserializer(), deserializer);
			consumer.subscribe(Arrays.asList(topic));
		}
		try {
			ConsumerRecords<String, DeviceEvent> records = consumer.poll(timeout);
			logger.info("Received: {}", records);
			records.forEach(record -> recordConsumer.accept(record.value()));
			consumer.commitSync();
		} catch (Exception e) {
			logger.error("Polling", e);
		}
	}

	public synchronized void close() {
		if (consumer != null) {
			consumer.close();
			consumer = null;
		}
		if (producer != null) {
			producer.close();
			producer = null;
		}
	}
}
