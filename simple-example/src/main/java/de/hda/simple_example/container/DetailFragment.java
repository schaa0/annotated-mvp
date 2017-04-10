package de.hda.simple_example.container;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mvp.annotation.Param;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;

import de.hda.simple_example.R;
import de.hda.simple_example.presenter.DetailFragmentPresenter;
import de.hda.simple_example.model.Repository;

@View(presenter = DetailFragmentPresenter.class)
public class DetailFragment extends Fragment implements DetailFragmentView {

    public static final String TAG = DetailFragment.class.getName();
    public static final String KEY_REPOSITORY = "repository";

    @Presenter
    DetailFragmentPresenter presenter;

    @Param
    public Repository getRepository() {
        return getArguments().getParcelable(KEY_REPOSITORY);
    }

    TextView textView;

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.detail_layout, container, false);
        textView = (TextView) view.findViewById(R.id.textView);
        return view;
    }

    @Override
    public void showId(String id) {
        textView.setText(id);
    }
}
