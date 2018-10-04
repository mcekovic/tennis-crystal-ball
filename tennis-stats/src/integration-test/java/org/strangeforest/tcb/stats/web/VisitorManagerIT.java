package org.strangeforest.tcb.stats.web;

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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VisitorITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
@Transactional
class VisitorManagerIT {

	@Autowired private VisitorManager manager;
	@Autowired private VisitorRepository repository;

	@AfterEach
	void tearDown() {
		manager.clearCache();
	}

	@Test
	void firstVisitCreatesVisitor() {
		String ipAddress = "178.148.80.189";

		Visitor visitor = manager.visit(ipAddress, WEB_BROWSER.name());

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getCountry()).isEqualTo("Serbia");
		assertThat(visitor.getHits()).isEqualTo(1);
	}

	@Test
	void secondVisitIncrementHitsButDoesNotSaveVisitor() {
		String ipAddress = "178.148.80.189";
		manager.visit(ipAddress, WEB_BROWSER.name());

		Visitor visitor = manager.visit(ipAddress, WEB_BROWSER.name());
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getHits()).isEqualTo(2);

		Optional<Visitor> optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		Visitor savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(1);
	}

	@Test
	void thirdVisitIncrementHitsButAndSaveVisitor() {
		String ipAddress = "178.148.80.189";
		manager.visit(ipAddress, WEB_BROWSER.name());
		manager.visit(ipAddress, WEB_BROWSER.name());

		Visitor visitor = manager.visit(ipAddress, WEB_BROWSER.name());
		assertThat(visitor.getHits()).isEqualTo(3);

		Optional<Visitor> optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		Visitor savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(3);
	}

	@Test
	void visitorsAreSavedOnExit() throws InterruptedException {
		String ipAddress = "178.148.80.189";
		manager.visit(ipAddress, MOBILE_BROWSER.name());

		Visitor visitor = manager.visit(ipAddress, MOBILE_BROWSER.name());
		assertThat(visitor.getHits()).isEqualTo(2);

		Optional<Visitor> optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		Visitor savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(1);

		manager.destroy();

		optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getHits()).isEqualTo(2);
	}
}
