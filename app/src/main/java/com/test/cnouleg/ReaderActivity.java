package com.test.cnouleg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.api.Author;
import com.test.cnouleg.api.Content;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.ValuesTranslator;
import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;
import java.util.HashMap;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.latex.JLatexMathPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.syntax.Prism4jTheme;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;


@PrismBundle(includeAll = true, grammarLocatorClassName = ".GrammarLocatorSourceCode")
public class ReaderActivity extends AppCompatActivity {
    TextView markdownView;
    TextView title, authorView;
    TextView ratingsView, ratingsCountView;
    ChipGroup tagsContainer;
    Chip subjectChip;
    Chip classChip;
    ShapeableImageView authorProfilePic, commentProfilePic;

    Note loadedNote;
    Author author;
    RatingBar ratingBar;
    RecyclerView imagesRecyclerView, videosRecyclerView, documentsRecyclerView;
    TextInputLayout comment;
    View imagesContainer, videosContainer, documentsContainer;

    private String queuedDownloadFilename;
    private final HashMap<Long, ContentAdapter.OnDownloadFinishedCallback> callbackMap = new HashMap<>();
    private ContentAdapter.OnDownloadFinishedCallback queuedDownloadCallback;
    public static final int STATUS_DOWNLOAD_SUCCEDED = 0;
    public static final int STATUS_DOWNLOAD_FAILED = 1;
    public static final int STATUS_DOWNLOAD_CANCELLED = 2;
    public static final int STATUS_DOWNLOAD_RUNNING = 3;
    public static final int STATUS_DOWNLOAD_NO_OP = -1;
    ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                    DownloadFile(queuedDownloadFilename, queuedDownloadCallback);
                else
                    queuedDownloadCallback.execute(STATUS_DOWNLOAD_CANCELLED, null, null);

            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        
        markdownView = findViewById(R.id.markdown);
        title = findViewById(R.id.titleView);
        authorView = findViewById(R.id.authorView);
        ratingsView = findViewById(R.id.rating_text_view);
        ratingsCountView = findViewById(R.id.rating_count_text_view);
        subjectChip = findViewById(R.id.subject_chip);
        classChip = findViewById(R.id.class_chip);
        tagsContainer = findViewById(R.id.tagsContainer);
        authorProfilePic = findViewById(R.id.author_profile_pic);
        comment = findViewById(R.id.comment_text_field);
        commentProfilePic = findViewById(R.id.comment_profile_pic);
        ratingBar = new RatingBar(findViewById(R.id.ratingButtonsContainer));

        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        documentsRecyclerView = findViewById(R.id.documentsRecyclerView);

        imagesContainer = findViewById(R.id.imagesContainer);
        videosContainer = findViewById(R.id.videosContainer);
        documentsContainer = findViewById(R.id.documentsContainer);

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (savedInstanceState == null)
            savedInstanceState = getIntent().getExtras();

        assert savedInstanceState != null;

        loadedNote = SharedUtils.GetParcelable(savedInstanceState, "note", Note.class);
        author = SharedUtils.GetParcelable(savedInstanceState, "author", Author.class);

        assert loadedNote != null;

        title.setText(loadedNote.getTitle());
        authorView.setText(author.getName());
        ratingsView.setText(String.valueOf(loadedNote.getAverageRating()));
        ratingsCountView.setText(String.valueOf(loadedNote.getNumberOfRatings()));
        subjectChip.setText(ValuesTranslator.getTranslatedSubject(this, loadedNote.getSubject()));
        subjectChip.setChipIcon(ValuesTranslator.getDrawableForSubject(this, loadedNote.getSubject()));
        classChip.setText(ValuesTranslator.getTranslatedClassLevel(this, loadedNote.getClassLevel()));

        for (int i = 2; i < tagsContainer.getChildCount(); i++) {
            Chip tag = (Chip) tagsContainer.getChildAt(i);

            if ((i - 2) >= loadedNote.getTags().length) {
                tag.setVisibility(View.GONE);
                continue;
            }

            tag.setText(loadedNote.getTags()[i - 2]);
        }

