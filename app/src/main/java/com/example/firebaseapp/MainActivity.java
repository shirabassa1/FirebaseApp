package com.example.firebaseapp;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.firebaseapp.FBRefs.refUsers;
import static com.example.firebaseapp.FBRefs.refUsersData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private ListView lvInfo;
    private User currUser;
    private ArrayList<Item> currUserData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init()
    {
        getUserData();
    }

    private void getUserData()
    {
        String userID = getIntent().getStringExtra("userID");

        if (userID == null)
        {
            return;
        }

        refUsers.child(userID).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    currUser = snapshot.getValue(User.class);

                    createListView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createListView()
    {
        lvInfo = findViewById(R.id.lvInfo);

        refUsersData.child(currUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    for (DataSnapshot data : snapshot.getChildren())
                    {
                        Item item = data.getValue(Item.class);

                        if (item != null)
                        {
                            currUserData.add(item);
                        }
                    }
                }
                else
                {
                    currUserData.add(new Item("h", 4, 3, true));
                    refUsersData.child(currUser.getUid()).child(currUserData.get(0).getName()).setValue(currUserData.get(0));
                }

                ShoppingListAdapter adp = new ShoppingListAdapter(MainActivity.this, currUserData);
                lvInfo.setAdapter(adp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}