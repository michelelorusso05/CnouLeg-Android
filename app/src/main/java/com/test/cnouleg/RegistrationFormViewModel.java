package com.test.cnouleg;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RegistrationFormViewModel extends ViewModel {
    private final MutableLiveData<RegistrationFormState> uiState =
            new MutableLiveData<>(new RegistrationFormState());
    public LiveData<RegistrationFormState> getUiState() {
        return uiState;
    }

    public void UpdateFirstStep(String email, String username, String password) {
        RegistrationFormState s = uiState.getValue();
        assert s != null;

        s.email = email;
        s.username = username;
        s.password = password;
        s.step = 1;

        uiState.setValue(s);
    }

    public void UpdateSecondStep(String date, String gender) {
        RegistrationFormState s = uiState.getValue();
        assert s != null;
        assert s.email != null;

        s.birthdate = date;
        s.gender = gender;
        s.step = 2;

        uiState.setValue(s);
    }

    public static class RegistrationFormState {
        public RegistrationFormState() {
            step = 0;
        }
        public String email;
        public String password;
        public String username;
        public String birthdate;
        public String gender;
        public int step;
    }
}
