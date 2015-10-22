package com.topanimestream.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.topanimestream.App;
import com.topanimestream.R;
import com.topanimestream.models.Anime;
import com.topanimestream.models.Link;
import com.topanimestream.utilities.AnimUtils;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.PixelUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;


public class LatestEpisodesGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int mItemWidth, mItemHeight, mMargin, mColumns;
    private ArrayList<OverviewItem> mItems = new ArrayList<>();
    //	private ArrayList<Media> mData = new ArrayList<>();
    private LatestEpisodesGridAdapter.OnItemClickListener mItemClickListener;
    final int NORMAL = 0, LOADING = 1;

    public LatestEpisodesGridAdapter(Context context, ArrayList<Link> items, Integer columns) {
        mColumns = columns;

        int screenWidth = PixelUtils.getScreenWidth(context);
        mItemWidth = (screenWidth / columns);
        mItemHeight = (int) ((double) mItemWidth * 0.677);
        mMargin = PixelUtils.getPixelsFromDp(context, 2);

        setItems(items);
    }
    public interface OnItemClickListener {
        public void onItemClick(View v, Link item, int position);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_griditem_loading, parent, false);
                return new LatestEpisodesGridAdapter.LoadingHolder(v);
            case NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.link_griditem, parent, false);
                return new LatestEpisodesGridAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        int double_margin = mMargin * 2;
        int top_margin = (position < mColumns) ? mMargin * 2 : mMargin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.height = mItemHeight;
        layoutParams.width = mItemWidth;
        if (position % mColumns == mColumns - 1) {
            layoutParams.setMargins(mMargin, top_margin, double_margin, mMargin);
        } else {
            layoutParams.setMargins(mMargin, top_margin, mMargin, mMargin);
        }
        viewHolder.itemView.setLayoutParams(layoutParams);

        if (getItemViewType(position) == NORMAL) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            final OverviewItem overviewItem = getItem(position);
            Link item = overviewItem.link;


            holder.title.setText(item.getAnime().getName());
            Timestamp timeStamp = Timestamp.valueOf(item.getAddedDate().replace("T", " "));
            long now = System.currentTimeMillis();
            long addedDate = timeStamp.getTime();
            holder.addedDate.setText(DateUtils.getRelativeTimeSpanString(addedDate, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));


            holder.coverImage.setVisibility(View.GONE);
            holder.title.setVisibility(View.GONE);
            holder.addedDate.setVisibility(View.GONE);

            if (item.getEpisode().getScreenshotHD() != null && !item.getEpisode().getScreenshotHD().equals("")) {
                Picasso.with(holder.coverImage.getContext()).load(ImageUtils.resizeImage(App.getContext().getString(R.string.image_host_path) + item.getEpisode().getScreenshotHD(), ImageUtils.ImageSize.w500.getValue()))
                        .resize(mItemWidth, mItemHeight)
                        .transform(DrawGradient.INSTANCE)
                        .into(holder.coverImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                overviewItem.isImageError = false;
                                AnimUtils.fadeIn(holder.coverImage);
                                AnimUtils.fadeIn(holder.title);
                                AnimUtils.fadeIn(holder.addedDate);
                            }

                            @Override
                            public void onError() {
                                overviewItem.isImageError = true;
                                AnimUtils.fadeIn(holder.title);
                                AnimUtils.fadeIn(holder.addedDate);
                            }
                        });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setOnItemClickListener(LatestEpisodesGridAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isLoadingItem) {
            return LOADING;
        }
        return NORMAL;
    }

    public OverviewItem getItem(int position) {
        if (position < 0 || mItems.size() <= position) return null;
        return mItems.get(position);
    }

    @DebugLog
    public void addLoading() {
        OverviewItem item = null;
        if (getItemCount() != 0) {
            item = mItems.get(getItemCount() - 1);
        }

        if (getItemCount() == 0 || (item != null && !item.isLoadingItem)) {
            mItems.add(new OverviewItem(true));
            notifyDataSetChanged();
        }
    }

    @DebugLog
    public boolean isLoading() {
        if (getItemCount() <= 0) return false;
        return getItemViewType(getItemCount() - 1) == LOADING;
    }

    @DebugLog
    public void setItems(ArrayList<Link> items) {
        // Add new items, if available
        if (null != items) {
            for (Link item : items) {
                mItems.add(new OverviewItem(item));
            }
        }
        notifyDataSetChanged();
        //Remove the isloading
        if (getItemCount() <= 0) return;
        try {
            OverviewItem overviewItem = mItems.get(getItemCount() - items.size() - 1);
            if (overviewItem.isLoadingItem) {
                mItems.remove(getItemCount() - items.size() - 1);
                notifyDataSetChanged();
            }
        }catch(ArrayIndexOutOfBoundsException e)
        {
            //theres no loading overview
            return;
        }

    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        @Bind(R.id.focus_overlay)
        View focusOverlay;
        @Bind(R.id.cover_image)
        ImageView coverImage;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.addedDate)
        TextView addedDate;

        private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusOverlay.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            coverImage.setMinimumHeight(mItemHeight);

            itemView.setOnFocusChangeListener(mOnFocusChangeListener);
        }

        public ImageView getCoverImage() {
            return coverImage;
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getPosition();
                Link item = getItem(position).link;
                mItemClickListener.onItemClick(view, item, position);
            }
        }

    }

    class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(mItemHeight);
        }

    }

    class OverviewItem {
        Link link;
        boolean isImageError = true;
        boolean isLoadingItem = false;

        OverviewItem(Link link) {
            this.link = link;
        }

        OverviewItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

    private static class DrawGradient implements Transformation {
        static Transformation INSTANCE = new DrawGradient();

        @Override
        public Bitmap transform(Bitmap src) {
            // Code borrowed from https://stackoverflow.com/questions/23657811/how-to-mask-bitmap-with-lineargradient-shader-properly
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(overlay);

            canvas.drawBitmap(src, 0, 0, null);
            src.recycle();

            Paint paint = new Paint();
            float gradientHeight = h / 1.5f;
            LinearGradient shader = new LinearGradient(0, h - gradientHeight, 0, h, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawRect(0, h - gradientHeight, w, h, paint);
            return overlay;
        }

        @Override
        public String key() {
            return "gradient()";
        }
    }
}