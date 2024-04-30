package com.test.cnouleg;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class ActivityRegistration extends AppCompatActivity {

    ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new StepperRegistration0();
                    case 1:
                        return new StepperRegistration1();
                    case 2:
                        return new StepperRegistration2();
                }

                throw new IllegalStateException();
            }

            @Override
            public int getItemCount() {
                return 3;
            }
        });

        RegistrationFormViewModel registrationFormViewModel = new ViewModelProvider(this).get(RegistrationFormViewModel.class);

        registrationFormViewModel.getUiState().observe(this, registrationFormState ->
                viewPager.setCurrentItem(registrationFormState.step, true));
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