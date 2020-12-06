package com.ecom.justbakers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.ecom.justbakers.orders.InfoClass;
import com.ecom.justbakers.verify.sms.PhoneAuthActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static com.ecom.justbakers.Utils.md5;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final String SESSION_KEY_USER_ID = "UserID";
    public static final String SESSION_KEY_GMAIL = "Gmail";
    public static final String SESSION_KEY_ADMIN_USER = "AdminUser";
    public static final String SESSION_KEY_DISPLAY_NAME = "Name";
    public static final String SESSION_KEY_NAME_ON_ORDER = "NameOnOrder";
    public static final String SESSION_KEY_PHONE_NUMBER = "Phone";
    public static final String SESSION_KEY_SOCIETY = "Society";
    public static final String SESSION_KEY_FLAT_NUMBER = "Flat";
    public static final String SESSION_KEY_PIN_CODE = "Pincode";

    private static final int RC_SIGN_IN = 0;
    private static final int RC_ON_BACK_FROM_LAUNCH = 1;

    private Firebase customersRef = new Firebase ("https://justbakers-285be.firebaseio.com/customers/");
    private final Firebase adminRef = new Firebase ("https://justbakers-285be.firebaseio.com/admin/");
    private ValueEventListener adminValueEventListener, customerValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.blackback));
            actionBar.setTitle(getResources().getString(R.string.welcome_app_name));
        }
        Log.i(TAG, "onCreate:" + "LoginActivity");
        setContentView(R.layout.activity_login);

        getAdmin();
        getSignIn();

        ShimmerTextView tagline = findViewById(R.id.tagline);
        ShimmerTextView tagline2 = findViewById(R.id.tagline2);
        ShimmerTextView tagline3 = findViewById(R.id.tagline3);
        Shimmer shimmer = new Shimmer();
        Shimmer shimmer1 = new Shimmer();
        Shimmer shimmer2 = new Shimmer();
        shimmer.setRepeatCount(500)
                .setDuration(3000)
                .setStartDelay(0)
                .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)
                .start(tagline);
        shimmer1.setRepeatCount(500)
                .setDuration(3000)
                .setStartDelay(0)
                .setDirection(Shimmer.ANIMATION_DIRECTION_RTL)
                .start(tagline2);
        shimmer2.setRepeatCount(500)
                .setDuration(3000)
                .setStartDelay(0)
                .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)
                .start(tagline3);

        /* FACEBOOK BUTTON **/
        ImageButton facebook = findViewById(R.id.fb);
        facebook.setOnClickListener(v -> goToUrl(getResources().getString(R.string.facebook)));
        /* EMAIL BUTTON **/
        ImageButton googleMail = findViewById(R.id.gmail);
        googleMail.setOnClickListener(v -> {
            Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" +getResources().getString(R.string.googlemail) ));
            startActivity(intent);
        });
        /* LINKEDIN BUTTON **/
        ImageButton Linkedin = findViewById(R.id.linkedin);
        Linkedin.setOnClickListener(v -> goToUrl(getResources().getString(R.string.linkedin)));
        /* GITHUB BUTTON **/
        ImageButton Github = findViewById(R.id.github);
        Github.setOnClickListener(v -> goToUrl(getResources().getString(R.string.github)));

        facebook.setVisibility(View.GONE);
        googleMail.setVisibility(View.GONE);
        Linkedin.setVisibility(View.GONE);
        Github.setVisibility(View.GONE);
    }

    private void getAdmin() {
        Context cxt = getApplicationContext();
        adminValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setDefaults(cxt, SESSION_KEY_ADMIN_USER, (String) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        adminRef.addValueEventListener(adminValueEventListener);
    }
    // OPENING OF BROWSER FOR PROFILE URLS
    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }


    private void getSignIn() {
        // GoogleSignInClient with the options specified by gso.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.i(TAG, "getSignIn:" + "LoginActivity");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "Not yet starting  handleSignInResult " + "LoginActivity");
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.i(TAG, "Starting handleSignInResult " + "LoginActivity");
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else if(requestCode == RC_ON_BACK_FROM_LAUNCH){
            Log.i(TAG, "Exiting from application.");

            finish();
        }
    }


    public void checkIfExistingCustomer(final String userId, final String userDisplayName, final String gmail) {
        Log.i(TAG, "checkIfExistingCustomer ");
        customersRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/" + userId);
        Context cxt = getApplicationContext();
        customerValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "checkIfExistingCustomer ValueEventListener onDataChange");
                String dval = dataSnapshot.getKey();
                boolean customerFound = dval.equals(userId) && (null != dataSnapshot.getValue());
                Log.i(TAG, "Customer Found : " + customerFound);

                customersRef.removeEventListener(this);
                if (!customerFound) { // Ask for Registration Details
                    Log.i(TAG, "Starting Auth flow!");
                    Intent phoneAuthIntent = new Intent(cxt, PhoneAuthActivity.class);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_USER_DISPLAY_NAME, userDisplayName);
                    phoneAuthIntent.putExtra(PhoneAuthActivity.INTENT_EXTRA_DATA_GMAIL, gmail);
                    startActivity(phoneAuthIntent);
                } else {
                    if(dataSnapshot.hasChild("info")){
                        for(DataSnapshot customerInfoDataSnapshot : dataSnapshot.getChildren()){
                            if("info".equals(customerInfoDataSnapshot.getKey())) {
                                InfoClass ic = customerInfoDataSnapshot.getValue(InfoClass.class);
                                List<Pair<String, String>> keyValuePairs = new ArrayList<>();
                                keyValuePairs.add(new Pair<>(SESSION_KEY_PHONE_NUMBER, ic.getPhoneNumber()));
                                keyValuePairs.add(new Pair<>(SESSION_KEY_SOCIETY, ic.getSociety()));
                                keyValuePairs.add(new Pair<>(SESSION_KEY_FLAT_NUMBER, ic.getFlatNumber()));
                                keyValuePairs.add(new Pair<>(SESSION_KEY_PIN_CODE, ic.getPincode()));
                                LoginActivity.setDefaults(cxt, keyValuePairs);
                                break;
                            }
                        }
                    }

                    Log.i(TAG, "Starting LaunchActivity");
                    Intent userIntent = new Intent(cxt, LaunchActivity.class);
                    userIntent.putExtra(LaunchActivity.INTENT_EXTRA_DATA_USER_DISPLAY_NAME, userDisplayName);
                    userIntent.putExtra(LaunchActivity.INTENT_EXTRA_DATA_GMAIL, gmail);
                    startActivityForResult(userIntent, RC_ON_BACK_FROM_LAUNCH);
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "The read failed: " + firebaseError.getMessage());
            }
        };

        customersRef.addValueEventListener(customerValueEventListener);

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.i(TAG, "handleSignInResult Entering...");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i(TAG, "handleSignInResult " + "Getting Account Details");
            // UI references.
            String accountName = account.getEmail();
            Log.i(TAG, "handleSignInResult AccountName:= " + accountName);
            String [] splUserId = accountName.split("@");
            String userId = md5 (splUserId[0]);
            Log.i(TAG, "handleSignInResult User Id:= " + splUserId[0]);

            setDefaults(this, SESSION_KEY_DISPLAY_NAME, account.getDisplayName());
            setDefaults(this, SESSION_KEY_USER_ID, userId);
            setDefaults(this, SESSION_KEY_GMAIL, splUserId[0]);
            ProgressBar mProgressBar = findViewById(R.id.progressbar);
            mProgressBar.setVisibility(View.GONE);
            checkIfExistingCustomer (userId, account.getDisplayName(), accountName);
        } catch (ApiException e) {
            Log.e(TAG, "handleSignInResult Exception" + "LoginActivity", e);
        }
    }

    public static void setDefaults( @NonNull Context context, @NonNull List<Pair<String, String>> keyValuePairs) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for(Pair<String, String> keyValuePair : keyValuePairs){
            if(null != keyValuePair.second) {
                editor.putString(keyValuePair.first, keyValuePair.second);
            }else{
                if(prefs.contains(keyValuePair.first)) {
                    editor.remove(keyValuePair.first);
                }
            }
        }
        editor.apply();
    }

    public static void setDefaults( @NonNull Context context,  @NonNull String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if(null != value) {
            editor.putString(key, value);
        }else{
            if(prefs.contains(key)) {
                editor.remove(key);
            }
        }

        editor.apply();
    }

    public static @NonNull String getLoggedInUser(Context context, boolean scrambled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(scrambled) {
            return preferences.getString(SESSION_KEY_USER_ID, "");
        }else{
            return preferences.getString(SESSION_KEY_GMAIL, "");
        }
    }

    public static @NonNull Boolean isUserLoggedIn(Context context) {
        return !("".equals(getLoggedInUser(context, true)));
    }

    public static @NonNull Boolean isLoggedInUserAdmin(Context context) {
        return getLoggedInUser(context, false).equals(getDefaults(context, SESSION_KEY_ADMIN_USER));
    }

    public static void signOut(Activity activity, Runnable callback){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, gso);
        googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            List<Pair<String, String>> keyValuePairs = new ArrayList<>();
            keyValuePairs.add(new Pair<>(SESSION_KEY_USER_ID, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_GMAIL, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_ADMIN_USER, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_PHONE_NUMBER, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_DISPLAY_NAME, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_NAME_ON_ORDER, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_SOCIETY, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_FLAT_NUMBER, null));
            keyValuePairs.add(new Pair<>(SESSION_KEY_PIN_CODE, null));
            LoginActivity.setDefaults(activity, keyValuePairs);
            callback.run();
        });
    }

    public static @NonNull String getDefaults(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop :" + "LoginActivity");
        try {
            if (null != customersRef && null != customerValueEventListener) {
                customersRef.removeEventListener(customerValueEventListener);
            }

            if (null != adminRef && null != adminValueEventListener) {
                adminRef.removeEventListener(adminValueEventListener);
            }
        }catch (Throwable ignore) {

            Log.i(TAG, "onStop Exception " + "LoginActivity");
        }
    }

    public enum UserType{
        NEW,
        EXISTING
    }
}

