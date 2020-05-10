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
import com.example.whatsapp.model.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupSelectedAdapter extends RecyclerView.Adapter<GroupSelectedAdapter.MyViewHolder> {

    private List<User> contacts;
    private Context context;

    public GroupSelectedAdapter(List<User> contacts, Context context) {
        this.contacts = contacts;
        this.context  = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_group_selected, parent, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = contacts.get(position);
        holder.name.setText(user.getName().split(" ")[0]);
        if(user.getPhoto() != null) {
            Uri uri = Uri.parse(user.getPhoto());
            Glide.with(context).load(uri).into(holder.circleImageView);
        }
        else{
            holder.circleImageView.setImageResource(R.drawable.padrao);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView circleImageView;
        TextView name;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.imageViewGroupPhoto);
            name            = itemView.findViewById(R.id.textViewGroupName);
        }
    }
}
