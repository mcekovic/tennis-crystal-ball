package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = VisitorITsConfig.class)
@Transactional
public class VisitorManagerIT {

	@Autowired private VisitorManager manager;
	@Autowired private VisitorRepository repository;

	@After
	public void tearDown() throws Exception {
		manager.clearCache();
	}

	@Test
	public void firstVisitCreatesVisitor() {
		String ipAddress = "178.148.80.189";

		Visitor visitor = manager.visit(ipAddress);

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getCountry()).isEqualTo("Serbia");
		assertThat(visitor.getVisits()).isEqualTo(1);
	}

	@Test
	public void secondVisitIncrementVisitsButDoesNotSaveVisitor() {
		String ipAddress = "178.148.80.189";
		manager.visit(ipAddress);

		Visitor visitor = manager.visit(ipAddress);
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getVisits()).isEqualTo(2);

		Optional<Visitor> optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		Visitor savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getVisits()).isEqualTo(1);
	}

	@Test
	public void thirdVisitIncrementVisitsButAndSaveVisitor() {
		String ipAddress = "178.148.80.189";
		manager.visit(ipAddress);
		manager.visit(ipAddress);

		Visitor visitor = manager.visit(ipAddress);
		assertThat(visitor.getVisits()).isEqualTo(3);

		Optional<Visitor> optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		Visitor savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getVisits()).isEqualTo(3);
	}

	@Test
	public void visitorsAreSavedOnExit() throws InterruptedException {
		String ipAddress = "178.148.80.189";
		manager.visit(ipAddress);

		Visitor visitor = manager.visit(ipAddress);
		assertThat(visitor.getVisits()).isEqualTo(2);

		Optional<Visitor> optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		Visitor savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getVisits()).isEqualTo(1);

		manager.destroy();

		optionalSavedVisitor = repository.find(ipAddress);
		assertThat(optionalSavedVisitor).isNotEmpty();
		savedVisitor = optionalSavedVisitor.get();
		assertThat(savedVisitor.getVisits()).isEqualTo(2);
	}
}
