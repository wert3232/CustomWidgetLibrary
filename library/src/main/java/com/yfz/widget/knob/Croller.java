package com.yfz.widget.knob;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.common.ConvertKt;
import com.library.R;

public class Croller extends View {
    /*private static final int[] sweepGradientColors = new int[]{Color.WHITE, Color.GREEN, Color.YELLOW, Color.RED};
    private static final float[] sweepGradientPosition = new float[]{0.25f,0.50f,0.75f,1f};*/
    private int[] sweepGradientColors = new int[]{
            Color.YELLOW,
            Color.RED,
            Color.RED,
            Color.WHITE,
            Color.WHITE,
            Color.GREEN,
            Color.GREEN,
            Color.YELLOW,
            Color.YELLOW
    };
    private float[] sweepGradientPosition = new float[]{
            0f,
            30f / 360f,
            60f / 360f,
            120f / 360f,
            150f / 360f,
            180f / 360f,
            240f / 360f,
            300f / 360f,
            360f / 360f
    };
    private float midx, midy;
    private Paint textPaint, valPaint, circlePaint, circlePaint2, linePaint;
    private float currdeg = 0, deg = 3, downdeg = 0;

    private boolean isContinuous = true;

    private int backCircleColor = Color.parseColor("#222222");
    private int mainCircleColor = Color.parseColor("#00000000");
    private int indicatorColor = Color.parseColor("#FFA036");
    private int progressPrimaryColor = Color.parseColor("#FFA036");
    private int progressSecondaryColor = Color.parseColor("#111111");

    private float progressPrimaryCircleSize = -1;
    private float progressSecondaryCircleSize = -1;

    private float progressPrimaryStrokeWidth = 10;
    private float progressSecondaryStrokeWidth = 10;

    private float mainCircleRadius = -1;
    private float backCircleRadius = -1;
    private float progressRadius = -1;
    private float mainCircleRadiusRatio = (float) 11 / (float) 15;
    private float backCircleRadiusRatio = (float) 13 / (float) 15;
    private float progressRadiusRatio = 1;

    private int startIndex = 0;
    private int endIndex = 100;
    private int max = 100;
    private int min = 1;

    private float indicatorWidth = 7;

    private String label = "Label";
    private int labelSize = 40;
    private int labelColor = Color.TRANSPARENT;

    private String mMinLabel = "min";
    private String mMaxLabel = "max";
    private float minBottomRatio = 0, maxBottomRatio = 0, minCenterRatio = -0.1f, maxCenterRatio = 0.1f;
    private int mValColor = Color.TRANSPARENT;
    private float mValSize = 16;

    //以什么角度为起点
    private int startOffset = 30;
    private int startOffset2 = 0;
    //进度所占有的角度
    private int sweepAngle = 300;

    private boolean isAntiClockwise = false;

    private boolean startEventSent = false;

    RectF oval;

    private onProgressChangedListener mProgressChangeListener;
    private OnCrollerChangeListener mCrollerChangeListener;

    private Bitmap mBackCircle, mMainCircle;
    private Drawable mBackDrawable, mMainDrawable;
    private int mReviseDegree = 0;

    //dataBinding
    public InverseBindingListener mInverseBindingListener;

    public interface onProgressChangedListener {
        void onProgressChanged(int progress);
    }

    public void setOnProgressChangedListener(onProgressChangedListener mProgressChangeListener) {
        this.mProgressChangeListener = mProgressChangeListener;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public void setOnCrollerChangeListener(OnCrollerChangeListener mCrollerChangeListener) {
        this.mCrollerChangeListener = mCrollerChangeListener;
    }

    public Croller(Context context) {
        this(context, null);
    }

    public Croller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Croller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        initXMLAttrs(context, attrs);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mMainCircle != null && !mMainCircle.isRecycled()) {
            mMainCircle.recycle();
        }
        if (mBackCircle != null && !mBackCircle.isRecycled()) {
            mBackCircle.recycle();
        }
        super.onDetachedFromWindow();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(labelColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(labelSize);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        valPaint = new Paint();
        valPaint.setAntiAlias(true);
        valPaint.setColor(mValColor);
        valPaint.setStyle(Paint.Style.FILL);
        valPaint.setTextSize(mValSize);
        valPaint.setFakeBoldText(false);
        valPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(progressSecondaryColor);
        circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
        circlePaint.setStyle(Paint.Style.FILL);

        circlePaint2 = new Paint();
        circlePaint2.setAntiAlias(true);
        circlePaint2.setColor(progressPrimaryColor);
        circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
        circlePaint2.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(indicatorColor);
        linePaint.setStrokeWidth(indicatorWidth);

        oval = new RectF();

    }

