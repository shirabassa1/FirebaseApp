package com.example.firebaseapp;

import android.graphics.Bitmap;

public class User
{
    private String email;
    private String nickname;
    private int age;
    private String Uid;
    private Bitmap profilePic;

    public User() {}

    public User (String email, String nickname, int age)
    {
        this.email = email;
        this.nickname = nickname;
        this.age = age;
    }

    public User(String email, String nickname, int age, String uid, Bitmap profilePic) {
        this.email = email;
        this.nickname = nickname;
        this.age = age;
        this.Uid = uid;
        this.profilePic = profilePic;
    }

    public User(String email, String nickname, int age, String uid) {
        this.email = email;
        this.nickname = nickname;
        this.age = age;
        this.Uid = uid;
    }

    public User(String email, String uid)
    {
        this.email = email;
        this.Uid = uid;
    }

    public User (String email)
    {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }
}
