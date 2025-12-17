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

import android.graphics.Point;

import java.util.Objects;

/**
 * 检测点
 * Created by Alex on 2025/12/16.
 */
public final class DocumentSkewCorrectionPoints {
    public static final int POINT_NONE = 0;
    public static final int POINT_LT = 1;
    public static final int POINT_RT = 2;
    public static final int POINT_LB = 3;
    public static final int POINT_RB = 4;
    private final float[] mPoints = new float[8];
    private int mWidth;
    private int mHeight;
    private boolean mSet;

    public DocumentSkewCorrectionPoints() {
    }

    DocumentSkewCorrectionPoints(int width, int height,
                                 float ltx, float lty, float rtx, float rty,
                                 float lbx, float lby, float rbx, float rby) {
        mPoints[0] = ltx;
        mPoints[1] = lty;
        mPoints[2] = rtx;
        mPoints[3] = rty;
        mPoints[4] = lbx;
        mPoints[5] = lby;
        mPoints[6] = rbx;
        mPoints[7] = rby;
        mWidth = width;
        mHeight = height;
        mSet = true;
    }

    DocumentSkewCorrectionPoints(DocumentSkewCorrectionPoints points) {
        mPoints[0] = points.mPoints[0];
        mPoints[1] = points.mPoints[1];
        mPoints[2] = points.mPoints[2];
        mPoints[3] = points.mPoints[3];
        mPoints[4] = points.mPoints[4];
        mPoints[5] = points.mPoints[5];
        mPoints[6] = points.mPoints[6];
        mPoints[7] = points.mPoints[7];
        mWidth = points.mWidth;
        mHeight = points.mHeight;
        mSet = points.mSet;
    }

