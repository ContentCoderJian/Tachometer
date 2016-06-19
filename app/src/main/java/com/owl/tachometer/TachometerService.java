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

                    sendValue(3000);
                    delay();
                    sendValue(1000);
                    delay();

                    sendValue(5000);
                    delay();
                    sendValue(900);
                    delay();

                    sendValue(7000);
                    delay();
                    sendValue(800);
                    delay();
                    sendValue(1100);
                    delay();
                    sendValue(1000);
                    delay(2);

                    push(2);
                    release(2);
                    push(3);
                    release(2);
                    push(4);
                    delay(2);
                    leave();

                }
                leave();

            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        private float calculateMomentum(int N, int i, boolean up) {
            float k = 1.5f;
//            return (float) (Math.pow(i / 4f, 2 * k));
            if (up) {
                return (float) (500 + 800 * Math.exp( 1f * i / N ));
            } else {
                return (float) (700 + 700 * Math.exp( 1f * i / N ));
            }

//            return 1;
        }

        private void push(int N) throws InterruptedException {
            Log.d("Service", "push");

            for (int i = 0; i < N && running; ++i) {
                rps += calculateMomentum(N, i, true);
                sendValue(rps);
                delay();
            }
        }

        private void release(int N) throws InterruptedException {
            Log.d("Service", "release");
            for (int i = 0; i < N && running; ++i) {
                rps -= calculateMomentum(N, i, false);
                sendValue(rps);
                delay();
            }
        }
//
        private void leave() throws InterruptedException {
            Log.d("Service", "leave");
            while (rps > 1000 && running) {
                rps -= calculateMomentum(4, 2, false);
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
            delay();
            sendValue(1200);
            delay();
            sendValue(800);
            delay();
            sendValue(1000);
            delay();
            rps = 1000;
        }

        private void sendValue(int value) {
            Intent intent = new Intent();
            intent.setAction(TACHOMETER_TICK_ACTION);
            intent.putExtra(TACHOMETER_TICK_VALUE, value);
            sendBroadcast(intent);
        }

        private void delay(int num) throws InterruptedException {
            for (int i = 0; i < num; ++i) {
                delay();
            }
        }

        private void delay() throws InterruptedException {
            Thread.sleep(500);
        }

    }
}
