package org.strangeforest.tcb.stats.web;

import java.time.*;
import java.util.*;

import org.junit.*;
import org.junit.After;
import org.mockito.*;
import org.mockito.verification.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class BaseVisitorManagerTest {

	@InjectMocks protected VisitorManager manager;
	@Mock protected VisitorRepository repository;
	@Mock protected GeoIPService geoIPService;
	@Captor protected ArgumentCaptor<Visitor> visitorCaptor;

	@Before
	public void setUp() {
		manager.init();
		verify(repository).findAll();
		verifyNoMoreInteractions(repository);
		reset(repository);
		when(geoIPService.getCountry(any())).thenReturn(Optional.empty());
	}

	@After
	public void tearDown() throws InterruptedException {
		manager.destroy();
		verifyNoMoreInteractions(geoIPService);
	}

	protected void visitAndVerifyFirstVisit(String ipAddress) {
		visitAndVerifyFirstVisit(ipAddress, times(1));
	}

	protected void visitAndVerifyFirstVisit(String ipAddress, VerificationMode mode) {
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(matches(ipAddress), any())).thenReturn(new Visitor(1L, ipAddress, "SRB", 1, Instant.now()));

		Visitor visitor = manager.visit(ipAddress);

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getVisits()).isEqualTo(1);

		verify(repository, mode).find(ipAddress);
		verify(repository, mode).create(matches(ipAddress), any());
		verifyNoMoreInteractions(repository);
		verify(geoIPService, mode).getCountry(ipAddress);
		verifyNoMoreInteractions(geoIPService);
	}
}