    static int adjustPoints(float[] points, int controlPoint, int w, int h,
                                   float ltx, float lty, float rtx, float rty,
                                   float lbx, float lby, float rbx, float rby) {
        if (controlPoint == POINT_LT) {
            // 变更左上点
            // 情况1：LT与LB连线与RT与RB连线出现交点，LT与RT互换
            final boolean intersect1 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (intersect1) {
                // 交换
                points[0] = rtx;
                points[1] = rty;
                points[2] = ltx;
                points[3] = lty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rbx;
                points[7] = rby;
                return POINT_RT;
            }
            // 情况2：LT与RT连线与LB与RB连续出现交点，LT与LB互换
            final boolean intersect2 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (intersect2) {
                // 交换
                points[0] = lbx;
                points[1] = lby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = ltx;
                points[5] = lty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LB;
            }
            // 情况3：LT与(0,0)的距离大于RB与(0,0)的距离，LT与RB互换
            final double d1 = Utils.calculatePointToPoint(0, 0, ltx, lty);
            final double d2 = Utils.calculatePointToPoint(0, 0, rbx, rby);
            if (d1 > d2) {
                // 交换
                points[0] = rbx;
                points[1] = rby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = ltx;
                points[7] = lty;
                return POINT_RB;
            }
        } else if (controlPoint == POINT_RT) {
            // 变更右上点
            // 情况1：LT与LB连线与RT与RB连线出现交点，RT与LT互换
            final boolean intersect1 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (intersect1) {
                // 交换
                points[0] = rtx;
                points[1] = rty;
                points[2] = ltx;
                points[3] = lty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LT;
            }
            // 情况2：LT与RT连线与LB与RB连续出现交点，RT与RB互换
            final boolean intersect2 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (intersect2) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rbx;
                points[3] = rby;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rtx;
                points[7] = rty;
                return POINT_RB;
            }
            // 情况3：RT与(w,0)的距离大于LB与(w,0)的距离，RT与LB互换
            final double d1 = Utils.calculatePointToPoint(w, 0, rtx, rty);
            final double d2 = Utils.calculatePointToPoint(w, 0, lbx, lby);
            if (d1 > d2) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = lbx;
                points[3] = lby;
                points[4] = rtx;
                points[5] = rty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LB;
            }
        } else if (controlPoint == POINT_LB) {
            // 变更左下点
            // 情况1：LT与LB连线与RT与RB连线出现交点，LB与RB互换
            final boolean intersect1 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (intersect1) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rtx;
                points[3] = rty;
                points[4] = rbx;
                points[5] = rby;
                points[6] = lbx;
                points[7] = lby;
                return POINT_RB;
            }
            // 情况2：LT与RT连线与LB与RB连续出现交点，LB与LT互换
            final boolean intersect2 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (intersect2) {
                // 交换
                points[0] = lbx;
                points[1] = lby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = ltx;
                points[5] = lty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_LT;
            }
            // 情况3：LB与(0,h)的距离大于RT与(0,h)的距离，LB与RT互换
            final double d1 = Utils.calculatePointToPoint(0, h, lbx, lby);
            final double d2 = Utils.calculatePointToPoint(0, h, rtx, rty);
            if (d1 > d2) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = lbx;
                points[3] = lby;
                points[4] = rtx;
                points[5] = rty;
                points[6] = rbx;
                points[7] = rby;
                return POINT_RT;
            }
        } else if (controlPoint == POINT_RB) {
            // 变更右下点
            // 情况1：LT与LB连线与RT与RB连线出现交点，RB与LB互换
            final boolean intersect1 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, lbx, lby, rtx, rty, rbx, rby);
            if (intersect1) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rtx;
                points[3] = rty;
                points[4] = rbx;
                points[5] = rby;
                points[6] = lbx;
                points[7] = lby;
                return POINT_LB;
            }
            // 情况2：LT与RT连线与LB与RB连续出现交点，RB与RT互换
            final boolean intersect2 = Utils.isIntersectLineSegmentToLineSegment(
                    ltx, lty, rtx, rty, lbx, lby, rbx, rby);
            if (intersect2) {
                // 交换
                points[0] = ltx;
                points[1] = lty;
                points[2] = rbx;
                points[3] = rby;
                points[4] = lbx;
                points[5] = lby;
                points[6] = rtx;
                points[7] = rty;
                return POINT_RT;
            }
            // 情况3：RB与(w,h)的距离大于LT与(w,h)的距离，RB与LT互换
            final double d1 = Utils.calculatePointToPoint(w, h, rbx, rby);
            final double d2 = Utils.calculatePointToPoint(w, h, ltx, lty);
            if (d1 > d2) {
                // 交换
                points[0] = rbx;
                points[1] = rby;
                points[2] = rtx;
                points[3] = rty;
                points[4] = lbx;
                points[5] = lby;
                points[6] = ltx;
                points[7] = lty;
                return POINT_LT;
            }
        }
        // 默认
        points[0] = ltx;
        points[1] = lty;
        points[2] = rtx;
        points[3] = rty;
        points[4] = lbx;
        points[5] = lby;
        points[6] = rbx;
        points[7] = rby;
        return controlPoint;
    }

    /**
     * 重置
     */
    public void reset() {
        mWidth = 0;
        mHeight = 0;
        mPoints[0] = 0;
        mPoints[1] = 0;
        mPoints[2] = 0;
        mPoints[3] = 0;
        mPoints[4] = 0;
        mPoints[5] = 0;
        mPoints[6] = 0;
        mPoints[7] = 0;
        mSet = false;
    }

    void set(int width, int height, int[] ps) {
        mWidth = width;
        mHeight = height;
        mPoints[0] = ps[0];
        mPoints[1] = ps[1];
        mPoints[2] = ps[2];
        mPoints[3] = ps[3];
        mPoints[4] = ps[4];
        mPoints[5] = ps[5];
        mPoints[6] = ps[6];
        mPoints[7] = ps[7];
        mSet = true;
    }

    void set(int width, int height, Point lt, Point rt,  Point lb, Point rb) {
        mWidth = width;
        mHeight = height;
        mPoints[0] = lt.x;
        mPoints[1] = lt.y;
        mPoints[2] = rt.x;
        mPoints[3] = rt.y;
        mPoints[4] = lb.x;
        mPoints[5] = lb.y;
        mPoints[6] = rb.x;
        mPoints[7] = rb.y;
        mSet = true;
    }

    void set(int originalWidth, int originalHeight,
             int scaledWidth, int scaledHeight, int[] ps) {
        mWidth = originalWidth;
        mHeight = originalHeight;
        mPoints[0] = 1f * ps[0] / scaledWidth * originalWidth;
        mPoints[1] = 1f * ps[1] / scaledHeight * originalHeight;
        mPoints[2] = 1f * ps[2] / scaledWidth * originalWidth;
        mPoints[3] = 1f * ps[3] / scaledHeight * originalHeight;
        mPoints[4] = 1f * ps[4] / scaledWidth * originalWidth;
        mPoints[5] = 1f * ps[5] / scaledHeight * originalHeight;
        mPoints[6] = 1f * ps[6] / scaledWidth * originalWidth;
        mPoints[7] = 1f * ps[7] / scaledHeight * originalHeight;
        mSet = true;
    }

    void set(int originalWidth, int originalHeight, int scaledWidth, int scaledHeight,
             Point lt, Point rt,  Point lb, Point rb) {
        mWidth = originalWidth;
        mHeight = originalHeight;
        mPoints[0] = 1f * lt.x / scaledWidth * originalWidth;
        mPoints[1] = 1f * lt.y / scaledHeight * originalHeight;
        mPoints[2] = 1f * rt.x / scaledWidth * originalWidth;
        mPoints[3] = 1f * rt.y / scaledHeight * originalHeight;
        mPoints[4] = 1f * lb.x / scaledWidth * originalWidth;
        mPoints[5] = 1f * lb.y / scaledHeight * originalHeight;
        mPoints[6] = 1f * rb.x / scaledWidth * originalWidth;
        mPoints[7] = 1f * rb.y / scaledHeight * originalHeight;
        mSet = true;
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * 获取高度
     *
     * @return 高度
     */
    public int getHeight() {
        return mHeight;
    }

    public float getLTX(int width) {
        if (width == 0 || mWidth == 0) {
            return 0;
        }
        return width == mWidth ? mPoints[0] : mPoints[0] / mWidth * width;
    }

    public float getLTY(int height) {
        if (height == 0 || mHeight == 0) {
            return 0;
        }
        return height == mHeight ? mPoints[1] : mPoints[1] / mHeight * height;
    }

    public float getRTX(int width) {
        if (width == 0 || mWidth == 0) {
            return 0;
        }
        return width == mWidth ? mPoints[2] : mPoints[2] / mWidth * width;
    }

    public float getRTY(int height) {
        if (height == 0 || mHeight == 0) {
            return 0;
        }
        return height == mHeight ? mPoints[3] : mPoints[3] / mHeight * height;
    }

    public float getLBX(int width) {
        if (width == 0 || mWidth == 0) {
            return 0;
        }
        return width == mWidth ? mPoints[4] : mPoints[4] / mWidth * width;
    }

    public float getLBY(int height) {
        if (height == 0 || mHeight == 0) {
            return 0;
        }
        return height == mHeight ? mPoints[5] : mPoints[5] / mHeight * height;
    }

    public float getRBX(int width) {
        if (width == 0 || mWidth == 0) {
            return 0;
        }
        return width == mWidth ? mPoints[6] : mPoints[6] / mWidth * width;
    }

    public float getRBY(int height) {
        if (height == 0 || mHeight == 0) {
            return 0;
        }
        return height == mHeight ? mPoints[7] : mPoints[7] / mHeight * height;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DocumentSkewCorrectionPoints that = (DocumentSkewCorrectionPoints) o;
        return mWidth == that.mWidth && mHeight == that.mHeight && mSet == that.mSet
                && Objects.deepEquals(mPoints, that.mPoints);
    }
}
