package com.mvp.example.photostream.view.container;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mvp.example.R;
import com.mvp.example.photostream.model.Repository;

import java.util.ArrayList;
import java.util.List;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.RepoViewHolder>{

    private List<Repository> repositories = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    @Override
    public RepoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repository_item, parent, false);
        return new RepoViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(RepoViewHolder holder, int position) {
        Repository repository = repositories.get(position);
        String url = repository.getOwner().getAvatarUrl();
        Uri uri = Uri.parse(url);
        Glide.with(holder.itemView.getContext()).load(uri).into(holder.imageView);
        holder.textView.setText(repository.getFullName());
    }

    @Override
    public int getItemCount() {
        return repositories.size();
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void set(List<Repository> repositories){
        this.repositories.clear();
        this.repositories.addAll(repositories);
        notifyDataSetChanged();
    }

    public void addAll(List<Repository> repositories) {
        int from = this.repositories.size();
        int count = repositories.size();
        this.repositories.addAll(repositories);
        notifyItemRangeInserted(from, count);
    }

    public Repository getItemAtPosition(int position) {
        return repositories.get(position);
    }

    static class RepoViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        RepoViewHolder(View itemView, final OnItemClickListener itemClickListener) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}
