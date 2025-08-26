package com.example.firebaseapp;

public class User
{
    private String email;
    private String nickname;
    private int age;
    private String Uid;

    public User() {}

    public User (String email, String nickname, int age)
    {
        this.email = email;
        this.nickname = nickname;
        this.age = age;
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
}
