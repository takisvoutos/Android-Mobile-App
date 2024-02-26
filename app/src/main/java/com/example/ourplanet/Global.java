package com.example.ourplanet;

import android.app.Application;

class Global extends Application {

    private String userId;
    private String userEmail;
    private String userPassword;
    private String userName;
    private String userProfilePhoto;
    private int userAge;
    private int userGroup;

    Global(String userName, String userId, String userEmail,String userPassword, String userProfilePhoto, int userAge, int userGroup) {
        this.userName = userName;
        this.userId = userId;
        this.userProfilePhoto = userProfilePhoto;
        this.userAge = userAge;
        this.userGroup = userGroup;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
    public String getUserId(){
        return userId;
    }
    public void setUserId(String str){
        userId = str;
    }
    public String getUserEmail(){
        return userEmail;
    }
    public void setUserEmail(String str){
        userEmail = str;
    }
    public String getUserPassword(){
        return userPassword;
    }
    public void setUserPassword(String str){
        userPassword = str;
    }
    public String getUserName(){
        return userName;
    }
    public void setUserName(String str){
        userName = str;
    }public String getUserProfilePhoto(){
        return userProfilePhoto;
    }
    public void setUserProfilePhoto(String str){
        userProfilePhoto = str;
    }public int getUserAge(){
        return userAge;
    }
    public void setUserAge(int age){
        userAge = age;
    }
    public int getUserGroup(){
        return userGroup;
    }
    public void setUserGroup(int group){
        userGroup = group;
    }
}
