## stickyDots
#####类似QQ的小红点，QQ can drag the little red dot,先看下效果图：

![demo](http://7xq6db.com1.z0.glb.clouddn.com/stickydots.gif)


[Download Demo](https://github.com/mabeijianxi/stickyDots/blob/master/simple.apk)

## Usage

###Step-1

###Gradle
`dependencies {
    compile project(':stickyDotsLib')}`

---
###Step-2
Choose the right place to create it，for example：

//             Note for needed to achieve drag effect of view needs to

//              individually specify a layout file,and layout is best cannot have viewGroup,

//              or view shown above the text may in drag does not recognize.

//              This is in order to facilitate, in order to reduce consumption.

            StickyViewHelper stickyViewHelper = new StickyViewHelper(mContext, viewHolder.mDragView,R.layout.includeview);

The layout of includeview like this:

`<?xml version="1.0" encoding="utf-8"?>`  
`<TextView xmlns:`
	`android="http://schemas.android.com/apk/res/android"`  
   ` android:id="@+id/mDragView"`  
   ` android:layout_width="wrap_content"`  
    `android:layout_height="20dp"`  
    `android:background="@drawable/red_bg"`  
  `  android:gravity="center"`  
  `  android:layout_gravity="center"`  
 `   android:singleLine="true"`  
  `  android:text="1"`
    `android:textSize="13sp"`  
    `android:textColor="@android:color/white"/>`

You can monitor each process of mobile:


	/**
     * view在范围内移动指此此Runnable
     * @param viewInRangeMoveRun
     */
    public void setViewInRangeMoveRun(Runnable viewInRangeMoveRun) {
        this.viewInRangeMoveRun = viewInRangeMoveRun;
    }

    /**
     * view在范围外移动执行此Runnable
     * @param viewOutRangeMoveRun
     */
    public void setViewOutRangeMoveRun(Runnable viewOutRangeMoveRun) {
        this.viewOutRangeMoveRun = viewOutRangeMoveRun;
    }

    /**
     * view移出过范围，最后在范围内松手执行次Runnable
     * @param viewOut2InRangeUpRun
     */
    public void setViewOut2InRangeUpRun(Runnable viewOut2InRangeUpRun) {
        this.viewOut2InRangeUpRun = viewOut2InRangeUpRun;
    }

    /**
     * view没有移出过范围，在范围内松手
     * @param mViewInRangeUpRun
     */
    public void setViewInRangeUpRun(Runnable mViewInRangeUpRun) {
        this.mViewInRangeUpRun = mViewInRangeUpRun;
    }

    /**
     * view移出范围，最后在范围外松手
     * @param viewOutRangeUpRun
     */
    public void setViewOutRangeUpRun(Runnable viewOutRangeUpRun) {
        this.viewOutRangeUpRun = viewOutRangeUpRun;
    }`

You can also specify mapping rules:



    /**
     * 设置最大拖拽范围
     * @param mFarthestDistance px
     */
    public void setFarthestDistance(float mFarthestDistance) {
        this.mFarthestDistance = mFarthestDistance;
    }

    /**
     * 设置拖拽过程中固定圆变化的最小半径值
     * @param mMinFixRadius px
     */
    public void setMinFixRadius(float mMinFixRadius) {
        this.mMinFixRadius = mMinFixRadius;
    }

    /**
     * 设置固定圆半径
     * @param mFixRadius px
     */
    public void setFixRadius(float mFixRadius) {
        this.mFixRadius = mFixRadius;
    }

    /**
     * 设置绘制颜色
     * @param mPathColor
     */
    public void setmPathColor(int mPathColor) {
        this.mPathColor = mPathColor;
    }`


##Theory
	
![Theory](http://7xq6db.com1.z0.glb.clouddn.com/%E7%B2%98%E6%80%A7%E6%8E%A7%E4%BB%B6.png)

It can be decomposed into three parts, a fixed circle, a drag and drop round, a connection rod.
According to the fingers to move the position of the drawing is ok.It need a little geometry knowledge.

`canvas.drawCircle(mFixCanterPoint.x, mFixCanterPoint.y, mFixRadius,
                    mPaint);`

`mPath.moveTo(mFixTangentPointes[0].x, mFixTangentPointes[0].y);`

`mPath.quadTo(mCanterPoint.x, mCanterPoint.y,
                    mDragTangentPoint[0].x, mDragTangentPoint[0].y);`

` mPath.lineTo(mDragTangentPoint[1].x, mDragTangentPoint[1].y);`

` mPath.quadTo(mCanterPoint.x, mCanterPoint.y,
                    mFixTangentPointes[1].x, mFixTangentPointes[1].y);`

` mPath.close();`
           ` canvas.drawPath(mPath, mPaint);`

`canvas.drawCircle(mDragCanterPoint.x, mDragCanterPoint.y,
                    mDragRadius, mPaint);`

Specific, you can download to learn more

##有什么不足的地方欢迎大家提出，也很乐意和大家交流，我的联系邮箱是*mabeijianxi@gmail.com*