    private void initXMLAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Croller);
        TypedArray ca = context.obtainStyledAttributes(attrs, R.styleable.commonAttr);
        final int N = a.getIndexCount();
        for (int i = 0; i < ca.getIndexCount(); i++){
            int attr = ca.getIndex(i);
            if (attr == R.styleable.commonAttr_appProgress) {
                setProgress(ca.getInt(attr, 1));
            }
        }

        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.Croller_label) {
                setLabel(a.getString(attr));
            } else if (attr == R.styleable.Croller_back_circle_color) {
                setBackCircleColor(a.getColor(attr, Color.parseColor("#222222")));
            } else if (attr == R.styleable.Croller_main_circle_color) {
                setMainCircleColor(a.getColor(attr, Color.parseColor("#000000")));
            } else if (attr == R.styleable.Croller_indicator_color) {
                setIndicatorColor(a.getColor(attr, Color.parseColor("#FFA036")));
            } else if (attr == R.styleable.Croller_progress_primary_color) {
                setProgressPrimaryColor(a.getColor(attr, Color.parseColor("#FFA036")));
            } else if (attr == R.styleable.Croller_progress_secondary_color) {
                setProgressSecondaryColor(a.getColor(attr, Color.parseColor("#111111")));
            } else if (attr == R.styleable.Croller_label_size) {
                setLabelSize(a.getInteger(attr, 40));
            } else if (attr == R.styleable.Croller_label_color) {
                setLabelColor(a.getColor(attr, Color.TRANSPARENT));
            } else if (attr == R.styleable.Croller_min_val_label) {
                setMinLabel(a.getString(attr));
            } else if (attr == R.styleable.Croller_max_val_label) {
                setMaxLabel(a.getString(attr));
            } else if (attr == R.styleable.Croller_val_label_color) {
                setValColor(a.getColor(attr, Color.TRANSPARENT));
            } else if (attr == R.styleable.Croller_val_label_size) {
                setValSize(a.getDimension(attr, 16));
            } else if (attr == R.styleable.Croller_max_val_margin_bottom_by_radiusRatio) {
                maxBottomRatio = a.getFloat(attr, 0);
            } else if (attr == R.styleable.Croller_max_val_margin_HorizontalCenter_by_radiusRatio) {
                maxCenterRatio = a.getFloat(attr, 0);
            } else if (attr == R.styleable.Croller_min_val_margin_bottom_by_radiusRatio) {
                minBottomRatio = a.getFloat(attr, 0);
            } else if (attr == R.styleable.Croller_min_val_margin_HorizontalCenter_by_radiusRatio) {
                minCenterRatio = a.getFloat(attr, 0);
            } else if (attr == R.styleable.Croller_indicator_width) {
                setIndicatorWidth(a.getFloat(attr, 7));
            } else if (attr == R.styleable.Croller_is_continuous) {
                setIsContinuous(a.getBoolean(attr, true));
            } else if (attr == R.styleable.Croller_progress_primary_circle_size) {
                setProgressPrimaryCircleSize(a.getFloat(attr, -1));
            } else if (attr == R.styleable.Croller_progress_secondary_circle_size) {
                setProgressSecondaryCircleSize(a.getFloat(attr, -1));
            } else if (attr == R.styleable.Croller_progress_primary_stroke_width) {
                setProgressPrimaryStrokeWidth(a.getFloat(attr, 10));
            } else if (attr == R.styleable.Croller_progress_secondary_stroke_width) {
                setProgressSecondaryStrokeWidth(a.getFloat(attr, 10));
            } else if (attr == R.styleable.Croller_sweep_angle) {
                setSweepAngle(a.getInt(attr, -1));
            } else if (attr == R.styleable.Croller_sweep_gradient_positions) {
               /* CharSequence[] array = a.getTextArray(attr);
                if(array != null){
                    sweepGradientPosition = ConvertKt.toFloat(array);
                }*/
                int ResId = a.getResourceId(attr,-1);
                if(ResId != -1){
                    sweepGradientPosition = ConvertKt.toFloat(getResources().getStringArray(ResId));
                }
            } else if (attr == R.styleable.Croller_sweep_gradient_colors) {
                int ResId = a.getResourceId(attr,-1);
                if(ResId != -1){
                    sweepGradientColors = getResources().getIntArray(ResId);
                }
            } else if (attr == R.styleable.Croller_start_offset) {
                setStartOffset(a.getInt(attr, 30));
            } else if (attr == R.styleable.Croller_max) {
                setMax(a.getInt(attr, 25));
            } else if (attr == R.styleable.Croller_min) {
                setMin(a.getInt(attr, 1));
                setDeg(min + 2);
            } else if (attr == R.styleable.Croller_main_circle_radius) {
                float val = a.getFloat(attr, -1);
                if (val > 0 && val <= 1) {
                    mainCircleRadiusRatio = val;
                    setMainCircleRadius(-1);
                } else {
                    setMainCircleRadius(val);
                }
            } else if (attr == R.styleable.Croller_back_circle_radius) {
                float val = a.getFloat(attr, -1);
                if (val > 0 && val <= 1) {
                    backCircleRadiusRatio = val;
                    setBackCircleRadius(-1);
                } else {
                    setBackCircleRadius(val);
                }
            } else if (attr == R.styleable.Croller_progress_radius) {
                float val = a.getFloat(attr, -1);
                if (val > 0 && val <= 2) {
                    progressRadiusRatio = val;
                    setProgressRadius(-1);
                } else {
                    setProgressRadius(val);
                }
            } else if (attr == R.styleable.Croller_anticlockwise) {
                setAntiClockwise(a.getBoolean(attr, false));
            } else if (attr == R.styleable.Croller_back_circle_drawable) {
                mBackDrawable = a.getDrawable(attr);
                mBackCircle = DrawableUtil.drawableToBitmap(mBackDrawable);
            } else if (attr == R.styleable.Croller_main_circle_drawable) {
                mMainDrawable = a.getDrawable(attr);
                mMainCircle = DrawableUtil.drawableToBitmap(mMainDrawable);
            } else if (attr == R.styleable.Croller_main_circle_drawable_revise_degree) {
                mReviseDegree = a.getInteger(attr, 0);
            }
        }
        a.recycle();
        ca.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int minWidth = (int) Utils.convertDpToPixel(160, getContext());
        int minHeight = (int) Utils.convertDpToPixel(160, getContext());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(minWidth, widthSize);
        } else {
            // only in case of ScrollViews, otherwise MeasureSpec.UNSPECIFIED is never triggered
            // If width is wrap_content i.e. MeasureSpec.UNSPECIFIED, then make width equal to height
            width = heightSize;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(minHeight, heightSize);
        } else {
            // only in case of ScrollViews, otherwise MeasureSpec.UNSPECIFIED is never triggered
            // If height is wrap_content i.e. MeasureSpec.UNSPECIFIED, then make height equal to width
            height = widthSize;
        }

        if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
            width = minWidth;
            height = minHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mProgressChangeListener != null) {
            mProgressChangeListener.onProgressChanged((int) (deg - 2));
        }

        if (mCrollerChangeListener != null) {
            mCrollerChangeListener.onProgressChanged(this, (int) (deg - 2));
        }

        midx = getWidth() / 2;
        midy = getHeight() / 2;
        if (progressPrimaryStrokeWidth < 1) {
            progressPrimaryStrokeWidth = progressPrimaryStrokeWidth * Math.min(midx, midy);
        }
        if (progressSecondaryStrokeWidth < 1) {
            progressSecondaryStrokeWidth = progressSecondaryStrokeWidth * Math.min(midx, midy);
        }
        circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
        circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
        if (!isContinuous) {

            startOffset2 = startOffset - 15;

            circlePaint.setColor(progressSecondaryColor);
            circlePaint2.setColor(progressPrimaryColor);
            linePaint.setStrokeWidth(indicatorWidth);
            linePaint.setColor(indicatorColor);
            textPaint.setColor(labelColor);
            textPaint.setTextSize(labelSize);

            int radius = (int) (Math.min(midx, midy) * ((float) 14.5 / 16));

            if (sweepAngle == -1) {
                sweepAngle = 360 - (2 * startOffset2);
            }

            if (mainCircleRadius == -1) {
                mainCircleRadius = radius * mainCircleRadiusRatio;
            }
            if (backCircleRadius == -1) {
                backCircleRadius = radius * backCircleRadiusRatio;
            }
            if (progressRadius == -1) {
                progressRadius = radius * progressRadiusRatio;
            }


            float x, y;
            float deg2 = Math.max(3, deg);
            float deg3 = Math.min(deg, max + 2);
            for (int i = (int) (deg2); i < max + 3; i++) {
                float tmp = ((float) startOffset2 / 360) + ((float) sweepAngle / 360) * (float) i / (max + 5);

                if (isAntiClockwise) {
                    tmp = 1.0f - tmp;
                }

                x = midx + (float) (progressRadius * Math.sin(2 * Math.PI * (1.0 - tmp)));
                y = midy + (float) (progressRadius * Math.cos(2 * Math.PI * (1.0 - tmp)));
                circlePaint.setColor(progressSecondaryColor);
                if (progressSecondaryCircleSize == -1) {
                    canvas.drawCircle(x, y, ((float) radius / 30 * ((float) 20 / max) * ((float) sweepAngle / 270)), circlePaint);
                } else {
                    canvas.drawCircle(x, y, progressSecondaryCircleSize, circlePaint);
                }
            }
            for (int i = 3; i <= deg3; i++) {
                float tmp = ((float) startOffset2 / 360) + ((float) sweepAngle / 360) * (float) i / (max + 5);

                if (isAntiClockwise) {
                    tmp = 1.0f - tmp;
                }

                x = midx + (float) (progressRadius * Math.sin(2 * Math.PI * (1.0 - tmp)));
                y = midy + (float) (progressRadius * Math.cos(2 * Math.PI * (1.0 - tmp)));

                if (progressPrimaryCircleSize == -1)
                    canvas.drawCircle(x, y, (progressRadius / 15 * ((float) 20 / max) * ((float) sweepAngle / 270)), circlePaint2);
                else
                    canvas.drawCircle(x, y, progressPrimaryCircleSize, circlePaint2);
            }

            float tmp2 = ((float) startOffset2 / 360) + ((float) sweepAngle / 360) * deg / (max + 5);

            if (isAntiClockwise) {
                tmp2 = 1.0f - tmp2;
            }

            float x1 = midx + (float) (radius * ((float) 2 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
            float y1 = midy + (float) (radius * ((float) 2 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));
            float x2 = midx + (float) (radius * ((float) 3 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
            float y2 = midy + (float) (radius * ((float) 3 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));

            circlePaint.setColor(backCircleColor);
            canvas.drawCircle(midx, midy, backCircleRadius, circlePaint);
            circlePaint.setColor(mainCircleColor);
            canvas.drawCircle(midx, midy, mainCircleRadius, circlePaint);
            canvas.drawText(label, midx, midy + (float) (radius * 1.1), textPaint);
            canvas.drawLine(x1, y1, x2, y2, linePaint);

            if (mBackCircle != null) {
                int temp = mBackCircle.getWidth() > mBackCircle.getHeight() ? mBackCircle.getWidth() : mBackCircle.getHeight();
                float ratio = backCircleRadius * 2 / temp;
                mBackCircle = DrawableUtil.scaleBitmap(mBackCircle, ratio);
                canvas.drawBitmap(mBackCircle, (canvas.getWidth() - mBackCircle.getWidth()) / 2, (canvas.getHeight() - mBackCircle.getHeight()) / 2, null);
            }
            if (mMainCircle != null) {
                int temp = mMainCircle.getWidth() > mMainCircle.getHeight() ? mMainCircle.getWidth() : mMainCircle.getHeight();
                float ratio = mainCircleRadius * 2 / temp;
                mMainCircle = DrawableUtil.scaleBitmap(mMainCircle, ratio);
                Matrix matrix = new Matrix();
                matrix.setRotate(360 * tmp2 - mReviseDegree, mMainCircle.getWidth() / 2, mMainCircle.getHeight() / 2);
                matrix.postTranslate((canvas.getWidth() - mMainCircle.getWidth()) / 2, (canvas.getHeight() - mMainCircle.getHeight()) / 2);
                canvas.drawBitmap(mMainCircle, matrix, null);
            }

        } else {

            int radius = (int) (Math.min(midx, midy) * ((float) 14.5 / 16));

            if (sweepAngle == -1) {
                sweepAngle = 360 - (2 * startOffset);
            }

            if (mainCircleRadius == -1) {
                mainCircleRadius = radius * mainCircleRadiusRatio;
            }
            if (backCircleRadius == -1) {
                backCircleRadius = radius * backCircleRadiusRatio;
            }
            if (progressRadius == -1) {
                progressRadius = radius * progressRadiusRatio;
            }
            circlePaint.setColor(progressSecondaryColor);
            circlePaint.setStrokeWidth(progressSecondaryStrokeWidth);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint2.setColor(progressPrimaryColor);
            circlePaint2.setStrokeWidth(progressPrimaryStrokeWidth);
            circlePaint2.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(indicatorWidth);
            linePaint.setColor(indicatorColor);
            textPaint.setColor(labelColor);
            textPaint.setTextSize(labelSize);

            float deg3 = Math.min(deg, max + 2);

            oval.set(midx - progressRadius, midy - progressRadius, midx + progressRadius, midy + progressRadius);

            //
            SweepGradient sweepGradient = new SweepGradient(midx, midy, sweepGradientColors, sweepGradientPosition);
            circlePaint2.setShader(sweepGradient);
            //

            canvas.drawArc(oval, (float) 90 + startOffset, (float) sweepAngle, false, circlePaint);
            if (isAntiClockwise) {
                canvas.drawArc(oval, (float) 90 - startOffset, -1 * ((deg3 - 2) * ((float) sweepAngle / max)), false, circlePaint2);
            } else {
                canvas.drawArc(oval, (float) 90 + startOffset, ((deg3 - 2) * ((float) sweepAngle / max)), false, circlePaint2);
            }
            float tmp2 = ((float) startOffset / 360) + (((float) sweepAngle / 360) * ((deg - 2) / (max)));

            if (isAntiClockwise) {
                tmp2 = 1.0f - tmp2;
            }

            float x1 = midx + (float) (radius * ((float) 2 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
            float y1 = midy + (float) (radius * ((float) 2 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));
            float x2 = midx + (float) (radius * ((float) 3 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
            float y2 = midy + (float) (radius * ((float) 3 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));

            circlePaint.setStyle(Paint.Style.FILL);

            circlePaint.setColor(backCircleColor);
            canvas.drawCircle(midx, midy, backCircleRadius, circlePaint);
            circlePaint.setColor(mainCircleColor);
            canvas.drawCircle(midx, midy, mainCircleRadius, circlePaint);
            canvas.drawText(label, midx, midy + (float) (radius * 1.1), textPaint);
            canvas.drawLine(x1, y1, x2, y2, linePaint);

            canvas.drawText(mMinLabel, midx + radius * minCenterRatio, midy + (float) (radius * (1.1 - minBottomRatio)), valPaint);
            canvas.drawText(mMaxLabel, midx + radius * maxCenterRatio, midy + (float) (radius * (1.1 - maxBottomRatio)), valPaint);
            if (mBackCircle != null) {
                int temp = mBackCircle.getWidth() > mBackCircle.getHeight() ? mBackCircle.getWidth() : mBackCircle.getHeight();
                float ratio = backCircleRadius * 2 / temp;
                mBackCircle = DrawableUtil.scaleBitmap(mBackCircle, ratio);
                canvas.drawBitmap(mBackCircle, (canvas.getWidth() - mBackCircle.getWidth()) / 2, (canvas.getHeight() - mBackCircle.getHeight()) / 2, null);
            }
            if (mMainCircle != null) {
                int temp = mMainCircle.getWidth() > mMainCircle.getHeight() ? mMainCircle.getWidth() : mMainCircle.getHeight();
                float ratio = mainCircleRadius * 2 / temp;
                mMainCircle = DrawableUtil.scaleBitmap(mMainCircle, ratio);
                Matrix matrix = new Matrix();
                matrix.setRotate(360 * tmp2 - mReviseDegree, mMainCircle.getWidth() / 2, mMainCircle.getHeight() / 2);
                matrix.postTranslate((canvas.getWidth() - mMainCircle.getWidth()) / 2, (canvas.getHeight() - mMainCircle.getHeight()) / 2);
                canvas.drawBitmap(mMainCircle, matrix, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
       /* if (Utils.getDistance(e.getX(), e.getY(), midx, midy) > Math.max(mainCircleRadius, Math.max(backCircleRadius, progressRadius))) {
            if (startEventSent && mCrollerChangeListener != null) {
                mCrollerChangeListener.onStopTrackingTouch(this);
                startEventSent = false;
            }
            return super.onTouchEvent(e);
        }*/

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            setPressed(true);
            float dx = e.getX() - midx;
            float dy = e.getY() - midy;
            downdeg = (float) ((Math.atan2(dy, dx) * 180) / Math.PI);
            downdeg -= 90;
            if (downdeg < 0) {
                downdeg += 360;
            }
            downdeg = (float) Math.floor((downdeg / 360) * (max + 5));

            if (mCrollerChangeListener != null) {
                mCrollerChangeListener.onStartTrackingTouch(this);
                startEventSent = true;
            }

            return true;
        }
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            setPressed(true);
            float dx = e.getX() - midx;
            float dy = e.getY() - midy;
            currdeg = (float) ((Math.atan2(dy, dx) * 180) / Math.PI);
            currdeg -= 90;
            if (currdeg < 0) {
                currdeg += 360;
            }
            currdeg = (float) Math.floor((currdeg / 360) * (max + 5));

            if ((currdeg / (max + 4)) > 0.75f && ((downdeg - 0) / (max + 4)) < 0.25f) {
                if (isAntiClockwise) {
                    setDeg(deg + 1);
                } else {
                    setDeg(deg - 1);
                }
            } else if ((downdeg / (max + 4)) > 0.75f && ((currdeg - 0) / (max + 4)) < 0.25f) {
                if (isAntiClockwise) {
                    setDeg(deg - 1);
                } else {
                    setDeg(deg + 1);
                }
            } else {
                if (isAntiClockwise) {
                    setDeg(deg - (currdeg - downdeg));
                } else {
                    setDeg(deg + (currdeg - downdeg));
                }
            }

            downdeg = currdeg;
            invalidate();
            return true;

        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            setPressed(false);
            if (mCrollerChangeListener != null) {
                mCrollerChangeListener.onStopTrackingTouch(this);
                startEventSent = false;
            }
            return true;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getParent() != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(event);
    }

    public int getProgress() {
        return (int) (deg - 2);
    }

    @InverseBindingAdapter(attribute = "appProgress", event = "appProgressAttrChanged")
    public static int getAppProgress(Croller croller) {
        return croller.getProgress();
    }

    public void setProgress(int x) {
        if (deg == x + 2) {

        } else {
            setDeg(x + 2);
        }
    }

    @BindingAdapter(value = {"appProgress"})
    public static void setProgress(Croller croller, int progress) {
        if (croller.getProgress() != progress) {
            croller.setProgress(progress);
        }
    }

    @BindingAdapter(value = {"appProgressAttrChanged"}, requireAll = false)
    public static void setAppProgressAttrChanged(Croller croller, InverseBindingListener inverseBindingListener) {
        if (inverseBindingListener == null) {
            croller.mInverseBindingListener = null;
        } else {
            croller.mInverseBindingListener = inverseBindingListener;
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String txt) {
        label = txt;
        invalidate();
    }

    public void setMinLabel(String mMinLabel) {
        this.mMinLabel = mMinLabel;
        invalidate();
    }

    public void setMaxLabel(String mMaxLabel) {
        this.mMaxLabel = mMaxLabel;
        invalidate();
    }

    public void setValColor(int mValColor) {
        this.mValColor = mValColor;
        invalidate();
    }

    public void setValSize(float mValSize) {
        this.mValSize = mValSize;
        invalidate();
    }

    public int getBackCircleColor() {
        return backCircleColor;
    }

    public void setBackCircleColor(int backCircleColor) {
        this.backCircleColor = backCircleColor;
        invalidate();
    }

    public int getMainCircleColor() {
        return mainCircleColor;
    }

    public void setMainCircleColor(int mainCircleColor) {
        this.mainCircleColor = mainCircleColor;
        invalidate();
    }

    public int getIndicatorColor() {
        return indicatorColor;
    }

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public int getProgressPrimaryColor() {
        return progressPrimaryColor;
    }

    public void setProgressPrimaryColor(int progressPrimaryColor) {
        this.progressPrimaryColor = progressPrimaryColor;
        invalidate();
    }

    public int getProgressSecondaryColor() {
        return progressSecondaryColor;
    }

    public void setProgressSecondaryColor(int progressSecondaryColor) {
        this.progressSecondaryColor = progressSecondaryColor;
        invalidate();
    }

    public int getLabelSize() {
        return labelSize;
    }

    public void setLabelSize(int labelSize) {
        this.labelSize = labelSize;
        invalidate();
    }

    public int getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        invalidate();
    }

    public float getIndicatorWidth() {
        return indicatorWidth;
    }

    public void setIndicatorWidth(float indicatorWidth) {
        this.indicatorWidth = indicatorWidth;
        invalidate();
    }

    public boolean isContinuous() {
        return isContinuous;
    }

    public void setIsContinuous(boolean isContinuous) {
        this.isContinuous = isContinuous;
        invalidate();
    }

    public float getProgressPrimaryCircleSize() {
        return progressPrimaryCircleSize;
    }

    public void setProgressPrimaryCircleSize(float progressPrimaryCircleSize) {
        this.progressPrimaryCircleSize = progressPrimaryCircleSize;
        invalidate();
    }

    public float getProgressSecondaryCircleSize() {
        return progressSecondaryCircleSize;
    }

    public void setProgressSecondaryCircleSize(float progressSecondaryCircleSize) {
        this.progressSecondaryCircleSize = progressSecondaryCircleSize;
        invalidate();
    }

    public float getProgressPrimaryStrokeWidth() {
        return progressPrimaryStrokeWidth;
    }

    public void setProgressPrimaryStrokeWidth(float progressPrimaryStrokeWidth) {
        this.progressPrimaryStrokeWidth = progressPrimaryStrokeWidth;
        invalidate();
    }

    public float getProgressSecondaryStrokeWidth() {
        return progressSecondaryStrokeWidth;
    }

    public void setProgressSecondaryStrokeWidth(float progressSecondaryStrokeWidth) {
        this.progressSecondaryStrokeWidth = progressSecondaryStrokeWidth;
        invalidate();
    }

    public int getSweepAngle() {
        return sweepAngle;
    }

    public void setSweepAngle(int sweepAngle) {
        this.sweepAngle = sweepAngle;
        invalidate();
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max < min) {
            this.max = min;
        } else {
            this.max = max;
        }
        invalidate();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        if (min < 0) {
            this.min = 0;
        } else if (min > max) {
            this.min = max;
        } else {
            this.min = min;
        }
        invalidate();
    }

    public float getMainCircleRadius() {
        return mainCircleRadius;
    }

    public void setMainCircleRadius(float mainCircleRadius) {
        this.mainCircleRadius = mainCircleRadius;
        invalidate();
    }

    public float getBackCircleRadius() {
        return backCircleRadius;
    }

    public void setBackCircleRadius(float backCircleRadius) {
        this.backCircleRadius = backCircleRadius;
        invalidate();
    }

    public float getProgressRadius() {
        return progressRadius;
    }

    public void setProgressRadius(float progressRadius) {
        this.progressRadius = progressRadius;
        invalidate();
    }

    public boolean isAntiClockwise() {
        return isAntiClockwise;
    }

    public void setAntiClockwise(boolean antiClockwise) {
        isAntiClockwise = antiClockwise;
        invalidate();
    }

    @Override
    public void refreshDrawableState() {
        super.refreshDrawableState();
//        Log.d(this.getClass().getName(),"refreshDrawableState");
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
//        Log.d(this.getClass().getName(),"drawableStateChanged");
        boolean isChange = false;
        if (mMainDrawable != null && mMainDrawable.isStateful()) {
            StateListDrawable d = (StateListDrawable) mMainDrawable;
            int[] state = getDrawableState();
            d.setState(state);
            mMainCircle = DrawableUtil.drawableToBitmap(mMainDrawable);
            isChange = true;
        }
        if (mBackDrawable != null && mBackDrawable.isStateful()) {
            StateListDrawable d = (StateListDrawable) mBackDrawable;
            int[] state = getDrawableState();
            d.setState(state);
            mBackCircle = DrawableUtil.drawableToBitmap(mBackDrawable);
            isChange = true;
        }
        if (isChange) {
            invalidate();
        }
    }

    private void setDeg(float newDeg) {
        if (newDeg > max + 2) {
            newDeg = max + 2;
        }
        if (newDeg < (min + 2)) {
            newDeg = min + 2;
        }
        if (this.deg != newDeg) {
            deg = newDeg;
            if (mInverseBindingListener != null) {
                mInverseBindingListener.onChange();
            }
            invalidate();
        }
    }
}