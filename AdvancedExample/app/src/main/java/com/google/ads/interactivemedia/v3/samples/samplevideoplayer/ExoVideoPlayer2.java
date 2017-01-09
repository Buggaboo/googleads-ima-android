package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.samples.exo.EventLogger;
import com.google.ads.interactivemedia.v3.samples.exo.TrackSelectionHelper;
import com.google.ads.interactivemedia.v3.samples.videoplayerapp.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jasmsison on 09/01/17.
 */

public class ExoVideoPlayer2 extends FrameLayout implements VideoPlayer, View.OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

    // constants
    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    public static final String URI_EXTRA = "uri";
    public static final String EXTENSION_EXTRA = "extension";
    public static final String URI_LIST_EXTRA = "uri_list";
    public static final String EXTENSION_LIST_EXTRA = "extension_list";

    // persist
    private static Bundle sArgs = null;

    // builder params
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }


    public ExoVideoPlayer2(Context context) {
        super(context);
        init(context);
    }

    public ExoVideoPlayer2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExoVideoPlayer2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Handler mainHandler;
    private Timeline.Window window;
    private EventLogger eventLogger;
    private LinearLayout debugRootView;
    private TextView debugTextView;
    private Button retryButton;
    private DataSource.Factory mediaDataSourceFactory;

    private DefaultTrackSelector trackSelector;
    private TrackSelectionHelper trackSelectionHelper;
    private DebugTextViewHelper debugViewHelper;
    private boolean playerNeedsSource;
    private boolean shouldAutoPlay;

    private boolean isTimelineStatic;
    private int playerWindow;
    private long playerPosition;
    private MainApplication application;

    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;


    private PlaybackState mPlaybackState;
    private final List<PlayerCallback> mVideoPlayerCallbacks = new ArrayList<PlayerCallback>(1);

    private void init(Context context) {
        mPlaybackState = PlaybackState.STOPPED;
        setKeepScreenOn(true);

        application = MainApplication.instance;

        android.view.LayoutInflater.from(context).inflate(R.layout.player_merge, this, true);

        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        window = new Timeline.Window();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        this.setOnClickListener(this);
        debugRootView = (LinearLayout) findViewById(R.id.controls_root);
        debugTextView = (TextView) findViewById(R.id.debug_text_view);
        retryButton = (Button) findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this);

        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        playerView.setControllerVisibilityListener(this);
        playerView.requestFocus();
    }


    //enablePlaybackControls(); // TODO necessary?

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
        }
        */

    @Override
    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    @Override
    public void seekTo(int videoPosition) {
        if (player == null) {
            return;
            //initializePlayer(sArgs); // also MIA
        }

        player.seekTo(videoPosition);
    }

    @Override
    public void setVideoPath(String videoUrl) {
        if (sArgs != null) {
            releasePlayer();
        }
        initializePlayer(videoUrl);
    }

    @Override
    public int getDuration() {
        return mPlaybackState == SampleVideoPlayer.PlaybackState.STOPPED ? 0 : (int) player.getDuration();
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
        player.setPlayWhenReady(shouldAutoPlay);
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
        // TODO stop
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
        // TODO stop and tear down
        mPlaybackState = SampleVideoPlayer.PlaybackState.STOPPED;
    }

    @Override
    public void disablePlaybackControls() {
        playerView.hideController();
    }

    @Override
    public void enablePlaybackControls() {
        playerView.showController();
        showControls();
    }

    @Override
    public void addPlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.add(callback);
    }

    @Override
    public void removePlayerCallback(PlayerCallback callback) {
        mVideoPlayerCallbacks.remove(callback);
    }

    // clean up
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releasePlayer();
    }

    /**
     * Specific to ExoPlayer
     */


    public void releasePlayer() {
        if (player != null) {
            debugViewHelper.stop();
            debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            Timeline timeline = player.getCurrentTimeline();
            if (!timeline.isEmpty() && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition = player.getCurrentPosition();
            }
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
            eventLogger = null;
        }
    }

    public void initializePlayer(String url) {
        Bundle args = new Bundle();
        args.putString(URI_EXTRA, url);
        initializePlayer(args);
    }

    public void initializePlayer(Bundle args) {
        if (args == null) {
            throw new IllegalArgumentException("No video Uri provided");
        }
        sArgs = args;
        Set<String> bundleKeySet = args.keySet();

        // TODO remove debug
        if (player != null) throw new IllegalArgumentException();

        if (player == null) {
            boolean preferExtensionDecoders = args.getBoolean(PREFER_EXTENSION_DECODERS, false);
            UUID drmSchemeUuid = bundleKeySet.contains(DRM_SCHEME_UUID_EXTRA)
                    ? UUID.fromString(args.getString(DRM_SCHEME_UUID_EXTRA)) : null;
            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            if (drmSchemeUuid != null) {
                String drmLicenseUrl = args.getString(DRM_LICENSE_URL);
                String[] keyRequestPropertiesArray = args.getStringArray(DRM_KEY_REQUEST_PROPERTIES);
                Map<String, String> keyRequestProperties;
                if (keyRequestPropertiesArray == null || keyRequestPropertiesArray.length < 2) {
                    keyRequestProperties = null;
                } else {
                    keyRequestProperties = new HashMap<>();
                    for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                        keyRequestProperties.put(keyRequestPropertiesArray[i],
                                keyRequestPropertiesArray[i + 1]);
                    }
                }
                try {
                    drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl,
                            keyRequestProperties);
                } catch (UnsupportedDrmException e) {
                    int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                            : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
                    showToast(errorStringId); // TODO use snackbar and error handling callback
                    return;
                }
            }

            @SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
                    application.useExtensionRenderers()
                            ? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
                            : SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);

            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(application, trackSelector, new DefaultLoadControl(),
                    drmSessionManager, extensionRendererMode);
            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);

            playerView.setPlayer(player);
            if (isTimelineStatic) {
                if (playerPosition == C.TIME_UNSET) {
                    player.seekToDefaultPosition(playerWindow);
                } else {
                    player.seekTo(playerWindow, playerPosition);
                }
            }
            player.setPlayWhenReady(shouldAutoPlay);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
            playerNeedsSource = true;
        }
        if (playerNeedsSource) {
            Uri[] uris;
            String[] extensions;
            if (bundleKeySet.contains(URI_EXTRA)) {
                uris = new Uri[]{(Uri) args.getSerializable(URI_EXTRA)};
                extensions = new String[]{args.getString(EXTENSION_EXTRA, "")};
            }else if (bundleKeySet.contains(URI_LIST_EXTRA)) {
                String[] uriStrings = args.getStringArray(URI_LIST_EXTRA);
                uris = new Uri[uriStrings.length];
                for (int i = 0; i < uriStrings.length; i++) {
                    uris[i] = Uri.parse(uriStrings[i]);
                }
                extensions = args.getStringArray(EXTENSION_LIST_EXTRA);
                if (extensions == null) {
                    extensions = new String[uriStrings.length];
                }
            } else {
                //showToast(getString(R.string.unexpected_intent_action, action)); // TODO use snackbar and error handling callback
                // TODO throw error, neither a playlist or item
                //return;
                throw new IllegalArgumentException("Bad bundle provided");
            }

            /*
            // TODO necessary?
            if (Util.maybeRequestReadExternalStoragePermission(getActivity(), uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            */
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            player.prepare(mediaSource, !isTimelineStatic, !isTimelineStatic);
            playerNeedsSource = false;
            updateButtonVisibilities();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == retryButton) {
            initializePlayer(sArgs);
        } else if (view.getParent() == debugRootView) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                trackSelectionHelper.showSelectionDialog(getContext(), ((Button) view).getText(),
                        trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());
            }
        }
    }

    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }


    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                             String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            // TODO send debug message, stating there is no DRM
            return null;
        }
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new StreamingDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    // TODO move this out to StreamOne Util class, and use BuildConfig to set this boolean
    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return application
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // TODO move this out to StreamOne Util class, and use BuildConfig to set this boolean
    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return application
                .buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    // ExoPlayer.EventListener implementation
    @Override
    public void onLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
        updateButtonVisibilities();
    }

    @Override
    public void onPositionDiscontinuity() {
        // Do nothing.
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        isTimelineStatic = !timeline.isEmpty()
                && !timeline.getWindow(timeline.getWindowCount() - 1, window).isDynamic;
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        String errorString = null;
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            Exception cause = e.getRendererException();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.decoderName == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = application.getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = application.getString(R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = application.getString(R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = application.getString(R.string.error_instantiating_decoder,
                            decoderInitializationException.decoderName);
                }
            }
        }
        if (errorString != null) {
            showToast(errorString); // TODO use snackbar and error handling callback
        }
        playerNeedsSource = true;
        updateButtonVisibilities();
        showControls();
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        updateButtonVisibilities();
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_video); // TODO use snackbar and error handling callback
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(R.string.error_unsupported_audio); // TODO use snackbar and error handling callback
            }
        }
    }

    // User controls
    private void updateButtonVisibilities() {
        debugRootView.removeAllViews();

        retryButton.setVisibility(playerNeedsSource ? View.VISIBLE : View.GONE);
        debugRootView.addView(retryButton);

        if (player == null) {
            return;
        }

        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }

        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(getContext());
                int label;
                switch (player.getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.text;
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener(this);
                debugRootView.addView(button, debugRootView.getChildCount() - 1);
            }
        }
    }

    public void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    // TODO replace with showSnackbar
    private void showToast(int messageId) {
        showToast(application.getString(messageId));
    }

    // TODO replace with showSnackbar
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

}


