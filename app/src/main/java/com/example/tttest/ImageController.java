package com.example.tttest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        // 14 is aantal keys hier
        int[] keys = new int[14];


        Bitmap img = cropImage(image, image.getWidth() / 2 - 20, image.getHeight() / 2 - 20, 40, 40);

        if (isColoredInRegion(img)) {
            keys[0] = 1;
        } else keys[0] = 0;

        return keys;
    }

    @androidx.camera.core.ExperimentalGetImage
    public Bitmap cropImage(ImageProxy image, int xOffset, int yOffset, int cropWidth, int cropHeight) {
        Bitmap bitmap = toBitmap(image.getImage());

        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, cropWidth, cropHeight);
        }

        return bitmap;
    }

    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public boolean isColoredInRegion(Bitmap image) {
        if (image == null) {
            Log.e("ImageController", "Bitmap is null");
            return false;
        }

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                try {
                    int pixel = image.getPixel(x, y);
                    int red = Color.red(pixel);
                    int green = Color.green(pixel);
                    int blue = Color.blue(pixel);
                    if ((red > Color.red(activeColor) - colorRange && red < Color.red(activeColor) + colorRange) &&
                            (green > Color.green(activeColor) - colorRange && green < Color.green(activeColor) + colorRange) &&
                            (blue > Color.blue(activeColor) - colorRange && blue < Color.blue(activeColor) + colorRange)) {
                        return true;
                    }
                } catch (Exception e) {
                    Log.e("ImageController", "Error accessing pixel at x=" + x + ", y=" + y + ": " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }
}
