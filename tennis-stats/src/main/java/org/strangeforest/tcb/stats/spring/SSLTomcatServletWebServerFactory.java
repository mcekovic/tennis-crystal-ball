package org.strangeforest.tcb.stats.spring;

import javax.annotation.*;

import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.coyote.*;
import org.apache.coyote.http11.*;
import org.apache.tomcat.util.descriptor.web.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.embedded.tomcat.*;
import org.springframework.boot.web.server.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

@Component
@Profile("ssl")
public class SSLTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

	@Autowired private ServerSSLProperties sslProperties;

	@PostConstruct
	public void init() {
		if (sslProperties.isNativeEnabled()) {
			setProtocol(Http11AprProtocol.class.getName());
			addConnectorCustomizers(this::naiveHTTPSConnectorCustomizer);
		}
		addRedirectConnector();
	}

	@Override public Ssl getSsl() {
		return sslProperties.isNativeEnabled() ? null : super.getSsl();
	}

	@Override protected void postProcessContext(Context context) {
		SecurityConstraint securityConstraint = new SecurityConstraint();
		securityConstraint.setUserConstraint("CONFIDENTIAL");
		SecurityCollection collection = new SecurityCollection();
		collection.addPattern("/*");
		securityConstraint.addCollection(collection);
		context.addConstraint(securityConstraint);
	}

	private void naiveHTTPSConnectorCustomizer(Connector connector) {
		connector.setScheme("https");
		connector.setSecure(true);
		ProtocolHandler protocol = connector.getProtocolHandler();
		if (protocol instanceof AbstractHttp11Protocol) {
			AbstractHttp11Protocol httpProtocol = (AbstractHttp11Protocol)protocol;
			httpProtocol.setSSLEnabled(true);
			httpProtocol.setSSLDisableCompression(false);
			sslProperties.customizeProtocol(httpProtocol);
		}
	}

	private void addRedirectConnector() {
		Connector connector = new Connector();
		connector.setScheme("http");
		connector.setPort(sslProperties.getRedirectFromPort());
		connector.setSecure(false);
		connector.setRedirectPort(getPort() > 0 ? getPort() : 0);
		addAdditionalTomcatConnectors(connector);
	}
}
