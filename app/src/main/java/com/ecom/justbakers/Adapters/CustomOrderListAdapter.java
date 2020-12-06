package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.OrderListItemLayoutBinding;
import com.ecom.justbakers.orders.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import info.hoang8f.widget.FButton;

public class CustomOrderListAdapter extends RecyclerView.Adapter<CustomOrderListAdapter.ViewHolder> {
    private final Context mContext;
    private ArrayList<Order> orderList;
    Consumer<Order> customerOrderUpdateCallback;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));
    private final SimpleDateFormat dateFormatWithDay = new SimpleDateFormat("EEE, MMM d, yyyy, hh:mm a", Locale.getDefault());

    public CustomOrderListAdapter (Context c, ArrayList<Order> orderList, Consumer<Order> customerOrderUpdateCallback) {
       mContext = c;
       this.orderList = orderList;
       this.customerOrderUpdateCallback = customerOrderUpdateCallback;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView orderDate;
        TextView orderAmount;
        TextView trackOrderButton;
        TextView orderDetailsButton;
        FButton cancelOrderButton;
        RecyclerView trackOrderRecyclerView;
        FButton orderDetailTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.btn_orderDate);
            orderAmount = itemView.findViewById(R.id.btn_orderamount);
            trackOrderButton = itemView.findViewById(R.id.track_order_btn);
            orderDetailsButton = itemView.findViewById(R.id.orderStatusTextView);
            cancelOrderButton = itemView.findViewById(R.id.cancel_order_btn);
            trackOrderRecyclerView = itemView.findViewById(R.id.track_order_recycler_view);
            orderDetailTextView = itemView.findViewById(R.id.orderDetailTextView);
        }
    }

    @NonNull
    @Override
    public CustomOrderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(OrderListItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull CustomOrderListAdapter.ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderDate.setText(dateFormat.format(order.getDate()));

        String status = order.getOrderStatus().getValue().toUpperCase();
        holder.orderDetailsButton.setText( String.format("%1$s%2$s", status, "..."));
        holder.orderAmount.setText(currencyFormat.format(order.getReceipt().getFinalamount()));

        if ( status.equals("CANCELLED")) {
            holder.trackOrderButton.setEnabled(false);
            holder.trackOrderButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            holder.cancelOrderButton.setVisibility(View.GONE);
        } else if( status.equals("DELIVERED")) {
            holder.cancelOrderButton.setVisibility(View.GONE);
        }

        holder.trackOrderButton.setOnClickListener(v -> orderTrackButtonClick(position, holder));
        holder.orderDetailsButton.setOnClickListener(v -> orderDetailsButtonClick(position, holder));
        holder.cancelOrderButton.setOnClickListener(v -> cancelOrderButtonClick(position, holder));
    }

    public void orderTrackButtonClick(int position, CustomOrderListAdapter.ViewHolder holder){
        RecyclerView trackOrderRecyclerView = holder.trackOrderRecyclerView;
        TextView orderDetailTextView = holder.orderDetailTextView;
        Button cancelOrderButton = holder.cancelOrderButton;

        int vis = trackOrderRecyclerView.getVisibility();
        if ( vis == View.GONE || vis == View.INVISIBLE) {
            vis = View.VISIBLE;
        } else {
            vis = View.GONE;
        }

        cancelOrderButton.setVisibility(View.GONE);
        orderDetailTextView.setVisibility(View.GONE);
        trackOrderRecyclerView.setVisibility(vis);

        String[] trackArr = {"Order\nPlaced", "Order\nConfirmed", "Order\nProcessing", "Order\nDelivered"};

        Order curOrder = orderList.get(position);
        OrderDetailsListAdapter mAdapter = new OrderDetailsListAdapter(mContext, curOrder, trackArr);
        trackOrderRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        trackOrderRecyclerView.setLayoutManager(manager);
        trackOrderRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void orderDetailsButtonClick(int position, CustomOrderListAdapter.ViewHolder holder){
        TextView orderDetailsTextview = holder.orderDetailTextView;
        RecyclerView orderTrackTextView = holder.trackOrderRecyclerView;

        Button cancelOrderButton = holder.cancelOrderButton;
        int vis = orderDetailsTextview.getVisibility();
        StringBuilder cartItems = new StringBuilder();

        orderTrackTextView.setVisibility(View.GONE);
        if ( vis == View.GONE) {
            vis = View.VISIBLE;
        } else {
            vis = View.GONE;
        }

        Order curOrder = orderList.get(position);
        orderDetailsTextview.setVisibility(vis);

        cancelOrderButton.setVisibility(vis);

        for(int i=1; i<=curOrder.getCart().size(); i++) {
            Product pc = curOrder.getCart().get(i-1);
            if ( null != pc.getDiscount() && pc.getDiscount() > 0 ) {
                String price = currencyFormat.format(pc.getPrice());
                cartItems.append(String.format(Locale.getDefault(), "%1$d) %2$s, Qty: %3$d, Price: %4$s, Discount: %5$.2f%%", i, pc.getName(), pc.getQuantity(), price, pc.getDiscount())).append("\n");
            } else {
                String price = currencyFormat.format(pc.getPrice());
                cartItems.append(String.format(Locale.getDefault(), "%1$d) %2$s, Qty: %3$d, Price: %4$s", i, pc.getName(), pc.getQuantity(), price)).append("\n");
            }
        }

        String amount = currencyFormat.format(curOrder.getReceipt().getTotalamount());
        orderDetailsTextview.setText (String.format(mContext.getString(R.string.order_details), curOrder.getOrderStatus().getValue().toUpperCase(),
                dateFormatWithDay.format(curOrder.getDate()), amount, cartItems.toString()));

        if(Order.OrderStatus.CANCELLED == curOrder.getOrderStatus()) {
            cancelOrderButton.setVisibility(View.GONE);
        }
    }

    public void cancelOrderButtonClick(int position, CustomOrderListAdapter.ViewHolder holder){
        Order curOrder = orderList.get(position);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure you want to Cancel this order.").setPositiveButton (android.R.string.yes, (dialog, whichButton) -> {
            customerOrderUpdateCallback.accept(curOrder);
            TextView orderDetailTextView = holder.orderDetailTextView;
            Button cancelOrderButton = holder.cancelOrderButton;
            TextView buttonOrderDetails = holder.orderDetailsButton;
            TextView trackOrderButton = holder.trackOrderButton;

            orderDetailTextView.setVisibility(View.GONE);
            cancelOrderButton.setVisibility(View.GONE);
            buttonOrderDetails.setText(R.string.cancelled);
            trackOrderButton.setEnabled(false);
            trackOrderButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
            notifyItemChanged(position);
        });

        builder.setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});
        builder.show();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void setOrderList(ArrayList<Order> orderList){
        this.orderList = orderList;

        notifyDataSetChanged();
    }

}
