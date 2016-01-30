package com.xebia.xtime.shared;

import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import timber.log.Timber;

public abstract class XTimeRequest {

    public static final String CONTENT_TYPE_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * Submits the request. Does preliminary work if required, e.g. circumventing the SSL
     * security, and calls through to #connect() to make the actual HTTP request.
     *
     * @return Response content, or null if the request failed.
     * @throws XTimeAuthenticationException If the request was denied due to invalid session.
     */
    public String submit() throws XTimeAuthenticationException {

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(getUrl());

            // override SSL certificate checks
            if (shouldHackSsl()) {
                trustAllCertificates();
            }

            urlConnection = (HttpURLConnection) url.openConnection();

            // do not follow redirects, we use the Location header to see if the request was OK
            urlConnection.setInstanceFollowRedirects(false);

            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);

            // write request data
            if (!TextUtils.isEmpty(getRequestData())) {
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", getContentType());
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                writeStream(out);
            }

            // request was successful if the Location header does not redirect to an error page
            String location = urlConnection.getHeaderField("Location");
            if (location != null && location.contains("error=true")) {
                throw new XTimeAuthenticationException("Invalid session");
            }

            // read response data
            InputStream in = urlConnection.getInputStream();
            return readStream(in);

        } catch (IOException e) {
            Timber.e(e, "Failed to connect to XTime");
            return null;
        } catch (GeneralSecurityException e) {
            Timber.e(e, "Failed to set custom SSL certificate manager");
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private boolean shouldHackSsl() {
        // TODO: Create preference to toggle SSL hack
        return false;
    }

    /**
     * Disables checks on security certificates for HttpsUrlConnections. This seems to be the
     * only way to get around issues with expired/untrusted certificates.
     */
    private void trustAllCertificates() throws GeneralSecurityException {

        // Create a trust manager that does not validate certificate chains
        TrustManager trustAllCerts = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // do not check
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // do not check
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{trustAllCerts}, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        Timber.w("Careful: SSL certificate checks are disabled!");
    }

    private void writeStream(OutputStream out) throws IOException {
        out.write(getRequestData().getBytes());
        out.flush();
        out.close();
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8196);
        String line;
        StringBuilder responseContent = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            responseContent.append(line);
        }
        return responseContent.toString();
    }

    /**
     * @return The content type of the request data, or <code>null</code> if there is no data to
     * be transmitted.
     */
    public abstract String getContentType();

    /**
     * @return The request data, or <code>null</code> if there is no data to be transmitted.
     */
    public abstract String getRequestData();

    /**
     * @return The URL to connect to.
     */
    public abstract String getUrl();
}
