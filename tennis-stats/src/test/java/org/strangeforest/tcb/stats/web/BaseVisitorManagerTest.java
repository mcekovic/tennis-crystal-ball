package org.strangeforest.tcb.stats.web;

import java.time.*;
import java.util.*;

import org.junit.After;
import org.junit.*;
import org.mockito.*;
import org.mockito.verification.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class BaseVisitorManagerTest {

	@InjectMocks protected VisitorManager manager;
	@Mock protected VisitorRepository repository;
	@Mock protected GeoIPService geoIPService;

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

	protected Visitor visitAndVerifyFirstVisit(String ipAddress) {
		return visitAndVerifyFirstVisit(ipAddress, times(1));
	}

	protected Visitor visitAndVerifyFirstVisit(String ipAddress, VerificationMode mode) {
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(any(), any(), any())).thenAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			return new Visitor(1L, (String)args[0], (String)args[1], (String)args[2], 1, Instant.now());
		});

		Visitor visitor = manager.visit(ipAddress);

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getHits()).isEqualTo(1);

		verify(repository, mode).find(ipAddress);
		verify(repository, mode).create(matches(ipAddress), any(), any());
		verifyNoMoreInteractions(repository);
		verify(geoIPService, mode).getCountry(ipAddress);
		verifyNoMoreInteractions(geoIPService);

		return visitor;
	}
}
