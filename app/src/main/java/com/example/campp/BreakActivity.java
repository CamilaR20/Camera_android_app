package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BreakActivity extends AppCompatActivity {

    private String pathToDir;
    private Integer counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);

        pathToDir = getIntent().getStringExtra("directory");
        counter = getIntent().getIntExtra("counter", 1);

    }

    // To record next video
    public void goToCamera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", counter);
        startActivity(intent);
    }

    // To repeat last video
    public void goBack(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", counter - 1);
        startActivity(intent);
    }

    // Don´t allow to go back from this screen
    @Override
    public void onBackPressed () {

    }
}