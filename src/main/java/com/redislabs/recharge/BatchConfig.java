package com.redislabs.recharge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.recharge.file.FileConfig;
import com.redislabs.recharge.generator.GeneratorConfig;
import com.redislabs.recharge.generator.GeneratorReader;
import com.redislabs.recharge.meter.ProcessorMeter;
import com.redislabs.recharge.meter.ReaderMeter;
import com.redislabs.recharge.meter.WriterMeter;
import com.redislabs.recharge.processor.SpelProcessor;
import com.redislabs.recharge.redis.RedisConfig;
import com.redislabs.recharge.redis.RedisWriter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@SuppressWarnings("rawtypes")
@Slf4j
public class BatchConfig {

	@Autowired
	private RechargeConfiguration config;
	@Autowired
	private JobBuilderFactory jobs;
	@Autowired
	private StepBuilderFactory steps;
	@Autowired
	private FileConfig file;
	@Autowired
	private GeneratorConfig generator;
	@Autowired
	private RedisConfig redis;
	@Autowired
	private WriterMeter<Map> writerMeter;
	@Autowired
	private ReaderMeter<Map> readerMeter;
	@Autowired
	private ProcessorMeter<Map, Map> processorMeter;
	@Autowired
	private StatefulRediSearchConnection<String, String> connection;

	@Bean
	public Job job() throws RechargeException {
		return jobs.get("recharge-job").start(step()).build();
	}

	@Bean
	public Step step() throws RechargeException {
		TaskletStep taskletStep = taskletStep();
		if (config.getPartitions() > 1) {
			IndexedPartitioner partitioner = new IndexedPartitioner(config.getPartitions());
			return steps.get("recharge-step").partitioner("delegate-step", partitioner).step(taskletStep)
					.taskExecutor(new SimpleAsyncTaskExecutor()).build();
		}
		return taskletStep;
	}

	@Bean
	@StepScope
	public AbstractItemCountingItemStreamItemReader<Map> reader() throws RechargeException {
		if (config.getFile() != null) {
			AbstractItemCountingItemStreamItemReader<Map> reader = file.reader();
			if (config.getMaxItemCount() > 0) {
				int maxItemCount = config.getMaxItemCount() / config.getPartitions();
				reader.setMaxItemCount(maxItemCount);
			}
			if (config.getName() == null) {
				config.setName(file.getBaseName());
			}
			return reader;
		}
		if (config.getGenerator() != null) {
			GeneratorReader reader = generator.reader();
			reader.setConnection(connection);
			if (config.getMaxItemCount() > 0) {
				int maxItemCount = config.getMaxItemCount() / config.getPartitions();
				reader.setMaxItemCount(maxItemCount);
			}
			return reader;
		}
		throw new RechargeException("No reader configured");
	}

	@Bean
	@StepScope
	public ItemStreamWriter<Map> writer() throws RechargeException {
		List<RedisWriter<?>> writers = redis.writers();
		if (writers.size() == 1) {
			return writers.get(0);
		}
		CompositeItemWriter<Map> composite = new CompositeItemWriter<>();
		composite.setDelegates(new ArrayList<>(writers));
		return composite;
	}

	@Bean
	public TaskletStep taskletStep() throws RechargeException {
		SimpleStepBuilder<Map, Map> builder = steps.get("main-step").<Map, Map>chunk(50);
		builder.reader(reader());
		if (config.getProcessor() != null) {
			SpelProcessor processor = processor();
			builder.processor(processor);
			builder.listener(new StepExecutionListener() {

				@Override
				public void beforeStep(StepExecution stepExecution) {
					try {
						processor.open();
					} catch (Exception e) {
						log.error("Could not open processor");
					}
				}

				@Override
				public ExitStatus afterStep(StepExecution stepExecution) {
					processor.close();
					return null;
				}
			});
		}
		builder.writer(writer());
		if (config.isMeter()) {
			builder.listener(writerMeter);
			builder.listener(readerMeter);
			builder.listener(processorMeter);
		}
		return builder.build();
	}

	@Bean
	@StepScope
	public SpelProcessor processor() {
		SpelProcessor processor = config.getProcessor().processor();
		processor.setConnection(connection);
		return processor;
	}

}