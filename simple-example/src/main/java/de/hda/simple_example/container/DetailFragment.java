package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;

import de.hda.simple_example.R;
import de.hda.simple_example.business.DetailPresenter;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 18.12.2016.
 */

@UIView(presenter = DetailPresenter.class)
public class DetailFragment extends Fragment implements IDetailView{

    public static final String TAG = DetailFragment.class.getName();
    public static final String KEY_REPOSITORY = "repository";

    @Presenter DetailPresenter presenter;

    @ModuleParam public Repository getRepository() {
        return getArguments().getParcelable(KEY_REPOSITORY);
    }

    TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textView = (TextView) getView().findViewById(R.id.textView);
    }

    public static DetailFragment newInstance() {
        return newInstance(Repository.NULL);
    }

    public static DetailFragment newInstance(Repository repository){
        DetailFragment fragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_REPOSITORY, repository);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void showId(String id) {
        textView.setText(id);
    }
}
