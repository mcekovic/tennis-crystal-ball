package org.strangeforest.tcb.stats.web;

import java.time.*;
import java.util.*;

import org.junit.*;
import org.mockito.*;

import static org.mockito.Mockito.*;

public abstract class BaseVisitorManagerTest {

	@InjectMocks protected VisitorManager manager;
	@Mock protected VisitorRepository repository;

	@Before
	public void setUp() {
		manager.init();
		verify(repository).findAll();
		verifyNoMoreInteractions(repository);
	}

	@After
	public void tearDown() throws InterruptedException {
		manager.destroy();
	}

	protected void visitAndVerifyFirstVisit(String ipAddress) {
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(ipAddress)).thenReturn(new Visitor(1L, ipAddress, 1, Instant.now()));

		manager.visit(ipAddress);

		verify(repository).find(ipAddress);
		verify(repository).create(ipAddress);
		verifyNoMoreInteractions(repository);
	}
}
