package com.mvp.weather_example.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.mvp.EventBus;
import com.mvp.annotation.Event;
import com.mvp.weather_example.R;
import com.mvp.weather_example.di.WeatherApplication;
import com.mvp.weather_example.di.ViewPagerFragmentFactory;
import com.mvp.weather_example.event.PermissionEvent;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity
{

    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tab_layout1) TabLayout tabLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Inject public SectionsPagerAdapter mSectionsPagerAdapter;
    @Inject public EventBus eventBus;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);
        ((WeatherApplication) getApplication()).createComponentActivity(this).inject(this);

        eventBus.register(this);

        setSupportActionBar(toolbar);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setupWithViewPager(mViewPager, true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length > 0 && grantResults.length > 0)
        {
            eventBus.dispatchEvent(new PermissionEvent(requestCode, permissions, grantResults)).toAny();
        }
    }

    @Override
    protected void onDestroy()
    {
        unbinder.unbind();
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Event
    public void onLoadingWeatherFailed(IOException e){
        Toast.makeText(this, String.format("Loading weather failed: %s", e.toString()), Toast.LENGTH_SHORT).show();
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        private ViewPagerFragmentFactory viewPagerFragmentFactory;

        @Inject
        public SectionsPagerAdapter(FragmentManager fm, ViewPagerFragmentFactory viewPagerFragmentFactory)
        {
            super(fm);
            this.viewPagerFragmentFactory = viewPagerFragmentFactory;
        }

        @Override
        public Fragment getItem(int position)
        {
            return viewPagerFragmentFactory.getItem(position);
        }

        @Override
        public int getCount()
        {
            return viewPagerFragmentFactory.getCount();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return viewPagerFragmentFactory.getPageTitle(position);
        }
    }
}
