package com.ecom.justbakers;

import com.firebase.client.Firebase;

public class JustBakersApplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);
    }
}
