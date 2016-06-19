package com.owl.tachometer.Views;

import android.view.animation.Interpolator;

/**
 * Created by owl on 19.06.16.
 */
class CompositeInterpolator implements Interpolator {

    private int A, B;
    private final int switchValue = 4000;

    public void setRange(int start, int end) {
        A = start;
        B = end;
    }

    @Override
    public float getInterpolation(float t) {
        boolean up = (B > A) ? true : false;
        if (Math.abs(A - B) > switchValue) {
            // for big steps we will use..
            if (up) {
                // accelerate interpolator..
                return (float) (Math.pow(t, 1.6f));
            } else {
                // or decelerate interpolator
                return (float) (1 - Math.pow(1 - t, 1.6f));
            }
        } else {
            // for small steps we will use linear
            return t;
        }
    }
}
