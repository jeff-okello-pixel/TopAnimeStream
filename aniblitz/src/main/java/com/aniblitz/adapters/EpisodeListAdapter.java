package com.aniblitz.adapters;

import java.util.ArrayList;

import com.aniblitz.App;
import com.aniblitz.R;
import com.aniblitz.SQLiteHelper;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Episode;
import com.aniblitz.models.EpisodeInformations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class EpisodeListAdapter extends BaseAdapter{
	private final Context context;
	private ArrayList<Episode> values;
	private Resources re;
	private Activity act;
	private ViewHolder holder;
	private SQLiteHelper sqlite;
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
		this.act = (Activity)context;
		sqlite = new SQLiteHelper(act);
		prefs = PreferenceManager.getDefaultSharedPreferences(act);
		app = ((App)context.getApplicationContext());
		this.animeName = animeName;
		this.animeDescription = animeDescription;
		this.animePoster = animePoster;
        this.animeBackdrop = animeBackdrop;
        this.animeGenres = animeGenres;
        this.animeRating = animeRating;
	}
	public void update()
	{
		notifyDataSetChanged();
	}
	public void add(Episode episode)
	{
		values.add(episode);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View vi = convertView;
		Episode episode = values.get(position);
		if(convertView == null){
			vi = inflater.inflate(R.layout.row_episode, null);
			holder = new ViewHolder();
			holder.txtEpisodeNumber = (TextView) vi.findViewById(R.id.txtEpisodeNumber);
			holder.imgWatched = (ImageView) vi.findViewById(R.id.imgWatched);
			vi.setTag(holder);
		}else {
            holder = (ViewHolder) vi.getTag();
        }
		holder.imgWatched.setOnClickListener(new imageViewClickListener(position,holder.imgWatched));
        String episodeName = re.getString(R.string.episode) + episode.getEpisodeNumber();
        EpisodeInformations episodeInfo = episode.getEpisodeInformations();
        if(episodeInfo != null)
        {
            if(episodeInfo.getEpisodeName() != null && !episodeInfo.getEpisodeName().equals(""))
            {
                episodeName = episodeInfo.getEpisodeName();
            }
        }
        holder.txtEpisodeNumber.setText(episodeName);
		if(sqlite.isWatched(episode.getEpisodeId(), prefs.getString("prefLanguage", "1")))
		{
			holder.imgWatched.setBackgroundColor(Color.parseColor("#D9245169"));
			holder.imgWatched.setImageDrawable(re.getDrawable(R.drawable.ic_watched));
		}
		else
		{
			holder.imgWatched.setBackgroundColor(Color.parseColor("#00000000"));
			holder.imgWatched.setImageDrawable(re.getDrawable(R.drawable.ic_not_watched));
		}
		
		return vi;
	}
	  static class ViewHolder {
	        TextView txtEpisodeNumber;
	        ImageView imgWatched;
	    }
	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	  class imageViewClickListener implements OnClickListener {
		   int position;
		   ImageView imgView;
		    public imageViewClickListener(int pos, ImageView img)
		        {
		            position = pos;
		            imgView = img;
		        }

		    public void onClick(View v) {
		    	Episode episode = values.get(position);
		    	if(sqlite.isWatched(episode.getEpisodeId(), prefs.getString("prefLanguage", "1")))
				{
		    		sqlite.removeWatched(episode.getEpisodeId(),prefs.getString("prefLanguage", "1"));
		    		v.setBackgroundColor(Color.parseColor("#00000000"));
		    		imgView.setImageDrawable(re.getDrawable(R.drawable.ic_not_watched));
				}
				else
				{
					sqlite.addWatched(episode.getAnimeId(), animeName, animePoster, animeDescription, episode.getEpisodeId(), episode.getEpisodeNumber(), animeBackdrop, animeGenres, animeRating, Integer.valueOf(prefs.getString("prefLanguage", "1")));
					v.setBackgroundColor(Color.parseColor("#D9245169"));
					imgView.setImageDrawable(re.getDrawable(R.drawable.ic_watched));
				}
		     }
	  }



}