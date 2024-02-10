package com.kmobile.anonymouschat.adapters;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kmobile.anonymouschat.R;
import com.kmobile.anonymouschat.activities.ChatActivity;
import com.kmobile.anonymouschat.model.MessageRequest;
import com.kmobile.anonymouschat.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private ArrayList<User> userList;
    private Context mContext;
    private User user;
    private View v;
    private int userPosition;

    private Dialog msgDialog;
    private ImageView imgCancel;
    private LinearLayout linearSend;
    private CircleImageView civUserImage;
    private EditText editMessage;
    private String txtMessage;
    private TextView txtUserName;
    private Window msgWindow;

    private FirebaseFirestore mFireStore;
    private DocumentReference mRef;
    private String mUid, mIsim, mProfilResmi, channelID, msgDocID;
    private MessageRequest msgRequest;
    private HashMap<String, Object> mData;

    public UserAdapter(ArrayList<User> userList, Context context, String mUid, String mIsim, String mProfilResmi){
        this.userList = userList;
        this.mContext = context;
        mFireStore = FirebaseFirestore.getInstance();
        this.mUid = mUid;
        this.mIsim = mIsim;
        this.mProfilResmi = mProfilResmi;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        user = userList.get(position);
        holder.userName.setText(user.getUserName());

        if(user.isUserOnline()){
            holder.ivOnline.setImageResource(R.drawable.kullanici_online);
        }else {
            holder.ivOnline.setImageResource(R.drawable.kullanici_offline);
        }

        if(user.getUserImage().equals("default")){
            holder.civ.setImageResource(R.drawable.user);
        }else {
            Picasso.get().load(user.getUserImage()).resize(66,66).into(holder.civ);
        }

        holder.itemView.setOnClickListener(view -> {
            userPosition = holder.getAdapterPosition();

            if(userPosition != RecyclerView.NO_POSITION){

                mRef = mFireStore.collection("kullanicilar").document(mUid)
                        .collection("kanal").document(userList.get(userPosition).getUserID());

                mRef.get().addOnSuccessListener(documentSnapshot -> {

                    if(documentSnapshot.exists()){
                        //Mesajlaşma aktivite
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        intent.putExtra("hedefId",userList.get(userPosition).getUserID());
                        intent.putExtra("kanalId", documentSnapshot.getData().get("kanalID").toString());
                        intent.putExtra("hedefProfilResim",userList.get(userPosition).getUserImage());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent);

                    }else{
                        showDialogSendMessage(user);
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void showDialogSendMessage(User user){
        msgDialog = new Dialog(mContext);
        msgDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        msgWindow = msgDialog.getWindow();
        msgWindow.setGravity(Gravity.CENTER);
        msgDialog.setContentView(R.layout.custom_dialog_send_message);

        imgCancel = msgDialog.findViewById(R.id.cstm_dialog_cancel_msg);
        linearSend = msgDialog.findViewById(R.id.cstm_dialog_send_message);
        civUserImage = msgDialog.findViewById(R.id.cstm_dialog_snd_msg_user_image);
        editMessage = msgDialog.findViewById(R.id.cstm_dialog_snd_msg_edittext);

        txtUserName = msgDialog.findViewById(R.id.cstm_dialog_txt_user_name);
        txtUserName.setText(userList.get(userPosition).getUserName());

        if(user.getUserImage().equals("default")){
            civUserImage.setImageResource(R.drawable.user);
        }else {
            Picasso.get().load(user.getUserImage()).resize(126,126).into(civUserImage);
        }

        imgCancel.setOnClickListener(v -> {
            msgDialog.dismiss();
        });

        linearSend.setOnClickListener(v -> {
            txtMessage = editMessage.getText().toString();

            if(!TextUtils.isEmpty(txtMessage)){
                //mesaj gönderme işlemleri
                channelID = UUID.randomUUID().toString();

                msgRequest = new MessageRequest(channelID,mUid, mIsim, mProfilResmi);

                mFireStore.collection("mesajistekleri").document(user.getUserID()).collection("istekler")
                        .document(mUid)
                        .set(msgRequest)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //chat bölümü
                                    msgDocID = UUID.randomUUID().toString();

                                    mData = new HashMap<>();
                                    mData.put("mesajIcerigi",txtMessage);
                                    mData.put("gonderen",mUid);
                                    mData.put("alici",user.getUserID());
                                    mData.put("mesajTipi","text");
                                    mData.put("mesajTarihi", FieldValue.serverTimestamp());
                                    mData.put("docId",msgDocID);

                                    mFireStore.collection("chatkanallari").document(channelID)
                                            .collection("mesajlar").document(msgDocID)
                                            .set(mData)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(mContext, "Mesaj isteğiniz iletildi.", Toast.LENGTH_SHORT).show();

                                                        if(msgDialog.isShowing())
                                                            msgDialog.dismiss();

                                                    }else {
                                                        Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                }else {
                                    Toast.makeText(mContext, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });

        msgWindow.setLayout(ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT);
        msgDialog.show();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        private TextView userName;
        private CircleImageView civ;
        private ImageView ivOnline;
        private UserViewHolder(View view){
            super(view);
            this.userName = view.findViewById(R.id.ui_user_name);
            this.civ = view.findViewById(R.id.ui_user_profile_img);
            this.ivOnline = view.findViewById(R.id.iv_user_item_online);
        }
    }
}
