package leomrlima.common.redis;

import java.util.Optional;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisMap<K, V> {

	private static final String NAMESPACE_SEPARATOR = ":";

	private static final Logger logger = LoggerFactory.getLogger(RedisMap.class);

	private final String namespace;

	public RedisMap(String namespace) {
		this.namespace = namespace;
	}

	private RedissonClient getClient() {
		return RedisAccess.getInstance().getClient();
	}
	
	public Optional<V> get(K key) {
		RBucket<V> bucket = getClient().getBucket(getInternalKey(key));
		if (bucket.isExists()) {
			if (logger.isDebugEnabled()) {
				logger.debug("loaded {}: {}", getInternalKey(key), key.hashCode());
			}
			return Optional.ofNullable(bucket.get());
		} else {
			return Optional.empty();
		}
	}

	public String getInternalKey(K key) {
		return namespace + NAMESPACE_SEPARATOR + key;
	}

	public void set(K key, V value) {
		if (logger.isDebugEnabled()) {
			logger.debug("set {}: {}", getInternalKey(key), value.hashCode());
		}
		getClient().getBucket(getInternalKey(key)).set(value);
	}

	public void delete(K key) {
		getClient().getKeys().deleteAsync(getInternalKey(key)).thenAccept( count -> {
			logger.debug("deleted {}: {}", getInternalKey(key), count);
		});
	}

	@SuppressWarnings("unchecked")
	public Optional<V> getAndDelete(K key) {
		Optional<V> returned = Optional.ofNullable((V) getClient().getBucket(getInternalKey(key)).getAndDelete());
		if (returned.isPresent() && logger.isDebugEnabled()) {
			logger.debug("get and delete: key {} deleted #{}", getInternalKey(key), returned.get().hashCode());
		}
		return returned;
	}
	
	public boolean keyExists(K key) {
		if (key == null) {
			return false;
		}
		return getClient().getKeys().countExists(getInternalKey(key)) > 0;
	}
}
