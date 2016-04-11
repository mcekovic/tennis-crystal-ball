package org.strangeforest.tcb.stats.web;

import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class VisitorManagerTest extends BaseVisitorManagerTest {

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
		Visitor visitor = manager.visit(ipAddress);

		assertThat(visitor.getHits()).isEqualTo(2);

		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(geoIPService);
	}

	@Test
	public void thirdVisitSavesVisitor() {
		setField(manager, "saveEveryHitCount", 3);
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
		manager.visit(ipAddress);
		Visitor visitor = manager.visit(ipAddress);

		assertThat(visitor.getHits()).isEqualTo(3);

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
		assertThat(visitors).extracting(Visitor::getIpAddress).containsExactly(ipAddress1);
		verifyNoMoreInteractions(repository);
	}
}
