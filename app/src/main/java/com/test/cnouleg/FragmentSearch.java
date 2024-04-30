package com.test.cnouleg;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.ProfileResults;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.NotesResults;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class FragmentSearch extends Fragment {
    Activity context;
    private ViewModelSearch viewModel;

    RecyclerView recyclerView;
    SwipeRefreshLayout refreshLayout;
    FloatingActionButton createNote;

    public FragmentSearch() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireActivity();
        viewModel = new ViewModelProvider(this).get(ViewModelSearch.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        createNote = view.findViewById(R.id.create_note_fab);
        createNote.setOnClickListener((v) -> {
            startActivity(new Intent(requireContext(), ActivityNoteEditor.class));
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setOnRefreshListener(this::BeginSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        refreshLayout.setRefreshing(true);

        BeginSearch();
    }

    private void BeginSearch() {
        new Thread(() -> {
            try {
                String srv = SharedUtils.GetServer(context);
                NotesResults notesResults = StaticData.getMapper().readValue(new URL(srv + "/api/notes/"), NotesResults.class);

                StringBuilder b = new StringBuilder(srv + "/api/users/?");

                for (Note note : notesResults.getNotes()) {
                    b.append("&include_id[]=").append(note.getAuthorID());
                }

                ProfileResults authorResults = StaticData.getMapper().readValue(new URL(b.toString()), ProfileResults.class);
                HashMap<String, Profile> authorMapping = new HashMap<>(authorResults.getUsers().length);

                for (Profile author : authorResults.getUsers()) {
                    authorMapping.put(author.getId(), author);
                }

                context.runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);

                    SearchAdapter adapter = new SearchAdapter(context, notesResults.getNotes(), authorMapping);
                    recyclerView.setAdapter(adapter);
                });
            } catch (IOException e) {
                e.printStackTrace();
                context.runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);

                    SearchAdapter adapter = new SearchAdapter(context, new Note[0], null);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }
}