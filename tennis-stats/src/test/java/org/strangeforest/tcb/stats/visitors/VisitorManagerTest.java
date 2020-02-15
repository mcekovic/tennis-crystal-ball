package org.strangeforest.tcb.stats.visitors;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import static eu.bitwalker.useragentutils.BrowserType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class VisitorManagerTest extends BaseVisitorManagerTest {

	@Captor ArgumentCaptor<Collection<Visitor>> visitorsCaptor;

	@Test
	void managerIsInitialized() {
		verifyNoMoreInteractions(repository);
	}

	@Test
	void firstVisitCreatesVisitor() {
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
	}

	@Test
	void secondVisitDoesNothing() {
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
		Visit visit = manager.visit(ipAddress, WEB_BROWSER.name());

		assertThat(visit.visitor.getHits()).isEqualTo(2);

		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(geoIPService);
	}

	@Test
	void thirdVisitSavesVisitor() {
		setField(manager, "saveEveryHitCount", 3);
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);
		manager.visit(ipAddress, WEB_BROWSER.name());
		Visit visit = manager.visit(ipAddress, WEB_BROWSER.name());

		assertThat(visit.visitor.getHits()).isEqualTo(3);

		verify(repository).save(visitorCaptor.capture());
		assertThat(visitorCaptor.getValue().getIpAddress()).isEqualTo(ipAddress);
		verifyNoMoreInteractions(repository);
	}


	@Test
	void onlyUnsavedVisitorsAreSavedOnDestroy() throws InterruptedException {
		String ipAddress1 = "192.168.1.1";
		String ipAddress2 = "192.168.1.2";

		visitAndVerifyFirstVisit(ipAddress1);
		visitAndVerifyFirstVisit(ipAddress2);
		manager.visit(ipAddress1, WEB_BROWSER.name());

		verifyNoMoreInteractions(repository);

		manager.destroy();

		verify(repository).saveAll(visitorsCaptor.capture());
		verify(repository).findAll();
		Collection<Visitor> visitors = visitorsCaptor.getValue();
		assertThat(visitors).hasSize(1);
		assertThat(visitors).extracting(Visitor::getIpAddress).containsExactly(ipAddress1);
		verifyNoMoreInteractions(repository);
	}
}
