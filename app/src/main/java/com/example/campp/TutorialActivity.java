package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;


public class TutorialActivity extends AppCompatActivity {
    private String pathToDir;
    private Integer counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        pathToDir = getIntent().getStringExtra("directory");
        counter = getIntent().getIntExtra("counter", 1);

        // Set tutorial video on video view
        VideoView tutorialView = findViewById(R.id.tutorialView);

        // According to counter choose video
        String vidPath = "";
        if (counter == 0){
            vidPath = "android.resource://" + getPackageName() + "/" + R.raw.test;
        } else if (counter == 2){
            vidPath = "android.resource://" + getPackageName() + "/" + R.raw.test1;
        } else {
            vidPath = "android.resource://" + getPackageName() + "/" + R.raw.test2;
        }
        Uri uri = Uri.parse(vidPath);
        tutorialView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        tutorialView.setMediaController(mediaController);
        mediaController.setAnchorView(tutorialView);
        tutorialView.start();
    }

    public void goToCamera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", counter);
        startActivity(intent);
    }
}