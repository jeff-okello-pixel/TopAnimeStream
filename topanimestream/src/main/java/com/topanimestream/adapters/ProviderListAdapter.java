package com.topanimestream.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import com.topanimestream.models.Mirror;
import com.topanimestream.R;


public class ProviderListAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<Mirror> values;
    private ViewHolder holder;

    public ProviderListAdapter(Context context, ArrayList<Mirror> values) {
        this.context = context;
        this.values = values;
    }

    public void update() {
        notifyDataSetChanged();
    }

    public void add(Mirror mirror) {
        values.add(mirror);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View vi = convertView;
        Mirror mirror = values.get(position);
        if (convertView == null) {
            vi = inflater.inflate(R.layout.row_provider, null);
            holder = new ViewHolder();
            holder.txtProviderName = (TextView) vi.findViewById(R.id.txtProviderName);
            //holder.imgBroken = (ImageView)vi.findViewById(R.id.imgBroken);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        String[] recommendProvider = new String[]{"vk", "uploadcrazy", "videocrazy", "yourupload", "vidcrazy", "auengine"};
        if (Arrays.asList(recommendProvider).contains(mirror.getProvider().getName().toLowerCase()) && mirror.isVisible())
            holder.txtProviderName.setText(mirror.getProvider().getName() + " " + context.getString(R.string.recommended));
        else if (!mirror.isVisible())
            holder.txtProviderName.setText(mirror.getProvider().getName() + " " + Html.fromHtml("<font color=#E50000>" + context.getString(R.string.bad) + "</font>"));
        else
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

        public imageViewClickListener(int pos, ImageView img) {
            position = pos;
            imgView = img;
        }

        public void onClick(View v) {
            Mirror mirror = values.get(position);

        }
    }
}