package org.strangeforest.tcb.stats.spring;

import java.io.*;

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
import org.springframework.util.*;

@Component
@Profile("ssl")
public class SSLTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

	@Value("${server.port:8443}") private int port;
	@Value("${server.http-port:8080}") private int httpPort;
	@Value("${server.ssl.native:false}") private boolean nativeSsl;
	@Value("${server.ssl.certificate-key}") private String sslCertificateKey;
	@Value("${server.ssl.certificate}") private String sslCertificate;

	@PostConstruct
	public void init() {
		if (nativeSsl) {
			setProtocol(Http11AprProtocol.class.getName());
			addConnectorCustomizers(this::naiveHTTPSConnectorCustomizer);
		}
		addStandardConnector();
	}

	@Override public Ssl getSsl() {
		return nativeSsl ? null : super.getSsl();
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
		if (protocol instanceof Http11AprProtocol) {
			Http11AprProtocol aprProtocol = (Http11AprProtocol)protocol;
			aprProtocol.setSSLEnabled(true);
			try {
				aprProtocol.setSSLCertificateKeyFile(ResourceUtils.getFile(sslCertificateKey).toString());
				aprProtocol.setSSLCertificateFile(ResourceUtils.getFile(sslCertificate).toString());
			}
			catch (FileNotFoundException ex) {
				throw new WebServerException("Could not load SSL certificate files: " + ex.getMessage(), ex);
			}
		}
	}

	private void addStandardConnector() {
		Connector connector = new Connector();
		connector.setScheme("http");
		connector.setPort(httpPort);
		connector.setSecure(false);
		connector.setRedirectPort(port);
		addAdditionalTomcatConnectors(connector);
	}
}
