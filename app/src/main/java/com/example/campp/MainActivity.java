package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private int selectedPosition = 0;
    private String[] paths = {"empty", "empty", "empty"};
    private String[] sent_status = {"F", "F", "F"};

    private FirebaseStorage storage = FirebaseStorage.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get path to last tests and sent status
        SharedPreferences mPrefs = getSharedPreferences("lastTests", Context.MODE_PRIVATE);
        paths[0] = mPrefs.getString("p1", "empty");
        paths[1] = mPrefs.getString("p2", "empty");
        paths[2] = mPrefs.getString("p3", "empty");
        sent_status[0] = mPrefs.getString("s1", "F");
        sent_status[1] = mPrefs.getString("s2", "F");
        sent_status[2] = mPrefs.getString("s3", "F");

        // Get info from paths to show in readable format
        String[] testsNames = new String[3];
        if (paths[0].equals("empty")){
            testsNames[0] = testsNames[1] = testsNames[2] = "empty";
        } else if (paths[1].equals("empty")) {
            testsNames[0] = getFormattedTxt(paths[0].substring(paths[0].length() - 21));
            testsNames[1] = testsNames[2] = "empty";
        } else if (paths[2].equals("empty")) {
            testsNames[0] = getFormattedTxt(paths[0].substring(paths[0].length() - 21));
            testsNames[1] = getFormattedTxt(paths[1].substring(paths[1].length() - 21));
            testsNames[2] = "empty";
        } else {
            testsNames[0] = getFormattedTxt(paths[0].substring(paths[0].length() - 21));
            testsNames[1] = getFormattedTxt(paths[1].substring(paths[1].length() - 21));
            testsNames[2] = getFormattedTxt(paths[2].substring(paths[2].length() - 21));
        }

        // List of last tests
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, testsNames);
        ListView listView = findViewById(R.id.testsList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // List that shows sent status of last tests
        final String[] EMPTY = {"", "", ""};
        ArrayAdapter adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, EMPTY);
        ListView listView2 = findViewById(R.id.statusList);
        listView2.setAdapter(adapter2);
        listView2.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView2.setEnabled(false);
        modifyStatus();

        // When test is selected to be sent background color changes
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
        });
    }

    // Get info stored in the paths of tests
    private String getFormattedTxt(String txt) {
        String id = "ID: " + txt.substring(0, 4) + ", ";
        String date = "Fecha: " + txt.substring(5, 7) + "/" + txt.substring(7, 9) + "/" + txt.substring(9, 13) + ", ";
        String time = "Hora: " + txt.substring(14, 16) + ":" + txt.substring(16, 18) + ", ";
        String status;
        if (txt.endsWith("F")){
            status = "OFF";
        } else {
            status = "ON";
        }
        return id + date + time + status;
    }

    // Modify listview check, that indicates if test has been sent to the cloud
    private void modifyStatus() {
        ListView listView = findViewById(R.id.statusList);
        for (int i=0; i<3; i++){
            listView.setItemChecked(i, sent_status[i].equals("T"));
        }

    }

    public void sendToCloud(View view){
        // Send folder contents to cloud
        String pathToDir = paths[selectedPosition];

        // Firebase path
        String test_info = paths[selectedPosition].substring(paths[selectedPosition].length() - 21);
        String firebasedir = test_info.substring(0, 4) + "/" + test_info.substring(5, 18) + "/";
        String firebasepath = firebasedir + "pic.png";

        // Send to Firebase
        StorageReference fileRef = storage.getReference(firebasepath);
        File f = new File(pathToDir + "/ft_l.jpg");
        Uri file = Uri.fromFile(f);
        UploadTask uploadTask = fileRef.putFile(file);

        // Check item that was sent(if successful)
        sent_status[selectedPosition] = "T";
        modifyStatus();
    }

    public void goToInstructions(View view){
        // Change sent status of items
        SharedPreferences mPrefs = getSharedPreferences("lastTests", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("s1", sent_status[0]).commit();
        mEditor.putString("s2", sent_status[1]).commit();
        mEditor.putString("s3", sent_status[2]).commit();
//        Intent intent = new Intent(this, CameraActivity.class);
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }
}