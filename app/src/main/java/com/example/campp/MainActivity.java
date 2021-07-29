package com.example.campp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    int selectedPosition = 0;
    String[] paths = {"empty", "empty", "empty"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences mPrefs = getSharedPreferences("lastTests", Context.MODE_PRIVATE);
        paths[0] = mPrefs.getString("p1", "empty");
        paths[1] = mPrefs.getString("p2", "empty");
        paths[2] = mPrefs.getString("p3", "empty");
        Log.d("pathtopic", "PATHS: " + paths[0] + " " + paths[1] + " " + paths[2]);

        String[] testsNames = new String[3];
        if (paths[0].equals("empty")){
            testsNames[0] = testsNames[1] = testsNames[2] = "empty";
        } else if (paths[1].equals("empty")) {
            testsNames[0] = paths[0].substring(paths[0].length() - 17);
            testsNames[1] = testsNames[2] = "empty";
        } else if (paths[2].equals("empty")) {
            testsNames[0] = paths[0].substring(paths[0].length() - 17);
            testsNames[1] = paths[1].substring(paths[1].length() - 17);
            testsNames[2] = "empty";
        } else {
            testsNames[0] = paths[0].substring(paths[0].length() - 17);
            testsNames[1] = paths[1].substring(paths[1].length() - 17);
            testsNames[2] = paths[2].substring(paths[2].length() - 17);
        }

        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, testsNames);
        ListView listView = (ListView) findViewById(R.id.testsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) findViewById(R.id.testsList);
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                listView.setItemChecked(position, true);
                selectedPosition = position;
            }
        });
    }

    public void sendToCloud(View view){
        // Send folder contents to cloud
        String pathToDir = paths[selectedPosition];
    }

    public void goToInstructions(View view){
//        Intent intent = new Intent(this, CameraActivity.class);
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }
}