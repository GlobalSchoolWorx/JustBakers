package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.OrderDetailsRecyleItemLayoutBinding;
import com.ecom.justbakers.orders.Order;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
public class OrderDetailsListAdapter extends RecyclerView.Adapter<OrderDetailsListAdapter.ViewHolder> {
    private final Context mContext;
    private final String[] mDataset;
    private final Order mOrder;

    public OrderDetailsListAdapter (Context mContext, Order mOrder, String[] dataSet) {
        this.mContext = mContext;
        this.mDataset = dataSet;
        this.mOrder = mOrder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public com.github.vipulasri.timelineview.TimelineView timelineView;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            timelineView = itemView.findViewById(R.id.timeLineView);
            textView = itemView.findViewById(R.id.textViewOrderDetail);
        }
    }

    @NonNull
    @Override
    public OrderDetailsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = OrderDetailsRecyleItemLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false).getRoot();

        // <sbindra> Use post of parent to make sure the width of parent is initialized.
        // Another mechanism is to use view.getViewTreeObserver().addOnGlobalLayoutListener
        parent.post(() -> {
            /*ViewGroup.LayoutParams lp = new RecyclerView.LayoutParams((int)(parent.getWidth() * 0.25), RecyclerView.LayoutParams.WRAP_CONTENT);
            rootView.setLayoutParams(lp);*/
            //<sbindra> As recycler View uses LayoutParameter for storing ViewHolder, Use following mechanism.
            rootView.getLayoutParams().width = (int)(parent.getWidth() * 0.25);
            rootView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
            rootView.requestLayout();
        });
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailsListAdapter.ViewHolder holder, int position) {
        switch (position) {
            case 0: {
                holder.textView.setText(mDataset[position]);
                holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                break;
            }
            case 1: {
                holder.textView.setText(mDataset[position]);
                if (Order.OrderStatus.CONFIRMED == mOrder.getOrderStatus() || Order.OrderStatus.DELIVERED == mOrder.getOrderStatus()) {
                    holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                } else {
                    holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));
                }
                break;
            }
            case 2: {
                holder.textView.setText(mDataset[position]);
                if (Order.OrderStatus.PROCESSING == mOrder.getOrderStatus() || Order.OrderStatus.DELIVERED == mOrder.getOrderStatus()) {
                    holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                } else {
                    holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));
                }
                break;
            }
            case 3: {
                holder.textView.setText(mDataset[position]);
                if (Order.OrderStatus.DELIVERED == mOrder.getOrderStatus()) {
                    holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker));
                } else {
                    holder.timelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker_inactive));
                }
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
