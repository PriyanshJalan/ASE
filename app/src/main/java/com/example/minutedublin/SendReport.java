package com.example.minutedublin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.icu.text.UFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;

import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.http.HTTP;

public class SendReport extends AppCompatActivity {

    TextView reportlocation;
    public static double alertlat;
    public static double alertlng;
    //public static place = "";

    private Button send;

    ////get permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

        reportlocation = (TextView) findViewById(R.id.location2);
        /////receive point location
        Intent intent = getIntent();

        StringBuilder reportPoint = new StringBuilder();
        /////////////////////////
        Point point = Point.fromLngLat(alertlng, alertlat);

        String Lat = String.format("%.6f", point.latitude());
        String Lng = String.format("%.6f", point.longitude());
        reportPoint.append("location：(").append(Lng).
                append(",").append("  ").append(Lat).append(")");

        /////////////////////////

        //reportPoint.append("location：(").append(string1).append(")");
        Intent intent1 =  new Intent(this,MainActivity.class);
        MainActivity.relat = alertlat;
        MainActivity.relng = alertlng;
        MainActivity.flag = 1;
        //startActivity(intent1);
        reportlocation.setText(reportPoint);

        String type = "fire";
        String time = "17/02/2020 08:43:38";
        String comment = "hello!";

        send = (Button) findViewById(R.id.btnguide);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {


                JSONObject postData = new JSONObject();
                try {
                    postData.put("type", "fire");
                    postData.put("report_time", "18/02/2020 08:43:38");
                    postData.put("comment", "towermalta");
                    postData.put("longitude", "-6.2609");
                    postData.put("latitude","53.3497");

                    //URL url = new URL("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/report/report");
                    new SendDeviceDetails().execute("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/report/report", postData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }


    private class SendDeviceDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json;utf-8");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(params[1]);
                wr.flush();
                wr.close();


                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

}

