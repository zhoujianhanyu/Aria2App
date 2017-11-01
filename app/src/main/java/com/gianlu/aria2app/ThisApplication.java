package com.gianlu.aria2app;

import android.widget.Toast;

import com.gianlu.aria2app.NetIO.ErrorHandler;
import com.gianlu.aria2app.NetIO.HTTPing;
import com.gianlu.aria2app.NetIO.WebSocketing;
import com.gianlu.commonutils.AnalyticsApplication;
import com.gianlu.commonutils.Logging;
import com.gianlu.commonutils.Prefs;
import com.google.android.gms.analytics.HitBuilders;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashSet;
import java.util.Set;

public class ThisApplication extends AnalyticsApplication implements ErrorHandler.IErrorHandler {
    private boolean firstStart = true;

    public boolean isFirstStart() {
        return firstStart;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected int getTrackerConfiguration() {
        return R.xml.tracking;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ErrorHandler.setup(Prefs.getFakeInt(this, PKeys.A2_UPDATE_INTERVAL, 1) * 1000, this);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);

        // Backward compatibility
        if (!Prefs.has(getApplicationContext(), PKeys.A2_CUSTOM_INFO)) {
            Set<String> defaultValues = new HashSet<>();
            defaultValues.add(CustomDownloadInfo.Info.DOWNLOAD_SPEED.name());
            defaultValues.add(CustomDownloadInfo.Info.REMAINING_TIME.name());
            Prefs.putSet(getApplicationContext(), PKeys.A2_CUSTOM_INFO, defaultValues);
        }
    }

    @Override
    public void onFatal(Throwable ex) {
        WebSocketing.clear();
        HTTPing.clear();
        Toast.makeText(this, R.string.fatalExceptionMessage, Toast.LENGTH_LONG).show();
        LoadingActivity.startActivity(this, ex);

        sendAnalytics(getApplicationContext(), new HitBuilders.ExceptionBuilder()
                .setDescription(Logging.getStackTrace(ex))
                .setFatal(false));
    }

    @Override
    public void onSubsequentExceptions() {
        WebSocketing.clear();
        HTTPing.clear();
        LoadingActivity.startActivity(this, null);
    }

    public void firstStarted() {
        this.firstStart = true;
    }

    @Override
    public void onException(Throwable ex) {
        Logging.logMe(this, ex);

        /*
        sendAnalytics(getApplicationContext(), new HitBuilders.ExceptionBuilder()
                .setDescription(Logging.getStackTrace(ex))
                .setFatal(false)
                .build());
                */
    }
}
