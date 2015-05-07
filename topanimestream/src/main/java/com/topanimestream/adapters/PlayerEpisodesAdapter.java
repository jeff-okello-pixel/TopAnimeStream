package com.topanimestream.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Episode;
import com.topanimestream.models.EpisodeInformations;
import com.topanimestream.utilities.Utils;

import java.sql.Timestamp;
import java.util.ArrayList;


public class PlayerEpisodesAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Episode> values;
    private ViewHolder holder;
    App app;

    public PlayerEpisodesAdapter(Context context, ArrayList<Episode> values) {
        this.context = context;
        this.values = values;
        app = ((App) context.getApplicationContext());
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Episode episode) {
        values.add(episode);
    }

    public void remove(Episode episode) {
        values.remove(episode);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vi = convertView;
        Episode episode = values.get(position);
        if (convertView == null) {
            vi = inflater.inflate(R.layout.row_player_episode, null);
            holder = new ViewHolder();
            holder.txtEpisodeNumber = (TextView) vi.findViewById(R.id.txtEpisodeNumber);
            holder.txtEpisodeName = (TextView) vi.findViewById(R.id.txtEpisodeName);
            holder.imgScreenshot = (ImageView) vi.findViewById(R.id.imgScreenshot);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.imgScreenshot.setImageResource(android.R.color.transparent);
        App.imageLoader.displayImage(Utils.resizeImage(context.getString(R.string.image_host_path) + episode.getScreenshotHD(), App.ImageSize.w300.getValue()), holder.imgScreenshot);
        holder.txtEpisodeNumber.setText(context.getString(R.string.episode) + " " + episode.getEpisodeNumber());

        EpisodeInformations episodeInfo = episode.getEpisodeInformations();
        if(episodeInfo != null)
        {
            //Episode info exist for the current language
            holder.txtEpisodeName.setText(episodeInfo.getEpisodeName());
        }



        return vi;
    }

    public void clear() {
        values.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView txtEpisodeNumber;
        TextView txtEpisodeName;
        ImageView imgScreenshot;
    }
    public int getItemPosition(Episode episode)
    {
        return values.indexOf(episode);
    }
    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Episode getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}