package com.ecom.justbakers.sms_verify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ecom.justbakers.AdminActivity;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.MainActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.UserActivity;
import com.ecom.justbakers.gpay.TempCheckoutActivity;
import com.ecom.justbakers.orders.InfoClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static com.ecom.justbakers.sms_verify.AppSignatureHelper.TAG;

public class PhoneAuthActivity extends AppCompatActivity {
    boolean mVerificationInProgress = false;
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private String mPhoneNumber;
    private String mName;
    private String mSociety;
    private String mPincode;
    private String mFlatNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_auth);

        mPhoneNumber = "+91" + getIntent().getStringExtra("CONTACT");
        mName = getIntent().getStringExtra("NAME");
        mPincode = getIntent().getStringExtra("PINCODE");
        mSociety = getIntent().getStringExtra("SOCIETY");
        mFlatNumber = getIntent().getStringExtra("FLAT_NUMBER");

        signInWithPhoneNumber();
        Button btn = findViewById(R.id.buttonProceed);
        final EditText et = findViewById(R.id.editTextCode);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating the credential
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, et.getText().toString());
                signInWithPhoneAuthCredential(credential);
            }
        });
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneAuthActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //verification successful we will start the profile activity
                            storeDetailsInDatabase(LoginActivity.getDefaults("UserID", getApplicationContext()), LoginActivity.getDefaults("Gmail", getApplicationContext()),
                                                                              mName, mPhoneNumber , mFlatNumber, mSociety, mPincode );
                            LoginActivity.setDefaults("Name", mName, getApplicationContext());
                            LoginActivity.setDefaults("Phone", mPhoneNumber, getApplicationContext());
                            LoginActivity.setDefaults("Society", mSociety, getApplicationContext());
                            LoginActivity.setDefaults("Flat", mFlatNumber, getApplicationContext());
                            LoginActivity.setDefaults("Pincode", mPincode, getApplicationContext());
                            Intent intent = new Intent(PhoneAuthActivity.this, UserActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }


    public void signInWithPhoneNumber() {


        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.i(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onCodeSent(@NonNull String var1, @NonNull PhoneAuthProvider.ForceResendingToken var2) {

                mVerificationId = var1;
                Log.i(TAG, "onCodeSent:" + mVerificationId);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.w(TAG, "onVerificationFailed", e);
                    Log.i(TAG, "onVerificationFailed" + "Invalid Phone Number");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.w(TAG, "onVerificationFailed", e);
                    Log.i(TAG, "onVerificationFailed" + "SMS quota exceeded");
                }

            }

        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhoneNumber,     // Phone Number
                60,              // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,    // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }


    public void storeDetailsInDatabase (String userid, String gmail, String name, String phone_number, String area, String society, String flatNumber) {


     //   String userid = md5(gmail); //gmail.split("@");
        Firebase custRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userid + "/info");
        InfoClass infoObject = new InfoClass(gmail, name, phone_number, area, society, flatNumber);
        //custRef.child("info").push().setValue(infoObject);
        custRef.setValue(infoObject);

    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void storeAddressDetailsInDatabase (String gmail, String flatNumber, String society, String pincode) {

        String userid = md5 (gmail);
        Firebase custRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userid + "/info");

        //custRef.child("info").push().setValue(infoObject);
        custRef.child("flatNumber").setValue(flatNumber);
        custRef.child("society").setValue(society);
        custRef.child("pincode").setValue(pincode);

    }

    public static void updateAddressDetailsInDatabase (String userid, String flatNumber, String society, String pincode) {

        Firebase custRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userid + "/info");

        //custRef.child("info").push().setValue(infoObject);
        custRef.child("flatNumber").setValue(flatNumber);
        custRef.child("society").setValue(society);
        custRef.child("pincode").setValue(pincode);

    }

   }