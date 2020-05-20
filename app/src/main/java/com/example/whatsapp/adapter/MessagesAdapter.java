package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.DateUtil;
import com.example.whatsapp.model.Message;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MyViewHolder> {

    private List<Message> listMessages;
    private Context context;
    private static final int TYPE_SEND    = 0;
    private static final int TYPE_ARRIVED = 1;
    private boolean send = false;

    public MessagesAdapter(List<Message> list, Context c) {
        this.listMessages = list;
        this.context      = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item;
        if(viewType == TYPE_SEND) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_message_send, parent, false);
        }
        else {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_message_arrived, parent, false);
        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message  = listMessages.get(position);
        holder.date.setVisibility(View.VISIBLE);
        holder.date.setText(setData(message.getHour()));

        if(message.getImage() != null) {
            Uri url = Uri.parse(message.getImage());
            Glide.with(context).load(url).into(holder.image);
            holder.message.setVisibility(View.GONE);
        }
        else {
            holder.message.setText(message.getMessage());
            holder.image.setVisibility(View.GONE);
        }
        if(!message.getName().equals("")) {
            if(send) {
                holder.name.setVisibility(View.GONE);
            }
            else{
                holder.name.setText(message.getName());
            }
        }
        else{
            holder.name.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return listMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message  = listMessages.get(position);
        String currentId = CurrentUserFirebase.getIdCurrentUser();
        if(message.getIdUser().equals(currentId)) {
            send = true;
            return TYPE_SEND;
        }
        send = false;
        return TYPE_ARRIVED;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView message, date, name;
        ImageView image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            message = itemView.findViewById(R.id.textViewMessageText);
            image   = itemView.findViewById(R.id.imageViewMessagePhoto);
            date    = itemView.findViewById(R.id.textViewMessageTextHour);
            name    = itemView.findViewById(R.id.textViewMessageTextName);
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
