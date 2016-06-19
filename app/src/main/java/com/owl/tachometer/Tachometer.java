package com.owl.tachometer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;


/**
 * TODO: document your custom view class.
 */
public class Tachometer extends View {

    final static String TACHOMETER_VIEW_STATE = "com.owl.tachometer.TACHOMETER_VIEW_STATE";
    final static String TACHOMETER_VIEW_ROTATION = "com.owl.tachometer.TACHOMETER_VIEW_ROTATION";

    private int start = 0;
    private int end = 8;
    private int redZone = 7;
    private Drawable sticker;
    private int backgroundColor = Color.BLACK;
    private int numColor = Color.WHITE;
    private int arrowColor = Color.RED;
    private int divisionColor = Color.WHITE;
    private float availableAngle = 270f;
    float startAngle;

    private TextPaint numPaint; // paint for digit letters
    private TextPaint rpmPaint; // paint for rpm text
    private Paint bigDivisionPaint;
    private Paint smallDivisionPaint;
    private Paint mediumDivisionPaint;
    private Paint smallDivisionRedPaint;
    private Paint arrowPaint;
    private Paint backgroundPaint;
    private Paint round1, round2, round3; // paints for frame

    private String rpmText;
    private int frameWidth;
    private int offsetFrame; // offset from frame to ticks
    private int offsetDigit; // offset from ticks to digits
    private int smallTickSize, mediumTickSize, bigTickSize;
    private int divisionWidth;

    Rect r = new Rect(); // template rect (outside function to reduce allocations per draw)

    // config after onMeasure
    private int currentSize;
    private int centerX, centerY;
    private int contentWidth, contentHeight;
    private int paddingLeft, paddingRight, paddingTop, paddingBottom;
    private int piece; // 1/5 from content size

    private int rotationSpeed = 0;

    public Tachometer(Context context) {
        super(context);
        init(null, 0);
    }

    public Tachometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Tachometer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Tachometer, defStyle, 0);

        start = a.getInteger(R.styleable.Tachometer_start, start);
        end = a.getInteger(R.styleable.Tachometer_end, end);
        redZone = a.getInteger(R.styleable.Tachometer_redZone, redZone);
        backgroundColor = a.getColor(R.styleable.Tachometer_backgroundColor, backgroundColor);
        numColor = a.getColor(R.styleable.Tachometer_numColor, numColor);
        arrowColor = a.getColor(R.styleable.Tachometer_arrowColor, arrowColor);
        divisionColor = a.getColor(R.styleable.Tachometer_divisionColor, divisionColor);
        availableAngle = (float) (a.getInteger(R.styleable.Tachometer_availableAngle, (int) availableAngle));
        startAngle = (360 - availableAngle) / 2;

        if (a.hasValue(R.styleable.Tachometer_sticker)) {
            sticker = a.getDrawable(
                    R.styleable.Tachometer_sticker);
            sticker.setCallback(this);
        }

        a.recycle();

        rpmText = getResources().getString(R.string.rpm);
        frameWidth = getResources().getDimensionPixelSize(R.dimen.frame_width);
        divisionWidth = getResources().getDimensionPixelSize(R.dimen.divisionWidth);
        offsetFrame = getResources().getDimensionPixelSize(R.dimen.offset_from_frame);
        offsetDigit = getResources().getDimensionPixelOffset(R.dimen.digit_offset);
        smallTickSize = getResources().getDimensionPixelSize(R.dimen.small_tick_size);
        mediumTickSize = getResources().getDimensionPixelSize(R.dimen.medium_tick_size);
        bigTickSize = getResources().getDimensionPixelSize(R.dimen.big_tick_size);

        initPaints();


        invalidateState();
    }

    public void setRotationSpeed(final int r) {


        ObjectAnimator anim = ObjectAnimator.ofInt(this, "RS", rotationSpeed, r);
        anim.setDuration(500);
        final MyInterpolator interpol = new MyInterpolator();
        interpol.A = rotationSpeed;
        interpol.B = r;
        anim.setInterpolator(interpol);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animProgress = (Integer) animation.getAnimatedValue();
                //seekBar.setProgress(animProgress);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // done. setup residual animation
                int sign = (r > rotationSpeed) ? 1 : -1;
                int length = Math.abs(r - rotationSpeed);
                ObjectAnimator animR = ObjectAnimator.ofInt(this, "RS", r, r + sign * length / 5);
                animR.setDuration(500);
                animR.setInterpolator(interpol);
//                animR.start();
            }
        });


