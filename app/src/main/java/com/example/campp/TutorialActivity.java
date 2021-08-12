package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;


public class TutorialActivity extends AppCompatActivity {
    private final String[] MOVEMENT_NAMES =  new String[]{"Golpeteo de dedos", "Golpeteo de dedos",
            "Prono-supinación", "Prono-supinación", "Cierre de puño", "Cierre de puño"};
    private String pathToDir;
    private Integer counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        pathToDir = getIntent().getStringExtra("directory");
        counter = getIntent().getIntExtra("counter", 1);

        // Textview that shows which movement
        TextView movTxt = findViewById(R.id.textTitle);
        movTxt.setText(MOVEMENT_NAMES[counter]);

        // Disable repeat button if test has just started
        Button btn_repeat = findViewById(R.id.button_repeat);
        if (counter == 0) {
            btn_repeat.setEnabled(false);
        } else {
            btn_repeat.setEnabled(true);
        }

        // Set tutorial video on video view
        VideoView tutorialView = findViewById(R.id.tutorialView);

        // According to counter choose video
        String vidPath;
        if (counter == 0){
            vidPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorial_ft;
        } else if (counter == 2){
            vidPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorial_ps;
        } else {
            vidPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorial_oc;
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

    public void goBack(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", counter - 1);
        startActivity(intent);
    }

    @Override
    public void onBackPressed () {

    }
}