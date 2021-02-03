package org.strangeforest.tcb.stats.spring;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.jobs.*;

import static java.lang.String.*;

@Component @Endpoint(id = "ssl")
@Profile("ssl")
public class SSLEndpoint {

	@Autowired private SSLCertificateRenewalCommand command;

	@WriteOperation
	public String renewSSLCertificate() {
		var exitCode = command.renewSSLCertificate();
		return format("SSL certificate renewed [%1$s]", exitCode);
	}
}