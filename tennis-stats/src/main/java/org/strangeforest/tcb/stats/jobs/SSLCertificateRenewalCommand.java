package org.strangeforest.tcb.stats.jobs;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.spring.*;

@Component
@Profile("ssl")
public class SSLCertificateRenewalCommand {

	@Autowired private SSLTomcatServletWebServerFactory tomcat;

	@Value("${tennis-stats.jobs.ssl-certificate-renewal.command:./certbot-auto renew --no-random-sleep-on-renew}")
	private String sslCertificateRenewalCommand;

	private static final Logger LOGGER = LoggerFactory.getLogger(SSLCertificateRenewalCommand.class);

	public int renewSSLCertificate() {
		int exitCode = -1;
		try {
			LOGGER.info("Stopping Tomcat redirect connector");
			tomcat.stopRedirectConnector();

			try {
				LOGGER.info("Renewing SSL certificate");
				exitCode = CommandExecutor.execute("SSLCertificateRenewal", sslCertificateRenewalCommand.split(" "));
			}
			finally {
				LOGGER.info("Starting Tomcat redirect connector");
				tomcat.startRedirectConnector();
			}

			if (exitCode == 0) {
				LOGGER.info("Restarting Tomcat SSL connector");
				tomcat.restartConnector();
			}
		}
		catch (Exception ex) {
			LOGGER.error("Error renewing SSL certificate", ex);
		}
		return exitCode;
	}
}
