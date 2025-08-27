package com.example.firebaseapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FBRefs
{
    // Auth
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    // Realtime fb
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();
    public static DatabaseReference refUsers = FBDB.getReference("Users");
    public static DatabaseReference refUsersData = FBDB.getReference("UsersData");

    // Firestore
    public static FirebaseFirestore FBFS = FirebaseFirestore.getInstance();
    public static CollectionReference RefImages = FBFS.collection("Images");
}
