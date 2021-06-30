package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToInstructions(View view){
        Intent intent = new Intent(this, CameraActivity.class);
//        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }
}