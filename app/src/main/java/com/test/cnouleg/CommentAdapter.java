package com.test.cnouleg;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.test.cnouleg.api.Profile;
import com.test.cnouleg.api.ProfileResults;
import com.test.cnouleg.api.Comment;
import com.test.cnouleg.api.CommentResults;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Activity context;
    ArrayList<Comment> comments;
    HashMap<String, Profile> authorHashMap;
    HashMap<String, Integer> commentReplyLoadingStatus;
    Consumer<Integer> openFragment;
    BiConsumer<String, String> onReply;

    ShapeAppearanceModel topCard;
    ShapeAppearanceModel bottomCard;
    ShapeAppearanceModel singleCard;
    ShapeAppearanceModel middleCard;
    private final float PIXELS_TO_DP;

    private static final int VIEW_FRAG = 0;
    private static final int VIEW_COMMENT = 1;
    private static final int VIEW_REPLY = 2;

    private static final int LOADING_REPLIES = 1;
    private static final int LOADED_REPLIES = 2;

    private static final String PAYLOAD = "status_update";

    public CommentAdapter(Activity ctx, Consumer<Integer> o, BiConsumer<String, String> re) {
        context = ctx;
        comments = new ArrayList<>();
        authorHashMap = new HashMap<>();
        commentReplyLoadingStatus = new HashMap<>();
        openFragment = o;
        onReply = re;

        Resources r = context.getResources();
        PIXELS_TO_DP = r.getDisplayMetrics().density;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_FRAG;
        else {
            return (comments.get(position - 1).getParentID() != null) ? VIEW_REPLY : VIEW_COMMENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_FRAG:
                return new FragmentHolder(LayoutInflater.from(context).inflate(R.layout.item_fragment, parent, false));
            case VIEW_COMMENT:
            {
                CommentHolder commentHolder = new CommentHolder(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false));

                // Init custom shape appearence models
                if (topCard == null)
                {
                    ShapeAppearanceModel.Builder model = commentHolder.cardView.getShapeAppearanceModel().toBuilder();

                    middleCard = model.build();
                    model
                            .setTopLeftCornerSize(16.0f * PIXELS_TO_DP)
                            .setTopRightCornerSize(16.0f * PIXELS_TO_DP);

                    topCard = model.build();
                    model
                            .setBottomLeftCornerSize(16.0f * PIXELS_TO_DP)
                            .setBottomRightCornerSize(16.0f * PIXELS_TO_DP);

                    singleCard = model.build();
                    model
                            .setTopLeftCornerSize(0)
                            .setTopRightCornerSize(0);

                    bottomCard = model.build();
                }

                commentHolder.loadRepliesButton.setOnClickListener((v) ->
                        loadReplies(commentHolder.getBindingAdapterPosition()));

                commentHolder.replyButton.setOnClickListener((v) -> {
                    if (onReply != null) {
                        Comment c = comments.get(commentHolder.getBindingAdapterPosition() - 1);
                        String parentID = c.getParentID() != null ? c.getParentID() : c.getId();
                        Profile author = authorHashMap.get(c.getUserID());
                        assert author != null;
                        onReply.accept(author.getUsername(), parentID);
                    }
                });

                return commentHolder;
            }
            case VIEW_REPLY:
                CommentHolder commentHolder = new CommentHolder(LayoutInflater.from(context).inflate(R.layout.item_comment_reply, parent, false));

                commentHolder.replyButton.setOnClickListener((v) -> {
                    if (onReply != null) {
                        Comment c = comments.get(commentHolder.getBindingAdapterPosition() - 1);
                        String parentID = c.getParentID() != null ? c.getParentID() : c.getId();
                        Profile author = authorHashMap.get(c.getUserID());
                        assert author != null;
                        onReply.accept(author.getUsername(), parentID);
                    }
                });

                return commentHolder;
        }
        throw new IllegalArgumentException("Invalid viewType");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_FRAG) {
            openFragment.accept(((FragmentHolder) holder).fragmentContainer.getId());
        }
        else {
            CommentHolder commentHolder = (CommentHolder) holder;

            Comment comment = comments.get(position - 1);
            Profile author = authorHashMap.get(comment.getUserID());

            assert author != null;

            // Set rounded corners appropriately
            if (getItemViewType(position) == VIEW_COMMENT) {
                // Main comment AND doesn't have subcomments
                if (position >= comments.size() || getItemViewType(position + 1) == VIEW_COMMENT)
                    commentHolder.cardView.setShapeAppearanceModel(singleCard);
                // Main comment AND has subcomments
                else
                    commentHolder.cardView.setShapeAppearanceModel(topCard);
            }
            else {
                // Subcomment AND it's the last one
                if (position >= comments.size() || getItemViewType(position + 1) == VIEW_COMMENT)
                    commentHolder.cardView.setShapeAppearanceModel(bottomCard);
                // Subcomment but there are more after this one
                else
                    commentHolder.cardView.setShapeAppearanceModel(middleCard);
            }

            commentHolder.authorView.setText(author.getUsername());
            commentHolder.contentView.setText(comment.getText());
            commentHolder.likesView.setText(SharedUtils.formatPowerOrder(comment.getLikes()));
            commentHolder.dateView.setText(SharedUtils.FormatDateLocale(context, comment.getDate()));

            // Main comment AND has subcomments
            if (comment.hasChildren()) {
                Integer status = commentReplyLoadingStatus.get(comment.getId());

                if (status == null) {
                    commentHolder.progressBar.setVisibility(View.GONE);
                    commentHolder.loadRepliesButton.setVisibility(View.VISIBLE);
                    commentHolder.loadRepliesButton.setEnabled(true);
                }
                else if (status == LOADING_REPLIES) {
                    commentHolder.progressBar.setVisibility(View.VISIBLE);
                    commentHolder.loadRepliesButton.setVisibility(View.VISIBLE);
                    commentHolder.loadRepliesButton.setEnabled(false);
                }
                else if (status == LOADED_REPLIES) {
                    commentHolder.progressBar.setVisibility(View.GONE);
                    commentHolder.loadRepliesButton.setVisibility(View.GONE);
                }
            }
            // Main comment but doesn't have subcomments
            else if (getItemViewType(position) == VIEW_COMMENT)
                commentHolder.loadRepliesButton.setVisibility(View.GONE);

            String server = SharedUtils.GetServer(context);

            if (author.getProfilePicURL() != null && !author.getProfilePicURL().isEmpty()) {
                Glide
                    .with(context)
                    .load(server + "/profile_pics/" + author.getProfilePicURL())
                    .placeholder(R.drawable.account_circle_24px)
                    .into(commentHolder.authorProfilePic)
                ;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position);
        else if (payloads.contains(PAYLOAD)) {
            Comment comment = comments.get(position - 1);
            CommentHolder commentHolder = (CommentHolder) holder;

            Integer status = commentReplyLoadingStatus.get(comment.getId());

            if (status == null) {
                commentHolder.progressBar.setVisibility(View.GONE);
                commentHolder.loadRepliesButton.setVisibility(View.VISIBLE);
                commentHolder.loadRepliesButton.setEnabled(true);
            }
            else if (status == LOADING_REPLIES) {
                commentHolder.progressBar.setVisibility(View.VISIBLE);
                commentHolder.loadRepliesButton.setVisibility(View.VISIBLE);
                commentHolder.loadRepliesButton.setEnabled(false);
            }
            else if (status == LOADED_REPLIES) {
                commentHolder.progressBar.setVisibility(View.GONE);
                commentHolder.loadRepliesButton.setVisibility(View.GONE);
            }

            if (position >= comments.size() || getItemViewType(position + 1) == VIEW_COMMENT)
                commentHolder.cardView.setShapeAppearanceModel(singleCard);
            else
                commentHolder.cardView.setShapeAppearanceModel(topCard);
        }
    }

    public int addComment(Comment c, Profile a) {
        int pos = 0;

        if (c.getParentID() != null && !c.getParentID().isEmpty()) {
            boolean foundParent = false;
            pos = comments.size();

            for (int i = 0; i < comments.size(); i++) {
                Comment comment = comments.get(i);
                if (!foundParent && comment.getId().equals(c.getParentID())) {
                    foundParent = true;
                }
                else if (foundParent && comment.getParentID() == null) {
                    pos = i;
                    break;
                }
            }

            if (!foundParent)
                throw new IllegalArgumentException("No parent with given ID");
        }

        comments.add(pos, c);
        authorHashMap.put(a.getId(), a);
        notifyItemInserted(pos + 1);

        return pos + 1;
    }
    public void addComments(Collection<Comment> c, HashMap<String, Profile> a) {
        comments.addAll(c);
        authorHashMap.putAll(a);
        notifyItemRangeInserted(1, c.size());
    }

    private synchronized void loadReplies(int position) {
        Comment parent = comments.get(position - 1);
        assert parent.hasChildren();

        commentReplyLoadingStatus.put(parent.getId(), LOADING_REPLIES);
        notifyItemChanged(position, PAYLOAD);

        new Thread(() -> {
            try {
                String srv = SharedUtils.GetServer(context);
                CommentResults commentResults = StaticData.getMapper().readValue(new URL(srv + "/api/comments/?parent_id=" + parent.getId()), CommentResults.class);

                StringBuilder b = new StringBuilder(srv + "/api/users/?");

                for (Comment comment : commentResults.getComments()) {
                    b.append("&include_id[]=").append(comment.getUserID());
                }

                ProfileResults authorResults = StaticData.getMapper().readValue(new URL(b.toString()), ProfileResults.class);
                HashMap<String, Profile> authorMapping = new HashMap<>(authorResults.getUsers().length);

                for (Profile author : authorResults.getUsers()) {
                    authorMapping.put(author.getId(), author);
                }

                context.runOnUiThread(() -> {
                    commentReplyLoadingStatus.put(parent.getId(), LOADED_REPLIES);

                    comments.addAll(position, Arrays.asList(commentResults.getComments()));
                    authorHashMap.putAll(authorMapping);
                    notifyItemRangeInserted(position + 1, commentResults.getComments().length);
                    notifyItemChanged(position, PAYLOAD);
                });
            } catch (IOException e) {
                context.runOnUiThread(() -> {
                    commentReplyLoadingStatus.remove(parent.getId());
                    notifyItemChanged(position, PAYLOAD);
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return comments.size() + 1;
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        ShapeableImageView authorProfilePic;
        TextView authorView, contentView, dateView, likesView;
        MaterialCardView cardView;
        Button replyButton, loadRepliesButton;
        CircularProgressIndicator progressBar;
        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            authorProfilePic = itemView.findViewById(R.id.author_profile_pic);
            authorView = itemView.findViewById(R.id.authorView);
            contentView = itemView.findViewById(R.id.content);
            dateView = itemView.findViewById(R.id.upload_date);
            likesView = itemView.findViewById(R.id.likes);
            cardView = itemView.findViewById(R.id.cardView);
            replyButton = itemView.findViewById(R.id.replyButton);
            loadRepliesButton = itemView.findViewById(R.id.loadMoreButton);
            progressBar = itemView.findViewById(R.id.progressBar);
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
