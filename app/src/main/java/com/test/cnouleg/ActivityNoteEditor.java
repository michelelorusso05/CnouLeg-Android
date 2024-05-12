package com.test.cnouleg;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.IconArrayAdapter;
import com.test.cnouleg.utils.InputStreamRequestBody;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.ext.latex.JLatexMathPlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.syntax.Prism4jTheme;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityNoteEditor extends AppCompatActivity {
    FloatingActionButton sendFab;
    EditText markdownEditText;
    TextView markdownPreview;
    View editorToolbarContainer;
    CircularProgressIndicator progressBar;
    MaterialButton formatHeaderButton, formatBoldButton, formatItalicButton,
                    formatStrikethroughButton, formatUListButton, formatOListButton,
                    formatImageButton, formatLinkButton, formatQuotationButton,
                    formatMonospaceButton, formatCodeButton, formatTableButton,
                    formatHorizontalRuleButton, formatEquationButton, toolbarDoneButton;

    TextInputLayout titleLayout, descriptionLayout, schoolLayout, subjectLayout, markdownLayout;
    AutoCompleteTextView schoolEditText, subjectEditText;
    String subject, school;
    EditText title, description, insertTags;
    ChipGroup tags;
    RecyclerView addedImages, addedVideos, addedDocuments;
    ContentUploadAdapter addedImagesAdapter, addedVideosAdapter, addedDocumentsAdapter;
    ActivityResultLauncher<PickVisualMediaRequest> pickImages =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), o ->
                    addedImagesAdapter.AddData(o));
    ActivityResultLauncher<String> pickImagesLegacy =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), o ->
                    addedImagesAdapter.AddData(o));
    ActivityResultLauncher<PickVisualMediaRequest> pickVideos =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), o ->
                    addedVideosAdapter.AddData(o));

    ActivityResultLauncher<String> pickVideosLegacy =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), o ->
                    addedVideosAdapter.AddData(o));

    ActivityResultLauncher<String> pickDocuments = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(),
            o -> addedDocumentsAdapter.AddData(o));

    Timer delayedUpdate;
    long lastUpdate;
    boolean shouldUpdate;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        editorToolbarContainer = findViewById(R.id.format_toolbar);
        editorToolbarContainer.setVisibility(View.GONE);

        progressBar = findViewById(R.id.progressBar);

        sendFab = findViewById(R.id.send_fab);
        sendFab.setOnClickListener((v) -> {
            if (Validate())
                SendNote();
        });

        titleLayout = findViewById(R.id.title_text_field);
        title = titleLayout.getEditText();
        assert title != null;

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                titleLayout.setErrorEnabled(false);
            }
        });

        descriptionLayout = findViewById(R.id.description_text_field);
        description = descriptionLayout.getEditText();
        assert description != null;

        schoolLayout = findViewById(R.id.school_text_field);
        schoolEditText = findViewById(R.id.schoolEditText);
        schoolEditText.setAdapter(new ArrayAdapter<>(this, R.layout.dropdown_item,
                getResources().getStringArray(R.array.class_level_strings)));
        schoolEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) HideKeyboard(v);
        });
        schoolEditText.setOnItemClickListener((parent, view, position, id) -> {
            school = getResources().getStringArray(R.array.class_level_values)[position];
            schoolLayout.setErrorEnabled(false);
        });

        subjectLayout = findViewById(R.id.subject_text_field);
        subjectEditText = findViewById(R.id.subjectEditText);
        subjectEditText.setAdapter(IconArrayAdapter.createFromRes(this, R.array.subject_values, R.array.subject_strings, R.array.subjects_icons));
        subjectEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) HideKeyboard(v);
        });
        subjectEditText.setOnItemClickListener((parent, view, position, id) -> {
            subject = getResources().getStringArray(R.array.subject_values)[position];
            subjectLayout.setErrorEnabled(false);
        });

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                descriptionLayout.setErrorEnabled(false);
            }
        });

        tags = findViewById(R.id.tagsContainer);

        insertTags = findViewById(R.id.tagEditText);
        insertTags.addTextChangedListener(new TextWatcher() {
            boolean tagsInserted = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.chars().anyMatch(value -> value == '\n' || value == '\t' || value == ',')) {
                    tagsInserted = true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (tagsInserted) {
                    tagsInserted = false;
                    String[] tagParts = s.toString().split("[\n\t,]+");
                    for (String tag : tagParts) {
                        if (tags.getChildCount() >= 5) break;

                        Chip tagView = (Chip) getLayoutInflater().inflate(R.layout.tag_chip, tags, false);
                        tagView.setText(tag);
                        tagView.setChipIcon(AppCompatResources.getDrawable(ActivityNoteEditor.this, R.drawable.cancel_24px));
                        tagView.setOnClickListener((v) -> tags.removeView(tagView));
                        tags.addView(tagView);
                    }

                    s.clear();
                }
            }
        });

        addedImages = findViewById(R.id.imagesRecyclerView);
        addedImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addedImagesAdapter = new ContentUploadAdapter(this, () -> {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
                pickImages.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
            }
            else {
                pickImagesLegacy.launch("image/*");
            }
        }, "image", 10);
        addedImages.setAdapter(addedImagesAdapter);

        addedVideos = findViewById(R.id.videosRecyclerView);
        addedVideos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addedVideosAdapter = new ContentUploadAdapter(this, () -> {
            if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
                pickVideos.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                    .build());
            }
            else {
                pickVideosLegacy.launch("video/*");
            }
        }, "video", 5);
        addedVideos.setAdapter(addedVideosAdapter);

        addedDocuments = findViewById(R.id.documentsRecyclerView);
        addedDocuments.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addedDocumentsAdapter = new ContentUploadAdapter(this, () ->
                pickDocuments.launch("*/*"), "document", 5);
        addedDocuments.setAdapter(addedDocumentsAdapter);

        markdownLayout = findViewById(R.id.markdown_text_field);
        markdownEditText = markdownLayout.getEditText();
        assert markdownEditText != null;

        markdownEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editorToolbarContainer.setVisibility(View.VISIBLE);
                // sendFab.setVisibility(View.GONE);
            }
            else {
                editorToolbarContainer.setVisibility(View.GONE);
                // sendFab.setVisibility(View.VISIBLE);
            }
        });

        markdownPreview = findViewById(R.id.markdown);

        Prism4jTheme codeBlockTheme =
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
                        ? Prism4jThemeDarkula.create()
                        : Prism4jThemeDefault.create()
                ;

        final GrammarLocator locator = new GrammarLocatorBundle();
        final Markwon markwon = Markwon.builder(this)
                .usePlugin(StrikethroughPlugin.create())
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
                                .with(ActivityNoteEditor.this)
                                .load(drawable.getDestination())
                                .apply(requestOptions);
                    }

                    @Override
                    public void cancel(@NonNull Target<?> target) {
                        Glide.with(ActivityNoteEditor.this).clear(target);
                    }
                }))
                .usePlugin(SyntaxHighlightPlugin.create(new Prism4j(locator), codeBlockTheme))
                .usePlugin(JLatexMathPlugin.create(markdownPreview.getTextSize(), builder -> {
                    // DISABLE inlines
                    builder.inlinesEnabled(false);
                }))
                .usePlugin(HtmlPlugin.create())
                .build();

        final MarkwonEditor editor = MarkwonEditor.create(markwon);

        // Update for unordered lists
        markdownEditText.addTextChangedListener(new TextWatcher() {
            boolean newlineInserted = false;
            boolean newlineRemove = false;
            int where = -1;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 3) return;
                if (count == 1 && s.charAt(start) == '\n') {
                    int pos = FindPrevCharIndex('\n', s, start) + 1;
                    if (start - pos < 2) return;
                    if ("- ".contentEquals(s.subSequence(pos, pos + 2))) {
                        if (Character.isWhitespace(s.charAt(pos + 2))) {
                            newlineRemove = true;
                            where = pos;
                        }
                        else {
                            newlineInserted = true;
                            where = start + 1;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (newlineInserted) {
                    newlineInserted = false;
                    s.insert(where, "- ");
                }
                else if (newlineRemove) {
                    newlineRemove = false;
                    s.replace(where, where + 2, "");
                }
            }
        });

        // Update for ordered lists
        markdownEditText.addTextChangedListener(new TextWatcher() {
            boolean newlineInserted = false;
            boolean newlineRemove = false;
            int where = -1;
            int whereEnd = -1;
            int prevNo = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 3) return;
                if (count == 1 && s.charAt(start) == '\n') {
                    int pos = FindPrevCharIndex('\n', s, start) + 1;
                    if (start - pos < 2) return;

                    Matcher m = OLIST_PATTERN.matcher(s.subSequence(pos, start));

                    if (m.find() && m.start() == 0) {
                        String num = m.group();
                        prevNo = Integer.parseInt(num.substring(0, num.length() - 2));

                        if (Character.isWhitespace(s.charAt(pos + num.length()))) {
                            newlineRemove = true;
                            where = pos;
                            whereEnd = pos + num.length();
                        }
                        else {
                            newlineInserted = true;
                            where = start + 1;
                            whereEnd = where;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (newlineInserted) {
                    newlineInserted = false;
                    s.insert(where, (prevNo + 1) + ". ");
                }
                else if (newlineRemove) {
                    newlineRemove = false;
                    s.replace(where, whereEnd, "");
                }
            }
        });

        // Update editor
        markdownEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                markdownLayout.setErrorEnabled(false);

                editor.preRender(markdownEditText.getText(), result ->
                        result.dispatchTo(markdownEditText.getText()));
            }
        });

        lastUpdate = System.currentTimeMillis();
        delayedUpdate = new Timer(true);
        delayedUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                // Timeout, update the Markdown view
                if (System.currentTimeMillis() > lastUpdate + 250 && shouldUpdate) {
                    runOnUiThread(() -> markwon.setMarkdown(markdownPreview, markdownEditText.getText().toString()));
                    shouldUpdate = false;
                }
            }
        }, 0, 100);

        // Update Markdown
        markdownEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Reset the timer
                lastUpdate = System.currentTimeMillis();
                shouldUpdate = true;
            }
        });

        formatHeaderButton = findViewById(R.id.format_header);
        formatBoldButton = findViewById(R.id.format_bold);
        formatItalicButton = findViewById(R.id.format_italic);
        formatStrikethroughButton = findViewById(R.id.format_strikethrough);
        formatUListButton = findViewById(R.id.format_ulist);
        formatOListButton = findViewById(R.id.format_olist);
        formatImageButton = findViewById(R.id.format_image);
        formatLinkButton = findViewById(R.id.format_link);
        formatQuotationButton = findViewById(R.id.format_quotation);
        formatMonospaceButton = findViewById(R.id.format_monospace);
        formatCodeButton = findViewById(R.id.format_code);
        formatTableButton = findViewById(R.id.format_table);
        formatHorizontalRuleButton = findViewById(R.id.format_horizontalrule);
        formatEquationButton = findViewById(R.id.format_function);
        toolbarDoneButton = findViewById(R.id.editor_done);

        formatHeaderButton.setOnClickListener((v) -> {
            PopupMenu popupMenu = new PopupMenu(this, formatHeaderButton);

            popupMenu.getMenuInflater().inflate(R.menu.insert_header_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                // Why Android
                if (menuItem.getItemId() == R.id.header1)
                    InsertHeader(1);
                else if (menuItem.getItemId() == R.id.header2)
                    InsertHeader(2);
                else if (menuItem.getItemId() == R.id.header3)
                    InsertHeader(3);
                else if (menuItem.getItemId() == R.id.header4)
                    InsertHeader(4);
                else if (menuItem.getItemId() == R.id.header5)
                    InsertHeader(5);
                else if (menuItem.getItemId() == R.id.header6)
                    InsertHeader(6);

                return true;
            });
            popupMenu.show();
        });
        formatBoldButton.setOnClickListener((v) -> Decorate("**"));
        formatItalicButton.setOnClickListener((v) -> Decorate("*"));
        formatStrikethroughButton.setOnClickListener((v) -> Decorate("~~"));
        formatUListButton.setOnClickListener(this::InsertUList);
        formatOListButton.setOnClickListener(this::InsertOList);
        formatImageButton.setOnClickListener(this::InsertImage);
        formatLinkButton.setOnClickListener(this::InsertLink);
        formatQuotationButton.setOnClickListener(this::InsertQuotationBlock);
        formatMonospaceButton.setOnClickListener((v) -> Decorate("`"));
        formatCodeButton.setOnClickListener(this::InsertCodeBlock);
        formatTableButton.setOnClickListener(this::InsertTable);
        formatHorizontalRuleButton.setOnClickListener(this::InsertHorizontalRule);
        formatEquationButton.setOnClickListener(this::InsertLatex);

        toolbarDoneButton.setOnClickListener((v) -> {
            markdownEditText.clearFocus();
            HideKeyboard(v);
        });

        // Restore state
        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras();
        }
        if (savedInstanceState == null)
            return;

        id = savedInstanceState.getString("id", null);
        title.setText(savedInstanceState.getString("title", ""));
        description.setText(savedInstanceState.getString("description", ""));
        subject = savedInstanceState.getString("subject");
        subjectEditText.setText(SharedUtils.GetMatchingString(this, R.array.subject_values, R.array.subject_strings, subject), false);
        school = savedInstanceState.getString("classLevel");
        schoolEditText.setText(SharedUtils.GetMatchingString(this, R.array.class_level_values, R.array.class_level_strings, school), false);

        String[] tagsArr = savedInstanceState.getStringArray("tags");
        if (tagsArr == null)
            tagsArr = new String[0];

        for (String tag : tagsArr) {
            if (tags.getChildCount() >= 5) break;

            Chip tagView = (Chip) getLayoutInflater().inflate(R.layout.tag_chip, tags, false);
            tagView.setText(tag);
            tagView.setChipIcon(AppCompatResources.getDrawable(ActivityNoteEditor.this, R.drawable.cancel_24px));
            tagView.setOnClickListener((v) -> tags.removeView(tagView));
            tags.addView(tagView);
        }

        markdownEditText.setText(savedInstanceState.getString("markdown", ""));

        if (id == null) {
            ArrayList<Uri> savedImages = SharedUtils.GetParcelableArrayList(savedInstanceState, "images", Uri.class);
            addedImagesAdapter.AddData(savedImages);
            ArrayList<Uri> savedVideos = SharedUtils.GetParcelableArrayList(savedInstanceState, "videos", Uri.class);
            addedVideosAdapter.AddData(savedVideos);
            ArrayList<Uri> savedDocuments = SharedUtils.GetParcelableArrayList(savedInstanceState, "documents", Uri.class);
            addedDocumentsAdapter.AddData(savedDocuments);
        }
        else {
            findViewById(R.id.imagesContainer).setVisibility(View.GONE);
            findViewById(R.id.videosContainer).setVisibility(View.GONE);
            findViewById(R.id.documentsContainer).setVisibility(View.GONE);
            findViewById(R.id.editNoteInfo).setVisibility(View.VISIBLE);
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("id", id);
        outState.putString("title", title.getText().toString());
        outState.putString("description", description.getText().toString());
        outState.putString("subject", subject);
        outState.putString("classLevel", school);

        String[] tagStrings = new String[tags.getChildCount()];
        for (int i = 0; i < tags.getChildCount(); i++) {
            tagStrings[i] = ((Chip) tags.getChildAt(i)).getText().toString();
        }
        outState.putStringArray("tags", tagStrings);

        outState.putString("markdown", markdownEditText.getText().toString());
        outState.putParcelableArrayList("images", addedImagesAdapter.contents);
        outState.putParcelableArrayList("videos", addedVideosAdapter.contents);
        outState.putParcelableArrayList("documents", addedDocumentsAdapter.contents);
    }

    private void HideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private int FindPrevCharIndex(Predicate<Character> p, CharSequence s, int start) {
        int pos = start;

        if (s.length() == 0) return -1;
        if (pos != 0) pos--;

        while (pos >= 0) {
            if (p.test(s.charAt(pos))) {
                return pos;
            }
            pos--;
        }

        return -1;
    }
    @SuppressWarnings("SameParameterValue")
    private int FindPrevCharIndex(char toFind, CharSequence s, int start) {
        return FindPrevCharIndex(character -> character == toFind, s, start);
    }
    private int FindNextCharIndex(Predicate<Character> p, CharSequence s, int start) {
        int pos = start;

        if (s.length() == 0) return -1;
        if (pos != 0) pos--;

        while (pos < s.length()) {
            if (p.test(s.charAt(pos))) {
                return pos;
            }
            pos++;
        }

        return -1;
    }
    @SuppressWarnings("unused")
    private int FindNextCharIndex(char toFind, CharSequence s, int start) {
        return FindNextCharIndex(character -> character.equals(toFind), s, start);
    }
    private void InsertHeader(int count) {
        Editable editable = markdownEditText.getText();

        int headerStartPos = FindPrevCharIndex('\n', editable, markdownEditText.getSelectionStart()) + 1;

        int existingHeaderEnd = headerStartPos;

        boolean headerStartFound = false;

        while (existingHeaderEnd < editable.length()) {
            if (editable.charAt(existingHeaderEnd) == ' ') {
                existingHeaderEnd++;
                if (headerStartFound)
                    break;
            }
            else if (editable.charAt(existingHeaderEnd) == '#') {
                headerStartFound = true;
                existingHeaderEnd++;
            }
            else {
                existingHeaderEnd = headerStartPos;
                break;
            }
        }

        StringBuilder toInsert = new StringBuilder(count + 1);
        for (int i = 0; i < count; i++)
            toInsert.append('#');
        toInsert.append(' ');

        markdownEditText.getText().replace(
                headerStartPos,
                existingHeaderEnd,
                toInsert.toString(),
                0,
                toInsert.length()
        );
    }

    private void Decorate(String chars) {
        Editable editable = markdownEditText.getText();
        if (markdownEditText.getSelectionEnd() != markdownEditText.getSelectionStart()) {
            int boundStart = markdownEditText.getSelectionStart();
            int boundEnd = markdownEditText.getSelectionEnd();
            editable.insert(boundEnd, chars);
            editable.insert(boundStart, chars);
        }
        else {
            int start = markdownEditText.getSelectionStart();

            int boundStart = (start == editable.length()) ? Math.max(editable.length() - 1, 0) :
                    FindPrevCharIndex(Character::isWhitespace, editable,
                    Character.isWhitespace(editable.charAt(start)) ? start + 1 : start) + 1;
            int boundEnd = FindNextCharIndex(Character::isWhitespace, editable, start);

            if (boundEnd == -1) boundEnd = editable.length();

            editable.insert(boundEnd, chars);
            editable.insert(boundStart, chars);
        }
    }

    private void InsertUList(View v) {
        Editable editable = markdownEditText.getText();

        int headerStartPos = FindPrevCharIndex('\n', editable, markdownEditText.getSelectionStart()) + 1;

        markdownEditText.getText().insert(
                headerStartPos,
                "- "
        );
    }

    private static final Pattern OLIST_PATTERN = Pattern.compile("[0-9]+\\. ");
    private void InsertOList(View v) {
        Editable editable = markdownEditText.getText();

        int headerStartPos = FindPrevCharIndex('\n', editable, markdownEditText.getSelectionStart()) + 1;
        int prevLineStartPos = FindPrevCharIndex('\n', editable, headerStartPos - 1) + 1;

        int num = 1;

        if (headerStartPos - prevLineStartPos >= 3) {
            String prevLine = editable.toString().substring(prevLineStartPos, headerStartPos - 1);
            Matcher m = OLIST_PATTERN.matcher(prevLine);
            if (m.find() && m.start() == 0) {
                String s = m.group();
                num = Integer.parseInt(s.substring(0, s.length() - 2)) + 1;
            }
        }

        markdownEditText.getText().insert(
                headerStartPos,
                num + ". "
        );
    }

    private void InsertImage(View v) {
        HideKeyboard(v);
        View layout = LayoutInflater.from(this).inflate(R.layout.dialog_insert_image, null);

        EditText altEditText = layout.findViewById(R.id.alt_text_edittext);
        EditText urlEditText = layout.findViewById(R.id.url_edittext);

        new MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setTitle(R.string.action_insert_image)
                .setIcon(R.drawable.format_image_24px)
                .setPositiveButton(R.string.action_insert, (dialogInterface, which) -> {
                    String toInsert = String.format("![%1$s](%2$s)", altEditText.getText(), urlEditText.getText());

                    markdownEditText.getText().replace(
                            markdownEditText.getSelectionStart(),
                            markdownEditText.getSelectionEnd(),
                            toInsert,
                            0,
                            toInsert.length()
                    );
                })
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.cancel())
                .show();
    }
    private void InsertLink(View v) {
        HideKeyboard(v);
        View layout = LayoutInflater.from(this).inflate(R.layout.dialog_insert_link, null);

        EditText showedTextEditText = layout.findViewById(R.id.showed_text_edittext);
        EditText urlEditText = layout.findViewById(R.id.url_edittext);

        new MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setTitle(R.string.action_insert_link)
                .setIcon(R.drawable.format_link_24px)
                .setPositiveButton(R.string.action_insert, (dialogInterface, which) -> {
                    String toInsert = String.format("[%1$s](%2$s)", showedTextEditText.getText(), urlEditText.getText());

                    markdownEditText.getText().replace(
                            markdownEditText.getSelectionStart(),
                            markdownEditText.getSelectionEnd(),
                            toInsert,
                            0,
                            toInsert.length()
                    );
                })
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.cancel())
                .show();
    }

    private void InsertQuotationBlock(View v) {
        Editable editable = markdownEditText.getText();

        int headerStartPos = FindPrevCharIndex('\n', editable, markdownEditText.getSelectionStart()) + 1;

        markdownEditText.getText().insert(
                headerStartPos,
                "> "
        );
    }

    int selected = 0;
    private void InsertCodeBlock(View v) {
        HideKeyboard(v);
        selected = 0;
        View layout = LayoutInflater.from(this).inflate(R.layout.dialog_insert_code_block, null);

        AutoCompleteTextView languageEditText = layout.findViewById(R.id.language_edittext);

        IconArrayAdapter adapter = IconArrayAdapter.createFromRes(this, R.array.programming_languages_values, R.array.programming_languages, R.array.programming_languages_drawables);
        languageEditText.setAdapter(adapter);
        languageEditText.setText((String) adapter.getItem(0), false);
        languageEditText.setOnItemClickListener((parent, view, position, id) ->
                selected = position);

        new MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setTitle(R.string.action_insert_code_block)
                .setIcon(R.drawable.format_code_24px)
                .setPositiveButton(R.string.action_insert, (dialogInterface, which) -> {
                    String toInsert = String.format("\n```%1$s\n\n```\n", adapter.getItemValue(selected));

                    markdownEditText.getText().replace(
                            markdownEditText.getSelectionStart(),
                            markdownEditText.getSelectionEnd(),
                            toInsert,
                            0,
                            toInsert.length()
                    );
                })
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.cancel())
                .show();
    }

    private void InsertTable(View v) {
        HideKeyboard(v);
        View layout = LayoutInflater.from(this).inflate(R.layout.dialog_insert_table, null);

        AutoCompleteTextView rowsEditText = layout.findViewById(R.id.rows_edittext);
        AutoCompleteTextView columnsEditText = layout.findViewById(R.id.columns_edittext);

        final Integer[] elements = { 1, 2, 3, 4, 5, 6, 7, 8 };

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, elements);

        rowsEditText.setAdapter(adapter);
        columnsEditText.setAdapter(adapter);

        rowsEditText.setText("2", false);
        columnsEditText.setText("2", false);

        new MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setTitle(R.string.action_insert_table)
                .setIcon(R.drawable.format_table_24px)
                .setPositiveButton(R.string.action_insert, (dialogInterface, which) -> {
                    int rows = Integer.parseInt(rowsEditText.getText().toString());
                    int columns = Integer.parseInt(columnsEditText.getText().toString());

                    StringBuilder toInsert = new StringBuilder("\n|");
                    for (int i = 0; i < columns; i++)
                        toInsert.append(getString(R.string.table_header, i + 1)).append("|");

                    toInsert.append("\n|");
                    for (int i = 0; i < columns; i++)
                        toInsert.append("----|");

                    for (int i = 0; i < rows; i++) {
                        toInsert.append("\n|");
                        for (int j = 0; j < columns; j++) {
                            toInsert.append(getString(R.string.table_content)).append("|");
                        }
                    }
                    toInsert.append("\n");

                    markdownEditText.getText().replace(
                            markdownEditText.getSelectionStart(),
                            markdownEditText.getSelectionEnd(),
                            toInsert.toString(),
                            0,
                            toInsert.length()
                    );
                })
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.cancel())
                .show();
    }

    private void InsertHorizontalRule(View v) {
        String toInsert = "\n---\n";

        markdownEditText.getText().replace(
                markdownEditText.getSelectionStart(),
                markdownEditText.getSelectionEnd(),
                toInsert,
                0,
                toInsert.length()
        );
    }

    private void InsertLatex(View v) {
        String toInsert = "\n$$\n\n$$\n";

        markdownEditText.getText().replace(
                markdownEditText.getSelectionStart(),
                markdownEditText.getSelectionEnd(),
                toInsert,
                0,
                toInsert.length()
        );
    }

    private boolean Validate() {
        boolean validate = true;

        if (title.getText().toString().trim().isEmpty()) {
            validate = false;
            titleLayout.setError(getString(R.string.error_empty_title));
        }
        if (description.getText().toString().trim().isEmpty()) {
            validate = false;
            descriptionLayout.setError(getString(R.string.error_empty_desc));
        }
        if (subject == null || subjectEditText.getText().toString().trim().isEmpty()) {
            validate = false;
            subjectLayout.setError(getString(R.string.error_empty_subject));
        }
        if (school == null || schoolEditText.getText().toString().trim().isEmpty()) {
            validate = false;
            schoolLayout.setError(getString(R.string.error_empty_class_level));
        }
        String mdText = markdownEditText.getText().toString().trim();
        if (mdText.isEmpty()) {
            validate = false;
            markdownLayout.setError(getString(R.string.error_empty_markdown));
        }
        else if (mdText.length() < 100) {
            validate = false;
            markdownLayout.setError((getString(R.string.error_markdown_too_short)));
        }

        return validate;
    }
    private void SendNote() {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title.getText().toString())
                .addFormDataPart("description", description.getText().toString())
                .addFormDataPart("subject", subject)
                .addFormDataPart("school", school)
                .addFormDataPart("markdown", markdownEditText.getText().toString());

        for (int i = 0; i < tags.getChildCount(); i++) {
            requestBodyBuilder.addFormDataPart("tags", ((Chip) tags.getChildAt(i)).getText().toString());
        }

        if (id == null) {
            for (Uri uri : addedImagesAdapter.contents) {
                requestBodyBuilder.addFormDataPart("images", SharedUtils.GetFilenameFromURI(this, uri),
                        new InputStreamRequestBody(getContentResolver(), uri));
            }

            for (Uri uri : addedVideosAdapter.contents) {
                requestBodyBuilder.addFormDataPart("videos", SharedUtils.GetFilenameFromURI(this, uri),
                        new InputStreamRequestBody(getContentResolver(), uri));
            }

            for (Uri uri : addedDocumentsAdapter.contents) {
                requestBodyBuilder.addFormDataPart("documents", SharedUtils.GetFilenameFromURI(this, uri),
                        new InputStreamRequestBody(getContentResolver(), uri));
            }
        }
        else {
            requestBodyBuilder.addFormDataPart("note_id", id);
        }

        String token = AccessTokenUtils.GetAccessToken(this);

        Request.Builder builder = new Request.Builder()
                .url(SharedUtils.GetServer(this) + "/api/notes/")
                .header("authorization", "Bearer " + token);

        if (id == null)
            builder.post(requestBodyBuilder.build());
        else
            builder.put(requestBodyBuilder.build());

        progressBar.setVisibility(View.VISIBLE);
        sendFab.setEnabled(false);

        StaticData.getClient().newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Snackbar.make(sendFab, R.string.error_generic_server, Snackbar.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    sendFab.setEnabled(true);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                int status = response.code();
                response.body().close();

                if (status == 401) {
                    runOnUiThread(() -> Snackbar.make(sendFab, R.string.error_authentication_error, Snackbar.LENGTH_SHORT).show());
                }
                else {
                    finish();
                }

                runOnUiThread(() -> {
                    sendFab.setEnabled(true);
                    progressBar.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}