package com.example.minutedublin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private Handler mWaitHandler = new Handler();

    Animation frombotton;
    Animation fromtop;
    ImageView help1;
    ImageView help2;
    ImageView minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        help1 = (ImageView)  findViewById(R.id.help1);
        help2 = (ImageView)  findViewById(R.id.help2);

        fromtop = AnimationUtils.loadAnimation(this,R.anim.fromtop);
        frombotton = AnimationUtils.loadAnimation(this,R.anim.frombotton);

        help2.setAnimation(frombotton);
        help1.setAnimation(fromtop);

        Handler handler = new Handler();
        handler.postDelayed(new splashRunnable(), 3000);
//        mWaitHandler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                //The following code will execute after the 5 seconds.
//                    //Go to next page i.e, start the next activity.
//                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//                    SplashActivity.this.startActivity(intent);
//                    //Let's Finish Splash Activity since we don't want to show this when user press back button.
//                    SplashActivity.this.finish();
//
//            }
//        }, 1000);  // Give a 5 seconds delay.
    }

    public class splashRunnable implements Runnable{
        @Override
        public void run() {
            startActivity(new Intent(getApplication(), MainActivity.class));
            SplashActivity.this.finish();
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mWaitHandler.removeCallbacksAndMessages(null);
//    }
}

