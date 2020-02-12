package com.example.splashanimation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Animation frombotton;
    Animation fromtop;
    ImageView help1;
    ImageView help2;
    ImageView minute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        help1 = (ImageView)  findViewById(R.id.help1);
        help2 = (ImageView)  findViewById(R.id.help2);

        fromtop = AnimationUtils.loadAnimation(this,R.layout.fromtop);
        frombotton = AnimationUtils.loadAnimation(this,R.layout.frombotton);

        help2.setAnimation(frombotton);
        help1.setAnimation(fromtop);
    }
}
