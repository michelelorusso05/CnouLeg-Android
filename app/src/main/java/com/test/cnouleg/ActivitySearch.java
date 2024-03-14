package com.test.cnouleg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.test.cnouleg.api.Author;
import com.test.cnouleg.api.AuthorResults;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.NotesResults;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ActivitySearch extends AppCompatActivity {
    RecyclerView recyclerView;
    SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this::BeginSearch);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshLayout.setRefreshing(true);
        BeginSearch();
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

    private void BeginSearch() {
        new Thread(() -> {
            try {
                String srv = SharedUtils.GetServer(ActivitySearch.this);
                NotesResults notesResults = StaticData.getMapper().readValue(new URL(srv + "/api/notes/"), NotesResults.class);

                StringBuilder b = new StringBuilder(srv + "/api/users/?");

                for (Note note : notesResults.getNotes()) {
                    b.append("&include_id[]=").append(note.getAuthorID());
                }

                AuthorResults authorResults = StaticData.getMapper().readValue(new URL(b.toString()), AuthorResults.class);
                HashMap<Integer, Author> authorMapping = new HashMap<>(authorResults.getUsers().length);

                for (Author author : authorResults.getUsers()) {
                    authorMapping.put(author.getId(), author);
                }

                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);

                    SearchAdapter adapter = new SearchAdapter(ActivitySearch.this, notesResults.getNotes(), authorMapping);
                    recyclerView.setAdapter(adapter);
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);

                    SearchAdapter adapter = new SearchAdapter(ActivitySearch.this, new Note[0], null);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }
}