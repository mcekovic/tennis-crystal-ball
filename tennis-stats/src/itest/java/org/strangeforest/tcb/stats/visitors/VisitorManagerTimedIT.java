package org.strangeforest.tcb.stats.visitors;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class VisitorManagerTimedIT extends BaseVisitorManagerTest {

	@BeforeEach
	@Override public void setUp() {
		setField(manager, "expiryPeriod", Duration.ofSeconds(1L));
		setField(manager, "expiryCheckPeriod", Duration.ofSeconds(1L));
		super.setUp();
	}

	@Test
	void whenVisitorIsExpiredVisitBySameIPAddressCreatesNewVisitor() throws InterruptedException {
		var ipAddress = "192.168.1.1";

		var visitor = visitAndVerifyFirstVisit(ipAddress);

		when(repository.findAll()).thenReturn(List.of(visitor));
		Thread.sleep(2000L);

		verify(repository, atLeast(1)).findAll();
		verify(repository, atLeast(1)).expire(visitorCaptor.capture());
		assertThat(visitorCaptor.getValue().getIpAddress()).isEqualTo(ipAddress);

		visitAndVerifyFirstVisit(ipAddress, times(2));
	}
}
