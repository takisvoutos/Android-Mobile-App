package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{

    private RecyclerView myFriendList;

    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();

    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendsRef, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendsActivity.FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position, @NonNull final Friends model)
            {

                holder.friendsDate.setText(model.getDate());

                final String usersIDs = getRef(position).getKey();

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileImage").getValue().toString();

                            holder.friendsName.setText(userName);
                            Picasso.get().load(profileImage).into(holder.friendsImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                 userName + "'s Profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if(which == 0)
                                            {
                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("visit_user_id", usersIDs);
                                                startActivity(profileIntent);
                                            }
                                            if (which == 1)
                                            {
                                                Intent ChatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                ChatIntent.putExtra("visit_user_id", usersIDs);
                                                ChatIntent.putExtra("userName", userName);
                                                startActivity(ChatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });





                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();

                        Intent friendsIntent = new Intent(FriendsActivity.this, Friends.class);
                       friendsIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(friendsIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout,parent, false);
                FriendsActivity.FriendsViewHolder viewHolder = new FriendsViewHolder(view);

                return viewHolder;
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {

       TextView friendsName,friendsDate;
       CircleImageView friendsImage;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            friendsName = itemView.findViewById(R.id.all_users_profile_full_name);
            friendsDate = itemView.findViewById(R.id.all_users_status);
            friendsImage = itemView.findViewById(R.id.all_users_profile_image);

        }


    }

}