        Glide
            .with(this)
            .load(SharedUtils.GetServer(this) + "/profile_pics/" + author.getProfilePicURL())
            .placeholder(R.drawable.account_circle_24px)
            .into(authorProfilePic)
        ;
        Glide
                .with(this)
                .load(SharedUtils.GetServer(this) + "/profile_pics/" + "michelelorusso05.jpg")
                .placeholder(R.drawable.account_circle_24px)
                .into(commentProfilePic)
        ;

        comment.setEndIconOnClickListener((v) -> {

        });

        Prism4jTheme codeBlockTheme =
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
                ? Prism4jThemeDarkula.create()
                : Prism4jThemeDefault.create()
        ;

        final GrammarLocator locator = new GrammarLocatorSourceCode();
        final Markwon markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                    @NonNull
                    @Override
                    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions
                                .transform(new RoundedCorners((int) (16.0f * getResources().getDisplayMetrics().density)))
                        ;

                        return Glide
                                .with(ReaderActivity.this)
                                .load(drawable.getDestination())
                                .apply(requestOptions);
                    }

                    @Override
                    public void cancel(@NonNull Target<?> target) {
                        Glide.with(ReaderActivity.this).clear(target);
                    }
                }))
                .usePlugin(SyntaxHighlightPlugin.create(new Prism4j(locator), codeBlockTheme))
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(JLatexMathPlugin.create(markdownView.getTextSize(), builder -> {
                    // ENABLE inlines
                    builder.inlinesEnabled(true);
                }))
                .usePlugin(HtmlPlugin.create())
                .build();

        markwon.setMarkdown(markdownView, loadedNote.getMarkdown());

        ArrayList<Content> images = new ArrayList<>();
        ArrayList<Content> videos = new ArrayList<>();
        ArrayList<Content> documents = new ArrayList<>();

        for (Content c : loadedNote.getContents()) {
            switch (c.getType()) {
                case "image":
                    images.add(c);
                    break;
                case "video":
                    videos.add(c);
                    break;
                case "document":
                    documents.add(c);
                    break;
            }
        }

        if (images.isEmpty())
            imagesContainer.setVisibility(View.GONE);
        if (videos.isEmpty())
            videosContainer.setVisibility(View.GONE);
        if (documents.isEmpty())
            documentsContainer.setVisibility(View.GONE);

        imagesRecyclerView.setAdapter(new ContentAdapter(this, loadedNote.getId(), images, this::DownloadFile));
        videosRecyclerView.setAdapter(new ContentAdapter(this, loadedNote.getId(), videos, this::DownloadFile));
        documentsRecyclerView.setAdapter(new ContentAdapter(this, loadedNote.getId(), documents, this::DownloadFile));
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
        }
        else
            registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(onDownloadComplete);
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (callbackMap.containsKey(id)) {
                DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);

                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);

                    ContentAdapter.OnDownloadFinishedCallback callback = callbackMap.remove(id);
                    assert callback != null;

                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
                        String uriString = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIAPROVIDER_URI));
                        String mimeType = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE));

                        Uri uri = Uri.parse(uriString);

                        // FileProvider.getUriForFile(this, "cocolorussococo.cnouleg.fileprovider", new File(uri));

                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        openFileIntent.setDataAndType(uri, mimeType);

                        callback.execute(STATUS_DOWNLOAD_SUCCEDED, uri, mimeType);

                        startActivity(openFileIntent);
                    }
                    else {
                        callback.execute(STATUS_DOWNLOAD_FAILED, null, null);
                    }
                }
            }
        }
    };

    private void DownloadFile(String filename, ContentAdapter.OnDownloadFinishedCallback onDownloadEnd) {
        String url = SharedUtils.GetServer(this) + "/content/" + loadedNote.getId() + "/" + filename;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                queuedDownloadFilename = filename;
                queuedDownloadCallback = onDownloadEnd;

                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.drawable.folder_24px)
                        .setTitle(R.string.allow_storage_title)
                        .setMessage(R.string.allow_storage_desc)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        .show();
                return;
            }
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                .setTitle(filename);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1);
            String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

            request
                    .setMimeType(mimetype)
                    .allowScanningByMediaScanner();
        }

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        long downloadId = dm.enqueue(request);

        callbackMap.put(downloadId, onDownloadEnd);
    }
}