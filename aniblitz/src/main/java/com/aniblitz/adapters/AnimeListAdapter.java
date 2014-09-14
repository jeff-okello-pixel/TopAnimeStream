package com.aniblitz.adapters;

import java.util.ArrayList;
import java.util.List;

import com.aniblitz.App;
import com.aniblitz.R;
import com.aniblitz.Utils;
import com.aniblitz.models.Anime;
import com.aniblitz.models.AnimeInformation;
import com.aniblitz.models.Genre;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class AnimeListAdapter extends BaseAdapter{
	private final Context context;
	private ArrayList<Anime> values;
	private Activity act;
	private ViewHolder holder;
	private ArrayList<Anime> origData;
    private SharedPreferences prefs;
	App app;
	
	public AnimeListAdapter(Context context, ArrayList<Anime> values) {
		this.context = context;
		this.values = values;
		this.act = (Activity)context;
		this.origData = values;
		app = ((App)context.getApplicationContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	public void update()
	{
		notifyDataSetChanged();
	}
	public void add(Anime anime)
	{
		values.add(anime);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View vi = convertView;
		Anime anime = values.get(position);
		if(convertView == null){
			vi = inflater.inflate(R.layout.row_anime, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) vi.findViewById(R.id.txtName);
			holder.txtGenres = (TextView) vi.findViewById(R.id.txtGenres);
            holder.rtbRating = (RatingBar) vi.findViewById(R.id.rtbRating);
			holder.txtDescription = (TextView)vi.findViewById(R.id.txtDescription);
			holder.imgPoster = (ImageView) vi.findViewById(R.id.imgPoster);
			vi.setTag(holder);
		}else {
            holder = (ViewHolder) vi.getTag();
        }
        if(anime.getRating() != null)
            holder.rtbRating.setRating((float)Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));
		holder.txtName.setText(anime.getName());
		holder.txtGenres.setText(anime.getGenresFormatted());
        if(holder.txtGenres.getText().equals(""))
            holder.txtGenres.setVisibility(View.GONE);
		holder.imgPoster.setImageResource(android.R.color.transparent);

        String language = prefs.getString("prefLanguage", "1");
        for(AnimeInformation animeInfo : anime.getAnimeInformations())
        {
            if(String.valueOf(animeInfo.getLanguageId()).equals(language))
            {
                if(animeInfo.getOverview() != null && !animeInfo.getOverview().equals(""))
                    holder.txtDescription.setText(animeInfo.getOverview().trim());
                else if(animeInfo.getDescription() != null && !animeInfo.getDescription().equals(""))
                    holder.txtDescription.setText(animeInfo.getDescription().trim());
                else
                    holder.txtDescription.setVisibility(View.GONE);

                break;

            }
        }
		App.imageLoader.displayImage(anime.getPosterPath("185"), holder.imgPoster);

		return vi;
	}
    public void clear()
    {
        values.clear();
        notifyDataSetChanged();
    }
	  static class ViewHolder {
	        TextView txtName;
	        TextView txtGenres;
            RatingBar rtbRating;
	        TextView txtDescription;
	        ImageView imgPoster;
	    }
	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Anime getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}