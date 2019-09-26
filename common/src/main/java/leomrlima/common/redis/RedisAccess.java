package leomrlima.common.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisAccess {

	private static enum AccessType {
		SINGLE, CLUSTER;
	}

	private static final Logger logger = LoggerFactory.getLogger(RedisAccess.class);

	private static final RedisAccess INSTANCE = new RedisAccess();

	public static RedisAccess getInstance() {
		return INSTANCE;
	}

	private final RedissonClient client;

	public RedisAccess() {
		Config config = new Config();

		AccessType type = AccessType.SINGLE;
		try {
			if (System.getenv("REDIS_ACCESS_TYPE") != null) {
				type = AccessType.valueOf(System.getenv("REDIS_ACCESS_TYPE"));
			}
		} catch (Exception e) {
			logger.error("Error reading Redis Access Type. Configuration must be one of {}. Assuming default {}",
					AccessType.values(), type, e);
		}

		try {
			switch (type) {
			case CLUSTER:
				config.useClusterServers().addNodeAddress(System.getenv("REDIS_BROKERS").split("\\;"));
				break;
			case SINGLE:
				config.useSingleServer().setAddress(System.getenv("REDIS_BROKERS"));
				break;
			}

			config.setCodec(org.redisson.codec.JsonJacksonCodec.INSTANCE);
			logger.debug("Redis config: {}", config.toJSON());
		} catch (Exception e1) {
			logger.error("Configuring Redis", e1);
			throw new IllegalStateException(e1);
		}

		client = Redisson.create(config);
	}

	public RedissonClient getClient() {
		return client;
	}
}
