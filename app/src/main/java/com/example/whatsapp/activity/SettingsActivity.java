package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.Permission;
import com.example.whatsapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private String[] mPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private ImageButton imageButtonCamera, imageButtonGalery;
    private static final int SELECTION_CAMERA = 100;
    private static final int SELECTION_GALERY = 200;
    private CircleImageView perfil;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private EditText editTextPerfilName, editTextPerfilPhone, editTextPerfilStatus;
    private User currentUser;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private ProgressBar progressBarPerfil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageButtonCamera     = findViewById(R.id.imageButtonCamera);
        imageButtonGalery     = findViewById(R.id.imageButtonGalery);
        perfil                = findViewById(R.id.profileCircleImage);
        storageReference      = ConfigFirebase.getFirebaseStorage();
        firebaseUser          = CurrentUserFirebase.getCurrentUser();
        editTextPerfilName    = findViewById(R.id.editTextPerfilName);
        editTextPerfilPhone   = findViewById(R.id.editTextPerfilPhone);
        editTextPerfilStatus  = findViewById(R.id.editTextPerfilStatus);
        currentUser           = CurrentUserFirebase.getUser();
        progressBarPerfil     = findViewById(R.id.progressBarPerfil);
        progressBarPerfil.setVisibility(View.INVISIBLE);


        editTextPerfilPhone.setText(firebaseUser.getPhoneNumber());
        editTextPerfilName.setText(firebaseUser.getDisplayName());

        Uri url = firebaseUser.getPhotoUrl();
        if(url == null) {
            perfil.setImageResource(R.drawable.padrao);
        }
        else {
            Glide.with(SettingsActivity.this)
                    .load(url)
                    .into(perfil);
        }

        //Validation of permission
        Permission.permissionValidation(mPermissions, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECTION_CAMERA);
                }
            }
        });

        imageButtonGalery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECTION_GALERY);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        progressBarPerfil.setVisibility(View.VISIBLE);
        databaseReference  = ConfigFirebase.getDatabaseReference().child("Users").child(CurrentUserFirebase.getIdCurrentUser());
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User newUser = dataSnapshot.getValue(User.class);
                currentUser.setStatus(newUser.getStatus());
                editTextPerfilStatus.setText(currentUser.getStatus());
                progressBarPerfil.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
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
                    case SELECTION_CAMERA:
                        image = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if (image != null) {
                    perfil.setImageBitmap(image);
                    String id = CurrentUserFirebase.getIdCurrentUser();

                    //Take data to firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();

                    //Save image in firebase
                    StorageReference imageRef = storageReference
                            .child("images")
                            .child("perfil")
                            .child(id)
                            .child("perfil.jpeg");

                    UploadTask uploadTask = imageRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsActivity.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(SettingsActivity.this, "Success to upload the image", Toast.LENGTH_SHORT).show();

                            Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                            while(!uri.isComplete());
                            Uri url = uri.getResult();
                            updatePhoto(url);
                        }
                    });
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void updatePhoto(Uri url) {
        if(CurrentUserFirebase.updatePhoto(url)) {
            currentUser.setPhoto(url.toString());
            currentUser.update();
            Toast.makeText(this, "Your profile photo has changed", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "We were unable to update your profile photo", Toast.LENGTH_SHORT).show();
        }
        
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int resultPermission : grantResults) {
            if(resultPermission == PackageManager.PERMISSION_DENIED) {
                alertValidation();
            }
        }
    }

    private void alertValidation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions Denied");
        builder.setMessage("To use the app you need to accept the permissions");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    public void changeName(View v) {
        String name = editTextPerfilName.getText().toString();
        if(name.isEmpty()) {
            Toast.makeText(this, "Please fill in your name", Toast.LENGTH_SHORT).show();
        }
        else {
            if(name.equals(currentUser.getName())) {
                Toast.makeText(this, "Please enter another name", Toast.LENGTH_SHORT).show();
            }
            else {
                CurrentUserFirebase.updateName(name);
                editTextPerfilName.setText(name);
                currentUser.setName(name);
                currentUser.update();
                Toast.makeText(this, "Name changed", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public void changeStatus(View v) {
        String status = editTextPerfilStatus.getText().toString();
        if(status.isEmpty()) {
            Toast.makeText(this, "Please fill in your status", Toast.LENGTH_SHORT).show();
        }
        else{
            if(status.equals(currentUser.getStatus())) {
                Toast.makeText(this, "Please enter another status", Toast.LENGTH_SHORT).show();
            }
            else {
                editTextPerfilStatus.setText(status);
                currentUser.setStatus(status);
                currentUser.update();
                Toast.makeText(this, "status changed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendToProfilePhoto(View v) {
        Intent intent = new Intent(SettingsActivity.this, PerfilPhotoActivity.class);
        intent.putExtra("user", currentUser);
        startActivity(intent);
    }
}
