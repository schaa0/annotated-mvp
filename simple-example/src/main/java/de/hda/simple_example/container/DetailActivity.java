package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hda.simple_example.R;
import de.hda.simple_example.model.Repository;

public class DetailActivity extends AppCompatActivity {

    public static final String KEY_REPOSITORY = "repository";

    View orientationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_activity);
        orientationView = findViewById(R.id.orientation);
        ButterKnife.bind(this);
        if (!isInLandscape()) {
            if (savedInstanceState == null) {
                Repository repository = getIntent().getParcelableExtra(KEY_REPOSITORY);
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
        return orientationView.getTag().toString().equals("sw600dp|land");
    }

}
