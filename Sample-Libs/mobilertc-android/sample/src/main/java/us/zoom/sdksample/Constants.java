package us.zoom.sdksample;

import android.os.StrictMode;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



public class Constants {

    // TODO Change it to your web domain
    public final static String WEB_DOMAIN = "zoom.us";


    public	static String TOKEN(String sessionName ){
        // Create URL
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try{

            TrustManager[] trustAllCertificates = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            // Create an SSLContext with the custom TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());

            // Apply the custom SSLContext as the default SSLSocketFactory
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());


            String url = "https://asdc.cc/video/";
            URL urlObj = new URL(url);
            HttpsURLConnection conn = (HttpsURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setRequestProperty("Accept","application/json");


            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);

            conn.connect();

            String _sessionName=sessionName;
            if (_sessionName==null) _sessionName="webchun6871";





            JSONObject jsonParam = new JSONObject();
            jsonParam.put("sessionName", _sessionName);
            jsonParam.put("role", 1);
            jsonParam.put("user_identity", "user_identity6871");
            jsonParam.put("session_key", "session_key6871");



            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            wr.writeBytes(jsonParam.toString());

            wr.flush();
            wr.close();

            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject obj = new JSONObject(result.toString());
                return obj.getString("signature");

            } catch (IOException e) {
                e.printStackTrace();
                return "meow";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "meow";
        }



    }
}