package com.example.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ContactsActivity;
import com.example.whatsapp.activity.MainActivity;
import com.example.whatsapp.activity.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    private FloatingActionButton floatingActionButton;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
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
}
