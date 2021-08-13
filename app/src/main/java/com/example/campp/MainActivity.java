package com.example.campp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {

    private int selectedPosition = 0;
    private String[] paths = {"empty", "empty", "empty"};
    private String[] sent_status = {"F", "F", "F"};

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    private MenuItem signoutBtn;
    private TextView textUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        textUser = findViewById(R.id.textViewUser);
        // Initialize Firebase storage
        storage = FirebaseStorage.getInstance();

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
        listView.setOnItemClickListener((parent, view, position, id) -> selectedPosition = position);
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

    private void sendToCloud(){
        Button sendBtn = findViewById(R.id.button_send);
        sendBtn.setEnabled(false);
        // Send folder contents to cloud
        String pathToDir = paths[selectedPosition];

        // Firebase path
        String test_info = paths[selectedPosition].substring(paths[selectedPosition].length() - 21);
        String firebasedir = test_info.substring(0, 4) + "/" + test_info.substring(5, 18) + "/";
        String firebasepath = firebasedir + "test.zip";

        // Send to Firebase
        // Check if zip file exists
        File zipFile = new File(pathToDir + "/test.zip");
        String zipStatus = "Success";
        if (!zipFile.exists()){
            zipStatus = zip(pathToDir);
        }
        // if zip exists or folder was zipped succesfully, then send to cloud
        if (zipStatus.equals("Success")) {
            StorageReference fileRef = storage.getReference(firebasepath);
            File f = new File(pathToDir + "/test.zip");
            Uri file = Uri.fromFile(f);
            UploadTask uploadTask = fileRef.putFile(file);
            uploadTask.addOnFailureListener(exception -> {
                // Handle unsuccessful uploads
                Toast.makeText(MainActivity.this, "Error al enviar la prueba. Intente más tarde.", Toast.LENGTH_SHORT).show();
                sendBtn.setEnabled(true);
            }).addOnSuccessListener(taskSnapshot -> {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Toast.makeText(MainActivity.this, "Prueba enviada exitosamente.", Toast.LENGTH_SHORT).show();
                sent_status[selectedPosition] = "T";
                modifyStatus();
                sendBtn.setEnabled(true);
            });
        } else {
            Toast.makeText(MainActivity.this, "Error al comprimir los archivos de la prueba.", Toast.LENGTH_SHORT).show();
            sendBtn.setEnabled(true);
        }
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

    private String zip(String pathToDir) {
        final int BUFFER = 2048;
        File dir = new File(pathToDir);
        File[] listFiles = dir.listFiles();
        String zipFileName = pathToDir + "/test.zip";
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte[] data = new byte[BUFFER];

            for (File child : listFiles) {
                Log.d("Compress", "Adding: " + child.toString());
                FileInputStream fi = new FileInputStream(child);
                origin = new BufferedInputStream(fi, BUFFER);

                String childPath = child.getAbsolutePath();
                ZipEntry entry = new ZipEntry(childPath.substring(childPath.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
            Log.d("Compress", "Compressing success");
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Compress", "Compressing error");
            return "Error";
        }
    }

    // When send to cloud button is pressed it first checks if user is signed in
    public void checkUserSignedIn(View view){
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            signInDialog();
        } else {
            String email = currentUser.getEmail();
            textUser.setText("Sesión iniciada como: " + email);
            sendToCloud();
        }
    }

    // Create an action bar button to sign out
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.altmenu, menu);
        signoutBtn = menu.findItem(R.id.signoutBtn);
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            textUser.setText("No hay ninguna sesión activa.");
        } else {
            String email = currentUser.getEmail();
            textUser.setText("Sesión iniciada como: " + email);
        }
        signoutBtn.setVisible(currentUser != null);
        return super.onCreateOptionsMenu(menu);
    }

    // Handle SignOut button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.signoutBtn) {
            mAuth.signOut();
            signoutBtn.setVisible(false);
            textUser.setText("No hay ninguna sesión activa.");
        }
        return super.onOptionsItemSelected(item);
    }

    // Custom alert dialog for sign in
    private void signInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View prompt = inflater.inflate(R.layout.dialog_signin, null);
        final EditText emailtxt = prompt.findViewById(R.id.username);
        final EditText pswdtxt = prompt.findViewById(R.id.password);
        builder.setView(prompt).setCancelable(false)
                // Add action buttons
                .setPositiveButton("Iniciar sesión", (dialog, id) -> {
                    // sign in the user
                    final String email = emailtxt.getText().toString();
                    final String pswd = pswdtxt.getText().toString();
                    signInUser(email, pswd);
                })
                .setNegativeButton("Cancelar", (dialog, id) -> Toast.makeText(MainActivity.this, "Debe iniciar sesión para enviar pruebas a la nube.", Toast.LENGTH_SHORT).show());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void signInUser(String email, String pswd){
        mAuth.signInWithEmailAndPassword(email, pswd)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("SignIn", "signInWithEmail:success");
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        String email1 = currentUser.getEmail();
                        Toast.makeText(MainActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                        signoutBtn.setVisible(true);
                        textUser.setText("Sesión iniciada como: " + email1);
                        sendToCloud();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("SignIn", "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Error de autenticación.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}