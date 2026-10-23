
package com.example.app_ck;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {
    private ThamQuanTuDong thamQuanTuDong;
    private final androidx.activity.result.ActivityResultLauncher<String[]> quyenGpsThamQuan =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        if (thamQuanTuDong != null) thamQuanTuDong.nhanQuyen(Boolean.TRUE.equals(
                                result.get(android.Manifest.permission.ACCESS_FINE_LOCATION)));
                    });

    void xinQuyenGpsThamQuan() {
        quyenGpsThamQuan.launch(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    protected void thongBaoDoiDiemThamQuan() {
        if (thamQuanTuDong != null) thamQuanTuDong.chonThuCong();
    }

    @Override protected void onPostCreate(@Nullable Bundle state) {
        super.onPostCreate(state);
        if (!isFinishing() && this instanceof NguonThamQuanTuDong
                && findViewById(R.id.tqDanhSachKhuVuc) != null) {
            apDungKhoangTrongHeThong();
            thamQuanTuDong = new ThamQuanTuDong(this, (NguonThamQuanTuDong) this);
        }
    }

    @Override protected void onStop() {
        if (thamQuanTuDong != null) thamQuanTuDong.dung("Đã tạm dừng AUTO khi rời màn hình. Bấm Demo hoặc GPS thật để tiếp tục.");
        super.onStop();
    }
    protected void apDungKhoangTrongHeThong() {
        android.view.View noiDung = findViewById(android.R.id.content);
        noiDung.setBackgroundColor(android.graphics.Color.parseColor("#F6F8F5"));
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(noiDung, (view, insets) -> {
            androidx.core.graphics.Insets khoangTrong = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int bottom = khoangTrong.bottom;
            if (findViewById(R.id.navTrangChu) != null) {
                bottom = Math.max(bottom, insets.getInsetsIgnoringVisibility(
                        WindowInsetsCompat.Type.navigationBars()).bottom);
            }
            view.setPadding(khoangTrong.left, khoangTrong.top, khoangTrong.right, bottom);
            return insets;
        });
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        androidx.core.view.ViewCompat.requestApplyInsets(noiDung);
    }

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
