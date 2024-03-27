package com.test.cnouleg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;
import com.test.cnouleg.utils.SharedUtils;

public class GalleryPagesAdapter extends RecyclerView.Adapter<GalleryPagesAdapter.ViewHolder> {
    private final Context context;
    private final String noteID;
    private final String server;
    private final String[] paths;
    private final Runnable onClickListener;

    public GalleryPagesAdapter(Context context, String noteID, String[] paths, Runnable onClickListener) {
        this.context = context;
        this.noteID = noteID;
        this.paths = paths;
        this.onClickListener = onClickListener;

        server = SharedUtils.GetServer(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_page, parent, false);
        view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.image.SetOnSingleTapConfirmedEvent(onClickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide
            .with(context)
            .load(server + "/content/" + noteID + "/" + paths[position])
            .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return paths.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ZoomageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }
}
