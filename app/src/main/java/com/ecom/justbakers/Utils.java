package com.ecom.justbakers;

import android.util.Log;

import com.firebase.client.Firebase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    private static final String TAG = "Utils";

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

    public static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte [] messageDigest = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "MD5 failed! ", e);
        }
        return "";
    }
}
