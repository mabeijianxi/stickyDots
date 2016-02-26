package com.mabeijianxi.stickydotslib.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;

import com.mabeijianxi.stickydotslib.utils.DisplayUtils;
import com.mabeijianxi.stickydotslib.utils.GeometryUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * Created by mabeijianxi on 2016/1/19.
 */
public class StickyView extends View {

    private final Context mContext;
    /**
     * 拖拽圆到固定圆之间的距离
     */
    private float rangeMove;
    /**
     * 拖拽的view
     */
    private View mDragView;
    /**
     * 拖拽圆的圆心
     */
    private PointF mDragCanterPoint = new PointF(250, 550);
    /**
     * 固定圆的圆心
     */
    private PointF mFixCanterPoint = new PointF(250, 550);
    /**
     * 控制点
     */
    private PointF mCanterPoint = new PointF(250, 250);
    /**
     * 固定圆的切点
     */
    private PointF[] mFixTangentPointes = new PointF[]{new PointF(100, 190),
            new PointF(100, 210)};

    /**
     * 拖拽圆的切点
     */
    private PointF[] mDragTangentPoint = new PointF[]{new PointF(200, 180),
            new PointF(200, 220)};
    /**
     * 最大拖拽范围
     */
    private float mFarthestDistance;
    /**
     * 动画中固定员的最小半径
     */
    private float mMinFixRadius;
    /**
     * 拖拽圆半径
     */
    private float mDragRadius;
    /**
     * 固定圆半径
     */
    private float mFixRadius;
    /**
     * 超出范围
     */
    private boolean isOut;
    /**
     * 在超出范围的地方松手
     */
    private boolean isOutUp;
    private float startX;
    private float startY;
    private Paint mPaint;
    private Path mPath;
    private WindowManager mWm;
    private int mDragViewHeight;
    private int mDragViewWidth;
    private int mStatusBarHeight;
    private WindowManager.LayoutParams mParams;
    private DragStickViewListener dragStickViewListener;

    public StickyView(Context context, View mDragView, WindowManager mWm) {
        super(context);
        this.mContext=context;
        this.mDragView = mDragView;
        this.mWm = mWm;
        init();

    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPath = new Path();
//      需要手动测量
        mDragView.measure(1, 1);
        mDragViewHeight = mDragView.getMeasuredHeight() / 2;
        mDragViewWidth = mDragView.getMeasuredWidth() / 2;
        mDragRadius = mDragViewHeight;

        mFixRadius=DisplayUtils.dip2Dimension(8,mContext);
        mFarthestDistance=DisplayUtils.dip2Dimension(80,mContext);
        mMinFixRadius=DisplayUtils.dip2Dimension(3f,mContext);

        mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSLUCENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.gravity = Gravity.TOP | Gravity.LEFT;
    }


    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    /**
     * 设置状态栏高度，最好外面传进来，当view还没有绑定到窗体的时候是测量不到的
     * @param mStatusBarHeight
     */
    public void setStatusBarHeight(int mStatusBarHeight) {
        this.mStatusBarHeight = mStatusBarHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
//      需要去除状态栏高度偏差
        canvas.translate(0, -mStatusBarHeight);
//      移出了范围后将不再绘制链接部分和固定圆
        if (!isOut) {
//            根据圆心距动态绘制固定圆大小
            float mFixRadius = updateStickRadius();
            canvas.drawCircle(mFixCanterPoint.x, mFixCanterPoint.y, mFixRadius,
                    mPaint);
//      设置控制点，这里的控制点选择的是两圆心连接成的直线的中心位置
            mCanterPoint.set((mDragCanterPoint.x + mFixCanterPoint.x) / 2,
                    (mDragCanterPoint.y + mFixCanterPoint.y) / 2);
//            接下来是计算两个圆的外切点，会用到一点几何知识，忘了的回去找高中老师
            float dy = mDragCanterPoint.y - mFixCanterPoint.y;
            float dx = mDragCanterPoint.x - mFixCanterPoint.x;

            if (dx != 0) {
                float k1 = dy / dx;
                float k2 = -1 / k1;
                mDragTangentPoint = GeometryUtil.getIntersectionPoints(
                        mDragCanterPoint, mDragRadius, (double) k2);
                mFixTangentPointes = GeometryUtil.getIntersectionPoints(
                        mFixCanterPoint, mFixRadius, (double) k2);
            } else {
                mDragTangentPoint = GeometryUtil.getIntersectionPoints(
                        mDragCanterPoint, mDragRadius, (double) 0);
                mFixTangentPointes = GeometryUtil.getIntersectionPoints(
                        mFixCanterPoint, mFixRadius, (double) 0);
            }
//           必须重设上一次的路径
            mPath.reset();
//            moveTo顾名思义就是移动到某个位置，这里移动到固定圆的第一个外切点
            mPath.moveTo(mFixTangentPointes[0].x, mFixTangentPointes[0].y);
//           quadTo是绘制二阶贝塞尔曲线，这种曲线很想ps里面画矢量路径的那种。二阶的话需要一个控制点，一个起点一个终点
            mPath.quadTo(mCanterPoint.x, mCanterPoint.y,
                    mDragTangentPoint[0].x, mDragTangentPoint[0].y);
//            从上一个点绘制一条直线到下面这个位置
            mPath.lineTo(mDragTangentPoint[1].x, mDragTangentPoint[1].y);
//            再绘制一条二阶贝塞尔曲线
            mPath.quadTo(mCanterPoint.x, mCanterPoint.y,
                    mFixTangentPointes[1].x, mFixTangentPointes[1].y);
//            执行close，表示形成闭合路径
            mPath.close();
//            绘制到界面上
            canvas.drawPath(mPath, mPaint);

        }
//        当在范围外松手的时候是不再绘制拖拽圆的
        if (!isOutUp) {
            canvas.drawCircle(mDragCanterPoint.x, mDragCanterPoint.y,
                    mDragRadius, mPaint);
        }
//        参考范围，没实际作用
		/*mPaint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(mFixCanterPoint.x, mFixCanterPoint.y, mFarthestDistance, mPaint);
		mPaint.setStyle(Paint.Style.FILL);*/

        canvas.restore();
    }

