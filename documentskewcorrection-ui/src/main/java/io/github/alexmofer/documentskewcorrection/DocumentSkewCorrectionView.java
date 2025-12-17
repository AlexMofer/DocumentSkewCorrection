/*
 * Copyright (C) 2025 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.alexmofer.documentskewcorrection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Magnifier;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.util.TypedValueCompat;

import java.util.Objects;

import io.github.alexmofer.documentskewcorrection.ui.R;

/**
 * 校正点控制器
 * Created by Alex on 2025/5/28.
 */
public class DocumentSkewCorrectionView extends AppCompatImageView {
    private final float[] tPoints = new float[8];
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();
    private final float[] mTouchDownPoints = new float[8];
    private int mStrokeColor = Color.BLUE;
    private int mFillColor = Color.WHITE;
    private DocumentSkewCorrectionPoints mPoints;
    private int mPointRadius;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private float mOffsetX;
    private float mOffsetY;
    private float mLTX;
    private float mLTY;
    private float mRTX;
    private float mRTY;
    private float mLBX;
    private float mLBY;
    private float mRBX;
    private float mRBY;
    private int mTouchPoint = DocumentSkewCorrectionPoints.POINT_NONE;
    private float mTouchOffsetX = 0;
    private float mTouchOffsetY = 0;
    private float mTouchSlop;
    private float mTouchPointX;
    private float mTouchPointY;
    private Object mMagnifier;
    private float mMagnifierOffset;

