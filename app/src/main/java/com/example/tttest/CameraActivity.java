package com.example.tttest;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageController imageController;
    private SoundPlayer soundPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.previewView);

        imageController = new ImageController(Color.rgb(118, 199, 228), 20);
        soundPlayer = new SoundPlayer(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindImageAnalysis(cameraProvider);
                turnOnLight(true);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                turnOnLight(false);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            int skip = 10;
            int counter = 0;

            @Override
            public void analyze(@NonNull ImageProxy image) {
                if (counter >= skip) {
                    int[] keys = imageController.getActiveKeys(image);
                    Log.d("TTCAM", String.format("%d", keys[0]));
                    for (int i = 0; i < keys.length; i++) {
                        if (keys[i] == 1) {
                            Log.d("TTCAM", String.format("Green dot detected in rectangle: %d", i));
                        }
                    }
                    soundPlayer.playSound(keys);
                    counter = 0;
                } else {
                    counter++;
                }
                image.close();
            }
        });

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }

    private void turnOnLight(boolean enabled){
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            String camId = null;
            try {
                camId = cameraManager.getCameraIdList()[0];
                cameraManager.setTorchMode(camId, enabled);
            } catch (CameraAccessException| IllegalArgumentException e){
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(this, "No flash available", Toast.LENGTH_SHORT).show();
        }
    }
}
