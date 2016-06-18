package com.owl.tachometer;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Random;

public class MainActivity extends Activity {

    private Tachometer tachometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tachometer = (Tachometer) findViewById(R.id.tachometer);
    }

    public void fun(View v) {
        //tachometer.setExampleString("qqqqqqqqqqqqqqqqqq");
        Random rnd = new Random();
        //int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        //tachometer.setExampleColor(color);
        //tachometer.setRotationSpeed(rnd.nextInt(8000));


        // do something long
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 8000; i+= 50) {
                    final int value = i;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tachometer.post(new Runnable() {
                        @Override
                        public void run() {
                            tachometer.setRotationSpeed(value);
                        }
                    });
                }
            }
        };
        new Thread(runnable).start();


    }
}
