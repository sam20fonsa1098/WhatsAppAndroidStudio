package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.adapter.MessagesAdapter;
import com.example.whatsapp.api.NotificationService;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.DateUtil;
import com.example.whatsapp.model.Chat;
import com.example.whatsapp.model.DataNotification;
import com.example.whatsapp.model.Group;
import com.example.whatsapp.model.Message;
import com.example.whatsapp.model.Notification;
import com.example.whatsapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private User userClicked;
    private Group groupClicked;
    private User currentUser;
    private CircleImageView circleImageView;
    private TextView textView, textViewGroup;
    private EditText editTextSend;
    private FloatingActionButton floatingActionButtonSend;
    private ImageView imageViewSend;
    private RecyclerView recyclerViewMessages;
    private MessagesAdapter messagesAdapter;
    private List<Message> listMessages = new ArrayList<>();
    private DatabaseReference databaseReferenceMessages;
    private ValueEventListener valueEventListener;
    private static final int SELECTION_CAMERA = 100;
    private StorageReference storageReference;
    private List<String> aux = new ArrayList<>();
    private Retrofit retrofit;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseUrl = "https://fcm.googleapis.com/fcm/";
        retrofit = new Retrofit.Builder()
                .baseUrl( baseUrl )
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        circleImageView           = findViewById(R.id.circleImageChat);
        textView                  = findViewById(R.id.textViewChatName);
        textViewGroup             = findViewById(R.id.textViewChatMembersGroups);
        editTextSend              = findViewById(R.id.editTextSend);
        floatingActionButtonSend  = findViewById(R.id.floatingActionButtonSend);
        imageViewSend             = findViewById(R.id.imageViewSend);
        currentUser               = CurrentUserFirebase.getUser();
        recyclerViewMessages      = findViewById(R.id.recyclerViewMessages);
        storageReference          = ConfigFirebase.getFirebaseStorage();

        if(!getIntent().getExtras().containsKey("UserClicked")) {
            groupClicked = (Group) getIntent().getSerializableExtra("GroupClicked");
            if( groupClicked.getPhoto() != null) {
                Uri url = Uri.parse(groupClicked.getPhoto());
                if(url == null) {
                    circleImageView.setImageResource(R.drawable.padrao);
                }
                else {
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageView);
                }
            }
            else {
                circleImageView.setImageResource(R.drawable.padrao);
            }

            textView.setText(groupClicked.getName());

            databaseReferenceMessages = ConfigFirebase.getDatabaseReference()
                    .child("Messages")
                    .child(Base64Custom.encodeBase64(currentUser.getPhoneNumber()))
                    .child(groupClicked.getId());

            String subtitle = "";
            StringBuilder stringBuilder = new StringBuilder(subtitle);
            for(User user : groupClicked.getMembers()) {
                if(!user.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
                    stringBuilder.insert(stringBuilder.length(), user.getName().split(" ")[0] +", ");
                }
            }
            stringBuilder.insert(stringBuilder.length(), "you");
            textViewGroup.setText(stringBuilder.toString());
        }
        else{
            textViewGroup.setVisibility(View.GONE);
            userClicked = (User) getIntent().getSerializableExtra("UserClicked");
            if(userClicked.getPhoto() != null){
                Uri url = Uri.parse(userClicked.getPhoto());
                if(url == null) {
                    circleImageView.setImageResource(R.drawable.padrao);
                }
                else {
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageView);
                }
            }
            else{
                circleImageView.setImageResource(R.drawable.padrao);

            }

            textView.setText(userClicked.getName());

            databaseReferenceMessages = ConfigFirebase.getDatabaseReference()
                    .child("Messages")
                    .child(Base64Custom.encodeBase64(currentUser.getPhoneNumber()))
                    .child(Base64Custom.encodeBase64(userClicked.getPhoneNumber()));
        }


        floatingActionButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //Config adapter
        messagesAdapter = new MessagesAdapter(listMessages, getApplicationContext());
        //Config recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
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
        String idClicked;
        if(userClicked != null) {
            idClicked = Base64Custom.encodeBase64(userClicked.getPhoneNumber());
            if(!message.isEmpty()) {
                Message msg = new Message();
                msg.setHour(DateUtil.currentDate());
                msg.setIdUser(Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
                msg.setMessage(message);
                msg.setName("");
                msg.save(idClicked);
                msg.arrive(idClicked);
                sendNotification();
                editTextSend.setText("");
                saveChat(msg);
            }
            else {
                Toast.makeText(this, "Please write your message", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            idClicked = groupClicked.getId();
            if(!message.isEmpty()) {
                Message msg = new Message();
                msg.setHour(DateUtil.currentDate());
                msg.setMessage(message);
                msg.setName(currentUser.getName());
                sendNotification();
                editTextSend.setText("");
                for(User user : groupClicked.getMembers()) {
                    msg.setIdUser(Base64Custom.encodeBase64(user.getPhoneNumber()));
                    msg.save(idClicked, Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
                    saveChat(msg);
                }
            }
            else {
                Toast.makeText(this, "Please write your message", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void takeMessages() {
        valueEventListener = databaseReferenceMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    if(!aux.contains(data.getKey())) {
                        aux.add(data.getKey());
                        Message message = data.getValue(Message.class);
                        listMessages.add(message);
                    }
                }
                messagesAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(listMessages.size() - 1);

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

    private void sendNotification() {
        Notification notification;
        if (userClicked != null) {
            if (!editTextSend.getText().toString().isEmpty()) {
                notification  = new Notification(currentUser.getName(), editTextSend.getText().toString());
            } else {
                notification = new Notification(currentUser.getName(), "Photo");
            }
            //IF i need to send a topic
//            String to = "/topics/nameTopic";
            String to = userClicked.getToken();
            DataNotification dataNotification = new DataNotification(to, notification);
            NotificationService service = retrofit.create(NotificationService.class);
            Call<DataNotification> call = service.saveNotification(dataNotification);
            call.enqueue(new Callback<DataNotification>() {
                @Override
                public void onResponse(Call<DataNotification> call, Response<DataNotification> response) {

                }

                @Override
                public void onFailure(Call<DataNotification> call, Throwable t) {

                }
            });
        } else {
            if (!editTextSend.getText().toString().isEmpty()) {
                notification  = new Notification(groupClicked.getName(), editTextSend.getText().toString());
            } else {
                notification = new Notification(groupClicked.getName(), "Photo");
            }
            for(User user : groupClicked.getMembers()) {
                if (!user.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
                    String to = user.getToken();
                    DataNotification dataNotification = new DataNotification(to, notification);
                    NotificationService service = retrofit.create(NotificationService.class);
                    Call<DataNotification> call = service.saveNotification(dataNotification);
                    call.enqueue(new Callback<DataNotification>() {
                        @Override
                        public void onResponse(Call<DataNotification> call, Response<DataNotification> response) {

                        }

                        @Override
                        public void onFailure(Call<DataNotification> call, Throwable t) {

                        }
                    });
                }
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReferenceMessages.removeEventListener(valueEventListener);
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
                    String id = Base64Custom.encodeBase64(currentUser.getPhoneNumber());
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
                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            String idClicked;
                            Message message = new Message();
                            message.setHour(DateUtil.currentDate());
                            message.setImage(url.toString());
                            if(userClicked != null) {
                                message.setIdUser(Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
                                idClicked = Base64Custom.encodeBase64(userClicked.getPhoneNumber());
                                message.setName("");
                                message.save(idClicked);
                                message.arrive(idClicked);
                                saveChat(message);
                                sendNotification();
                            }
                            else {
                                idClicked = groupClicked.getId();
                                message.setName(currentUser.getName());
                                for(User user : groupClicked.getMembers()) {
                                    message.setIdUser(Base64Custom.encodeBase64(user.getPhoneNumber()));
                                    message.save(idClicked, Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
                                    saveChat(message);
                                    sendNotification();
                                }
                            }

                        }
                    });
                }
            }
            catch (Exception e) {
                    e.printStackTrace();
            }
        }
    }

    private void saveChat(Message message) {
        Chat chatSend = new Chat();
        chatSend.setDate(message.getHour());
        chatSend.setLastMessage(message.getMessage());
        chatSend.setIdSend(Base64Custom.encodeBase64(currentUser.getPhoneNumber()));
        if(userClicked != null) {
            chatSend.setIdArrived(Base64Custom.encodeBase64(userClicked.getPhoneNumber()));
            chatSend.setUser(userClicked);
            chatSend.setIsGroup(false);
            chatSend.save(currentUser);
        }
        else{
            chatSend.setIsGroup(true);
            chatSend.setGroup(groupClicked);
            chatSend.setIdArrived(groupClicked.getId());
            for(User user: groupClicked.getMembers()) {
                chatSend.save(Base64Custom.encodeBase64(user.getPhoneNumber()));
            }
        }
    }
}
