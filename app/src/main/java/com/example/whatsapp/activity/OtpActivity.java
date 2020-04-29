package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfigFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String mAuthVerificationId;
    private String mPhoneNumber;
    private String mCountryCode;

    private EditText mOtpText;
    private Button mVerifyBtn;

    private ProgressBar mOtpProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mAuth = ConfigFirebase.getFirebaseAuth();

        mAuthVerificationId = getIntent().getStringExtra("AuthCredentials");
        mPhoneNumber        = getIntent().getStringExtra("PhoneNumber");
        mCountryCode        = getIntent().getStringExtra("CountryCode");

        mOtpProgress = findViewById(R.id.otp_progress_bar);
        mOtpText = findViewById(R.id.otp_text_view);

        mVerifyBtn = findViewById(R.id.verify_otp);

        mOtpProgress.setVisibility(View.INVISIBLE);
        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = mOtpText.getText().toString();

                if(otp.isEmpty()){
                    Toast.makeText(OtpActivity.this, "Please fill in the form and try again.", Toast.LENGTH_SHORT).show();
                } else {

                    mOtpProgress.setVisibility(View.VISIBLE);
                    mVerifyBtn.setEnabled(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mAuthVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToGetData();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(OtpActivity.this, "There was an error verifying OTP", Toast.LENGTH_SHORT).show();
                            }
                        }
                        mOtpProgress.setVisibility(View.INVISIBLE);
                        mVerifyBtn.setEnabled(true);
                    }
                });
    }

    public void sendUserToGetData() {
        Intent homeIntent = new Intent(OtpActivity.this, GetDataUserActivity.class);
        homeIntent.putExtra("PhoneNumber", mPhoneNumber);
        homeIntent.putExtra("CountryCode", mCountryCode);
        startActivity(homeIntent);
        finish();
    }
}
