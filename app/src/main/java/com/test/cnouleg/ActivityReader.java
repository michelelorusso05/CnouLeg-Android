package com.test.cnouleg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.ProfileResults;
import com.test.cnouleg.api.Comment;
import com.test.cnouleg.api.CommentResults;
import com.test.cnouleg.api.InsertionResult;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.Rating;
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityReader extends AppCompatActivity {
    Note loadedNote;
    Profile author;

    FragmentReader fragmentReader;
    RecyclerView recyclerView;
    TextInputLayout comment;
    EditText commentField;
    ShapeableImageView commentProfilePic;
    View notLoggedInLayout;
    View loggedInLayout;
    boolean isCommentbarUp;
    boolean isWritingComment;
    String parentReplyingTo;
    OnBackInvokedCallback onBackPressedCallback = null;

    CommentAdapter commentAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        if (savedInstanceState == null)
            savedInstanceState = getIntent().getExtras();

        assert savedInstanceState != null;

        loadedNote = SharedUtils.GetParcelable(savedInstanceState, "note", Note.class);
        author = SharedUtils.GetParcelable(savedInstanceState, "author", Profile.class);

        assert loadedNote != null;

        Bundle extras = new Bundle();
        extras.putParcelable("note", loadedNote);
        extras.putParcelable("author", author);

        fragmentReader = new FragmentReader();
        fragmentReader.setArguments(extras);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loggedInLayout = findViewById(R.id.comment_bar_layout);
        notLoggedInLayout = findViewById(R.id.not_logged_in_bar);

        comment = findViewById(R.id.comment_text_field);

        View bottomBar = findViewById(R.id.bottomCommentBar);
        Drawable endIcon = comment.getEndIconDrawable();
        commentField = comment.getEditText();

        assert endIcon != null;
        assert commentField != null;

        parentReplyingTo = null;

        bottomBar.setVisibility(View.GONE);

        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        slideUp.setFillAfter(true);
        slideUp.setDuration(200);
        TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, 0, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
        slideDown.setFillAfter(true);
        slideDown.setDuration(200);

        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                View firstVisibleChild = layoutManager.getChildAt(0);
                int vHeight = firstVisibleChild != null ? firstVisibleChild.getHeight() : 0;
                int scroll = recyclerView.computeVerticalScrollOffset();
                int h = recyclerView.getHeight();

                if (vHeight == 0) return;

                if (layoutManager.findFirstVisibleItemPosition() >= 1
                        || scroll + h >= vHeight + 112.0f * getResources().getDisplayMetrics().density) {
                    if (!isCommentbarUp) {
                        bottomBar.setVisibility(View.VISIBLE);
                        bottomBar.startAnimation(slideUp);

                        Context wrappedContext = DynamicColors.wrapContextIfAvailable(ActivityReader.this);
                        TypedValue val = new TypedValue();
                        wrappedContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHigh, val, true);

                        getWindow().setNavigationBarColor(wrappedContext.getColor(val.resourceId));

                        isCommentbarUp = true;
                    }
                }
                else {
                    if (isCommentbarUp) {
                        bottomBar.setVisibility(View.GONE);
                        bottomBar.startAnimation(slideDown);

                        bottomBar.postDelayed(() -> {
                            Context wrappedContext = DynamicColors.wrapContextIfAvailable(ActivityReader.this);
                            TypedValue val = new TypedValue();
                            wrappedContext.getTheme().resolveAttribute(android.R.attr.colorBackground, val, true);

                            getWindow().setNavigationBarColor(wrappedContext.getColor(val.resourceId));
                        }, 200);

                        isCommentbarUp = false;
                    }
                }
            }
        };

        recyclerView.addOnScrollListener(onScrollListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            onBackPressedCallback = () -> commentField.clearFocus();

        View touchBlock = findViewById(R.id.touchIntercept);

        View.OnTouchListener onTouchListener = (view, m) -> {
            if (m.getAction() == MotionEvent.ACTION_UP)
                commentField.clearFocus();
            return true;
        };

        touchBlock.setOnTouchListener(onTouchListener);
        // Block all touch
        bottomBar.setOnTouchListener((v, m) -> true);

        commentField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isWritingComment) {
                isWritingComment = true;

                recyclerView.clearOnScrollListeners();
                touchBlock.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY, onBackPressedCallback);
            }
            else if (!hasFocus && isWritingComment) {
                isWritingComment = false;

                touchBlock.setVisibility(View.GONE);
                recyclerView.addOnScrollListener(onScrollListener);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressedCallback);

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentField.getWindowToken(), 0);

                comment.setHelperTextEnabled(false);
                commentField.setText("");
                parentReplyingTo = null;

                bottomBar.postDelayed(() ->
                        onScrollListener.onScrolled(recyclerView, 0, 0), 2000);
            }
        });

        View.OnClickListener commentIconOnClickListener = (v) -> {
            if (commentField.getText().toString().trim().isEmpty())
                return;

            SendComment(commentField.getText().toString(), parentReplyingTo, () -> {
                commentField.setEnabled(true);
                commentField.setText("");
                commentField.clearFocus();
            });
        };

        comment.setOnClickListener(commentIconOnClickListener);
        comment.setEndIconCheckable(false);
        endIcon.setAlpha(128);

        commentField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Enabled
                if (!s.toString().trim().isEmpty()) {
                    comment.setEndIconOnClickListener(commentIconOnClickListener);
                    endIcon.setAlpha(255);
                }
                // Disabled
                else {
                    comment.setEndIconOnClickListener(null);
                    endIcon.setAlpha(128);
                    comment.setEndIconVisible(false);
                    comment.setEndIconVisible(true);
                    comment.refreshEndIconDrawableState();
                }
            }
        });

        commentProfilePic = findViewById(R.id.comment_profile_pic);

        commentAdapter = new CommentAdapter(this, viewID -> {
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentReader.isAdded()) {
                    fragmentManager.popBackStackImmediate(FragmentReader.TAG, 0);

                } else {
                    fragmentManager.beginTransaction()
                            .replace(viewID, fragmentReader, FragmentReader.TAG)
                            .commit();

                    fragmentReader.getLifecycle().addObserver((LifecycleEventObserver) (lifecycleOwner, event) -> {
                        if (event == Lifecycle.Event.ON_RESUME) {
                            ActivityReader.this.findViewById(R.id.progressBar).setVisibility(View.GONE);
                            recyclerView.post(() -> onScrollListener.onScrolled(recyclerView, 0, 0));
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, (authorName, parentID) -> {
            commentField.requestFocus();
            comment.setHelperTextEnabled(true);
            comment.setHelperText(getString(R.string.replying_to, authorName));

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(commentField, InputMethodManager.SHOW_IMPLICIT);

            parentReplyingTo = parentID;
        });
        recyclerView.setAdapter(commentAdapter);

        BeginSearch();

        onScrollListener.onScrolled(recyclerView, 0, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String token = AccessTokenUtils.GetAccessToken(this);
        if (token == null || token.isEmpty()) {
            loggedInLayout.setVisibility(View.GONE);
            notLoggedInLayout.setVisibility(View.VISIBLE);

            notLoggedInLayout.setOnClickListener((v) -> {
                Intent i = new Intent(this, ActivityRegistration.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            });
        }
        else {
            loggedInLayout.setVisibility(View.VISIBLE);
            notLoggedInLayout.setVisibility(View.GONE);

            String id = AccessTokenUtils.GetMongoDBIDFromToken(token);

            new Thread(() -> {
                try {
                    Profile profile = StaticData.getMapper().readValue(new URL(SharedUtils.GetServer(ActivityReader.this) + "/api/users/?include_id[]=" + id),
                            ProfileResults.class).getUsers()[0];

                    runOnUiThread(() ->
                        Glide
                            .with(ActivityReader.this)
                            .load(SharedUtils.GetServer(this) + "/profile_pics/" + profile.getProfilePicURL())
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.account_circle_24px)
                            .into(commentProfilePic));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("note", loadedNote);
        outState.putParcelable("author", author);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void BeginSearch() {
        String token = AccessTokenUtils.GetAccessToken(this);
        String srv = SharedUtils.GetServer(this);

        Request.Builder builder = new Request.Builder()
                .url(srv + "/api/comments/?post_id=" + loadedNote.getId())
                .get();

        if (token != null)
            builder.header("authorization", "Bearer " + token);

        StaticData.getClient().newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.code() != 200) return;

                CommentResults commentResults = StaticData.getMapper().readValue(response.body().bytes(), CommentResults.class);

                StringBuilder b = new StringBuilder(srv + "/api/users/?");

                if (token != null && !token.isEmpty())
                    // Add self
                    b.append("include_id[]=").append(AccessTokenUtils.GetMongoDBIDFromToken(token));

                // Add other users
                for (Comment comment : commentResults.getComments()) {
                    b.append("&include_id[]=").append(comment.getUserID());
                }

                ProfileResults authorResults = StaticData.getMapper().readValue(new URL(b.toString()), ProfileResults.class);
                HashMap<String, Profile> authorMapping = new HashMap<>(authorResults.getUsers().length);

                for (Profile author : authorResults.getUsers()) {
                    authorMapping.put(author.getId(), author);
                }

                runOnUiThread(() ->
                        commentAdapter.addComments(Arrays.asList(commentResults.getComments()), authorMapping));
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (isWritingComment) {
                commentField.clearFocus();
            }
            else {
                super.onBackPressed();
            }
        }
        else
            super.onBackPressed();
    }

    private void SendComment(String content, @Nullable String parent, Runnable onCommentSent) {
        HashMap<String, String> requestBodyMap = new HashMap<>();

        requestBodyMap.put("text", content);
        requestBodyMap.put("post_id", (parent == null) ? loadedNote.getId() : null);
        requestBodyMap.put("parent_id", parent);

        String json;
        try {
            json = StaticData.getMapper().writeValueAsString(requestBodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        String token = AccessTokenUtils.GetAccessToken(this);

        Request.Builder builder = new Request.Builder()
                .url(SharedUtils.GetServer(ActivityReader.this) + "/api/comments/")
                .header("authorization", "Bearer " + token)
                .post(requestBody);

        StaticData.getClient().newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {

                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                InsertionResult inserted = StaticData.getMapper().readValue(response.body().bytes(), InsertionResult.class);

                runOnUiThread(() -> {
                    Comment c = new Comment();
                    c.setId(inserted.getInsertedId());
                    c.setUserID(AccessTokenUtils.GetMongoDBIDFromToken(token));
                    c.setText(content);
                    c.setDate(inserted.getUploadDate());

                    if (parent != null && !parent.isEmpty())
                        c.setParentID(parent);

                    int insertedCommentPosition = commentAdapter.addComment(c, author);
                    recyclerView.smoothScrollToPosition(insertedCommentPosition);

                    if (onCommentSent != null)
                        onCommentSent.run();
                });
            }
        });
    }
}