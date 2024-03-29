package leomrlima.consumer2;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.eclipse.jnosql.diana.redis.keyvalue.RedisBucketManagerFactory;
import org.eclipse.jnosql.diana.redis.keyvalue.RedisConfiguration;

import jakarta.nosql.keyvalue.BucketManager;
import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;

@ApplicationScoped
class BucketManagerProducer {

	static final String STATUS_BUCKET = "status";

	private RedisConfiguration configuration;

	private RedisBucketManagerFactory managerFactory;

	@PostConstruct
	void init() {
		configuration = new RedisConfiguration();
		managerFactory = configuration.get();
	}

	@Produces
	@Database(value = DatabaseType.KEY_VALUE, provider = STATUS_BUCKET)
	BucketManager getManagerStatus() {
		return managerFactory.getBucketManager(STATUS_BUCKET);
	}

	void destroy(@Disposes @Any BucketManager manager) {
		manager.close();
	}

}
