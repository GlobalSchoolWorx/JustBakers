package com.ecom.justbakers.verify.sms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.ecom.justbakers.LaunchActivity;
import com.ecom.justbakers.R;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyOtpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        final AutoCompleteTextView av = findViewById(R.id.otpinput);


        info.hoang8f.widget.FButton submitButton = findViewById(R.id.submitbutton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp_sent = getIntent().getStringExtra("VERIFICATION_CODE");
                if( verifyOtp(av.getText().toString(), otp_sent)){
                   startActivity(new Intent(getApplicationContext(), LaunchActivity.class));
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid OTP. Please try again.",
                            Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    /** FUNCTON TO VERIFY OTP  **/
    boolean verifyOtp ( String phoneNumber, String otpSent) {
        boolean verified = false;
         if(phoneNumber.equals(otpSent)) {
             verified = true;
         }
         return verified;
    }



}
