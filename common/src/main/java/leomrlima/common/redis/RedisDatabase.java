package leomrlima.common.redis;

import java.util.function.Function;

public class RedisDatabase<V> extends RedisMap<String, V> {

	private final Function<V, String> keyFunction;

	public RedisDatabase(String namespace, Function<V, String> keyFunction) {
		super(namespace);
		this.keyFunction = keyFunction;
	}
	
	public void set(V value) {
		set(keyFunction.apply(value), value);
	}

	public void deleteValue(V value) {
		delete(keyFunction.apply(value));
	}
}
