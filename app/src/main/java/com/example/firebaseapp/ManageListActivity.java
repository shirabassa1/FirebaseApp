package com.example.firebaseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.firebaseapp.FBRefs.refUsers;
import static com.example.firebaseapp.FBRefs.refUsersData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageListActivity extends AppCompatActivity
{
    Button btnAddItem, btnSet;
    TextView txtKey;
    EditText etValue;
    private ListView lvInfo;
    private User currUser;
    Item currItem;
    private final String[] FIELDS = {"name", "quantity", "price", "bought"};
    private int currFieldInd = 0;
    private ArrayList<Item> currUserData = new ArrayList<>();
    private ValueEventListener usersDataListener;
    private ShoppingListAdapter adp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_list);

        init();
    }

    private void init()
    {
        btnAddItem = findViewById(R.id.btnAddItem);
        txtKey = findViewById(R.id.txtKey);
        etValue = findViewById(R.id.etValue);
        btnSet = findViewById(R.id.btnSet);

        createButtons();

        btnSet.setEnabled(false);
        btnAddItem.setVisibility(View.VISIBLE);
        btnSet.setVisibility(View.GONE);
        etValue.setVisibility(View.GONE);
        txtKey.setVisibility(View.GONE);

        getUser();
    }

    private void createButtons()
    {
        btnAddItem.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                txtKey.setText("");
                etValue.setText("");
                btnAddItem.setEnabled(false);
                btnSet.setEnabled(true);

                btnAddItem.setVisibility(View.GONE);
                btnSet.setVisibility(View.VISIBLE);
                etValue.setVisibility(View.VISIBLE);
                txtKey.setVisibility(View.VISIBLE);

                currItem = new Item();
                addItem();
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (usersDataListener != null)
        {
            refUsersData.removeEventListener(usersDataListener);
        }
    }

    private void getUser()
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

                adp = new ShoppingListAdapter(ManageListActivity.this, currUserData);
                lvInfo.setAdapter(adp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addItem()
    {
        if (currFieldInd >= FIELDS.length)
        {
            txtKey.setText("");
            etValue.setText("");
            btnSet.setEnabled(false);
            btnAddItem.setEnabled(true);

            btnAddItem.setVisibility(View.VISIBLE);
            btnSet.setVisibility(View.GONE);
            etValue.setVisibility(View.GONE);
            txtKey.setVisibility(View.GONE);

            currUserData.add(currItem);
            adp.notifyDataSetChanged();
            refUsersData.child(currUser.getUid()).child(currItem.getName()).setValue(currItem);

            currFieldInd = 0;
            return;
        }

        String key = FIELDS[currFieldInd];

        txtKey.setText(key);
        etValue.setText("");

        btnSet.setOnClickListener(view ->
        {
            clickingSetAction(key);
        });

        etValue.setOnEditorActionListener((v, actionId, event) ->
        {
            clickingSetAction(key);
            return true;
        });
    }

    private void clickingSetAction(String key)
    {
        String enteredValue = etValue.getText().toString().trim();

        if (!enteredValue.isEmpty())
        {
            try
            {
                java.lang.reflect.Field field = currItem.getClass().getDeclaredField(key);
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object value;

                if (fieldType == String.class)
                {
                    value = enteredValue;
                }
                else if (fieldType == int.class || fieldType == Integer.class)
                {
                    value = Integer.parseInt(enteredValue);
                }
                else if (fieldType == double.class || fieldType == Double.class)
                {
                    value = Double.parseDouble(enteredValue);
                }
                else if (fieldType == boolean.class || fieldType == Boolean.class)
                {
                    if (enteredValue.equalsIgnoreCase("true") || enteredValue.equalsIgnoreCase("yes") || enteredValue.equals("1"))
                    {
                        value = true;
                    }
                    else if (enteredValue.equalsIgnoreCase("false") || enteredValue.equalsIgnoreCase("no") || enteredValue.equals("0"))
                    {
                        value = false;
                    }
                    else
                    {
                        etValue.setError("Enter true/false, yes/no, or 1/0");
                        return;
                    }
                }
                else
                {
                    etValue.setError("Unsupported field type");
                    return;
                }

                field.set(currItem, value);
                currFieldInd++;
                addItem();
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                txtKey.setError("Field not found in User class");
            }
            catch (NumberFormatException e)
            {
                etValue.setError("Invalid input for the field type");
            }
        }
    }
}