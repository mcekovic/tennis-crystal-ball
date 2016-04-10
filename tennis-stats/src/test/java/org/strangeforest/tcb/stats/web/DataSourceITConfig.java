package org.strangeforest.tcb.stats.web;

import javax.sql.*;

import org.postgresql.ds.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.*;

@Configuration
public class DataSourceITConfig {

	@Value("${test.datasource.url}")
	private String dataSourceUrl;

	@Value("${test.datasource.username}")
	private String dataSourceUsername;

	@Value("${test.datasource.password}")
	private String dataSourcePassword;

	@Bean
	public DataSource dataSource() {
		PGPoolingDataSource dataSource = new PGPoolingDataSource();
		dataSource.setDataSourceName("Test");
		dataSource.setUrl(dataSourceUrl);
		dataSource.setUser(dataSourceUsername);
		dataSource.setPassword(dataSourcePassword);
		return dataSource;
	}

	@Bean
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
		return new NamedParameterJdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager txManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
