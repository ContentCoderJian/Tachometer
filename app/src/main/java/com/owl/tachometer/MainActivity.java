package com.owl.tachometer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity {

    private Tachometer tachometer;
    private MyReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tachometer = (Tachometer) findViewById(R.id.tachometer);
    }

    public void startBtn(View v) {
        Intent intent = new Intent(MainActivity.this,
                TachometerService.class);
        startService(intent);
    }

    public void stopBtn(View v) {
        Intent intent = new Intent(MainActivity.this,
                TachometerService.class);
        stopService(intent);
        tachometer.setRotationSpeed(0);
    }

    @Override
    protected void onStart() {

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TachometerService.TACHOMETER_TICK_ACTION);
        registerReceiver(myReceiver, intentFilter);

        //Start our own service
        Intent intent = new Intent(MainActivity.this,
                TachometerService.class);
        startService(intent);

        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    private void receivedBroadcast(Intent i) {
        int value = i.getIntExtra(TachometerService.TACHOMETER_TICK_VALUE, 0);

//        Log.d("MainActivity", String.valueOf(value));

        tachometer.setRotationSpeed(value);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.receivedBroadcast(intent);
        }

    }
}