    public DocumentSkewCorrectionView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DocumentSkewCorrectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DocumentSkewCorrectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mPointRadius = Math.round(TypedValueCompat.dpToPx(8, metrics));
        mMagnifierOffset = TypedValueCompat.dpToPx(64, metrics);
        ViewUtils.obtainStyledAttributes(
                custom -> {
                    mStrokeColor = custom.getColor(
                            R.styleable.DocumentSkewCorrectionView_android_strokeColor, mStrokeColor);
                    mFillColor = custom.getColor(
                            R.styleable.DocumentSkewCorrectionView_android_fillColor, mFillColor);
                    float strokeWidth = TypedValueCompat.dpToPx(2, metrics);
                    if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_android_lineHeight)) {
                        strokeWidth = custom.getDimension(
                                R.styleable.DocumentSkewCorrectionView_android_lineHeight, strokeWidth);
                    }
                    if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_DSC_strokeWidth)) {
                        strokeWidth = custom.getDimension(
                                R.styleable.DocumentSkewCorrectionView_DSC_strokeWidth, strokeWidth);
                    }
                    if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_android_innerRadius)) {
                        mPointRadius = custom.getDimensionPixelOffset(
                                R.styleable.DocumentSkewCorrectionView_android_innerRadius, mPointRadius);
                    }
                    if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_DSC_pointRadius)) {
                        mPointRadius = custom.getDimensionPixelOffset(
                                R.styleable.DocumentSkewCorrectionView_DSC_pointRadius, mPointRadius);
                    }
                    if (custom.hasValue(R.styleable.DocumentSkewCorrectionView_DSC_magnifierOffset)) {
                        mMagnifierOffset = custom.getDimension(
                                R.styleable.DocumentSkewCorrectionView_DSC_magnifierOffset, mMagnifierOffset);
                    }
                    mPaint.setStrokeWidth(strokeWidth);
                }, context, attrs,
                R.styleable.DocumentSkewCorrectionView, defStyleAttr, 0);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mMagnifier = new Magnifier(this);
        }
        mTouchSlop = mPointRadius + ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable src = getDrawable();
        if (src == null) {
            return;
        }
        refreshParams();
        canvas.save();
        canvas.translate(mOffsetX, mOffsetY);
        draw(canvas, mPaint, mPath, mLTX, mLTY, mRTX, mRTY, mRBX, mRBY, mLBX, mLBY, mTouchPoint);
        canvas.restore();
    }

    protected void draw(Canvas canvas, Paint paint, Path path,
                        float ltx, float lty, float rtx, float rty,
                        float rbx, float rby, float lbx, float lby,
                        int touchPoint) {
        paint.setColor(mStrokeColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        paint.setColor(mFillColor);
        paint.setStyle(Paint.Style.FILL);
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_LT) {
            canvas.drawCircle(ltx, lty, mPointRadius, paint);
        }
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_RT) {
            canvas.drawCircle(rtx, rty, mPointRadius, paint);
        }
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_RB) {
            canvas.drawCircle(rbx, rby, mPointRadius, paint);
        }
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_LB) {
            canvas.drawCircle(lbx, lby, mPointRadius, paint);
        }
        paint.setColor(mStrokeColor);
        paint.setStyle(Paint.Style.STROKE);
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_LT) {
            canvas.drawCircle(ltx, lty, mPointRadius, paint);
        }
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_RT) {
            canvas.drawCircle(rtx, rty, mPointRadius, paint);
        }
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_RB) {
            canvas.drawCircle(rbx, rby, mPointRadius, paint);
        }
        if (touchPoint != DocumentSkewCorrectionPoints.POINT_LB) {
            canvas.drawCircle(lbx, lby, mPointRadius, paint);
        }
    }

    private void refreshParams() {
        final Drawable src = getDrawable();
        if (src != null) {
            final int drawableWidth = src.getIntrinsicWidth();
            final int drawableHeight = src.getIntrinsicHeight();
            final int paddingLeft = getPaddingLeft();
            final int paddingRight = getPaddingRight();
            final int paddingTop = getPaddingTop();
            final int paddingBottom = getPaddingBottom();
            mOffsetX = paddingLeft + (getWidth() - paddingLeft - paddingRight) * 0.5f - drawableWidth * 0.5f;
            mOffsetY = paddingTop + (getHeight() - paddingTop - paddingBottom) * 0.5f - drawableHeight * 0.5f;
            if (drawableWidth != mDrawableWidth || drawableHeight != mDrawableHeight) {
                mDrawableWidth = drawableWidth;
                mDrawableHeight = drawableHeight;
                if (mPoints == null) {
                    mLTX = 0;
                    mLTY = 0;
                    mRTX = mDrawableWidth;
                    mRTY = 0;
                    mLBX = 0;
                    mLBY = mDrawableHeight;
                    mRBX = mDrawableWidth;
                    mRBY = mDrawableHeight;
                } else {
                    mLTX = mPoints.getLTX(mDrawableWidth);
                    mLTY = mPoints.getLTY(mDrawableHeight);
                    mRTX = mPoints.getRTX(mDrawableWidth);
                    mRTY = mPoints.getRTY(mDrawableHeight);
                    mLBX = mPoints.getLBX(mDrawableWidth);
                    mLBY = mPoints.getLBY(mDrawableHeight);
                    mRBX = mPoints.getRBX(mDrawableWidth);
                    mRBY = mPoints.getRBY(mDrawableHeight);
                }
                refreshPath();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean superResult = super.onTouchEvent(event);
        final Drawable src = getDrawable();
        if (src == null) {
            return superResult;
        }
        final int action = event.getAction();
        final float x = event.getX() - mOffsetX;
        final float y = event.getY() - mOffsetY;
        if (action == MotionEvent.ACTION_DOWN) {
            // 确定当前触摸点为原始控制点的哪一个
            mTouchPoint = getTouchPoint(x, y);
            mTouchPointX = -1;
            mTouchPointY = -1;
            if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_LT) {
                startTouch(x, y, mLTX, mLTY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_RT) {
                startTouch(x, y, mRTX, mRTY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_LB) {
                startTouch(x, y, mLBX, mLBY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_RB) {
                startTouch(x, y, mRBX, mRBY);
                invalidate();
                showMagnifier(event.getX(), event.getY());
                return true;
            } else {
                return superResult;
            }
        }
        if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_NONE) {
            return superResult;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            mLTX = mTouchDownPoints[0];
            mLTY = mTouchDownPoints[1];
            mRTX = mTouchDownPoints[2];
            mRTY = mTouchDownPoints[3];
            mLBX = mTouchDownPoints[4];
            mLBY = mTouchDownPoints[5];
            mRBX = mTouchDownPoints[6];
            mRBY = mTouchDownPoints[7];
            refreshPath();
            onPointsChangedByTouch();
            mTouchPoint = DocumentSkewCorrectionPoints.POINT_NONE;
            invalidate();
            dismissMagnifier();
            return superResult;
        }
        final float pointX = Math.max(0, Math.min(mDrawableWidth, (x + mTouchOffsetX)));
        final float pointY = Math.max(0, Math.min(mDrawableHeight, (y + mTouchOffsetY)));
        if (mTouchPointX != pointX || mTouchPointY != pointY) {
            mTouchPointX = pointX;
            mTouchPointY = pointY;
            final float[] points = tPoints;
            if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_LT) {
                mTouchPoint = DocumentSkewCorrectionPoints.adjustPoints(
                        points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mTouchPointX, mTouchPointY, mRTX, mRTY, mLBX, mLBY, mRBX, mRBY);
            } else if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_RT) {
                mTouchPoint = DocumentSkewCorrectionPoints.adjustPoints(
                        points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mLTX, mLTY, mTouchPointX, mTouchPointY, mLBX, mLBY, mRBX, mRBY);
            } else if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_LB) {
                mTouchPoint = DocumentSkewCorrectionPoints.adjustPoints(
                        points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mLTX, mLTY, mRTX, mRTY, mTouchPointX, mTouchPointY, mRBX, mRBY);
            } else if (mTouchPoint == DocumentSkewCorrectionPoints.POINT_RB) {
                mTouchPoint = DocumentSkewCorrectionPoints.adjustPoints(
                        points, mTouchPoint, mDrawableWidth, mDrawableHeight,
                        mLTX, mLTY, mRTX, mRTY, mLBX, mLBY, mTouchPointX, mTouchPointY);
            }
            mLTX = points[0];
            mLTY = points[1];
            mRTX = points[2];
            mRTY = points[3];
            mLBX = points[4];
            mLBY = points[5];
            mRBX = points[6];
            mRBY = points[7];
            refreshPath();
            onPointsChangedByTouch();
            invalidate();
        }
        if (action == MotionEvent.ACTION_UP) {
            mTouchPoint = DocumentSkewCorrectionPoints.POINT_NONE;
            invalidate();
            dismissMagnifier();
        } else {
            showMagnifier(event.getX(), event.getY());
        }
        return true;
    }

    /**
     * 获取校正点
     *
     * @return 校正点
     */
    @Nullable
    public DocumentSkewCorrectionPoints getPoints() {
        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return mPoints == null ? null : new DocumentSkewCorrectionPoints(mPoints);
        }
        return new DocumentSkewCorrectionPoints(mDrawableWidth, mDrawableHeight,
                mLTX, mLTY, mRTX, mRTY, mLBX, mLBY, mRBX, mRBY);
    }

    /**
     * 设置校正点
     *
     * @param points 校正点
     */
    public void setPoints(@Nullable DocumentSkewCorrectionPoints points) {
        if (Objects.equals(mPoints, points)) {
            return;
        }
        mPoints = points == null ? null : new DocumentSkewCorrectionPoints(points);
        mTouchPoint = DocumentSkewCorrectionPoints.POINT_NONE;
        // 置0用于刷新参数
        mDrawableWidth = 0;
        mDrawableHeight = 0;
        invalidate();
    }

    /**
     * 控制点因触摸事件而改变
     */
    protected void onPointsChangedByTouch() {
    }

    private void refreshPath() {
        mPath.rewind();
        mPath.moveTo(mLTX, mLTY);
        mPath.lineTo(mRTX, mRTY);
        mPath.lineTo(mRBX, mRBY);
        mPath.lineTo(mLBX, mLBY);
        mPath.close();
    }

    private int getTouchPoint(float x, float y) {
        double distance = Utils.calculatePointToPoint(x, y, mLBX, mLBY);
        if (mTouchSlop > distance) {
            return DocumentSkewCorrectionPoints.POINT_LB;
        }
        distance = Utils.calculatePointToPoint(x, y, mRBX, mRBY);
        if (mTouchSlop > distance) {
            return DocumentSkewCorrectionPoints.POINT_RB;
        }
        distance = Utils.calculatePointToPoint(x, y, mRTX, mRTY);
        if (mTouchSlop > distance) {
            return DocumentSkewCorrectionPoints.POINT_RT;
        }
        distance = Utils.calculatePointToPoint(x, y, mLTX, mLTY);
        if (mTouchSlop > distance) {
            return DocumentSkewCorrectionPoints.POINT_LT;
        }
        return DocumentSkewCorrectionPoints.POINT_NONE;
    }

    private void startTouch(float touchX, float touchY, float pointX, float pointY) {
        mTouchDownPoints[0] = mLTX;
        mTouchDownPoints[1] = mLTY;
        mTouchDownPoints[2] = mRTX;
        mTouchDownPoints[3] = mRTY;
        mTouchDownPoints[4] = mLBX;
        mTouchDownPoints[5] = mLBY;
        mTouchDownPoints[6] = mRBX;
        mTouchDownPoints[7] = mRBY;
        mTouchOffsetX = pointX - touchX;
        mTouchOffsetY = pointY - touchY;
    }

    private void showMagnifier(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ((Magnifier) mMagnifier).show(x, y, x, y - mMagnifierOffset);
            return;
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            ((Magnifier) mMagnifier).show(x, y);
        }
    }

    private void dismissMagnifier() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ((Magnifier) mMagnifier).dismiss();
        }
    }
}
