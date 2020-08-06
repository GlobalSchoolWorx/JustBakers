package com.ecom.justbakers.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.R;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.OrderClass;
import com.ecom.justbakers.sms_verify.PhoneAuthActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class CustomerPendingOrderListAdapter  extends BaseAdapter {

    private Context mContext;
    private ArrayList<OrderClass> mOrderList;
    private ArrayList<InfoClass> mInfoList;

    public CustomerPendingOrderListAdapter(Context c, ArrayList<InfoClass> infoList, ArrayList<OrderClass> orderList) {
        mContext = c;
        mOrderList = orderList;
        mInfoList = infoList;
    }


    @Override
    public int getCount() {
        return mOrderList.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            String str = "";
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cust_pending_order_list_item_layout, parent, false);

            TextView cust_name = convertView.findViewById(R.id.btn_custname);
            TextView order_amount_btn = convertView.findViewById(R.id.btn_orderamount);
            TextView order_details_button = convertView.findViewById(R.id.tv_orderdetails);
            TextView cart_button = convertView.findViewById(R.id.cart_btn);
            String gmail = mInfoList.get(position).getGmail();
            String ordernumber = mOrderList.get(position).getOrdernumber();

            cust_name.setText((position+1) + ". " + mInfoList.get(position).getName() + " " + gmail + " " +
                    mInfoList.get(position).getPhoneNumber() + " " + mInfoList.get(position).getFlatNumber() + " " + mInfoList.get(position).getSociety() + " " +
                    mInfoList.get(position).getArea());
            order_amount_btn.setText (String.valueOf(mOrderList.get(position).getReceipt().getFinalamount()));
            for ( int i = 0 ; i < mOrderList.get(position).getCart().size(); i++) {
                str += ("\n" + (i+1) + "." + mOrderList.get(position).getCart().get(i).getName() + " -- " +
                                      mOrderList.get(position).getCart().get(i).getQuantity() );
            }
            cart_button.setText ( str);

            cart_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String spl_order[] = ordernumber.split("#");
                    Firebase CustomerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+ PhoneAuthActivity.md5(gmail)+"/orders/placed/"+spl_order[1]);
                    Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/"+ spl_order[1]);

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder (mContext);

                    builder.setMessage("Are you sure you want to mark this order as Delivered.").setPositiveButton (android.R.string.yes, (dialog, whichButton) -> {
                          CustomerChangeOrderRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    OrderClass obj = (OrderClass) dataSnapshot.getValue(OrderClass.class);
                                    if (obj != null) {

                                        CustomerChangeOrderRef.child("status").setValue("delivered");

                                    }
                    }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });

                          pendingOrderRef.removeValue();


                        }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

                        builder.show();
                    }
                });


        }
        return convertView;
    }
}
