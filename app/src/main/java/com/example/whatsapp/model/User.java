package com.example.whatsapp.model;


import com.example.whatsapp.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable {

    private String userId;
    private String name;
    private String phoneNumber;
    private String status;
    private String photo;

    public User() {
    }


    public void save() {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference user              = databaseReference.child("Users").child(getUserId());
        user.setValue(this);
    }

    public void update() {
        DatabaseReference firebase = ConfigFirebase.getDatabaseReference();
        DatabaseReference useRef   = firebase.child("Users").child(getUserId());

        Map<String, Object> valuesUser= convertToMap();
        useRef.updateChildren(valuesUser);
    }

    @Exclude
    public Map<String, Object> convertToMap() {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("phoneNumber", getPhoneNumber());
        userMap.put("name", getName());
        userMap.put("photo", getPhoto());
        userMap.put("status", getStatus());
        return userMap;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
