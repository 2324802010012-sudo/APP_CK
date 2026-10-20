
package com.example.app_ck;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        anThanhDieuHuongSamsung();
    }

    @Override
    protected void onResume() {
        super.onResume();

        anThanhDieuHuongSamsung();
    }

    protected void anThanhDieuHuongSamsung() {

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.hide(
                WindowInsetsCompat.Type.navigationBars()
        );

        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }
}
