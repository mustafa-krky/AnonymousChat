package com.kmobile.anonymouschat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding _binding;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ProgressDialog mProgress;
    private String txtEmail, txtPassword;

    private void init(){
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());
        init();

        if(mUser != null){
            startActivity(new Intent(SignInActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }

        _binding.btnSignIn.setOnClickListener(view -> {
            btnSignIn();
        });

        _binding.btnGoToRegister.setOnClickListener(view -> {
            startActivity(new Intent(SignInActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });
    }

    private void btnSignIn(){
        txtEmail = _binding.etEmail.getText().toString();
        txtPassword = _binding.etPassword.getText().toString();

        if(checkInput()){

            mProgress = new ProgressDialog(SignInActivity.this);
            mProgress.setTitle("Giriş yapılıyor...");
            mProgress.show();

            mAuth.signInWithEmailAndPassword(txtEmail,txtPassword)
                    .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                setProgress();

                                startActivity(new Intent(SignInActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                finish();
                            }else {
                                setProgress();
                                Snackbar.make(_binding.signInLinear,task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean checkInput(){
        if(TextUtils.isEmpty(txtEmail)){
            _binding.tilEmail.setError("geçerli bir e-mail adresi giriniz");
            if(TextUtils.isEmpty(txtPassword)){
                _binding.tilPassword.setError("şifrenizi giriniz");
                return false;
            }
            return false;
        }
        return true;
    }

    private void setProgress(){
        if(mProgress.isShowing()){
            mProgress.dismiss();
        }
    }
}