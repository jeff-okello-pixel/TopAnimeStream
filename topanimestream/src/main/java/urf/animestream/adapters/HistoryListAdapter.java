package urf.animestream.adapters;

import java.util.ArrayList;

import urf.animestream.App;
import urf.animestream.R;
import urf.animestream.models.Anime;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class HistoryListAdapter extends BaseAdapter  {
	private final Context context;
	private ArrayList<Anime> values;
	private Resources re;
	private Activity act;
	private ViewHolder holder;
	App app;
	
	public HistoryListAdapter(Context context, ArrayList<Anime> values) {
		this.context = context;
		this.values = values;
		this.re = this.context.getResources();
		this.act = (Activity)context;
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
			vi = inflater.inflate(R.layout.row_history, null);
			holder = new ViewHolder();
			holder.txtName = (TextView) vi.findViewById(R.id.txtName);
			//holder.txtDescription = (TextView)vi.findViewById(R.id.txtDescription);
			//holder.imgPoster = (ImageView) vi.findViewById(R.id.imgPoster);
			holder.txtEpisodeNumber =(TextView)vi.findViewById(R.id.txtEpisodeNumber);
			vi.setTag(holder);
		}else {
            holder = (ViewHolder) vi.getTag();
        }
		holder.txtName.setText(anime.getName());
		//holder.txtDescription.setText(anime.getDescription());
		//holder.imgPoster.setImageResource(android.R.color.transparent);
		//We only save 1 episode in the history, a new record is created for every episode
		holder.txtEpisodeNumber.setText(re.getString(R.string.episode) + anime.getEpisodes().get(0).getEpisodeNumber());
		//App.imageLoader.displayImage(anime.getPosterPath(), holder.imgPoster);

		return vi;
	}
	  static class ViewHolder {
	        TextView txtName;
	        TextView txtEpisodeNumber;
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