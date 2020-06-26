package com.rulerlibrary;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by HARRY on 2018/12/26.
 */

public class RulerView extends View {

    //控件宽
    private int mMeasuredWidth = 0;
    //控件高
    private int mMeasuredHeight = 0;
    //标尺宽
    private int mRulerWid;
    //标尺高
    private int mRulerHei;
    //标尺的左侧离控件的左侧的距离
    private int mLeft;
    //标尺的右侧离控件左侧的距离
    private int mRight;
    //标尺顶部离控件顶部距离
    private int mTop;
    //标尺底部离控件顶部的距离
    private int mBottom;
    //标题
    private String mTitleName;
    //画笔
    private Paint mPaint = new Paint();
    //目前设置刻度值默认显示6个单位值，打个比方初始显示可见的值是20-80，那么mLineOffset的值就是，标尺的宽除以6
    private int mLineOffset;
    //标尺刻度的长度
    private int mLineLong;
    //标尺指示器的长度
    private int mIndicatorLong;
    //最大刻度值
    private int mMaxValue;
    //最小刻度值
    private int mMinValue;
    //现在的刻度值
    private float mCurrentValue;
    //平均单位值下占有的px值
    private float mAverage;
    //标尺中间坐标x值
    private int mRulerMiddleX;
    //标尺刻度值y坐标
    private int mValueY;
    //实时手指移动时候的x坐标
    private float mCurrentDownX;
    //点击标尺时候记录下的点击y坐标
    private float mStartClickY;
    //点击标尺时候记录下的点击x坐标
    private float mStartClickX;
    //标尺单位名
    private String mUnitName;
    //滑动监听
    private SizeViewValueChangeListener listener;
    private GestureDetectorCompat mDetector;
    private ValueAnimator animator;
    private Path mPath = new Path(); //标尺裁剪线
    private float mCornor; //标尺圆角
    private RectF mRectFLeftTop;
    private RectF mRectFLeftBottom;
    private RectF mRectFRightTop;
    private RectF mRectFRightBottom;
    private int mRulerStokeWidth = 3;//刻度尺边框宽度

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mDetector = new GestureDetectorCompat(context, new RulerGestureListener());
        mCornor = DensityUtil.dip2px(getContext(), 6);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
        if (array != null) {
            String titleName = array.getString(R.styleable.RulerView_titleName);
            String unitName = array.getString(R.styleable.RulerView_unitName);
            int minValue = array.getInt(R.styleable.RulerView_minValue, 30);
            int maxValue = array.getInt(R.styleable.RulerView_maxValue, 120);
            int currentValue = array.getInt(R.styleable.RulerView_currentValue, 70);

            if (TextUtils.isEmpty(titleName)) {
                mTitleName = "体重";
            } else {
                mTitleName = titleName;
            }
            if (TextUtils.isEmpty(unitName)) {
                mUnitName = "kg";
            } else {
                mUnitName = unitName;
            }
            mMaxValue = maxValue;
            mMinValue = minValue;
            //默认让标尺距离左侧为100
            mLeft = 100;
            //默认让标尺距离顶部距离为100
            mTop = 100;
            setCurrentValue(currentValue);
            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int modeHei = MeasureSpec.getMode(heightMeasureSpec);
        mMeasuredWidth = getMeasuredWidth();
        if (modeHei == MeasureSpec.AT_MOST || modeHei == MeasureSpec.UNSPECIFIED) {
            //不确定控件高度情况下设置一个默认值
            int heiDefault = DensityUtil.dip2px(getContext(), 140);
            setMeasuredDimension(mMeasuredWidth, heiDefault);
        } else if (modeHei == MeasureSpec.EXACTLY) {
            //确切值不做任何操作
        }
        mMeasuredHeight = getMeasuredHeight();
        //设置标尺宽度
        mRulerWid = mMeasuredWidth - 2 * mLeft;

        mRight = mLeft + mRulerWid;
        //默认让标尺可见的范围内可以显示6个单位的刻度
        mLineOffset = mRulerWid / 6;
        mRulerHei = mMeasuredHeight - 2 * mTop;
        mBottom = mRulerHei + mTop;
        //使刻度线的长度为标尺高度的1/3
        mLineLong = mRulerHei / 3;
        //标尺的指示器的长度为标尺高度的1/2
        mIndicatorLong = mRulerHei / 2;

        //单位刻度下占有的屏幕宽度
        mAverage = mLineOffset / 10;
        //标尺中间的位置的x坐标
        mRulerMiddleX = mMeasuredWidth / 2;
        //标尺刻度值的y坐标位置
        mValueY = mTop + mLineLong + 100;

        //计算剪辑路径
        measureClipPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画整体背景为白色
        initDraw(canvas);
        //画标尺
        drawRuler(canvas);
        //画标题
        drawTitle(canvas);
        //画实时刻度值
        drawRulerValue(canvas, (int) mCurrentValue);
        //剪辑
        canvas.clipPath(mPath, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.WHITE);
    }

