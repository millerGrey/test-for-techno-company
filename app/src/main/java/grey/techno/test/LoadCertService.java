package grey.techno.test;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.tls.OkHostnameVerifier;

public class LoadCertService extends IntentService {

    private OkHttpClient client;
    private List<ChainData> chains = new ArrayList<>();
    private X509Certificate startCert;

    public LoadCertService() {
        super("LoadCertService");
    }

    @Override
    public void onCreate() {

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        X509TrustManager trustAllCerts = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                startCert = chain[0];
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };


        try {
            sslContext.init(null, (new TrustManager[]{trustAllCerts}), new java.security.SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustAllCerts)
                .build();

        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Set<String> set = new LinkedHashSet<>(intent.getStringArrayListExtra(MainActivity.URL_LIST));
        PendingIntent pi = intent.getParcelableExtra(MainActivity.PENDING_INTENT);
        try {
            if (isNetworkAvailableAndConnected()) {
                for (String url : set) {
                    loadURLcerts(url);
                }
                pi.send(this, MainActivity.CODE_LOADER_RESULT_OK, new Intent()
                        .putParcelableArrayListExtra(MainActivity.CHAIN_LIST, (ArrayList<? extends Parcelable>) chains));
            } else {
                pi.send(this, MainActivity.CODE_LOADER_RESULT_FAIL, new Intent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadURLcerts(String url) {
        List<X509Certificate> certificates = new ArrayList<>();
        ChainData chainData = new ChainData(url);
        if (!url.contains("//")) {
            url = "https://" + url;
        } else if (url.contains("http:")) {
            url = "https:" + url.substring("http:".length());
        }
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (startCert != null) {
            certificates.add(startCert);
            getNextCert(findNextCertURL(startCert), certificates);
        }
        for (int i = 0; i < certificates.size(); i++) {
            if (i == certificates.size() - 1)
                chainData.add(getCertSubject(certificates.get(i)),
                        checkCertValidity(certificates.get(i), null));
            else if (i == 0)
                chainData.add(getCertSubject(certificates.get(i)),
                        checkCertValidity(certificates.get(i), certificates.get(i + 1)) &&
                                checkHostname(certificates.get(i), request.url()));
            else
                chainData.add(getCertSubject(certificates.get(i)),
                        checkCertValidity(certificates.get(i), certificates.get(i + 1)));
        }
        startCert = null;
        chains.add(chainData);
    }

    private void getNextCert(String certURL, List<X509Certificate> certs) {
        if (certURL == null) {
            return;
        }
        Request request = new Request.Builder()
                .url(certURL)
                .build();
        Response response;
        Call call = client.newCall(request);
        try {
            response = call.execute();
            X509Certificate cert = (X509Certificate) CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(response.body().bytes()));
            certs.add(cert);
            getNextCert(findNextCertURL(cert), certs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String findNextCertURL(X509Certificate cert) {
        int ind = cert.toString().lastIndexOf("CA Issuers - URI:");
        if (ind > 0) {
            return cert.toString().substring(ind + "CA Issuers - URI:".length()).split("\n")[0];
        } else {
            return null;
        }
    }

    private boolean checkCertValidity(X509Certificate cert, X509Certificate parentCert) {
        try {
            cert.checkValidity();
            if (parentCert != null) {
                cert.verify(parentCert.getPublicKey());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkHostname(X509Certificate cert, HttpUrl url) {
        return OkHostnameVerifier.INSTANCE.verify(url.host(), cert);
    }

    private String getCertSubject(X509Certificate cert) {
        return cert.getSubjectDN().toString().substring(3).split(",")[0];
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
