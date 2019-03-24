package com.example.eventer2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventer2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText mLoginEmailText;
    private EditText mLoginPasswordText;
    private TextView mSignUpView;
    private Button mLoginButton;
    private ProgressBar mLoginProgress;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        mSignUpView.setOnClickListener(v -> {
            Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(regIntent);
            finish();
        });

        mLoginButton.setOnClickListener(v -> {
            String loginEmail = mLoginEmailText.getText().toString();
            String loginPassword = mLoginPasswordText.getText().toString();

            if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPassword)){
                mLoginProgress.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        sendToMain();
                    }else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                    mLoginProgress.setVisibility(View.INVISIBLE);
                });
            }
        });

    }

    private void init(){
        mLoginEmailText = findViewById(R.id.login_email);
        mLoginPasswordText = findViewById(R.id.login_password);
        mSignUpView = findViewById(R.id.login_signup);
        mLoginButton = findViewById(R.id.login_btn);
        mLoginProgress = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToMain(){
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
