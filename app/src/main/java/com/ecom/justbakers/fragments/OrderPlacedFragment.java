package com.ecom.justbakers.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.LaunchActivity;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.databinding.FragmentOrderPlacedBinding;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.orders.Order;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import static com.ecom.justbakers.LoginActivity.SESSION_KEY_DISPLAY_NAME;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_FLAT_NUMBER;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_PHONE_NUMBER;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_PIN_CODE;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_SOCIETY;

public class OrderPlacedFragment extends Fragment {
    public final static String TAG = "OrderPlacedFragment";
    public final static String ARG_TOTAL_PRICE = "totalPrice";
    public final static String ARG_CART_LIST = "cartList";
    private FragmentOrderPlacedBinding orderPlacedBinding;

    public static OrderPlacedFragment newInstance(Double totalPrice, ArrayList<Product> cartList){
        OrderPlacedFragment fragment = new OrderPlacedFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_TOTAL_PRICE, totalPrice);
        args.putParcelableArray(ARG_CART_LIST, cartList.toArray(new Product[0]));

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBackToLaunchActivity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(android.R.id.home == item.getItemId()){
            goBackToLaunchActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        orderPlacedBinding = FragmentOrderPlacedBinding.inflate(inflater, container, false);
        return orderPlacedBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Firebase msgRef = new Firebase("https://justbakers-285be.firebaseio.com/config/confirmationmsg");
        msgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(null != getView() && getViewLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    com.romainpiel.shimmer.ShimmerTextView tagLineShimmerTextView = orderPlacedBinding.taglineShimmerTextView;
                    tagLineShimmerTextView.setText((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        });

        Button btn = orderPlacedBinding.backToHomeButton;
        btn.setOnClickListener(v -> goBackToLaunchActivity());

        saveOrderInDatabase();
    }

    private void goBackToLaunchActivity(){
        Intent intent = new Intent(requireActivity(), LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        requireActivity().finish();
    }

    public void saveOrderInDatabase () {
        Context cxt = requireContext();
        String userId = LoginActivity.getLoggedInUser(requireContext(), true);
        Firebase pendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/pending/cart");
        Firebase placedOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+userId+"/orders/placed");
        //Firebase counterRef = new Firebase("https://justbakers-285be.firebaseio.com/slotbooking");
        Firebase totalPendingOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/pendingOrders");
        Date date = Calendar.getInstance().getTime();
        long currentTimeInMillis = date.getTime();
        String currentTimeInMillisString = Long.toString(currentTimeInMillis);

        OrderPlacedFragmentArgs orderPlacedFragmentArgs = OrderPlacedFragmentArgs.fromBundle(requireArguments());
        String nameOnOrder = orderPlacedFragmentArgs.getNameOnOrder();
        nameOnOrder = TextUtils.isEmpty(nameOnOrder) ? LoginActivity.getDefaults(cxt, SESSION_KEY_DISPLAY_NAME) : nameOnOrder;
        TotalSummary totalSummary = orderPlacedFragmentArgs.getTotalSummary();
        Product [] products = orderPlacedFragmentArgs.getCartList();
        List<Product> cartList = Arrays.asList(products);
        InfoClass info = new InfoClass(LoginActivity.getLoggedInUser(cxt, false),  nameOnOrder,
                LoginActivity.getDefaults(cxt, SESSION_KEY_PHONE_NUMBER), LoginActivity.getDefaults(cxt, SESSION_KEY_PIN_CODE),
                LoginActivity.getDefaults(cxt, SESSION_KEY_SOCIETY), LoginActivity.getDefaults(cxt, SESSION_KEY_FLAT_NUMBER));

        Order order = new Order("#"+currentTimeInMillisString, (cartList instanceof ArrayList) ? (ArrayList<Product>)cartList : new ArrayList<>(cartList), new Order.OrderReceiptClass(totalSummary.getAmount()), date, date.getTime(), Order.OrderStatus.PENDING.getValue());

        placedOrderRef.child(currentTimeInMillisString).setValue(order);

        totalPendingOrderRef.child(currentTimeInMillisString).child("info").setValue(info);

        totalPendingOrderRef.child(currentTimeInMillisString).child("order").setValue(order);
        pendingOrderRef.removeValue();
    }
}
