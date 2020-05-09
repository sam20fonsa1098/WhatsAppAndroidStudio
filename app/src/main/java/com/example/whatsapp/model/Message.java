package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Message {
    private String idUser;
    private String message;
    private String image;

    public Message() {
    }

    public void save(String userDestId) {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference message           = databaseReference
                                                            .child("Messages")
                                                            .child(this.idUser)
                                                            .child(userDestId);
        message.push().setValue(this);
    }

    public void arrive(String userArrivId, String currentId) {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference message           = databaseReference
                .child("Messages")
                .child(userArrivId)
                .child(currentId);
        message.push().setValue(this);
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
