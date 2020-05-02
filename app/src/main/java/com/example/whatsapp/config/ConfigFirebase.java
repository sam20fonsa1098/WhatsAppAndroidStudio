package com.example.whatsapp.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfigFirebase {
    private static DatabaseReference databaseReference;
    private static FirebaseAuth firebaseAuth;
    private static StorageReference storage;

    public static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    public static DatabaseReference getDatabaseReference() {
        if(databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    public static StorageReference getFirebaseStorage() {
        if(storage == null) {
            storage = FirebaseStorage.getInstance().getReference();
        }
        return storage;
    }
}
