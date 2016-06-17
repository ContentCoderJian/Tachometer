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
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        //tachometer.setExampleColor(color);
    }
}
