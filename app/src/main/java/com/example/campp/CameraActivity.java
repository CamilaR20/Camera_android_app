package com.example.campp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import androidx.fragment.app.DialogFragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
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
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor cameraExecutor = Executors.newSingleThreadExecutor();
    ImageCapture imageCapture;
    VideoCapture videoCapture;

    // Files directory, image and video paths
    File directory;
    String pathToPicture = "";
    String pathToVideo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        TextView timerTxt = findViewById(R.id.textTimer); // TextView that shows timer count down
        TextView statusTxt = findViewById(R.id.textStatus); // TextView that shows status

        // Check permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }

        // Directory where image and video are going to be saved
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        // Timer de calibración
        CountDownTimer calibrationTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millis) {
                timerTxt.setText("¿Preparado? " + millis / 1000 + "s");
            }

            public void onFinish() {
                // Captura foto de calibración e inicia timer de video
                takePicture();
                statusTxt.setText("Calibrado!");
            }
        };
        // Timer de grabación
        CountDownTimer videoTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millis) {
                timerTxt.setText("¿Preparado? " + millis / 1000 + "s");
            }

            public void onFinish() {
                // Captura video

            }
        };

        calibrationTimer.start();
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

    // Camera configuration
    void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Add preview to show what the camera sees
    void bindPreview(ProcessCameraProvider cameraProvider) {
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

    void takePicture() {
        File picturePath = new File(directory, "mytest.jpg");
        // Print path to picture
        pathToPicture = picturePath.getAbsolutePath();
        Log.d("pathtopic", pathToPicture);

        // Capture and save image
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(picturePath).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        // Image path to send to other activity
                        Log.d("pathtopic", "Image was saved");
                        takeVideo();
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        Log.d("pathtopic", "Image was not saved: ON ERROR TAKE PICTURE");
//                        DialogFragment newFragment = new SavingErrorDialogFragment();
//                        newFragment.show(getSupportFragmentManager(), "savingError");
                    }
                }
        );
    }

    @SuppressLint("RestrictedApi")
    private void takeVideo() {
        File videoPath = new File(directory, "vidtest.mp4");
        // Print path to picture
        pathToVideo = videoPath.getAbsolutePath();
        Log.d("pathtopic", pathToVideo);

        VideoCapture.OutputFileOptions outputFileOptions =
                new VideoCapture.OutputFileOptions.Builder(videoPath).build();

        videoCapture.startRecording(outputFileOptions, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(VideoCapture.OutputFileResults outputFileResults) {
                        // Image path to send to other activity
                        Log.d("pathtopic", "Video was saved");
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull @NotNull String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {

                    }
                }
        );

}

    @SuppressLint("RestrictedApi")
    public void goToTest(View view){
//        DialogFragment newFragment = new SavingErrorDialogFragment();
//        newFragment.show(getSupportFragmentManager(), "savingError");
        videoCapture.stopRecording();
//        Intent intent = new Intent(this, TestActivity.class);
//        intent.putExtra("imPath", pathToPicture);
//        startActivity(intent);
    }

    public void goToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

//    // Alert Dialog to inform of error saving the video or photo
//    public class SavingErrorDialogFragment extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            // Use the Builder class for convenient dialog construction
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setMessage("Error guardando imagen. Verificar permisos")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            //
//                        }
//                    });
//            // Create the AlertDialog object and return it
//            return builder.create();
//        }
//    }


}