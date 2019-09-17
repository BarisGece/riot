package com.redislabs.riot.redis.writer;

import java.util.Map;

import com.redislabs.riot.redis.RedisConverter;

public abstract class RedisMapWriter<C> implements JedisMapWriter, LettuceMapWriter<C> {

	private RedisConverter converter;

	public RedisConverter getConverter() {
		return converter;
	}

	public void setConverter(RedisConverter converter) {
		this.converter = converter;
	}

	protected Map<String, String> stringMap(Map<String, Object> item) {
		return converter.stringMap(item);
	}

	protected <T> T convert(Object source, Class<T> targetType) {
		return converter.convert(source, targetType);
	}

	protected String join(Map<String, Object> item, String[] fields) {
		return converter.join(item, fields);
	}

	protected String key(Map<String, Object> item) {
		return converter.key(item);
	}

}