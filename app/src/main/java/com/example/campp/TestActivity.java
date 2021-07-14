package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestActivity extends AppCompatActivity {
    String pathToVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        String pathToPicture = getIntent().getStringExtra("imPath");
        pathToVideo = getIntent().getStringExtra("vidPath");
        loadImageFromStorage(pathToPicture);

    }

    private void loadImageFromStorage(String path) {

        try {
            File f = new File(path);
            Bitmap img = BitmapFactory.decodeStream(new FileInputStream(f));
             // Find rotation from metadata an rotate
            final int rotation = getImageOrientation(path);
            Bitmap img_rot = checkRotationFromCamera(img, rotation);

            ImageView imView = findViewById(R.id.imView);
            imView.setImageBitmap(img_rot);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    private Bitmap checkRotationFromCamera(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    public void showVideo(View view){
        ImageView imView = findViewById(R.id.imView);
        imView.setVisibility(View.INVISIBLE);

        VideoView vidView = findViewById(R.id.videoView);
        vidView.setVisibility(View.VISIBLE);

        vidView.setVideoPath(pathToVideo);
        MediaController mediaController = new MediaController(this);
        vidView.setMediaController(mediaController);
        mediaController.setAnchorView(vidView);
        vidView.start();

    }
}