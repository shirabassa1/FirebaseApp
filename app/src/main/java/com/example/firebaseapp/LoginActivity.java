package com.example.firebaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import static com.example.firebaseapp.FBHandler.refAuth;

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

public class LoginActivity extends AppCompatActivity
{
    private EditText etEmail, etPass;
    private Button btnLoginSign;
    private String email, pass;

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

        if (user != null)
        {
            user.reload().addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    email = user.getEmail();
                    finishLogin();
                }
                else
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
                            finishLogin();
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}