package com.owl.tachometer.Views;

import android.animation.ObjectAnimator;
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
import android.view.View;

import com.owl.tachometer.R;


/**
 * Custom Tachometer view class
 */
public class Tachometer extends View {

    final static String TACHOMETER_VIEW_STATE = "com.owl.tachometer.TACHOMETER_VIEW_STATE";
    final static String TACHOMETER_VIEW_ROTATION = "com.owl.tachometer.TACHOMETER_VIEW_ROTATION";

    // ARGS FROM XML LAYOUT
    private int start = 0;
    private int end = 8;
    private int redZone = 7;
    private Drawable sticker;
    private int backgroundColor = Color.BLACK;
    private int numColor = Color.WHITE;
    private int arrowColor = Color.RED;
    private int divisionColor = Color.WHITE;
    private float availableAngle = 270f;

    // SET OF PAINTS
    private TextPaint numPaint; // paint for digit letters
    private TextPaint rpmPaint; // paint for rpm text
    private Paint bigDivisionPaint;
    private Paint smallDivisionPaint;
    private Paint mediumDivisionPaint;
    private Paint smallDivisionRedPaint;
    private Paint arrowPaint;
    private Paint backgroundPaint;
    private Paint round1, round2, round3; // paints for frame

    // ARGS FROM RESOURCES
    private String rpmText;
    private int frameWidth;
    private int offsetFrame; // offset from frame to ticks
    private int offsetDigit; // offset from ticks to digits
    private int smallTickSize, mediumTickSize, bigTickSize;
    private int divisionWidth;

    // CONFIG AFTER onMeasure
    private int currentSize;
    private int centerX, centerY;
    private int contentWidth, contentHeight;
    private int paddingLeft, paddingRight, paddingTop, paddingBottom;
    private int piece; // 1/5 from content size

    private int rotationSpeed = 0;

    float startAngle = 45f;
    final int duration = 500;
    float bigStep;
    float smallStep;
    float redAngle;
    Rect r = new Rect(); // template rect
    PointF a = new PointF(), a1 = new PointF(),
            b = new PointF(), b1 = new PointF(),
            c = new PointF(), c1 = new PointF(); // template points
    private ObjectAnimator animator;
    private CompositeInterpolator interpol;


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
        bigStep = availableAngle / end;
        smallStep = bigStep / 10f;
        redAngle = redZone * bigStep + startAngle;

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

        animator = ObjectAnimator.ofInt(this, "RotationSpeedInternal", 0, 1);
        animator.setDuration(duration);

        interpol = new CompositeInterpolator();

        invalidateState();
    }

    public void setRotationSpeed(int r) {
        interpol.setRange(rotationSpeed, r);
        animator.setIntValues(rotationSpeed, r);
        animator.setInterpolator(interpol);
        animator.start();
    }

    public int getRotationSpeed() {
        return rotationSpeed;
    }

    private void setRotationSpeedInternal(int r) {
        if (r > end * 1000) {
            r = end * 1000;
        }
        if (r < 0) {
            r = 0;
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
        float radius = contentWidth / 2f - offsetFrame;
        float currentAngle = startAngle;

        int counter = start;
        while (currentAngle < startAngle + availableAngle) {
            // draw big tick
            calculateCirclePoint(currentAngle, radius, a);
            calculateCirclePoint(currentAngle, radius - bigTickSize, b);
            canvas.drawLine(a.x, a.y, b.x, b.y, bigDivisionPaint);
            // draw num text
            calculateCirclePoint(currentAngle, radius - bigTickSize - offsetDigit, a);
            drawText(canvas, numPaint, String.valueOf(counter++), a.x, a.y);

            for (int j = 0; j < 10; ++j) {
                currentAngle += smallStep;
                // draw small or medium tick
                calculateCirclePoint(currentAngle, radius, a1);
                calculateCirclePoint(currentAngle, radius - smallTickSize, b1);
                if (j == 4) {
                    calculateCirclePoint(currentAngle, radius - mediumTickSize, b1);
                    canvas.drawLine(a1.x, a1.y, b1.x, b1.y, mediumDivisionPaint);
                } else if (currentAngle < redAngle) {
                    canvas.drawLine(a1.x, a1.y, b1.x, b1.y, smallDivisionPaint);
                } else {
                    // in red zone - small ticks are red
                    canvas.drawLine(a1.x, a1.y, b1.x, b1.y, smallDivisionRedPaint);
                }
            }
        }
        // draw last big tick
        calculateCirclePoint(currentAngle, radius, a);
        calculateCirclePoint(currentAngle, radius - bigTickSize, b);
        canvas.drawLine(a.x, a.y, b.x, b.y, bigDivisionPaint);
        // draw last num text
        calculateCirclePoint(currentAngle, radius - bigTickSize - offsetDigit, a);
        drawText(canvas, numPaint, String.valueOf(counter++), a.x, a.y);

    }

    private void drawArrow(Canvas canvas) {
        float angle = (startAngle + rotationSpeed * availableAngle / (end * 1000));
        calculateCirclePoint(angle, contentWidth / 2f - offsetFrame - smallTickSize, a);
        calculateCirclePoint(angle + 2, contentWidth / 2f - offsetFrame - mediumTickSize, a1);
        calculateCirclePoint(angle + 90, 12, b);
        calculateCirclePoint(angle - 90, 12, b1);
        calculateCirclePoint(angle - 2, contentWidth / 2f - offsetFrame - mediumTickSize, c);

        Path p = new Path();
        p.reset();
        p.moveTo(a.x, a.y);
        p.lineTo(a1.x, a1.y);
        p.lineTo(b.x, b.y);
        p.lineTo(b1.x, b1.y);
        p.lineTo(c.x, c.y);
        p.lineTo(a.x, a.y);

        canvas.drawPath(p, arrowPaint);

        calculateCirclePoint(angle + 180, (contentWidth / 2f - offsetFrame) * 0.2f, c1);
        canvas.drawLine(c1.x, c1.y, centerX, centerY, arrowPaint);
        canvas.drawCircle(centerX, centerY, piece / 10, arrowPaint);
    }

    private void calculateCirclePoint(float angle, float R, PointF res) {
        res.set((float) (centerX - R * Math.sin(angle / 180 * Math.PI)),
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
