package org.strangeforest.tcb.stats.web;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.mockito.verification.*;

import static eu.bitwalker.useragentutils.BrowserType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

abstract class BaseVisitorManagerTest {

	@InjectMocks protected VisitorManager manager;
	@Mock protected VisitorRepository repository;
	@Mock protected GeoIPService geoIPService;
	@Captor protected ArgumentCaptor<Visitor> visitorCaptor;

	@BeforeEach
	void setUp() {
		manager.init();
		verify(repository).findAll();
		verifyNoMoreInteractions(repository);
		reset(repository);
	}

	@AfterEach
	void tearDown() throws InterruptedException {
		manager.destroy();
		verifyNoMoreInteractions(geoIPService);
	}

	Visitor visitAndVerifyFirstVisit(String ipAddress) {
		return visitAndVerifyFirstVisit(ipAddress, times(1));
	}

	Visitor visitAndVerifyFirstVisit(String ipAddress, VerificationMode mode) {
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(any(), any(), any(), any())).thenAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			return new Visitor(1L, (String)args[0], (String)args[1], (String)args[2], (String)args[3], 1, Instant.now());
		});

		Visitor visitor = manager.visit(ipAddress, WEB_BROWSER.name());

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getHits()).isEqualTo(1);

		verify(repository, mode).find(ipAddress);
		verify(repository, mode).create(matches(ipAddress), any(), any(), any());
		verifyNoMoreInteractions(repository);
		verify(geoIPService, mode).getCountry(ipAddress);
		verifyNoMoreInteractions(geoIPService);

		return visitor;
	}
}
