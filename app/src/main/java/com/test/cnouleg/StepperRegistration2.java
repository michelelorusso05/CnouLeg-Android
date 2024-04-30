package com.test.cnouleg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class StepperRegistration2 extends StepperFragment {

    public StepperRegistration2() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.stepper_fragment_registration_2, container, false);

        nextButton = view.findViewById(R.id.nextButton);
        nextButton.setOnClickListener((v) -> {
            requireActivity().finish();
        });

        return view;
    }

    @Override
    protected boolean Validate() {
        return true;
    }
}