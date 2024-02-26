package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {


    private EditText UserName, FullName, CountryName, Age, Hobby;
    private Button SaveInformationbutton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;
    private RadioGroup radioSmokingGroup, radioGender, radioPlasticGroup, radioRecycleGroup;
    private RadioButton radioSmokingButton, radioGenderButton, radioPlasticButton, radioRecycleButton;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_country);
        Age = (EditText) findViewById(R.id.setup_age);
        Hobby = (EditText) findViewById(R.id.setup_hobby);
        radioSmokingGroup = (RadioGroup) findViewById(R.id.radioSmoking);
        radioGender = (RadioGroup) findViewById(R.id.radioGender);
        radioPlasticGroup = (RadioGroup) findViewById(R.id.radioPlastic);
        radioRecycleGroup = (RadioGroup) findViewById(R.id.radioRecycle);
        SaveInformationbutton = (Button) findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


        //load profile image to header
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileImage"))
                    {
                        String image = dataSnapshot.child("profileImage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                UsersRef.child("profileImage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfIntent);
                                            Toast.makeText(SetupActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
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



    private void SaveAccountSetupInformation()
    {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();
        String age = Age.getText().toString();
        String hobby = Hobby.getText().toString();

        int selectedSmoking = radioSmokingGroup.getCheckedRadioButtonId();
        radioSmokingButton = (RadioButton) findViewById(selectedSmoking);
        String smoking = radioSmokingButton.getText().toString();

        int selectedGender = radioGender.getCheckedRadioButtonId();
        radioGenderButton = (RadioButton) findViewById(selectedGender);
        String gender = radioGenderButton.getText().toString();

        int selectedPlastic = radioPlasticGroup.getCheckedRadioButtonId();
        radioPlasticButton = (RadioButton) findViewById(selectedPlastic);
        String plastic = radioPlasticButton.getText().toString();

        int selectedRecycle = radioRecycleGroup.getCheckedRadioButtonId();
        radioRecycleButton = (RadioButton) findViewById(selectedRecycle);
        String recycle = radioRecycleButton.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please write your fullname", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(age))
        {
            Toast.makeText(this, "Please write your age", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(hobby))
        {
            Toast.makeText(this, "Please write your hobby", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(smoking))
        {
            Toast.makeText(this, "Please write if you are a smoker or not", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(plastic))
        {
            Toast.makeText(this, "Please select if you use plastic cups", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(recycle))
        {
            Toast.makeText(this, "Please select if you recycle", Toast.LENGTH_SHORT).show();
        }
        else
        {

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "none");
            userMap.put("gender", gender);
            userMap.put("age", age);
            userMap.put("hobby", hobby);
            userMap.put("smoking", smoking);
            userMap.put("plastic", plastic);
            userMap.put("recycle", recycle);
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account has been created successfully", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}