package com.example.ourplanet;

public class Friends
{
    public String date, friendsUsername, friendsImage;

    public Friends()
    {

    }

    public Friends(String date, String friendsUsername, String friendsImage) {
        this.date = date;
        this.friendsUsername = friendsUsername;
        this.friendsImage = friendsImage;
    }


    public String getFriendsImage() {
        return friendsImage;
    }

    public void setFriendsImage(String friendsImage) {
        this.friendsImage = friendsImage;
    }

    public String getFriendsUsername() {
        return friendsUsername;
    }

    public void setFriendsUsername(String friendsUsername) {
        this.friendsUsername = friendsUsername;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
