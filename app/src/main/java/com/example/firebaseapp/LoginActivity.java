package com.example.firebaseapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.firebaseapp.FBRefs.refAuth;
import static com.example.firebaseapp.FBRefs.refUsers;
import static com.example.firebaseapp.FBRefs.refImages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends MasterActivity
{
    private TextView txtKey;
    private EditText etEmail, etPass, etValue;
    private Button btnLoginSign, btnSetInfo;
    private String email, pass;
    private final String[] FIELDS = {"nickname", "age"};
    private int currFieldInd = 0;
    private static final int REQUEST_PICK_IMAGE = 301;

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
                                currUser = new User(email,
                                        snapshot.child("nickname").getValue(String.class),
                                        snapshot.child("age").getValue(Integer.class),
                                        refAuth.getCurrentUser().getUid());

                                userUploadProfileImage(null);
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
                                    DataSnapshot snapshot = taskDB.getResult();

                                    if (snapshot.exists())
                                    {
                                        String nickname = snapshot.child("nickname").getValue(String.class);
                                        Integer age = snapshot.child("age").getValue(Integer.class);
                                        currUser = new User(email, nickname, age, refAuth.getCurrentUser().getUid());
                                    }

                                    if (taskDB.getResult().exists()) // User exists in the db too
                                    {
                                        userUploadProfileImage(null);
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
        currUser = new User(email, refAuth.getCurrentUser().getUid());

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

            txtKey.setVisibility(View.GONE);
            etValue.setVisibility(View.GONE);
            btnSetInfo.setVisibility(View.GONE);

            userUploadProfileImage(null);
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

    public void userUploadProfileImage(View view)
    {
        DocumentReference imageRef = refImages.document(refAuth.getCurrentUser().getUid());

        imageRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot imageSnapshot)
                    {
                        if (imageSnapshot.exists())
                        {
                            Blob blob = imageSnapshot.getBlob("imageData");

                            if (blob != null)
                            {
                                byte[] bytes = blob.toBytes();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                String uid = refAuth.getCurrentUser().getUid();

                                refUsers.child(uid).get().addOnCompleteListener(task ->
                                {
                                    if (task.isSuccessful())
                                    {
                                        DataSnapshot snapshot = task.getResult();

                                        if (snapshot.exists())
                                        {
                                            String nickname = snapshot.child("nickname").getValue(String.class);
                                            updateActionBar(nickname, bitmap);
                                            currUser.setProfilePic(bitmap);
                                            finishActivity();
                                        }
                                    }
                                });
                            }
                        }
                        else
                        {
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, REQUEST_PICK_IMAGE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(LoginActivity.this, "Image download failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri)
    {
        if (imageUri != null)
        {
            String Uid = refAuth.getCurrentUser().getUid();

            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Uploading...");
            pd.show();

            try
            {
                InputStream stream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                stream = getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = IOUtils.toByteArray(stream);

                Map<String, Object> imageMap = new HashMap<>();

                imageMap.put("Uid", Uid);
                imageMap.put("imageData", Blob.fromBytes(imageBytes));

                refImages.document(Uid).set(imageMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void unused)
                            {
                                pd.dismiss();
                                Toast.makeText(LoginActivity.this, "Uploaded seccessfuly",
                                        Toast.LENGTH_SHORT).show();
                                currUser.setProfilePic(bitmap);
                                finishActivity();
                            }
                        })

                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                pd.dismiss();
                                Toast.makeText(LoginActivity.this, "Upload failed",
                                        Toast.LENGTH_SHORT).show();
                                finishActivity();
                            }
                        });
            }
            catch (IOException e)
            {
                e.printStackTrace();
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "Error processing image",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void finishActivity()
    {
        FBRefs.setUser(currUser);
        Intent intent = new Intent(this, ManageListActivity.class);
        startActivity(intent);
        finish();
    }
}