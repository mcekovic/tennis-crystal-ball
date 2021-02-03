package org.strangeforest.tcb.stats.visitors;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.transaction.annotation.*;

import static eu.bitwalker.useragentutils.BrowserType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.MethodOrderer.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VisitorITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
@Transactional
@TestMethodOrder(OrderAnnotation.class)
class VisitorManagerIT {

	@Autowired private VisitorManager manager;
	@Autowired private VisitorRepository repository;

	@AfterEach
	void tearDown() {
		manager.clearCache();
	}

	@Test @Order(1)
	void firstVisitCreatesVisitor() {
		var ipAddress = "178.148.80.189";

		var visitor = manager.visit(ipAddress, WEB_BROWSER.name()).visitor;

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getCountry()).isEqualTo("Serbia");
		assertThat(visitor.getHits()).isEqualTo(1);
	}

	@Test @Order(2)
	void secondVisitIncrementHitsButDoesNotSaveVisitor() {
		var ipAddress = "178.148.80.189";
		manager.visit(ipAddress, WEB_BROWSER.name());

		var visitor = manager.visit(ipAddress, WEB_BROWSER.name()).visitor;
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getHits()).isEqualTo(2);

		var optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		var savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(1);
	}

	@Test @Order(3)
	void thirdVisitIncrementHitsButAndSaveVisitor() {
		var ipAddress = "178.148.80.189";
		manager.visit(ipAddress, WEB_BROWSER.name());
		manager.visit(ipAddress, WEB_BROWSER.name());

		var visitor = manager.visit(ipAddress, WEB_BROWSER.name()).visitor;
		assertThat(visitor.getHits()).isEqualTo(3);

		var optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		var savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(3);
	}

	@Test @Order(4)
	void visitorsAreSavedOnExit() throws InterruptedException {
		var ipAddress = "178.148.80.189";
		manager.visit(ipAddress, MOBILE_BROWSER.name());

		var visitor = manager.visit(ipAddress, MOBILE_BROWSER.name()).visitor;
		assertThat(visitor.getHits()).isEqualTo(2);

		var optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		var savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(1);

		manager.destroy();

		optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(2);
	}
}
