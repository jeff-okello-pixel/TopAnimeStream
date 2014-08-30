package com.aniblitz.adapters;

import java.util.ArrayList;
import java.util.List;

import com.aniblitz.App;
import com.aniblitz.R;
import com.aniblitz.Utils;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Genre;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.widget.SectionIndexer;
import android.widget.TextView;


public class AnimeListAdapter extends BaseAdapter implements SectionIndexer, Filterable  {
	private final Context context;
	private ArrayList<Anime> values;
	private Resources re;
	private Activity act;
	private ViewHolder holder;
	private ArrayList<Anime> origData;
	private static String sections = "#abcdefghijklmnopqrstuvwxyz";
	App app;
	
	public AnimeListAdapter(Context context, ArrayList<Anime> values) {
		this.context = context;
		this.values = values;
		this.re = this.context.getResources();
		this.act = (Activity)context;
		this.origData = values;
		app = ((App)context.getApplicationContext());
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
			holder.txtDescription = (TextView)vi.findViewById(R.id.txtDescription);
			holder.imgPoster = (ImageView) vi.findViewById(R.id.imgPoster);
			vi.setTag(holder);
		}else {
            holder = (ViewHolder) vi.getTag();
        }
		holder.txtName.setText(anime.getName());
		holder.txtGenres.setText(anime.getGenresFormatted());
		holder.txtDescription.setText(anime.getDescription());
		holder.imgPoster.setImageResource(android.R.color.transparent);
		App.imageLoader.displayImage(anime.getPosterPath("185"), holder.imgPoster);

		return vi;
	}
	  static class ViewHolder {
	        TextView txtName;
	        TextView txtGenres;
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
	@Override
	public Object[] getSections() {
		String[] sectionsArr = new String[sections.length()];
		for (int i=0; i < sections.length(); i++)
		  sectionsArr[i] = "" + sections.charAt(i);
		  return sectionsArr;
	}
	@Override
	public int getPositionForSection(int sectionIndex) {
		for (int i=0; i < this.getCount(); i++) {
	      String item = this.getItem(i).getName().toLowerCase();
	      
	      if(sections.charAt(sectionIndex) == '#')
	      {
	    	if(i < 10)
	    	{
			    if (item.charAt(0) == sections.charAt(sectionIndex) || Utils.isNumeric(String.valueOf(item.charAt(0))) || item.charAt(0) == '.')
			    {
			    	return i;
			    }
	    	}
	      }
	      if (item.charAt(0) == sections.charAt(sectionIndex))
	          return i;
		}
		return 0;
	}
	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}
	@Override
	public Filter getFilter(){
	   return new Filter(){

		     @Override
		     protected FilterResults performFiltering(CharSequence constraint) {
		             constraint = constraint.toString().toLowerCase();
		             FilterResults result = new FilterResults();
	
		                if (constraint != null && constraint.toString().length() > 0) {
		                  List<Anime> founded = new ArrayList<Anime>();
		                        for(Anime item: origData){
		                            if(item.getName().toLowerCase().contains(constraint)){
		                                founded.add(item);
		                            }
		                    }
		                        result.values = founded;
		                        result.count = founded.size();
		                    }else {
		                        result.values = origData;
		                        result.count = origData.size();
		                    }
		            return result;
	
	
		    }
		    @Override
		    protected void publishResults(CharSequence constraint, FilterResults results) {
			    values = (ArrayList<Anime>)results.values;
			    notifyDataSetChanged();
	
		    }

	    };
	   
	}
}