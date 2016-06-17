package com.owl.tachometer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class Tachometer extends View {

    private int start = 0;
    private int end = 8;
    private int redZone = 7;
    private Drawable sticker;
    private int backgroundColor = Color.BLACK;
    private int numColor = Color.WHITE;
    private int arrowColor = Color.RED;
    private int divisionColor = Color.WHITE;
    private float availableAngle = 270f;

    private TextPaint numPaint; // paint for digit letters
    private TextPaint rpmPaint; // paint for rpm text
    private Paint bigDivisionPaint;
    private Paint smallDivisionPaint;
    private Paint smallDivisionRedPaint;
    private Paint ArrowPaint;
    private Paint backgroundPaint;
    private Paint round1, round2, round3; // paints for frame

    private String rpmText;
    private int frameWidth;
    private int divisionWidth;
    Rect r = new Rect(); // template rect (outside function to reduce allocations per draw)

    private int currentSize;
    private int centerX, centerY;
    private int contentWidth, contentHeight;
    private int paddingLeft, paddingRight, paddingTop, paddingBottom;
    private int piece; // 1/5 from content size


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
        availableAngle = (float) (a.getInteger(R.styleable.Tachometer_availableAngle, (int)availableAngle));

        if (a.hasValue(R.styleable.Tachometer_sticker)) {
            sticker = a.getDrawable(
                    R.styleable.Tachometer_sticker);
            sticker.setCallback(this);
        }

        a.recycle();

        rpmText = getResources().getString(R.string.rpm);
        frameWidth = getResources().getDimensionPixelSize(R.dimen.frame_width);
        divisionWidth = getResources().getDimensionPixelSize(R.dimen.divisionWidth);

        initPaints();


        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawFrame(canvas);
        drawClock(canvas);

        drawText(canvas, numPaint, "o", centerX, centerY);


    }


    private void initPaints() {
        numPaint = new TextPaint();
        numPaint.setTextAlign(Paint.Align.LEFT);
        numPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.num_size));
        numPaint.setColor(numColor);
        numPaint.setTypeface(Typeface.create("Arial", Typeface.BOLD));

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

        smallDivisionPaint = new Paint();
        smallDivisionPaint.setColor(divisionColor);
        smallDivisionPaint.setStrokeWidth(divisionWidth / 2);

        smallDivisionRedPaint = new Paint();
        smallDivisionRedPaint.setColor(Color.RED);
        smallDivisionRedPaint.setStrokeWidth(divisionWidth / 2);

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
        drawText(canvas, rpmPaint, rpmText, centerX, centerY + 1.5f * piece);
        // draw sticker
        if (sticker != null) {
            int left = centerX - piece / 2;
            int top = (int) (centerY - piece * 1.4);
            sticker.setAlpha(150);
            sticker.setBounds(left, top, left + piece, top + piece);
            sticker.draw(canvas);
        }
    }

    private void drawClock(Canvas canvas) {
        float startAngle = (360 - availableAngle) / 2;
        float bigStep = availableAngle / end;
        float smallStep = bigStep / 10f;
        float bigRadius = contentWidth * 0.90f / 2f;
        float mediumRadius = contentWidth * 0.80f / 2f;
        float smallRadius = contentWidth * 0.70f / 2f;

        float currentAngle = startAngle;
        float redAngle = redZone * bigStep + startAngle;

        while (currentAngle < startAngle + availableAngle) {
        //for (int i = 0; i <= end; ++i) {

            PointF a = calculateCirclePoint(currentAngle, bigRadius);
            PointF b = calculateCirclePoint(currentAngle, smallRadius);
            canvas.drawLine(a.x, a.y, b.x, b.y, bigDivisionPaint);

            for (int j = 0; j < 10; ++j) {
                currentAngle += smallStep;

                PointF a2 = calculateCirclePoint(currentAngle, bigRadius);
                PointF b2 = calculateCirclePoint(currentAngle, mediumRadius);
                if (currentAngle < redAngle) {
                    canvas.drawLine(a2.x, a2.y, b2.x, b2.y, smallDivisionPaint);
                } else {
                    canvas.drawLine(a2.x, a2.y, b2.x, b2.y, smallDivisionRedPaint);
                }


            }

            a = calculateCirclePoint(currentAngle, bigRadius);
            b = calculateCirclePoint(currentAngle, smallRadius);
            canvas.drawLine(a.x, a.y, b.x, b.y, bigDivisionPaint);

        }



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

    private void invalidateTextPaintAndMeasurements() {
        //mTextPaint.setTextSize(mExampleDimension);
//        mTextPaint.setColor(mExampleColor);
        //mTextWidth = mTextPaint.measureText(mExampleString);
//        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size));


        //Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //mTextHeight = fontMetrics.bottom;

        invalidate();
        requestLayout();
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
    }

//    @Override
//    protected Parcelable onSaveInstanceState() {
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("instanceState", super.onSaveInstanceState());
////        bundle.putString("myString", myEditText.getText().toString());
//        return bundle;
//    }

//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        if (state instanceof Bundle) {
//            Bundle bundle = (Bundle) state;
////            myEditText.setText(bundle.getString("myString"));
//            state = bundle.getParcelable("instanceState");
//        }
//        super.onRestoreInstanceState(state);
//    }


    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
//    public int getExampleColor() {
//        return mExampleColor;
//    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
//    public void setExampleColor(int exampleColor) {
//        mExampleColor = exampleColor;
//        invalidateTextPaintAndMeasurements();
//    }


}
