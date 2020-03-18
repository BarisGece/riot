package com.redislabs.riot.cli.db;

import java.util.Map;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import picocli.CommandLine.Option;

public class DatabaseImportOptions extends DatabaseOptions {

	@Option(names = "--sql", description = "SELECT statement", paramLabel = "<sql>")
	private String sql;
	@Option(names = "--fetch", description = "Number of rows to return with each fetch", paramLabel = "<size>")
	private Integer fetchSize;
	@Option(names = "--rows", description = "Max number of rows the ResultSet can contain", paramLabel = "<count>")
	private Integer maxRows;
	@Option(names = "--timeout", description = "The time in milliseconds for the query to timeout", paramLabel = "<ms>")
	private Integer queryTimeout;
	@Option(names = "--shared-connection", description = "Use same conn for cursor and other processing")
	private boolean useSharedExtendedConnection;
	@Option(names = "--verify", description = "Verify position of ResultSet after RowMapper")
	private boolean verifyCursorPosition;

	public JdbcCursorItemReader<Map<String, Object>> reader() throws Exception {
		JdbcCursorItemReaderBuilder<Map<String, Object>> builder = new JdbcCursorItemReaderBuilder<Map<String, Object>>();
		builder.dataSource(dataSource());
		if (fetchSize != null) {
			builder.fetchSize(fetchSize);
		}
		if (maxRows != null) {
			builder.maxRows(maxRows);
		}
		builder.name("database-reader");
		if (queryTimeout != null) {
			builder.queryTimeout(queryTimeout);
		}
		builder.rowMapper(new ColumnMapRowMapper());
		builder.sql(sql);
		builder.useSharedExtendedConnection(useSharedExtendedConnection);
		builder.verifyCursorPosition(verifyCursorPosition);
		JdbcCursorItemReader<Map<String, Object>> reader = builder.build();
		reader.afterPropertiesSet();
		return reader;
	}

}
