package com.ecom.justbakers.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ecom.justbakers.Classes.Product;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.FragmentOrderConfirmationBinding;
import com.ecom.justbakers.orders.InfoClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

public class OrderConfirmationFragment extends Fragment {
    public final static String TAG = "OrderConfirmationFrag..";
    public final static String ARG_NAME_ON_ORDER = "nameOnOrder";
    public final static String ARG_TOTAL_PRICE = "totalPrice";
    public final static String ARG_CART_LIST = "cartList";

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "in"));

    private FragmentOrderConfirmationBinding orderConfirmationBinding;
    private Firebase addressRef;
    private ValueEventListener addressValueEventListener;

    public static OrderConfirmationFragment newInstance(String nameOnOrder, Double totalPrice, ArrayList<Product> cartList){
        OrderConfirmationFragment fragment = new OrderConfirmationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME_ON_ORDER, nameOnOrder);
        args.putDouble(ARG_TOTAL_PRICE, totalPrice);
        args.putParcelableArray(ARG_CART_LIST, cartList.toArray(new Product[0]));

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userId = LoginActivity.getLoggedInUser(requireActivity(), true);
        addressRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/info");
        addressValueEventListener  = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                InfoClass ic = dataSnapshot.getValue(InfoClass.class);
                String sessionDisplayName = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_DISPLAY_NAME);
                if(!sessionDisplayName.equals(ic.getName())){
                    ic.setName(sessionDisplayName);
                    addressRef.setValue(ic);
                }
                String nameOnOrder = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_NAME_ON_ORDER);
                sessionDisplayName = TextUtils.isEmpty(nameOnOrder) ? sessionDisplayName : nameOnOrder;
                orderConfirmationBinding.addressView.setText(
                        getAddressText(sessionDisplayName, ic.getFlatNumber(), ic.getSociety(), ic.getPincode()),
                        TextView.BufferType.SPANNABLE);
                orderConfirmationBinding.addressView.setTag(sessionDisplayName);
                orderConfirmationBinding.phoneNumberView.setText(String.format("Contact Number\n%1$s", getWithUndefined(ic.getPhoneNumber())));
                orderConfirmationBinding.phoneNumberView.setTag(ic.getPhoneNumber());
                if(!isAddressComplete(sessionDisplayName, ic.getFlatNumber(), ic.getSociety(), ic.getPincode(), ic.getPhoneNumber())){
                    orderConfirmationBinding.checkoutButton.setEnabled(false);
                    orderConfirmationBinding.addressView.setError("Invalid Delivery Address!");
                }else{
                    orderConfirmationBinding.checkoutButton.setEnabled(true);
                    orderConfirmationBinding.addressView.setError(null);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        addressRef.addValueEventListener(addressValueEventListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        orderConfirmationBinding = FragmentOrderConfirmationBinding.inflate(inflater, container, false);
        return orderConfirmationBinding.getRoot();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAddressComplete(String displayName, String flatNumber, String society, String pinCode, String phoneNumber){
        return !TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(flatNumber) && !TextUtils.isEmpty(society) && !TextUtils.isEmpty(pinCode) && !TextUtils.isEmpty(phoneNumber);
    }

    private String get(String value) {
        if (null == value) return "";
        return value;
    }

    private String getWithUndefined(String value) {
        if (null == value) return "<UNDEFINED>";
        return value;
    }

    private Spannable getAddressText(String displayName, String flatNumber, String society, String pinCode){
        SpannableStringBuilder addressStr = new SpannableStringBuilder("Deliver To:");
        int index = addressStr.length();
        addressStr.setSpan(new StyleSpan(Typeface.BOLD), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        addressStr.append(String.format("\n%1$s\n%2$s\n%3$s\n" + "Pune - %4$s", getWithUndefined(displayName), get(flatNumber), get(society), getWithUndefined(pinCode)));
        addressStr.setSpan(new StyleSpan(Typeface.ITALIC), index, addressStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return addressStr;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        orderConfirmationBinding.checkoutButton.setEnabled(false);
        String nameOnOrder = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_NAME_ON_ORDER);
        String displayName = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_DISPLAY_NAME);
        String flatNumber = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_FLAT_NUMBER);
        String society = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_SOCIETY);
        String pinCode = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_PIN_CODE);
        String phoneNumber = LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_PHONE_NUMBER);
        nameOnOrder = TextUtils.isEmpty(nameOnOrder) ? displayName : nameOnOrder;
        orderConfirmationBinding.addressView.setText(getAddressText(nameOnOrder, flatNumber, society, pinCode), TextView.BufferType.SPANNABLE);
        orderConfirmationBinding.phoneNumberView.setText(String.format("Contact Number\n%1$s", getWithUndefined(phoneNumber)));
        if(!isAddressComplete(nameOnOrder, flatNumber, society, pinCode, phoneNumber)) {
            orderConfirmationBinding.addressView.setError("Invalid Delivery Address!");
        }

        orderConfirmationBinding.chgAddrButton.setOnClickListener((v) -> {
            String customerName = (String) orderConfirmationBinding.addressView.getTag();
            String phoneNumberFromDb = (String) orderConfirmationBinding.phoneNumberView.getTag();
            NavDirections action = OrderConfirmationFragmentDirections.actionFromOrderConfirmationFragmentToUserDetailsFragment(customerName, phoneNumberFromDb);
            Navigation.findNavController(requireActivity(), R.id.nav_cart_host_fragment).navigate(action);
        });

        OrderConfirmationFragmentArgs orderConfirmationFragmentArgs = OrderConfirmationFragmentArgs.fromBundle(requireArguments());
        TotalSummary totalSummary = orderConfirmationFragmentArgs.getTotalSummary();
        Product[] cartProductsArray = orderConfirmationFragmentArgs.getCartList();

        orderConfirmationBinding.checkoutButton.setOnClickListener((v) -> {
            addressRef.removeEventListener(addressValueEventListener);
            String nameOnOrder_ = TextUtils.isEmpty((CharSequence) orderConfirmationBinding.addressView.getTag()) ? LoginActivity.getDefaults(requireContext(), LoginActivity.SESSION_KEY_DISPLAY_NAME) : (String) orderConfirmationBinding.addressView.getTag();
            NavDirections action = OrderConfirmationFragmentDirections.actionFromCartFragmentToOrderPlacedFragment(nameOnOrder_, totalSummary, cartProductsArray);
            Navigation.findNavController(requireActivity(), R.id.nav_cart_host_fragment).navigate(action);
        });

        updateSummary(totalSummary);
    }

    public enum SummaryType{
        DETAILED,
        CONDENSED
    }

    private void updateSummary(@NonNull TotalSummary totalSummary){
        updateSummary(orderConfirmationBinding.summaryTextView, currencyFormat, totalSummary, SummaryType.DETAILED);
    }

    public static void updateSummary(@NonNull TextView summaryTextView, @NonNull NumberFormat currencyFormat, @NonNull TotalSummary totalSummary, @NonNull SummaryType summaryType){
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (totalSummary.getShippingCharges() > 0) {
            stringBuilder.append("Total Amount : ");
            int index = stringBuilder.length();
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(currencyFormat.format(totalSummary.getAmount()));
            stringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), index, stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if(totalSummary.getAmount() > 0) {
                index = stringBuilder.length();
                if(SummaryType.DETAILED == summaryType) {
                    stringBuilder.append(" (Including Delivery Charges : ").append(currencyFormat.format(totalSummary.getShippingCharges())).append(")");
                    stringBuilder.append("\n(Delivery charges applicable for orders less than ").append(currencyFormat.format(totalSummary.getThresholdForDeliveryCharges())).append(")");
                }else{
                    stringBuilder.append("\n(Incl Delivery : ").append(currencyFormat.format(totalSummary.getShippingCharges())).append(")");
                }
                stringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), index, stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                index = stringBuilder.length();
                stringBuilder.append("\nSavings : ");
                stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), index, stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.append(currencyFormat.format(totalSummary.getDiscount())).append("/-");
            }
        }else{
            stringBuilder.append("Total Amount : ");
            int index = stringBuilder.length();
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, index, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            stringBuilder.append(currencyFormat.format(totalSummary.getAmount())).append("/-");
            stringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), index, stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if(totalSummary.getAmount() > 0) {
                if (totalSummary.getDiscount() > 0) {
                    index = stringBuilder.length();
                    stringBuilder.append("\nSavings : ");
                    stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), index, stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    stringBuilder.append(currencyFormat.format(totalSummary.getDiscount())).append("/-");
                }
            }
        }

        if(null != summaryTextView) {
            summaryTextView.setText(stringBuilder, TextView.BufferType.SPANNABLE);
        }

        summaryTextView.setEnabled(totalSummary.getAmount() > 0);
    }

    @Override
    public void onStop() {
        addressRef.removeEventListener(addressValueEventListener);
        super.onStop();
    }
}
