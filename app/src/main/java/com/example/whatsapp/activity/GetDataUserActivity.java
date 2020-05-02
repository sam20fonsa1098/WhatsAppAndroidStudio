package com.example.whatsapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.model.User;

public class GetDataUserActivity extends AppCompatActivity {

    private String mPhoneNumber;
    private String mCountryCode;
    private EditText mUsername;
    private Button mConfirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data_user);

        mPhoneNumber  = getIntent().getStringExtra("PhoneNumber");
        mUsername     = findViewById(R.id.editTextUsername);
        mConfirm      = findViewById(R.id.buttonConfirmData);
        mCountryCode  = getIntent().getStringExtra("CountryCode");


        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mUsername.getText().toString();
                if(!name.isEmpty()) {
                    try{
                        String id = Base64Custom.encodeBase64(mCountryCode + mPhoneNumber);
                        User user = new User();
                        user.setName(mUsername.getText().toString());
                        user.setPhoneNumber(mCountryCode + mPhoneNumber);
                        user.setUserId(id);
                        user.setStatus(getString(R.string.defaulStatus));
                        user.save();
                        CurrentUserFirebase.updateName(user.getName());
                        Toast.makeText(GetDataUserActivity.this, "User saved successfully", Toast.LENGTH_SHORT).show();
                        sendUserToHome();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(GetDataUserActivity.this, "Please fill in your Username", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void sendUserToHome() {
        Intent homeIntent = new Intent(GetDataUserActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}
