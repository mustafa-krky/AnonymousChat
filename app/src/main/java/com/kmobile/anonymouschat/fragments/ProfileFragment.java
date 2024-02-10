package com.kmobile.anonymouschat.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.databinding.FragmentProfileBinding;
import com.kmobile.anonymouschat.model.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ProfileFragment extends Fragment {
    private final int IZIN_KODU = 0;
    private final int IZIN_ALINDI_KODU = 1;
    private FragmentProfileBinding binding;
    private FirebaseFirestore mFireStore;
    private StorageReference mStorageRef, yeniStorageRef, sRef;
    private DocumentReference mRef;
    private FirebaseUser mUser;
    private User kullanici;
    private Intent galeriIntent;
    private Uri mUri;
    private Bitmap gelenResim;
    private ImageDecoder.Source imgSource;
    private ByteArrayOutputStream outputStream;
    private byte[] imgByte;
    private String kayitYeri, indirmeLinki;
    private HashMap<String, Object> mData;

    private Query mQuery;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());

        mFireStore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mRef = mFireStore.collection("kullanicilar").document(mUser.getUid());
        mRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null && value.exists()) {
                    kullanici = value.toObject(User.class);

                    if (kullanici != null) {
                        binding.etProfileFragmentIsim.setText(kullanici.getUserName());
                        binding.etProfileFragmentEmail.setText(kullanici.getEmail());

                        if (kullanici.getUserImage().equals("default")) {
                            binding.civProfilFragment.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Picasso.get().load(kullanici.getUserImage()).resize(156, 156).into(binding.civProfilFragment);
                        }
                    }
                }
            }
        });

        binding.profileFragmentYeniresim.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                        , IZIN_KODU);

            } else {
                galeriyeGit();
            }
        });

        return binding.getRoot();
    }

    private void galeriyeGit() {
        galeriIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galeriIntent, IZIN_ALINDI_KODU);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == IZIN_KODU) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galeriyeGit();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IZIN_ALINDI_KODU) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                mUri = data.getData();

                try {

                    if (Build.VERSION.SDK_INT >= 28) {

                        imgSource = ImageDecoder.createSource(requireContext().getContentResolver(), mUri);
                        gelenResim = ImageDecoder.decodeBitmap(imgSource);

                    } else {
                        gelenResim = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), mUri);
                    }

                    outputStream = new ByteArrayOutputStream();
                    gelenResim.compress(Bitmap.CompressFormat.PNG, 75, outputStream);

                    imgByte = outputStream.toByteArray();

                    kayitYeri = "kullanicilar/" + kullanici.getEmail() + "/profile.png";
                    sRef = mStorageRef.child(kayitYeri);

                    sRef.putBytes(imgByte)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    yeniStorageRef = FirebaseStorage.getInstance().getReference(kayitYeri);

                                    yeniStorageRef.getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    indirmeLinki = uri.toString();

                                                    mData = new HashMap<>();
                                                    mData.put("userImage", indirmeLinki);

                                                    mFireStore.collection("kullanicilar")
                                                            .document(mUser.getUid())
                                                            .update(mData)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        iletisimIcinProfilGuncelle(indirmeLinki);
                                                                    } else {
                                                                        Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(requireContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } catch (IOException e) {
                    Toast.makeText(requireContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void iletisimIcinProfilGuncelle(final String link) {
        mQuery = mFireStore.collection("kullanicilar").document(mUser.getUid()).collection("kanal");

        mQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.getDocuments().size() > 0) {
                        for (DocumentSnapshot snp : queryDocumentSnapshots.getDocuments()) {

                            mData = new HashMap<>();
                            mData.put("userProfileImage", link);

                            mFireStore.collection("kullanicilar").document(snp.getData().get("userID").toString())
                                    .collection("kanal").document(mUser.getUid())
                                    .update(mData);

                        }

                        Toast.makeText(requireContext(), "Profil resmi güncellendi", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(binding.getRoot().getContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show());
    }
}