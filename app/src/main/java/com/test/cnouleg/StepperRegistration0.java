package com.test.cnouleg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.api.EmailValidationResult;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class StepperRegistration0 extends StepperFragment {
    Context context;

    TextInputLayout emailEditText;
    EditText emailEditTextField;
    TextInputLayout usernameEditText;
    EditText usernameEditTextField;
    TextInputLayout passwordEditText;
    EditText passwordEditTextField;

    public StepperRegistration0() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = requireContext();

        View view = inflater.inflate(R.layout.stepper_fragment_registration_0, container, false);

        RegistrationFormViewModel registrationFormViewModel = new ViewModelProvider(requireActivity()).get(RegistrationFormViewModel.class);

        nextButton = view.findViewById(R.id.nextButton);
        progressIndicator = view.findViewById(R.id.formProgressBar);

        emailEditText = view.findViewById(R.id.email_text_field);
        emailEditTextField = emailEditText.getEditText();
        assert emailEditTextField != null;
        emailEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                emailEditText.setError(null);
                emailEditText.setErrorEnabled(false);
            }
        });

        usernameEditText = view.findViewById(R.id.username_text_field);
        usernameEditTextField = usernameEditText.getEditText();
        assert usernameEditTextField != null;
        usernameEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                usernameEditText.setError(null);
                usernameEditText.setErrorEnabled(false);
            }
        });

        passwordEditText = view.findViewById(R.id.password_text_field);
        passwordEditTextField = passwordEditText.getEditText();
        assert passwordEditTextField != null;
        passwordEditTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                passwordEditText.setError(null);
                passwordEditText.setErrorEnabled(false);
            }
        });

        MaterialButton loginButton = view.findViewById(R.id.loginButton);

        loginButton.setOnClickListener((v) -> {
            Intent intent = new Intent(context, ActivityLogin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(intent);
        });

        nextButton.setOnClickListener((v) -> {
            String email = emailEditTextField.getText().toString().trim();
            String username = usernameEditTextField.getText().toString().trim();
            String password = passwordEditTextField.getText().toString().trim();

            if (Validate()) {
                progressIndicator.setVisibility(View.VISIBLE);
                nextButton.setEnabled(false);

                Request.Builder builder = new Request.Builder()
                    .url(SharedUtils.GetServer(context) + "/api/validate_email/?email=" + email)
                    .get();

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
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        boolean exists = StaticData.getMapper().readValue(response.body().bytes(), EmailValidationResult.class).exists();
                        response.body().close();

                        requireActivity().runOnUiThread(() -> {
                            if (!exists)
                                registrationFormViewModel.UpdateFirstStep(email, username, password);
                            else {
                                emailEditText.setError(context.getString(R.string.error_email_already_exists));
                                progressIndicator.setVisibility(View.INVISIBLE);
                                nextButton.setEnabled(true);
                            }
                        });
                    }
                });
            }
        });

        return view;
    }

    @Override
    protected boolean Validate() {
        boolean validate = true;

        String email = emailEditTextField.getText().toString();
        String username = usernameEditTextField.getText().toString();
        String password = passwordEditTextField.getText().toString();

        if (email.trim().isEmpty()) {
            emailEditText.setErrorEnabled(true);
            emailEditText.setError(context.getString(R.string.error_email_empty));
            validate = false;
        }
        else if (!email.matches("^([\\w.\\-]+)@([\\w\\-]+)((\\.(\\w){2,4})+)$")) {
            emailEditText.setErrorEnabled(true);
            emailEditText.setError(context.getString(R.string.error_email_invalid));
            validate = false;
        }

        if (username.trim().isEmpty()) {
            usernameEditText.setErrorEnabled(true);
            usernameEditText.setError(context.getString(R.string.error_username_empty));
            validate = false;
        }

        if (password.trim().isEmpty()) {
            passwordEditText.setErrorEnabled(true);
            passwordEditText.setError(context.getString(R.string.error_password_empty));
            validate = false;
        }

        return validate;
    }
}