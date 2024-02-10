package com.kmobile.anonymouschat.fragments;

import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.kmobile.anonymouschat.adapters.UserAdapter;
import com.kmobile.anonymouschat.databinding.FragmentUsersBinding;
import com.kmobile.anonymouschat.model.User;

import java.util.ArrayList;

public class UsersFragment extends Fragment {
    private FragmentUsersBinding _binding;
    private ArrayList<User> userList;
    private User user;
    private UserAdapter adapter;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private DocumentReference mRef;
    private User kullanici;
    private Query query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFireStore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        mRef = mFireStore.collection("kullanicilar").document(mUser.getUid());

        mRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            kullanici = documentSnapshot.toObject(User.class);

                            query = mFireStore.collection("kullanicilar");
                            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                    if (error != null) {
                                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    if (value != null) {
                                        userList.clear();

                                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                                            user = snapshot.toObject(User.class);

                                            assert user != null;

                                            if (!user.getUserID().equals(mUser.getUid())) {
                                                userList.add(user);
                                            }
                                        }

                                        if(_binding.frUsersRv.getItemDecorationCount() > 0){
                                            _binding.frUsersRv.removeItemDecorationAt(0);
                                        }

                                        _binding.frUsersRv.addItemDecoration(new LinearDecoration(20, userList.size()));
                                        adapter = new UserAdapter(userList, _binding.getRoot().getContext(), kullanici.getUserID()
                                                ,kullanici.getUserName(), kullanici.getUserImage());

                                        _binding.frUsersRv.setAdapter(adapter);
                                    }

                                }
                            });
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _binding = FragmentUsersBinding.inflate(getLayoutInflater());

        _binding.frUsersRv.setHasFixedSize(true);
        _binding.frUsersRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        return _binding.getRoot();
    }

    private class LinearDecoration extends RecyclerView.ItemDecoration {
        private int boslukMiktari;
        private int veriSayisi;

        public LinearDecoration(int boslukMiktari, int veriSayisi) {
            this.boslukMiktari = boslukMiktari;
            this.veriSayisi = veriSayisi;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (position != (veriSayisi - 1)) {
                outRect.bottom = boslukMiktari;
            }
        }
    }
}