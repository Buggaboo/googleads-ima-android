package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jasmsison on 07/01/17.
 */

/**
 * A VideoView that intercepts various methods and reports them back via a PlayerCallback.
 */
public class ExoSampleVideoPlayer extends ExoBaseVideoPlayer implements VideoPlayer {

    public ExoSampleVideoPlayer() {}

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int videoPosition) {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public void stopPlayback() {

    }

    @Override
    public void disablePlaybackControls() {

    }

    @Override
    public void enablePlaybackControls() {

    }

    @Override
    public void setVideoPath(String videoUrl) {

    }

    @Override
    public void addPlayerCallback(PlayerCallback callback) {

    }

    @Override
    public void removePlayerCallback(PlayerCallback callback) {

    }
}

