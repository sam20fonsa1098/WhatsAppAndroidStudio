package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.R;
import com.example.whatsapp.adapter.GroupSelectedAdapter;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.model.Group;
import com.example.whatsapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterNewGroupActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private List<User> userList = new ArrayList<>();
    private TextView textView;
    private RecyclerView recyclerView;
    private GroupSelectedAdapter groupSelectedAdapter;
    private CircleImageView circleImageView;
    private static final int SELECTION_GALERY = 200;
    private StorageReference storageReference;
    private Group group;
    private FloatingActionButton floatingActionButton;
    private EditText editText;
    private ProgressBar progressBar;
    private boolean canCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_group);

        canCreate            = true;
        progressBar          = findViewById(R.id.progressBarRegisterNewGroup);
        editText             = findViewById(R.id.editTextRegisterNewGroup);
        floatingActionButton = findViewById(R.id.floatingActionButtonRegisterNewGroup);
        circleImageView      = findViewById(R.id.circleImageViewNewGroup);
        recyclerView         = findViewById(R.id.recyclerViewRegisterNewGroup);
        textView             = findViewById(R.id.textViewRegisterNewGroupSize);
        toolbar              = findViewById(R.id.toolbarMain);
        storageReference     = ConfigFirebase.getFirebaseStorage();
        group                = new Group();
        toolbar.setTitle("New Group");
        toolbar.setSubtitle("Add subject");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.INVISIBLE);

        if(getIntent().getExtras() != null) {
            userList.addAll((List<User>) getIntent().getExtras().getSerializable("members"));
            textView.setText("Participants: " + userList.size());
        }

        groupSelectedAdapter = new GroupSelectedAdapter(userList, getApplicationContext());
        RecyclerView.LayoutManager layoutManager1 = new GridLayoutManager(getApplicationContext(),4);
        recyclerView.setLayoutManager(layoutManager1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(groupSelectedAdapter);

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, SELECTION_GALERY);
                }
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                group.setName(editText.getText().toString());
                if(!userList.contains(CurrentUserFirebase.getUser())) {
                    userList.add(CurrentUserFirebase.getUser());
                }
                group.setMembers(userList);
                while (!canCreate) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.INVISIBLE);
                group.save();
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("GroupClicked", group);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Bitmap image = null;

            try {
                switch (requestCode) {
                    case SELECTION_GALERY:
                        Uri localImage = data.getData();
                        image          = MediaStore.Images.Media.getBitmap(getContentResolver(), localImage);
                        break;
                }
                if (image != null) {
                    circleImageView.setImageBitmap(image);
                    String id = CurrentUserFirebase.getIdCurrentUser();

                    //Take data to firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    //Save image in firebase
                    StorageReference imageRef = storageReference
                            .child("images")
                            .child("groups")
                            .child(group.getId())
                            .child("perfil.jpeg");

                    progressBar.setVisibility(View.VISIBLE);
                    canCreate = false;
                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegisterNewGroupActivity.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(RegisterNewGroupActivity.this, "Success to upload the image", Toast.LENGTH_SHORT).show();

                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            group.setPhoto(url.toString());
                            progressBar.setVisibility(View.INVISIBLE);
                            canCreate = true;
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
