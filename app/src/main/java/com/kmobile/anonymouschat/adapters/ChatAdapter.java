package com.kmobile.anonymouschat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.model.Chat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    private static final int MESAJ_SAG = 0;
    private static final int MESAJ_SOL = 1;
    private ArrayList<Chat> chatList;
    private View view;
    private Context mContext;
    private String mUID;
    private String hedefResimURL;
    private Chat mChat;

    public ChatAdapter(ArrayList<Chat> chatList, Context mContext, String mUID, String hedefResimURL) {
        this.chatList = chatList;
        this.mContext = mContext;
        this.mUID = mUID;
        this.hedefResimURL = hedefResimURL;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MESAJ_SOL){
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_lefs,parent,false);
        }else if(viewType == MESAJ_SAG){
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
        }

        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        mChat = chatList.get(position);

        if(mChat.getMesajTipi().equals("text")){
            holder.imgResim.setVisibility(View.GONE);
            holder.mProgress.setVisibility(View.GONE);
            holder.txtMessage.setText(mChat.getMesajIcerigi());
        }else {
            holder.txtMessage.setVisibility(View.GONE);
            Picasso.get().load(mChat.getMesajIcerigi()).resize(200,200).into(holder.imgResim);
        }



        if(!mChat.getGonderen().equals(mUID)){
            if(hedefResimURL.equals("default")){
                holder.civKullaniciProfil.setImageResource(R.mipmap.ic_launcher);
            }else {
                Picasso.get().load(hedefResimURL).resize(56,56).into(holder.civKullaniciProfil,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.mProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatList.get(position).getGonderen().equals(mUID)){
            return MESAJ_SAG;
        }else {
            return MESAJ_SOL;
        }
    }

    public class ChatHolder extends RecyclerView.ViewHolder{
        private CircleImageView civKullaniciProfil;
        private TextView txtMessage;
        private ImageView imgResim;
        private ProgressBar mProgress;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            civKullaniciProfil = itemView.findViewById(R.id.iv_chat_item_profilresim);
            txtMessage = itemView.findViewById(R.id.tv_chat_item_mesaj);
            imgResim = itemView.findViewById(R.id.chat_item_imgResim);
            mProgress = itemView.findViewById(R.id.chat_item_progress);
        }
    }
}
