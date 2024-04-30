package com.test.cnouleg;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public abstract class StepperFragment extends Fragment {
    protected MaterialButton nextButton;
    protected CircularProgressIndicator progressIndicator;

    protected abstract boolean Validate();
}