    /**
     * 计算剪辑路径
     */
    private void measureClipPath() {
        int left = mLeft - 2;
        int top = mTop - 2;
        int right = mRight + 2;
        int bottom = mBottom + 2;

        mRectFLeftTop = new RectF(left, top, left + 2 * mCornor, top + 2 * mCornor);
        mRectFRightTop = new RectF(right - 2 * mCornor, top, right, top + 2 * mCornor);
        mRectFRightBottom = new RectF(right - 2 * mCornor, bottom - 2 * mCornor, right, bottom);
        mRectFLeftBottom = new RectF(left, bottom - 2 * mCornor, left + 2 * mCornor, bottom);

        int left2 = mLeft - mRulerStokeWidth;
        int top2 = mTop - mRulerStokeWidth;
        int right2 = mRight + mRulerStokeWidth;
        int bottom2 = mBottom + mRulerStokeWidth;

        mPath.moveTo(left2 + mCornor, top2);
        mPath.lineTo(right - mCornor, top2);
        mPath.arcTo(mRectFRightTop, 270f, 90f, false);
        mPath.lineTo(right2, bottom2 - mCornor);
        mPath.arcTo(mRectFRightBottom, 0f, 90f, false);
        mPath.lineTo(left2 + mCornor, bottom2);
        mPath.arcTo(mRectFLeftBottom, 90f, 90f, false);
        mPath.lineTo(left2, top2 + mCornor);
        mPath.arcTo(mRectFLeftTop, 180f, 90f, false);
    }

    private void drawTitle(Canvas canvas) {
        setTitlePaint();
        //画标题,使标题底部正好在标尺顶部，baseLineY的实现原理这里不讲了，大家百度下
        Paint.FontMetricsInt fm = mPaint.getFontMetricsInt();
        int baseLineY = mTop - fm.bottom;
        canvas.drawText(mTitleName, mMeasuredWidth / 2, baseLineY - 10, mPaint);
    }

    private void initDraw(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#FFFFFF"));
        mPaint.setStyle(Paint.Style.FILL);

        RectF rect = new RectF(0, 0, mMeasuredWidth, mMeasuredHeight);
        canvas.drawRect(rect, mPaint);
    }

