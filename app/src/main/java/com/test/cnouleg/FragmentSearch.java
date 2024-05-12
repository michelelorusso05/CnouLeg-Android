package com.test.cnouleg;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
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
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class FragmentSearch extends Fragment {
    FragmentActivity context;
    private ViewModelSearch viewModel;

    RecyclerView recyclerView;
    SearchAdapter searchAdapter;
    SwipeRefreshLayout refreshLayout;
    FloatingActionButton createNote;
    FragmentSearchBar searchBarFragment;

    public FragmentSearch() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireActivity();
        viewModel = new ViewModelProvider(this).get(ViewModelSearch.class);
        searchBarFragment = new FragmentSearchBar();

        getParentFragmentManager().setFragmentResultListener("search", this, (requestKey, result) -> {
            String text = result.getString("text", null);
            int rating = result.getInt("rating", 0);
            String[] tags = result.getStringArray("tags");
            String subject = result.getString("subject", null);
            String classLevel = result.getString("classLevel", null);

            BeginSearch(text, rating, tags, subject, classLevel);
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.refreshLayout);

        createNote = view.findViewById(R.id.create_note_fab);
        createNote.setOnClickListener((v) ->
                startActivity(new Intent(requireContext(), ActivityNoteEditor.class)));

        createNote.setVisibility(AccessTokenUtils.GetAccessToken(context) != null ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshLayout.setOnRefreshListener(this::BeginSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        searchAdapter = new SearchAdapter(context, viewID -> {
            try {
                FragmentManager fragmentManager = context.getSupportFragmentManager();
                if (searchBarFragment.isAdded()) {
                    fragmentManager.popBackStackImmediate(FragmentSearchBar.TAG, 0);

                } else {
                    fragmentManager.beginTransaction()
                            .replace(viewID, searchBarFragment, FragmentSearchBar.TAG)
                            .commit();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        recyclerView.setAdapter(searchAdapter);

        // refreshLayout.setRefreshing(true);

        BeginSearch();
    }

    @Override
    public void onStart() {
        super.onStart();
        createNote.setVisibility(AccessTokenUtils.GetAccessToken(context) != null ? View.VISIBLE : View.GONE);
    }

    private void BeginSearch() {
        BeginSearch(null, 0, null, null, null);
    }
    public void BeginSearch(String text, int rating, String[] tags, String subject, String classLevel) {
        new Thread(() -> {
            try {
                String srv = SharedUtils.GetServer(context);

                StringBuilder notesQuery = new StringBuilder(srv + "/api/notes/?");

                if (text != null && !text.trim().isEmpty())
                    notesQuery.append("&text=").append(text);
                if (rating > 0)
                    notesQuery.append("&rating=").append(rating);
                if (tags != null) {
                    for (String tag : tags) {
                        notesQuery.append("&tags[]=").append(tag);
                    }
                }
                if (subject != null && !"any".contentEquals(subject))
                    notesQuery.append("&subject=").append(subject);
                if (classLevel != null && !"any".contentEquals(classLevel))
                    notesQuery.append("&school=").append(classLevel);

                NotesResults notesResults = StaticData.getMapper().readValue(new URL(notesQuery.toString()), NotesResults.class);

                HashMap<String, Profile> authorMapping = new HashMap<>();

                if (notesResults.getNotes().length > 0) {
                    StringBuilder b = new StringBuilder(srv + "/api/users/?");
                    for (Note note : notesResults.getNotes()) {
                        b.append("&include_id[]=").append(note.getAuthorID());
                    }

                    ProfileResults authorResults = StaticData.getMapper().readValue(new URL(b.toString()), ProfileResults.class);
                    for (Profile author : authorResults.getUsers()) {
                        authorMapping.put(author.getId(), author);
                    }
                }

                context.runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    searchAdapter.ReplaceNotes(Arrays.asList(notesResults.getNotes()), authorMapping);
                    getParentFragmentManager().setFragmentResult("searchEnd", Bundle.EMPTY);
                });
            } catch (IOException e) {
                context.runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    getParentFragmentManager().setFragmentResult("searchEnd", Bundle.EMPTY);
                });
            }
        }).start();
    }
}