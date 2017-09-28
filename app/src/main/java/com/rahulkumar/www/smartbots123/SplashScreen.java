package com.rahulkumar.www.smartbots123;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    private Handler mHandler= new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mHandler.postDelayed( new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        },1000);

    }
}
