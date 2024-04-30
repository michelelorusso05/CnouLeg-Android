package com.test.cnouleg;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainActivityFragmentAdapter extends FragmentStateAdapter {
    public MainActivityFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FragmentSearch();
            case 1:
                return new FragmentProfile();
        }
        throw new IllegalStateException("This ViewPager only has " + getItemCount() + " fragments");
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
