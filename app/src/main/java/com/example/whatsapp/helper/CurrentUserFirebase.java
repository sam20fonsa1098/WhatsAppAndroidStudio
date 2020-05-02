package com.example.whatsapp.helper;

import android.app.usage.NetworkStatsManager;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CurrentUserFirebase {
    private User user;

    public static String getIdCurrentUser () {
        return Base64Custom.encodeBase64(ConfigFirebase.getFirebaseAuth().getCurrentUser().getPhoneNumber());
    }
    public static FirebaseUser getCurrentUser() {
        return ConfigFirebase.getFirebaseAuth().getCurrentUser();
    }
    public static boolean updateName(String name) {
        try {
            FirebaseUser user = getCurrentUser();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {

                    }
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updatePhoto(Uri url) {
        try{
            FirebaseUser user = getCurrentUser();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url)
                    .build();
            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()) {

                    }
                }
            });
            return true;
        }
        catch (Exception e ) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getUser() {
        User user = new User();
        FirebaseUser firebaseUser = getCurrentUser();
        user.setName(firebaseUser.getDisplayName());
        user.setPhoneNumber(firebaseUser.getPhoneNumber());
        user.setUserId(getIdCurrentUser());

        if(firebaseUser.getPhotoUrl() == null) {
            user.setPhoto("");
        }
        else{
            user.setPhoto(firebaseUser.getPhotoUrl().toString());
        }

        return user;

    }

}
