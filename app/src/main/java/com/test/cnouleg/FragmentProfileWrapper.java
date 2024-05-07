package com.test.cnouleg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.cnouleg.api.NotesResults;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.ProfileResults;
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class FragmentProfileWrapper extends Fragment {
    FragmentActivity context;
    RecyclerView recyclerView;
    SearchAdapter searchAdapter;
    FragmentProfile profileFragment;
    Profile loadedProfile;

    public FragmentProfileWrapper() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireActivity();

        if (savedInstanceState == null)
            savedInstanceState = getArguments();

        if (savedInstanceState != null)
            loadedProfile = SharedUtils.GetParcelable(savedInstanceState, "profile", Profile.class);

        profileFragment = new FragmentProfile();
        if (loadedProfile != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("profile", loadedProfile);
            profileFragment.setArguments(bundle);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadedProfile != null)
            outState.putParcelable("profile", loadedProfile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_wrapper, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        searchAdapter = new SearchAdapter(context, viewID -> {
            try {
                FragmentManager fragmentManager = context.getSupportFragmentManager();
                if (profileFragment.isAdded()) {
                    fragmentManager.popBackStackImmediate(FragmentProfile.TAG, 0);

                } else {
                    fragmentManager.beginTransaction()
                            .replace(viewID, profileFragment, FragmentReader.TAG)
                            .commit();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        recyclerView.setAdapter(searchAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        BeginSearch();
    }

    private void BeginSearch() {
        String id;
        if (loadedProfile != null) {
            id = loadedProfile.getId();
        }
        else {
            String token = AccessTokenUtils.GetAccessToken(context);
            if (token == null) {
                searchAdapter.ClearNotes();
                return;
            }
            id = AccessTokenUtils.GetMongoDBIDFromToken(token);
        }

        new Thread(() -> {
            try {
                String srv = SharedUtils.GetServer(context);
                NotesResults notesResults = StaticData.getMapper().readValue(new URL(srv + "/api/notes/?user_id=" + id), NotesResults.class);

                HashMap<String, Profile> authorMapping;
                if (loadedProfile != null) {
                    authorMapping = new HashMap<>(1);
                    authorMapping.put(loadedProfile.getId(), loadedProfile);
                }
                else {
                    ProfileResults authorResults = StaticData.getMapper().readValue(new URL(srv + "/api/users/?include_id[]=" + id), ProfileResults.class);
                    authorMapping = new HashMap<>(authorResults.getUsers().length);

                    for (Profile author : authorResults.getUsers()) {
                        authorMapping.put(author.getId(), author);
                    }
                }

                context.runOnUiThread(() -> {
                    searchAdapter.ReplaceNotes(Arrays.asList(notesResults.getNotes()), authorMapping);
                });
            } catch (IOException e) {
                // context.runOnUiThread(() -> {});
            }
        }).start();
    }
}