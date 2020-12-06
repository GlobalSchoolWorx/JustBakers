package com.ecom.justbakers.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ecom.justbakers.Adapters.CustomOrderListAdapter;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.FragmentCustomerOrdersBinding;
import com.ecom.justbakers.orders.Order;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OrdersFragment extends Fragment {
    private static final String TAG = "OrdersFragment";

    private ProgressDialog progress;
    private CustomOrderListAdapter orderAdapter;

    private Firebase placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers");
    private ValueEventListener placedValueEventListener;
    private FragmentCustomerOrdersBinding customerOrdersBinding;

    public OrdersFragment() {}

    public static OrdersFragment newInstance() {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        customerOrdersBinding = FragmentCustomerOrdersBinding.inflate(inflater, container, false);
        return customerOrdersBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Firebase.setAndroidContext(requireActivity());
        String userId = LoginActivity.getLoggedInUser(requireActivity(), true);
        if(!LoginActivity.isUserLoggedIn(requireActivity())) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            this.startActivity(intent);
        } else {
            placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed");
        }

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getResources().getString(R.string.Loading1));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        ArrayList<Order> placedOrdersList = new ArrayList<>();

        orderAdapter = new CustomOrderListAdapter(requireActivity(), placedOrdersList, customerChangeOrderUpdaterFunc.apply(userId));


        RecyclerView ordersList = customerOrdersBinding.ordersList;
        ordersList.setAdapter(orderAdapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL, false);
        ordersList.setLayoutManager(manager);
        ordersList.setItemAnimator(new DefaultItemAnimator());

        placedValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( null != getView() && getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && !requireActivity().isFinishing()) {
                    progress.show();
                    placedOrdersList.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Order post = postSnapshot.getValue(Order.class);
                        Order currentOrder = new Order(post.getOrdernumber()
                                , post.getCart()
                                , post.getReceipt()
                                , post.getDate()
                                , post.getTime()
                                , post.getStatus());
                        placedOrdersList.add(currentOrder);
                    }

                    Collections.sort(placedOrdersList, orderClassComparator);
                    orderAdapter.setOrderList(placedOrdersList);
                    orderAdapter.notifyDataSetChanged();
                    progress.dismiss();
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        placedOrderRef.addValueEventListener( placedValueEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    Function<String, Consumer<Order>> customerChangeOrderUpdaterFunc = (userId) -> (order) ->{
        String str = order.getOrdernumber();
        String []strTokens = str.split("#");
        Firebase CustomerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed/"+strTokens[1]);
        CustomerChangeOrderRef.child("status").setValue(Order.OrderStatus.CANCELLED.getValue());
        order.setOrderStatus(Order.OrderStatus.CANCELLED);
    };

    private final Comparator<Order> orderClassComparator = (e1, e2) -> Long.compare(e2.getDate().getTime(), e1.getDate().getTime());

    @Override
    public void onStop() {
        super.onStop();

        if(null != placedValueEventListener) {
            placedOrderRef.removeEventListener(placedValueEventListener);
        }
    }
}
