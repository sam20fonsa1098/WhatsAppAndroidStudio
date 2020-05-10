package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.example.whatsapp.adapter.ContactsAdapter;
import com.example.whatsapp.adapter.GroupSelectedAdapter;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGroupSelected, recyclerViewGroupContacts;
    private ContactsAdapter contactsAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> userSelected = new ArrayList<>();
    private ValueEventListener valueEventListener;
    private DatabaseReference databaseReference;
    private User currentUser;
    private GroupSelectedAdapter groupSelectedAdapter;
    private Toolbar toolbar;
    private String allContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        toolbar = findViewById(R.id.toolbarGroup);
        toolbar.setTitle("New Group");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allContacts               = getIntent().getStringExtra("sizeContacts");
        recyclerViewGroupSelected = findViewById(R.id.recyclerViewNewGroupSelected);
        recyclerViewGroupContacts = findViewById(R.id.recyclerViewNewGroupContacts);
        contactsAdapter           = new ContactsAdapter(userList, getApplicationContext());
        databaseReference         = ConfigFirebase.getDatabaseReference().child("Users");
        currentUser               = CurrentUserFirebase.getUser();

        toolbar.setSubtitle(userSelected.size() + " of " + allContacts);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewGroupContacts.setLayoutManager(layoutManager);
        recyclerViewGroupContacts.setHasFixedSize(true);
        recyclerViewGroupContacts.setAdapter(contactsAdapter);


        recyclerViewGroupContacts.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerViewGroupContacts,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        User user       = userList.get(position);
                        userList.remove(user);
                        contactsAdapter.notifyDataSetChanged();
                        userSelected.add(user);
                        groupSelectedAdapter.notifyDataSetChanged();
                        toolbar.setSubtitle(userSelected.size() + " of " + allContacts);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        groupSelectedAdapter = new GroupSelectedAdapter(userSelected, getApplicationContext());
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerViewGroupSelected.setLayoutManager(layoutManager1);
        recyclerViewGroupSelected.setHasFixedSize(true);
        recyclerViewGroupSelected.setAdapter(groupSelectedAdapter);

        recyclerViewGroupSelected.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerViewGroupSelected,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        User user       = userSelected.get(position);
                        userList.add(user);
                        contactsAdapter.notifyDataSetChanged();
                        userSelected.remove(user);
                        groupSelectedAdapter.notifyDataSetChanged();
                        toolbar.setSubtitle(userSelected.size() + " of " + allContacts);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));
    }

    public void takeContacts() {
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    boolean flag = true;
                    for(User cada : userList) {
                        if(cada.getPhoneNumber().equals(user.getPhoneNumber())) {
                            flag = false;
                            break;
                        }
                    }

                    if (!user.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
                        if (flag) {
                            userList.add(user);
                        }
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(userList.size() == 0) {
            takeContacts();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
    }
}
