package com.example.sheffieldtrains;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrainTimesRecyclerViewAdapter extends RecyclerView.Adapter<TrainTimesRecyclerViewAdapter.ViewHolder> {
    private List<TrainTime> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    TrainTimesRecyclerViewAdapter(Context context, List<TrainTime> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.train_time_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrainTime trainTime = mData.get(position);

        String time = trainTime.time;
        holder.trainTimeLabel.setText(time);

        String name = trainTime.name;
        holder.trainNameLabel.setText(name);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView trainTimeLabel;
        TextView trainNameLabel;

        ViewHolder(View itemView) {
            super(itemView);
            trainTimeLabel = itemView.findViewById(R.id.time_label);
            trainNameLabel = itemView.findViewById(R.id.name_label);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id).name;
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}