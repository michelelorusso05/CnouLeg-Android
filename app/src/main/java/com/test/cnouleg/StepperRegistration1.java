package com.test.cnouleg;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StepperRegistration1 extends StepperFragment {
    Context context;

    TextInputLayout birthdateEditText;
    EditText birthdateEditTextField;
    TextInputLayout genderEditText;
    MaterialAutoCompleteTextView genderEditTextField;

    private static final String[] genders = {
        "male",
        "female",
        "other"
    };

    String birthdate = null;
    String gender = null;

    public StepperRegistration1() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = requireContext();

        View view = inflater.inflate(R.layout.stepper_fragment_registration_1, container, false);

        RegistrationFormViewModel registrationFormViewModel = new ViewModelProvider(requireActivity()).get(RegistrationFormViewModel.class);

        nextButton = view.findViewById(R.id.nextButton);
        progressIndicator = view.findViewById(R.id.formProgressBar);

        birthdateEditText = view.findViewById(R.id.birthdate_text_field);
        birthdateEditTextField = birthdateEditText.getEditText();
        assert birthdateEditTextField != null;

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.form_birth_date)
                .build();

        datePicker.addOnPositiveButtonClickListener(date -> {
            birthdateEditTextField.setText(SharedUtils.FormatDateLocale(context, date));
            birthdate = SharedUtils.FormatDateApi(date);
        });

        birthdateEditTextField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                datePicker.show(getParentFragmentManager(), "birthday");
                birthdateEditTextField.clearFocus();
            }
        });

        birthdateEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                birthdateEditText.setError(null);
                birthdateEditText.setErrorEnabled(false);
            }
        });

        genderEditText = view.findViewById(R.id.gender_text_field);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.dropdown_item, context.getResources().getStringArray(R.array.genders));

        genderEditTextField = view.findViewById(R.id.genderEditText);
        genderEditTextField.setAdapter(adapter);
        genderEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                genderEditText.setError(null);
                genderEditText.setErrorEnabled(false);
            }
        });

        genderEditTextField.setOnItemClickListener((parent, view1, position, id) -> gender = genders[position]);

        nextButton.setOnClickListener((v) -> {
            final String f_birthdate = birthdate;
            final String f_gender = gender;

            if (Validate()) {
                progressIndicator.setVisibility(View.VISIBLE);
                nextButton.setEnabled(false);

                RegistrationFormViewModel.RegistrationFormState state = registrationFormViewModel.getUiState().getValue();
                assert state != null;

                HashMap<String, String> requestBodyMap = new HashMap<>();

                requestBodyMap.put("email", state.email);
                requestBodyMap.put("password", state.password);
                requestBodyMap.put("username", state.username);
                requestBodyMap.put("birthdate", f_birthdate);
                requestBodyMap.put("gender", f_gender);
                requestBodyMap.put("profile_pic_url", "");

                String json;
                try {
                    json = StaticData.getMapper().writeValueAsString(requestBodyMap);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

                Request.Builder builder = new Request.Builder()
                        .url(SharedUtils.GetServer(context) + "/api/user/")
                        .post(requestBody);

                StaticData.getClient().newCall(builder.build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        requireActivity().runOnUiThread(() -> {
                            progressIndicator.setVisibility(View.GONE);
                            nextButton.setEnabled(true);

                            Snackbar.make(nextButton, R.string.error_generic_server, Snackbar.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        requireActivity().runOnUiThread(() ->
                                registrationFormViewModel.UpdateSecondStep(f_birthdate, f_gender));
                    }
                });
            }
        });

        return view;
    }

    @Override
    protected boolean Validate() {
        boolean validate = true;

        if (birthdate == null || birthdate.trim().isEmpty()) {
            birthdateEditText.setError(context.getString(R.string.error_birthdate_empty));
            validate = false;
        }
        if (gender == null || gender.trim().isEmpty()) {
            genderEditText.setError(context.getString(R.string.error_gender_empty));
            validate = false;
        }

        return validate;
    }
}