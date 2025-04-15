package com.example.bookmark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bookmark.R;
import com.example.bookmark.model.SpinnerModel;


import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    private List<SpinnerModel> list;
    private Context context;

    public SpinnerAdapter(List<SpinnerModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        SpinnerHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
            holder = new SpinnerHolder(view);
            view.setTag(holder);
        } else {
            holder = (SpinnerHolder) view.getTag();
        }

        SpinnerModel model = list.get(position);
        holder.activityTypeTV.setText(model.getActivityType());

        return view;
    }

    static class SpinnerHolder {
        private TextView activityTypeTV;
        
        public SpinnerHolder(View itemView) {
            activityTypeTV = itemView.findViewById(R.id.activityTypeTV);
        }
    }
}