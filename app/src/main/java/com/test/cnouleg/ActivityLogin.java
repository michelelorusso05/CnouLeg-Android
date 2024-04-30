package com.test.cnouleg;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.test.cnouleg.api.LoginResult;
import com.test.cnouleg.utils.AccessTokenUtils;
import com.test.cnouleg.utils.SharedUtils;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityLogin extends AppCompatActivity {

    protected MaterialButton nextButton;
    protected CircularProgressIndicator progressIndicator;
    TextInputLayout emailEditText;
    EditText emailEditTextField;
    TextInputLayout passwordEditText;
    EditText passwordEditTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nextButton = findViewById(R.id.nextButton);
        progressIndicator = findViewById(R.id.formProgressBar);

        emailEditText = findViewById(R.id.email_text_field);
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

        passwordEditText = findViewById(R.id.password_text_field);
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

        findViewById(R.id.registerButton).setOnClickListener((v) -> {
            Intent i = new Intent(this, ActivityRegistration.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
        });

        nextButton.setOnClickListener((v) -> {
            String email = emailEditTextField.getText().toString().trim();
            String password = passwordEditTextField.getText().toString().trim();

            if (Validate()) {
                progressIndicator.setVisibility(View.VISIBLE);
                nextButton.setEnabled(false);

                HashMap<String, String> requestBodyMap = new HashMap<>();

                requestBodyMap.put("email", email);
                requestBodyMap.put("password", password);

                String json;
                try {
                    json = StaticData.getMapper().writeValueAsString(requestBodyMap);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

                Request.Builder builder = new Request.Builder()
                        .url(SharedUtils.GetServer(ActivityLogin.this) + "/api/login/")
                        .post(requestBody);

                StaticData.getClient().newCall(builder.build()).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            progressIndicator.setVisibility(View.GONE);
                            nextButton.setEnabled(true);

                            Snackbar.make(nextButton, "Impossibile raggiungere il server", Snackbar.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.code() == 404) {
                            runOnUiThread(() -> emailEditText.setError("Non esiste un utente con questa mail."));
                        }
                        else if (response.code() == 401) {
                            runOnUiThread(() -> passwordEditText.setError("Password errata."));
                        }
                        else {
                            String body = response.body().string();
                            String token = StaticData.getMapper().readValue(body, LoginResult.class).getToken();

                            AccessTokenUtils.SaveToken(ActivityLogin.this, token);

                            finish();
                        }

                        runOnUiThread(() -> {
                            progressIndicator.setVisibility(View.GONE);
                            nextButton.setEnabled(true);
                        });
                    }
                });
            }
        });
    }
    protected boolean Validate() {
        boolean validate = true;

        String email = emailEditTextField.getText().toString();
        String password = passwordEditTextField.getText().toString();

        if (email.trim().isEmpty()) {
            emailEditText.setErrorEnabled(true);
            emailEditText.setError(getString(R.string.error_email_empty));
            validate = false;
        }
        else if (!email.matches("^([\\w.\\-]+)@([\\w\\-]+)((\\.(\\w){2,4})+)$")) {
            emailEditText.setErrorEnabled(true);
            emailEditText.setError(getString(R.string.error_email_invalid));
            validate = false;
        }

        if (password.trim().isEmpty()) {
            passwordEditText.setErrorEnabled(true);
            passwordEditText.setError(getString(R.string.error_password_empty));
            validate = false;
        }

        return validate;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}