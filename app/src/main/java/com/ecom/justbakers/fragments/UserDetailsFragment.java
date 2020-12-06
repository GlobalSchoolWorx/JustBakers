package com.ecom.justbakers.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.Utils;
import com.ecom.justbakers.databinding.FragmentUserDetailsBinding;
import com.ecom.justbakers.orders.InfoClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import static com.ecom.justbakers.LoginActivity.SESSION_KEY_FLAT_NUMBER;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_NAME_ON_ORDER;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_PIN_CODE;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_SOCIETY;

public class UserDetailsFragment extends Fragment {
    private final static String TAG = "UserDetailsFragment";

    private FragmentUserDetailsBinding userDetailsBinding;
    private Firebase addressRef;
    private ValueEventListener addressValueEventListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String userId = LoginActivity.getLoggedInUser(requireContext(), true);
        addressRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/info");

        addressValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoClass ic = (InfoClass) dataSnapshot.getValue(InfoClass.class);
                if ( ic != null ) {
                    userDetailsBinding.addressLineEditTextView.setText(ic.getFlatNumber());
                    userDetailsBinding.societyEditTextView.setText(ic.getSociety());
                    userDetailsBinding.pincodeEditTextView.setText(ic.getPincode());
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
        userDetailsBinding = FragmentUserDetailsBinding.inflate(inflater, container, false);

        return userDetailsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userDetailsBinding.addressLineEditTextView.setOnClickListener(v -> userDetailsBinding.addressLineEditTextView.getText().clear());
        UserDetailsFragmentArgs userDetailsFragmentArgs = UserDetailsFragmentArgs.fromBundle(requireArguments());
        userDetailsBinding.customerName.setText(userDetailsFragmentArgs.getCustomerName());
        userDetailsBinding.phoneNumber.setText(userDetailsFragmentArgs.getPhoneNumber());

        userDetailsBinding.updateAddressButton.setOnClickListener(v -> {
            String flatNumber = userDetailsBinding.addressLineEditTextView.getText().toString();
            String society = userDetailsBinding.societyEditTextView.getText().toString();
            String pinCode = userDetailsBinding.pincodeEditTextView.getText().toString();

            if (flatNumber.length() < 3) {
                Toast.makeText(requireContext().getApplicationContext(), "Please enter a valid Flat Number.", Toast.LENGTH_LONG).show();
            } else {
                Utils.updateAddressDetailsInDatabase(LoginActivity.getLoggedInUser(requireActivity(), true), flatNumber, society, pinCode);
                List<Pair<String,String>> keyValuePairs = new ArrayList<>();
                keyValuePairs.add(new Pair<>(SESSION_KEY_NAME_ON_ORDER, userDetailsBinding.customerName.getText().toString()));
                keyValuePairs.add(new Pair<>(SESSION_KEY_SOCIETY, society));
                keyValuePairs.add(new Pair<>(SESSION_KEY_FLAT_NUMBER, flatNumber));
                keyValuePairs.add(new Pair<>(SESSION_KEY_PIN_CODE, pinCode));
                LoginActivity.setDefaults(requireActivity(), keyValuePairs);

                Navigation.findNavController(requireActivity(), R.id.nav_cart_host_fragment).popBackStack();
                    // fallback route to cart.
                    /*NavDirections action = UserDetailsFragmentDirections.actionUserDetailsToCartFragment();
                    Navigation.findNavController(requireActivity(), R.id.nav_cart_host_fragment).navigate(action);*/
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        addressRef.removeEventListener(addressValueEventListener);
    }
}
