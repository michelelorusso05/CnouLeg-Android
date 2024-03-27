package com.test.cnouleg;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.test.cnouleg.utils.SharedUtils;

import java.util.ArrayList;

public class ActivityVideo extends FullscreenActivity {
    String noteID;
    String[] contents;
    ExoPlayer player;
    int position;
    long elapsedVideoMs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
            savedInstanceState = getIntent().getExtras();

        assert savedInstanceState != null;

        noteID = savedInstanceState.getString("noteID");
        contents = savedInstanceState.getStringArray("contents");
        position = savedInstanceState.getInt("startVideo", 0);
        elapsedVideoMs = savedInstanceState.getLong("elapsedVideoMs", 0);
    }

    @OptIn(markerClass = UnstableApi.class) @Override
    protected void onStart() {
        super.onStart();
        if (player != null)
            return;

        player = new ExoPlayer.Builder(this).build();

        PlayerView playerView = findViewById(R.id.playerView);
        playerView.setPlayer(player);

        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility ->
                onTouch.accept(visibility != View.VISIBLE));

        ArrayList<MediaItem> mediaItems = new ArrayList<>(contents.length);
        for (String content : contents) {
            mediaItems.add(MediaItem.fromUri(SharedUtils.GetServer(this) + "/content/" + noteID + "/" + content));
        }

        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.setPauseAtEndOfMediaItems(true);
        player.setMediaItems(mediaItems, position, elapsedVideoMs);
        player.prepare();
        player.play();

        playerView.hideController();
    }



    @Override
    protected void onStop() {
        super.onStop();

        if (player == null) return;

        position = player.getCurrentMediaItemIndex();
        elapsedVideoMs = player.getContentPosition();

        player.release();

        player = null;
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_video;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteID", noteID);
        outState.putStringArray("contents", contents);
        outState.putInt("startVideo", position);
        outState.putLong("elapsedVideoMs", elapsedVideoMs);
    }
}