package org.strangeforest.tcb.stats.web;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = VisitorITsConfig.class)
@Transactional
public class VisitorManagerIT {

	@Autowired private VisitorManager manager;

	@Test
	public void firstVisitCreatesVisitor() {
		String ipAddress = "178.148.80.189";

		Visitor visitor = manager.visit(ipAddress);

		assertThat(visitor.getIpAddress()).isEqualTo(ipAddress);
		assertThat(visitor.getCountryId()).isEqualTo("SRB");
		assertThat(visitor.getVisits()).isEqualTo(1);
	}
}
