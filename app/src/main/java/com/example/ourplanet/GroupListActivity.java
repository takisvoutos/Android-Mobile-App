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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupListActivity extends AppCompatActivity {

    private RecyclerView groupList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef, GroupRef;
    ImageView back;

    String currentUserID, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        groupList = (RecyclerView) findViewById(R.id.all_group_list);
        groupList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        groupList.setLayoutManager(linearLayoutManager);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backMain = new Intent (GroupListActivity.this, MainActivity.class);
                startActivity(backMain);
            }
        });
        DisplayAllGroups();
    }
    private void DisplayAllGroups()
    {
        FirebaseRecyclerOptions<Groups> options = new FirebaseRecyclerOptions.Builder<Groups>()
                .setQuery(GroupRef, Groups.class)
                .build();
        FirebaseRecyclerAdapter<Groups, GroupListActivity.GroupsViewHolder> adapter = new FirebaseRecyclerAdapter<Groups, GroupListActivity.GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GroupListActivity.GroupsViewHolder holder, int position, @NonNull Groups model)
            {
//                Log.e("",model.Id);
                final String groupName = getRef(position).getKey();
                holder.groupName.setText(getRef(position).getKey());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent clickPostIntent = new Intent (GroupListActivity.this, UsersListActivity.class);
                        clickPostIntent.putExtra("groupName", groupName);
                        startActivity(clickPostIntent);
                    }
                });
            }
            @NonNull
            @Override
            public GroupListActivity.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_groups_layout, parent, false);
                GroupListActivity.GroupsViewHolder viewHolder = new GroupListActivity.GroupsViewHolder(view);
                return viewHolder;
            }
        };

        adapter.startListening();
        groupList.setAdapter(adapter);
    }
    public static class GroupsViewHolder extends RecyclerView.ViewHolder
    {
        TextView groupName;
        public GroupsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
        }
    }
}