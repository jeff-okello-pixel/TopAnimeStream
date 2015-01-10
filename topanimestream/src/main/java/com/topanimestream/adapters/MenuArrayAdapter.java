package com.topanimestream.adapters;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.topanimestream.R;


public class MenuArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
	private Resources re;
	public MenuArrayAdapter(Context context, String[] values) {
		super(context, R.layout.menurow, values);
		this.context = context;
		this.values = values;
		this.re = this.context.getResources();
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			convertView = inflater.inflate(R.layout.menurow, parent, false);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.txtMenu);
		textView.setText(values[position]);
		Drawable textViewIcon = null;
		if(values[position].equals(re.getString(R.string.menu_favorites)))
		{
			textViewIcon = re.getDrawable(R.drawable.ic_favorite);
		}
		else if(values[position].equals(re.getString(R.string.menu_history)))
		{
			textViewIcon = re.getDrawable(R.drawable.ic_history);
		}
		else if(values[position].equals(re.getString(R.string.menu_share)))
		{
			textViewIcon = re.getDrawable(R.drawable.ic_share);
		}
		else if(values[position].equals(re.getString(R.string.menu_settings)))
		{
			textViewIcon = re.getDrawable(R.drawable.ic_tools);
		}
        else if(values[position].equals(re.getString(R.string.menu_logout)))
        {
            textViewIcon = re.getDrawable(R.drawable.ic_action_logout);
        }
        else if(values[position].equals(re.getString(R.string.menu_pro)))
        {
            textViewIcon = re.getDrawable(R.drawable.ic_action_like);
        }
		textView.setCompoundDrawablesWithIntrinsicBounds(textViewIcon, null, null, null);
		
		convertView.setPadding(20, 20, 20, 20);
		return convertView;
	}
}