package com.kmobile.anonymouschat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.adapters.ChatAdapter;
import com.kmobile.anonymouschat.databinding.ActivityChatBinding;
import com.kmobile.anonymouschat.model.Chat;
import com.kmobile.anonymouschat.model.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private final int IZIN_KODU = 0;
    private final int IZIN_ALINDI_KODU = 1;
    private ProgressDialog mProgress;
    private Intent galeriIntenti;
    private Uri imgUri;
    private String kayitYeri, indirmeLinki;
    private Bitmap imgBitmat;
    private ImageDecoder.Source imgSource;
    private ByteArrayOutputStream outputStream;
    private byte[] imgByte;
    private StorageReference mStorageRef, yeniRef, sRef;
    private FirebaseUser mUser;
    private HashMap<String,Object> mData;
    private ActivityChatBinding binding;
    private Intent gelenIntent;
    private String hedefID, kanalID, hedefProfilURL;
    private User hedefKullanici;
    private DocumentReference hedefRef;
    private FirebaseFirestore mFireStore;
    private ArrayList<Chat> chatList;
    private Chat mChat;
    private Query chatQuery;
    private ChatAdapter chatAdapter;
    private String txtMesaj, docId;
    private LinearLayoutManager mManager;

    private void init(){
        mFireStore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        gelenIntent = getIntent();
        hedefID = gelenIntent.getStringExtra("hedefId");
        kanalID = gelenIntent.getStringExtra("kanalId");
        hedefProfilURL = gelenIntent.getStringExtra("hedefProfilResim");

        mProgress = new ProgressDialog(this);

        chatList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        hedefRef = mFireStore.collection("kullanicilar").document(hedefID);
        hedefRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if(error != null){
                    Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(value != null && value.exists()){
                    hedefKullanici = value.toObject(User.class);

                    if(hedefKullanici != null){
                        binding.tvChatHedefIsim.setText(hedefKullanici.getUserName());

                        if(hedefKullanici.getUserImage().equals("default")){
                            binding.civChatHedefProfil.setImageResource(R.mipmap.ic_launcher);
                        }else {
                            Picasso.get().load(hedefKullanici.getUserImage()).resize(76,76).into(binding.civChatHedefProfil);
                        }
                    }
                }
            }
        });

        binding.chatRecycler.setHasFixedSize(true);
        mManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mManager.setStackFromEnd(true);
        binding.chatRecycler.setLayoutManager(mManager);

        chatQuery = mFireStore.collection("chatkanallari").document(kanalID).collection("mesajlar")
                        .orderBy("mesajTarihi",Query.Direction.ASCENDING);

        chatQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(ChatActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(value != null){
                    chatList.clear();

                    for(DocumentSnapshot snapshot: value.getDocuments()){
                        mChat = snapshot.toObject(Chat.class);

                        if(mChat != null){
                            chatList.add(mChat);
                        }
                    }

                    chatAdapter = new ChatAdapter(chatList,ChatActivity.this, mUser.getUid(), hedefProfilURL);
                    binding.chatRecycler.setAdapter(chatAdapter);
                }
            }
        });


        binding.ivChatMesajGonder.setOnClickListener(v -> {
            txtMesaj = binding.etChatMesaj.getText().toString();

            if(!TextUtils.isEmpty(txtMesaj)){
                mesajGonder(txtMesaj,"text");
            }else {

            }
        });

        binding.chatActivityResimGonder.setOnClickListener(v -> {
            btnGaleridenResimGonder();
        });

        binding.ivChatKapat.setOnClickListener(v -> {
            finish();
        });
    }

    private void btnGaleridenResimGonder(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},IZIN_KODU);
        }else {
            galeriIntent();
        }
    }

    private void galeriIntent(){
        galeriIntenti = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galeriIntenti,IZIN_ALINDI_KODU);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == IZIN_KODU){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                galeriIntent();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == IZIN_ALINDI_KODU){
            if(resultCode == RESULT_OK && data != null && data.getData() != null){
                imgUri = data.getData();

                try {
                    if(Build.VERSION.SDK_INT >= 28){
                        imgSource = ImageDecoder.createSource(this.getContentResolver(),imgUri);
                        imgBitmat = ImageDecoder.decodeBitmap(imgSource);
                    }else {
                        imgBitmat = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imgUri);
                    }

                    outputStream = new ByteArrayOutputStream();
                    imgBitmat.compress(Bitmap.CompressFormat.PNG,75,outputStream);
                    imgByte = outputStream.toByteArray();

                    kayitYeri = "chatYuklenenler/"+kanalID+"/"+mUser.getEmail()+"/"+System.currentTimeMillis()+".png";
                    sRef = mStorageRef.child(kayitYeri);

                    sRef.putBytes(imgByte)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mProgress.setTitle("Resim Gönderiliyor...");
                                    mProgress.show();

                                    yeniRef = FirebaseStorage.getInstance().getReference(kayitYeri);
                                    yeniRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    indirmeLinki = uri.toString();

                                                    mesajGonder(indirmeLinki,"resim");
                                                }
                                            });
                                }
                            });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void mesajGonder(String txtMesaj, String mesajTipi){
        docId = UUID.randomUUID().toString();

        mData = new HashMap<>();
        mData.put("mesajIcerigi",txtMesaj);
        mData.put("gonderen",mUser.getUid());
        mData.put("alici",hedefID);
        mData.put("mesajTipi",mesajTipi);
        mData.put("mesajTarihi", FieldValue.serverTimestamp());
        mData.put("docId",docId);

        mFireStore.collection("chatkanallari").document(kanalID).collection("mesajlar")
                .document(docId)
                .set(mData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            binding.etChatMesaj.setText("");
                            progressAyar();
                        }else {
                            Toast.makeText(ChatActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void progressAyar(){
        if(mProgress.isShowing())
            mProgress.dismiss();
    }
}