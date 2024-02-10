package com.kmobile.anonymouschat.activities;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.adapters.MesajIstekleriAdapter;
import com.kmobile.anonymouschat.databinding.ActivityMainBinding;
import com.kmobile.anonymouschat.fragments.MessagesFragment;
import com.kmobile.anonymouschat.fragments.ProfileFragment;
import com.kmobile.anonymouschat.fragments.UsersFragment;
import com.kmobile.anonymouschat.model.MessageRequest;
import com.kmobile.anonymouschat.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private HashMap<String,Object> mData;
    private ActivityMainBinding _binding;
    private UsersFragment usersFragment;
    private MessagesFragment messagesFragment;
    private ProfileFragment profileFragment;
    private FragmentTransaction transaction;
    private Toolbar mToolbar;
    private RelativeLayout relativeNotif;
    private TextView txtBildirimSayisi;
    private FirebaseFirestore mFireStore;
    private FirebaseUser mUser;
    private Query mQuery;
    private MessageRequest messageRequest;
    private ArrayList<MessageRequest> msgRequestList;
    private Dialog mesajIstekleriDialog;
    private ImageView msjIstekleriKapat;
    private RecyclerView msjIstekleriRV;
    private MesajIstekleriAdapter mAdapter;
    private DocumentReference mRef;
    private User kullanici;

    private void init() {
        mFireStore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mRef = mFireStore.collection("kullanicilar").document(mUser.getUid());

        usersFragment = new UsersFragment();
        profileFragment = new ProfileFragment();
        messagesFragment = new MessagesFragment();

        msgRequestList = new ArrayList<>();

        relativeNotif = findViewById(R.id.bar_relative);
        txtBildirimSayisi = findViewById(R.id.txt_bildirim_sayisi);

        changeFragment(usersFragment);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        init();

        mQuery = mFireStore.collection("mesajistekleri").document(mUser.getUid()).collection("istekler");
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    txtBildirimSayisi.setText(String.valueOf(value.getDocuments().size()));
                    msgRequestList.clear();

                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        messageRequest = snapshot.toObject(MessageRequest.class);
                        msgRequestList.add(messageRequest);
                    }
                }
            }
        });

        relativeNotif.setOnClickListener(view -> {
            mRef.get()
                    .addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                kullanici = documentSnapshot.toObject(User.class);

                                showMsjIstekleriDialog(kullanici);
                            }
                        }
                    });
        });

        _binding.mainActivityBottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.bottom_nav_people) {
                relativeNotif.setVisibility(View.GONE);
                changeFragment(usersFragment);
            } else if (item.getItemId() == R.id.bottom_nav_message) {
                relativeNotif.setVisibility(View.VISIBLE);
                changeFragment(messagesFragment);
            } else {
                relativeNotif.setVisibility(View.GONE);
                changeFragment(profileFragment);
            }

            return true;
        });
    }

    private void changeFragment(Fragment fragment) {
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame_layout, fragment);
        transaction.commit();
    }

    private void showMsjIstekleriDialog(User mykullanici) {
        mesajIstekleriDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        mesajIstekleriDialog.setContentView(R.layout.cstm_dialog_gelen_mesaj_istekleri);

        msjIstekleriKapat = mesajIstekleriDialog.findViewById(R.id.cstm_dialog_gelen_msj_istek_kapat);
        msjIstekleriRV = mesajIstekleriDialog.findViewById(R.id.cstm_dialog_gelen_msj_istek_rv);

        msjIstekleriKapat.setOnClickListener(view -> {
            mesajIstekleriDialog.dismiss();
        });

        msjIstekleriRV.setHasFixedSize(true);
        msjIstekleriRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mAdapter = new MesajIstekleriAdapter(msgRequestList, this
                , mykullanici.getUserID(), mykullanici.getUserName(), mykullanici.getUserImage());
        msjIstekleriRV.setAdapter(mAdapter);

        mesajIstekleriDialog.show();
    }

    private void kullaniciSetOnline(boolean b){
        mData = new HashMap<>();
        mData.put("userOnline",b);

        mFireStore.collection("kullanicilar").document(mUser.getUid())
                .update(mData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        kullaniciSetOnline(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        kullaniciSetOnline(false);
    }
}