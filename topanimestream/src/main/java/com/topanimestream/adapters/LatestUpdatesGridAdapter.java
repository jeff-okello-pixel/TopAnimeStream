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
import com.topanimestream.models.Update;
import com.topanimestream.utilities.AnimUtils;
import com.topanimestream.utilities.ImageUtils;
import com.topanimestream.utilities.PixelUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;


public class LatestUpdatesGridAdapter extends HeaderRecyclerViewAdapter {
    private int mItemWidth, mItemHeight, mMargin, mColumns;
    private ArrayList<OverviewItem> mItems = new ArrayList<>();
    //	private ArrayList<Media> mData = new ArrayList<>();
    private LatestUpdatesGridAdapter.OnItemClickListener mItemClickListener;
    final public static int TYPE_NORMAL = 0, TYPE_LOADING = 1;

    public LatestUpdatesGridAdapter(Context context, ArrayList<Update> items, Integer columns) {
        mColumns = columns;

        int screenWidth = PixelUtils.getScreenWidth(context);
        mItemWidth = (screenWidth / columns);
        mItemHeight = (int) ((double) mItemWidth * 0.677);
        mMargin = PixelUtils.getPixelsFromDp(context, 2);

        setItems(items);
    }
    public interface OnItemClickListener {
        public void onItemClick(View v, Update item, int position);
    }
    @Override
    public RecyclerView.ViewHolder onCreateBasicItemViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_griditem_loading, parent, false);
                return new LatestUpdatesGridAdapter.LoadingHolder(v);
            case TYPE_NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_griditem, parent, false);
                return new LatestUpdatesGridAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindBasicItemView(RecyclerView.ViewHolder viewHolder, int position) {
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

        if (getBasicItemType(position) == TYPE_NORMAL) {
            final ViewHolder holder = (ViewHolder) viewHolder;
            final OverviewItem overviewItem = getItem(position);

            holder.coverImage.setVisibility(View.GONE);
            holder.txtTitle.setVisibility(View.GONE);
            holder.addedDate.setVisibility(View.GONE);
            holder.imgFlag.setVisibility(View.GONE);
            holder.txtAddedEpisodes.setVisibility(View.GONE);

            Update item = overviewItem.update;

            holder.txtTitle.setText(item.getAnime().getName());
            String addedEpisodes = App.getContext().getString(R.string.episode) + " " + item.getFirstEpisodeNumber();
            if(!item.getFirstEpisodeNumber().equals(item.getLastEpisodeNumber()))
                addedEpisodes += " " + App.getContext().getString(R.string.to) + " " + item.getLastEpisodeNumber();
            holder.txtAddedEpisodes.setText(addedEpisodes);

            long now = System.currentTimeMillis();
            long addedDate = item.getLastUpdatedDate().getTime();
            holder.addedDate.setText(DateUtils.getRelativeTimeSpanString(addedDate, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            holder.imgFlag.setImageResource(item.getLanguage().getFlagDrawable());

            String imagePath = item.getAnime().getBackdropPath();
            if(!item.getAnime().isMovie())
            {
                if(item.getEpisode() != null && item.getEpisode().getScreenshotHD() != null && !item.getEpisode().getScreenshotHD().equals(""))
                {
                    imagePath = item.getEpisode().getScreenshotHD();
                }
            }

            Picasso.with(holder.coverImage.getContext()).load(ImageUtils.resizeImage(App.getContext().getString(R.string.image_host_path) + imagePath, 500))
                    .resize(mItemWidth, mItemHeight)
                    .transform(DrawGradient.INSTANCE)
                    .into(holder.coverImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            overviewItem.isImageError = false;
                            AnimUtils.fadeIn(holder.coverImage);
                            AnimUtils.fadeIn(holder.txtTitle);
                            AnimUtils.fadeIn(holder.addedDate);
                            AnimUtils.fadeIn(holder.imgFlag);
                            AnimUtils.fadeIn(holder.txtAddedEpisodes);
                        }

                        @Override
                        public void onError() {
                            overviewItem.isImageError = true;
                            AnimUtils.fadeIn(holder.txtTitle);
                            AnimUtils.fadeIn(holder.addedDate);
                            AnimUtils.fadeIn(holder.imgFlag);
                            AnimUtils.fadeIn(holder.txtAddedEpisodes);
                        }
                    });
        }
    }

    public void setOnItemClickListener(LatestUpdatesGridAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    @Override
    public boolean useHeader() {
        return true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fake_header, parent, false);
        return new LatestUpdatesGridAdapter.HeaderItem(v);
    }

    @Override
    public void onBindHeaderView(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public boolean useFooter() {
        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindFooterView(RecyclerView.ViewHolder holder, int position) {

    }



    @Override
    public int getBasicItemCount() {
        return mItems.size();
    }

    @Override
    public int getBasicItemType(int position) {
        if(mItems.get(position).isLoadingItem)
            return TYPE_LOADING;
        else
            return TYPE_NORMAL;
    }

    public OverviewItem getItem(int position) {
        return mItems.get(position);
    }

    @DebugLog
    public void addLoading() {
        OverviewItem item = null;
        if (getBasicItemCount() > 0) {
            item = mItems.get(getBasicItemCount() - 1);
        }

        if (getBasicItemCount() == 0 || (item != null && !item.isLoadingItem)) {
            mItems.add(new OverviewItem(true));
            notifyDataSetChanged();
        }
    }

    @DebugLog
    public boolean isLoading() {
        if (getBasicItemCount() <= 0) return false;
        return getItemViewType(getBasicItemCount() - 1) == TYPE_LOADING;
    }

    @DebugLog
    public void setItems(ArrayList<Update> items) {
        // Add new items, if available
        if (items != null) {
            for (Update item : items) {
                mItems.add(new OverviewItem(item));
            }
        }
        notifyDataSetChanged();
        //Remove the isloading
        if (getBasicItemCount() <= 0) return;
        try {
            OverviewItem overviewItem = mItems.get(getBasicItemCount() - items.size() - 1);
            if (overviewItem.isLoadingItem) {
                mItems.remove(getBasicItemCount() - items.size() - 1);
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
        @Bind(R.id.imgFlag)
        ImageView imgFlag;
        @Bind(R.id.cover_image)
        ImageView coverImage;
        @Bind(R.id.txtTitle)
        TextView txtTitle;
        @Bind(R.id.addedDate)
        TextView addedDate;
        @Bind(R.id.txtAddedEpisodes)
        TextView txtAddedEpisodes;

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
                Update item = getItem(position).update;
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
        Update update;
        boolean isImageError = true;
        boolean isLoadingItem = false;

        OverviewItem(Update update) {
            this.update = update;
        }

        OverviewItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

    class HeaderItem extends RecyclerView.ViewHolder {

        public HeaderItem(View itemView) {
            super(itemView);
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