package org.strangeforest.tcb.stats.web;

import java.time.*;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class VisitorManagerTimedIT extends BaseVisitorManagerTest {


	@Before
	@Override public void setUp() {
		setField(manager, "expiryPeriod", Duration.ofSeconds(1L));
		setField(manager, "expiryCheckPeriod", Duration.ofSeconds(1L));
		super.setUp();
	}

	@Test
	public void whenVisitorIsExpiredVisitBySameIPAddressCreatesNewVisitor() throws InterruptedException {
		String ipAddress = "192.168.1.1";

		visitAndVerifyFirstVisit(ipAddress);

		Thread.sleep(3000L);

		verify(repository).expire(visitorCaptor.capture());
		assertThat(visitorCaptor.getValue().getIpAddress()).isEqualTo(ipAddress);

		visitAndVerifyFirstVisit(ipAddress, times(2));
	}
}
