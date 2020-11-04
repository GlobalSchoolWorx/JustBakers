package com.ecom.justbakers.gpay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ecom.justbakers.CartActivity;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.sms_verify.AppSignatureHelper;
import com.ecom.justbakers.sms_verify.PhoneAuthActivity;
import com.ecom.justbakers.sms_verify.VerifyOtpActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Random;

public class TempCheckoutActivity extends AppCompatActivity {
    int RESOLVE_HINT = 0;
    final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    String message;
    String phoneNo;
    String name;
    String society;
    String pincode;
    String flatNumber;
    int verification_code;
    private boolean newLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_checkout);
        info.hoang8f.widget.FButton placeOrderButton = findViewById(R.id.placeOrderButton);
        info.hoang8f.widget.FButton updateAddressButton = findViewById(R.id.updateAddressButton);
        final AutoCompleteTextView tvContact = findViewById(R.id.contact);
        final TextView tvCode = findViewById(R.id.tvCountryCode);
        final AutoCompleteTextView tvName = findViewById(R.id.name);
        final AutoCompleteTextView tvFlatNumber = findViewById(R.id.flat_number);
        final AutoCompleteTextView tvSociety = findViewById(R.id.society);
        final AutoCompleteTextView tvPincode = findViewById(R.id.pincode);
        String userId = LoginActivity.getDefaults("UserID", this);
        final Firebase addressRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/info");

        ValueEventListener addr_vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                InfoClass ic = (InfoClass) dataSnapshot.getValue(InfoClass.class);
                if ( ic != null ) {
                    society = ic.getSociety();
                    flatNumber = ic.getFlatNumber();
                    pincode = ic.getPincode();

                    tvFlatNumber.setText(flatNumber);
                    tvSociety.setText(society);
                    tvPincode.setText(pincode);
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        addressRef.addValueEventListener(addr_vel);
        Intent intent = getIntent();

        String srcContext = intent.getStringExtra("CONTEXT");
        if(srcContext.equals("NEW"))
            newLogin = true;

        tvFlatNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFlatNumber.getText().clear();
            }
        });
        if(!newLogin) {
            tvName.setVisibility(View.GONE);
            tvContact.setVisibility(View.GONE);
            tvCode.setVisibility(View.GONE);
            placeOrderButton.setVisibility(View.GONE);
            updateAddressButton.setVisibility(View.VISIBLE);
        } else {
            tvName.setVisibility(View.VISIBLE);
            tvContact.setVisibility(View.VISIBLE);
            tvCode.setVisibility(View.VISIBLE);
            placeOrderButton.setVisibility(View.VISIBLE);
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

        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = tvName.getText().toString();
                flatNumber = tvFlatNumber.getText().toString();
                society = tvSociety.getText().toString();
                pincode = tvPincode.getText().toString();
                phoneNo = tvContact.getText().toString();

                if(name.length() < 3 ) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid Name.",
                            Toast.LENGTH_LONG).show();
                } else if (flatNumber.length() < 3) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid Flat Number.",
                            Toast.LENGTH_LONG).show();
                }
                else if(!validatePhoneNumber(phoneNo))
                {
                    Toast.makeText(getApplicationContext(), "Please enter a valid 10 digit phone number.",
                            Toast.LENGTH_LONG).show();
                } else if(!validatePincodeNumber(pincode))
                {
                    Toast.makeText(getApplicationContext(), "Please enter a valid 6 digit pincode number.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                    Integer tc = new Integer(verification_code);


                    intent.putExtra("CONTACT", phoneNo);
                    intent.putExtra("FLAT_NUMBER", flatNumber);
                    intent.putExtra("SOCIETY", society);
                    intent.putExtra("PINCODE", pincode);
                    intent.putExtra("NAME", name);
                    startActivity(intent);
                }
            }
        });

        updateAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                flatNumber = tvFlatNumber.getText().toString();
                society = tvSociety.getText().toString();
                pincode = tvPincode.getText().toString();

                if (flatNumber == null || flatNumber.length() < 3) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid Flat Number.",
                            Toast.LENGTH_LONG).show();
                } else if (!newLogin) {
                    PhoneAuthActivity.updateAddressDetailsInDatabase(LoginActivity.getDefaults("UserID", getApplicationContext()), flatNumber, society, pincode);
                    Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                    startActivity(intent);
                }
                else {
                        Intent intent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                        Integer tc = new Integer(verification_code);
                        intent.putExtra("CONTACT", phoneNo);
                        intent.putExtra("FLAT_NUMBER", flatNumber);
                        intent.putExtra("SOCIETY", society);
                        intent.putExtra("PINCODE", pincode);
                        intent.putExtra("NAME", name);
                        startActivity(intent);
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
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "OTP sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }


}
