package com.ecom.justbakers.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ecom.justbakers.Adapters.CustomCartListAdapter;
import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.FragmentCartBinding;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import info.hoang8f.widget.FButton;

public class CartFragment extends Fragment {
    private final static String TAG = "CartFragment";
    private ValueEventListener cartValueEventListener;
    private Firebase cartRef;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

    public CartFragment(){}

    public static CartFragment newInstance(){
        return new CartFragment();
    }

    private FragmentCartBinding cartBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        cartBinding = FragmentCartBinding.inflate(inflater, container, false);
        return cartBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Firebase.setAndroidContext(requireActivity());

        ListView cartListView = cartBinding.cartListView;
        FButton checkoutButton = cartBinding.checkoutButton;

        ArrayList<Product> cartProductList = new ArrayList<>();
        ProgressDialog progress = new ProgressDialog(requireActivity());
        progress.setCancelable(false);
        progress.setMessage("LOADING CART...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        String userId = LoginActivity.getLoggedInUser(requireActivity(), true);
        cartRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/orders/pending/cart");
        final TotalSummary totalSummary = new TotalSummary();
        cartValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartProductList.clear();
                totalSummary.reset();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.i(TAG, "POST SNAPSHOT IS " + postSnapshot);

                    Product cartProduct = postSnapshot.getValue(Product.class);

                    Product product = new Product(cartProduct.getName()
                            , cartProduct.getImage()
                            , cartProduct.getPrice()
                            , cartProduct.getDiscount()
                            , cartProduct.getDescription()
                            , cartProduct.getSeller()
                            , cartProduct.getId()
                            , cartProduct.getQuantity()
                            , cartProduct.getLimit());

                    Pair<Double, Double> amountAndDiscount = getPriceAndDiscount(cartProduct);
                    totalSummary.addAmount(amountAndDiscount.first);
                    totalSummary.addDiscount(amountAndDiscount.second);
                    cartProductList.add(product);
                }

                if (!cartProductList.isEmpty()) {
                    int screenWidth = requireActivity().getWindowManager().getDefaultDisplay().getWidth();
                    CustomCartListAdapter adapter = new CustomCartListAdapter(cartProductList, requireActivity()
                            , productChangedConsumer
                            , screenWidth);

                    cartListView.setAdapter(adapter);

                    updateSummary(totalSummary);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };
        cartRef.addValueEventListener(cartValueEventListener);

        progress.dismiss();

        checkoutButton.setOnClickListener(v -> {
            cartRef.removeEventListener(cartValueEventListener);
            NavDirections action = CartFragmentDirections.actionFromCartFragmentToOrderConfirmationFragment(getTotalSummary(), cartProductList.toArray(new Product[0]));
            Navigation.findNavController(requireActivity(), R.id.nav_cart_host_fragment).navigate(action);
        });
    }

    private @NonNull Pair<Double, Double> getPriceAndDiscount(Product product){
        double discount = product.getDiscount() != null ? product.getDiscount() : 0;
        if (discount > 0) {
            discount = product.getPrice() * product.getQuantity() * discount * .01;
        }
        double amount = (product.getPrice() * product.getQuantity());

        return new Pair<>(amount, discount);
    }

    private TotalSummary getTotalSummary(){
        TotalSummary totalSummary = new TotalSummary();
        ListView cartListView = cartBinding.cartListView;
        CustomCartListAdapter adapter = (CustomCartListAdapter) cartListView.getAdapter();
        for(Product cartProduct: adapter.getCartProductList()){
            Pair<Double, Double> amountAndDiscount = getPriceAndDiscount(cartProduct);
            totalSummary.addAmount(amountAndDiscount.first);
            totalSummary.addDiscount(amountAndDiscount.second);
        }

        return totalSummary;
    }

    private final Consumer<Pair<Action, Product>> productChangedConsumer = (data) -> {
        if (Action.DELETE == data.first) {
            String userId = LoginActivity.getLoggedInUser(requireContext().getApplicationContext(), true);
            Firebase deleteRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/orders/pending/cart/" + data.second.getId());
            deleteRef.removeValue();
            if(null != getView()){
                updateSummary(getTotalSummary());
                Snackbar.make(getView(), String.format(Locale.getDefault(), "'%1$s' DELETED FROM CART", data.second.getName()) //getResources().getString(R.string.Deleted_Cart)
                        , Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        } else {
            cartRef.child(data.second.getId()).setValue(data.second);
        }
    };

    private void updateSummary(@NonNull TotalSummary totalSummary){
        OrderConfirmationFragment.updateSummary(cartBinding.summaryTextView, currencyFormat, totalSummary, OrderConfirmationFragment.SummaryType.CONDENSED);
    }

    @Override
    public void onStop() {
        cartRef.removeEventListener(cartValueEventListener);
        super.onStop();
    }
}
