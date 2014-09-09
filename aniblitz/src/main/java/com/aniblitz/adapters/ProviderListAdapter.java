package com.aniblitz.adapters;

import java.util.ArrayList;

import com.aniblitz.App;
import com.aniblitz.R;
import com.aniblitz.Utils;
import com.aniblitz.adapters.EpisodeListAdapter.imageViewClickListener;
import com.aniblitz.models.Anime;
import com.aniblitz.models.Episode;
import com.aniblitz.models.Mirror;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class ProviderListAdapter extends BaseAdapter  {
	private final Context context;
	private ArrayList<Mirror> values;
	private ViewHolder holder;
	
	public ProviderListAdapter(Context context, ArrayList<Mirror> values) {
		this.context = context;
		this.values = values;
	}
	public void update()
	{
		notifyDataSetChanged();
	}
	public void add(Mirror mirror)
	{
		values.add(mirror);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View vi = convertView;
		Mirror mirror = values.get(position);
		if(convertView == null){
			vi = inflater.inflate(R.layout.row_provider, null);
			holder = new ViewHolder();
			holder.txtProviderName = (TextView) vi.findViewById(R.id.txtProviderName);
			//holder.imgBroken = (ImageView)vi.findViewById(R.id.imgBroken);
			vi.setTag(holder);
		}else {
            holder = (ViewHolder) vi.getTag();
        }
		holder.txtProviderName.setText(mirror.getProvider().getName());
		//holder.imgBroken.setOnClickListener(new imageViewClickListener(position,holder.imgBroken));

		return vi;
	}
	  static class ViewHolder {
	        TextView txtProviderName;
	        ImageView imgBroken;
	    }
	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Mirror getItem(int position) {
		return values.get(position);
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
		    	Mirror mirror = values.get(position);

		     }
	  }
}