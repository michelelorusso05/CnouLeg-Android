package com.test.cnouleg;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.Note;
import com.test.cnouleg.api.ValuesTranslator;
import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<Note> notes;
    HashMap<String, Profile> authorHashMap;
    Consumer<Integer> openFragment;
    private static final int VIEW_FRAG = 0;
    private static final int VIEW_NOTE = 1;
    public SearchAdapter(Context ctx, Consumer<Integer> o) {
        context = ctx;
        notes = new ArrayList<>();
        authorHashMap = new HashMap<>();
        openFragment = o;
    }

    private int getStart() {
        return openFragment != null ? 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (openFragment != null && position == 0)
            return VIEW_FRAG;

        return VIEW_NOTE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_FRAG)
            return new FragmentHolder(LayoutInflater.from(context).inflate(R.layout.item_fragment, parent, false));

        return new NoteViewHolder(LayoutInflater.from(context).inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        if (getItemViewType(position) == VIEW_FRAG) {
            openFragment.accept(((FragmentHolder) h).fragmentContainer.getId());
            return;
        }

        NoteViewHolder holder = (NoteViewHolder) h;

        Note current = notes.get(position - getStart());
        Profile author = authorHashMap.get(current.getAuthorID());
        assert author != null;

        holder.title.setText(current.getTitle());
        holder.author.setText(author.getUsername());
        holder.description.setText(current.getDescription());
        holder.classLevel.setText(ValuesTranslator.getTranslatedClassLevel(context, current.getClassLevel()));
        holder.subject.setText(ValuesTranslator.getTranslatedSubject(context, current.getSubject()));
        if (current.getAverageRating() == 0) {
            holder.ratings.setText("--");
        }
        else {
            holder.ratings.setText(String.valueOf(current.getAverageRating()).substring(0, 3));
        }

        String server = SharedUtils.GetServer(context);

        if (author.getProfilePicURL() != null && !author.getProfilePicURL().isEmpty()) {
            Glide
                .with(context)
                .load(server + "/profile_pics/" + author.getProfilePicURL())
                .placeholder(R.drawable.account_circle_24px)
                .into(holder.picThumb)
            ;
        }

        holder.clickableLayout.setOnClickListener((v) -> {
            Intent activity = new Intent(context, ActivityReader.class);
            activity.putExtra("note", notes.get(position - getStart()));
            activity.putExtra("author", author);
            context.startActivity(activity);
        });
    }

    @Override
    public int getItemCount() {
        return notes.size() + getStart();
    }

    public void ReplaceNotes(Collection<Note> n, HashMap<String, Profile> a) {
        int oldSize = notes.size();
        int newSize = n.size();
        int delta = n.size() - notes.size();

        notes.clear();
        authorHashMap.clear();

        notes.addAll(n);
        authorHashMap.putAll(a);

        if (delta < 0) {
            notifyItemRangeRemoved(getStart() + newSize, -delta);

            if (newSize > 0)
                notifyItemRangeChanged(getStart(), newSize);
        }
        if (delta == 0) {
            notifyItemRangeChanged(getStart(), newSize);
        }
        if (delta > 0) {
            notifyItemRangeInserted(getStart() + newSize, delta);

            if (oldSize > 0)
                notifyItemRangeChanged(getStart(), newSize);
        }
    }
    public void AddNotes(Collection<Note> n, HashMap<String, Profile> a) {
        int prevEnd = getItemCount();
        notes.addAll(n);
        authorHashMap.putAll(a);

        notifyItemRangeInserted(prevEnd, n.size());
    }
    public void ClearNotes() {
        int count = notes.size();

        notes.clear();
        authorHashMap.clear();

        notifyItemRangeRemoved(1, count);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView title, author, description;
        Chip classLevel, subject, ratings;
        View clickableLayout;
        ShapeableImageView picThumb;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            author = itemView.findViewById(R.id.card_author);
            description = itemView.findViewById(R.id.description);
            classLevel = itemView.findViewById(R.id.class_chip);
            subject = itemView.findViewById(R.id.subject_chip);
            ratings = itemView.findViewById(R.id.ratings_chip);
            picThumb = itemView.findViewById(R.id.author_profile_pic);

            clickableLayout = itemView.findViewById(R.id.rowLayout);
        }
    }

    static class FragmentHolder extends RecyclerView.ViewHolder {
        FrameLayout fragmentContainer;
        FragmentHolder(View itemView) {
            super(itemView);
            fragmentContainer = itemView.findViewById(R.id.fragment_container_adapter);
        }
    }
}
