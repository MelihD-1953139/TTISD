// OverlayView.java
package com.example.tttest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    private Paint paint;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int rectWidth = width / 24;
        int rectHeight = height;

        for (int i = 0; i < 24; i++) {
            int left = i * rectWidth;
            int top = 0;
            int right = left + rectWidth;
            int bottom = top + rectHeight;
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}
