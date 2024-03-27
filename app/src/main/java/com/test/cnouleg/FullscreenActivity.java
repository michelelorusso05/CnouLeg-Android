package com.test.cnouleg;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public abstract class FullscreenActivity extends AppCompatActivity {
    boolean isShowing = true;
    private WindowInsetsControllerCompat windowInsetsController;
    private ActionBar actionBar;

    final Consumer<Boolean> onTouch = (b) -> {
        if (b == null) b = isShowing;

        if (b) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            actionBar.hide();
        }
        else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
            actionBar.show();
        }
        isShowing = !b;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(getLayout());

        windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        );

        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());

        // Set custom toolbar
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        ViewCompat.setOnApplyWindowInsetsListener(t, (v, insets) -> {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) t.getLayoutParams();
            Insets systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            params.setMargins(0, systemBarInsets.top, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @LayoutRes
    protected abstract int getLayout();
}
