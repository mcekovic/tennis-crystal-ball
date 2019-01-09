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

import static com.google.common.base.Strings.*;

@Component
@Profile("ssl")
public class SSLTomcatServletWebServerFactory extends TomcatServletWebServerFactory {

	@Value("${server.http-port:8080}") private int httpPort;
	@Value("${server.ssl.native:false}") private boolean nativeSsl;
	@Value("${server.ssl.certificate-key:}") private String sslCertificateKey;
	@Value("${server.ssl.certificate:}") private String sslCertificate;
	@Value("${server.ssl.certificate-chain:}") private String sslCertificateChain;
	@Value("${server.ssl.ca-certificate:}") private String sslCACertificate;

	@PostConstruct
	public void init() {
		if (nativeSsl) {
			setProtocol(Http11AprProtocol.class.getName());
			addConnectorCustomizers(this::naiveHTTPSConnectorCustomizer);
		}
		addRedirectConnector();
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
		if (protocol instanceof AbstractHttp11Protocol) {
			AbstractHttp11Protocol httpProtocol = (AbstractHttp11Protocol)protocol;
			httpProtocol.setSSLEnabled(true);
			try {
				if (!isNullOrEmpty(sslCertificateKey))
					httpProtocol.setSSLCertificateKeyFile(ResourceUtils.getFile(sslCertificateKey).toString());
				if (!isNullOrEmpty(sslCertificate))
					httpProtocol.setSSLCertificateFile(ResourceUtils.getFile(sslCertificate).toString());
				if (!isNullOrEmpty(sslCertificateChain))
					httpProtocol.setSSLCertificateChainFile(ResourceUtils.getFile(sslCertificateChain).toString());
				if (!isNullOrEmpty(sslCACertificate))
					httpProtocol.setSSLCACertificateFile(ResourceUtils.getFile(sslCACertificate).toString());
			}
			catch (FileNotFoundException ex) {
				throw new WebServerException("Could not load SSL certificate files: " + ex.getMessage(), ex);
			}
			httpProtocol.setSSLDisableCompression(false);
		}
	}

	private void addRedirectConnector() {
		Connector connector = new Connector();
		connector.setScheme("http");
		connector.setPort(httpPort);
		connector.setSecure(false);
		connector.setRedirectPort(getPort() > 0 ? getPort() : 0);
		addAdditionalTomcatConnectors(connector);
	}
}
