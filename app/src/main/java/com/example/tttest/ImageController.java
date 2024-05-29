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

        // Define the number of keys and their widths
        int numKeys = 24;
        float[] keyWidths = calculateKeyWidths(width);

        // Iterate through each key region
        for (int i = 0; i < numKeys; i++) {
            int xOffset = Math.round((i == 0) ? 0 : keyWidths[i - 1]); // Offset for starting position
            int keyWidth = Math.round(keyWidths[i]); // Width of the key region

            // Extract the region corresponding to the key
            Bitmap cropped = Bitmap.createBitmap(bitmap, xOffset, 0, keyWidth, height);

            // Check if the key region contains the active color
            if (isColoredInRegion(cropped)) {
                keys[i] = 1;
            }
        }

        return keys;
    }

    private float[] calculateKeyWidths(int totalWidth) {
        // Initialize the key widths array
        float[] keyWidths = new float[24];

        // Calculate the width of the middle key
        float middleKeyWidth = totalWidth * 0.9f / 24; // 90% of the width for keys

        // Adjust the widths for outer keys
        for (int i = 0; i < 24; i++) {
            float factor = (i < 12) ? 0.98f - ((0.98f - 1f) * i / 11) : 0.98f - ((0.98f - 1f) * (23 - i) / 11);
            keyWidths[i] = middleKeyWidth * factor;
        }

        return keyWidths;
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
