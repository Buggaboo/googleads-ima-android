package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;

/**
 * Created by jasmsison on 08/01/17.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.samples.exo.EventLogger;
import com.google.ads.interactivemedia.v3.samples.exo.TrackSelectionHelper;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.R;
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
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.ApicFrame;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlaybackControlView.SeekDispatcher;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A high level view for {@link SimpleExoPlayer} media playbacks. It displays video, subtitles and
 * album art during playback, and displays playback controls using a {@link PlaybackControlView}.
 * <p>
 * A SimpleExoPlayerView can be customized by setting attributes (or calling corresponding methods),
 * overriding the view's layout file or by specifying a custom view layout file, as outlined below.
 * <p>
 * <h3>Attributes</h3>
 * The following attributes can be set on a SimpleExoPlayerView when used in a layout XML file:
 * <p>
 * <ul>
 * <li><b>{@code use_artwork}</b> - Whether artwork is used if available in audio streams.
 * <ul>
 * <li>Corresponding method: {@link #setUseArtwork(boolean)}</li>
 * <li>Default: {@code true}</li>
 * </ul>
 * </li>
 * <li><b>{@code use_controller}</b> - Whether playback controls are displayed.
 * <ul>
 * <li>Corresponding method: {@link #setUseController(boolean)}</li>
 * <li>Default: {@code true}</li>
 * </ul>
 * </li>
 * <li><b>{@code resize_mode}</b> - Controls how video and album art is resized within the view.
 * Valid values are {@code fit}, {@code fixed_width}, {@code fixed_height} and {@code fill}.
 * <ul>
 * <li>Corresponding method: {@link #setResizeMode(int)}</li>
 * <li>Default: {@code fit}</li>
 * </ul>
 * </li>
 * <li><b>{@code surface_type}</b> - The type of surface view used for video playbacks. Valid
 * values are {@code surface_view}, {@code texture_view} and {@code none}. Using {@code none}
 * is recommended for audio only applications, since creating the surface can be expensive.
 * Using {@code surface_view} is recommended for video applications.
 * <ul>
 * <li>Corresponding method: None</li>
 * <li>Default: {@code surface_view}</li>
 * </ul>
 * </li>
 * <li><b>{@code player_layout_id}</b> - Specifies the id of the layout to be inflated. See below
 * for more details.
 * <ul>
 * <li>Corresponding method: None</li>
 * <li>Default: {@code R.id.exo_simple_player_view}</li>
 * </ul>
 * <li><b>{@code controller_layout_id}</b> - Specifies the id of the layout resource to be
 * inflated by the child {@link PlaybackControlView}. See below for more details.
 * <ul>
 * <li>Corresponding method: None</li>
 * <li>Default: {@code R.id.exo_playback_control_view}</li>
 * </ul>
 * <li>All attributes that can be set on a {@link PlaybackControlView} can also be set on a
 * SimpleExoPlayerView, and will be propagated to the inflated {@link PlaybackControlView}.
 * </li>
 * </ul>
 * <p>
 * <h3>Overriding the layout file</h3>
 * To customize the layout of SimpleExoPlayerView throughout your app, or just for certain
 * configurations, you can define {@code exo_simple_player_view.xml} layout files in your
 * application {@code res/layout*} directories. These layouts will override the one provided by the
 * ExoPlayer library, and will be inflated for use by SimpleExoPlayerView. The view identifies and
 * binds its children by looking for the following ids:
 * <p>
 * <ul>
 * <li><b>{@code exo_content_frame}</b> - A frame whose aspect ratio is resized based on the video
 * or album art of the media being played, and the configured {@code resize_mode}. The video
 * surface view is inflated into this frame as its first child.
 * <ul>
 * <li>Type: {@link AspectRatioFrameLayout}</li>
 * </ul>
 * </li>
 * <li><b>{@code exo_shutter}</b> - A view that's made visible when video should be hidden. This
 * view is typically an opaque view that covers the video surface view, thereby obscuring it
 * when visible.
 * <ul>
 * <li>Type: {@link View}</li>
 * </ul>
 * </li>
 * <li><b>{@code exo_subtitles}</b> - Displays subtitles.
 * <ul>
 * <li>Type: {@link SubtitleView}</li>
 * </ul>
 * </li>
 * <li><b>{@code exo_artwork}</b> - Displays album art.
 * <ul>
 * <li>Type: {@link ImageView}</li>
 * </ul>
 * </li>
 * <li><b>{@code exo_controller_placeholder}</b> - A placeholder that's replaced with the inflated
 * {@link PlaybackControlView}.
 * <ul>
 * <li>Type: {@link View}</li>
 * </ul>
 * </li>
 * <li><b>{@code exo_overlay}</b> - A {@link FrameLayout} positioned on top of the player which
 * the app can access via {@link #getOverlayFrameLayout()}, provided for convenience.
 * <ul>
 * <li>Type: {@link FrameLayout}</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * All child views are optional and so can be omitted if not required, however where defined they
 * must be of the expected type.
 * <p>
 * <h3>Specifying a custom layout file</h3>
 * Defining your own {@code exo_simple_player_view.xml} is useful to customize the layout of
 * SimpleExoPlayerView throughout your application. It's also possible to customize the layout for a
 * single instance in a layout file. This is achieved by setting the {@code player_layout_id}
 * attribute on a SimpleExoPlayerView. This will cause the specified layout to be inflated instead
 * of {@code exo_simple_player_view.xml} for only the instance on which the attribute is set.
 */
