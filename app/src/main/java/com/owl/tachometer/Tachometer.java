package com.owl.tachometer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private Drawable sticker;
    private int currentSize = 0;

    private TextPaint mTextPaint;
    private Paint circlePaint;
    private Paint circlePaint2;

    private float mTextWidth;
    private float mTextHeight;

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
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Tachometer, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.Tachometer_exampleString);
        mExampleColor = a.getColor(
                R.styleable.Tachometer_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.Tachometer_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.Tachometer_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.Tachometer_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        if (a.hasValue(R.styleable.Tachometer_sticker)) {
            sticker = a.getDrawable(
                    R.styleable.Tachometer_sticker);
            sticker.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        circlePaint = new Paint();
        circlePaint.setColor(Color.BLACK);

        circlePaint2 = new Paint();
        circlePaint2.setColor(Color.RED);
        circlePaint2.setStyle(Paint.Style.STROKE);
        circlePaint2.setStrokeWidth(10);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

//        int contentWidth = getWidth() - paddingLeft - paddingRight;
//        int contentHeight = getHeight() - paddingTop - paddingBottom;

        currentSize = getWidth();

        int contentWidth = currentSize - paddingLeft - paddingRight;
        int contentHeight = currentSize - paddingTop - paddingBottom;
        int quarter = contentWidth / 5;

        int centerX = paddingLeft + currentSize / 2;
        int centerY = paddingTop + currentSize / 2;

        canvas.drawCircle(centerX, centerY, contentWidth / 2, circlePaint);
        canvas.drawCircle(centerX, centerY, 10, mTextPaint);
        canvas.drawLine(centerX, centerY, centerX + contentWidth / 2, centerY, mTextPaint);

        canvas.drawCircle(centerX, centerY, contentWidth / 2, circlePaint2);

        // Draw the text.
//        canvas.drawText(mExampleString,
//                paddingLeft + (contentWidth - mTextWidth) / 2,
//                paddingTop + (contentHeight + mTextHeight) / 2,
//                mTextPaint);

        // Draw the example drawable on top of the text.
//        if (mExampleDrawable != null) {
//            mExampleDrawable.setBounds(paddingLeft, paddingTop,
//                    paddingLeft + contentWidth, paddingTop + contentHeight);
//            mExampleDrawable.draw(canvas);
//        }

        if (sticker != null) {
            //left top right bottom
            int width = quarter; //sticker.getIntrinsicWidth();
            int height = quarter; //sticker.getIntrinsicHeight();



            int left = centerX - width / 2;
            int top = centerY - height / 2 - quarter;
            sticker.setAlpha(200);
            sticker.setBounds(
                    left,
                    top,
                    left + width,
                    top + height);
            sticker.draw(canvas);
        }
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;

//        invalidate();
//        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);
        int lesserDimension = (specWidth > specHeight) ? specHeight : specWidth;
        currentSize = lesserDimension;
        setMeasuredDimension(lesserDimension, lesserDimension);
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
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
