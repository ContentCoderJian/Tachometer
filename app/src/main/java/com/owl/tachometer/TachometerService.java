package com.owl.tachometer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TachometerService extends Service {
    final static String TACHOMETER_TICK_ACTION = "com.owl.tachometer.TACHOMETER_TICK_ACTION";
    final static String TACHOMETER_TICK_VALUE = "com.owl.tachometer.TACHOMETER_TICK_VALUE";

    private MyThread thread;
    private boolean running;

    public TachometerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        if (thread == null) {
            thread = new MyThread();
            thread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        running = false;
        super.onDestroy();
    }

    public class MyThread extends Thread{

        private int rps = 0;

        @Override
        public void run() {
            try {
                turnOnTheEngine();
                while(running) {
                    push();
                    release();
                    push();
                    release();
                    release();
                    push();
                    push();

                    leave();
                    delay();
                    delay();
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        private float calculateMomentum() {

            return (float) (1 + 2 * Math.exp(- rps / 1000));
        }

        private void push() throws InterruptedException {
            Log.d("Service", "push");
            for (int i = 0; i < 4 && running; ++i) {
                rps += 500 * calculateMomentum();
                sendValue(rps);
                delay();
            }
        }

        private void release() throws InterruptedException {
            Log.d("Service", "release");
            for (int i = 0; i < 3 && running; ++i) {
                rps -= 500 * calculateMomentum();
                sendValue(rps);
                delay();
            }
        }

        private void leave() throws InterruptedException {
            Log.d("Service", "leave");
            while (rps > 1000 && running) {
                rps -= 600 * calculateMomentum();
                sendValue(rps);
                delay();
            }
            rps = 1100;
            sendValue(rps);
            delay();
            sendValue(1000);
            delay();
        }

        private void turnOnTheEngine() throws InterruptedException {
            delay(500);
            sendValue(1200);
            delay(500);
            sendValue(800);
            delay(500);
            sendValue(1000);
            delay(1000);
            rps = 1000;
        }

        private void sendValue(int value) {
            Intent intent = new Intent();
            intent.setAction(TACHOMETER_TICK_ACTION);
            intent.putExtra(TACHOMETER_TICK_VALUE, value);
            sendBroadcast(intent);
        }

        private void delay(int time) throws InterruptedException {
            Thread.sleep(time);
        }

        private void delay() throws InterruptedException {
            Thread.sleep(500);
        }

    }
}
