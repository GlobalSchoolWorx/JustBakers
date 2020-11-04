package com.ecom.justbakers.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.ecom.justbakers.R;
import com.ecom.justbakers.orders.OrderClass;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class OrderDetailsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final double CONFIRMATION_TIME = 0.5;
    private static final double PROCESSING_TIME = 12;
    private Context mContext;
    private String[] mDataset;
    private OrderClass mOrder;

    public OrderDetailsListAdapter (Context mContext, OrderClass mOrder, String[] dataSet) {
        this.mContext = mContext;
        this.mDataset = dataSet;
        this.mOrder = mOrder;
    }
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public com.github.vipulasri.timelineview.TimelineView timelineView;
        public TextView textView;
        public ViewHolder(View v) {
            super(v);
            timelineView = v.findViewById(R.id.timeLineView);
            textView = v.findViewById(R.id.textViewOrderDetail);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // create a new view


        View v = inflater.inflate(R.layout.order_details_recyle_item_layout , parent, false);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams((int) (parent.getWidth() * 0.25),ViewGroup.LayoutParams.WRAP_CONTENT);

        v.setLayoutParams(lp);


        ViewHolder vh = new ViewHolder(v);


        return vh;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //if you need three fix imageview in width

        //if you need same height as width you can set devicewidth in holder.image_view.getLayoutParams().height
  //      holder.itemView.getLayoutParams().height = deviceheight;

        switch (position) {
            case 0: {
                ((ViewHolder) holder).textView.setText(mDataset[position]);
                ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                break;
            }
            case 1: {
                ((ViewHolder) holder).textView.setText(mDataset[position]);
                if (mOrder.getStatus().equals("confirmed") || mOrder.getStatus().equals("delivered"))
                  ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                else
                  ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));
                break;
            }
            case 2: {
                ((ViewHolder) holder).textView.setText(mDataset[position]);
                if (mOrder.getStatus().equals("processing") || mOrder.getStatus().equals("delivered"))
                  ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                else
                  ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));

                break;
            }
            case 3: {
                ((ViewHolder) holder).textView.setText(mDataset[position]);
                if (mOrder.getStatus().equals("delivered")) {
                    ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                }
                else
                    ((ViewHolder) holder).timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));

                break;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }


}
