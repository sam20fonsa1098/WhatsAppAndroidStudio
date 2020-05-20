package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.helper.DateUtil;
import com.example.whatsapp.model.Chat;
import com.example.whatsapp.model.Group;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.MyViewHolder> {

    private List<Chat> chatList;
    private Context context;

    public ChatsAdapter(List<Chat> chatList, Context context) {
        this.chatList = chatList;
        this.context  = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contacts, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        if(chat.getIsGroup()) {
            Group group = chat.getGroup();
            holder.name.setText(group.getName());
            if(group.getPhoto() != null) {
                Uri uri = Uri.parse(group.getPhoto());
                Glide.with(context).load(uri).into(holder.circleImageView);
            }
            else{
                holder.circleImageView.setImageResource(R.drawable.padrao);
            }
            holder.date.setText(setData(chat.getDate()));

        }
        else{
            holder.name.setText(chat.getUser().getName());
            if(chat.getUser().getPhoto() != null) {
                Uri uri = Uri.parse(chat.getUser().getPhoto());
                Glide.with(context).load(uri).into(holder.circleImageView);
            }
            else{
                holder.circleImageView.setImageResource(R.drawable.padrao);
            }
            holder.date.setText(setData(chat.getDate()));
        }

        if(chat.getLastMessage() == null) {
            holder.lastMessage.setText("Photo");
        }
        else{
            holder.lastMessage.setText(chat.getLastMessage());
            holder.lastMessage.setCompoundDrawables(null, null, null, null);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        TextView name, lastMessage, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.imageViewPhotoContact);
            name            = itemView.findViewById(R.id.textViewNameContact);
            lastMessage     = itemView.findViewById(R.id.textViewStatusContact);
            date            = itemView.findViewById(R.id.textViewHours);
        }
    }

    private String setData(String date) {
        String currentdDate       = DateUtil.currentDate();
        String arrayDate[]        = date.split(" ");
        String arrayCurrentDate[] = currentdDate.split(" ");
        if(arrayDate[0].equals(arrayCurrentDate[0])) {
            String aux[] = arrayDate[1].split(":");
            return aux[0] + ":" + aux[1];
        }
        return arrayDate[0];
    }
}

