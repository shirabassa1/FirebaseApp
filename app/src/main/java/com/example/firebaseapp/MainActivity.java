package com.example.firebaseapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.firebaseapp.FBHandler.refUsers;
import static com.example.firebaseapp.FBHandler.refUsersData;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String email = getIntent().getStringExtra("email");
        User currUser = new User(email);
        currUser.setKeyID(refUsers.push().getKey());

        refUsers.child(currUser.getKeyID()).setValue(currUser);
    }
}