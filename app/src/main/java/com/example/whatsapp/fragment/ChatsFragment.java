package com.example.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.activity.ContactsActivity;
import com.example.whatsapp.activity.MainActivity;
import com.example.whatsapp.activity.SettingsActivity;
import com.example.whatsapp.adapter.ChatsAdapter;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.model.Chat;
import com.example.whatsapp.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.AEADBadTagException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private ChatsAdapter chatsAdapter;
    private List<Chat> chatList = new ArrayList<>();
    private DatabaseReference chatsRef;
    private FirebaseUser firebaseUser;
    private ValueEventListener valueEventListener;
    private List<String> aux = new ArrayList<>();

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView      = view.findViewById(R.id.recyclerViewListChats);
        firebaseUser      = CurrentUserFirebase.getCurrentUser();
        chatsRef          = ConfigFirebase.getDatabaseReference()
                                                    .child("Chats")
                                                    .child(Base64Custom.encodeBase64(firebaseUser.getPhoneNumber()));

        //Config adapter
        chatsAdapter = new ChatsAdapter(chatList, getActivity());
        //Config recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(chatsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Chat chat = chatList.get(position);
                        openChat(chat.getUser());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        floatingActionButton = (FloatingActionButton)view.findViewById(R.id.floatingActionButtonChats);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onGlobalLayout() {

    }

    public void takeChats() {
        valueEventListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    if(!aux.contains(data.getKey())) {
                        aux.add(data.getKey());
                        Chat chat = data.getValue(Chat.class);
                        chatList.add(chat);
                        chatsAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        takeChats();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatsRef.removeEventListener(valueEventListener);
    }

    private void openChat(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("UserClicked", user);
        startActivity(intent);
    }

    public void searchChats (String text) {
        List<Chat> chatListSearch = new ArrayList<>();
        for(Chat chat: chatList) {
            String name        = chat.getUser().getName().toLowerCase();
            String lastMessage = chat.getLastMessage();
            if(lastMessage == null) {
                if(name.contains(text)) {
                    chatListSearch.add(chat);
                }
            }
            else {
                if(name.contains(text) || lastMessage.contains(text)) {
                    chatListSearch.add(chat);
                }
            }

        }
        chatsAdapter = new ChatsAdapter(chatListSearch, getActivity());
        recyclerView.setAdapter(chatsAdapter);
        chatsAdapter.notifyDataSetChanged();
    }
}
