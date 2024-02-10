package com.kmobile.anonymouschat.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.adapters.MesajlarAdapter;
import com.kmobile.anonymouschat.databinding.FragmentMessagesBinding;
import com.kmobile.anonymouschat.model.MessageRequest;

import java.util.ArrayList;

public class MessagesFragment extends Fragment {
    private FragmentMessagesBinding binding;
    private FirebaseFirestore mFireStore;
    private FirebaseUser mUser;
    private Query mQuery;
    private Query sonMsgQuery;
    private ArrayList<MessageRequest> mList;
    private ArrayList<String> sonMesajList;
    private MesajlarAdapter adapter;
    private MessageRequest mesajIstegi;

    private int sonMsgIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(getLayoutInflater());

        mFireStore = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mList = new ArrayList<>();
        sonMesajList = new ArrayList<>();

        binding.rvMesajlarFragment.setHasFixedSize(true);
        binding.rvMesajlarFragment.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false));

        mQuery = mFireStore.collection("kullanicilar").document(mUser.getUid()).collection("kanal");

        mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null){
                    mList.clear();

                    sonMsgIndex = 0;

                    for(DocumentSnapshot snapshot: value.getDocuments()){
                        mesajIstegi = snapshot.toObject(MessageRequest.class);

                        if(mesajIstegi != null){
                            mList.add(mesajIstegi);

                            sonMsgQuery = mFireStore.collection("chatkanallari").document(mesajIstegi.getKanalID())
                                    .collection("mesajlar").orderBy("mesajTarihi",Query.Direction.DESCENDING)
                                    .limit(1);

                            sonMsgQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value2, @Nullable FirebaseFirestoreException error) {
                                    if(error == null && value2 != null){
                                        sonMesajList.clear();

                                        for(DocumentSnapshot snp: value2.getDocuments()){
                                            sonMesajList.add(snp.getData().get("mesajIcerigi").toString());

                                            sonMsgIndex++;

                                            if(sonMsgIndex == value.getDocuments().size()){
                                                adapter = new MesajlarAdapter(mList,sonMesajList,requireContext());
                                                binding.rvMesajlarFragment.setAdapter(adapter);
                                                sonMsgIndex = 0;
                                            }
                                        }
                                    }
                                }
                            });
                        }

                    }


                }
            }
        });

        return binding.getRoot();
    }
}