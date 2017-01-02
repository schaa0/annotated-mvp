package de.hda.simple_example.container;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;

import java.util.List;

import de.hda.simple_example.R;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.model.Repository;

/**
 * Created by Andy on 18.12.2016.
 */

@UIView(presenter = MainPresenter.class)
public class MainFragment extends Fragment implements IMainView, RepositoryAdapter.OnItemClickListener {

    public static final String TAG = MainFragment.class.getName();

    @Presenter MainPresenter presenter;

    @ModuleParam
    public MainPresenter.State provideState(){
        return getArguments().getParcelable(MainPresenter.KEY_STATE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        lm = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (presenter != null && !presenter.isLoading() && !presenter.reachedEndOfStream()) {
                    if (lm.findLastVisibleItemPosition() == adapter.getItemCount() - 1) {
                        presenter.loadMoreRepositories();
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        adapter = new RepositoryAdapter();
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null){
            adapter.onRestoreInstanceState(savedInstanceState.getBundle("adapter"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("adapter", adapter.onSaveInstanceState());
    }

    @Override
    public void onItemClick(int position) {
        Repository repository = adapter.getItemAtPosition(position);
        presenter.showDetailView(repository);
    }

    @Override
    public void setRepositories(List<Repository> repositories) {
        adapter.set(repositories);
    }

    @Override
    public void addRepositories(List<Repository> repositories) {
        adapter.addAll(repositories);
    }

    @Override
    public void showDetailViewInActivity(Repository repository) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("repository", repository);
        getActivity().startActivity(intent);
    }

    @Override
    public String provideOrientationTag() {
        return getView().findViewById(R.id.orientation).getTag().toString();
    }

    @Override
    public void onDestroyView() {
        recyclerView.removeOnScrollListener(scrollListener);
        super.onDestroyView();
    }

    RecyclerView recyclerView;
    RepositoryAdapter adapter;
    RecyclerView.OnScrollListener scrollListener;
    LinearLayoutManager lm;

    public static MainFragment newInstance(MainPresenter.State state) {
        Bundle args = new Bundle();
        args.putParcelable(MainPresenter.KEY_STATE, state);
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static MainFragment newInstance(){
        return newInstance(new MainPresenter.State());
    }

}
