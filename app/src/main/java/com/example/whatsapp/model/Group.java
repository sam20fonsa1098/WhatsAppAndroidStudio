package com.example.whatsapp.model;

import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    private String id;
    private String name;
    private String photo;
    private List<User> members;

    public Group() {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference groupRef          = databaseReference.child("Groups");
        String idFirebase                   = groupRef.push().getKey();
        setId(idFirebase);
    }

    public void save() {
        DatabaseReference databaseReference = ConfigFirebase.getDatabaseReference();
        DatabaseReference groupRef          = databaseReference.child("Groups");
        groupRef.child(getId())
                .setValue(this);

        for(User user: getMembers()) {
            Chat chat = new Chat();
            chat.setIdSend(Base64Custom.encodeBase64(user.getPhoneNumber()));
            chat.setIdArrived(getId());
            chat.setLastMessage("");
            chat.setIsGroup(true);
            chat.setGroup(this);
            chat.save();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }
}
