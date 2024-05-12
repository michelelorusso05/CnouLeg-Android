package com.test.cnouleg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.ProfileResults;
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;

public class FragmentProfile extends Fragment {
    public static final String TAG = FragmentProfile.class.getSimpleName();
    MaterialButton loginButton;
    MaterialButton registrationButton;
    MaterialButton editProfile;

    View infoCard;
    View loginControlsCard;

    MaterialTextView authorName, bio, birthdate, studyingAt, notesLabel;
    ShapeableImageView authorProfilePic;
    Profile loadedProfile;
    String loadedProfileId;

    public FragmentProfile() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        infoCard = view.findViewById(R.id.infoCard);
        loginControlsCard = view.findViewById(R.id.accountFormsCard);

        authorName = view.findViewById(R.id.card_author);
        authorProfilePic = view.findViewById(R.id.profile_pic);

        bio = view.findViewById(R.id.profile_bio);
        birthdate = view.findViewById(R.id.profile_birthdate);
        studyingAt = view.findViewById(R.id.profile_studying);

        loginButton = view.findViewById(R.id.loginButton);
        registrationButton = view.findViewById(R.id.registrationButton);

        editProfile = view.findViewById(R.id.edit_profile_button);

        notesLabel = view.findViewById(R.id.notesLabel);

        loginButton.setOnClickListener((v) -> {
            Intent i = new Intent(requireContext(), ActivityLogin.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
        });

        registrationButton.setOnClickListener((v) -> {
            Intent i = new Intent(requireContext(), ActivityRegistration.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
        });

        if (savedInstanceState == null)
            savedInstanceState = getArguments();

        if (savedInstanceState != null) {
            loadedProfile = SharedUtils.GetParcelable(savedInstanceState, "profile", Profile.class);
            loadedProfileId = savedInstanceState.getString("profileID", null);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loadedProfile != null)
            outState.putParcelable("profile", loadedProfile);

        outState.putString("profileID", loadedProfileId);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Display mode, disable all edit options
        if (loadedProfile != null || loadedProfileId != null) {
            infoCard.setVisibility(View.VISIBLE);
            loginControlsCard.setVisibility(View.GONE);
            notesLabel.setVisibility(View.VISIBLE);

            if (loadedProfile != null)
                ApplyInfo(loadedProfile, false);
            else
                GetUserInfo(loadedProfileId);
        }
        else {
            String token = AccessTokenUtils.GetAccessToken(requireContext());

            if (token != null && !token.isEmpty()) {
                infoCard.setVisibility(View.VISIBLE);
                loginControlsCard.setVisibility(View.GONE);
                notesLabel.setVisibility(View.VISIBLE);
                GetUserInfo(AccessTokenUtils.GetMongoDBIDFromToken(token));
            }
            else {
                infoCard.setVisibility(View.GONE);
                loginControlsCard.setVisibility(View.VISIBLE);
                notesLabel.setVisibility(View.GONE);
                authorProfilePic.setImageResource(R.drawable.account_circle_24px);
                authorName.setText(R.string.account_guest_name);
            }
        }
    }

    private void GetUserInfo(String id) {
        new Thread(() -> {
            try {
                URL url = new URL(SharedUtils.GetServer(requireContext()) + "/api/users/?include_id[]=" + id);
                ProfileResults authorResults = StaticData.getMapper().readValue(url, ProfileResults.class);

                if (authorResults.getUsers().length == 0 || authorResults.getUsers()[0] == null)
                    throw new IOException("Server didn't recognize the JWT token. Wrong API maybe?");

                Profile profile = authorResults.getUsers()[0];

                Activity activity = getActivity();
                if (activity == null) return;

                activity.runOnUiThread(() -> {
                    ApplyInfo(profile, true);
                });

            } catch (IOException e) {
                Activity activity = getActivity();
                if (activity == null) return;

                if (loadedProfileId != null) {
                    activity.runOnUiThread(() -> {
                        new MaterialAlertDialogBuilder(activity)
                                .setTitle("Errore")
                                .setMessage("Si è verificato un errore.")
                                .setPositiveButton(android.R.string.ok, (v, m) -> v.dismiss())
                                .setOnDismissListener((v) -> activity.finish())
                                .show();
                    });
                }
                else {
                    activity.runOnUiThread(() ->
                           Snackbar.make(requireView(), R.string.error_generic_server, Snackbar.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void ApplyInfo(Profile profile, boolean self) {
        authorName.setText(profile.getUsername());

        if (profile.getProfilePicURL() != null && !profile.getProfilePicURL().isEmpty()) {
            Glide
                    .with(requireContext())
                    .load(SharedUtils.GetServer(requireContext()) + "/profile_pics/" + profile.getProfilePicURL())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.account_circle_24px)
                    .into(authorProfilePic)
            ;
        }
        else {
            authorProfilePic.setImageResource(R.drawable.account_circle_24px);
        }

        if (profile.getBio() == null || profile.getBio().isEmpty())
            bio.setText(R.string.bio_not_set);
        else
            bio.setText(profile.getBio());

        birthdate.setText(requireContext().getString(R.string.profile_birthday, SharedUtils.FormatDateLocale(requireContext(), profile.getBirthdate())));

        if (profile.getRole() == null || profile.getRole().isEmpty() || "other".equals(profile.getRole())) {
            studyingAt.setVisibility(View.GONE);
        }
        else {
            studyingAt.setVisibility(View.VISIBLE);
            if ("student".equals(profile.getRole())) {
                if (profile.getSchool() != null && !profile.getSchool().isEmpty()) {
                    if (profile.getSubject() != null && !profile.getSubject().isEmpty())
                        studyingAt.setText(getString(R.string.profile_studies, profile.getSubject(), profile.getSchool()));
                    else
                        studyingAt.setText(getString(R.string.profile_studies_alt, profile.getSchool()));
                }
                else
                    studyingAt.setText(R.string.profile_student);
            }
            else if ("teacher".equals(profile.getRole())) {
                if (profile.getSchool() != null && !profile.getSchool().isEmpty()) {
                    if (profile.getSubject() != null && !profile.getSubject().isEmpty())
                        studyingAt.setText(getString(R.string.profile_teaches, profile.getSubject(), profile.getSchool()));
                    else
                        studyingAt.setText(getString(R.string.profile_teaches_alt, profile.getSchool()));
                }
                else
                    studyingAt.setText(R.string.profile_teacher);
            }
        }

        if (self) {
            editProfile.setVisibility(View.VISIBLE);
            editProfile.setOnClickListener((v) -> {
                Intent i = new Intent(requireContext(), ActivityEditProfileDetails.class);
                i.putExtra("profileInfo", profile);
                startActivity(i);
            });
        }
        else {
            editProfile.setVisibility(View.GONE);
        }
    }
}