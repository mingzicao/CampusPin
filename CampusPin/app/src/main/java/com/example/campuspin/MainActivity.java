package com.example.campuspin;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.campuspin.R.id.editText;
import static com.example.campuspin.R.id.textView2;

public class MainActivity extends AppCompatActivity{
    private static final int RESULT_LOAD_IMAGE = 1;
    public static final String EXTRA_MESSAGE = "com.example.campuspin.MESSAGE";
    private FirebaseAnalytics mFirebaseAnalytics;
    ImageView imageToUpload;
    private DatabaseReference mDatabase;

    @IgnoreExtraProperties
    public class Building {
        public String name;
        public String address;
        public Building() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }
        public Building(String name, String address) {
            this.name = name;
            this.address = address;

        }
    }

    private void writeNewBuilding(String buildingName, String name, String address) {
        Building Building = new Building(name,address);
        mDatabase.child("Building").child(buildingName).setValue(Building);
    }

    DatabaseReference myRefBuilding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        //imageToUpload.setOnClickListener(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRefBuilding = database.getReference("Building");
        myRef = database.getReference();
        myRef.child("user").child("searchHistory").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s){
                final TextView textView= (TextView) findViewById(R.id.textView2);
                final String searchString = dataSnapshot.getValue().toString();
                myRefBuilding.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(searchString)){
                            textView.setText(dataSnapshot.child(searchString).child("address").getValue().toString());
                        }
                        else{
                            textView.setText("no");
                        }
                    }
                    @Override                    public void onCancelled(DatabaseError databaseError) {}
                });
            }
            @Override            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            @Override            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    public void sendToSignIn(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        Intent intent = new Intent(this, EmailPasswordActivity.class);
        startActivity(intent);
    }

    public void chooseImage(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
    }

    private DatabaseReference myRef;
    private StorageReference myStorage;

    public void search(View view) {
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        myRef.child("user").child("searchHistory").push().setValue(message);
        //myRef.child("hello").push().setValue(message);
        /*TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText(editText.getText().toString());
        String ha= myRef.child("Building").child("PHO").getKey();
        myRef.child("yes").push().setValue(ha);
        editText.setText("");*/
    }

    public void sendMessage2(View view) { // send pic to somewhre
        FirebaseStorage storage = FirebaseStorage.getInstance();
        myStorage = storage.getReference(selectedImage.getLastPathSegment());
        myStorage.putFile(selectedImage).addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Uri downloadUrl = task.getResult().getDownloadUrl();
                String url = downloadUrl.toString();
                myRef.child("url").push().setValue(url);
            }
        });
    }

    private Uri selectedImage;
    @Override // when click image, here will be called
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode ==RESULT_OK && data != null) {
            selectedImage = data.getData();   // uniform resours address of the image in the phone
            imageToUpload.setImageURI(selectedImage);
            //myRef.child("hi").push().setValue("yeah");
        }
    }
}