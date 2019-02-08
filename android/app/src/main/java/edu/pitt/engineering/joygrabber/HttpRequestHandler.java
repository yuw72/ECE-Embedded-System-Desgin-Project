package edu.pitt.engineering.joygrabber;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import android.os.SystemClock;
import android.util.Log;
import android.os.AsyncTask;

public class HttpRequestHandler extends AsyncTask<String, Void, String> {

    private static Semaphore http_lock = new Semaphore(1, true);

    protected String doInBackground(String... urls) {

        try {
            /* TODO: convert parameters to URL */


            //String req_url_string = "http://192.168.43.57/";
            // TODO: process urls[2] ("GET")
            int count = urls.length;
            String req_url_string = urls[0] + urls[1];
            if (count > 3) {
                req_url_string += "?";
                for (int i = 3; i < count; i += 2) {
                    req_url_string += urls[i] + "=" + urls[i+1];
                    if (i+2 < count) {
                        req_url_string += "&";
                    }
                }
            }

            Log.w("joygrabber/http", "Started running bg http handler");
            URL url = new URL(req_url_string);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.w("joygrabber/http", "After openConnection()");
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                Log.w("joygrabber/http","Received response of 200");
            } else {
                Log.e("joygrabber/http", "Wrong resp code received");
            }
            Log.w("joygrabber/http", "Afer resp detection");


        } catch (Exception e) {
            Log.e("joygrabber/http", "Exception met");
            e.printStackTrace();
        }
        return "";
    }

    protected void onPostExecute() {

    }

}