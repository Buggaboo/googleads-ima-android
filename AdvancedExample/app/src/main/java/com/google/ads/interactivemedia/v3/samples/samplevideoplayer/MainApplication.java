package com.google.ads.interactivemedia.v3.samples.samplevideoplayer;

import android.app.Application;

import com.google.ads.interactivemedia.v3.samples.videoplayerapp.BuildConfig;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import timber.log.Timber;
import static timber.log.Timber.DebugTree;

/**
 * Created by jasmsison on 07/01/17.
 * <p>
 * Placeholder application to facilitate overriding Application methods for debugging and testing.
 */
public class MainApplication extends Application {

    public static MainApplication instance;

    private String userAgent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }

        // TODO use BuildConfig
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");

        instance = this; // hack
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return BuildConfig.FLAVOR.equals("withExtensions");
    }

}

