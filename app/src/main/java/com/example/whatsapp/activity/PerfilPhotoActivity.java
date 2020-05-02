package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class PerfilPhotoActivity extends AppCompatActivity {

    private ImageView imageViewProfilePhoto;
    private FirebaseUser firebaseUser;
    private static final int SELECTION_CAMERA = 100;
    private static final int SELECTION_GALERY = 200;
    private User currentUser;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_photo);

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        toolbar.setTitle("Profile Photo");
        toolbar.setBackgroundColor(Color.BLACK);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseUser          = CurrentUserFirebase.getCurrentUser();
        imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto);
        currentUser           = (User) getIntent().getSerializableExtra("user");
        storageReference      = ConfigFirebase.getFirebaseStorage();


        Uri url = firebaseUser.getPhotoUrl();
        if(url == null) {
            imageViewProfilePhoto.setImageResource(R.drawable.padrao);
        }
        else {
            Glide.with(PerfilPhotoActivity.this)
                    .load(url)
                    .into(imageViewProfilePhoto);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_photo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case R.id.menuChangeCamera:
                i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECTION_CAMERA);
                }
                break;
            case R.id.menuChangeGalery:
                i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(i, SELECTION_GALERY);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
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
                    imageViewProfilePhoto.setImageBitmap(image);
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
                            Toast.makeText(PerfilPhotoActivity.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
                        }
                    });
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(PerfilPhotoActivity.this, "Success to upload the image", Toast.LENGTH_SHORT).show();

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
}
