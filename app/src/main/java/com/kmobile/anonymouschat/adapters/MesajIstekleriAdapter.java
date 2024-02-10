package com.kmobile.anonymouschat.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.model.MessageRequest;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MesajIstekleriAdapter extends RecyclerView.Adapter<MesajIstekleriAdapter.MesajIstekleriViewHolder> {

    private ArrayList<MessageRequest> mesajIstegiList;
    private Context mContext;
    private MessageRequest mesajIstegi, yeniMesajIstegi;
    private int mPosition;
    private FirebaseFirestore mFireStore;
    private String mUID, mIsim, mProfilResmi;

    public MesajIstekleriAdapter(ArrayList<MessageRequest> mesajIstegiList, Context mContext,
                                 String mUID, String mIsim, String mProfilResmi) {
        this.mesajIstegiList = mesajIstegiList;
        this.mContext = mContext;
        this.mFireStore = FirebaseFirestore.getInstance();
        this.mUID = mUID;
        this.mIsim = mIsim;
        this.mProfilResmi = mProfilResmi;
    }

    @NonNull
    @Override
    public MesajIstekleriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gelen_msj_istekleri_item,parent,false);
        return new MesajIstekleriViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MesajIstekleriViewHolder holder, int position) {
        mesajIstegi = mesajIstegiList.get(position);

        holder.txtMsg.setText(mesajIstegi.getUserName()+" kullanıcısı sana mesaj göndermek istiyor.");

        if(mesajIstegi.getUserProfileImage().equals("default")){
            holder.ivUserProfile.setImageResource(R.drawable.user);
        }else {
            Picasso.get().load(mesajIstegi.getUserProfileImage()).resize(77,77).into(holder.ivUserProfile);
        }

        holder.ivOnay.setOnClickListener(v -> {
            mPosition = holder.getAdapterPosition();

            if(mPosition != RecyclerView.NO_POSITION){

                yeniMesajIstegi = new MessageRequest(mesajIstegiList.get(mPosition).getKanalID(),
                        mesajIstegiList.get(mPosition).getUserID(), mesajIstegiList.get(mPosition).getUserName(),
                        mesajIstegiList.get(mPosition).getUserProfileImage());

                mFireStore.collection("kullanicilar").document(mUID)
                        .collection("kanal").document(mesajIstegiList.get(mPosition).getUserID())
                        .set(yeniMesajIstegi)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    yeniMesajIstegi = new MessageRequest(mesajIstegiList.get(mPosition).getKanalID(),
                                            mUID,mIsim,mProfilResmi);

                                    mFireStore.collection("kullanicilar").document(mesajIstegiList.get(mPosition).getUserID())
                                            .collection("kanal").document(mUID)
                                            .set(yeniMesajIstegi)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful()){
                                                        mesajIstegiSil(mesajIstegiList.get(mPosition).getUserID()
                                                        ,"Mesaj isteği kabul edildi.");
                                                    }else {
                                                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }

        });

        holder.ivIptal.setOnClickListener(v -> {
            mPosition = holder.getAdapterPosition();

            if(mPosition != RecyclerView.NO_POSITION){
                mesajIstegiSil(mesajIstegiList.get(mPosition).getUserID()
                        ,"Mesaj isteği silindi.");
            }
        });

    }

    @Override
    public int getItemCount() {
        return mesajIstegiList.size();
    }

    private void mesajIstegiSil(String hedefUID, String mesajIcerigi){
        mFireStore.collection("mesajistekleri").document(mUID).collection("istekler")
                .document(hedefUID)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            notifyDataSetChanged();
                            Toast.makeText(mContext, mesajIcerigi, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public class MesajIstekleriViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView ivUserProfile;
        private TextView txtMsg;
        private ImageView ivOnay, ivIptal;

        public MesajIstekleriViewHolder(View view){
            super(view);
            ivUserProfile = view.findViewById(R.id.cstm_dialog_gelen_msj_istek_item_imgProfile);
            txtMsg = view.findViewById(R.id.gelen_mesaj_istekleri_txtMsj);
            ivOnay = view.findViewById(R.id.gelen_mesaj_istekleri_onayla);
            ivIptal = view.findViewById(R.id.gelen_mesaj_istekleri_iptal);
        }
    }
}
