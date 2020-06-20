package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecom.justbakers.R;
import com.ecom.justbakers.orders.OrderClass;


import java.util.ArrayList;

public class CustomOrderListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<OrderClass> orderList;
    OrderButtonClickListener mOrderDetailListener;
    OrderButtonClickListener mOrderTrackListener;
    OrderButtonClickListener mCancelOrderListener;

    public CustomOrderListAdapter (Context c, ArrayList<OrderClass> orderList, OrderButtonClickListener ordertracklistener,
                                   OrderButtonClickListener orderdetaillistener, OrderButtonClickListener cancelorderlistener ) {
       mContext = c;
       this.orderList = orderList;
       this.mOrderDetailListener = orderdetaillistener;
       this.mOrderTrackListener = ordertracklistener;
       this.mCancelOrderListener = cancelorderlistener;

    }
    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.order_list_item_layout, parent, false);

            TextView order_date = convertView.findViewById(R.id.btn_orderdate);
            TextView order_amount = convertView.findViewById(R.id.btn_orderamount);
            TextView track_order_button = convertView.findViewById(R.id.track_order_btn);
            TextView order_details_button = convertView.findViewById(R.id.tv_orderdetails);
            info.hoang8f.widget.FButton cancel_order_button = convertView.findViewById(R.id.cancel_order_btn);
            String str = orderList.get(position).getDate().toString();
            String str1[] = str.split("GMT");
            order_date.setText(str1[0]);

            String status = orderList.get(position).getStatus().toUpperCase();
            order_details_button.setText( status + "...");
            order_amount.setText(String.valueOf(orderList.get(position).getReceipt().getFinalamount()));

            if ( status.equals("CANCELLED")) {
                track_order_button.setEnabled(false);
                track_order_button.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
                cancel_order_button.setVisibility(View.GONE);
            } else if( status.equals("DELIVERED")) {
                cancel_order_button.setVisibility(View.GONE);
            }

            final View view = convertView;
            track_order_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOrderTrackListener != null)
                        mOrderTrackListener.onButtonClick(position, view);
                }
            });

                order_details_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOrderDetailListener != null)
                        mOrderDetailListener.onButtonClick(position, view);
                }
            });

            cancel_order_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCancelOrderListener != null)
                        mCancelOrderListener.onButtonClick(position, view);
                }
            });

            if ( mOrderTrackListener == null)
                track_order_button.setVisibility(View.GONE);

        }
        return convertView;
    }

    public void setOrderList(ArrayList<OrderClass> orderList){
        this.orderList = orderList;

        notifyDataSetChanged();
    }

    public interface OrderButtonClickListener {
        public abstract void onButtonClick(int position, View view);
    }
}
