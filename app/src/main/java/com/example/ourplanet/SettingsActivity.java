package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;

    private EditText userName, userProfName, userStatus, userCountry, userGender, userAge, userHobby, userSmoking, userPlastic, userRecycle;
    private Button UpdateAccountSettingsButton;
    private CircleImageView userProfImage;

    private DatabaseReference SettingsUserRef, postRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;

    private String currentUserID;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settings_username);
        userProfName = (EditText) findViewById(R.id.settings_profile_fullname);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userAge = (EditText) findViewById(R.id.settings_age);
        userHobby = (EditText) findViewById(R.id.settings_hobby);
        userSmoking = (EditText) findViewById(R.id.settings_smoking);
        userPlastic = (EditText) findViewById(R.id.settings_plastic);
        userRecycle = (EditText) findViewById(R.id.settings_recycle);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        UpdateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_buttons);
        loadingBar = new ProgressDialog(this);

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {

                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myAge = dataSnapshot.child("age").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myHobby = dataSnapshot.child("hobby").getValue().toString();
                    String mySmoking = dataSnapshot.child("smoking").getValue().toString();
                    String myPlastic = dataSnapshot.child("plastic").getValue().toString();
                    String myRecycle = dataSnapshot.child("recycle").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userAge.setText(myAge);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userHobby.setText(myHobby);
                    userSmoking.setText(mySmoking);
                    userPlastic.setText(myPlastic);
                    userRecycle.setText(myRecycle);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        UpdateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // some conditions for the picture
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                // Get the result of the image and store it in resultUri
                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                SettingsUserRef.child("profileImage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SettingsActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }

                        });

                    }

                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }



    private void ValidateAccountInfo()
    {
        String username = userName.getText().toString();
        String profileName = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String age = userAge.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String hobby = userHobby.getText().toString();
        String smoking = userSmoking.getText().toString();
        String plastic = userPlastic.getText().toString();
        String recycle = userRecycle.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profileName))
        {
            Toast.makeText(this, "Please write your profile name.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Please write your status.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(age))
        {
            Toast.makeText(this, "Please write your date of birth.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please write your gender.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(hobby))
        {
            Toast.makeText(this, "Please write your hobby.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(smoking))
        {
            Toast.makeText(this, "Please write if you are a smoker or not.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(plastic))
        {
            Toast.makeText(this, "Please write if you are using plastic cups.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(recycle))
        {
            Toast.makeText(this, "Please write if you recycle or not.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username, profileName, status, age, country, gender, hobby, smoking, plastic, recycle);
        }
    }

    private void UpdateAccountInfo(String username, String profileName, String status, String age, String country, String gender, String hobby, String smoking, String plastic, String recycle)
    {
        HashMap userMap = new HashMap();
                userMap.put("status", status);
                userMap.put("username", username);
                userMap.put("fullname", profileName);
                userMap.put("age", age);
                userMap.put("country", country);
                userMap.put("gender", gender);
                userMap.put("hobby", hobby);
                userMap.put("smoking", smoking);
                userMap.put("plastic", plastic);
                userMap.put("recycle", recycle);
        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account settings updated successfully.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}