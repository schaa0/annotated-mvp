package com.mvp.weather_example.view;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mvp.EventBus;
import com.mvp.weather_example.R;
import com.mvp.weather_example.di.ComponentActivity;
import com.mvp.weather_example.di.DaggerComponentActivity;
import com.mvp.weather_example.di.ModuleProvider;
import com.mvp.weather_example.event.PermissionEvent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;
    @BindView(R.id.container) ViewPager mViewPager;

    @Inject
    EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ComponentActivity componentActivity = DaggerComponentActivity.builder().componentEventBus(((ModuleProvider) getApplication()).componentEventBus()).build();
        componentActivity.inject(this);
        eventBus.register(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout layout = (TabLayout) findViewById(R.id.tab_layout1);
        layout.addTab(layout.newTab());
        layout.addTab(layout.newTab());
        layout.setupWithViewPager(mViewPager, true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0 && grantResults.length > 0) {
            eventBus.dispatchEvent(new PermissionEvent(requestCode, permissions, grantResults)).toAny();
        }
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int PAGE_COUNT = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new TodayWeatherFragment();
                case 1:
                    return new TomorrowWeatherFragment();
                default:
                    throw new IllegalArgumentException(String.format("invalid position : %d", position));
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Today";
                case 1:
                    return "Tomorrow";
                default:
                    throw new IllegalArgumentException(String.format("invalid position : %d", position));
            }
        }
    }

    @Override
    protected void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }
}
