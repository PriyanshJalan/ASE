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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

        reportlocation = (TextView) findViewById(R.id.location2);

        /////receive point location
        Intent intent = getIntent();
        //private Point origin = Point.fromLngLat(orilng, orilat);

        StringBuilder reportPoint = new StringBuilder();
        /*
        reportPoint.append("location：(").append("longtitude").
                append(",").append("latitude").append(")");
        */

        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() :
                        getString(R.string.access_token))
                .query(Point.fromLngLat(alertlng, alertlat))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();

// The result of this reverse geocode will give you "Pennsylvania Ave NW"



        reportPoint.append("location：(").append(alertlng).
                append(",").append(alertlat).append(")");


        //reportPoint.append("location：(").append(reverseGeocode).append(")");


        reportlocation.setText(reportPoint);

    }
}
