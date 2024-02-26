package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;
    SearchView search_bar;
    ImageView location;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef, GroupRef, FriendRef;

    String currentUserID, currentUserName, currentGroup;
    String adminId;
    ArrayList<String> friendIdList;
    Boolean LikeChecker = false;

    Query filterQuery;

    protected LocationManager locationManager;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    double latitude, longitude;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int currentNotificationID = 0;
    private Bitmap icon;
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    private String placeName;
    private LatLng placePosition;

    FirebaseRecyclerAdapter<Posts, PostsViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        friendIdList = new ArrayList<>();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);
//map location
        checkLocationPermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);

//        final Handler h = new Handler();
//        h.postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                getData();
//                h.postDelayed(this, 60000*15);
//            }
//        }, 60000*15);
//
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        search_bar = (SearchView) findViewById(R.id.search_bar);

        location = (ImageView) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent MapIntent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(MapIntent);
            }
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                    if(childDataSnapshot.hasChild("admin")) adminId = childDataSnapshot.getKey();
                }
                if(dataSnapshot.child(currentUserID).hasChild("fullname"))
                {
                    String fullname = dataSnapshot.child(currentUserID).child("fullname").getValue().toString();
                    currentUserName = fullname;
                    NavProfileUserName.setText(fullname);
                }
                if (dataSnapshot.child(currentUserID).hasChild("userGroup")) currentGroup = dataSnapshot.child(currentUserID).child("userGroup").getValue().toString();
                if (dataSnapshot.child(currentUserID).hasChild("profileImage"))
                {
                    String image = dataSnapshot.child(currentUserID).child("profileImage").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Profile name do not exists.", Toast.LENGTH_SHORT).show();
                }
                FriendRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            for(DataSnapshot childDataSnapshot : dataSnapshot.getChildren()){
                                friendIdList.add(childDataSnapshot.getKey());
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                    }
                });
                DisplayAllUsersPosts();
                FilterPosts("");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
//                if(currentGroup == null) {
//                    Toast.makeText(getApplicationContext(), "Please Join to one Group or create new Group !", Toast.LENGTH_SHORT).show();
//                } else {
                    SendUserToPostActivity();
//                }
            }
        });
    }
    void getData(){
        String url = String.format("https://api.mapbox.com/geocoding/v5/mapbox.places/waterfall&lake&mountain&sea&beach.json?proximity="+longitude+","+latitude+"&access_token="+getString(R.string.token));

        RequestQueue ExampleRequestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest ExampleRequest = new JsonObjectRequest(Request.Method.GET, url, null ,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray=response.getJSONArray("features");
                    ArrayList<Features> featuresList=new ArrayList<Features>();
                    ArrayList<Double> distances = new ArrayList<>();
                    ArrayList<String> notificationNameList = new ArrayList<>();
                    ArrayList<LatLng> centerList = new ArrayList<>();

                    if(jsonArray!=null){
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject object=jsonArray.getJSONObject(i);
                            JSONArray center = new JSONArray(object.getString("center"));
//                            Log.e("", "+++++++++++"+center);
                            centerList.add(new LatLng(Double.parseDouble(center.get(1).toString()), Double.parseDouble(center.get(0).toString())));
                            double temp = CalculationByDistance(new LatLng(Double.parseDouble(center.get(1).toString()), Double.parseDouble(center.get(0).toString())), new LatLng(latitude,longitude));
                            if(temp <1000) notificationNameList.add(object.getString("place_name"));
                            distances.add(CalculationByDistance(new LatLng(Double.parseDouble(center.get(1).toString()), Double.parseDouble(center.get(0).toString())), new LatLng(latitude,longitude)));
                            Features features=new Features();
                            features.setPlace_name(object.getString("place_name"));
                            featuresList.add(features);
                        }
                    }
//                    Log.e("", distances.toString());
//                    Log.e("", notificationNameList.toString());
                    if(distances.size() >0) {
                        placeName = featuresList.get(0).getPlace_name();
                        placePosition = centerList.get(0);
                        scheduleNotification(getNotification( "5 second delay" ) , 1000 ) ;
                    }
                    Toast.makeText(getApplicationContext(), featuresList.get(0).getPlace_name(), Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        });
        ExampleRequestQueue.add(ExampleRequest);
    }
//notification
    private void scheduleNotification (Notification notification , int delay) {
        Intent notificationIntent = new Intent( this, MyNotificationPublisher. class ) ;
        notificationIntent.putExtra(MyNotificationPublisher. NOTIFICATION_ID , 1 ) ;
        notificationIntent.putExtra(MyNotificationPublisher. NOTIFICATION , notification) ;
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( this, 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        long futureInMillis = SystemClock. elapsedRealtime () + delay ;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context. ALARM_SERVICE ) ;
        assert alarmManager != null;
        alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP , futureInMillis , pendingIntent) ;
    }
    private Notification getNotification (String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, default_notification_channel_id ) ;
        builder.setContentTitle("Είστε κοντά σε βουνό/νερό.") ;
        //builder.setContentText(placePosition.getLatitude()+" : "+placePosition.getLongitude()) ;
        builder.setContentText("Μην πετάτε πλαστικά/τσιγάρα & σκουπίδια!");
        builder.setSmallIcon(R.drawable. ic_launcher_foreground ) ;
        builder.setAutoCancel( true ) ;
        builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
        return builder.build() ;
    }
