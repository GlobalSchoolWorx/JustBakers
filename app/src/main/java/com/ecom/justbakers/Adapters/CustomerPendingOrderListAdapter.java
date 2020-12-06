package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.R;
import com.ecom.justbakers.Utils;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.Order;
import com.ecom.justbakers.orders.Order.OrderStatus;
import com.firebase.client.Firebase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CustomerPendingOrderListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Order> mOrderList;
    private ArrayList<InfoClass> mInfoList;

    public CustomerPendingOrderListAdapter(Context c, ArrayList<InfoClass> infoList, ArrayList<Order> orderList) {
        mContext = c;
        mOrderList = orderList;
        mInfoList = infoList;
    }


    @Override
    public int getCount() {
        return mOrderList.size();
    }

    @Override
    public Order getItem(int position) {
        return mOrderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public InfoClass getInfo(int position){
        return mInfoList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            String str = "";
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cust_pending_order_list_item_layout, parent, false);
            TableRow tableRow = convertView.findViewById(R.id.table_row);
            TextView cust_name = convertView.findViewById(R.id.btn_custname);
            TextView order_amount_btn = convertView.findViewById(R.id.btn_orderamount);
            TextView cart_button = convertView.findViewById(R.id.cart_btn);
            Button confirmOrderButton = convertView.findViewById(R.id.confirmOrderBtn);
            Button processOrderButton = convertView.findViewById(R.id.processOrderBtn);
            Button orderDeliveredButton = convertView.findViewById(R.id.orderDeliveredBtn);
            String gmail = mInfoList.get(position).getGmail();
            String ordernumber = mOrderList.get(position).getOrdernumber();
            OrderStatus status = mOrderList.get(position).getOrderStatus();

            if ( OrderStatus.CONFIRMED == status)
                confirmOrderButton.setVisibility(View.GONE);
            else if ( OrderStatus.PROCESSING == status) {
                confirmOrderButton.setVisibility(View.GONE);
                processOrderButton.setVisibility(View.GONE);
            }

            confirmOrderButton.setOnClickListener(v -> {
                String [] spl_order = ordernumber.split("#");
                Firebase CustomerConfirmOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + Utils.md5(gmail) + "/orders/placed/" + spl_order[1]);
                Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/" + spl_order[1] + "/order");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);

                builder.setMessage("Are you sure you want to mark this order as Confirmed.").setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    pendingOrderRef.child("status").setValue(OrderStatus.CONFIRMED.getValue());
                    CustomerConfirmOrderRef.child("status").setValue(OrderStatus.CONFIRMED.getValue());
                }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

                builder.show();
            });
            processOrderButton.setOnClickListener(v -> {
                String [] spl_order = ordernumber.split("#");
                Firebase CustomerConfirmOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + Utils.md5(gmail) + "/orders/placed/" + spl_order[1]);
                Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/" + spl_order[1] + "/order");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);

                builder.setMessage("Are you sure you want to mark this order as Processing.").setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                    CustomerConfirmOrderRef.child("status").setValue(OrderStatus.PROCESSING.getValue());
                    pendingOrderRef.child("status").setValue(OrderStatus.PROCESSING.getValue());
                    /*
                    CustomerConfirmOrderRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderClass obj = (OrderClass) dataSnapshot.getValue(OrderClass.class);
                            if (obj != null) {

                                CustomerConfirmOrderRef.child("status").setValue(Order.OrderStatus.PROCESSING.getValue());

                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });


                    pendingOrderRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderClass obj = (OrderClass) dataSnapshot.getValue(OrderClass.class);
                            if (obj != null) {
                                pendingOrderRef.child("status").setValue(Order.OrderStatus.PROCESSING.getValue());

                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

                     */

                }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {


                });
                builder.show();
            });

            InfoClass info = mInfoList.get(position);
            cust_name.setText((position + 1) + "." + get(info.getName())
                    + get(gmail)
                    + get(info.getPhoneNumber())
                    + get(info.getFlatNumber())
                    + get(info.getSociety())
                    + get(info.getPincode()));
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

            order_amount_btn.setText(numberFormat.format(mOrderList.get(position).getReceipt().getFinalamount())
                                     + "   " + mOrderList.get(position).getOrderStatus().getValue());
            ArrayList<Product> cartProducts = mOrderList.get(position).getCart();
            for (int i = 0; i < cartProducts.size(); i++) {
                str += ((i + 1) + ". " + mOrderList.get(position).getCart().get(i).getName() + " -- " +
                        mOrderList.get(position).getCart().get(i).getQuantity());
                if(i < (cartProducts.size() -1)){
                    str += "\n";
                }
            }
            cart_button.setText(str);

            orderDeliveredButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String spl_order[] = ordernumber.split("#");
                    Firebase CustomerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + Utils.md5(gmail) + "/orders/placed/" + spl_order[1]);
                    Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/" + spl_order[1]);

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);

                    builder.setMessage("Are you sure you want to mark this order as Delivered.").setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        CustomerChangeOrderRef.child("status").setValue(OrderStatus.DELIVERED.getValue());
                        pendingOrderRef.removeValue();
                        /*
                        CustomerChangeOrderRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                OrderClass obj = (OrderClass) dataSnapshot.getValue(OrderClass.class);
                                if (obj != null) {

                                    CustomerChangeOrderRef.child("status").setValue(Order.OrderStatus.DELIVERED.getValue());

                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

                        pendingOrderRef.removeValue();
                     */

                    }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
                    });

                    builder.show();
                }
            });
        }
        return convertView;
    }

    private String get(String value){
        if(null != value){
            return " "+value;
        }
        return "";
    }
}