    /**
     * 设置固定圆、拖拽圆、控制点的圆心坐标
     *
     * @param x
     * @param y
     */
    public void setShowCanterPoint(float x, float y) {
        mFixCanterPoint.set(x, y);
        mDragCanterPoint.set(x, y);
        mCanterPoint.set(x, y);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isOut = false;
                startX = event.getRawX();
                startY = event.getRawY();
                updateDragCenterPoint(startX, startY);
                break;
            case MotionEvent.ACTION_MOVE:
                float endX = event.getRawX();
                float endY = event.getRawY();
//                更加手的移动位置绘制拖拽圆的位置
                updateDragCenterPoint(endX, endY);
                distance();
//                当移出了规定的范围的时候
                if (rangeMove > mFarthestDistance) {
                    isOut = true;

                    if (dragStickViewListener != null) {
                        dragStickViewListener.outRangeMove(mDragCanterPoint);
                    }

                } else {
//                    不能把isOut改为false,因为移出一次后就算它移出过了
//				isOut=false;
                    isOutUp = false;
                    if (dragStickViewListener != null) {
                        dragStickViewListener.inRangeMove(mDragCanterPoint);
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
//                防止误操作
                mDragView.setEnabled(false);
                this.setEnabled(false);
                distance();
                if (isOut) {
                    outUp();
                }
                // 没有超出，做动画
                else {
                    inUp();
                }
                invalidate();
                break;
        }
        return true;
    }


    /**
     * 计算此时拖拽圆心到固定圆心的距离
     */
    private void distance() {
        rangeMove = GeometryUtil.getDistanceBetween2Points(
                mFixCanterPoint, mDragCanterPoint);
    }

    /**
     * 移动的时候一直在范围内，最后在范围内松手
     */
    private void inUp() {
        final PointF startPoint = new PointF(mDragCanterPoint.x,
                mDragCanterPoint.y);
        final PointF endPoint = new PointF(mFixCanterPoint.x,
                mFixCanterPoint.y);
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                PointF byPercent = GeometryUtil.getPointByPercent(
                        startPoint, endPoint, fraction);
                updateDragCenterPoint(byPercent.x, byPercent.y);
            }
        });
        addUpEndAnimListener(animator);
        animator.setInterpolator(new OvershootInterpolator(4.0f));
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 移动出规定范围
     */
    private void outUp() {
        // 外面松手
        if (rangeMove > mFarthestDistance) {
            isOutUp = true;
            if (dragStickViewListener != null) {
                dragStickViewListener.outRangeUp(mDragCanterPoint);
            }
        }
        // 里面松手
        else {
            isOutUp = false;
            if (dragStickViewListener != null) {
                dragStickViewListener.out2InRangeUp(mDragCanterPoint);
            }
        }
        updateDragCenterPoint(mFixCanterPoint.x, mFixCanterPoint.y);
    }

    /**
     * 没有超出范围松手会弹后的回调
     *
     * @param animator
     */
    private void addUpEndAnimListener(ValueAnimator animator) {
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (dragStickViewListener != null) {
                    dragStickViewListener.inRangeUp(mDragCanterPoint);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 更新拖拽圆圆心
     */
    private void updateDragCenterPoint(float x, float y) {
        mDragCanterPoint.set(x, y);
        updateManagerView(x, y);
        invalidate();
    }

    private void updateManagerView(float x, float y) {
        mParams.x=(int)(x-mDragViewWidth);
        mParams.y=(int)(y-mDragViewHeight-mStatusBarHeight);
        try {
            mWm.updateViewLayout(mDragView, mParams);
        }
        catch (Exception e){

        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /**
         * 获取状态栏高度
         */
        mStatusBarHeight = DisplayUtils.getStatusBarHeight(this);

    }

    /**
     * 计算拖动过程中固定圆的半径
     */
    private float updateStickRadius() {
        float distance = GeometryUtil.getDistanceBetween2Points(mDragCanterPoint, mFixCanterPoint);
        distance = Math.min(distance, mFarthestDistance);
        float percent = distance * 1.0f / mFarthestDistance;

        return mFixRadius + (mMinFixRadius - mFixRadius) * percent;
    }

    /**
     * 设置画笔颜色
     * @param mPaintColor
     */
    public void setPaintColor(int mPaintColor) {
        if(mPaint!=null){
            mPaint.setColor(mPaintColor);
        }
    }

    /**
     * 得到最大拖拽范围
     * @return
     */
    public float getmFarthestDistance() {
        return mFarthestDistance;
    }

    /**
     * 设置最大拖拽范围
     * @param mFarthestDistance
     */
    public void setFarthestDistance(float mFarthestDistance) {
        this.mFarthestDistance = mFarthestDistance;
    }
    public float getmMinFixRadius() {
        return mMinFixRadius;
    }

    /**
     * 设置拖拽过程中固定圆变化的最小半径值
     * @param mMinFixRadius
     */
    public void setMinFixRadius(float mMinFixRadius) {
        this.mMinFixRadius = mMinFixRadius;
    }
    public float getmDragRadius() {
        return mDragRadius;
    }

    /**
     * 设置拖拽圆半径
     * @param mDragRadius
     */
    public void setmDragRadius(float mDragRadius) {
        this.mDragRadius = mDragRadius;
    }

    public float getmFixRadius() {
        return mFixRadius;
    }

    /**
     * 设置固定圆半径
     * @param mFixRadius
     */
    public void setFixRadius(float mFixRadius) {
        this.mFixRadius = mFixRadius;
    }
    /**
     * 拖拽过程监听接口
     */
    public interface DragStickViewListener {
        /**
         * 在范围内移动回调
         * @param dragCanterPoint 拖拽的中心坐标
         */
        void inRangeMove(PointF dragCanterPoint);
        /**
         * 在范围外移动回调
         * @param dragCanterPoint 拖拽的中心坐标
         */
        void outRangeMove(PointF dragCanterPoint);
        /**
         *  当移出了规定范围，最后在范围内松手的回调
         * @param dragCanterPoint
         */
        void out2InRangeUp(PointF dragCanterPoint);
        /**
         * 当移出了规定范围，最后在范围外松手的回调
         * @param dragCanterPoint
         */
        void outRangeUp(PointF dragCanterPoint);
        /**
         * 一直没有移动出范围，在范围内松手的回调
         * @param dragCanterPoint
         */
        void inRangeUp(PointF dragCanterPoint);
    }

    public DragStickViewListener getDragStickViewListener() {
        return dragStickViewListener;
    }

    public void setDragStickViewListener(DragStickViewListener dragStickViewListener) {
        this.dragStickViewListener = dragStickViewListener;
    }
}
