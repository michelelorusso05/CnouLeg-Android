package com.test.cnouleg;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;
import java.util.Collection;

public class ContentUploadAdapter extends RecyclerView.Adapter<ContentUploadAdapter.ViewHolder> {
    Context context;
    ArrayList<Uri> contents;
    Runnable loadData;
    final String contentType;
    final int maxContents;

    public ContentUploadAdapter(Context ctx, Runnable openContent, String content, int max) {
        context = ctx;
        loadData = openContent;
        contentType = content;
        maxContents = max;

        contents = new ArrayList<>(maxContents);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_content, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == contents.size()) {
            holder.preview.setImageResource(R.drawable.add_24px);

            holder.clickableLayout.setOnClickListener((v) -> loadData.run());

            holder.name.setText(R.string.action_add);
            return;
        }

        Uri current = contents.get(position);

        holder.name.setText(SharedUtils.GetFilenameFromURI(context, current));

        switch (contentType) {
            case "image":
                Glide
                        .with(context)
                        .load(current)
                        .centerCrop()
                        .thumbnail(0.4f)
                        .placeholder(R.drawable.image_24px)
                        .into(holder.preview)
                ;

                holder.clickableLayout.setOnClickListener((v) -> {

                });
                break;
            case "video":
                holder.preview.setImageResource(R.drawable.video_24px);

                holder.clickableLayout.setOnClickListener((v) -> {

                });
                break;
            case "document":
                holder.preview.setImageResource(R.drawable.document_24px);
                holder.clickableLayout.setOnClickListener((v) -> {

                });
                break;
        }
    }
    @SuppressWarnings("unused")
    public void AddData(Uri uri) {
        int ptr = contents.size();
        if (ptr >= maxContents) return;

        contents.add(uri);

        if (contents.size() == maxContents)
            notifyItemChanged(ptr);
        else
            notifyItemInserted(ptr);
    }
    public void AddData(Collection<Uri> uris) {
        int ptr = contents.size();
        int added = 0;

        for (Uri uri : uris) {
            if (ptr + added >= maxContents) break;

            contents.add(uri);
            added++;
        }

        if (contents.size() == maxContents) {
            notifyItemRangeInserted(ptr, added - 1);
            notifyItemChanged(maxContents - 1);
        }
        else {
            notifyItemRangeInserted(ptr, added);
        }
    }

    @Override
    public int getItemCount() {
        return (contents.size() == maxContents) ? contents.size() : contents.size() + 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        View clickableLayout;
        ShapeableImageView preview;
        CircularProgressIndicator progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contentName);
            preview = itemView.findViewById(R.id.preview);
            progressBar = itemView.findViewById(R.id.progressBar);

            clickableLayout = itemView.findViewById(R.id.rowLayout);
        }
    }
}
