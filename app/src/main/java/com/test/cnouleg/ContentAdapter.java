package com.test.cnouleg;

import android.content.Context;
import android.content.Intent;
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
import com.test.cnouleg.api.Content;
import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder> {
    Context context;
    ArrayList<Content> contents;
    DownloadMetadataWrapper[] contentStatus;
    String noteID;
    BiConsumer<String, OnDownloadFinishedCallback> onDownloadAction;

    public interface OnDownloadFinishedCallback {
        void execute(int statusCode, Uri file, String mime);
    }

    private static class DownloadMetadataWrapper {
        DownloadMetadataWrapper() {
            status = FragmentReader.STATUS_DOWNLOAD_NO_OP;
            file = null;
            mime = null;
        }
        public void setStatus(int status, Uri file, String mime) {
            this.status = status;
            this.file = file;
            this.mime = mime;
        }
        public int status;
        public Uri file;
        public String mime;
    }

    private static final String PAYLOAD = "status_update";


    public ContentAdapter(Context ctx, String id, ArrayList<Content> a, BiConsumer<String, OnDownloadFinishedCallback> onDownload) {
        context = ctx;
        noteID = id;
        contents = a;
        onDownloadAction = onDownload;

        contentStatus = new DownloadMetadataWrapper[contents.size()];
        for (int i = 0; i < contentStatus.length; i++) {
            contentStatus[i] = new DownloadMetadataWrapper();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_content, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Content current = contents.get(position);

        holder.name.setText(current.getPath());

        String server = SharedUtils.GetServer(context);

        switch (current.getType()) {
            case "image":
                Glide
                        .with(context)
                        .load(server + "/content/" + noteID + "/" + current.getPath())
                        .centerCrop()
                        .thumbnail(0.4f)
                        .placeholder(R.drawable.image_24px)
                        .into(holder.preview)
                ;

                holder.clickableLayout.setOnClickListener((v) -> {
                    String[] paths = new String[contents.size()];

                    for (int i = 0; i < paths.length; i++) {
                        paths[i] = contents.get(i).getPath();
                    }

                    Intent intent = new Intent(context, ActivityGallery.class);
                    intent.putExtra("noteID", noteID);
                    intent.putExtra("content", paths);
                    intent.putExtra("page", position);
                    context.startActivity(intent);
                });
                break;
            case "video":
                /*
                try {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(context, Uri.parse(Uri.encode(server + "/content/" + noteID + "/" + current.getPath())));



                    Bitmap b = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);
                    Glide
                            .with(context)
                            .load(b)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .thumbnail(0.4f)
                            .placeholder(R.drawable.video_24px)
                            .into(holder.preview)
                    ;

                    retriever.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */

                holder.preview.setImageResource(R.drawable.video_24px);

                holder.clickableLayout.setOnClickListener((v) -> {
                    String[] paths = new String[contents.size()];

                    for (int i = 0; i < paths.length; i++) {
                        paths[i] = contents.get(i).getPath();
                    }

                    Intent intent = new Intent(context, ActivityVideo.class);
                    intent.putExtra("noteID", noteID);
                    intent.putExtra("contents", paths);
                    intent.putExtra("startVideo", position);
                    context.startActivity(intent);
                });
                break;
            case "document":
                /*
                InsetDrawable drawable = new InsetDrawable(AppCompatResources.getDrawable(context, R.drawable.geography_24px), (int) (16.0f * context.getResources().getDisplayMetrics().density));
                holder.preview.setImageDrawable(drawable);
                */
                holder.preview.setImageResource(R.drawable.document_24px);
                holder.clickableLayout.setOnClickListener((v) -> {
                    contentStatus[position].status = FragmentReader.STATUS_DOWNLOAD_RUNNING;
                    notifyItemChanged(position, PAYLOAD);
                    onDownloadAction.accept(contents.get(position).getPath(),
                        (status, uri, mime) -> {
                            contentStatus[position].setStatus(status, uri, mime);
                            notifyItemChanged(position, PAYLOAD);
                        });
                });
                break;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position);
        else if (payloads.contains(PAYLOAD)) {
            switch (contentStatus[position].status) {
                case FragmentReader.STATUS_DOWNLOAD_RUNNING:
                    holder.preview.setImageAlpha(102);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    break;
                case FragmentReader.STATUS_DOWNLOAD_SUCCEDED:
                    holder.clickableLayout.setOnClickListener((v) -> {
                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        openFileIntent.setDataAndType(contentStatus[position].file, contentStatus[position].mime);
                        context.startActivity(openFileIntent);
                    });
                case FragmentReader.STATUS_DOWNLOAD_NO_OP:
                default:
                    holder.preview.setImageAlpha(255);
                    holder.progressBar.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
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
