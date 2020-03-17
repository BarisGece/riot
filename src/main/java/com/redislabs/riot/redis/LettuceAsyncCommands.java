package com.redislabs.riot.redis;

import java.util.List;
import java.util.Map;

import com.redislabs.lettusearch.RediSearchAsyncCommands;
import com.redislabs.lettusearch.search.AddOptions;

import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.async.RedisGeoAsyncCommands;
import io.lettuce.core.api.async.RedisHashAsyncCommands;
import io.lettuce.core.api.async.RedisKeyAsyncCommands;
import io.lettuce.core.api.async.RedisListAsyncCommands;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import io.lettuce.core.api.async.RedisSetAsyncCommands;
import io.lettuce.core.api.async.RedisSortedSetAsyncCommands;
import io.lettuce.core.api.async.RedisStreamAsyncCommands;
import io.lettuce.core.api.async.RedisStringAsyncCommands;

@SuppressWarnings("unchecked")
public class LettuceAsyncCommands implements RedisCommands<Object> {

	@Override
	public Object del(Object redis, String... keys) {
		return ((RedisKeyAsyncCommands<String, String>) redis).del(keys);
	}

	@Override
	public Object geoadd(Object redis, String key, double longitude, double latitude, String member) {
		return ((RedisGeoAsyncCommands<String, String>) redis).geoadd(key, longitude, latitude, member);
	}

	@Override
	public Object hmset(Object redis, String key, Map<String, String> map) {
		return ((RedisHashAsyncCommands<String, String>) redis).hmset(key, map);
	}

	@Override
	public Object xadd(Object redis, String key, Map<String, String> map) {
		return ((RedisStreamAsyncCommands<String, String>) redis).xadd(key, map);
	}

	@Override
	public Object xadd(Object redis, String key, String id, Map<String, String> map, long maxlen,
			boolean approximateTrimming) {
		return ((RedisStreamAsyncCommands<String, String>) redis).xadd(key,
				new XAddArgs().id(id).maxlen(maxlen).approximateTrimming(approximateTrimming), map);
	}

	@Override
	public Object xadd(Object redis, String key, Map<String, String> map, long maxlen, boolean approximateTrimming) {
		return ((RedisStreamAsyncCommands<String, String>) redis).xadd(key,
				new XAddArgs().maxlen(maxlen).approximateTrimming(approximateTrimming), map);
	}

	@Override
	public Object xadd(Object redis, String key, String id, Map<String, String> map) {
		return ((RedisStreamAsyncCommands<String, String>) redis).xadd(key, new XAddArgs().id(id), map);
	}

	@Override
	public Object xadd(Object redis, String key, List<String> ids, List<Map<String, String>> maps) {
		throw new UnsupportedOperationException("Multi-message xadd not supported");
	}

	@Override
	public Object zadd(Object redis, String key, double score, String member) {
		return ((RedisSortedSetAsyncCommands<String, String>) redis).zadd(key, score, member);
	}

	@Override
	public Object zadd(Object redis, String key, List<ScoredValue<String>> scoredValues) {
		return ((RedisSortedSetAsyncCommands<String, String>) redis).zadd(key, scoredValues);
	}

	@Override
	public Object set(Object redis, String key, String value) {
		return ((RedisStringAsyncCommands<String, String>) redis).set(key, value);
	}

	@Override
	public Object sadd(Object redis, String key, String... members) {
		return ((RedisSetAsyncCommands<String, String>) redis).sadd(key, members);
	}

	@Override
	public Object rpush(Object redis, String key, String... members) {
		return ((RedisListAsyncCommands<String, String>) redis).rpush(key, members);
	}

	@Override
	public Object lpush(Object redis, String key, String... members) {
		return ((RedisListAsyncCommands<String, String>) redis).lpush(key, members);
	}

	@Override
	public Object expire(Object redis, String key, long timeout) {
		return ((RedisKeyAsyncCommands<String, String>) redis).expire(key, timeout);
	}

	@Override
	public Object evalsha(Object redis, String sha, ScriptOutputType type, String[] keys, String[] args) {
		return ((RedisScriptingAsyncCommands<String, String>) redis).evalsha(sha, type, keys, args);
	}

	@Override
	public Object ftadd(Object redis, String index, String docId, double score, Map<String, String> map,
			AddOptions options) {
		return ((RediSearchAsyncCommands<String, String>) redis).add(index, docId, score, map, options);
	}

	@Override
	public Object ftadd(Object redis, String index, String docId, double score, Map<String, String> map,
			AddOptions options, String payload) {
		return ((RediSearchAsyncCommands<String, String>) redis).add(index, docId, score, map, options, payload);
	}

	@Override
	public Object sugadd(Object redis, String index, String string, double score, boolean increment) {
		return ((RediSearchAsyncCommands<String, String>) redis).sugadd(index, string, score, increment);
	}

	@Override
	public Object sugadd(Object redis, String index, String string, double score, boolean increment, String payload) {
		return ((RediSearchAsyncCommands<String, String>) redis).sugadd(index, string, score, increment, payload);
	}

	@Override
	public Object restore(Object redis, String key, byte[] value, long ttl, boolean replace) {
		RestoreArgs args = new RestoreArgs().ttl(ttl).replace(replace);
		return ((RedisKeyAsyncCommands<String, String>) redis).restore(key, value, args);
	}
}
