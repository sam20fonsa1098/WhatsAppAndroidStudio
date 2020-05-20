package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;

public class Message {
    private String idUser;
    private String message;
    private String image;
    private String hour;
    private String name;

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


    public void save(String userDestId, String idSend) {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference message           = databaseReference
                .child("Messages")
                .child(this.idUser)
                .child(userDestId);
        this.setIdUser(idSend);
        message.push().setValue(this);
    }

    public void arrive(String userArrivId) {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference message           = databaseReference
                .child("Messages")
                .child(userArrivId)
                .child(this.idUser);
        message.push().setValue(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
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
