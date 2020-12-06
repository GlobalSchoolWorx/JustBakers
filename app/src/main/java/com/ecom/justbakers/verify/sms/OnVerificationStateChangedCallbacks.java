package com.ecom.justbakers.verify.sms;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

public class OnVerificationStateChangedCallbacks extends PhoneAuthProvider.OnVerificationStateChangedCallbacks{
    private static OnVerificationStateChangedCallbacks callbacks = null;

    public static OnVerificationStateChangedCallbacks getCallbacks(LifecycleOwner lifecycleOwner, OnSMSVerificationStateChangedListener listener){
        if(null == callbacks) {
            callbacks = new OnVerificationStateChangedCallbacks();
        }
        callbacks.listenersData.add(new ListenerData(lifecycleOwner, listener));

        return callbacks;
    }

    private final List<ListenerData> listenersData = new ArrayList<>();

    @Override
    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        for(ListenerData listenerData : listenersData){
            try {
                LifecycleOwner lifecycleOwner = listenerData.lifecycleOwnerWeakReference.get();
                if(null != lifecycleOwner && lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
                    listenerData.listener.onVerificationCompleted(phoneAuthCredential);
                }
            }catch(Throwable ignored){}
        }
    }

    @Override
    public void onVerificationFailed(@NonNull FirebaseException e) {
        for(ListenerData listenerData : listenersData){
            try {
                LifecycleOwner lifecycleOwner = listenerData.lifecycleOwnerWeakReference.get();
                if(null != lifecycleOwner && lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
                    listenerData.listener.onVerificationFailed(e);
                }
            }catch(Throwable ignored){}
        }
    }

    @Override
    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        for(ListenerData listenerData : listenersData){
            try {
                LifecycleOwner lifecycleOwner = listenerData.lifecycleOwnerWeakReference.get();
                if(null != lifecycleOwner && lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.CREATED)) {
                    listenerData.listener.onCodeSent(s, forceResendingToken);
                }
            }catch(Throwable ignored){}
        }
    }

    public interface OnSMSVerificationStateChangedListener{
        void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential);
        void onVerificationFailed(@NonNull FirebaseException e);
        void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token);
    }

    private static class ListenerData{
        WeakReference<LifecycleOwner> lifecycleOwnerWeakReference;
        OnSMSVerificationStateChangedListener listener;

        public ListenerData(LifecycleOwner lifecycleOwner, OnSMSVerificationStateChangedListener listener) {
            this.lifecycleOwnerWeakReference = new WeakReference<>(lifecycleOwner);
            this.listener = listener;
        }
    }
}
