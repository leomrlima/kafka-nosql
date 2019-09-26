package leomrlima.common.kafka;

import java.util.Objects;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	private final Properties configuration;

	public Bootstrap(String applicationName) {
		this(applicationName, null);
	}

	public Bootstrap(String applicationName, String groupId) {
		this.configuration = new Properties();
		loadConfig("bootstrap.servers", "kafka:9092");
		loadConfig("client.id", Objects.requireNonNull(applicationName));
		if (groupId != null) {
			loadConfig("group.id", groupId);
		}
		loadConfig("acks", "all");
		loadConfig("delivery.timeout.ms", 30000);
		loadConfig("batch.size", 16384);
		loadConfig("linger.ms", 100);
		loadConfig("buffer.memory", 33554432);
		loadConfig("retry.backoff.ms", 5000);
		loadConfig("retries", 1000);
	}

	private void loadConfig(String configKey, Object defaultValue) {
		Object value = System.getenv("KAFKA_" + configKey.toUpperCase().replace('.', '_'));
		if (value == null) {
			value = defaultValue;
		}
		logger.warn("{} = {}", configKey, value);
		this.configuration.put(configKey, value);
	}

	public <K, V> KafkaProducer<K, V> startProducer(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
		return new KafkaProducer<>(configuration, keySerializer, valueSerializer);
	}

	public <K, V> KafkaConsumer<K, V> startConsumer(Deserializer<K> keyDeserializer,
			Deserializer<V> valueDeserializer) {
		return new KafkaConsumer<K, V>(configuration, keyDeserializer, valueDeserializer);
	}

}
