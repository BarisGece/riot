
package com.redislabs.recharge.redis.writers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.redislabs.lettusearch.RediSearchAsyncCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.recharge.RechargeConfiguration.AbstractRedisConfiguration;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public abstract class AbstractPipelineRedisWriter<T extends AbstractRedisConfiguration> extends AbstractRedisWriter<T> {

	protected AbstractPipelineRedisWriter(T config) {
		super(config);
	}

	@Override
	public void write(List<? extends Map> records) throws Exception {
		List<RedisFuture<?>> futures = new ArrayList<>();
		StatefulRediSearchConnection<String, String> connection = getConnection();
		try {
			RediSearchAsyncCommands<String, String> commands = connection.async();
			commands.setAutoFlushCommands(false);
			for (Map record : records) {
				RedisFuture<?> future = write(getKey(record), record, commands);
				if (future != null) {
					futures.add(future);
				}
			}
			commands.flushCommands();
			boolean result = LettuceFutures.awaitAll(5, TimeUnit.SECONDS,
					futures.toArray(new RedisFuture[futures.size()]));
			if (result) {
				log.debug("Wrote {} records", records.size());
			} else {
				log.warn("Could not write {} records", records.size());
				for (RedisFuture<?> future : futures) {
					if (future.getError() != null) {
						log.error(future.getError());
					}
				}
			}
		} finally {
			release(connection);
		}
	}

	protected abstract RedisFuture<?> write(String key, Map record, RediSearchAsyncCommands<String, String> commands);

}
