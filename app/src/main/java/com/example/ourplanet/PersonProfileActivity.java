package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendReqButton, DeclineFriendRequestButton;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String sendertUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        sendertUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        IntializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileImage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
//                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
//                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
//                    userDOB.setText("DOB: " + myDOB);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
//                    userRelation.setText("Relationship[: " + myRelationStatus);

                    MaintananceofButtons();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(!sendertUserId.equals(receiverUserId))
        {
            SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SendFriendReqButton.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendsRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnFriendExistingFriend();
                    }
                }
            });
        }
        else
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }

    }

    private void UnFriendExistingFriend()
    {
        FriendsRef.child(sendertUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(sendertUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendsRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsRef.child(sendertUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(sendertUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                FriendRequestRef.child(sendertUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(sendertUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        SendFriendReqButton.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        SendFriendReqButton.setText("Unfriend");

                                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(sendertUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(sendertUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintananceofButtons()
    {
        FriendRequestRef.child(sendertUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_type";
                                SendFriendReqButton.setText("Cancel Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }
                            else  if (request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                SendFriendReqButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            FriendsRef.child(sendertUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE = "friends";
                                                SendFriendReqButton.setText("Unfriend");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestToaPerson()
    {
        FriendRequestRef.child(sendertUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(sendertUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendFriendReqButton.setText("Cancel Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void IntializeFields()
    {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView) findViewById(R.id.person_fullname);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelation = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        SendFriendReqButton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton = (Button) findViewById(R.id.person_decline_friend_request);

        CURRENT_STATE = "not_friends";


    }
}