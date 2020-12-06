package com.ecom.justbakers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.justbakers.LoginActivity.UserType;
import com.ecom.justbakers.databinding.ActivityUserDetailsBinding;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.verify.sms.PhoneAuthActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.ecom.justbakers.LoginActivity.SESSION_KEY_FLAT_NUMBER;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_PIN_CODE;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_SOCIETY;

public class UserDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_DATA_CONTEXT = "CONTEXT";
    public static final String INTENT_EXTRA_DATA_USER_DISPLAY_NAME = "userDisplayName";
    public static final String INTENT_EXTRA_DATA_GMAIL = "gmail";

    private static final String TAG = "UserDetailsActivity";
    final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    String message;
    ActivityUserDetailsBinding mUserDetailsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserDetailsBinding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(mUserDetailsBinding.getRoot());

        info.hoang8f.widget.FButton verifyOTPButton = mUserDetailsBinding.placeOrderButton;
        info.hoang8f.widget.FButton updateAddressButton = mUserDetailsBinding.updateAddressButton;
        final AutoCompleteTextView phoneNumberTextView = mUserDetailsBinding.contact;
        final TextView CountryCodeTextView = mUserDetailsBinding.tvCountryCode;
        final AutoCompleteTextView displayNameTextView = mUserDetailsBinding.name;
        final AutoCompleteTextView flatNumberTextView = mUserDetailsBinding.flatNumber;
        final AutoCompleteTextView societyTextView = mUserDetailsBinding.society;
        final AutoCompleteTextView pinCodeTextView = mUserDetailsBinding.pincode;
        final String userId = LoginActivity.getLoggedInUser(this, true);
        final Firebase addressRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/info");

        ValueEventListener addressValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoClass ic = (InfoClass) dataSnapshot.getValue(InfoClass.class);
                if ( ic != null ) {
                    flatNumberTextView.setText(ic.getFlatNumber());
                    societyTextView.setText(ic.getSociety());
                    pinCodeTextView.setText(ic.getPincode());
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        addressRef.addValueEventListener(addressValueEventListener);
        Intent intent = getIntent();

        UserType user = (UserType) intent.getSerializableExtra(INTENT_EXTRA_DATA_CONTEXT);
        String gmail = intent.getStringExtra(INTENT_EXTRA_DATA_GMAIL);

        flatNumberTextView.setOnClickListener(v -> flatNumberTextView.getText().clear());

        displayNameTextView.setText(intent.getStringExtra(INTENT_EXTRA_DATA_USER_DISPLAY_NAME));
        if(UserType.NEW != user) {
            displayNameTextView.setEnabled(false);
            phoneNumberTextView.setVisibility(View.GONE);
            CountryCodeTextView.setVisibility(View.GONE);
            verifyOTPButton.setVisibility(View.GONE);
            updateAddressButton.setVisibility(View.VISIBLE);
        } else {
            displayNameTextView.setVisibility(View.VISIBLE);
            phoneNumberTextView.setVisibility(View.VISIBLE);
            CountryCodeTextView.setVisibility(View.VISIBLE);
            verifyOTPButton.setVisibility(View.VISIBLE);
            updateAddressButton.setVisibility(View.GONE);
        }
        /*
        /** GETTING THE ADDRESS DATA - AREA  INTO  LIST
        areaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList <String> areaList = new ArrayList<String>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String area = (String)postSnapshot.getKey();
                    System.out.println(area);
                    areaList.add(area);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, areaList);

                // Here, you set the data in your ListView

                //areaListView.setAdapter(adapter);
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        */
        //final Spinner societyListView = findViewById(R.id.society_list);
        //ListAdapter adapter = new ListAdapter(this, dataItems);
        //adapter.setCustomButtonListner(this);
        //listView.setAdapter(adapter);
        /*
        areaListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String area = parent.getAdapter().getItem(position).toString();
                onAreaListButtonClickListener(area);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }


        });


        societyListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSocietyIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        */

        verifyOTPButton.setOnClickListener(v -> {
            String name = displayNameTextView.getText().toString();
            String flatNumber = flatNumberTextView.getText().toString();
            String society = societyTextView.getText().toString();
            String pinCode = pinCodeTextView.getText().toString();
            String phoneNo = phoneNumberTextView.getText().toString();

            if(name.length() < 3 ) {
                Toast.makeText(getApplicationContext(), "Please enter a valid Name.", Toast.LENGTH_LONG).show();
            } else if (flatNumber.length() < 3) {
                Toast.makeText(getApplicationContext(), "Please enter a valid Flat Number.", Toast.LENGTH_LONG).show();
            } else if(!validatePhoneNumber(phoneNo)) {
                Toast.makeText(getApplicationContext(), "Please enter a valid 10 digit phone number.",
                        Toast.LENGTH_LONG).show();
            } else if(!validatePincodeNumber(pinCode)) {
                Toast.makeText(getApplicationContext(), "Please enter a valid 6 digit pincode number.",
                        Toast.LENGTH_LONG).show();
            } else {
                Intent phoneAuthIntent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_PHONE_NUMBER, phoneNo);
                phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_FLAT_NUMBER, flatNumber);
                phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_SOCIETY, society);
                phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_PIN_CODE, pinCode);
                phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_USER_DISPLAY_NAME, name);
                phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_GMAIL, gmail);
                startActivity(phoneAuthIntent);
            }
        });

        updateAddressButton.setOnClickListener(v -> {
            String name = displayNameTextView.getText().toString();
            String flatNumber = flatNumberTextView.getText().toString();
            String society = societyTextView.getText().toString();
            String pinCode = pinCodeTextView.getText().toString();
            String phoneNo = phoneNumberTextView.getText().toString();

            if (flatNumber.length() < 3) {
                Toast.makeText(getApplicationContext(), "Please enter a valid Flat Number.", Toast.LENGTH_LONG).show();
            } else if (UserType.NEW != user) {
                Utils.updateAddressDetailsInDatabase(LoginActivity.getLoggedInUser(getApplicationContext(), true), flatNumber, society, pinCode);
                List<Pair<String,String>> keyValuePairs = new ArrayList<>();
                keyValuePairs.add(new Pair<>(SESSION_KEY_SOCIETY, society));
                keyValuePairs.add(new Pair<>(SESSION_KEY_FLAT_NUMBER, flatNumber));
                keyValuePairs.add(new Pair<>(SESSION_KEY_PIN_CODE, pinCode));
                LoginActivity.setDefaults(getApplicationContext(), keyValuePairs);

                Intent intent12 = new Intent(getApplicationContext(), CartNavigationActivity.class);
                startActivity(intent12);
            } else {
                if(!validatePhoneNumber(phoneNo)) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid 10 digit phone number.", Toast.LENGTH_LONG).show();
                }else if(!validatePincodeNumber(pinCode)) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid 6 digit pin code number.", Toast.LENGTH_LONG).show();
                } else {
                    Intent phoneAuthIntent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_PHONE_NUMBER, phoneNo);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_FLAT_NUMBER, flatNumber);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_SOCIETY, society);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_PIN_CODE, pinCode);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_USER_DISPLAY_NAME, name);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_GMAIL, gmail);
                    startActivity(phoneAuthIntent);
                }
            }
        });
    }

    boolean validatePhoneNumber (String phoneNo) {
        String MobilePattern = "[0-9]{10}";
        boolean valid = false;

        if ( !phoneNo.isEmpty() && phoneNo.matches(MobilePattern))
            valid  = true;

        return valid;
    }

    boolean validatePincodeNumber (String pincodeNo) {
        String PinPattern = "[0-9]{6}";
        boolean valid = false;

        if ( !pincodeNo.isEmpty() && pincodeNo.matches(PinPattern) )
            valid  = true;

        return valid;
    }
    /*
        public void onAreaListButtonClickListener(String area) {
            Firebase societyRef = new Firebase("https://justbakers-285be.firebaseio.com/deliveryAddress").child(area);

     //      Firebase ref = societyRef.child(area);
            /** GETTING THE ADDRESS DATA - SOCIETY  INTO  LIST
            societyRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList <String> societyList = new ArrayList<String>();
                    Spinner societyListView = findViewById(R.id.society_list);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                     String str = (String)postSnapshot.getValue();
                     societyList.add(str);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, societyList);

                    // Here, you set the data in your ListView

                    societyListView.setAdapter(adapter);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        };

    */
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(mUserDetailsBinding.contact.getText().toString(), null, message, null, null);
                Toast.makeText(getApplicationContext(), "OTP sent.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
