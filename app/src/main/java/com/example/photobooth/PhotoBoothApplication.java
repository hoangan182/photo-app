package com.example.photobooth;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class PhotoBoothApplication extends Application {
    private static final String TAG = "PhotoBoothApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        
        // Initialize Firebase App Check
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        );
        
        // Enable debug provider in debug builds
        boolean isDebuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (isDebuggable) {
            firebaseAppCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            );
            Log.d(TAG, "Firebase App Check debug provider installed");
        }
    }
} 