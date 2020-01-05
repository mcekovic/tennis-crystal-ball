package org.strangeforest.tcb.dataload

import org.junit.jupiter.api.*
import org.testcontainers.containers.*
import org.testcontainers.utility.MountableFile

import static org.junit.jupiter.api.MethodOrderer.*
import static org.junit.jupiter.api.TestInstance.*

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class CreateDBIT {

	PostgreSQLContainer container
	ATPTennisLoader loader
	SqlPool sqlPool

	@BeforeAll
	void setUp() {
		container = new PostgreSQLContainer<>('postgres')
		container.start()
		System.setProperty(SqlPool.DB_URL_PROPERTY, container.jdbcUrl)
		System.setProperty(SqlPool.USERNAME_PROPERTY, container.username)
		System.setProperty(SqlPool.PASSWORD_PROPERTY, container.password)
		loader = new ATPTennisLoader()
		sqlPool = new SqlPool()
	}

	@AfterAll
	void tearDown() {
		container?.stop();
	}

	@Test @Order(1)
	void 'Install Extensions'() {
		sqlPool.withSql { sql -> loader.installExtensions(sql) }
	}

	@Test @Order(2)
	void 'Create Database'() {
		sqlPool.withSql { sql -> loader.createDatabase(sql) }
	}

	@Test @Order(3)
	void 'Drop Database'() {
		sqlPool.withSql { sql -> loader.dropDatabase(sql) }
	}
}
