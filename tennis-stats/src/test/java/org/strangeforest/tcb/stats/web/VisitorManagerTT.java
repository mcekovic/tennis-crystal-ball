package org.strangeforest.tcb.stats.web;

import java.time.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VisitorManagerTT extends BaseVisitorManagerTest {

	@Test
	public void whenVisitorIsExpiredVisitBySameIPAddressCreatesNewVisitor() throws InterruptedException {
		manager.setExpiryCheckPeriod(Duration.ofMillis(50L));
		manager.setExpiryTimeout(Duration.ofMillis(50L));
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);

		Thread.sleep(200L);

		visitAndVerifyFirstVisit(ipAddress);

		verifyNoMoreInteractions(repository);
	}
}
