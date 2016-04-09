package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VisitorManagerTest extends BaseVisitorManagerTest {

	@Captor ArgumentCaptor<Visitor> visitorCaptor;
	@Captor ArgumentCaptor<Collection<Visitor>> visitorsCaptor;

	@Test
	public void managerIsInitialized() {
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void firstVisitCreatesVisitor() {
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
	}

	@Test
	public void secondVisitDoesNothing() {
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
		manager.visit(ipAddress);

		verifyNoMoreInteractions(repository);
	}

	@Test
	public void thirdVisitSavesVisitor() {
		manager.setSaveAfterVisitCount(3);
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
		manager.visit(ipAddress);
		manager.visit(ipAddress);

		verify(repository).save(visitorCaptor.capture());
		assertThat(visitorCaptor.getValue().getIpAddress()).isEqualTo(ipAddress);
		verifyNoMoreInteractions(repository);
	}


	@Test
	public void onlyUnsavedVisitorsAreSavedOnDestroy() throws InterruptedException {
		String ipAddress1 = "192.168.1.1";
		String ipAddress2 = "192.168.1.2";

		visitAndVerifyFirstVisit(ipAddress1);
		visitAndVerifyFirstVisit(ipAddress2);
		manager.visit(ipAddress1);

		verifyNoMoreInteractions(repository);

		manager.destroy();

		verify(repository).saveAll(visitorsCaptor.capture());
		Collection<Visitor> visitors = visitorsCaptor.getValue();
		assertThat(visitors).hasSize(1);
		assertThat(visitors.iterator().next().getIpAddress()).isEqualTo(ipAddress1);
		verifyNoMoreInteractions(repository);
	}
}
