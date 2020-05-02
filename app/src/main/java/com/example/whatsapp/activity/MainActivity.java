package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfigFirebase;
import com.example.whatsapp.fragment.CallsFragment;
import com.example.whatsapp.fragment.CameraFragment;
import com.example.whatsapp.fragment.ChatsFragment;
import com.example.whatsapp.fragment.StatusFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("CAMERA", CameraFragment.class)
                .add("CHATS", ChatsFragment.class)
                .add("STATUS", StatusFragment.class)
                .add("CALLS", CallsFragment.class)
                .create()
        );

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPageTab = findViewById(R.id.viewpagertab);
        final LayoutInflater inflater = LayoutInflater.from(this);
        final Resources res = getResources();


        viewPageTab.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                if(position == 0) {
                    ImageView icon = (ImageView) inflater.inflate(R.layout.custom_tab_icon, container,
                            false);
                    icon.setImageDrawable(res.getDrawable(R.drawable.ic_camera_alt_white_24dp));
                    return icon;
                }
                else{
                    View itemView = inflater.inflate(R.layout.custom_tab_text, container, false);
                    TextView text = (TextView) itemView.findViewById(R.id.custom_tab_text);
                    text.setText(adapter.getPageTitle(position));
                    return itemView;
                }
            }
        });


        viewPageTab.setViewPager(viewPager);

        mAuth        = ConfigFirebase.getFirebaseAuth();
        mCurrentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser == null){
            sendUserToLogin();
        }
    }

    private void sendUserToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuOut:
                signOutUser();
                break;
            case R.id.menuConfig:
                openSettings();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signOutUser() {
        try{
            mAuth.signOut();
            sendUserToLogin();
        }
        catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
