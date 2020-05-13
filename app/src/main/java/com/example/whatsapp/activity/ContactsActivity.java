package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.whatsapp.R;
import com.example.whatsapp.adapter.ChatsAdapter;
import com.example.whatsapp.adapter.ContactsAdapter;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.model.Chat;
import com.example.whatsapp.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewContacts;
    private ContactsAdapter contactsAdapter;
    private ArrayList<User> contactLists = new ArrayList<>();
    private DatabaseReference usersRef;
    private ValueEventListener valueEventListener;
    private ProgressBar progressBarContact;
    private User currentUser;
    private MaterialSearchView materialSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        toolbar.setTitle("Select contact");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        materialSearchView   = findViewById(R.id.materialSearchMain);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        contactsAdapter      = new ContactsAdapter(contactLists, getApplicationContext());
        usersRef             = ConfigFirebase.getDatabaseReference().child("Users");
        progressBarContact   = findViewById(R.id.progressBarContact);
        currentUser          = CurrentUserFirebase.getUser();
        progressBarContact.setVisibility(View.INVISIBLE);

        //Config RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewContacts.setLayoutManager(layoutManager);
        recyclerViewContacts.setHasFixedSize(true);
        recyclerViewContacts.setAdapter(contactsAdapter);

        //Config click event on recyclerview
        recyclerViewContacts.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerViewContacts,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        User user       = contactLists.get(position);
                        if(!user.getName().equals("New Group")) {
                            openChat(user);
                        }
                        else{
                            openNewGroup();
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
        }
        ));

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                searchContacts("");
            }
        });

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchContacts(newText.toLowerCase());
                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contacts, menu);

        MenuItem item = menu.findItem(R.id.menuContactSearch);
        materialSearchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuContactRefresh:
                takeContacts();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void takeContacts() {
        progressBarContact.setVisibility(View.VISIBLE);
        valueEventListener = usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    boolean flag = true;
                    for(User cada : contactLists) {
                        if(cada.getPhoneNumber().equals(user.getPhoneNumber())) {
                            flag = false;
                            break;
                        }
                    }

                    if (!user.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
                        if (flag) {
                            contactLists.add(user);
                        }
                    }
                }
                contactsAdapter.notifyDataSetChanged();
                progressBarContact.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        usersRef.removeEventListener(valueEventListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(contactLists.size() == 0 ){
            User userGroup = new User();
            userGroup.setName("New Group");
            userGroup.setStatus("");
            userGroup.setPhoneNumber("");
            contactLists.add(userGroup);
            takeContacts();
        }
    }

    public void openNewGroup() {
        Intent intent = new Intent(ContactsActivity.this, NewGroupActivity.class);
        startActivity(intent);
    }

    public void openChat(User user) {
        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
        intent.putExtra("UserClicked", user);
        startActivity(intent);
    }

    public void searchContacts (String text) {
        List<User> chatListContacts = new ArrayList<>();
        for(User user: contactLists) {
            String name   = user.getName().toLowerCase();
            String status = user.getStatus().toLowerCase();
            if(name.contains(text) || status.contains(text)) {
                chatListContacts.add(user);
            }

        }
        contactsAdapter = new ContactsAdapter(chatListContacts, getApplicationContext());
        recyclerViewContacts.setAdapter(contactsAdapter);
        contactsAdapter.notifyDataSetChanged();
    }
}
