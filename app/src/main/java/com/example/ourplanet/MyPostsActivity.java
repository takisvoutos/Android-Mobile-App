package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private RecyclerView myPostsList;

    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, UsersRef, LikesRef;
    private String currentUserID;

    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts()
    {

        Query myPostsQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(myPostsQuery, Posts.class)
                .build();


        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> adapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder holder, int position, @NonNull Posts model)
            {

                final String PostKey = getRef(position).getKey();

                holder.userName.setText(model.getFullname());
                holder.postDate.setText(model.getDate());
                holder.postTime.setText(model.getTime());
                holder.postDescription.setText(model.getDescription());
                Picasso.get().load(model.getPostimage()).into(holder.postImage);
                Picasso.get().load(model.getProfileimage()).into(holder.profileImage);


                holder.setLikeButtonStatus(PostKey);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent clickPostIntent = new Intent (MyPostsActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent commentsIntent = new Intent (MyPostsActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        startActivity(commentsIntent);
                    }
                });

                holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        LikeChecker = true;

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(LikeChecker.equals(true))
                                {
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });


            }

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                MyPostsViewHolder viewHolder = new MyPostsViewHolder(view);
                return viewHolder;
            }
        };

        adapter.startListening();

        myPostsList.setAdapter(adapter);

    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, postDescription, postDate, postTime;
        CircleImageView profileImage;
        ImageView postImage;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;

        public MyPostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.post_user_name);
            postDescription = itemView.findViewById(R.id.post_description);
            postDate = itemView.findViewById(R.id.post_date);
            postTime = itemView.findViewById(R.id.post_time);
            profileImage = itemView.findViewById(R.id.post_profile_image);
            postImage = itemView.findViewById(R.id.post_image);

            LikePostButton = itemView.findViewById(R.id.like_button);
            CommentPostButton = itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}