package com.example.firebaseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.firebaseapp.FBHandler.refUsers;
import static com.example.firebaseapp.FBHandler.refUsersData;

public class MainActivity extends AppCompatActivity
{
    private TextView txtKey;
    private EditText etValue;
    private Button btnSetInfo;
    private User currUser;
    private final String[] FIELDS = {"nickname", "age"};
    private int currFieldInd = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        String email = getIntent().getStringExtra("email");
        currUser = new User(email);

        currUser.setKeyID(refUsers.push().getKey());
        refUsers.child(currUser.getKeyID()).setValue(currUser);

        getInfo();
    }

    private void init()
    {
        txtKey = findViewById(R.id.txtKey);
        etValue = findViewById(R.id.etValue);
        btnSetInfo = findViewById(R.id.btnSetInfo);
    }

    private void getInfo()
    {
        if (currFieldInd >= FIELDS.length)
        {
            txtKey.setText("");
            etValue.setText("");
            btnSetInfo.setEnabled(false);

            return;
        }

        String key = FIELDS[currFieldInd];

        txtKey.setText(key);
        etValue.setText("");

        btnSetInfo.setOnClickListener(view ->
        {
            String enteredValue = etValue.getText().toString().trim();

            if (!enteredValue.isEmpty())
            {
                try
                {
                    java.lang.reflect.Field field = currUser.getClass().getDeclaredField(key);
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
                        value = Boolean.parseBoolean(enteredValue);
                    }
                    else
                    {
                        etValue.setError("Unsupported field type");
                        return;
                    }

                    field.set(currUser, value);
                    refUsers.child(currUser.getKeyID()).child(key).setValue(value);

                    currFieldInd++;
                    getInfo();
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
        });
    }
}