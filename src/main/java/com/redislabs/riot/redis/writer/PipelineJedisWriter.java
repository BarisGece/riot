package com.redislabs.riot.redis.writer;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.util.Pool;

public class PipelineJedisWriter<O> extends AbstractRedisItemWriter<O> {

	private Pool<Jedis> pool;

	public PipelineJedisWriter(Pool<Jedis> pool) {
		this.pool = pool;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void write(List<? extends O> items) {
		try (Jedis jedis = pool.getResource()) {
			Pipeline p = jedis.pipelined();
			List<Response> responses = new ArrayList<>();
			for (O item : items) {
				try {
					responses.add((Response) writer.write(p, item));
				} catch (Exception e) {
					logWriteError(item, e);
				}
			}
			p.sync();
			for (int index = 0; index < responses.size(); index++) {
				if (responses.get(index) == null) {
					continue;
				}
				try {
					responses.get(index).get();
				} catch (Exception e) {
					logWriteError(items.get(index), e);
				}
			}
		}
	}

	@Override
	public void close() {
		if (pool != null) {
			pool.close();
			pool = null;
		}
		super.close();
	}

}
