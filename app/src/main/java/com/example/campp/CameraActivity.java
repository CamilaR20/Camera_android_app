package com.example.campp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    // Variables, Objects
    private final String[] FILE_NAMES =  new String[]{"fingertap_l", "fingertap_r", "pronosup_l", "pronosup_r", "fist_l" , "fist_r"};
    private final String[] MOVEMENT_NAMES =  new String[]{"Golpeteo de dedos\nMano izquierda",
            "Golpeteo de dedos\nMano derecha", "Prono-supinación\nMano izquierda", "Prono-supinación\nMano derecha",
            "Cierre de puño\nMano izquierda" , "Cierre de puño\nMano derecha"};
    // To check permisions
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"};
    // For Camera X
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor cameraExecutor = Executors.newSingleThreadExecutor();
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    // Files directory, image and video paths
    private File directory;
    private String pathToDir = "";
    private Integer counter;
    // Video timer
    private CountDownTimer videoTimer = new CountDownTimer(15000, 1000) {
        public void onTick(long millis) {
            TextView timerTxt = findViewById(R.id.textTimer); // TextView that shows timer count down
            timerTxt.setText("Grabando: " + millis / 1000 + " s");
        }
        public void onFinish() {
            // Stops recording
            videoCapture.stopRecording();
        }
    };
    // Calibration timer: to take calibration picture before each movement
    private CountDownTimer calibrationTimer = new CountDownTimer(5000, 1000) {
        public void onTick(long millis) {
            TextView timerTxt = findViewById(R.id.textTimer); // TextView that shows timer count down
            timerTxt.setText("¿Preparado? " + millis / 1000 + " s");
        }
        public void onFinish() {
            // Captura foto de calibración e inicia timer de video
            takePicture();
            TextView statusTxt = findViewById(R.id.textStatus); // TextView that shows status
            statusTxt.setText("Calibrado!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        pathToDir = getIntent().getStringExtra("directory");
        counter = getIntent().getIntExtra("counter", 1);

        // Textview that shows which movement
        TextView movTxt = findViewById(R.id.textMovement);
        movTxt.setText(MOVEMENT_NAMES[counter]);

        directory = new File(pathToDir);

        Button finishBtn = findViewById(R.id.finish_btn); // Disable finish button until calibration picture is
        finishBtn.setEnabled(false);

        // Check permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }

    }

    // Check if permissions are granted
    public boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // After requesting permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    // Try to access camera and configure it
    void startCamera() {
        calibrationTimer.start();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraConfig(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Camera configuration: preview, image capture and video capture
    void cameraConfig(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        PreviewView previewView = findViewById(R.id.previewView);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(display.getRotation())
                        .build();
        videoCapture = new VideoCapture.Builder().build();
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, videoCapture, preview);
    }

    // To take calibration picture
    void takePicture() {
        File picturePath = new File(directory, FILE_NAMES[counter] + ".jpg");
        // Print path to picture
        Log.d("pathtopic", picturePath.getAbsolutePath());

        // Capture and save image
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(picturePath).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        // Image path to send to other activity
                        Log.d("pathtopic", "Image was saved");
                        videoTimer.start();
                        takeVideo();
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.d("pathtopic", "Image was not saved: ON ERROR TAKE PICTURE");
                        errorAlert("No fue posible guardar foto de calibración.");
                    }
                }
        );
    }

    // To take video of movement
    @SuppressLint("RestrictedApi")
    private void takeVideo() {
        File videoPath = new File(directory, FILE_NAMES[counter] + ".mp4");
        // Print path to picture
        Log.d("pathtopic", videoPath.getAbsolutePath());

        // Enable button to stop the recording
        runOnUiThread(() -> {
            Button finishBtn = findViewById(R.id.finish_btn);
            finishBtn.setEnabled(true);
        });

        VideoCapture.OutputFileOptions outputFileOptions =
                new VideoCapture.OutputFileOptions.Builder(videoPath).build();

        videoCapture.startRecording(outputFileOptions, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(VideoCapture.OutputFileResults outputFileResults) {
                        // Image path to send to other activity
                        Log.d("pathtopic", "Video was saved");
                        goToOther();
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull @NotNull String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {
                        errorAlert("Error on video saving.");
                    }
                }
        );

}

    @SuppressLint("RestrictedApi")
    public void stopRec(View view) {
        videoCapture.stopRecording();
    }

    // In case there is an error on saving image or video
    void errorAlert(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("Error").setMessage(msg).setCancelable(false)
                .setPositiveButton("Volver al inicio", (dialog, which) -> finish());
        AlertDialog alert = builder.create();
        alert.show();
    }

    void goToOther(){
        counter ++;
        counter = 6;
        if (counter <= 5) {
            Intent intent;
            if (counter == 2 || counter == 4){
                intent = new Intent(this, TutorialActivity.class);

            } else {
                intent = new Intent(this, BreakActivity.class);
            }
            intent.putExtra("directory", pathToDir);
            intent.putExtra("counter", counter);
            startActivity(intent);
        } else {
            // Save path of last test and delete path to older one
            SharedPreferences mPrefs = getSharedPreferences("lastTests", Context.MODE_PRIVATE);
            String p1 = mPrefs.getString("p1", "empty");
            String p2 = mPrefs.getString("p2", "empty");
            String p3 = mPrefs.getString("p3", "empty");
            String s1 = mPrefs.getString("s1", "F");
            String s2 = mPrefs.getString("s2", "F");
            String s3;

            // Delete oldest test
            if (!p3.equals("empty")){
                File folderToDelete = new File(p3);
                DeleteRecursive(folderToDelete);
                Log.d("pathtopic", "Last folder deleted.");
            }

            p3 = p2;
            p2 = p1;
            p1 = pathToDir;

            s3 = s2;
            s2 = s1;
            s1 = "F";

            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.putString("p1", p1).commit();
            mEditor.putString("p2", p2).commit();
            mEditor.putString("p3", p3).commit();
            mEditor.putString("s1", s1).commit();
            mEditor.putString("s2", s2).commit();
            mEditor.putString("s3", s3).commit();

            runOnUiThread(this::finishAlert);
        }

    }

    // When movement videos are done
    void finishAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
        builder.setTitle("Prueba Finalizada").setMessage("Prueba realizada satisfactoriamente.").setCancelable(false)
                .setPositiveButton("Volver al inicio", (dialog, which) -> {
                    Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                    startActivity(intent);
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                child.delete();
                DeleteRecursive(child);
            }
        fileOrDirectory.delete();
    }

    @Override
    public void onBackPressed () {

    }

}