package com.topanimestream.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Vote;
import com.topanimestream.utilities.Utils;

import java.util.ArrayList;


public class VoteListAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Vote> values;
    private Resources re;
    private Activity act;
    private ViewHolder holder;
    App app;

    public VoteListAdapter(Context context, ArrayList<Vote> values) {
        this.context = context;
        this.values = values;
        this.re = this.context.getResources();
        this.act = (Activity) context;
        app = ((App) context.getApplicationContext());
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Vote vote) {
        values.add(vote);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vi = convertView;
        Vote vote = values.get(position);
        if (convertView == null) {
            vi = inflater.inflate(R.layout.row_vote, null);
            holder = new ViewHolder();
            holder.imgPoster = (ImageView) vi.findViewById(R.id.imgPoster);
            holder.txtName = (TextView) vi.findViewById(R.id.txtName);
            holder.rtbRating = (RatingBar)vi.findViewById(R.id.rtbRating);
            holder.txtVote = (TextView) vi.findViewById(R.id.txtVote);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        holder.txtName.setText(vote.getAnime().getPosterPath("185"));
        holder.txtName.setText(vote.getAnime().getName());
        holder.rtbRating.setRating((float) Utils.roundToHalf(vote.getValue() != 0 ? vote.getValue() / 2 : vote.getValue()));
        holder.txtVote.setText(String.valueOf(Utils.roundToHalf(vote.getValue() != 0 ? vote.getValue() / 2 : vote.getValue())));

        return vi;
    }

    static class ViewHolder {
        ImageView imgPoster;
        TextView txtName;
        RatingBar rtbRating;
        TextView txtVote;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Vote getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}