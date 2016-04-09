package org.strangeforest.tcb.stats.web;

import java.time.*;
import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VisitorManagerTest {

	@InjectMocks private VisitorManager manager;
	@Mock VisitorRepository repository;

	@Before
	public void setUp() {
		manager.init();
		verify(repository).findAll();
	}

	@After
	public void tearDown() throws InterruptedException {
		manager.destroy();
	}

	@Test
	public void managerIsInitialized() {
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void firstVisitCreatesVisitor() {
		String ipAddress = "192.168.1.1";
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(ipAddress)).thenReturn(new Visitor(1L, ipAddress, 1, Instant.now()));

		manager.visit(ipAddress);

		verify(repository).find(ipAddress);
		verify(repository).create(ipAddress);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void secondVisitDoesNothing() {
		String ipAddress = "192.168.1.1";
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(ipAddress)).thenReturn(new Visitor(1L, ipAddress, 1, Instant.now()));

		manager.visit(ipAddress);

		verify(repository).find(ipAddress);
		verify(repository).create(ipAddress);

		manager.visit(ipAddress);

		verifyNoMoreInteractions(repository);
	}

	@Test
	public void thirdVisitSavesVisitor() {
		manager.setSaveAfterVisitCount(3);
		String ipAddress = "192.168.1.1";
		when(repository.find(ipAddress)).thenReturn(Optional.empty());
		when(repository.create(ipAddress)).thenReturn(new Visitor(1L, ipAddress, 1, Instant.now()));

		manager.visit(ipAddress);

		verify(repository).find(ipAddress);
		verify(repository).create(ipAddress);

		manager.visit(ipAddress);
		manager.visit(ipAddress);

		verify(repository).save(any());
		verifyNoMoreInteractions(repository);
	}
}
