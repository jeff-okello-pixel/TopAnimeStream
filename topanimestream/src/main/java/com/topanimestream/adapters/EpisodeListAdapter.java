package com.topanimestream.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.topanimestream.App;
import com.topanimestream.SQLiteHelper;
import com.topanimestream.models.Episode;
import com.topanimestream.models.EpisodeInformations;
import com.topanimestream.R;


public class EpisodeListAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Episode> values;
    private Resources re;
    private Activity act;
    private ViewHolder holder;
    private SharedPreferences prefs;
    private TextView txtEpisodeNumber;
    private ImageView imgWatched;
    private String animeName;
    private String animeDescription;
    private String animePoster;
    private String animeBackdrop;
    private String animeGenres;
    private String animeRating;
    App app;

    public EpisodeListAdapter(Context context, ArrayList<Episode> values, String animeName, String animeDescription, String animePoster, String animeBackdrop, String animeGenres, String animeRating) {
        this.context = context;
        this.values = values;
        this.re = this.context.getResources();
        this.act = (Activity) context;
        prefs = PreferenceManager.getDefaultSharedPreferences(act);
        app = ((App) context.getApplicationContext());
        this.animeName = animeName;
        this.animeDescription = animeDescription;
        this.animePoster = animePoster;
        this.animeBackdrop = animeBackdrop;
        this.animeGenres = animeGenres;
        this.animeRating = animeRating;
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Episode episode) {
        values.add(episode);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vi = convertView;
        Episode episode = values.get(position);
        if (convertView == null) {
            vi = inflater.inflate(R.layout.row_episode, null);
            holder = new ViewHolder();
            holder.txtEpisodeNumber = (TextView) vi.findViewById(R.id.txtEpisodeNumber);
            holder.txtEpisodeName = (TextView) vi.findViewById(R.id.txtEpisodeName);
            holder.imgWatched = (ImageView) vi.findViewById(R.id.imgWatched);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        holder.imgWatched.setOnClickListener(new imageViewClickListener(position, holder.imgWatched));
        holder.txtEpisodeNumber.setText(re.getString(R.string.episode) + " " + episode.getEpisodeNumber());
        EpisodeInformations episodeInfo = episode.getEpisodeInformations();
        if (episodeInfo != null) {
            if (episodeInfo.getEpisodeName() != null && !episodeInfo.getEpisodeName().equals("")) {
                holder.txtEpisodeName.setVisibility(View.VISIBLE);
                holder.txtEpisodeName.setText(episodeInfo.getEpisodeName());
            } else {
                holder.txtEpisodeName.setVisibility(View.GONE);
            }
        } else {
            holder.txtEpisodeName.setVisibility(View.GONE);
        }
        SQLiteHelper sqlite = new SQLiteHelper(act);
        if (sqlite.isWatched(episode.getEpisodeId(), prefs.getString("prefLanguage", "1"))) {
            holder.imgWatched.setBackgroundColor(Color.parseColor("#D9245169"));
            holder.imgWatched.setImageDrawable(re.getDrawable(R.drawable.ic_watched));
        } else {
            holder.imgWatched.setBackgroundColor(Color.parseColor("#00000000"));
            holder.imgWatched.setImageDrawable(re.getDrawable(R.drawable.ic_not_watched));
        }
        sqlite.close();

        return vi;
    }

    static class ViewHolder {
        TextView txtEpisodeNumber;
        TextView txtEpisodeName;
        ImageView imgWatched;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Episode getItem(int position) {
        return values.get(position);
    }
    public ArrayList<Episode> getAllEpisodes()
    {
        return values;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    class imageViewClickListener implements OnClickListener {
        int position;
        ImageView imgView;

        public imageViewClickListener(int pos, ImageView img) {
            position = pos;
            imgView = img;
        }

        public void onClick(View v) {
            Episode episode = values.get(position);
            SQLiteHelper sqlite = new SQLiteHelper(act);
            if (sqlite.isWatched(episode.getEpisodeId(), prefs.getString("prefLanguage", "1"))) {
                sqlite.removeWatched(episode.getEpisodeId(), prefs.getString("prefLanguage", "1"));
                v.setBackgroundColor(Color.parseColor("#00000000"));
                imgView.setImageDrawable(re.getDrawable(R.drawable.ic_not_watched));
            } else {
                sqlite.addWatched(episode.getAnimeId(), animeName, animePoster, animeDescription, episode.getEpisodeId(), episode.getEpisodeNumber(), animeBackdrop, animeGenres, animeRating, Integer.valueOf(prefs.getString("prefLanguage", "1")));
                v.setBackgroundColor(Color.parseColor("#D9245169"));
                imgView.setImageDrawable(re.getDrawable(R.drawable.ic_watched));
            }
            sqlite.close();
        }
    }


}