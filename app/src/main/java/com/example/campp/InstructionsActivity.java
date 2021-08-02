package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
    }

    public void goToTutorial(View view){
        EditText idInput = findViewById(R.id.patientNumber);
        String patientId = idInput.getText().toString();
        if (patientId.length() < 4) {
            Toast.makeText(this, "Ingrese un id válido.", Toast.LENGTH_SHORT).show();
            return;
        }
        Switch statusSwitch = findViewById(R.id.switchONOFF);
        String status;
        if (statusSwitch.isChecked()) {
            status = "ON";
        } else {
            status = "OF";
        }
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        String dirName = patientId + "_" + timeStamp + "_" + status;
        // Directory where image and video are going to be saved
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(dirName, Context.MODE_PRIVATE);
        String pathToDir = directory.getAbsolutePath();

        Intent intent = new Intent(this, TutorialActivity.class);
        intent.putExtra("directory", pathToDir);
        intent.putExtra("counter", 0);
        startActivity(intent);
    }
}