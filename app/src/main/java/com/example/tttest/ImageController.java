package com.example.tttest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageController {
    int activeColor;
    int colorRange;

    ImageController() {
        this.activeColor = Color.GREEN;
        this.colorRange = 20;
    }

    ImageController(int activeColor) {
        this.activeColor = activeColor;
        this.colorRange = 20;
    }

    ImageController(int activeColor, int colorRange) {
        this.activeColor = activeColor;
        this.colorRange = colorRange;
    }

    public int[] getActiveKeys(ImageProxy image) {
        int[] keys = new int[24];
        Bitmap bitmap = toBitmap(image.getImage());

        if (bitmap == null) return keys;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int numKeys = 24;
        int middleKeyIndex = numKeys / 2;
        float totalWidthWithoutMargins = (float) (width * 0.9); // 90% of the width for keys
        float middleKeyWidth = totalWidthWithoutMargins / (numKeys + (OverlayView.OUTER_KEY_WIDTH_FACTOR - 1) * 2);
        float totalWidth = middleKeyWidth * numKeys;

        // Adjust the currentLeft to match the position of the keys
        int currentLeft = (width - (int) totalWidth) / 2; // Center the rectangles horizontally
        for (int i = 0; i < numKeys; i++) {
            float rectWidth;
            if (i < middleKeyIndex) {
                // Outer keys on the left
                rectWidth = middleKeyWidth * (OverlayView.OUTER_KEY_WIDTH_FACTOR - ((OverlayView.OUTER_KEY_WIDTH_FACTOR - 1) * i / middleKeyIndex));
            } else {
                // Outer keys on the right
                rectWidth = middleKeyWidth * (OverlayView.OUTER_KEY_WIDTH_FACTOR - ((OverlayView.OUTER_KEY_WIDTH_FACTOR - 1) * (numKeys - i - 1) / middleKeyIndex));
            }

            // Add extra spacing for specified keys
            if (i == 1 || i == 7 || i == 8 || i == 12 || i == 13 || i == 19 || i == 20) {
                currentLeft += 31;
                if (i == 12) {
                    currentLeft += 5;
                }
            }

            // Make inner keys slightly smaller
            if (i > 2 && i < 20) {
                rectWidth *= OverlayView.INNER_KEY_WIDTH_FACTOR;
            }

            int xOffset = currentLeft;
            int yOffset = height * 72 / 100; // Place the rectangles at 3/4 of the height

            // die crashed hier anders op sommige apparaten, komt nie altijd uit. Joren
            if (xOffset + (int)rectWidth > 180){
                xOffset = 180 - (int)rectWidth;
            }

            Bitmap cropped = Bitmap.createBitmap(bitmap, xOffset, yOffset, (int) rectWidth, height - yOffset);

            if (isColoredInRegion(cropped)) {
                keys[i] = 1;
            }

            currentLeft += rectWidth;
        }

        return keys;
    }

    private Bitmap toBitmap(Image image) {
        if (image == null) return null;

        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private boolean isColoredInRegion(Bitmap image) {
        if (image == null) {
            Log.e("ImageController", "Bitmap is null");
            return false;
        }

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                if ((red > Color.red(activeColor) - colorRange && red < Color.red(activeColor) + colorRange) &&
                        (green > Color.green(activeColor) - colorRange && green < Color.green(activeColor) + colorRange) &&
                        (blue > Color.blue(activeColor) - colorRange && blue < Color.blue(activeColor) + colorRange)) {
                    return true;
                }
            }
        }
        return false;
    }
}
