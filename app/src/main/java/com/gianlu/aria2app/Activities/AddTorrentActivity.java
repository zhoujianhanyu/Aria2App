package com.gianlu.aria2app.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gianlu.aria2app.Activities.AddDownload.AddBase64Bundle;
import com.gianlu.aria2app.Activities.AddDownload.AddDownloadBundle;
import com.gianlu.aria2app.Activities.AddDownload.AddTorrentBundle;
import com.gianlu.aria2app.Activities.AddDownload.Base64Fragment;
import com.gianlu.aria2app.Activities.AddDownload.OptionsFragment;
import com.gianlu.aria2app.Activities.AddDownload.UrisFragment;
import com.gianlu.aria2app.Activities.EditProfile.InvalidFieldException;
import com.gianlu.aria2app.Adapters.PagerAdapter;
import com.gianlu.aria2app.R;
import com.gianlu.aria2app.Utils;
import com.gianlu.commonutils.Analytics.AnalyticsApplication;

public class AddTorrentActivity extends AddDownloadActivity {
    private ViewPager pager;
    private UrisFragment urisFragment;
    private OptionsFragment optionsFragment;
    private Base64Fragment base64Fragment;

    public static void startAndAdd(Context context, @NonNull Uri uri) {
        context.startActivity(new Intent(context, AddTorrentActivity.class).putExtra("uri", uri));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState, @Nullable AddDownloadBundle bundle) {
        setContentView(R.layout.activity_add_download);
        setTitle(R.string.addTorrent);

        Toolbar toolbar = findViewById(R.id.addDownload_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        pager = findViewById(R.id.addDownload_pager);
        final TabLayout tabLayout = findViewById(R.id.addDownload_tabs);

        if (bundle instanceof AddTorrentBundle) {
            base64Fragment = Base64Fragment.getInstance(this, (AddBase64Bundle) bundle);
            urisFragment = UrisFragment.getInstance(this, bundle);
            optionsFragment = OptionsFragment.getInstance(this, bundle);
        } else {
            base64Fragment = Base64Fragment.getInstance(this, true, (Uri) getIntent().getParcelableExtra("uri"));
            urisFragment = UrisFragment.getInstance(this, false, null);
            optionsFragment = OptionsFragment.getInstance(this, false);
        }

        pager.setAdapter(new PagerAdapter<>(getSupportFragmentManager(), base64Fragment, urisFragment, optionsFragment));
        pager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(pager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_download, menu);
        return true;
    }

    @Nullable
    @Override
    public AddDownloadBundle createBundle() {
        AnalyticsApplication.sendAnalytics(AddTorrentActivity.this, Utils.ACTION_NEW_TORRENT);


        String base64 = null;
        try {
            base64 = base64Fragment.getBase64();
        } catch (InvalidFieldException ex) {
            if (ex.fragmentClass == Base64Fragment.class) {
                pager.setCurrentItem(0, true);
                return null;
            }
        }

        String filename = base64Fragment.getFilenameOnDevice();
        Uri fileUri = base64Fragment.getFileUri();
        if (base64 == null || filename == null || fileUri == null) {
            pager.setCurrentItem(0, true);
            return null;
        }

        return new AddTorrentBundle(base64, filename, fileUri, urisFragment.getUris(), optionsFragment.getPosition(), optionsFragment.getOptions());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.addDownload_done:
                done();
                break;
        }

        return true;
    }
}
