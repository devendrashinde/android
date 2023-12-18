package com.example.dshinde.myapplication_xmlpref.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.dshinde.myapplication_xmlpref.R;
import com.example.dshinde.myapplication_xmlpref.adapters.MediaFragmentAdapter;
import com.example.dshinde.myapplication_xmlpref.common.Constants;
import com.example.dshinde.myapplication_xmlpref.model.MediaFields;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class MediaViewActivity extends AppCompatActivity {

    private static final String TAG = MediaViewActivity.class.getSimpleName();
    String collectionName;
    TabLayout tabLayout;
    ViewPager viewPager;

    MediaFields mediaFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        collectionName = Constants.STORAGE_PATH_NOTES +
                bundle.getString(Constants.USERID) + "/" +
                bundle.getString("noteId");
        Gson gson = new GsonBuilder().create();

        String mediaValues = bundle.getString("mediaFields");
        mediaFields = gson.fromJson(mediaValues, MediaFields.class);
        mediaFields.init();
        if(mediaFields.hasMedia()) {
            mediaFields.setValues(gson.fromJson(mediaValues, Map.class));
        } else {
            finish();
        }

        setContentView(R.layout.activity_mediaview);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText("Photos"));
        tabLayout.addTab(tabLayout.newTab().setText("Documents"));

        final MediaFragmentAdapter adapter = new MediaFragmentAdapter(this, getSupportFragmentManager(),
                tabLayout.getTabCount(), collectionName, mediaFields);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mediaview, menu);
        return true;
    }

}
