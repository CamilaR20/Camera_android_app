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
//import android.util.Log;
import android.util.Size;
import android.view.View;
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
    // Video timer: maximum time to record each movement
    private CountDownTimer videoTimer = new CountDownTimer(16000, 1000) {
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
    private CountDownTimer calibrationTimer = new CountDownTimer(6000, 1000) {
        public void onTick(long millis) {
            TextView timerTxt = findViewById(R.id.textTimer); // TextView that shows timer count down
            timerTxt.setText("¿Preparado? " + millis / 1000 + " s");
        }
        public void onFinish() {
            // Takes calibration picture and starts video timer
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

        Button finishBtn = findViewById(R.id.finish_btn); // Disable finish button until calibration picture is taken
        finishBtn.setEnabled(false);

        Button readyBtn = findViewById(R.id.ready_btn); // Set ready button to be visible at the start
        readyBtn.setVisibility(View.VISIBLE);

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

        cameraProvider.unbindAll();

        imageCapture = new ImageCapture.Builder().setTargetResolution(new Size(540, 960)).build();
        videoCapture = new VideoCapture.Builder().setTargetResolution(new Size(540, 960)).build();
        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, videoCapture, preview);
    }

    // Starts calibration timer after button indicating person is ready is pressed
    public void startTest(View view){
        calibrationTimer.start();
        Button readyBtn = findViewById(R.id.ready_btn);
        readyBtn.setVisibility(View.GONE);
    }

    // To take calibration picture
    void takePicture() {
        File picturePath = new File(directory, FILE_NAMES[counter] + ".jpg");
        //Log.d("pathtopic", picturePath.getAbsolutePath());

        // Capture and save image
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(picturePath).build();

        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        //Log.d("pathtopic", "Image was saved");
                        videoTimer.start();
                        takeVideo();
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        //Log.d("pathtopic", "Image was not saved: ON ERROR TAKE PICTURE");
                        errorAlert("No fue posible guardar foto de calibración.");
                    }
                }
        );
    }

    // To record video of movement
    @SuppressLint("RestrictedApi")
    private void takeVideo() {
        File videoPath = new File(directory, FILE_NAMES[counter] + ".mp4");
//        Log.d("pathtopic", videoPath.getAbsolutePath());

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
//                        Log.d("pathtopic", "Video was saved");
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

    // Selects which activity to go to on each step of the test
    void goToOther(){
        counter ++;
        // Decide which activity to go: either to tutorial for next movement or to break for next hand
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
            int nRecords = 15;
            String[] pathName = {"p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10",
                    "p11", "p12", "p13", "p14", "p15"};
            String[] paths = {"vacío", "vacío", "vacío", "vacío", "vacío", "vacío", "vacío",
                    "vacío", "vacío", "vacío", "vacío", "vacío", "vacío", "vacío", "vacío"};
            String[] sentStatusName = {"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10",
                    "s11", "s12", "s13", "s14", "s15"};
            String[] sent_status = {"F", "F", "F", "F", "F", "F", "F", "F", "F", "F", "F", "F",
                    "F", "F", "F"};

            // Get paths and sent status of saved tests
            SharedPreferences mPrefs = getSharedPreferences("lastTests", Context.MODE_PRIVATE);

            // Delete oldest test
            String oldestPath = mPrefs.getString(pathName[nRecords-1], "vacío");
            File folderToDelete = new File(oldestPath);
            if (!oldestPath.equals("vacío")) {
                if (folderToDelete.exists()){
                    DeleteRecursive(folderToDelete);
//                    Log.d("pathtopic", "Last folder deleted.");
                }
            }

            // Add new test path to saved paths and update accordingly
            paths[0] = pathToDir;
            sent_status[0] = "F";
            for (int i = nRecords - 1; i > 0; i--){
                paths[i] = mPrefs.getString(pathName[i-1], "vacío");
                sent_status[i] = mPrefs.getString(sentStatusName[i-1], "F");
            }

            SharedPreferences.Editor mEditor = mPrefs.edit();
            for (int i = 0; i < nRecords; i++){
                mEditor.putString(pathName[i], paths[i]).commit();
                mEditor.putString(sentStatusName[i], sent_status[i]).commit();
            }
            runOnUiThread(this::finishAlert);
        }

    }

    // When movement videos are done show alert that says test was completed to return to the main activity
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

    // Delete folder and files within
    private void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                child.delete();
                DeleteRecursive(child);
            }
        fileOrDirectory.delete();
    }

    // Don´t allow to go back from this screen
    @Override
    public void onBackPressed () {

    }



}