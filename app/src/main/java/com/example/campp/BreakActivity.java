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

    public void goToCamera(View view){
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", counter);
        startActivity(intent);
    }

    public void goBack(View view){
        // To repeat
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", counter - 1);
        startActivity(intent);
    }

    @Override
    public void onBackPressed () {

    }
}