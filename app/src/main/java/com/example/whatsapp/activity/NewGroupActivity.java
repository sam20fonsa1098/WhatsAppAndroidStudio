package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.adapter.ContactsAdapter;
import com.example.whatsapp.adapter.GroupSelectedAdapter;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.helper.CurrentUserFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerViewGroupSelected, recyclerViewGroupContacts;
    private ContactsAdapter contactsAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> userSelected = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private ValueEventListener valueEventListener;
    private DatabaseReference databaseReference;
    private User currentUser;
    private GroupSelectedAdapter groupSelectedAdapter;
    private Toolbar toolbar;
    private Integer allContacts;
    private ImageView divider;
    private MaterialSearchView materialSearchView;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        toolbar = findViewById(R.id.toolbarMain);
        toolbar.setTitle("New Group");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        floatingActionButton      = findViewById(R.id.floatingActionButtonNewGroup);
        materialSearchView        = findViewById(R.id.materialSearchMain);
        recyclerViewGroupSelected = findViewById(R.id.recyclerViewNewGroupSelected);
        recyclerViewGroupContacts = findViewById(R.id.recyclerViewNewGroupContacts);
        contactsAdapter           = new ContactsAdapter(userList, getApplicationContext());
        databaseReference         = ConfigFirebase.getDatabaseReference().child("Users");
        currentUser               = CurrentUserFirebase.getUser();
        divider                   = findViewById(R.id.dividerNewGroup);
        divider.setVisibility(View.INVISIBLE);

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
                        toolbar.setSubtitle(userSelected.size() + " of " + allContacts + " selected");
                        if(userList.size() > 0) {
                            divider.setVisibility(View.VISIBLE);
                        }
                        else{
                            divider.setVisibility(View.INVISIBLE);
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
                        if(userSelected.size() == 0) {
                            divider.setVisibility(View.INVISIBLE);
                            toolbar.setSubtitle("Add participants");
                        }
                        else {
                            divider.setVisibility(View.VISIBLE);
                            toolbar.setSubtitle(userSelected.size() + " of " + allContacts + " selected");
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
                searchWithBack();
            }
        });

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchChats(newText.toLowerCase());
                return true;
            }
        });


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userSelected.size() == 0){
                    Toast.makeText(NewGroupActivity.this, "At least 1 contact must be selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    openRegisterNewGroup();
                }
            }
        });
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
                            allUsers.add(user);
                        }
                    }
                }
                allContacts = userList.size();
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
        toolbar.setSubtitle("Add participants");
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_new_group, menu);

        MenuItem item = menu.findItem(R.id.menuGroupSearch);
        materialSearchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);
    }

    public void searchChats (String text) {
        List<User> userListSearch = new ArrayList<>();
        for(User user: userList) {
            String name   = user.getName().toLowerCase();
            String status = user.getStatus();
            if(name.contains(text) || status.contains(text)) {
                userListSearch.add(user);
            }
        }
        contactsAdapter = new ContactsAdapter(userListSearch, getApplicationContext());
        recyclerViewGroupContacts.setAdapter(contactsAdapter);
        contactsAdapter.notifyDataSetChanged();
        userList = userListSearch;
    }

    public void searchWithBack() {
        List<User> userListSearch = new ArrayList<>();
        for(User user: allUsers) {
            if(!userSelected.contains(user)) {
               userListSearch.add(user);
            }
        }
        contactsAdapter = new ContactsAdapter(userListSearch, getApplicationContext());
        recyclerViewGroupContacts.setAdapter(contactsAdapter);
        contactsAdapter.notifyDataSetChanged();
        userList = userListSearch;
    }

    public void openRegisterNewGroup() {
        Intent intent = new Intent(NewGroupActivity.this, RegisterNewGroupActivity.class);
        intent.putExtra("members", (Serializable) userSelected);
        startActivity(intent);
    }
}
