package org.strangeforest.tcb.stats.jobs;

import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;

@Component
@Profile({"jobs", "ssl"})
public class SSLCertificateRenewalJob {

	@Autowired private SSLCertificateRenewalCommand command;

	@Scheduled(cron = "${tennis-stats.jobs.ssl-certificate-renewal:0 0 3 1/15 * *}")
	public void renewSSLCertificate() {
		command.renewSSLCertificate();
	}
}
