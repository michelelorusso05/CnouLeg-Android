package com.test.cnouleg;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.SharedUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityEditProfileDetails extends AppCompatActivity {

    MaterialButton chooseImageButton;
    ShapeableImageView profilePic;

    TextInputLayout rolesEditText;
    AutoCompleteTextView rolesEditTextField;
    TextInputLayout bioEditText, schoolEditText, subjectEditText;
    EditText bioEditTextField, schoolEditTextField, subjectEditTextField;
    MaterialButton nextButton;
    CircularProgressIndicator progressIndicator;

    Profile profile;
    Bitmap croppedImageResult;
    boolean hasDeletedImage;

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            hasDeletedImage = false;
            croppedImageResult = result.getBitmap(ActivityEditProfileDetails.this);
            profilePic.setImageBitmap(croppedImageResult);

            new Thread(() -> {
                // Cache image to file for future reload
                try {
                    FileOutputStream out = new FileOutputStream(getCacheDir() + "/temp.jpeg");
                    Bitmap.createScaledBitmap(croppedImageResult, 256, 256, true)
                        .compress(Bitmap.CompressFormat.JPEG, 100, out);

                    out.flush();
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    });

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::LaunchEdit);

    ActivityResultLauncher<String> pickMediaLegacy =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::LaunchEdit);

    private void LaunchEdit(Uri uri) {
        CropImageOptions options = new CropImageOptions();

        options.activityMenuIconColor = MaterialColors.getColor(ActivityEditProfileDetails.this, com.google.android.material.R.attr.colorOnBackground, 0);
        options.activityBackgroundColor = MaterialColors.getColor(ActivityEditProfileDetails.this, android.R.attr.colorBackground, 0);
        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.fixAspectRatio = true;
        options.outputRequestHeight = 256;
        options.outputRequestWidth = 256;
        options.outputCompressFormat = Bitmap.CompressFormat.JPEG;
        options.cropShape = CropImageView.CropShape.OVAL;

        if (uri != null) {
            cropImage.launch(new CropImageContractOptions(uri, options));
        }
    }

    private static final String[] roles = {
        "student",
        "teacher",
        "other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_details);

        if (savedInstanceState == null)
            savedInstanceState = getIntent().getExtras();

        assert savedInstanceState != null;

        profile = SharedUtils.GetParcelable(savedInstanceState, "profileInfo", Profile.class);

        chooseImageButton = findViewById(R.id.editImageButton);

        chooseImageButton.setOnClickListener((v) -> {
            PopupMenu popupMenu = new PopupMenu(ActivityEditProfileDetails.this, chooseImageButton);

            popupMenu.getMenuInflater().inflate(R.menu.edit_profile_pic_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.chooseImage) {
                    if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
                        pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                    }
                    else {
                        pickMediaLegacy.launch("image/*");
                    }
                }
                else if (menuItem.getItemId() == R.id.removeImage) {
                    croppedImageResult = null;
                    profilePic.setImageResource(R.drawable.account_circle_24px);
                    hasDeletedImage = true;
                }

                return true;
            });
            popupMenu.show();
        });

        profilePic = findViewById(R.id.profile_pic);

        if (savedInstanceState.getBoolean("hasEditedImage", false)) {
            Glide
                    .with(this)
                    .asBitmap()
                    .load(getCacheDir() + "/temp.jpeg")
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            croppedImageResult = resource;
                            profilePic.setImageBitmap(croppedImageResult);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        }
        else if (profile.getProfilePicURL() != null && !profile.getProfilePicURL().isEmpty()) {
            Glide
                .with(this)
                .load(SharedUtils.GetServer(this) + "/profile_pics/" + profile.getProfilePicURL())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.account_circle_24px)
                .into(profilePic)
            ;
        }

        rolesEditText = findViewById(R.id.role_text_field);

        String[] roleStrings = getResources().getStringArray(R.array.roles);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_item, roleStrings);

        rolesEditTextField = findViewById(R.id.roleEditText);
        rolesEditTextField.setAdapter(adapter);
        rolesEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                rolesEditTextField.setError(null);
                rolesEditText.setErrorEnabled(false);
            }
        });

        bioEditText = findViewById(R.id.bio_text_field);
        bioEditTextField = bioEditText.getEditText();
        assert bioEditTextField != null;
        bioEditTextField.setText(profile.getBio());

        schoolEditText = findViewById(R.id.school_text_field);
        schoolEditTextField = schoolEditText.getEditText();
        assert schoolEditTextField != null;
        schoolEditTextField.setText(profile.getSchool());

        subjectEditText = findViewById(R.id.subject_text_field);
        subjectEditTextField = subjectEditText.getEditText();
        assert subjectEditTextField != null;
        subjectEditTextField.setText(profile.getSubject());

        rolesEditTextField.setOnItemClickListener((parent, view, position, id) -> {
            profile.setRole(roles[position]);

            if ("other".equals(profile.getRole())) {
                schoolEditText.setVisibility(View.GONE);
                subjectEditText.setVisibility(View.GONE);
            }
            else {
                schoolEditText.setVisibility(View.VISIBLE);
                subjectEditText.setVisibility(View.VISIBLE);
            }
        });

        int pos;
        for (pos = 0; pos < roles.length; pos++) {
            if (roles[pos].equals(profile.getRole()))
                break;
        }

        rolesEditTextField.setText(roleStrings[pos], false);

        nextButton = findViewById(R.id.nextButton);
        progressIndicator = findViewById(R.id.formProgressBar);

        nextButton.setOnClickListener((v) -> {
            ApplyTextFields();

            if (Validate()) {
                progressIndicator.setVisibility(View.VISIBLE);
                nextButton.setEnabled(false);

                MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("bio", profile.getBio())
                        .addFormDataPart("role", profile.getRole())
                        .addFormDataPart("school", profile.getSchool())
                        .addFormDataPart("subject", profile.getSubject())
                        .addFormDataPart("username", profile.getUsername())
                        .addFormDataPart("birthdate", profile.getBirthdate());

                if (croppedImageResult != null) {
                    RequestBody profilePicBody = RequestBody.create(new File(getCacheDir() + "/temp.jpeg"), MediaType.parse("image/jpeg"));

                    requestBodyBuilder
                        .addFormDataPart("avatar", "avatar.jpg", profilePicBody)
                        .build();
                }
                else if (hasDeletedImage) {
                    requestBodyBuilder
                        .addFormDataPart("pic_deleted", "true");
                }

                String token = AccessTokenUtils.GetAccessToken(this);

                Request.Builder builder = new Request.Builder()
                        .url(SharedUtils.GetServer(ActivityEditProfileDetails.this) + "/api/user/")
                        .header("authorization", "Bearer " + token)
                        .put(requestBodyBuilder.build());

                StaticData.getClient().newCall(builder.build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            progressIndicator.setVisibility(View.GONE);
                            nextButton.setEnabled(true);

                            Snackbar.make(nextButton, R.string.error_generic_server, Snackbar.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        int status = response.code();
                        response.body().close();
                        if (status == 401) {
                            runOnUiThread(() -> Snackbar.make(nextButton, R.string.error_authentication_error, Snackbar.LENGTH_SHORT).show());
                        }
                        else {
                            finish();
                        }

                        runOnUiThread(() -> {
                            progressIndicator.setVisibility(View.GONE);
                            nextButton.setEnabled(true);
                        });
                    }
                });
            }
        });
    }

    private boolean Validate() {
        return true;
    }

    private void ApplyTextFields() {
        profile.setBio(bioEditTextField.getText().toString());
        profile.setSchool(schoolEditTextField.getText().toString());
        profile.setSubject(subjectEditTextField.getText().toString());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ApplyTextFields();

        outState.putParcelable("profileInfo", profile);
        outState.putBoolean("hasEditedImage", croppedImageResult != null);
        outState.putBoolean("hasDeletedImage", hasDeletedImage);
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
