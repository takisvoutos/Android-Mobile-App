package com.example.ourplanet;

import com.google.firebase.database.DatabaseReference;

public class Posts
{
    public String uid, time, date,  postimage, description,
            profileimage, fullname, feeling , group;



    public Posts()
    {

    }

    public Posts(String uid, String time, String date, String postimage,
                 String description, String profileimage, String fullname,
                 String feeling, String group)
    {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.feeling = feeling;
        this.group = group;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFeeling() {
        return feeling;
    }

    public void setFeeling(String postFeeling) {
        this.feeling = postFeeling;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
