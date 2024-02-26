 package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import  androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Continuation;
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

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

 public class PostActivity extends AppCompatActivity implements TagsChoicesDialog.onMultiChoiceListener
{

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private String Description;
    private String finalTags, finalFeeling;

    private StorageReference PostsImagesReference;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    private  String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private long countPosts = 0;

    private static final  int Gallery_Pick = 1;
    private Uri ImageUri;

    private static final int IMAGE_REQUEST = 2;

    private String link ;

    private TextView tagsList;

    String[] feelingsList;
    Button feelingsBtn;
    TextView feelingsTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        //PostsImagesReference = FirebaseStorage.getInstance().getReference();
        PostsImagesReference = FirebaseStorage.getInstance().getReference().child("Post Images");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);

        tagsList = (TextView) findViewById(R.id.Tags);
        Button btnSelectTagsChoices = (Button) findViewById(R.id.btnSelectTags);

        feelingsBtn = (Button) findViewById(R.id.feelings_btn);
        feelingsTextView = (TextView) findViewById(R.id.selected_feelings);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });

        btnSelectTagsChoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                DialogFragment multiTagChoiceDialog = new TagsChoicesDialog();
                multiTagChoiceDialog.setCancelable(false);
                multiTagChoiceDialog.show(getSupportFragmentManager(), "Tags Selector");
            }
        });

        feelingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //create list of items
                feelingsList = new String[]{"Happy","Hopeful","Angry","Sad","Frustrated","Worried"};
                AlertDialog.Builder fbuilder = new AlertDialog.Builder(PostActivity.this);
                fbuilder.setTitle("Choose a feeling");
                fbuilder.setSingleChoiceItems(feelingsList, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        feelingsTextView.setText(feelingsList[which]);
                        finalFeeling = String.valueOf(feelingsList[which]);
                        dialog.dismiss();
                    }
                });
                fbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                AlertDialog fDialog = fbuilder.create();
                fDialog.show();
            }
        });

    }

    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();
        
        if(ImageUri == null)
        {
            Toast.makeText(this, "Please select post image.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please fill the description box", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        //generate name for post image by date
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        //StorageReference filePath = PostsImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        final StorageReference filePath = PostsImagesReference.child(ImageUri.getLastPathSegment()+postRandomName+".jpg");



        filePath.putFile(ImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();
                    Toast.makeText(PostActivity.this, "Post Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                    downloadUrl = downUri.toString();
                    SavingPostInformationToDatabase();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void SavingPostInformationToDatabase()
    {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    countPosts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {


                if(dataSnapshot.exists())
                {
                    String userFullname = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String userGroup;
                    if(dataSnapshot.child("userGroup").exists()){
                        userGroup = dataSnapshot.child("userGroup").getValue().toString();
                    }

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullname);
                    postsMap.put("counter", countPosts);
                    postsMap.put("tags", finalTags);
                    postsMap.put("feeling", finalFeeling);
                    if(dataSnapshot.child("userGroup").exists()) postsMap.put("group", dataSnapshot.child("userGroup").getValue().toString());
                 PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                         .addOnCompleteListener(new OnCompleteListener() {
                             @Override
                             public void onComplete(@NonNull Task task)
                             {
                                 if(task.isSuccessful())
                                 {
                                     SendUserToMainActivity();
                                     Toast.makeText(PostActivity.this, "Post is updated successfully", Toast.LENGTH_SHORT).show();
                                     loadingBar.dismiss();
                                 }
                                 else
                                 {
                                     Toast.makeText(PostActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                                     loadingBar.dismiss();
                                 }
                             }
                         });



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    //post image of post
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public void onPositiveButtonClicked(String[] list, ArrayList<String> selectedItemList)
    {
        StringBuilder stringBuilder = new StringBuilder();
        //stringBuilder.append("Selected Choices = ");
        for(String str:selectedItemList)
        {
            stringBuilder.append(str + " ");

        }
        tagsList.setText(stringBuilder);
        finalTags = String.valueOf(stringBuilder);

    }

    @Override
    public void onNegativeButtonClicked()
    {
        tagsList.setText("Dialog Cancel");
    }
}