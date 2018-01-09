package com.demo.utils.http;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class DistTrustManager implements TrustManager, X509TrustManager {
	public DistTrustManager() {}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public boolean isServerTrusted(X509Certificate[] certs) {
		return true;
	}

	public boolean isClientTrusted(X509Certificate[] certs) {
		return true;
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType)
			throws CertificateException {
		return;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType)
			throws CertificateException {
		return;
	}

}
