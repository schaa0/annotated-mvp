package com.mvp.example.photostream.view.container;


import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.mvp.example.photostream.model.Repository;
import com.mvp.example.photostream.presenter.RecyclerViewPresenter;
import com.mvp.example.photostream.view.viewcontract.IRecyclerView;

import java.util.List;

public class RecyclerViewContainer implements IRecyclerView, RepositoryAdapter.OnItemClickListener {

    private final RecyclerView recyclerView;
    final RepositoryAdapter adapter;
    LinearLayoutManager lm;
    RecyclerViewPresenter presenter;
    private RecyclerView.OnScrollListener scrollListener;

    public RecyclerViewContainer(RecyclerView recyclerView, RepositoryAdapter adapter){
        this.recyclerView = recyclerView;
        this.adapter = adapter;
    }

    @Override
    public void setUp() {
        lm = new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (presenter != null && !presenter.isLoading() && !presenter.reachedEndOfStream()){
                    if (lm.findLastVisibleItemPosition() == adapter.getItemCount() - 1){
                        presenter.loadMoreRepositories();
                    }
                }

            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewDetached(){
        recyclerView.removeOnScrollListener(scrollListener);
        presenter = null;
    }

    @Override
    public void setPresenter(RecyclerViewPresenter presenter) {
        this.presenter = presenter;
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
    public void showToast(Repository repository) {
        Toast.makeText(recyclerView.getContext(), String.valueOf(repository.getId()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {
        Repository repository = adapter.getItemAtPosition(position);
        presenter.showDetailView(repository);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putBundle("items", adapter.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        adapter.onRestoreInstanceState(bundle.getBundle("items"));
    }
}
