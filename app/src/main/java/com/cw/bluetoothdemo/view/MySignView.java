package com.cw.bluetoothdemo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 作者：杨金玲 on 2017/12/28 08:39
 * 邮箱：18363820101@163.com
 */

public class MySignView extends View {

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;
    private Path path;
    private boolean isSetBitmap;
    private int totalNum; // 用户画了几笔

    public MySignView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth((float) 10);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(),
                    Bitmap.Config.ARGB_8888);
            this.canvas = new Canvas(bitmap);
            this.canvas.drawColor(Color.WHITE);
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(event.getX(), event.getY());
                path.lineTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(touchX, touchY);
                canvas.drawPath(path, paint);
                break;

            case MotionEvent.ACTION_UP:
                totalNum++;
                path.lineTo(touchX, touchY);
                canvas.drawPath(path, paint);
                break;

            default:
                break;
        }
        invalidate();
        return true;
    }

    public void empty() {
        isSetBitmap = false;
        bitmap.recycle();
        bitmap = null;
        path.reset();
        invalidate();
        totalNum = 0;
    }

    public Bitmap getBitmap() {
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        isSetBitmap = true;
        this.bitmap = bitmap;
        this.canvas = new Canvas(bitmap);
        invalidate();
    }

    public boolean isEmpty() {
        return path.isEmpty() && !isSetBitmap;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    private int lineColor;
    private float lineWidth;

    /**
     * @param lineColor the lineColor to set
     */
    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        paint.setColor(lineColor);
    }

    /**
     * @param lineWidth the lintWidth to set
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        paint.setStrokeWidth(lineWidth);
    }
}
