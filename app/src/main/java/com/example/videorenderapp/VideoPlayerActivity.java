package com.example.videorenderapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videorenderapp.model.MediaObject;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class VideoPlayerActivity extends AppCompatActivity {
    SimpleExoPlayer exoPlayer;
    PlayerView playerView;
    MediaObject object;
    FrameLayout frameLayout;
    Gson gson;

    int appNameStringRes = R.string.app_name;

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Objects.requireNonNull(getSupportActionBar()).hide();

        gson = new Gson();
        Intent receiveIntent = getIntent();
        String mediaJson = receiveIntent.getStringExtra("mediaJson");
        object = gson.fromJson(mediaJson, MediaObject.class);

        playerView = findViewById(R.id.pv_main);
        frameLayout = findViewById(R.id.frameLayout);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initPlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (Util.SDK_INT < 24 || exoPlayer == null) {
//            initPlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();

        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUi();


        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            hideSystemUi();

        }
    }


    private void initPlayer() {

        if (exoPlayer == null) {

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
            exoPlayer = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();

        }
        playerView.setPlayer(exoPlayer);

        String mediaURI;

        if (isFileAvailable(object)) {
            mediaURI = getApplicationContext().getFilesDir().getPath() + "/" + object.getMediaFileName();
            Log.i("Name",object.getMediaFileName()+"");

        } else {
            mediaURI = object.getMediaURL();
        }


        Uri uri = Uri.parse(mediaURI);
        MediaSource mediaSource = buildMediaSource(uri);

        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentWindow, playbackPosition);
        exoPlayer.prepare(mediaSource, false, false);


    }

    private boolean isFileAvailable(MediaObject mediaObject) {
        final String location = getApplication().getFilesDir().getPath()+ "/" + mediaObject.getMediaFileName();

        File file = new File(location);
        return file.exists();

    }


    private void releasePlayer() {

        if (exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {

        String userAgent = Util.getUserAgent(this, appNameStringRes + "");
        DefaultDataSourceFactory defDataSourceFactory = new DefaultDataSourceFactory(this, userAgent);
        return new ProgressiveMediaSource.Factory(defDataSourceFactory).createMediaSource(uri);

    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }
}
