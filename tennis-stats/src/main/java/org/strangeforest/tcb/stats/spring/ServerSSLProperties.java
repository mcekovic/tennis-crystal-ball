package org.strangeforest.tcb.stats.spring;

import java.io.*;

import org.apache.coyote.http11.*;
import org.springframework.boot.context.properties.*;
import org.springframework.boot.web.server.*;
import org.springframework.util.*;

import static com.google.common.base.Strings.*;

@ConfigurationProperties("server.ssl")
public class ServerSSLProperties {

	private int redirectFromPort = DEFAULT_REDIRECT_FROM_PORT;
	private boolean nativeEnabled = false;
	private String certificateKey;
	private String certificate;
	private String certificateChain;
	private String caCertificate;

	private static final int DEFAULT_REDIRECT_FROM_PORT = 8080;

	public int getRedirectFromPort() {
		return redirectFromPort;
	}

	public void setRedirectFromPort(int redirectFromPort) {
		this.redirectFromPort = redirectFromPort;
	}

	public boolean isNativeEnabled() {
		return nativeEnabled;
	}

	public void setNativeEnabled(boolean nativeEnabled) {
		this.nativeEnabled = nativeEnabled;
	}

	public String getCertificateKey() {
		return certificateKey;
	}

	public void setCertificateKey(String certificateKey) {
		this.certificateKey = certificateKey;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getCertificateChain() {
		return certificateChain;
	}

	public void setCertificateChain(String certificateChain) {
		this.certificateChain = certificateChain;
	}

	public String getCaCertificate() {
		return caCertificate;
	}

	public void setCaCertificate(String caCertificate) {
		this.caCertificate = caCertificate;
	}

	public void customizeProtocol(AbstractHttp11Protocol protocol) {
		try {
			if (!isNullOrEmpty(certificateKey))
				protocol.setSSLCertificateKeyFile(ResourceUtils.getFile(certificateKey).toString());
			if (!isNullOrEmpty(certificate))
				protocol.setSSLCertificateFile(ResourceUtils.getFile(certificate).toString());
			if (!isNullOrEmpty(certificateChain))
				protocol.setSSLCertificateChainFile(ResourceUtils.getFile(certificateChain).toString());
			if (!isNullOrEmpty(caCertificate))
				protocol.setSSLCACertificateFile(ResourceUtils.getFile(caCertificate).toString());
		}
		catch (FileNotFoundException ex) {
			throw new WebServerException("Could not load SSL certificate files: " + ex.getMessage(), ex);
		}
	}
}