    private void setTitlePaint() {
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(40);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawRuler(Canvas canvas) {
        RectF rulerRect = new RectF();
        rulerRect.set(mLeft, mTop, mRight, mBottom);

        //画标尺的背景
        setRulerBackgroundPaint();
        int cornor = DensityUtil.dip2px(getContext(), 6);
        canvas.drawRoundRect(rulerRect, cornor, cornor, mPaint);

        //画标尺的刻度线和刻度
        drawRulerLine(canvas, (int) mCurrentValue);

        //画标尺的指示器线
        setIndicatorLinePaint();
        canvas.drawLine(mMeasuredWidth / 2, mTop, mMeasuredWidth / 2, mTop + mIndicatorLong, mPaint);

        //画标尺的背景边框
        setRulerStrokePaint();
        canvas.drawRoundRect(rulerRect, cornor, cornor, mPaint);
    }

    private void drawRulerValue(Canvas canvas, int indicatorValue) {
        setIndicatorValuePaint();
        //计算出baseLine位置,使文字正好在标尺的下面，具体原理这里就不讲了，大家百度
        Paint.FontMetricsInt fm = mPaint.getFontMetricsInt();
        int baseLineY = mBottom - fm.top;
        canvas.drawText(indicatorValue + " " + mUnitName, mRulerMiddleX, baseLineY, mPaint);
    }

    private void drawRulerLine(Canvas canvas, int indicatorValue) {
        setTitlePaint();
        //目前标尺值十位数字值
        int remainder = indicatorValue % 10;

        setRulerLinePaint();
        //画右侧长线
        int startXRl = (int) (mRulerMiddleX + mAverage * (10 - remainder));
        int endXRl = startXRl;
        int startYRl = mTop;
        int endYRl = mTop + mLineLong;
        int valueRl = (indicatorValue / 10 + 1) * 10;
        while (startXRl <= mRight && valueRl <= mMaxValue) {
            canvas.drawLine(startXRl, startYRl, endXRl, endYRl, mPaint);
            canvas.drawText("" + valueRl, startXRl, mValueY, mPaint);
            startXRl = startXRl + mLineOffset;
            endXRl = startXRl;
            valueRl = valueRl + 10;
        }

        //画右侧短线
        int startYRs = mTop;
        int endYRs = mTop + mLineLong / 2;
        int valueRs = 0;
        int startXRs = 0;
        if (remainder > 5) {
            valueRs = (indicatorValue / 10 + 1) * 10 + 5;
            startXRs = (int) (mRulerMiddleX + mAverage * (10 - remainder + 5));
        } else {
            valueRs = (indicatorValue / 10) * 10 + 5;
            startXRs = (int) (mRulerMiddleX + mAverage * (5 - remainder));
        }
        int endXRs = startXRs;
        while (startXRs <= mRight && valueRs <= mMaxValue) {
            canvas.drawLine(startXRs, startYRs, endXRs, endYRs, mPaint);
            startXRs = startXRs + mLineOffset;
            endXRs = startXRs;
            valueRs = valueRs + 10;
        }

        //画左侧长线
        int startXLl = (int) (mRulerMiddleX - mAverage * remainder);
        int endXLl = startXLl;
        int startYLl = mTop;
        int endYLl = mTop + mLineLong;
        int valueLl = (indicatorValue / 10) * 10;
        while (startXLl >= mLeft && valueLl >= mMinValue) {
            canvas.drawLine(startXLl, startYLl, endXLl, endYLl, mPaint);
            canvas.drawText("" + valueLl, startXLl, mValueY, mPaint);
            startXLl = startXLl - mLineOffset;
            endXLl = startXLl;
            valueLl = valueLl - 10;
        }

        //画左侧短线
        int startYLs = mTop;
        int endYLs = mTop + mLineLong / 2;
        int startXLs = 0;
        int valueLs = 0;
        if (remainder > 5) {
            valueLs = (indicatorValue / 10) * 10 + 5;
            startXLs = (int) (mRulerMiddleX - mAverage * (remainder - 5));
        } else {
            valueLs = (indicatorValue / 10 - 1) * 10 + 5;
            startXLs = (int) (mRulerMiddleX - mAverage * (remainder + 5));
        }
        int endXLs = startXLs;
        while (startXLs >= mLeft && valueLs >= mMinValue) {
            canvas.drawLine(startXLs, startYLs, endXLs, endYLs, mPaint);
            startXLs = startXLs - mLineOffset;
            endXLs = startXLs;
            valueLs = valueLs - 10;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private class RulerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if (animator != null) {
                animator.cancel();
            }
            mCurrentDownX = e.getX();
            //记录用户点击屏幕时候的坐标点
            mStartClickX = mCurrentDownX;
            mStartClickY = e.getY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            System.out.println("onScroll");
            if (mStartClickX >= mLeft && mStartClickX <= mRight &&
                    mStartClickY >= mTop && mStartClickY < mBottom) {
                float value = mCurrentValue + distanceX / mAverage;
                setCurrentValue(value);
                invalidate();
                if (listener != null) {
                    listener.onValueChange((int) mCurrentValue);
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            System.out.println("onFling");
            if (mStartClickX >= mLeft && mStartClickX <= mRight &&
                    mStartClickY >= mTop && mStartClickY < mBottom) {
                animator = ValueAnimator.ofFloat(velocityX, 0);
                animator.setDuration(1000);
//                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float animatedValue = (float) animation.getAnimatedValue();
                        float value = mCurrentValue - (animatedValue * (float) 0.005) / mAverage;
                        setCurrentValue(value);
                        invalidate();
                        if (listener != null) {
                            listener.onValueChange((int) mCurrentValue);
                        }
                    }
                });
                animator.start();
            }
            return true;
        }
    }

    public void setCurrentValue(float value) {
        mCurrentValue = value;
        if (mCurrentValue < mMinValue) {
            mCurrentValue = mMinValue;
        } else if (mCurrentValue > mMaxValue) {
            mCurrentValue = mMaxValue;
        }
    }

    private void setIndicatorValuePaint() {
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(60);
        //使文字居中显示
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void setIndicatorLinePaint() {
        mPaint.setColor(Color.parseColor("#FF0000"));
        mPaint.setStrokeWidth(3);
    }

    private void setRulerLinePaint() {
//        mPaint.setColor(Color.parseColor("#E2E2E2"));
        mPaint.setColor(Color.parseColor("#000000"));
        mPaint.setStrokeWidth(3);
    }

    private void setRulerBackgroundPaint() {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#F8F8F8"));
    }

    private void setRulerStrokePaint() {
        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(Color.parseColor("#E2E2E2"));
        mPaint.setColor(Color.parseColor("#000000"));
        mPaint.setStrokeWidth(mRulerStokeWidth);
    }

    public void setOnValueChangeListener(SizeViewValueChangeListener listener) {
        this.listener = listener;
    }


}
