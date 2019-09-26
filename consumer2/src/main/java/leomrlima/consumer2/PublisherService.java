package leomrlima.consumer2;

import org.aerogear.kafka.SimpleKafkaProducer;
import org.aerogear.kafka.cdi.annotation.Producer;

import leomrlima.consumer2.config.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;

@ApplicationScoped
public class PublisherService {

    @Producer
    private SimpleKafkaProducer<String, JsonObject> producer;

    public <T> void sendMessage(T entity) {
        producer.send(Config.TOPIC, JsonUtils.toJson(entity));
    }

}
