package com.redislabs.riot.redis.writer;

import org.springframework.batch.item.support.AbstractItemStreamItemWriter;
import org.springframework.util.ClassUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRedisItemWriter<O> extends AbstractItemStreamItemWriter<O> {

	protected @Setter CommandWriter<O> writer;

	protected AbstractRedisItemWriter(CommandWriter<O> writer) {
		this.writer = writer;
	}

	public AbstractRedisItemWriter() {
		setName(ClassUtils.getShortName(this.getClass()));
	}

	protected void logWriteError(O item, Exception e) {
		if (log.isDebugEnabled()) {
			log.debug("Could not write record {}", item, e);
		} else {
			log.error("Could not write record: {}", e.getMessage());
		}
	}

}
