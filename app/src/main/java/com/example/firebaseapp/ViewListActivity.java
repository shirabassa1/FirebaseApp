package com.example.firebaseapp;

import static com.example.firebaseapp.FBRefs.refAuth;
import static com.example.firebaseapp.FBRefs.refUsers;
import static com.example.firebaseapp.FBRefs.refUsersData;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class ViewListActivity extends MasterActivity implements AdapterView.OnItemSelectedListener
{
    Spinner spinnerFiltering;
    String[] filteringOptions;
    ListView lvInfo;
    private ArrayList<Item> currUserData = new ArrayList<>();
    private ArrayList<ArrayList<Item>> dataFilteredLists;
    User currUser;
    private ShoppingListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        init();
    }

    private void init()
    {
        lvInfo = findViewById(R.id.lvInfo);

        filteringOptions = getResources().getStringArray(R.array.filteringOptions);
        spinnerFiltering = findViewById(R.id.spinnerFiltering);

        getUser();
    }

    private void getUser()
    {
        String userID = refAuth.getCurrentUser().getUid();

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

                listAdapter = new ShoppingListAdapter(ViewListActivity.this, currUserData);
                lvInfo.setAdapter(listAdapter);

                createFilteredLists();
                createSpinner();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createSpinner()
    {
        spinnerFiltering.setOnItemSelectedListener(this);

        ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filteringOptions);

        spinnerFiltering.setAdapter(spinnerAdp);
    }

    private void createFilteredLists()
    {
        dataFilteredLists = new ArrayList<>();

        dataFilteredLists.add(new ArrayList<>());

        for (int i=0; i<currUserData.size(); i++)
        {
            if (currUserData.get(i).getPrice() < 50)
            {
                dataFilteredLists.get(0).add(currUserData.get(i));
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        if (i == 0)
        {
            listAdapter.updateList(currUserData);
        }
        else
        {
            listAdapter.updateList(dataFilteredLists.get(i-1));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}