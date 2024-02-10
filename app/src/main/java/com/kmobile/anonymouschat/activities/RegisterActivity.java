package com.kmobile.anonymouschat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kmobile.anonymouschat.databinding.ActivityRegisterBinding;
import com.kmobile.anonymouschat.model.User;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding _binding;
    private User user;
    private FirebaseFirestore mFireStore;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private ProgressDialog progressDialog;
    private String txtName, txtEmail, txtPassword, txtAgainPassword;

    private void init(){
        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        init();

        _binding.btnRegister.setOnClickListener(view -> {
            btnRegister();
        });
    }

    private void btnRegister() {
        txtName = _binding.etUsername.getText().toString();
        txtEmail = _binding.etEmail.getText().toString();
        txtPassword = _binding.etPassword.getText().toString();
        txtAgainPassword = _binding.etAgainPassword.getText().toString();

        if(checkInput()){

            if(!txtPassword.equals(txtAgainPassword)){
                _binding.tilAgainPassword.setError("şifreler aynı olmalı");
            }else{

                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setTitle("Kayıt olunuyor...");
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(txtEmail,txtPassword)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    mUser = mAuth.getCurrentUser();
                                    if(mUser != null){
                                        user = new User(mUser.getUid(),txtName,txtEmail,"default",false);

                                        mFireStore.collection("kullanicilar").document(mUser.getUid())
                                                .set(user)
                                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            setProgress();
                                                            Toast.makeText(RegisterActivity.this, "Kayıt başarılı", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                            finish();
                                                        }else {
                                                            setProgress();
                                                            Snackbar.make(_binding.registerLinear,task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }else {
                                    setProgress();
                                    Snackbar.make(_binding.registerLinear,task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    private boolean checkInput() {

        if(TextUtils.isEmpty(txtName)){
            _binding.tilName.setError("isim giriniz");
            return false;
        }else if(TextUtils.isEmpty(txtEmail)){
            _binding.tilEmail.setError("e-mail giriniz");
            return false;
        }else if(TextUtils.isEmpty(txtPassword)){
            _binding.tilPassword.setError("şifre giriniz");
            return false;
        }else if(TextUtils.isEmpty(txtAgainPassword)){
            _binding.tilAgainPassword.setError("şifre giriniz");
            return false;
        }

        return true;
    }
    private void setProgress(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}