//        anim.setInterpolator(new AccelerateInterpolator(1.8f - half / 8000f));

        anim.start();

    }

    private class MyInterpolator implements Interpolator {

        public int A, B;

        @Override
        public float getInterpolation(float t) {
            boolean up = (B > A) ? true : false;
            float half = Math.abs(A - B) / 2f;
//            float k = 0.8f + Math.abs(A - B) / 4000f;
            float k = 0.8f;
            if (Math.abs(A - B) > 4000) {
                if (up) {
                    return (float) (Math.pow(t, 2 * k));
                } else {
                    return (float) (1 - Math.pow(1 - t, 2 * k));
                }
            }

            return t;

        }
    }

    private void setRS(int r) {
        if (r > end * 1000) {
            r = end * 1000;
        }
        rotationSpeed = r;
        invalidateState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawFrame(canvas);
        drawClock(canvas);
        drawArrow(canvas);

    }


    private void initPaints() {
        numPaint = new TextPaint();
        numPaint.setTextAlign(Paint.Align.LEFT);
        numPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.num_size));
        numPaint.setColor(numColor);
        numPaint.setTypeface(Typeface.create("mono", Typeface.BOLD));

        rpmPaint = new TextPaint();
        rpmPaint.setTextAlign(Paint.Align.LEFT);
        rpmPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.rpm_size));
        rpmPaint.setColor(numColor);
        rpmPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);

        bigDivisionPaint = new Paint();
        bigDivisionPaint.setColor(divisionColor);
        bigDivisionPaint.setStrokeWidth(divisionWidth);

        mediumDivisionPaint = new Paint();
        mediumDivisionPaint.setColor(divisionColor);
        mediumDivisionPaint.setStrokeWidth(divisionWidth / 2);

        smallDivisionPaint = new Paint();
        smallDivisionPaint.setColor(divisionColor);
        smallDivisionPaint.setStrokeWidth(divisionWidth / 3);

        smallDivisionRedPaint = new Paint();
        smallDivisionRedPaint.setColor(Color.RED);
        smallDivisionRedPaint.setStrokeWidth(divisionWidth / 2);

        arrowPaint = new Paint();
        arrowPaint.setColor(arrowColor);
        arrowPaint.setStrokeWidth(divisionWidth);
        arrowPaint.setStyle(Paint.Style.FILL);

        round1 = new Paint();
        round1.setColor(Color.parseColor("#9EA09F"));
        round1.setStyle(Paint.Style.STROKE);
        round1.setStrokeWidth(frameWidth / 6);

        round2 = new Paint();
        round2.setColor(Color.parseColor("#BABBBD"));
        round2.setStyle(Paint.Style.STROKE);
        round2.setStrokeWidth(frameWidth / 3);

        round3 = new Paint();
        round3.setColor(Color.parseColor("#3B3D3C"));
        round3.setStyle(Paint.Style.STROKE);
        round3.setStrokeWidth(frameWidth / 2);
    }

    private void drawFrame(Canvas canvas) {
        // draw background
        canvas.drawCircle(centerX, centerY, contentWidth / 2, backgroundPaint);
        // draw frame
        canvas.drawCircle(centerX, centerY, contentWidth / 2 - round1.getStrokeWidth() / 2, round1);
        canvas.drawCircle(centerX, centerY,
                contentWidth / 2 - round1.getStrokeWidth() - round2.getStrokeWidth() / 2, round2);
        canvas.drawCircle(centerX, centerY,
                contentWidth / 2 - round1.getStrokeWidth() - round2.getStrokeWidth() - round3.getStrokeWidth() / 2, round3);
        // draw rpm text
        drawText(canvas, rpmPaint, rpmText, centerX, centerY - 0.7f * piece);
        // draw sticker
        if (sticker != null) {
            int left = centerX - piece / 2;
            int top = (int) (centerY + piece * 0.5f);
            sticker.setAlpha(110);
            sticker.setBounds(left, top, left + piece, top + piece);
            sticker.draw(canvas);
        }
    }

    private void drawClock(Canvas canvas) {
        float bigStep = availableAngle / end;
        float smallStep = bigStep / 10f;

        float radius = contentWidth / 2f - offsetFrame;

        float currentAngle = startAngle;
        float redAngle = redZone * bigStep + startAngle;

        int counter = start;
        PointF a, a2, b, b2;

        while (currentAngle < startAngle + availableAngle) {

            a = calculateCirclePoint(currentAngle, radius);
            b = calculateCirclePoint(currentAngle, radius - bigTickSize);
            canvas.drawLine(a.x, a.y, b.x, b.y, bigDivisionPaint);

            a = calculateCirclePoint(currentAngle, radius - bigTickSize - offsetDigit);
            drawText(canvas, numPaint, String.valueOf(counter++), a.x, a.y);

            for (int j = 0; j < 10; ++j) {
                currentAngle += smallStep;

                a2 = calculateCirclePoint(currentAngle, radius);
                b2 = calculateCirclePoint(currentAngle, radius - smallTickSize);
                if (j == 4) {
                    b2 = calculateCirclePoint(currentAngle, radius - mediumTickSize);
                    canvas.drawLine(a2.x, a2.y, b2.x, b2.y, mediumDivisionPaint);
                } else if (currentAngle < redAngle) {
                    canvas.drawLine(a2.x, a2.y, b2.x, b2.y, smallDivisionPaint);
                } else {
                    canvas.drawLine(a2.x, a2.y, b2.x, b2.y, smallDivisionRedPaint);
                }
            }
        }

        a = calculateCirclePoint(currentAngle, radius);
        b = calculateCirclePoint(currentAngle, radius - bigTickSize);
        canvas.drawLine(a.x, a.y, b.x, b.y, bigDivisionPaint);

        a = calculateCirclePoint(currentAngle, radius - bigTickSize - offsetDigit);
        drawText(canvas, numPaint, String.valueOf(counter++), a.x, a.y);

    }

    private void drawArrow(Canvas canvas) {
        float angle = (startAngle + rotationSpeed * availableAngle / (end * 1000));
        PointF a = calculateCirclePoint(angle, contentWidth / 2f - offsetFrame - smallTickSize);
        PointF b = calculateCirclePoint(angle + 2, contentWidth / 2f - offsetFrame - mediumTickSize);
        PointF e = calculateCirclePoint(angle - 2, contentWidth / 2f - offsetFrame - mediumTickSize);
        PointF c = calculateCirclePoint(angle + 90, 12);
        PointF d = calculateCirclePoint(angle - 90, 12);

        Path p = new Path();
        p.reset();
        p.moveTo(a.x, a.y);
        p.lineTo(b.x, b.y);
        p.lineTo(c.x, c.y);
        p.lineTo(d.x, d.y);
        p.lineTo(e.x, e.y);
        p.lineTo(a.x, a.y);

        canvas.drawPath(p, arrowPaint);

        PointF g = calculateCirclePoint(angle + 180, (contentWidth / 2f - offsetFrame) * 0.2f);
        canvas.drawLine(g.x, g.y, centerX, centerY, arrowPaint);

        canvas.drawCircle(centerX, centerY, piece / 10, arrowPaint);
    }

    private PointF calculateCirclePoint(float angle, float R) {
        return new PointF(
                (float) (centerX - R * Math.sin(angle / 180 * Math.PI)),
                (float) (centerY + R * Math.cos(angle / 180 * Math.PI)));
    }


    private void drawText(Canvas c, Paint p, String text, float x, float y) {
        p.getTextBounds(text, 0, text.length(), r);
        c.drawText(text, x - r.width() / 2f, y + r.height() / 2f, p);
    }

    private void invalidateState() {

        invalidate();
//        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);
        int lesserDimension = (specWidth > specHeight) ? specHeight : specWidth;
        setMeasuredDimension(lesserDimension, lesserDimension);

        currentSize = getWidth();
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
        contentWidth = currentSize - paddingLeft - paddingRight;
        contentHeight = currentSize - paddingTop - paddingBottom;
        centerX = paddingLeft + contentWidth / 2;
        centerY = paddingTop + contentHeight / 2;
        piece = contentWidth / 5;

        invalidateState();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(TACHOMETER_VIEW_STATE, super.onSaveInstanceState());
        bundle.putInt(TACHOMETER_VIEW_ROTATION, rotationSpeed);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(TACHOMETER_VIEW_STATE);
            rotationSpeed = bundle.getInt(TACHOMETER_VIEW_ROTATION);
        }
        super.onRestoreInstanceState(state);
    }


}
