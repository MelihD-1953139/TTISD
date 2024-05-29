package com.example.tttest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    private Paint paint;
    public static final float INNER_KEY_WIDTH_FACTOR = 0.915f; // Adjusted factor to make inner keys slightly smaller
    public static final float OUTER_KEY_WIDTH_FACTOR = 0.95f; // Adjusted factor to make outer keys slightly wider
    private static final int EXTRA_SPACING = 5; // Extra spacing in pixels

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

        // Set the height of each rectangle to be 25% of the total height
        int rectHeight = height / 7;

        // Place the rectangles at 3/4 of the height
        int topOffset = (height *  72/100) - (rectHeight / 2);

        // Calculate the width of the middle key
        int numKeys = 24;
        int middleKeyIndex = numKeys / 2;
        float totalWidthWithoutMargins = (float) (width * 0.9); // 90% of the width for keys
        float middleKeyWidth = totalWidthWithoutMargins / (numKeys + (OUTER_KEY_WIDTH_FACTOR - 1) * 2);

        // Ensure the total width does not exceed the screen width
        float totalWidth = middleKeyWidth * numKeys;
        if (totalWidth > width) {
            middleKeyWidth *= width / totalWidth;
        }

        // Draw the rectangles with variable widths
        int currentLeft = (width - (int)totalWidth) / 2; // Center the rectangles horizontally
        for (int i = 0; i < numKeys; i++) {
            float rectWidth;
            if (i < middleKeyIndex) {
                // Outer keys on the left
                rectWidth = middleKeyWidth * (OUTER_KEY_WIDTH_FACTOR - ((OUTER_KEY_WIDTH_FACTOR - 1) * i / middleKeyIndex));
            } else {
                // Outer keys on the right
                rectWidth = middleKeyWidth * (OUTER_KEY_WIDTH_FACTOR - ((OUTER_KEY_WIDTH_FACTOR - 1) * (numKeys - i - 1) / middleKeyIndex));
            }

            // Add extra spacing for specified keys
            if (i == 1 || i == 7 || i == 8 || i == 12 || i == 13 || i == 19 || i == 20) {
                currentLeft += 31;
                if(i == 12){
                    currentLeft += 5;
                }
            }

            // Make inner keys slightly smaller
            if (i > 2 && i < 20) {
                rectWidth *= INNER_KEY_WIDTH_FACTOR;
            }

            int left = currentLeft;
            int top = topOffset;
            int right = (int) (left + rectWidth);
            int bottom = top + rectHeight;
            canvas.drawRect(left, top, right, bottom, paint);

            currentLeft += rectWidth;
        }
    }

}
