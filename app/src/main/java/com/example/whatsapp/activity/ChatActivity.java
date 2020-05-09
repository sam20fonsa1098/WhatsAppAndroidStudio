package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.adapter.MessagesAdapter;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.model.Message;
import com.example.whatsapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private User userClicked;
    private User currentUser;
    private CircleImageView circleImageView;
    private TextView textView;
    private EditText editTextSend;
    private FloatingActionButton floatingActionButtonSend;
    private ImageView imageViewSend;
    private RecyclerView recyclerViewMessages;
    private MessagesAdapter messagesAdapter;
    private List<Message> listMessages = new ArrayList<>();
    private DatabaseReference databaseReferenceMessages;
    private ChildEventListener childEventListener;
    private static final int SELECTION_CAMERA = 100;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userClicked               = (User) getIntent().getSerializableExtra("UserClicked");
        circleImageView           = findViewById(R.id.circleImageChat);
        textView                  = findViewById(R.id.textViewChatName);
        editTextSend              = findViewById(R.id.editTextSend);
        floatingActionButtonSend  = findViewById(R.id.floatingActionButtonSend);
        imageViewSend             = findViewById(R.id.imageViewSend);
        currentUser               = CurrentUserFirebase.getUser();
        recyclerViewMessages      = findViewById(R.id.recyclerViewMessages);
        storageReference          = ConfigFirebase.getFirebaseStorage();
        databaseReferenceMessages = ConfigFirebase.getDatabaseReference()
                .child("Messages")
                .child(Base64Custom.encodeBase64(currentUser.getPhoneNumber()))
                .child(Base64Custom.encodeBase64(userClicked.getPhoneNumber()));

        Uri url = Uri.parse(userClicked.getPhoto());
        if(url == null) {
            circleImageView.setImageResource(R.drawable.padrao);
        }
        else {
            Glide.with(ChatActivity.this)
                    .load(url)
                    .into(circleImageView);
        }
        textView.setText(userClicked.getName());

        floatingActionButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //Config adapter
        messagesAdapter = new MessagesAdapter(listMessages, getApplicationContext());
        //Config recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setHasFixedSize(true);
        recyclerViewMessages.setAdapter(messagesAdapter);

        //Click Event on Camera
        imageViewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECTION_CAMERA);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void sendMessage() {
        String message   = editTextSend.getText().toString();
        String idClicked = Base64Custom.encodeBase64(userClicked.getPhoneNumber());
        if(!message.isEmpty()) {
            Message msg = new Message();

            msg.setIdUser(Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
            msg.setMessage(message);

            msg.save(idClicked);
            msg.setIdUser(idClicked);
            msg.arrive(idClicked, Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
            editTextSend.setText("");
        }
        else {
            Toast.makeText(this, "Please write your message", Toast.LENGTH_SHORT).show();
        }
    }

    private void takeMessages() {
        childEventListener = databaseReferenceMessages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                listMessages.add(message);
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        takeMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReferenceMessages.removeEventListener(childEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Bitmap image = null;

            try {
                switch (requestCode) {
                    case SELECTION_CAMERA:
                        image = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if (image != null) {
                    String id = CurrentUserFirebase.getIdCurrentUser();
                    //Take data to firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    //Createing a name to the image
                    String nameImage = UUID.randomUUID().toString();

                    //Save image in firebase
                    StorageReference imageRef = storageReference
                            .child("images")
                            .child("photos")
                            .child(id)
                            .child(nameImage);

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatActivity.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(ChatActivity.this, "Success to upload the image", Toast.LENGTH_SHORT).show();

                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            Message message = new Message();
                            message.setIdUser(CurrentUserFirebase.getIdCurrentUser());
                            message.setImage(url.toString());

                            message.save(userClicked.getUserId());
                            message.setIdUser(userClicked.getUserId());
                            message.arrive(userClicked.getUserId(), CurrentUserFirebase.getIdCurrentUser());
                        }
                    });
                }
            }
            catch (Exception e) {
                    e.printStackTrace();
            }
        }
    }
}
