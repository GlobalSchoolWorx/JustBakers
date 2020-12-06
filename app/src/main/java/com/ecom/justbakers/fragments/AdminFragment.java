package com.ecom.justbakers.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.ecom.justbakers.Adapters.CustomerPendingOrderListAdapter;
import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.Utils;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.Order;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminFragment extends Fragment {
    private static final String TAG = "AdminFragment";

    private ProgressDialog progress;
    private Firebase custRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders");
    private ValueEventListener custValueEventListener;

    public AdminFragment() {}

    public static AdminFragment newInstance() {
        AdminFragment fragment = new AdminFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Firebase.setAndroidContext(requireActivity());
        ListView productsList = view.findViewById(R.id.productsList);

        if(!LoginActivity.isUserLoggedIn(requireActivity())) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            this.startActivity(intent);
        } else {
            custRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders");
        }

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getResources().getString(R.string.Loading1));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        ArrayList<InfoClass> customersInfoList = new ArrayList<>();
        ArrayList<Order> customersOrderList = new ArrayList<>();

        custValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                customersOrderList.clear();
                customersInfoList.clear();
                progress.show();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    InfoClass info = (postSnapshot.child ("info")).getValue(InfoClass.class);
                    DataSnapshot ds1 = postSnapshot.child ("order");
                    String ordernumber = (String)(ds1.child("ordernumber")).getValue();
                    double finalamount = (double)(ds1.child("receipt").child("finalamount")).getValue();
                    String status = (String)(ds1.child("status")).getValue();
                    long time = (long)(ds1.child("time")).getValue();
                    long dt = (long)(ds1.child("date")).getValue();
                    Date date = new Date(dt);
                    Order.OrderReceiptClass receipt = new Order.OrderReceiptClass(finalamount);
                    ArrayList<Product> cart = new ArrayList<>();
                    for ( DataSnapshot childSnapShot :  ds1.child("cart").getChildren()) {
                        Product pc = childSnapShot.getValue(Product.class);
                        cart.add(pc);
                    }

                    customersInfoList.add(info);
                    Order order = new Order(ordernumber, cart, receipt, date, time, status);
                    customersOrderList.add(order);
                }

                View rootView = getView();
                if(null != rootView) {
                    ListView productsList = rootView.findViewById(R.id.productsList);
                    productsList.invalidateViews();
                }
                progress.dismiss();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        custRef.addValueEventListener(custValueEventListener);
        CustomerPendingOrderListAdapter customerPendingOrderAdapter = new CustomerPendingOrderListAdapter(requireActivity(), customersInfoList, customersOrderList);
        productsList.setAdapter(customerPendingOrderAdapter);

        productsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        productsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_admin_actions, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray checkedItemPositions = productsList.getCheckedItemPositions();
                int itemId = item.getItemId();
                if (itemId == R.id.action_confirm) {
                    for (int i = 0; i < customerPendingOrderAdapter.getCount(); i++) {
                        if (checkedItemPositions.get(i, false)) {
                            confirmOrder(requireActivity(), customerPendingOrderAdapter.getItem(i), customerPendingOrderAdapter.getInfo(i));
                        }
                    }
                    mode.finish();
                    return true;
                } else if (itemId == R.id.action_process) {
                    for (int i = 0; i < customerPendingOrderAdapter.getCount(); i++) {
                        if (checkedItemPositions.get(i, false)) {
                            processOrder(requireActivity(), customerPendingOrderAdapter.getItem(i), customerPendingOrderAdapter.getInfo(i));
                        }
                    }
                    mode.finish();
                    return true;
                } else if (itemId == R.id.action_delivered) {
                    for (int i = 0; i < customerPendingOrderAdapter.getCount(); i++) {
                        if (checkedItemPositions.get(i, false)) {
                            deliverOrder(requireActivity(), customerPendingOrderAdapter.getItem(i), customerPendingOrderAdapter.getInfo(i));
                        }
                    }
                    mode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) { }

            private void confirmOrder(Context cxt, Order order, InfoClass info){
                String [] spl_order = order.getOrdernumber().split("#");
                Firebase customerConfirmOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + Utils.md5(info.getGmail()) + "/orders/placed/" + spl_order[1]);
                Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/" + spl_order[1] + "/order");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(cxt);

                builder.setMessage("Are you sure you want to mark this order as Confirmed.").setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    pendingOrderRef.child("status").setValue(Order.OrderStatus.CONFIRMED.getValue());
                    customerConfirmOrderRef.child("status").setValue(Order.OrderStatus.CONFIRMED.getValue());
                }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

                builder.show();
            }

            private void processOrder(Context cxt, Order order, InfoClass info){
                String [] spl_order = order.getOrdernumber().split("#");
                Firebase customerConfirmOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + Utils.md5(info.getGmail()) + "/orders/placed/" + spl_order[1]);
                Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/" + spl_order[1] + "/order");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(cxt);
                builder.setMessage("Are you sure you want to mark this order as Processing.").setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    customerConfirmOrderRef.child("status").setValue(Order.OrderStatus.PROCESSING.getValue());
                    pendingOrderRef.child("status").setValue(Order.OrderStatus.PROCESSING.getValue());
                }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

                builder.show();
            }

            private void deliverOrder(Context cxt, Order order, InfoClass info){
                String [] spl_order = order.getOrdernumber().split("#");
                Firebase customerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + Utils.md5(info.getGmail()) + "/orders/placed/" + spl_order[1]);
                Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders/" + spl_order[1]);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(cxt);

                builder.setMessage("Are you sure you want to mark this order as Delivered.").setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    customerChangeOrderRef.child("status").setValue(Order.OrderStatus.DELIVERED.getValue());
                    pendingOrderRef.removeValue();
                }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

                builder.show();
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        custRef.removeEventListener(custValueEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
