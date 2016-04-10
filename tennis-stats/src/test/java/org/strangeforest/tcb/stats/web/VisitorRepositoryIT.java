package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.transaction.annotation.*;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = VisitorITsConfig.class)
@Transactional
public class VisitorRepositoryIT {

	@Autowired private VisitorRepository repository;

	@Test
	public void visitorIsCreatedAndFound() {
		String ipAddress = "192.168.1.1";
		Visitor visitor = repository.create(ipAddress, "SRB");

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getVisits()).isEqualTo(1);

		Optional<Visitor> optionalVisitor = repository.find(ipAddress);
		assertThat(optionalVisitor).isNotEmpty();
		assertThat(optionalVisitor.get().getVisits()).isEqualTo(1);
	}

	@Test
	public void visitorIsNotFound() {
		String ipAddress = "192.168.1.1";
		Optional<Visitor> optionalVisitor = repository.find(ipAddress);

		assertThat(optionalVisitor).isEmpty();
	}

	@Test
	public void visitorsAreAllFound() {
		String ipAddress1 = "192.168.1.1";
		repository.create(ipAddress1, "SRB");
		String ipAddress2 = "192.168.1.2";
		repository.create(ipAddress2, "SRB");

		List<Visitor> visitors = repository.findAll();

		assertThat(visitors).hasSize(2);
		assertThat(visitors).extracting(Visitor::getIpAddress).containsExactlyInAnyOrder(ipAddress1, ipAddress2);
	}

	@Test
	public void visitorIsSaved() {
		String ipAddress = "192.168.1.1";
		Visitor visitor = repository.create(ipAddress, "SRB");

		visitor.visit();
		repository.save(visitor);

		Optional<Visitor> optionalVisitor = repository.find(ipAddress);
		assertThat(optionalVisitor).isNotEmpty();
		assertThat(optionalVisitor.get().getVisits()).isEqualTo(2);
	}

	@Test
	public void allVisitorAreSaved() {
		String ipAddress1 = "192.168.1.1";
		Visitor visitor1 = repository.create(ipAddress1, "SRB");
		String ipAddress2 = "192.168.1.2";
		Visitor visitor2 = repository.create(ipAddress2, "SRB");

		visitor1.visit();
		visitor2.visit();
		visitor2.visit();
		repository.saveAll(asList(visitor1, visitor2));

		List<Visitor> visitors = repository.findAll();
		assertThat(visitors).hasSize(2);
		assertThat(visitors).extracting(Visitor::getVisits).containsExactlyInAnyOrder(2, 3);
	}

	@Test
	public void visitorIsExpired() {
		String ipAddress = "192.168.1.1";
		Visitor visitor = repository.create(ipAddress, "SRB");

		repository.expire(visitor);

		Optional<Visitor> optionalVisitor = repository.find(ipAddress);
		assertThat(optionalVisitor).isEmpty();
	}
}
