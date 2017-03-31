package de.hda.simple_example.container;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hda.simple_example.R;
import de.hda.simple_example.business.MainPresenter;
import de.hda.simple_example.model.Repository;

@View(presenter = MainPresenter.class)
public class MainFragment extends Fragment implements IMainView, RepositoryAdapter.OnItemClickListener {

    public static final String TAG = MainFragment.class.getName();
    private static final String KEY_ADAPTER = "KEY_ADAPTER";

    @Presenter MainPresenter presenter;

    @Inject RepositoryAdapter repositoryAdapter;

    android.view.View orientationView;
    RecyclerView recyclerView;

    RecyclerView.OnScrollListener scrollListener;
    LinearLayoutManager lm;

    @ModuleParam
    public MainPresenter.State provideState(){
        return getArguments().getParcelable(MainPresenter.KEY_STATE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).getComponent().plus().inject(this);
        recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        orientationView = getView().findViewById(R.id.orientation);
        lm = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (presenter != null && !presenter.isLoading() && !presenter.reachedEndOfStream()) {
                    if (lm.findLastVisibleItemPosition() == repositoryAdapter.getItemCount() - 1) {
                        presenter.loadMoreRepositories();
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        repositoryAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(repositoryAdapter);

        if (savedInstanceState != null){
            repositoryAdapter.onRestoreInstanceState(savedInstanceState.getBundle(KEY_ADAPTER));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(KEY_ADAPTER, repositoryAdapter.onSaveInstanceState());
    }

    @Override
    public void onItemClick(int position) {
        Repository repository = repositoryAdapter.getItemAtPosition(position);
        presenter.showDetailView(repository);
    }

    @Override
    public void setRepositories(List<Repository> repositories) {
        repositoryAdapter.set(repositories);
    }

    @Override
    public void addRepositories(List<Repository> repositories) {
        repositoryAdapter.addAll(repositories);
    }

    @Override
    public void showDetailViewInActivity(Repository repository) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_REPOSITORY, repository);
        getActivity().startActivity(intent);
    }

    @Override
    public String provideOrientationTag() {
        return orientationView.getTag().toString();
    }

    @Override
    public void onDestroyView() {
        recyclerView.removeOnScrollListener(scrollListener);
        super.onDestroyView();
    }

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
