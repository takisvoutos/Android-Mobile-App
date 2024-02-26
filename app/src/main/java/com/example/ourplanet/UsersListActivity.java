package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersListActivity extends AppCompatActivity {

    private RecyclerView groupList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef, GroupRef;
    ImageView back;
    TextView title, join_group, leave_group;

    String currentUserID, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        Intent intent = getIntent();
        final String groupName = intent.getStringExtra("groupName");

        title = findViewById(R.id.users_title);
        title.setText(groupName);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);

        groupList = (RecyclerView) findViewById(R.id.all_user_list);
        groupList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        groupList.setLayoutManager(linearLayoutManager);

        back = findViewById(R.id.back_users_page);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backGroup = new Intent (UsersListActivity.this, GroupListActivity.class);
                startActivity(backGroup);
            }
        });
        leave_group = findViewById(R.id.leave_group);
        leave_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupRef.child(currentUserID).removeValue();
                UsersRef.child(currentUserID).child("userGroup").removeValue();
            }
        });
        join_group = findViewById(R.id.join_group);
        join_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupRef.child(currentUserID).setValue(Calendar.getInstance().getTimeInMillis())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    HashMap userMap = new HashMap();
                                    userMap.put("userGroup", groupName);
                                    UsersRef.child(currentUserID).updateChildren(userMap);
                                    Toast.makeText(UsersListActivity.this, groupName + "group joined successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        DisplayAllUsers();
    }
    private void DisplayAllUsers()
    {
//        FirebaseRecyclerOptions<Groups> options = new FirebaseRecyclerOptions.Builder<Groups>()
//                .setQuery(GroupRef, Groups.class)
//                .build();
        FirebaseRecyclerOptions<Groups> options =
                new FirebaseRecyclerOptions.Builder<Groups>()
                        .setQuery(GroupRef, new SnapshotParser<Groups>() {
                            @NonNull
                            @Override
                            public Groups parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Groups(snapshot.getKey());
                            }
                        })
                        .build();
        FirebaseRecyclerAdapter<Groups, UsersListActivity.GroupsViewHolder> adapter = new FirebaseRecyclerAdapter<Groups, UsersListActivity.GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersListActivity.GroupsViewHolder holder, int position, @NonNull final Groups model)
            {
                Log.e("",model.Id);
                UsersRef.child(model.Id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        String fullName = snapshot.child("fullname").getValue().toString();
                        String avatar = snapshot.child("profileImage").getValue().toString();
                        System.out.println(fullName);
                        holder.userName.setText(fullName);
                        Picasso.get().load(avatar).into(holder.userAvatar);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
            @NonNull
            @Override
            public UsersListActivity.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_layout, parent, false);
                UsersListActivity.GroupsViewHolder viewHolder = new UsersListActivity.GroupsViewHolder(view);
                return viewHolder;
            }
        };

        adapter.startListening();
        groupList.setAdapter(adapter);
    }
    public static class GroupsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;
        CircleImageView userAvatar;
        public GroupsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userAvatar = itemView.findViewById(R.id.user_profile_image);
        }
    }
}