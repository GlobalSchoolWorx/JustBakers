package com.ecom.justbakers.verify.sms;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.ecom.justbakers.LaunchActivity;
import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.ecom.justbakers.databinding.ActivityPhoneAuthBinding;
import com.ecom.justbakers.orders.InfoClass;
import com.firebase.client.Firebase;
import com.firebase.client.annotations.NotNull;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import static com.ecom.justbakers.LoginActivity.SESSION_KEY_DISPLAY_NAME;
import static com.ecom.justbakers.LoginActivity.SESSION_KEY_PHONE_NUMBER;

public class PhoneAuthActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String INTENT_EXTRA_DATA_PHONE_NUMBER = "phoneNumber";
    public static final String INTENT_EXTRA_DATA_FLAT_NUMBER = "flatNumber";
    public static final String INTENT_EXTRA_DATA_SOCIETY = "society";
    public static final String INTENT_EXTRA_DATA_PIN_CODE = "pin_code";
    public static final String INTENT_EXTRA_DATA_USER_DISPLAY_NAME = "userDisplayName";
    public static final String INTENT_EXTRA_DATA_GMAIL = "gmail";

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGN_IN_FAILED = 5;
    private static final int STATE_SIGN_IN_SUCCESS = 6;
    private static final String TAG = "PhoneAuthActivity";

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ActivityPhoneAuthBinding mBinding;

    private String mName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setIsVerificationInProgress(false);

        mBinding = ActivityPhoneAuthBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mName = getIntent().getStringExtra(INTENT_EXTRA_DATA_USER_DISPLAY_NAME);

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        mBinding.titleText.setText(getIntent().getStringExtra(INTENT_EXTRA_DATA_GMAIL));
        mBinding.buttonStartVerification.setOnClickListener(this);
        mBinding.buttonVerifyPhone.setOnClickListener(this);
        mBinding.buttonResend.setOnClickListener(this);
        mBinding.signOutButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mCallbacks = OnVerificationStateChangedCallbacks.getCallbacks(this, new OnVerificationStateChangedCallbacks.OnSMSVerificationStateChangedListener() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                setIsVerificationInProgress(false);
                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                setIsVerificationInProgress(false);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mBinding.fieldPhoneNumber.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(mBinding.coordinatorLayout, "SMS Quota exceeded.", Snackbar.LENGTH_SHORT).show();
                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                setVerificationId(verificationId);
                setResendVerificationToken(token);
                updateUI(STATE_CODE_SENT);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        if (isVerificationInProgress() && validatePhoneNumber()) {
            startPhoneNumberVerification(getPhoneNumber());
        }else if(isVerificationInProgress()){
            //Somehow the phone number is not valid, may be when the activity was restarted, the phone number gof goofed up.
            setIsVerificationInProgress(false);
            mBinding.progressbar.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, isVerificationInProgress());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setIsVerificationInProgress(savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS));
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        mBinding.progressbar.setVisibility(View.VISIBLE);
        setIsVerificationInProgress(true);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }


    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        mBinding.progressbar.setVisibility(View.VISIBLE);
        setIsVerificationInProgress(true);
    }

    private String getPhoneNumber(){
        if(null != mBinding.fieldPhoneNumber.getText()) {
            return mBinding.countryCodePhoneNumber.getText() + mBinding.fieldPhoneNumber.getText().toString();
        }
        return null;
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "signInWithCredential:success");
                String phoneNumber = getPhoneNumber();
                storeDetailsInDatabase(LoginActivity.getLoggedInUser(getApplicationContext(), true),
                        LoginActivity.getLoggedInUser(getApplicationContext(), false),
                        mName, phoneNumber);

                List<Pair<String, String>> keyValuePairs = new ArrayList<>();
                keyValuePairs.add(new Pair<>(SESSION_KEY_DISPLAY_NAME, mName));
                keyValuePairs.add(new Pair<>(SESSION_KEY_PHONE_NUMBER, phoneNumber));
                LoginActivity.setDefaults(getApplicationContext(), keyValuePairs);
                Intent intent = new Intent(PhoneAuthActivity.this, LaunchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                FirebaseUser user = task.getResult().getUser();
                updateUI(STATE_SIGN_IN_SUCCESS, user);
            } else {
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    mBinding.fieldVerificationCode.setError("Invalid code.");
                }
                updateUI(STATE_SIGN_IN_FAILED);
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGN_IN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(@SuppressWarnings("SameParameterValue") int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(@SuppressWarnings("SameParameterValue") int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                enableViews(mBinding.buttonStartVerification, mBinding.fieldPhoneNumber);
                disableViews(mBinding.buttonVerifyPhone, mBinding.buttonResend, mBinding.fieldVerificationCode);
                mBinding.detail.setText(null);
                break;
            case STATE_CODE_SENT:
                mBinding.progressbar.setVisibility(View.GONE);
                enableViews(mBinding.buttonVerifyPhone, mBinding.buttonResend, mBinding.fieldPhoneNumber, mBinding.fieldVerificationCode);
                disableViews(mBinding.buttonStartVerification);
                mBinding.detail.setText(R.string.status_code_sent);
                break;
            case STATE_VERIFY_FAILED:
                mBinding.progressbar.setVisibility(View.GONE);
                /*enableViews(mBinding.buttonStartVerification, mBinding.buttonVerifyPhone, mBinding.buttonResend, mBinding.fieldPhoneNumber,
                        mBinding.fieldVerificationCode);*/
                enableViews(mBinding.fieldPhoneNumber, mBinding.buttonStartVerification, mBinding.buttonResend);
                disableViews(mBinding.buttonVerifyPhone, mBinding.fieldVerificationCode);
                mBinding.detail.setText(R.string.status_verification_failed);
                break;
            case STATE_VERIFY_SUCCESS:
                mBinding.progressbar.setVisibility(View.GONE);
                disableViews(mBinding.buttonStartVerification, mBinding.buttonVerifyPhone, mBinding.buttonResend, mBinding.fieldPhoneNumber,
                        mBinding.fieldVerificationCode);
                mBinding.detail.setText(R.string.status_verification_succeeded);

                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mBinding.fieldVerificationCode.setText(cred.getSmsCode());
                    } else {
                        mBinding.fieldVerificationCode.setText(R.string.instant_validation);
                    }
                }

                break;
            case STATE_SIGN_IN_FAILED:
                mBinding.progressbar.setVisibility(View.GONE);
                mBinding.detail.setText(R.string.status_sign_in_failed);
                break;
            case STATE_SIGN_IN_SUCCESS:
                break;
        }

        if (user == null) {
            mBinding.phoneAuthFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);

            mBinding.status.setText(R.string.signed_out);
        } else {
            mBinding.phoneAuthFields.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);

            enableViews(mBinding.fieldPhoneNumber, mBinding.fieldVerificationCode);
            mBinding.fieldPhoneNumber.setText(null);
            mBinding.fieldVerificationCode.setText(null);

            mBinding.status.setText(R.string.signed_in);
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = getPhoneNumber();
        if (TextUtils.isEmpty(phoneNumber)) {
            mBinding.fieldPhoneNumber.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private boolean isVerificationInProgress(){
        MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        return Objects.requireNonNull(model.getPhoneAuthData().getValue()).isVerificationInProgress;
    }

    private void setIsVerificationInProgress(boolean value){
        MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        Objects.requireNonNull(model.getPhoneAuthData().getValue()).isVerificationInProgress = value;
    }

    private String getVerificationId(){
        MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        return Objects.requireNonNull(model.getPhoneAuthData().getValue()).verificationId;
    }

    private void setVerificationId(String verificationId){
        MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        Objects.requireNonNull(model.getPhoneAuthData().getValue()).verificationId = verificationId;
    }

    private PhoneAuthProvider.ForceResendingToken getResendToken(){
        MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        return Objects.requireNonNull(model.getPhoneAuthData().getValue()).resendToken;
    }

    private void setResendVerificationToken(PhoneAuthProvider.ForceResendingToken token){
        MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
        Objects.requireNonNull(model.getPhoneAuthData().getValue()).resendToken = token;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.buttonStartVerification) {
            if (!validatePhoneNumber()) {
                return;
            }
            startPhoneNumberVerification(getPhoneNumber());
        } else if (id == R.id.buttonVerifyPhone) {
            String code = mBinding.fieldVerificationCode.getText().toString();
            if (TextUtils.isEmpty(code)) {
                mBinding.fieldVerificationCode.setError("Cannot be empty.");
                return;
            }

            try {
                verifyPhoneNumberWithCode(getVerificationId(), code);
            } catch (Exception ex) {
                Log.e(TAG, "Input Code : " + code + " is wrong.", ex);
                Snackbar.make(findViewById(R.id.coordinator_layout), "Verification Code is wrong", Snackbar.LENGTH_LONG).show();
            }
        } else if (id == R.id.buttonResend) {
            resendVerificationCode(getPhoneNumber(), getResendToken());
        } else if (id == R.id.signOutButton) {
            signOut();
        }
    }

    public void storeDetailsInDatabase (String userId, String gmail, String name, String phoneNumber) {
        Firebase custRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId + "/info");
        InfoClass infoObject = new InfoClass(gmail, name, phoneNumber, null, null, null);
        custRef.setValue(infoObject);
    }

    public static class MyViewModel extends ViewModel {
        private MutableLiveData<PhoneAuthData> phoneAuthDataMutableLiveData;

        public @NotNull LiveData<PhoneAuthData> getPhoneAuthData() {
            if (phoneAuthDataMutableLiveData == null) {
                phoneAuthDataMutableLiveData = new MutableLiveData<>();
                phoneAuthDataMutableLiveData.setValue(new PhoneAuthData());
            }
            return phoneAuthDataMutableLiveData;
        }
    }

    public static class PhoneAuthData{
        private String verificationId;
        private PhoneAuthProvider.ForceResendingToken resendToken;
        private boolean isVerificationInProgress = false;
    }
}