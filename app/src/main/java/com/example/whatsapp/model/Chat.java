package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;

public class Chat {
    private String idSend;
    private String idArrived;
    private String lastMessage;
    private User user;
    private String date;

    public Chat() {

    }

    public void save() {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference chatRef           = databaseReference.child("Chats");
        chatRef.child(this.getIdSend())
               .child(this.getIdArrived())
               .setValue(this);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIdSend() {
        return idSend;
    }

    public void setIdSend(String idSend) {
        this.idSend = idSend;
    }

    public String getIdArrived() {
        return idArrived;
    }

    public void setIdArrived(String idArrived) {
        this.idArrived = idArrived;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
