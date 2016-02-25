package com.topanimestream.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl;
import com.topanimestream.models.Favorite;
import com.topanimestream.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class FavoriteListAdapter extends RecyclerSwipeAdapter {
    private ArrayList<FavoriteItem> mItems;
    private FavoriteListAdapter.OnItemClickListener mItemClickListener;
    public static final int TYPE_NORMAL = 0, TYPE_LOADING = 1;
    private LayoutInflater mInflater;
    private Context mContext;
    protected SwipeItemRecyclerMangerImpl mItemManger = new SwipeItemRecyclerMangerImpl(this);

    public FavoriteListAdapter(Context context, ArrayList<Favorite> favorites) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mItems = new ArrayList<>();
        addItems(favorites);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case TYPE_LOADING:
                v = mInflater.inflate(R.layout.row_loading, parent, false);
                return new FavoriteListAdapter.LoadingHolder(v);
            case TYPE_NORMAL:
            default:
                v = mInflater.inflate(R.layout.row_favorite, parent, false);
                return new FavoriteListAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Favorite favorite = getItem(position);
        switch(getItemType(position))
        {
            case TYPE_NORMAL:
                final ViewHolder favoriteHolder = (ViewHolder) holder;
                favoriteHolder.txtTitle.setText(favorite.getAnime().getName());

                favoriteHolder.laySwipe.setShowMode(SwipeLayout.ShowMode.PullOut);
                favoriteHolder.laySwipe.addSwipeListener(new SimpleSwipeListener() {
                    @Override
                    public void onOpen(SwipeLayout layout) {

                    }
                });
                favoriteHolder.laySwipe.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
                    @Override
                    public void onDoubleClick(SwipeLayout layout, boolean surface) {

                    }
                });

                mItemManger.bindView(favoriteHolder.itemView, position);
                break;
            case TYPE_LOADING:
                break;
        }
    }

    public void addItems(ArrayList<Favorite> items) {
        if (items != null) {
            for (Favorite item : items) {
                mItems.add(new FavoriteItem(item));
            }
            notifyDataSetChanged();
        }
    }

    public int getItemType(int position) {
        if(mItems.get(position).isLoadingItem)
            return TYPE_LOADING;
        else
            return TYPE_NORMAL;
    }

    public Favorite getItem(int position) {
        return mItems.get(position).favorite;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addLoading() {
        mItems.add(new FavoriteItem(true));
        notifyDataSetChanged();
    }

    public void removeLoading() {
        if(getItemCount() > 0) {
            if (mItems.get(getItemCount() - 1).isLoadingItem)
                mItems.remove(getItemCount() - 1);
        }
    }

    public void deleteItem(int position)
    {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public boolean isLoading() {
        if (getItemCount() <= 0) return false;
        return getItemViewType(getItemCount() - 1) == TYPE_LOADING;
    }

    @Override
    public int getItemViewType(int position) {
        if(mItems.get(position).isLoadingItem)
            return TYPE_LOADING;
        else
            return TYPE_NORMAL;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.laySwipe;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, Favorite favorite, int position);
        void onDeleteClick(View v, Favorite favorite, int position);
    }

    public void setOnItemClickListener(FavoriteListAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.btnDelete)
        RelativeLayout btnDelete;

        @Bind(R.id.laySwipe)
        SwipeLayout laySwipe;

        @Bind(R.id.txtTitle)
        TextView txtTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            btnDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getAdapterPosition();
                Favorite item = getItem(position);
                switch(view.getId())
                {
                    case R.id.btnDelete:
                        mItemClickListener.onDeleteClick(view, item, position);
                        break;
                    default:
                        mItemClickListener.onItemClick(view, item, position);
                        break;
                }
            }
        }
    }

    class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

    }

    class FavoriteItem {
        Favorite favorite;
        boolean isLoadingItem = false;

        FavoriteItem(Favorite favorite) {
            this.favorite = favorite;
        }

        FavoriteItem(boolean loading) {
            this.isLoadingItem = loading;
        }

    }
}