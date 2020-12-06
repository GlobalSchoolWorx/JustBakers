package com.ecom.justbakers.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecom.justbakers.LoginActivity;
import com.ecom.justbakers.R;
import com.firebase.client.Firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContactsFragment extends Fragment {
    private static final String TAG = "ContactFragment";

    public ContactsFragment() {}

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Firebase.setAndroidContext(requireActivity());
        if(!LoginActivity.isUserLoggedIn(requireActivity())) {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            this.startActivity(intent);
        }
    }
}
