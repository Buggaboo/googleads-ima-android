package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;


/**
 * Created by jasmsison on 07/01/17.
 */

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A VideoView that intercepts various methods and reports them back via a PlayerCallback.
 */
public class ExoSampleVideoPlayer extends ExoBaseVideoPlayer /* FrameLayout */ implements VideoPlayer {

    // TODO BuildConfig option?
    public static final boolean AUTOPLAY_WHEN_READY = true;

    public ExoSampleVideoPlayer(Context context) {
        super(context);
        init();
    }

    public ExoSampleVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExoSampleVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public int getCurrentPosition() {
        return (int)
                getCurrentPosition();
    }

    @Override
    public void seekTo(int videoPosition) {
        if (player != null) {
            player.seekTo((long) videoPosition);
        }
    }

    @Override
    public void setVideoPath(String videoUrl) {
        Bundle args = new Bundle();
        args.putParcelable(ExoBaseVideoPlayer.URI_EXTRA, Uri.parse(videoUrl));
        initializePlayer(args);
    }

    private SampleVideoPlayer.PlaybackState mPlaybackState;
    private final List<PlayerCallback> mVideoPlayerCallbacks = new ArrayList<PlayerCallback>(1);

    private void init() {
        enablePlaybackControls(); // TODO necessary?

        // Set OnCompletionListener to notify our callbacks when the video is completed.
        // TODO you need onLoadCompletion, it's already in the EventLogger
        /*
        super.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                // Reset the MediaPlayer.
                // This prevents a race condition which occasionally results in the media
                // player crashing when switching between videos.
                disablePlaybackControls();
                mediaPlayer.reset();
                mediaPlayer.setDisplay(getHolder());
                enablePlaybackControls();
                mPlaybackState = SampleVideoPlayer.PlaybackState.STOPPED;

                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onCompleted();
                }
            }
        });
        */

        // TODO you need onPlayerError, it's already in the EventLogger
        // Set OnErrorListener to notify our callbacks if the video errors.
        /*
        super.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mPlaybackState = SampleVideoPlayer.PlaybackState.STOPPED;
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onError();
                }

                // Returning true signals to MediaPlayer that we handled the error. This will
                // prevent the completion handler from being called.
                return true;
            }
        });
        */
    }

    @Override
    public int getDuration() {
        return mPlaybackState == SampleVideoPlayer.PlaybackState.STOPPED ? 0 :
                player != null ? (int) player.getDuration() : 0;
    }

    /*
    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        // The OnCompletionListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        // The OnErrorListener can only be implemented by SampleVideoPlayer.
        throw new UnsupportedOperationException();
    }
*/

    // Methods implementing the VideoPlayer interface.
    @Override
    public void play() {
        start();
    }

    public void start() {
        player.setPlayWhenReady(AUTOPLAY_WHEN_READY);
        switch (mPlaybackState) {
            case STOPPED:
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onPlay();
                }
                break;
            case PAUSED:
                for (PlayerCallback callback : mVideoPlayerCallbacks) {
                    callback.onResume();
                }
                break;
            default:
                // Already playing; do nothing.
                break;
        }
        mPlaybackState = SampleVideoPlayer.PlaybackState.PLAYING;
    }

    @Override
    public void pause() {
        if (player != null) {
            player.stop();
        }
        mPlaybackState = SampleVideoPlayer.PlaybackState.PAUSED;
        for (PlayerCallback callback : mVideoPlayerCallbacks) {
            callback.onPause();
        }
    }

    @Override
    public void stopPlayback() {
        if (mPlaybackState == SampleVideoPlayer.PlaybackState.STOPPED) {
            return;
        }
        if (player != null) {
            player.stop();
        }
        releasePlayer();
        mPlaybackState = SampleVideoPlayer.PlaybackState.STOPPED;
    }

    @Override
    public void disablePlaybackControls() {
        hideController();
    }

    @Override
    public void enablePlaybackControls() {
        showController();
    }

    @Override
    public void addPlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.add(callback);
    }

    @Override
    public void removePlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.remove(callback);
    }
}

