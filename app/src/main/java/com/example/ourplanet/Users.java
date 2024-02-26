package com.example.ourplanet;

public class Users
{
    public String fullName, avatar;

    public Users()
    {
    }
    public Users(String name, String avatar)
    {
        this.fullName = name;
        this.avatar = avatar;
    }


    public String getName() {
        return fullName;
    }
    public void setName(String name) { this.fullName = name; }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
