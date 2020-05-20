package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;

public class Chat {
    private String idSend;
    private String idArrived;
    private String lastMessage;
    private User user;
    private String date;
    private boolean isGroup;
    private Group group;

    public Chat() {
        this.setIsGroup(false);
    }

    public void save(User user) {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference chatRef           = databaseReference.child("Chats");
        chatRef.child(this.getIdSend())
               .child(this.getIdArrived())
               .setValue(this);

        if(!getIsGroup()) {
            this.setUser(user);
            chatRef.child(this.getIdArrived())
                    .child(this.getIdSend())
                    .setValue(this);
        }

    }

    public void save() {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference chatRef           = databaseReference.child("Chats");
        chatRef.child(this.getIdSend())
                .child(this.getIdArrived())
                .setValue(this);

        if(!getIsGroup()) {
            chatRef.child(this.getIdArrived())
                    .child(this.getIdSend())
                    .setValue(this);
        }

    }

    public void save(String id) {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference chatRef           = databaseReference.child("Chats");
        chatRef.child(id)
                .child(this.getIdArrived())
                .setValue(this);
    }


    public boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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
