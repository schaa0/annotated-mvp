package de.hda.simple_example.container;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import de.hda.simple_example.R;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 19.12.2016.
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_activity);
        if (!isInLandscape()) {
            if (savedInstanceState == null) {
                Repository repository = getIntent().getParcelableExtra("repository");
                DetailFragment fragment = DetailFragment.newInstance(repository);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, DetailFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isInLandscape())
            onBackPressed();
    }

    private boolean isInLandscape() {
        return findViewById(R.id.orientation).getTag().toString().equals("sw600dp|land");
    }

}
