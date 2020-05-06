package com.example.whatsapp.activity;

import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private User userClicked;
    private CircleImageView circleImageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userClicked     = (User) getIntent().getSerializableExtra("UserClicked");
        circleImageView = findViewById(R.id.circleImageChat);
        textView        = findViewById(R.id.textViewChatName);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
