package com.example.minutedublin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SendReport extends AppCompatActivity {

     TextView reportlocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_report);

        reportlocation = (TextView) findViewById(R.id.location2);

        StringBuilder reportPoint = new StringBuilder();
        reportPoint.append("location：(").append("longtitude").
                append(",").append("latitude").append(")");

        reportlocation.setText(reportPoint);

    }
}
