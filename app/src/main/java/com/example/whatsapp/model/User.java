package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class User {

    private String userId;
    private String name;
    private String phoneNumber;

    public User() {
    }

    public void save() {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference user              = databaseReference.child("Users").child(getUserId());
        user.setValue(this);
    }

    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
