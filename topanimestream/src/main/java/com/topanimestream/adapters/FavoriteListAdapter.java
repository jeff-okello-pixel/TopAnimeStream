package com.topanimestream.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import com.topanimestream.App;
import com.topanimestream.models.Favorite;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.Utils;
import com.topanimestream.models.Anime;
import com.topanimestream.R;


public class FavoriteListAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Favorite> values;
    private ViewHolder holder;

    public FavoriteListAdapter(Context context, ArrayList<Favorite> values) {
        this.context = context;
        this.values = values;
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Favorite favorite) {
        values.add(favorite);
    }

    public void remove(Favorite favorite) {
        values.remove(favorite);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vi = convertView;
        Anime anime = values.get(position).getAnime();
        if (convertView == null) {
            vi = inflater.inflate(R.layout.row_anime, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) vi.findViewById(R.id.txtName);
            holder.txtGenres = (TextView) vi.findViewById(R.id.txtGenres);
            holder.rtbRating = (RatingBar) vi.findViewById(R.id.rtbRating);
            holder.txtDescription = (TextView) vi.findViewById(R.id.txtDescription);
            holder.imgPoster = (ImageView) vi.findViewById(R.id.imgPoster);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        if (anime.getRating() != null)
            holder.rtbRating.setRating((float) Utils.roundToHalf(anime.getRating() != 0 ? anime.getRating() / 2 : anime.getRating()));
        holder.txtName.setText(anime.getName());
        holder.txtGenres.setText(anime.getGenresFormatted());
        if (holder.txtGenres.getText().equals(""))
            holder.txtGenres.setVisibility(View.GONE);
        holder.imgPoster.setImageResource(android.R.color.transparent);
        holder.txtDescription.setText(anime.getSynopsis());
        App.imageLoader.displayImage(ImageUtils.resizeImage(context.getString(R.string.image_host_path) + anime.getPosterPath(), 185), holder.imgPoster);

        return vi;
    }

    public void clear() {
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
    public Favorite getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}