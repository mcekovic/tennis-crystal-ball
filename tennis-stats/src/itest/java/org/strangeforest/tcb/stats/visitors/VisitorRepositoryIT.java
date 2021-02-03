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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VisitorITsConfig.class, initializers = ConfigFileApplicationContextInitializer.class)
@Transactional
class VisitorRepositoryIT {

	@Autowired private VisitorRepository repository;

	@Test
	void visitorIsCreatedAndFound() {
		var ipAddress = "192.168.1.1";
		var countryId = "SRB";
		var country = "Serbia";
		var agentType = WEB_BROWSER.name();
		var visitor = repository.create(ipAddress, countryId, country, agentType);

		assertThat(visitor.getId()).isPositive();
		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getHits()).isEqualTo(1);
		assertThat(visitor.getFirstHit()).isNotNull();

		var optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		var savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getCountryId()).isEqualTo(countryId);
		assertThat(savedVisitor.getCountry()).isEqualTo(country);
		assertThat(savedVisitor.getAgentType()).isEqualTo(agentType);
		assertThat(savedVisitor.getHits()).isEqualTo(1);
	}

	@Test
	void visitorIsNotFound() {
		var ipAddress = "192.168.1.1";
		var optionalVisitor = repository.find(ipAddress);

		assertThat(optionalVisitor).isEmpty();
	}

	@Test
	void visitorsAreAllFound() {
		var existingVisitors = getExistingVisitors();
		var ipAddress1 = "192.168.1.1";
		repository.create(ipAddress1, "SRB", "Serbia", WEB_BROWSER.name());
		var ipAddress2 = "192.168.1.2";
		repository.create(ipAddress2, "USA", "United States", MOBILE_BROWSER.name());

		var visitors = repository.findAll();

		assertThat(visitors).hasSize(existingVisitors + 2);
		assertThat(visitors).extracting(Visitor::getIpAddress).contains(ipAddress1, ipAddress2);
	}

	@Test
	void visitorIsSaved() {
		var ipAddress = "192.168.1.1";
		var visitor = repository.create(ipAddress, "SRB", "Serbia", WEB_BROWSER.name());

		visitor.visit();
		repository.save(visitor);

		var optionalVisitor = repository.find(ipAddress);
		assertThat(optionalVisitor).isNotEmpty();
		assertThat(optionalVisitor.get().getHits()).isEqualTo(2);
	}

	@Test
	void allVisitorAreSaved() {
		var existingVisitors = getExistingVisitors();
		var ipAddress1 = "192.168.1.1";
		var visitor1 = repository.create(ipAddress1, "SRB", "Serbia", WEB_BROWSER.name());
		var ipAddress2 = "192.168.1.2";
		var visitor2 = repository.create(ipAddress2, "USA", "United States", MOBILE_BROWSER.name());

		visitor1.visit();
		visitor2.visit();
		visitor2.visit();
		repository.saveAll(List.of(visitor1, visitor2));

		var visitors = repository.findAll();
		assertThat(visitors).hasSize(existingVisitors + 2);
		assertThat(visitors).extracting(Visitor::getHits).contains(2, 3);
	}

	@Test
	void visitorIsExpired() {
		var ipAddress = "192.168.1.1";
		var visitor = repository.create(ipAddress, "SRB", "Serbia", WEB_BROWSER.name());
		var optionalVisitor = repository.find(ipAddress);
		assertThat(optionalVisitor).isNotEmpty();

		repository.expire(visitor);

		var optionalExpiredVisitor = repository.find(ipAddress);
		assertThat(optionalExpiredVisitor).isEmpty();
	}

	private int getExistingVisitors() {
		return repository.findAll().size();
	}
}
