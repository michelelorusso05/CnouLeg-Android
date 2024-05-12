package com.test.cnouleg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ActivityViewProfileDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_details);

        Intent intent = getIntent();

        Bundle extras = savedInstanceState != null ? savedInstanceState : intent.getExtras();

        if (extras == null || !extras.containsKey("profile")) {
            if (extras == null)
                extras = new Bundle();

            Uri data = intent.getData();

            if (data == null)
                throw new IllegalArgumentException("Must provide either a Profile through extras or a ID through data.");

            List<String> linkParts = data.getPathSegments();
            String id = linkParts.get(linkParts.size() - 1);

            if (id.length() != 24) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Link non valido")
                        .setMessage("Questo utente non esiste.")
                        .setIcon(R.drawable.format_link_24px)
                        .setPositiveButton(android.R.string.ok, (v, m) -> v.dismiss())
                        .setOnDismissListener((v) -> finish())
                        .show();

                return;
            }

            extras.putString("profileID", id);
        }

        FragmentProfileWrapper profileFragment = new FragmentProfileWrapper();
        profileFragment.setArguments(extras);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, profileFragment)
                    .commitNow();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}