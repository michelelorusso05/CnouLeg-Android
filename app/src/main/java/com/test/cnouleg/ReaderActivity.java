package com.test.cnouleg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.test.cnouleg.api.Author;
import com.test.cnouleg.api.Content;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.ValuesTranslator;
import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.latex.JLatexMathPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;


@PrismBundle(includeAll = true, grammarLocatorClassName = ".GrammarLocatorSourceCode")
public class ReaderActivity extends AppCompatActivity {
    TextView markdownView;
    TextView title;
    TextView authorView;
    ChipGroup tagsContainer;
    Chip subjectChip;
    Chip classChip;
    ShapeableImageView authorProfilePic;

    Note loadedNote;
    Author author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        markdownView = findViewById(R.id.markdown);
        title = findViewById(R.id.titleView);
        authorView = findViewById(R.id.authorView);
        subjectChip = findViewById(R.id.subject_chip);
        classChip = findViewById(R.id.class_chip);
        tagsContainer = findViewById(R.id.tagsContainer);
        authorProfilePic = findViewById(R.id.author_profile_pic);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null)
            savedInstanceState = getIntent().getExtras();

        assert savedInstanceState != null;

        savedInstanceState.setClassLoader(getClass().getClassLoader());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            loadedNote = savedInstanceState.getParcelable("note", Note.class);
            author = savedInstanceState.getParcelable("author", Author.class);
        }
        else {
            loadedNote = savedInstanceState.getParcelable("note");
            author = savedInstanceState.getParcelable("author");
        }

        assert loadedNote != null;

        title.setText(loadedNote.getTitle());
        authorView.setText(author.getName());
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

        final GrammarLocator locator = new GrammarLocatorSourceCode();
        final Markwon markwon = Markwon.builder(this)
                .usePlugin(TablePlugin.create(this))
                .usePlugin(GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                    @NonNull
                    @Override
                    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions
                                .transform(new RoundedCorners(16))
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
                .usePlugin(SyntaxHighlightPlugin.create(new Prism4j(locator), Prism4jThemeDarkula.create()))
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(JLatexMathPlugin.create(markdownView.getTextSize(), builder -> {
                    // ENABLE inlines
                    builder.inlinesEnabled(true);
                }))
                .usePlugin(HtmlPlugin.create())
                .build();

        markwon.setMarkdown(markdownView, loadedNote.getMarkdown());

        ArrayList<String> images = new ArrayList<>();
        ArrayList<String> videos = new ArrayList<>();
        ArrayList<String> documents = new ArrayList<>();

        for (Content c : loadedNote.getContents()) {
            switch (c.getType()) {
                case "image":
                    images.add(c.getPath());
                    break;
                case "videos":
                    videos.add(c.getPath());
                    break;
                case "documents":
                    documents.add(c.getPath());
                    break;
            }
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("note", loadedNote);
        outState.putParcelable("author", author);
    }
}