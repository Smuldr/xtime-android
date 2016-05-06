package com.xebia.xtime.webservice;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.CertificatePinner;
import okio.Buffer;

public final class HttpsHelper {

    private HttpsHelper() {
        // do not instantiate
    }

    public static SSLSocketFactory customSocketFactory() {
        try {
            X509TrustManager trustManager = trustManagerForCertificates(trustedCertificatesInputStream());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an input stream containing one or more certificate PEM files. This implementation just
     * embeds the PEM files in Java strings; most applications will instead read this from a resource
     * file that gets bundled with the application.
     */
    private static InputStream trustedCertificatesInputStream() {
        String comodoRsaCertificationAuthority = ""
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIF2DCCA8CgAwIBAgIQTKr5yttjb+Af907YWwOGnTANBgkqhkiG9w0BAQwFADCB\n"
                + "hTELMAkGA1UEBhMCR0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4G\n"
                + "A1UEBxMHU2FsZm9yZDEaMBgGA1UEChMRQ09NT0RPIENBIExpbWl0ZWQxKzApBgNV\n"
                + "BAMTIkNPTU9ETyBSU0EgQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkwHhcNMTAwMTE5\n"
                + "MDAwMDAwWhcNMzgwMTE4MjM1OTU5WjCBhTELMAkGA1UEBhMCR0IxGzAZBgNVBAgT\n"
                + "EkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4GA1UEBxMHU2FsZm9yZDEaMBgGA1UEChMR\n"
                + "Q09NT0RPIENBIExpbWl0ZWQxKzApBgNVBAMTIkNPTU9ETyBSU0EgQ2VydGlmaWNh\n"
                + "dGlvbiBBdXRob3JpdHkwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCR\n"
                + "6FSS0gpWsawNJN3Fz0RndJkrN6N9I3AAcbxT38T6KhKPS38QVr2fcHK3YX/JSw8X\n"
                + "pz3jsARh7v8Rl8f0hj4K+j5c+ZPmNHrZFGvnnLOFoIJ6dq9xkNfs/Q36nGz637CC\n"
                + "9BR++b7Epi9Pf5l/tfxnQ3K9DADWietrLNPtj5gcFKt+5eNu/Nio5JIk2kNrYrhV\n"
                + "/erBvGy2i/MOjZrkm2xpmfh4SDBF1a3hDTxFYPwyllEnvGfDyi62a+pGx8cgoLEf\n"
                + "Zd5ICLqkTqnyg0Y3hOvozIFIQ2dOciqbXL1MGyiKXCJ7tKuY2e7gUYPDCUZObT6Z\n"
                + "+pUX2nwzV0E8jVHtC7ZcryxjGt9XyD+86V3Em69FmeKjWiS0uqlWPc9vqv9JWL7w\n"
                + "qP/0uK3pN/u6uPQLOvnoQ0IeidiEyxPx2bvhiWC4jChWrBQdnArncevPDt09qZah\n"
                + "SL0896+1DSJMwBGB7FY79tOi4lu3sgQiUpWAk2nojkxl8ZEDLXB0AuqLZxUpaVIC\n"
                + "u9ffUGpVRr+goyhhf3DQw6KqLCGqR84onAZFdr+CGCe01a60y1Dma/RMhnEw6abf\n"
                + "Fobg2P9A3fvQQoh/ozM6LlweQRGBY84YcWsr7KaKtzFcOmpH4MN5WdYgGq/yapiq\n"
                + "crxXStJLnbsQ/LBMQeXtHT1eKJ2czL+zUdqnR+WEUwIDAQABo0IwQDAdBgNVHQ4E\n"
                + "FgQUu69+Aj36pvE8hI6t7jiY7NkyMtQwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB\n"
                + "/wQFMAMBAf8wDQYJKoZIhvcNAQEMBQADggIBAArx1UaEt65Ru2yyTUEUAJNMnMvl\n"
                + "wFTPoCWOAvn9sKIN9SCYPBMtrFaisNZ+EZLpLrqeLppysb0ZRGxhNaKatBYSaVqM\n"
                + "4dc+pBroLwP0rmEdEBsqpIt6xf4FpuHA1sj+nq6PK7o9mfjYcwlYRm6mnPTXJ9OV\n"
                + "2jeDchzTc+CiR5kDOF3VSXkAKRzH7JsgHAckaVd4sjn8OoSgtZx8jb8uk2Intzna\n"
                + "FxiuvTwJaP+EmzzV1gsD41eeFPfR60/IvYcjt7ZJQ3mFXLrrkguhxuhoqEwWsRqZ\n"
                + "CuhTLJK7oQkYdQxlqHvLI7cawiiFwxv/0Cti76R7CZGYZ4wUAc1oBmpjIXUDgIiK\n"
                + "boHGhfKppC3n9KUkEEeDys30jXlYsQab5xoq2Z0B15R97QNKyvDb6KkBPvVWmcke\n"
                + "jkk9u+UJueBPSZI9FoJAzMxZxuY67RIuaTxslbH9qh17f4a+Hg4yRvv7E491f0yL\n"
                + "S0Zj/gA0QHDBw7mh3aZw4gSzQbzpgJHqZJx64SIDqZxubw5lT2yHh17zbqD5daWb\n"
                + "QOhTsiedSrnAdyGN/4fy3ryM7xfft0kL0fJuMAsaDk527RH89elWsn2/x20Kk4yl\n"
                + "0MC2Hb46TpSi125sC8KKfPog88Tk5c0NqMuRkrF8hey1FGlmDoLnzc7ILaZRfyHB\n"
                + "NVOFBkpdn627G190\n"
                + "-----END CERTIFICATE-----\n";
        String xebiaRsaCertificationAuthority = ""
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIFzTCCBLWgAwIBAgIQWSpqkWxZYvNFjBhyoRk87TANBgkqhkiG9w0BAQsFADCB\n"
                + "ljELMAkGA1UEBhMCR0IxGzAZBgNVBAgTEkdyZWF0ZXIgTWFuY2hlc3RlcjEQMA4G\n"
                + "A1UEBxMHU2FsZm9yZDEaMBgGA1UEChMRQ09NT0RPIENBIExpbWl0ZWQxPDA6BgNV\n"
                + "BAMTM0NPTU9ETyBSU0EgT3JnYW5pemF0aW9uIFZhbGlkYXRpb24gU2VjdXJlIFNl\n"
                + "cnZlciBDQTAeFw0xNTA3MjIwMDAwMDBaFw0xNjA4MDIyMzU5NTlaMIHJMQswCQYD\n"
                + "VQQGEwJOTDEPMA0GA1UEERMGMTIxM1RMMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5k\n"
                + "MRIwEAYDVQQHEwlIaWx2ZXJzdW0xGDAWBgNVBAkTD1V0cmVjaHRzZXdlZyA0OTEd\n"
                + "MBsGA1UEChMUWGViaWEgTmVkZXJsYW5kIEIuVi4xFzAVBgNVBAsTDkhvc3RlZCBi\n"
                + "eSBDeXNvMRUwEwYDVQQLEwxXaWxkY2FyZCBTU0wxFDASBgNVBAMMCyoueGViaWEu\n"
                + "Y29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0u/ernYIkCvPnvXF\n"
                + "hpJtlgqomMKu/G8TS+fvUjKHtRBxDv+fGHxouAzdVnPzpld/NA/hh0yuuQeRiGx3\n"
                + "p7m6BFsvGc9SiGK0zjgk658p2yZ0zxw1sVB5k+tikPTmLJNigTMAAgiX7Aw1BNPX\n"
                + "LWsWCBFwCtkSoGnQQSdBlEtLMqTOCFlqBHtlYUrI4C1+xnEkduJtSABFVbKQUcFj\n"
                + "vVFboQukUnEJVg5QKnqvc4DhRyWnhQ5mOoNCdLOlq+KaRVUHCjtQYT7CblNsykpJ\n"
                + "7gY0lTVHAYyIrCqJoxXojGssivETJK9fs85A9j03YoMB2PIM4bJO9GF1Vadi6odV\n"
                + "yYmmFwIDAQABo4IB4DCCAdwwHwYDVR0jBBgwFoAUmvMr2s+tT7YvuypISCoStxtC\n"
                + "wSQwHQYDVR0OBBYEFL6ca+HzXhT1I4mp/2aIWrluCtJFMA4GA1UdDwEB/wQEAwIF\n"
                + "oDAMBgNVHRMBAf8EAjAAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBQ\n"
                + "BgNVHSAESTBHMDsGDCsGAQQBsjEBAgEDBDArMCkGCCsGAQUFBwIBFh1odHRwczov\n"
                + "L3NlY3VyZS5jb21vZG8uY29tL0NQUzAIBgZngQwBAgIwWgYDVR0fBFMwUTBPoE2g\n"
                + "S4ZJaHR0cDovL2NybC5jb21vZG9jYS5jb20vQ09NT0RPUlNBT3JnYW5pemF0aW9u\n"
                + "VmFsaWRhdGlvblNlY3VyZVNlcnZlckNBLmNybDCBiwYIKwYBBQUHAQEEfzB9MFUG\n"
                + "CCsGAQUFBzAChklodHRwOi8vY3J0LmNvbW9kb2NhLmNvbS9DT01PRE9SU0FPcmdh\n"
                + "bml6YXRpb25WYWxpZGF0aW9uU2VjdXJlU2VydmVyQ0EuY3J0MCQGCCsGAQUFBzAB\n"
                + "hhhodHRwOi8vb2NzcC5jb21vZG9jYS5jb20wIQYDVR0RBBowGIILKi54ZWJpYS5j\n"
                + "b22CCXhlYmlhLmNvbTANBgkqhkiG9w0BAQsFAAOCAQEAsXQjMqPHTRjNLErMGLAu\n"
                + "Uc62iVZaA4rIKitiO7r2CSpVXel+AwvdS5kr5o71cSshr91NMHL7ebe+yQuTDawI\n"
                + "kDVyZbp8dJlBA1mXf6E6H10pSJV+vmEK6kq/mvBhWFyTYqzWySW5zO6KQt1v/3J8\n"
                + "w55ukqK9hzrvcV2QrmtIExFavHMxcZtt55YMzjFj7pu6A/KUKhz1WLmBBAEoGwLk\n"
                + "+i2S/I8x3euDv4P846u0WUGuX7o5mD97phg6Vsflc74NWxl9JzVS2KnYXtRNme7f\n"
                + "kAKyX31PdJ6fuvhpcPNjj+UvJZR9YoGuLz4lNvBcyY/iI2ZQt89/U0y8c6L/V4CZ\n"
                + "Ag==\n"
                + "-----END CERTIFICATE-----";
        return new Buffer()
                .writeUtf8(comodoRsaCertificationAuthority)
                .writeUtf8(xebiaRsaCertificationAuthority)
                .inputStream();
    }

    /**
     * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a {@code
     * SSLHandshakeException}.
     *
     * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
     * set. This is useful in development where certificate authority-trusted certificates aren't
     * available. Or in production, to avoid reliance on third-party certificate authorities.
     *
     * <p>See also {@link CertificatePinner}, which can limit trusted certificates while still using
     * the host platform's built-in trust store.
     *
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     *
     * <p>Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates in production without the blessing of your server's TLS
     * administrator.
     */
    private static X509TrustManager trustManagerForCertificates(InputStream in) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // By convention, 'null' creates an empty key store.
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
