package com.ecom.justbakers.gpay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ecom.justbakers.CartActivity;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
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
    String area;
    String flatNumber;
    int verification_code;
    private int selectedSocietyIndex = -1;
    private boolean newLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_checkout);
        info.hoang8f.widget.FButton placeOrderButton = findViewById(R.id.placeOrderButton);
        info.hoang8f.widget.FButton updateAddressButton = findViewById(R.id.updateAddressButton);
        final AutoCompleteTextView tvContact = findViewById(R.id.contact);
        final AutoCompleteTextView tvName = findViewById(R.id.name);
        final Spinner sArea = findViewById(R.id.area_list);
        final ListView tvSociety = findViewById(R.id.society_list);
        final AutoCompleteTextView tvFlatNumber = findViewById(R.id.flatNumber);

        final Firebase areaRef = new Firebase("https://justbakers-285be.firebaseio.com/deliveryAddress");
        final Spinner areaListView = findViewById(R.id.area_list);

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
            placeOrderButton.setVisibility(View.GONE);
            updateAddressButton.setVisibility(View.VISIBLE);
        } else {
            tvName.setVisibility(View.VISIBLE);
            tvContact.setVisibility(View.VISIBLE);
            placeOrderButton.setVisibility(View.VISIBLE);
            updateAddressButton.setVisibility(View.GONE);
        }

        /** GETTING THE ADDRESS DATA - AREA  INTO  LIST **/
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

                areaListView.setAdapter(adapter);
            }


            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        final ListView societyListView = findViewById(R.id.society_list);
        //ListAdapter adapter = new ListAdapter(this, dataItems);
        //adapter.setCustomButtonListner(this);
        //listView.setAdapter(adapter);
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


        societyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSocietyIndex = position;
            }
        });



        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNo = tvContact.getText().toString();
                flatNumber = tvFlatNumber.getText().toString();
                name = tvName.getText().toString();
                area = areaListView.getSelectedItem().toString();
                society = societyListView.getItemAtPosition(selectedSocietyIndex).toString();

                //requestHint();
                /** Start Listening SMSRetriever ***/
                getRetrieverClient();
                //sendSMSMessage(phoneNo);
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
                } else if(selectedSocietyIndex < 0)
                {
                    Toast.makeText(getApplicationContext(), "Please select your society or a society that is in your neighbourhood.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                    Integer tc = new Integer(verification_code);
                    intent.putExtra("CONTACT", phoneNo);
                    intent.putExtra("AREA", area);
                    intent.putExtra("SOCIETY", society);
                    intent.putExtra("FLAT_NUMBER", flatNumber);
                    intent.putExtra("NAME", name);
                    //intent.putExtra("VERIFICATION_CODE", tc.toString());
                    startActivity(intent);
                }
            }
        });

        updateAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                flatNumber = tvFlatNumber.getText().toString();
                area = areaListView.getSelectedItem().toString();


                //requestHint();
                /** Start Listening SMSRetriever ***/
                getRetrieverClient();
                if (flatNumber == null || flatNumber.length() < 3) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid Flat Number.",
                            Toast.LENGTH_LONG).show();
                } else if(selectedSocietyIndex < 0)
                {
                    Toast.makeText(getApplicationContext(), "Please select your society or a society that is in your neighbourhood.",
                            Toast.LENGTH_LONG).show();
                } else if (!newLogin) {
                    society = societyListView.getItemAtPosition(selectedSocietyIndex).toString();
                    PhoneAuthActivity.storeAddressDetailsInDatabase(LoginActivity.getDefaults("UserID", getApplicationContext()), area, society, flatNumber);
                    Intent intent = new Intent(getApplicationContext(), CartActivity.class);
                    startActivity(intent);
                }
                else {
                        society = societyListView.getItemAtPosition(selectedSocietyIndex).toString();
                        Intent intent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                        Integer tc = new Integer(verification_code);
                        intent.putExtra("CONTACT", phoneNo);
                        intent.putExtra("AREA", area);
                        intent.putExtra("SOCIETY", society);
                        intent.putExtra("FLAT_NUMBER", flatNumber);
                        intent.putExtra("NAME", name);
                        //intent.putExtra("VERIFICATION_CODE", tc.toString());
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

    public  void getRetrieverClient() {
        // Get an instance of SmsRetrieverClient, used to start listening for a matching
        // SMS message.
        SmsRetrieverClient client = SmsRetriever.getClient(this);

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                String otp_text = "Waiting for the OTP";
            }

        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to start retriever, inspect Exception for more details
                String otp_text = "Cannot Start SMS Retriever";
            }
        });
    }
    // Construct a request for phone numbers and show the picker
    private void requestHint() throws IntentSender.SendIntentException {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();


        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                mGoogleSignInClient.asGoogleApiClient(), hintRequest);
        startIntentSenderForResult(intent.getIntentSender(),
                RESOLVE_HINT, null, 0, 0, 0);
    }


    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
            }
        }
    }

    public void onAreaListButtonClickListener(String area) {
        Firebase societyRef = new Firebase("https://justbakers-285be.firebaseio.com/deliveryAddress").child(area);

 //      Firebase ref = societyRef.child(area);
        /** GETTING THE ADDRESS DATA - SOCIETY  INTO  LIST **/
        societyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList <String> societyList = new ArrayList<String>();
                ListView societyListView = findViewById(R.id.society_list);
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


    protected void sendSMSMessage(String phoneNo) {

        PhoneAuthActivity ph = new PhoneAuthActivity();

        ph.signInWithPhoneNumber();

        verification_code = new Random().hashCode();
        verification_code = verification_code / 10000;
        message = "<#> JustBakers: Your verification code is" + verification_code + "\n";

        AppSignatureHelper obj = new AppSignatureHelper(getApplicationContext());
        ArrayList<String> strArr = obj.getAppSignatures();
        message = message + strArr;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "OTP sent.",
                    Toast.LENGTH_LONG).show();
        }
    }

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