//
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.getLatitude();
        double lat2 = EndP.getLatitude();
        double lon1 = StartP.getLongitude();
        double lon2 = EndP.getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
//        Log.e("Radius Value", "" + valueResult + "   KM  " + kmInDec
//                + " Meter   " + meterInDec);

        return Radius * c;
    }
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("xxxxxx")
                        .setMessage("xxxxxxxxx")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            locationManager.removeUpdates(this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }
    private void FilterPosts(final String searchText){

        filterQuery = PostsRef;//.orderByKey().startAt(searchText).endAt(searchText + "\uf8ff");

        filterQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        final FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(filterQuery, Posts.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
            {
                try {
                        if(
                                model.getFullname().toLowerCase().contains(searchText.toLowerCase()) ||
                                model.getUid().toLowerCase().contains(searchText.toLowerCase()) ||
                                model.getFeeling().toLowerCase().contains(searchText.toLowerCase()) ||
                                model.getGroup().toLowerCase().contains(searchText.toLowerCase()) ||
                                model.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                        {
                            if(model.getUid().equalsIgnoreCase(adminId) || model.getUid().equalsIgnoreCase(currentUserID)){

                                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT ));
                                ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(holder.itemView.getLayoutParams());
                                marginLayoutParams.setMargins(10, 10, 10, 10);
                                holder.itemView.setLayoutParams(marginLayoutParams);

                                final String PostKey = getRef(position).getKey();
                                holder.userName.setText(model.getFullname());
                                holder.postDate.setText(model.getDate());
                                holder.postTime.setText(model.getTime());
                                holder.postDescription.setText(model.getDescription());

                                if(model.getFeeling() != null)
                                {
                                    holder.postFeeling.setText("Feeling - " + model.getFeeling());
                                }
                                Picasso.get().load(model.getPostimage()).into(holder.postImage);
                                Picasso.get().load(model.getProfileimage()).into(holder.profileImage);

                                holder.setLikeButtonStatus(PostKey);

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent clickPostIntent = new Intent (MainActivity.this, ClickPostActivity.class);
                                        clickPostIntent.putExtra("PostKey", PostKey);
                                        startActivity(clickPostIntent);
                                    }
                                });

                                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        Intent commentsIntent = new Intent (MainActivity.this, CommentsActivity.class);
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
                            } else {
                                holder.itemView.setVisibility(View.GONE);
                                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                            }
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }
                } catch (Exception e){
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };
        adapter.startListening();
        postList.setAdapter(adapter);
    }
    private void DisplayAllUsersPosts()
    {
        if (search_bar != null){
            search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    FilterPosts(query);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
//                    Query query;
//                    if(!(newText.isEmpty())) {
//                        query = PostsRef.orderByKey().startAt(newText).endAt(newText + "\uf8ff");
//                    }
//                    else{
//                        query = PostsRef;
//                    }
                    FilterPosts(newText);
                   return true;
                }
            });
        } else {
            FilterPosts("");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        getData();
//        Log.e("", String.valueOf(location.getLongitude()));
    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, postDescription, postDate, postTime, postFeeling;
        CircleImageView profileImage;
        ImageView postImage;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId;
        DatabaseReference LikesRef;


        public PostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.post_user_name);
            postDescription = itemView.findViewById(R.id.post_description);
            postDate = itemView.findViewById(R.id.post_date);
            postTime = itemView.findViewById(R.id.post_time);
            profileImage = itemView.findViewById(R.id.post_profile_image);
            postImage = itemView.findViewById(R.id.post_image);
            postFeeling = itemView.findViewById(R.id.post_feeling);

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

    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    //Check if user is registered to login
    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        //check if user is authenticated
        if(currentUser == null)
        {
         SendUserToLoginActivity();
        }
        //check if user is in realtime database
        else
        {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence()
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }



    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserToFindfriendsActivity();
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_create_group:
                RequestNewGroup();
                Toast.makeText(this, "Create Group", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_groups:
                SendUserToGroupListActivity();
                Toast.makeText(this, "Groups", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

        }
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder gbuilder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        gbuilder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g. MyGroup");
        gbuilder.setView(groupNameField);

        gbuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName = groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "Please write a group name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    CreateNewGroup(groupName);
                }
            }
        });

        gbuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        gbuilder.show();
    }

    private void CreateNewGroup(final String groupName)
    {
        HashMap userMap = new HashMap();
        userMap.put("userGroup", groupName);
        UsersRef.child(currentUserID).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Your group has been created successfully", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
        GroupRef.child(groupName).child(currentUserID).setValue(Calendar.getInstance().getTimeInMillis())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName + "group created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToFriendsActivity()
    {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToProfileActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToFindfriendsActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToGroupListActivity()
    {
        Intent groupListIntent = new Intent(MainActivity.this, GroupListActivity.class);
        startActivity(groupListIntent);
//        finish();
    }

}