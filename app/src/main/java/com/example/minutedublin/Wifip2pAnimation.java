package com.example.minutedublin;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Wifip2pAnimation extends AppCompatActivity {

    Animation frombottom;
    TextView btnaddphoto, titlepage, subtitlepage;
    Button btnpagephoto, btnpagechat;
    View dotmenu;
    ImageView icstates;
    Animation btt, bttwo, imgbounce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifip2p_animation);

        frombottom = AnimationUtils.loadAnimation(this,R.anim.frombotton);


        btt = AnimationUtils.loadAnimation(this, R.anim.btt);
        bttwo = AnimationUtils.loadAnimation(this, R.anim.bttwo);
        imgbounce = AnimationUtils.loadAnimation(this, R.anim.imgbounce);

        btnaddphoto = findViewById(R.id.btnaddphoto);
        btnaddphoto.setPaintFlags(btnaddphoto.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        titlepage = findViewById(R.id.titlepage);
        subtitlepage = findViewById(R.id.subtitlepage);

        btnpagephoto = findViewById(R.id.btnpagephoto);
        btnpagechat = findViewById(R.id.btnpagechat);

        dotmenu = findViewById(R.id.dotmenu);

        icstates = findViewById(R.id.icstates);

        btnpagechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icstates.setImageResource(R.drawable.icchat);
                titlepage.setText("No One Chat");
                subtitlepage.setText("Find a friend to have a chat");

                btnaddphoto.setText("FIND FRIEND");
                btnaddphoto.setTextColor(Color.parseColor("#C36E6E"));

                // pass animation
                icstates.startAnimation(imgbounce);
                titlepage.startAnimation(btt);
                subtitlepage.startAnimation(btt);
                btnaddphoto.startAnimation(bttwo);


                // another animation + img resource
                btnpagephoto.setBackgroundResource(R.drawable.iccamun);
                btnpagechat.setBackgroundResource(R.drawable.icmsgan);

                btnpagephoto.animate().scaleY(0.7f).scaleX(0.7f).setDuration(350).start();
                btnpagechat.animate().scaleY(1).scaleX(1).setDuration(350).start();
                dotmenu.animate().translationX(430).setDuration(350).setStartDelay(100).start();

            }
        });

        btnpagephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icstates.setImageResource(R.drawable.icphotos);
                titlepage.setText("No Files");
                subtitlepage.setText("Send a file for your friend");

                btnaddphoto.setText("SEND FILES");
                btnaddphoto.setTextColor(Color.parseColor("#C36E6E"));

                // pass animation
                icstates.startAnimation(imgbounce);
                titlepage.startAnimation(btt);
                subtitlepage.startAnimation(btt);
                btnaddphoto.startAnimation(bttwo);

                // another animation + img resource
                btnpagephoto.setBackgroundResource(R.drawable.iccaman);
                btnpagechat.setBackgroundResource(R.drawable.icmsgun);

                btnpagechat.animate().scaleY(0.7f).scaleX(0.7f).setDuration(350).start();
                btnpagephoto.animate().scaleY(1).scaleX(1).setDuration(350).start();
                dotmenu.animate().translationX(0).setDuration(350).setStartDelay(100).start();

            }
        });
    }

}
