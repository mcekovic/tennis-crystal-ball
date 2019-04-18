package org.strangeforest.tcb.stats.spring;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.boot.web.embedded.tomcat.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.*;

import com.google.common.base.*;

@Component @Endpoint(id = "restart")
@Profile("!dev")
public class RestartEndpoint {

	@Autowired private TomcatServletWebServerFactory tomcat;

	private static final Logger LOGGER = LoggerFactory.getLogger(RestartEndpoint.class);

	@WriteOperation
	public String restart() {
		TennisStatsApplication.restart();
		return "Restart initiated";
	}

	@WriteOperation
	public String restartConnector() {
		if (tomcat instanceof SSLTomcatServletWebServerFactory) {
			try {
				SSLTomcatServletWebServerFactory sslTomcat = (SSLTomcatServletWebServerFactory)tomcat;
				sslTomcat.restartConnector();
				sslTomcat.restartRedirectConnector();
				return "HTTP and HTTPS connectors restarted";
			}
			catch (Exception ex) {
				String message = "Error restarting connector: " + Throwables.getRootCause(ex).getMessage();
				LOGGER.error(message, ex);
				return message;
			}
		}
		else
			return "Connector restart not supported";
	}
}