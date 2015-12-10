package com.topanimestream.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.topanimestream.R;
import com.topanimestream.models.Episode;

import butterknife.Bind;
import butterknife.ButterKnife;


public class EpisodeListAdapter extends RecyclerView.Adapter {
    private final Context context;
    private ArrayList<Episode> episodes;
    private EpisodeListAdapter.OnItemClickListener mItemClickListener;
    public static final int TYPE_NORMAL = 0, TYPE_LOADING = 1;
    public EpisodeListAdapter(Context context, ArrayList<Episode> episodes) {
        this.context = context;
        this.episodes = episodes;
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, Episode episode, int position);
    }

    public void setOnItemClickListener(EpisodeListAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_episode, parent, false);
                return new EpisodeListAdapter.LoadingHolder(v);
            case TYPE_NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_episode, parent, false);
                return new EpisodeListAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder episodeHolder = (ViewHolder) holder;
        final Episode episode = getItem(position);

        episodeHolder.txtEpisodeName.setText(episode.getEpisodeName());
        episodeHolder.txtEpisodeNumber.setText(episode.getEpisodeNumber());
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public Episode getItem(int position) {
        return episodes.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;

        @Bind(R.id.txtEpisodeNumber)
        TextView txtEpisodeNumber;

        @Bind(R.id.txtEpisodeName)
        TextView txtEpisodeName;

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
                Episode episode = getItem(position);
                mItemClickListener.onItemClick(view, episode, position);
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
}