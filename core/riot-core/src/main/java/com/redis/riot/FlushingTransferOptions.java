package com.redis.riot;

import java.time.Duration;
import java.util.Optional;

import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.util.Assert;

import com.redis.spring.batch.KeyValue;
import com.redis.spring.batch.reader.LiveRedisItemReaderBuilder;
import com.redis.spring.batch.step.FlushingSimpleStepBuilder;

import picocli.CommandLine;

public class FlushingTransferOptions {

	@CommandLine.Option(names = "--flush-interval", description = "Max duration between flushes (default: ${DEFAULT-VALUE})", paramLabel = "<ms>")
	private long flushInterval = 50;
	@CommandLine.Option(names = "--idle-timeout", description = "Min duration of inactivity to consider transfer complete", paramLabel = "<ms>")
	private Optional<Long> idleTimeout = Optional.empty();

	public void setFlushInterval(Duration flushInterval) {
		Assert.notNull(flushInterval, "Flush interval must not be null");
		this.flushInterval = flushInterval.toMillis();
	}

	public void setIdleTimeout(Duration idleTimeoutDuration) {
		Assert.notNull(idleTimeoutDuration, "Duration must not be null");
		this.idleTimeout = Optional.of(idleTimeoutDuration.toMillis());
	}

	private Duration getFlushInterval() {
		return Duration.ofMillis(flushInterval);
	}

	private Optional<Duration> getIdleTimeout() {
		return idleTimeout.map(Duration::ofMillis);
	}

	public <I, O> FlushingSimpleStepBuilder<I, O> configure(FaultTolerantStepBuilder<I, O> step) {
		FlushingSimpleStepBuilder<I, O> builder = new FlushingSimpleStepBuilder<>(step)
				.flushingInterval(getFlushInterval());
		getIdleTimeout().ifPresent(builder::idleTimeout);
		return builder;
	}

	public <K, V, T extends KeyValue<K, ?>> LiveRedisItemReaderBuilder<K, V, T> configure(
			LiveRedisItemReaderBuilder<K, V, T> reader) {
		return reader.flushingInterval(getFlushInterval()).idleTimeout(getIdleTimeout());
	}

}