@TargetApi(16)
public class ExoBaseVideoPlayer extends FrameLayout implements View.OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

    // Basic
    private static final int SURFACE_TYPE_NONE = 0;
    private static final int SURFACE_TYPE_SURFACE_VIEW = 1;
    private static final int SURFACE_TYPE_TEXTURE_VIEW = 2;

    private final AspectRatioFrameLayout contentFrame;
    private final View shutterView;
    private final View surfaceView;
    private final ImageView artworkView;
    private final SubtitleView subtitleView;
    private final PlaybackControlView controller;
    private final ComponentListener componentListener;
    private final FrameLayout overlayFrameLayout;

    protected SimpleExoPlayer player;
    private boolean useController;
    private boolean useArtwork;
    private int controllerShowTimeoutMs;

    // Moar
    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String DRM_LICENSE_URL = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

    // TODO make this variable from the BuildConfig
    /*public static final String ACTION_VIEW_LIST =
            "nl.streamone.mobile.android.demo.action.VIEW_LIST";*/
    // TODO make this variable from the BuildConfig
    //public static final String ACTION_VIEW = "nl.streamone.mobile.android.demo.action.VIEW";

    public static final String URI_EXTRA = "uri";
    public static final String EXTENSION_EXTRA = "extension";
    public static final String URI_LIST_EXTRA = "uri_list";
    public static final String EXTENSION_LIST_EXTRA = "extension_list";

    protected static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    protected static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();

        // TODO BuildConfig option?
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    protected Handler mainHandler;
    protected Timeline.Window window;
    protected EventLogger eventLogger;
    protected LinearLayout debugRootView;
    protected TextView debugTextView;
    protected Button retryButton;
    protected DataSource.Factory mediaDataSourceFactory;

    protected DefaultTrackSelector trackSelector;
    protected TrackSelectionHelper trackSelectionHelper;
    protected DebugTextViewHelper debugViewHelper;
    protected boolean playerNeedsSource = true;
    protected boolean shouldAutoPlay;

    protected boolean isTimelineStatic;
    protected int playerWindow;
    protected long playerPosition;

    // we use this as the input datastructure, like a fragment
    protected Bundle bundle;

    // Nasty hack to access my builders...
    protected MainApplication application = MainApplication.instance;

    public ExoBaseVideoPlayer(Context context) {
        this(context, null);
    }

    public ExoBaseVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoBaseVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // TODO change this to the actual packageName
        int playerLayoutId = com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.layout.exo_simple_player_debug_view;
        boolean useArtwork = true;
        boolean useController = true;
        int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
        int resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT;
        int controllerShowTimeoutMs = PlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.SimpleExoPlayerView, 0, 0);
            try {
                playerLayoutId = a.getResourceId(R.styleable.SimpleExoPlayerView_player_layout_id,
                        playerLayoutId);
                useArtwork = a.getBoolean(R.styleable.SimpleExoPlayerView_use_artwork, useArtwork);
                useController = a.getBoolean(R.styleable.SimpleExoPlayerView_use_controller, useController);
                surfaceType = a.getInt(R.styleable.SimpleExoPlayerView_surface_type, surfaceType);
                resizeMode = a.getInt(R.styleable.SimpleExoPlayerView_resize_mode, resizeMode);
                controllerShowTimeoutMs = a.getInt(R.styleable.SimpleExoPlayerView_show_timeout,
                        controllerShowTimeoutMs);
            } finally {
                a.recycle();
            }
        }

        LayoutInflater.from(context).inflate(playerLayoutId, this);
        componentListener = new ComponentListener();
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        // Content frame.
        contentFrame = (AspectRatioFrameLayout) findViewById(R.id.exo_content_frame);
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame, resizeMode);
        }

        // Shutter view.
        shutterView = findViewById(R.id.exo_shutter);

        // Create a surface view and insert it into the content frame, if there is one.
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView = surfaceType == SURFACE_TYPE_TEXTURE_VIEW ? new TextureView(context)
                    : new SurfaceView(context);
            surfaceView.setLayoutParams(params);
            contentFrame.addView(surfaceView, 0);
        } else {
            surfaceView = null;
        }

        // Overlay frame layout.
        overlayFrameLayout = (FrameLayout) findViewById(R.id.exo_overlay);

        // Artwork view.
        artworkView = (ImageView) findViewById(R.id.exo_artwork);
        this.useArtwork = useArtwork && artworkView != null;

        // Subtitle view.
        subtitleView = (SubtitleView) findViewById(R.id.exo_subtitles);
        if (subtitleView != null) {
            subtitleView.setUserDefaultStyle();
            subtitleView.setUserDefaultTextSize();
        }

        // Playback control view.
        View controllerPlaceholder = findViewById(R.id.exo_controller_placeholder);
        if (controllerPlaceholder != null) {
            // Note: rewindMs and fastForwardMs are passed via attrs, so we don't need to make explicit
            // calls to set them.
            this.controller = new PlaybackControlView(context, attrs);
            controller.setLayoutParams(controllerPlaceholder.getLayoutParams());
            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
            parent.removeView(controllerPlaceholder);
            parent.addView(controller, controllerIndex);
        } else {
            this.controller = null;
        }
        this.controllerShowTimeoutMs = controller != null ? controllerShowTimeoutMs : 0;
        this.useController = useController && controller != null;
        hideController();

        // moar
        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        window = new Timeline.Window();
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        setOnClickListener(this);
        debugRootView = (LinearLayout) findViewById(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.id.controls_root);
        debugTextView = (TextView) findViewById(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.id.debug_text_view);
        retryButton = (Button) findViewById(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.id.retry_button);
        retryButton.setOnClickListener(this);

        setControllerVisibilityListener(this);

        requestFocus();
    }


    // TODO force the Activity/View to implement this as restartWindow or something
    /*
    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        isTimelineStatic = false;
        setIntent(intent);
    }
    */

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releasePlayer();
        isTimelineStatic = false;
    }

    /*
    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }
    */

    // TODO force activity to implement this
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }
    */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Show the controls on any key event.
        showController(); // TODO test
        return super.dispatchKeyEvent(event);
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {
        if (view == retryButton) {
            initializePlayer(bundle);
        } else if (view.getParent() == debugRootView) {
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                trackSelectionHelper.showSelectionDialog((Activity) getContext(), ((Button) view).getText(),
                        trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());
            }
        }
    }

    // PlaybackControlView.VisibilityListener implementation
    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    // Internal methods

    public void initializePlayer(Bundle args) {
        Set<String> bundleKeySet = args.keySet();
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
                    int errorStringId = Util.SDK_INT < 18 ? com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_drm_not_supported
                            : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_drm_unsupported_scheme : com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_drm_unknown);
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
            player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, new DefaultLoadControl(),
                    drmSessionManager, extensionRendererMode);
            player.addListener(this);

            eventLogger = new EventLogger(trackSelector);
            player.addListener(eventLogger);
            player.setAudioDebugListener(eventLogger);
            player.setVideoDebugListener(eventLogger);
            player.setId3Output(eventLogger);

            setPlayer(player);
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
                uris = new Uri[]{args.getParcelable(URI_EXTRA)};
                extensions = new String[]{args.getString(EXTENSION_EXTRA, "")};
            } else if (bundleKeySet.contains(URI_LIST_EXTRA)) {
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
                //showToast(application.getString(R.string.unexpected_intent_action, action)); // TODO use snackbar and error handling callback
                // TODO throw error, neither a playlist or item
                throw new IllegalArgumentException(); // TODO pass on argument
                //return;
            }
            /*
            if (Util.maybeRequestReadExternalStoragePermission(getContext(), uris)) {
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

    protected MediaSource buildMediaSource(Uri uri, String overrideExtension) {
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

    protected DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
                                                                             String licenseUrl, Map<String, String> keyRequestProperties) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            return null;
        }
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
                buildHttpDataSourceFactory(false), keyRequestProperties);
        return new StreamingDrmSessionManager<>(uuid,
                FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
    }

    protected void releasePlayer() {
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

    // TODO move this out to StreamOne Util class, and use BuildConfig to set this boolean

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    protected DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
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
    protected HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
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
                        //errorString = application.getString(application.useExtensionRenderers() ? R.string.error_querying_decoders : -1);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = application.getString(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_no_secure_decoder,
                                decoderInitializationException.mimeType);
                    } else {
                        errorString = application.getString(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_no_decoder,
                                decoderInitializationException.mimeType);
                    }
                } else {
                    errorString = application.getString(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_instantiating_decoder,
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
                showToast(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_unsupported_video); // TODO use snackbar and error handling callback
            }
            if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                showToast(com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.error_unsupported_audio); // TODO use snackbar and error handling callback
            }
        }
    }

    // User controls

    protected void updateButtonVisibilities() {
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
                        label = com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = com.google.ads.interactivemedia.v3.samples.videoplayerapp.R.string.text;
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener((OnClickListener) this);
                debugRootView.addView(button, debugRootView.getChildCount() - 1);
            }
        }
    }

    protected void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    // TODO replace with showSnackbar
    protected void showToast(int messageId) {
        showToast(application.getString(messageId));
    }

    // TODO replace with showSnackbar
    protected void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }


    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    public SimpleExoPlayer getPlayer() {
        return player;
    }

    /**
     * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
     * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and previous
     * assignments are overridden.
     *
     * @param player The {@link SimpleExoPlayer} to use.
     */
    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.setTextOutput(null);
            this.player.setVideoListener(null);
            this.player.removeListener(componentListener);
            this.player.setVideoSurface(null);
        }
        this.player = player;
        if (useController) {
            controller.setPlayer(player);
        }
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.setVideoListener(componentListener);
            player.addListener(componentListener);
            player.setTextOutput(componentListener);
            maybeShowController(false);
            updateForCurrentTrackSelections();
        } else {
            hideController();
            hideArtwork();
        }
    }

    /**
     * Sets the resize mode.
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@ResizeMode int resizeMode) {
        Assertions.checkState(contentFrame != null);
        contentFrame.setResizeMode(resizeMode);
    }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    public boolean getUseArtwork() {
        return useArtwork;
    }

    /**
     * Sets whether artwork is displayed if present in the media.
     *
     * @param useArtwork Whether artwork is displayed.
     */
    public void setUseArtwork(boolean useArtwork) {
        Assertions.checkState(!useArtwork || artworkView != null);
        if (this.useArtwork != useArtwork) {
            this.useArtwork = useArtwork;
            updateForCurrentTrackSelections();
        }
    }

    /**
     * Returns whether the playback controls are enabled.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether playback controls are enabled. If set to {@code false} the playback controls are
     * never visible and are disconnected from the player.
     *
     * @param useController Whether playback controls should be enabled.
     */
    public void setUseController(boolean useController) {
        Assertions.checkState(!useController || controller != null);
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player);
        } else if (controller != null) {
            controller.hide();
            controller.setPlayer(null);
        }
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return useController && controller.dispatchMediaKeyEvent(event);
    }

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     */
    public void showController() {
        if (useController) {
            maybeShowController(true);
        }
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    public void hideController() {
        if (controller != null) {
            controller.hide();
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause
     *                                the controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        Assertions.checkState(controller != null);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
    }

    /**
     * Set the {@link PlaybackControlView.VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setControllerVisibilityListener(PlaybackControlView.VisibilityListener listener) {
        Assertions.checkState(controller != null);
        controller.setVisibilityListener(listener);
    }

    /**
     * Sets the {@link SeekDispatcher}.
     *
     * @param seekDispatcher The {@link SeekDispatcher}, or null to use
     *                       {@link PlaybackControlView#DEFAULT_SEEK_DISPATCHER}.
     */
    public void setSeekDispatcher(SeekDispatcher seekDispatcher) {
        Assertions.checkState(controller != null);
        controller.setSeekDispatcher(seekDispatcher);
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds.
     */
    public void setRewindIncrementMs(int rewindMs) {
        Assertions.checkState(controller != null);
        controller.setRewindIncrementMs(rewindMs);
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        Assertions.checkState(controller != null);
        controller.setFastForwardIncrementMs(fastForwardMs);
    }

    /**
     * Gets the view onto which video is rendered. This is either a {@link SurfaceView} (default)
     * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
     *
     * @return Either a {@link SurfaceView} or a {@link TextureView}.
     */
    public View getVideoSurfaceView() {
        return surfaceView;
    }

    /**
     * Gets the overlay {@link FrameLayout}, which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay {@link FrameLayout}, or {@code null} if the layout has been customized and
     * the overlay is not present.
     */
    public FrameLayout getOverlayFrameLayout() {
        return overlayFrameLayout;
    }

    /**
     * Gets the {@link SubtitleView}.
     *
     * @return The {@link SubtitleView}, or {@code null} if the layout has been customized and the
     * subtitle view is not present.
     */
    public SubtitleView getSubtitleView() {
        return subtitleView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (controller.isVisible()) {
            controller.hide();
        } else {
            maybeShowController(true);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    private void maybeShowController(boolean isForced) {
        if (!useController || player == null) {
            return;
        }
        int playbackState = player.getPlaybackState();
        boolean showIndefinitely = playbackState == ExoPlayer.STATE_IDLE
                || playbackState == ExoPlayer.STATE_ENDED || !player.getPlayWhenReady();
        boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        if (isForced || showIndefinitely || wasShowingIndefinitely) {
            controller.show();
        }
    }

    private void updateForCurrentTrackSelections() {
        if (player == null) {
            return;
        }
        TrackSelectionArray selections = player.getCurrentTrackSelections();
        for (int i = 0; i < selections.length; i++) {
            if (player.getRendererType(i) == C.TRACK_TYPE_VIDEO && selections.get(i) != null) {
                // Video enabled so artwork must be hidden. If the shutter is closed, it will be opened in
                // onRenderedFirstFrame().
                hideArtwork();
                return;
            }
        }
        // Video disabled so the shutter must be closed.
        if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
        }
        // Display artwork if enabled and available, else hide it.
        if (useArtwork) {
            for (int i = 0; i < selections.length; i++) {
                TrackSelection selection = selections.get(i);
                if (selection != null) {
                    for (int j = 0; j < selection.length(); j++) {
                        Metadata metadata = selection.getFormat(j).metadata;
                        if (metadata != null && setArtworkFromMetadata(metadata)) {
                            return;
                        }
                    }
                }
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork();
    }

    private boolean setArtworkFromMetadata(Metadata metadata) {
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry metadataEntry = metadata.get(i);
            if (metadataEntry instanceof ApicFrame) {
                byte[] bitmapData = ((ApicFrame) metadataEntry).pictureData;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                if (bitmap != null) {
                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    if (bitmapWidth > 0 && bitmapHeight > 0) {
                        if (contentFrame != null) {
                            contentFrame.setAspectRatio((float) bitmapWidth / bitmapHeight);
                        }
                        artworkView.setImageBitmap(bitmap);
                        artworkView.setVisibility(VISIBLE);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void hideArtwork() {
        if (artworkView != null) {
            artworkView.setImageResource(android.R.color.transparent); // Clears any bitmap reference.
            artworkView.setVisibility(INVISIBLE);
        }
    }

    @SuppressWarnings("ResourceType")
    private static void setResizeModeRaw(AspectRatioFrameLayout aspectRatioFrame, int resizeMode) {
        aspectRatioFrame.setResizeMode(resizeMode);
    }

    private final class ComponentListener implements SimpleExoPlayer.VideoListener,
            TextRenderer.Output, ExoPlayer.EventListener {

        // TextRenderer.Output implementation

        @Override
        public void onCues(List<Cue> cues) {
            if (subtitleView != null) {
                subtitleView.onCues(cues);
            }
        }

        // SimpleExoPlayer.VideoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            if (contentFrame != null) {
                float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
                contentFrame.setAspectRatio(aspectRatio);
            }
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onTracksChanged(TrackGroupArray tracks, TrackSelectionArray selections) {
            updateForCurrentTrackSelections();
        }

        // ExoPlayer.EventListener implementation

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            maybeShowController(false);
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // Do nothing.
        }

    }

}
