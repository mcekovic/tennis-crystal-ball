package org.strangeforest.tcb.stats.spring;

import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.coyote.*;
import org.apache.coyote.http11.*;
import org.apache.tomcat.util.descriptor.web.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.web.embedded.tomcat.*;
import org.springframework.boot.web.server.*;
import org.springframework.boot.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;

@Component
@Profile("ssl")
public class SSLTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

	@Autowired private ServerSSLProperties sslProperties;
	private Connector connector;
	private Connector redirectConnector;

	@Override public WebServer getWebServer(ServletContextInitializer... initializers) {
		if (sslProperties.isNativeEnabled()) {
			setProtocol(Http11AprProtocol.class.getName());
			addConnectorCustomizers(this::naiveSSLConnectorCustomizer);
		}
		if (sslProperties.getRedirect().isEnabled())
			addRedirectConnector();
		return super.getWebServer(initializers);
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

	private void naiveSSLConnectorCustomizer(Connector connector) {
		this.connector = connector;
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
		redirectConnector = connector;
		connector.setScheme("http");
		connector.setPort(redirectPort());
		connector.setSecure(false);
		connector.setRedirectPort(port());
		ProtocolHandler protocol = connector.getProtocolHandler();
		if (protocol instanceof AbstractProtocol) {
			AbstractProtocol aProtocol = (AbstractProtocol)protocol;
			sslProperties.customizeRedirectProtocol(aProtocol);
		}
		addAdditionalTomcatConnectors(connector);
	}

	private int port() {
		return getPort() > 0 ? getPort() : 0;
	}

	private int redirectPort() {
		return sslProperties.getRedirect().getFromPort();
	}

	public void restartConnector() throws LifecycleException {
		try {
			stopConnector();
		}
		finally {
			startConnector();
		}
	}

	public void startConnector() throws LifecycleException {
		connector.start();
	}

	public void stopConnector() throws LifecycleException {
		connector.stop();
	}

	public void restartRedirectConnector() throws Exception {
		try {
			stopRedirectConnector();
		}
		finally {
			startRedirectConnector();
		}
	}

	public void startRedirectConnector() throws Exception {
		ProtocolHandler protocolHandler = redirectConnector.getProtocolHandler();
		protocolHandler.init();
		protocolHandler.start();
	}

	public void stopRedirectConnector() throws Exception {
		ProtocolHandler protocolHandler = redirectConnector.getProtocolHandler();
		protocolHandler.stop();
		protocolHandler.destroy();
	}
}
