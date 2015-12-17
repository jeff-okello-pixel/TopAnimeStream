package com.topanimestream.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import com.squareup.picasso.Picasso;
import com.topanimestream.R;
import com.topanimestream.enums.Languages;
import com.topanimestream.models.Episode;
import com.topanimestream.utilities.ImageUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EpisodeListAdapter extends RecyclerView.Adapter {
    private final Context context;
    private ArrayList<EpisodeItem> mItems = new ArrayList<>();
    private EpisodeListAdapter.OnItemClickListener mItemClickListener;
    public static final int TYPE_NORMAL = 0, TYPE_LOADING = 1, TYPE_UNAVAILABLE = 2;
    public EpisodeListAdapter(Context context, ArrayList<Episode> episodes) {
        this.context = context;
        addItems(episodes);
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_episode_loading, parent, false);
                return new EpisodeListAdapter.SimpleHolder(v);
            case TYPE_NORMAL:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_episode, parent, false);
                return new EpisodeListAdapter.EpisodeViewHolder(v);
            case TYPE_UNAVAILABLE:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_episode_unavailable, parent, false);
                return new EpisodeListAdapter.UnavailableHolder(v);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Episode episode = getItem(position);
        switch(getItemViewType(position))
        {
            case TYPE_NORMAL:
                final EpisodeViewHolder episodeHolder = (EpisodeViewHolder) holder;
                Picasso .with(context)
                        .load(context.getString(R.string.image_host_path) + ImageUtils.resizeImage(episode.getScreenshotHD(), 250))
                        .into(episodeHolder.imgScreenshot);

                episodeHolder.txtEpisodeName.setText(episode.getEpisodeName(Languages.English));
                episodeHolder.txtEpisodeNumber.setText(context.getString(R.string.episode) + " " + episode.getEpisodeNumber());

                if(episode.getAiredDate() != null) {
                    long now = System.currentTimeMillis();
                    long addedDate = episode.getAiredDate().getTime();
                    episodeHolder.txtReleasedDate.setText(DateUtils.getRelativeTimeSpanString(addedDate, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
                }
                else
                {
                    episodeHolder.txtReleasedDate.setText(context.getString(R.string.unknown_aired_date));
                }
                break;
            case TYPE_UNAVAILABLE:
                final UnavailableHolder unavailableHolder = (UnavailableHolder) holder;
                //TODO bind views
                break;
            case TYPE_LOADING:
                break;
        }
    }

    public void addItems(ArrayList<Episode> items) {
        if (items != null) {
            for (Episode item : items) {
                mItems.add(new EpisodeItem(item));
            }
            notifyDataSetChanged();
        }
    }

    public void addLoading() {
        mItems.add(new EpisodeItem(true));
        notifyDataSetChanged();
    }

    public void removeLoading() {
        if(mItems.get(getItemCount() - 1).isLoadingItem)
            mItems.remove(getItemCount() - 1);
    }

    public boolean isLoading() {
        if (getItemCount() <= 0) return false;
        return getItemViewType(getItemCount() - 1) == TYPE_LOADING;
    }

    @Override
    public int getItemViewType(int position) {
        EpisodeItem episodeItem = mItems.get(position);


        if(episodeItem.isLoadingItem)
            return TYPE_LOADING;
        else if(episodeItem.episode.isAvailable())
            return TYPE_NORMAL;
        else
            return TYPE_UNAVAILABLE;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public Episode getItem(int position) {
        return mItems.get(position).episode;
    }

    public ArrayList<Episode> getItems() {
        ArrayList<Episode> episodes = new ArrayList<Episode>();

        for (int i = 0; i < mItems.size(); i++)
            episodes.add(mItems.get(i).episode);

        return episodes;
    }

    public class EpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;

        @Bind(R.id.txtEpisodeNumber)
        TextView txtEpisodeNumber;

        @Bind(R.id.txtEpisodeName)
        TextView txtEpisodeName;

        @Bind(R.id.imgScreenshot)
        ImageView imgScreenshot;

        @Bind(R.id.txtReleasedDate)
        TextView txtReleasedDate;

        public EpisodeViewHolder(View itemView) {
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

    class UnavailableHolder extends RecyclerView.ViewHolder {

        View itemView;

        public UnavailableHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }

    }

    class SimpleHolder extends RecyclerView.ViewHolder {

        View itemView;

        public SimpleHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

    }

    class EpisodeItem {
        Episode episode;
        boolean isLoadingItem = false;

        EpisodeItem(Episode episode) {
            this.episode = episode;
        }

        EpisodeItem(boolean loading) {
            this.isLoadingItem = loading;
        }

    }
}