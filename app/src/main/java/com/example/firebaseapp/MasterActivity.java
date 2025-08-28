package com.example.firebaseapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import static com.example.firebaseapp.FBRefs.refAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MasterActivity extends AppCompatActivity
{
    User currUser;
    TextView tvUsername;
    ImageView ivProfile;
    Button btnLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        currUser = FBRefs.getUser();
        setupActionBar();
    }

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

    protected void setupActionBar()
    {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar_main);

            View customView = actionBar.getCustomView();
            tvUsername = customView.findViewById(R.id.toolbar_username);
            ivProfile = customView.findViewById(R.id.toolbar_profile_image);
            btnLogOut = customView.findViewById(R.id.toolbar_log_out);

            tvUsername.setVisibility(View.GONE);
            ivProfile.setVisibility(View.GONE);
            btnLogOut.setEnabled(false);
            btnLogOut.setVisibility(View.GONE);

            if (currUser != null)
            {
                updateActionBar(currUser.getNickname(), currUser.getProfilePic());
            }

            createButton();
        }
    }

    private void createButton()
    {
        btnLogOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                refAuth.signOut();
                FBRefs.setUser(null);
                Intent intent = new Intent(MasterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    protected void updateActionBar(String username, Bitmap profileBitmap)
    {
        if (tvUsername != null)
        {
            tvUsername.setText(username);
        }

        if (ivProfile != null && profileBitmap != null)
        {
            ivProfile.setImageBitmap(profileBitmap);
        }

        tvUsername.setVisibility(View.VISIBLE);
        ivProfile.setVisibility(View.VISIBLE);
        btnLogOut.setVisibility(View.VISIBLE);
        btnLogOut.setEnabled(true);
    }
}