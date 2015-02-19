package com.topanimestream.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Review;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;


public class ReviewListAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Review> values;
    private ViewHolder holder;
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_SEPARATOR = 1;
    public static final int TYPE_NOREVIEW = 2;
    public static final int TYPE_ADDREVIEW = 3;
    private static final int TYPE_MAX_COUNT = 4;
    private TreeSet mSeparatorsSet = new TreeSet();
    private TreeSet mNoReviewSet = new TreeSet();
    private TreeSet mAddReviewSet = new TreeSet();
    App app;

    public ReviewListAdapter(Context context, ArrayList<Review> values) {
        this.context = context;
        this.values = values;
        app = ((App) context.getApplicationContext());
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Review review) {
        values.add(review);
    }
    public void addSeparatorItem(Review reviewSeparator) {
        assert (reviewSeparator.getSeparatorTitle() != null && !reviewSeparator.equals(""));
        values.add(reviewSeparator);
        // save separator position
        mSeparatorsSet.add(values.size() - 1);
        notifyDataSetChanged();
    }

    public void addNoReviewItem(Review noReview) {
        assert (noReview.getSeparatorTitle() != null && !noReview.equals(""));
        values.add(noReview);
        // save separator position
        mNoReviewSet.add(values.size() - 1);
        notifyDataSetChanged();
    }

    public void addAddReviewItem(Review addReview) {
        assert (addReview.getSeparatorTitle() != null && !addReview.equals(""));
        values.add(addReview);
        // save separator position
        mAddReviewSet.add(values.size() - 1);
        notifyDataSetChanged();
    }


    public void remove(Review review) {
        values.remove(review);
    }
    @Override
    public int getItemViewType(int position) {
        if(mSeparatorsSet.contains(position))
            return TYPE_SEPARATOR;
        else if(mNoReviewSet.contains(position))
            return TYPE_NOREVIEW;
        else if(mAddReviewSet.contains(position))
            return TYPE_ADDREVIEW;
        else
            return TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int type = getItemViewType(position);
        View vi = convertView;
        Review review = values.get(position);

        if (convertView == null) {
            switch(type)
            {
                case TYPE_ITEM:
                    vi = inflater.inflate(R.layout.row_review, null);
                    holder = new ViewHolder();
                    holder.txtArtRating = (TextView) vi.findViewById(R.id.txtArtRating);
                    holder.txtCharacterRating = (TextView) vi.findViewById(R.id.txtCharacterRating);
                    holder.txtStoryRating = (TextView) vi.findViewById(R.id.txtStoryRating);
                    holder.txtSoundRating = (TextView) vi.findViewById(R.id.txtSoundRating);
                    holder.txtEnjoymentRating = (TextView) vi.findViewById(R.id.txtEnjoymentRating);
                    holder.imgProfilePic = (ImageView) vi.findViewById(R.id.imgProfilePic);
                    holder.txtOverallRating = (TextView) vi.findViewById(R.id.txtOverallRating);
                    holder.txtReview = (TextView) vi.findViewById(R.id.txtReview);
                    break;
                case TYPE_SEPARATOR:
                    vi = inflater.inflate(R.layout.separator_reviews, null);
                    holder = new ViewHolder();
                    holder.txtSeparatorTitle = (TextView) vi.findViewById(R.id.txtSeparatorTitle);
                    break;
                case TYPE_NOREVIEW:
                    vi = inflater.inflate(R.layout.row_empty_review, null);
                    holder = new ViewHolder();
                    holder.txtEmptyReview = (TextView) vi.findViewById(R.id.txtEmptyReview);
                    break;
                case TYPE_ADDREVIEW:
                    vi = inflater.inflate(R.layout.row_add_review, null);
                    holder = new ViewHolder();
                    holder.txtAddReview = (TextView) vi.findViewById(R.id.txtAddReview);
                    break;
            }
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        switch(type)
        {
            case TYPE_ITEM:
                holder.txtArtRating.setText(review.getArtRating() / 2 + "/5");
                holder.txtCharacterRating.setText(review.getCharacterRating() / 2 + "/5");
                holder.txtStoryRating.setText(review.getStoryRating() / 2 + "/5");
                holder.txtSoundRating.setText(review.getSoundRating() /2 + "/5");
                holder.txtEnjoymentRating.setText(review.getEnjoymentRating() / 2 + "/5");
                if(review.getAccount() != null)
                    App.imageLoader.displayImage(App.getContext().getString(R.string.image_host_path) + review.getAccount().getProfilePic(), holder.imgProfilePic);
                holder.txtOverallRating.setText(context.getString(R.string.overall_rating) + review.getOverallRating() / 2 + "/5");
                holder.txtReview.setText(review.getValue());
                break;
            case TYPE_SEPARATOR:
                holder.txtSeparatorTitle.setText(review.getSeparatorTitle());
                break;
            case TYPE_NOREVIEW:
                holder.txtEmptyReview.setText(review.getSeparatorTitle());
                break;
            case TYPE_ADDREVIEW:
                holder.txtAddReview.setText(review.getSeparatorTitle());
                break;
        }


        return vi;
    }

    public void clear() {
        values.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView txtArtRating;
        TextView txtCharacterRating;
        TextView txtStoryRating;
        TextView txtSoundRating;
        TextView txtEnjoymentRating;
        ImageView imgProfilePic;
        TextView txtOverallRating;
        TextView txtReview;
        TextView txtSeparatorTitle;
        TextView txtEmptyReview;
        TextView txtAddReview;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public Review getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}