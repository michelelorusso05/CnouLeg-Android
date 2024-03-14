package com.test.cnouleg;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.test.cnouleg.api.Author;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.ValuesTranslator;
import com.test.cnouleg.utils.SharedUtils;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    Context context;
    Note[] notes;
    HashMap<Integer, Author> authorHashMap;
    public SearchAdapter(Context ctx, Note[] a, HashMap<Integer, Author> authors) {
        context = ctx;
        notes = a;
        authorHashMap = authors;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_article, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note current = notes[position];
        Author author = authorHashMap.get(current.getAuthorID());
        assert author != null;


        holder.title.setText(current.getTitle());
        holder.author.setText(author.getName());
        holder.description.setText(current.getDescription());
        holder.classLevel.setText(ValuesTranslator.getTranslatedClassLevel(context, current.getClassLevel()));
        holder.subject.setText(ValuesTranslator.getTranslatedSubject(context, current.getSubject()));

        String server = SharedUtils.GetServer(context);

        Glide
            .with(context)
            .load(server + "/profile_pics/" + author.getProfilePicURL())
            .placeholder(R.drawable.account_circle_24px)
            .into(holder.picThumb)
        ;

        holder.clickableLayout.setOnClickListener((v) -> {
            Intent activity = new Intent(context, ReaderActivity.class);
            activity.putExtra("note", notes[position]);
            activity.putExtra("author", author);
            context.startActivity(activity);
        });
    }

    @Override
    public int getItemCount() {
        return notes.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, author, description;
        Chip classLevel, subject;
        View clickableLayout;
        ShapeableImageView picThumb;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            author = itemView.findViewById(R.id.card_author);
            description = itemView.findViewById(R.id.description);
            classLevel = itemView.findViewById(R.id.class_chip);
            subject = itemView.findViewById(R.id.subject_chip);
            picThumb = itemView.findViewById(R.id.author_profile_pic);

            clickableLayout = itemView.findViewById(R.id.rowLayout);
        }
    }
}
