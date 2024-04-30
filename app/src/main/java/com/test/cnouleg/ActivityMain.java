package com.test.cnouleg;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ActivityMain extends AppCompatActivity {

    ViewPager2 viewPager;
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.navigation);

        viewPager.setAdapter(new MainActivityFragmentAdapter(this));
        viewPager.setOffscreenPageLimit(2);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Menu menu = bottomNavigation.getMenu();
                switch (position) {
                    case 0:
                        menu.findItem(R.id.section_search).setChecked(true);
                        break;
                    case 1:
                        menu.findItem(R.id.section_profile).setChecked(true);
                        break;
                }
            }
        });

        bottomNavigation.setOnItemSelectedListener((menuItem) -> {
            if (menuItem.getItemId() == R.id.section_search)
                viewPager.setCurrentItem(0);
            else if (menuItem.getItemId() == R.id.section_profile)
                viewPager.setCurrentItem(1);
            else
                throw new IllegalStateException("No such menu was implemented");

            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.search_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}