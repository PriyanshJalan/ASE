package com.example.minutedublin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;

public class SendReport extends AppCompatActivity {

    TextView reportlocation;
    public static double alertlat;
    public static double alertlng;
    //public static place = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

        reportlocation = (TextView) findViewById(R.id.location2);

        /////receive point location
        Intent intent = getIntent();
        //String string1 =  intent.getStringExtra("place");
        //private Point origin = Point.fromLngLat(orilng, orilat);

        StringBuilder reportPoint = new StringBuilder();
        /*
        reportPoint.append("location：(").append("longtitude").
                append(",").append("latitude").append(")");


        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() :
                        getString(R.string.access_token))
                .query(Point.fromLngLat(alertlng, alertlat))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();
        */

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

        /////
    }
}
