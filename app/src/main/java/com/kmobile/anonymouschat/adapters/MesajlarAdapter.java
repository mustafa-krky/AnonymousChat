package com.kmobile.anonymouschat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.activities.ChatActivity;
import com.kmobile.anonymouschat.model.MessageRequest;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MesajlarAdapter extends RecyclerView.Adapter<MesajlarAdapter.MesajlarHolder> {
    private ArrayList<MessageRequest> mList;
    private ArrayList<String> sonMsgList;
    private Context mContext;
    private MessageRequest mesajIstegi;
    private int kPos;
    private Intent chatIntent;

    public MesajlarAdapter(ArrayList<MessageRequest> mList, ArrayList<String> sonMsgList, Context mContext) {
        this.mList = mList;
        this.sonMsgList = sonMsgList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MesajlarHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.mesajlar_item,parent,false);
        return new MesajlarHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MesajlarHolder holder, int position) {
        mesajIstegi = mList.get(position);
        holder.kullaniciIsmi.setText(mesajIstegi.getUserName());
        holder.sonMesaj.setText(sonMsgList.get(position));

        if(mesajIstegi.getUserProfileImage().equals("default")){
            holder.civKullaniciProfil.setImageResource(R.mipmap.ic_launcher);
        }else {
            Picasso.get().load(mesajIstegi.getUserProfileImage()).resize(66,66).into(holder.civKullaniciProfil);
        }

        holder.itemView.setOnClickListener(view -> {
            kPos = holder.getAdapterPosition();

            if(kPos != RecyclerView.NO_POSITION){
                chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra("kanalId",mList.get(kPos).getKanalID());
                chatIntent.putExtra("hedefId",mList.get(kPos).getUserID());
                chatIntent.putExtra("hedefProfilResim",mList.get(kPos).getUserProfileImage());
                chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(chatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MesajlarHolder extends RecyclerView.ViewHolder{
        private TextView kullaniciIsmi, sonMesaj;
        private CircleImageView civKullaniciProfil;
        public MesajlarHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciIsmi = itemView.findViewById(R.id.mesajlar_item_kullanici_adi);
            sonMesaj = itemView.findViewById(R.id.mesajlar_item_son_mesaj);
            civKullaniciProfil = itemView.findViewById(R.id.mesajlar_item_profile_img);
        }
    }
}
