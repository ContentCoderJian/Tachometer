package com.owl.tachometer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TachometerService extends Service {
    final static String TACHOMETER_TICK_ACTION = "com.owl.tachometer.TACHOMETER_TICK_ACTION";
    final static String TACHOMETER_TICK_VALUE = "com.owl.tachometer.TACHOMETER_TICK_VALUE";

    private MyThread thread;
    private boolean running;
    final int duration = 500;

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
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    public class MyThread extends Thread {

        private int rpm = 0;

        @Override
        public void run() {
            try {
                turnOnTheEngine();
                while (running) {
                    // emulate wrooom wrooooom wroooooom!
                    produceSequence(3000, 1000);
                    produceSequence(5000, 900);
                    produceSequence(7000, 800, 1100, 1000);
                    delay();

                    // emulate gear shift
                    push(2); release(2);
                    push(3); release(2);
                    push(4); delay(2);
                    leave();
                }
                leave();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * produce sequence of rpms measured by every duration interval
         * @param rpms
         * @throws InterruptedException
         */
        private void produceSequence(int... rpms) throws InterruptedException {
            for (int value : rpms) {
                sendValue(value);
                delay();
            }
        }

        /**
         * @param N number of ticks (by duration) to increase rpm
         * @throws InterruptedException
         */
        private void push(int N) throws InterruptedException {
            for (int i = 0; i < N && running; ++i) {
                rpm += calculateMomentum(N, i, true);
                sendValue(rpm);
                delay();
            }
        }

        /**
         * @param N number of ticks (by duration) to decrease rpm
         * @throws InterruptedException
         */
        private void release(int N) throws InterruptedException {
            for (int i = 0; i < N && running; ++i) {
                rpm -= calculateMomentum(N, i, false);
                sendValue(rpm);
                delay();
            }
        }

        /**
         * release accelerator while rpm not become 1000
         *
         * @throws InterruptedException
         */
        private void leave() throws InterruptedException {
            while (rpm > 1000 && running) {
                rpm -= calculateMomentum(4, 2, false);
                sendValue(rpm);
                delay();
            }
            rpm = 1100;
            sendValue(rpm);
            delay();
            sendValue(1000);
            delay();
        }

        /**
         * some exponential accelerate interpolator. for push(N)
         *
         * @param N  number of ticks in interval
         * @param i  current tick
         * @param up up or down
         * @return generated rpm step based on direction and interval progress
         */
        private float calculateMomentum(int N, int i, boolean up) {
            float k = 1.5f;
            if (up) {
                return (float) (500 + 800 * Math.exp(1f * i / N));
            } else {
                return (float) (700 + 700 * Math.exp(1f * i / N));
            }
        }

        /**
         * startup the engine. after that - rpm set on 1000
         *
         * @throws InterruptedException
         */
        private void turnOnTheEngine() throws InterruptedException {
            delay();
            sendValue(1200);
            delay();
            sendValue(800);
            delay();
            sendValue(1000);
            delay();
            rpm = 1000;
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
            Thread.sleep(duration);
        }

    }
}
