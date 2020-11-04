package com.ecom.justbakers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.ecom.justbakers.gpay.TempCheckoutActivity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static com.ecom.justbakers.sms_verify.AppSignatureHelper.TAG;
import static com.ecom.justbakers.sms_verify.PhoneAuthActivity.md5;

public class LoginActivity extends AppCompatActivity {
    // UI references.
    String account_name;
    static String admin;
    final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;
    static GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 0;

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail().build();

    private Firebase cartRef = new Firebase ("https://justbakers-285be.firebaseio.com/customers/");
    private Firebase adminRef = new Firebase ("https://justbakers-285be.firebaseio.com/admin/");
    private ValueEventListener vel, admin_vel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // REMOVE TITLE AND MAKE ACTIVITY FULLSCREEN
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.blackback));
            actionBar.setTitle(getResources().getString(R.string.welcome_app_name));
        }

        Log.i(TAG, "onCreate:" + "LoginActivity");
        setContentView(R.layout.activity_login);

        getAdmin();
        getSignIn();


        /*SHIMMERING TEXT VIEW ANIMATIONS*/
        ShimmerTextView tagline = findViewById(R.id.Tagline);
        ShimmerTextView tagline2 = findViewById(R.id.Tagline2);
        ShimmerTextView tagline3 = findViewById(R.id.Tagline3);
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

       // mUserView = (AutoCompleteTextView) findViewById(R.id.loginusername);

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



        /* ONCLICKLISTENER ON VIEW AS SELLER BUTTON
        Button mSignInButtonSeller = (Button) findViewById(R.id.sign_in_seller);
        mSignInButtonSeller.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sellerintent = new Intent(LoginActivity.this, SellerActivity.class);
                LoginActivity.this.startActivity(sellerintent);
            }
        });
      */
        //mSignInButtonSeller.setVisibility(View.GONE);

    }

    static String getAdminUser () {
        return admin;
    }
    private void getAdmin() {

        admin_vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                admin = (String) dataSnapshot.getValue();

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        adminRef.addValueEventListener(admin_vel);
    }
    // OPENING OF BROWSER FOR PROFILE URLS
    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }


    private void getSignIn() {
        // GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.i(TAG, "getSignIn:" + "LoginActivity");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static GoogleSignInClient getGoogleSignInClient () {
        return mGoogleSignInClient;
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
        }
    }


    public void checkIfExistingCustomer(final String gmail) {

        Log.i(TAG, "checkIfExistingCustomer " + "LoginActivity");
        vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "checkIfExistingCustomer ValueEventListener onDataChange" + "LoginActivity");
                String [] userId = gmail.split("@");
                boolean mCustomerFound = false;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String dval = postSnapshot.getKey();

                    if (dval.equals(userId[0])) {
                        mCustomerFound = true;
                        Log.i(TAG, "Customer Found" + "LoginActivity");
                        break;
                    }
                }

                if (!mCustomerFound) { // Ask for Registration Details
                    Log.i(TAG, "Customer Not Found" + "LoginActivity");
                    Intent userintent = new Intent(getApplicationContext(), TempCheckoutActivity.class);
                    userintent.putExtra("CONTEXT", "NEW");
                    startActivity(userintent);
                } else {
                    String user = LoginActivity.getDefaults("Gmail", getApplicationContext());
                    /*
                    if (user.equals(admin)) {
                        Log.i(TAG, "Admin" + "LoginActivity");
                        Intent userintent = new Intent(getApplicationContext(), AdminActivity.class);
                        //Intent userintent = new Intent(LoginActivity.this, PhoneAuthActivity.class);
                        LoginActivity.this.startActivity(userintent);
                    } else */
                    {
                        Log.i(TAG, "Starting UserActivity" + "LoginActivity");
                        Intent userintent = new Intent(LoginActivity.this, UserActivity.class);
                        //Intent userintent = new Intent(LoginActivity.this, PhoneAuthActivity.class);
                        LoginActivity.this.startActivity(userintent);
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        cartRef.addValueEventListener(vel);

    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.i(TAG, "handleSignInResult " + "LoginActivity");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i(TAG, "handleSignInResult " + "Getting Account Details");
            account_name = account.getEmail();
            Log.i(TAG, "handleSignInResult " + account_name);
            String [] splUserId = account_name.split("@");
            String userId = md5 (splUserId[0]);
            Log.i(TAG, "handleSignInResult User Id " + splUserId[0]);

            setDefaults("UserID", userId , this);
            setDefaults("Gmail", splUserId[0], this);
            Log.i(TAG, "handleSignInResult" + "LoginActivity");
            ProgressBar mProgressBar = findViewById(R.id.progressbar);
            mProgressBar.setVisibility(View.GONE);
            checkIfExistingCustomer (userId);
        } catch (ApiException e) {

            Log.i(TAG, "handleSignInResult Exception" + "LoginActivity");
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);

        }
    }

    /** FUNCTIONS FOR SETTING AND GETTING LOGIN IFNO IN/FROM SHARED PREFS **/
    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static @NonNull String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, "");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop :" + "LoginActivity");
        try {
            if (null != cartRef && null != vel) {
                cartRef.removeEventListener(vel);
            }

            if (null != adminRef && null != admin_vel) {
                adminRef.removeEventListener(admin_vel);
            }
        }catch (Throwable ignore) {

            Log.i(TAG, "onStop Exception " + "LoginActivity");
        }
    }
}

