package com.test.cnouleg;

import static android.content.Context.DOWNLOAD_SERVICE;

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
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
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
import com.test.cnouleg.api.Content;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.ValuesTranslator;
import com.test.cnouleg.utils.SharedUtils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.latex.JLatexMathPlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
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

@PrismBundle(includeAll = true, grammarLocatorClassName = ".GrammarLocatorBundle")
public class FragmentReader extends Fragment {
    public static final String TAG = FragmentReader.class.getSimpleName();

    Context context;
    TextView markdownView;
    TextView title, authorView, dateView;
    TextView ratingsView, ratingsCountView;
    ChipGroup tagsContainer;
    Chip subjectChip;
    Chip classChip;
    ShapeableImageView authorProfilePic;

    Note loadedNote;
    Profile author;
    RatingBar ratingBar;
    RecyclerView imagesRecyclerView, videosRecyclerView, documentsRecyclerView;
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


    public FragmentReader() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();

        if (savedInstanceState == null)
            savedInstanceState = getArguments();

        assert savedInstanceState != null;

        loadedNote = SharedUtils.GetParcelable(savedInstanceState, "note", Note.class);
        author = SharedUtils.GetParcelable(savedInstanceState, "author", Profile.class);

        assert loadedNote != null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("note", loadedNote);
        outState.putParcelable("author", author);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader, container, false);

        markdownView = view.findViewById(R.id.markdown);
        title = view.findViewById(R.id.titleView);
        authorView = view.findViewById(R.id.authorView);
        dateView = view.findViewById(R.id.postDate);
        ratingsView = view.findViewById(R.id.rating_text_view);
        ratingsCountView = view.findViewById(R.id.rating_count_text_view);
        subjectChip = view.findViewById(R.id.subject_chip);
        classChip = view.findViewById(R.id.class_chip);
        tagsContainer = view.findViewById(R.id.tagsContainer);
        authorProfilePic = view.findViewById(R.id.author_profile_pic);
        ratingBar = new RatingBar(view.findViewById(R.id.ratingButtonsContainer));

        imagesRecyclerView = view.findViewById(R.id.imagesRecyclerView);
        videosRecyclerView = view.findViewById(R.id.videosRecyclerView);
        documentsRecyclerView = view.findViewById(R.id.documentsRecyclerView);

        imagesContainer = view.findViewById(R.id.imagesContainer);
        videosContainer = view.findViewById(R.id.videosContainer);
        documentsContainer = view.findViewById(R.id.documentsContainer);

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        documentsRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        title.setText(loadedNote.getTitle());
        authorView.setText(author.getUsername());

        Date date = null;
        try {
            date = SharedUtils.dateFormat.parse(loadedNote.getUploadDate());
        } catch (ParseException ignored) {}

        java.text.DateFormat format = DateFormat.getDateFormat(context);

        dateView.setText(getString(R.string.posted_on, date == null ? "" : format.format(date)));
        ratingsView.setText(String.valueOf(loadedNote.getAverageRating()));
        ratingsCountView.setText(String.valueOf(loadedNote.getNumberOfRatings()));
        subjectChip.setText(ValuesTranslator.getTranslatedSubject(context, loadedNote.getSubject()));
        subjectChip.setChipIcon(ValuesTranslator.getDrawableForSubject(context, loadedNote.getSubject()));
        classChip.setText(ValuesTranslator.getTranslatedClassLevel(context, loadedNote.getClassLevel()));

        for (int i = 2; i < tagsContainer.getChildCount(); i++) {
            Chip tag = (Chip) tagsContainer.getChildAt(i);

            if (loadedNote.getTags() == null || (i - 2) >= loadedNote.getTags().length) {
                tag.setVisibility(View.GONE);
                continue;
            }

            tag.setText(loadedNote.getTags()[i - 2]);
        }

        if (author.getProfilePicURL() != null && !author.getProfilePicURL().isEmpty()) {
            Glide
                .with(this)
                .load(SharedUtils.GetServer(context) + "/profile_pics/" + author.getProfilePicURL())
                .placeholder(R.drawable.account_circle_24px)
                .into(authorProfilePic)
            ;
        }

        Prism4jTheme codeBlockTheme =
            (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
                ? Prism4jThemeDarkula.create()
                : Prism4jThemeDefault.create()
            ;

        final GrammarLocator locator = new GrammarLocatorBundle();
        final Markwon markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                    @NonNull
                    @Override
                    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions
                                .transform(new RoundedCorners((int) (16.0f * getResources().getDisplayMetrics().density)))
                        ;

                        return Glide
                                .with(context)
                                .load(drawable.getDestination())
                                .apply(requestOptions);
                    }

                    @Override
                    public void cancel(@NonNull Target<?> target) {
                        Glide.with(context).clear(target);
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

        imagesRecyclerView.setAdapter(new ContentAdapter(context, loadedNote.getId(), images, this::DownloadFile));
        videosRecyclerView.setAdapter(new ContentAdapter(context, loadedNote.getId(), videos, this::DownloadFile));
        documentsRecyclerView.setAdapter(new ContentAdapter(context, loadedNote.getId(), documents, this::DownloadFile));

        return view;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
        }
        else
            requireActivity().registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().unregisterReceiver(onDownloadComplete);
    }

    private void DownloadFile(String filename, ContentAdapter.OnDownloadFinishedCallback onDownloadEnd) {
        String url = SharedUtils.GetServer(context) + "/content/" + loadedNote.getId() + "/" + filename;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!(ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                queuedDownloadFilename = filename;
                queuedDownloadCallback = onDownloadEnd;

                new MaterialAlertDialogBuilder(context)
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

        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        long downloadId = dm.enqueue(request);

        callbackMap.put(downloadId, onDownloadEnd);
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (callbackMap.containsKey(id)) {
                DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
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

                        Uri uri;

                        // Failsafe: should not be happening. Covers some edge cases in Android 6.0.1, 7.0, 7.1
                        if (uriString == null) {
                            uriString = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                            String temp = Uri.parse(uriString).getPath();
                            assert temp != null;

                            File file = new File(temp);
                            uri = FileProvider.getUriForFile(context, "cocolorussococo.cnouleg.fileprovider", file);
                        }
                        else {
                            uri = Uri.parse(uriString);
                        }

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
}