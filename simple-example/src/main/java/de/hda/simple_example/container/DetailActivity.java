package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.Param;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;

import de.hda.simple_example.R;
import de.hda.simple_example.di.ComponentActivity;
import de.hda.simple_example.di.ComponentApplication;
import de.hda.simple_example.di.SimpleApplication;
import de.hda.simple_example.presenter.DetailActivityPresenter;
import de.hda.simple_example.model.Repository;


@View(presenter = DetailActivityPresenter.class)
public class DetailActivity extends AppCompatActivity implements DetailActivityView {

    public static final String KEY_REPOSITORY = "repository";

    private android.view.View orientationView;

    @Presenter
    DetailActivityPresenter presenter;

    private ComponentActivity component;

    @Param
    public Repository repository() {
        return getIntent().getParcelableExtra(KEY_REPOSITORY);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_activity);
        orientationView = findViewById(R.id.orientation);
        SimpleApplication provider = (SimpleApplication) getApplication();
        component = provider.componentActivity(this);
    }

    public ComponentApplication getParentComponent() {
        SimpleApplication provider = (SimpleApplication) getApplication();
        return provider.componentApplication();
    }

    public ComponentActivity getComponent() {
        return this.component;
    }

    private String orientationTag() {
        return orientationView.getTag().toString();
    }

    @Override
    public boolean isInLandscape() {
        return this.orientationTag().equals("sw600dp|land");
    }
}
