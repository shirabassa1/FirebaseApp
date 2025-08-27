package com.example.firebaseapp;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import static com.example.firebaseapp.FBRefs.refAuth;
import static com.example.firebaseapp.FBRefs.refUsers;
import static com.example.firebaseapp.FBRefs.refUsersData;

public class MasterActivity extends AppCompatActivity
{
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if ((id == R.id.menuManage) && !(this instanceof ManageListActivity))
        {
            Intent intent = new Intent(this, ManageListActivity.class);
            startActivity(intent);
        }
        else if ((id == R.id.menuViewFilter) && !(this instanceof ViewListActivity))
        {
            Intent intent = new Intent(this, ViewListActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}