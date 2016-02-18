package com.topanimestream.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topanimestream.R;
import com.topanimestream.models.WatchedAnime;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WatchListAdapter extends RecyclerView.Adapter {

    private final Context context;
    private WatchListAdapter.OnItemClickListener mItemClickListener;
    private ArrayList<WatchedAnimeItem> mItems;
    public static final int TYPE_NORMAL = 0, TYPE_LOADING = 1;
    public WatchListAdapter(Context context, ArrayList<WatchedAnime> watchedAnimes) {
        this.context = context;
        mItems = new ArrayList<>();
        addItems(watchedAnimes);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_loading, parent, false);
                return new WatchListAdapter.LoadingHolder(v);
            case TYPE_NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_watch, parent, false);
                return new WatchListAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WatchedAnime watchedAnime = getItem(position);
        switch(getItemType(position))
        {
            case TYPE_NORMAL:
                final ViewHolder watchedAnimeHolder = (ViewHolder) holder;
                watchedAnimeHolder.txtTitle.setText(watchedAnime.getAnime().getName());
                watchedAnimeHolder.progressBarWatch.setMax(watchedAnime.getAnime().getEpisodeCount());
                watchedAnimeHolder.progressBarWatch.setProgress(watchedAnime.getTotalWatchedEpisodes());
                break;
            case TYPE_LOADING:
                break;
        }
    }

    public void addItems(ArrayList<WatchedAnime> items) {
        if (items != null) {
            for (WatchedAnime item : items) {
                mItems.add(new WatchedAnimeItem(item));
            }
            notifyDataSetChanged();
        }
    }

    public int getItemType(int position) {
        if(mItems.get(position).isLoadingItem)
            return TYPE_LOADING;
        else
            return TYPE_NORMAL;
    }

    public WatchedAnime getItem(int position) {
        return mItems.get(position).watchedAnime;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addLoading() {
        mItems.add(new WatchedAnimeItem(true));
        notifyDataSetChanged();
    }

    public void removeLoading() {
        if(getItemCount() > 0) {
            if (mItems.get(getItemCount() - 1).isLoadingItem)
                mItems.remove(getItemCount() - 1);
        }
    }

    public boolean isLoading() {
        if (getItemCount() <= 0) return false;
        return getItemViewType(getItemCount() - 1) == TYPE_LOADING;
    }

    @Override
    public int getItemViewType(int position) {
        if(mItems.get(position).isLoadingItem)
            return TYPE_LOADING;
        else
            return TYPE_NORMAL;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, WatchedAnime watchedAnime, int position);
    }

    public void setOnItemClickListener(WatchListAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View itemView;

        @Bind(R.id.txtTitle)
        TextView txtTitle;

        @Bind(R.id.progressBarWatch)
        ProgressBar progressBarWatch;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getAdapterPosition();
                WatchedAnime item = getItem(position);
                mItemClickListener.onItemClick(view, item, position);
            }
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

    }

    class WatchedAnimeItem {
        WatchedAnime watchedAnime;
        boolean isLoadingItem = false;

        WatchedAnimeItem(WatchedAnime watchedAnime) {
            this.watchedAnime = watchedAnime;
        }

        WatchedAnimeItem(boolean loading) {
            this.isLoadingItem = loading;
        }

    }
}