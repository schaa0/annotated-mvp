package de.hda.simple_example.presenter;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Presenter;

import javax.inject.Inject;

import de.hda.simple_example.R;
import de.hda.simple_example.container.DetailActivityView;
import de.hda.simple_example.container.DetailFragment;
import de.hda.simple_example.di.ComponentActivity;
import de.hda.simple_example.model.Repository;

@Presenter(components = {ComponentActivity.class} )
public class DetailActivityPresenter extends MvpPresenter<DetailActivityView> {

    private Repository repository;

    protected DetailActivityPresenter() { }

    @Inject
    public DetailActivityPresenter(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void onViewAttached(DetailActivityView view) {

    }

    @Override
    public void onViewReattached(DetailActivityView view) {

    }

    @Override
    public void onNavigationEnabled() {
        super.onNavigationEnabled();
        if (!this.isReattached()) {
            getFragmentRouter()
                    .navigateTo(DetailFragment.class)
                    .replace(R.id.container, DetailFragment.TAG)
                    .putExtra(DetailFragment.KEY_REPOSITORY, this.repository)
                    .commit();
        }else {
            if (getView().isInLandscape()) {
                getActivityRouter().goBack();
            }
        }
    }

    @Override
    public void onViewDetached(DetailActivityView view) {

    }

}
