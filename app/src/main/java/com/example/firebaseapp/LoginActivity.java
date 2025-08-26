package com.example.firebaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.firebaseapp.FBHandler.refAuth;
import static com.example.firebaseapp.FBHandler.refUsers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
{
    private TextView txtKey;
    private EditText etEmail, etPass, etValue;
    private Button btnLoginSign, btnSetInfo;
    private String email, pass;
    User currUser;
    private final String[] FIELDS = {"nickname", "age"};
    private int currFieldInd = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        checkCurrentUser();
    }

    private void init()
    {
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        btnLoginSign = findViewById(R.id.btnLoginSign);

        txtKey = findViewById(R.id.txtKey);
        etValue = findViewById(R.id.etValue);
        btnSetInfo = findViewById(R.id.btnSetInfo);

        btnSetInfo.setEnabled(false);
        btnSetInfo.setVisibility(View.GONE);
        etValue.setVisibility(View.GONE);
        txtKey.setVisibility(View.GONE);

        createButton();
    }

    private void createButton()
    {
        btnLoginSign.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                email = etEmail.getText().toString();
                pass = etPass.getText().toString();

                createUser(email, pass);
            }
        });

        btnLoginSign.setEnabled(false);
    }

    private void activateButton()
    {
        btnLoginSign.setEnabled(true);
    }

    private void checkCurrentUser()
    {
        FirebaseUser user = refAuth.getCurrentUser();

        if (user != null) // Already signed in this phone
        {
            user.reload().addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    email = user.getEmail();

                    refUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (snapshot.exists()) // User exists in the db too
                            {
                                finishActivity();
                            }
                            else // User doesn't appear in the db - create one
                            {
                                finishLogin();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
                else //Signed in but user not exists anymore
                {
                    refAuth.signOut();
                    activateButton();
                }
            });
        }
        else
        {
            activateButton();
        }
    }

    private void createUser(String email, String pass)
    {
        etEmail.setError(null);
        etPass.setError(null);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting to your user...");
        pd.show();

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            finishLogin();
                        }
                        else
                        {
                            Exception exc = task.getException();

                            if (exc instanceof FirebaseAuthInvalidUserException)
                            {
                                etEmail.setError("Invalid email address");
                            }
                            else if (exc instanceof FirebaseAuthWeakPasswordException)
                            {
                                etPass.setError("Password too weak");
                            }
                            else if (exc instanceof FirebaseAuthUserCollisionException)
                            {
                                loginUser(email, pass);
                            }
                            else if (exc instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                etPass.setError("Invalid email or password");
                            }
                            else if (exc instanceof FirebaseNetworkException)
                            {
                                btnLoginSign.setError("Network error. Check your connection");
                            }
                            else
                            {
                                btnLoginSign.setError("An error occurred. Try again later");
                            }
                        }

                        pd.dismiss();
                    }
                });
    }

    private void loginUser(String email, String pass)
    {
        refAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            refUsers.child(refAuth.getCurrentUser().getUid()).get().addOnCompleteListener(taskDB ->
                            {
                                if (taskDB.isSuccessful())
                                {
                                    if (taskDB.getResult().exists()) // User exists in the db too
                                    {
                                        finishActivity();
                                    }
                                    else // User doesn't appear in the db - create one
                                    {
                                        finishLogin();
                                    }
                                }
                                else
                                {
                                    txtKey.setError("FB Error: " + taskDB.getException().getMessage());
                                }
                            });
                        }
                        else
                        {
                            Exception exc = task.getException();

                            if (exc instanceof FirebaseAuthWeakPasswordException)
                            {
                                etPass.setError("Password too weak");
                            }
                            else if (exc instanceof FirebaseAuthUserCollisionException || exc instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                etPass.setError("Wrong password");
                            }
                            else if (exc instanceof FirebaseNetworkException)
                            {
                                btnLoginSign.setError("Network error. Check your connection");
                            }
                            else
                            {
                                btnLoginSign.setError("An error occurred. Try again later");
                            }
                        }
                    }
                });
    }

    private void finishLogin()
    {
        String uid = refAuth.getCurrentUser().getUid();
        currUser = new User(email);
        currUser.setUid(uid);

        btnLoginSign.setEnabled(true);
        btnLoginSign.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        etPass.setVisibility(View.GONE);

        btnSetInfo.setEnabled(true);
        btnSetInfo.setVisibility(View.VISIBLE);
        etValue.setVisibility(View.VISIBLE);
        txtKey.setVisibility(View.VISIBLE);

        getInfo();
    }

    private void getInfo()
    {
        if (currFieldInd >= FIELDS.length)
        {
            txtKey.setText("");
            etValue.setText("");
            btnSetInfo.setEnabled(false);
            refUsers.child(currUser.getUid()).setValue(currUser);

            finishActivity();
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

    private void finishActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}