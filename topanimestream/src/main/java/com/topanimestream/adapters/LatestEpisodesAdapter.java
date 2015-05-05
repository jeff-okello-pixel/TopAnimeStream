package com.topanimestream.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Link;
import com.topanimestream.utilities.Utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;


public class LatestEpisodesAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Link> values;
    private ViewHolder holder;
    App app;

    public LatestEpisodesAdapter(Context context, ArrayList<Link> values) {
        this.context = context;
        this.values = values;
        app = ((App) context.getApplicationContext());
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Link link) {
        values.add(link);
    }

    public void remove(Link link) {
        values.remove(link);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vi = convertView;
        Link link = values.get(position);
        if (convertView == null) {
            vi = inflater.inflate(R.layout.row_latest_episode, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) vi.findViewById(R.id.txtName);
            holder.txtEpisodeNumber = (TextView) vi.findViewById(R.id.txtEpisodeNumber);
            holder.txtAddedDate = (TextView) vi.findViewById(R.id.txtAddedDate);
            holder.imgScreenshot = (ImageView) vi.findViewById(R.id.imgScreenshot);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.txtName.setText(link.getAnime().getName());
        holder.txtEpisodeNumber.setText(context.getString(R.string.episode) + " " + link.getEpisode().getEpisodeNumber());
        Timestamp timeStamp = Timestamp.valueOf(link.getAddedDate());
        holder.txtAddedDate.setText(DateUtils.getRelativeTimeSpanString(timeStamp.getTime(),System.currentTimeMillis()/1000, 0));
        App.imageLoader.displayImage(context.getString(R.string.image_host_path) + link.getEpisode().getScreenshotHD());

        return vi;
    }

    public void clear() {
        values.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtEpisodeNumber;
        TextView txtAddedDate;
        ImageView imgScreenshot;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Link getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}