
package com.example.app_ck;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {
    private KhungNgheThuyetMinh khungNghe;

    /** Màn hình con dừng AUTO/TTS trước khi mở trình nghe thủ công. */
    protected void dungAmThanhThamQuan() {}

    protected void moKhungThuyetMinh(boolean phatNgay) {
        android.widget.TextView ten = findViewById(R.id.tqTenKhuVuc);
        android.widget.TextView bai = findViewById(R.id.tqMoTaKhuVuc);
        android.widget.TextView diaDiem = findViewById(R.id.tqTenDiaDiem);
        android.widget.ImageView anh = findViewById(R.id.tqAnhDiaDiem);
        if (ten == null || bai == null || isFinishing()) return;
        dungAmThanhThamQuan();
        if (khungNghe == null) khungNghe = new KhungNgheThuyetMinh(this);
        khungNghe.mo(ten.getText().toString(), bai.getText().toString(),
                diaDiem.getText().toString(), anh.getDrawable(), phatNgay);
    }

    protected void dongKhungThuyetMinh() { if (khungNghe != null) khungNghe.dong(); }

    @Override protected void onDestroy() {
        if (khungNghe != null) khungNghe.giaiPhong();
        super.onDestroy();
    }

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

    @Override protected void onPostCreate(@Nullable Bundle state) {
        super.onPostCreate(state);
        android.view.View moKhung = findViewById(R.id.tqNutPhat);
        if (moKhung != null) {
            moKhung.setOnClickListener(v -> moKhungThuyetMinh(true));
            apDungKhoangTrongHeThong();
        }
        if (!isFinishing() && this instanceof MainActivity
                && findViewById(R.id.khungThuyetMinhTuDong) != null) {
            apDungKhoangTrongHeThong();
            thamQuanTuDong = new ThamQuanTuDong((MainActivity) this);
            if (state == null && getIntent().getBooleanExtra(MainActivity.BAT_DAU_HUONG_DAN, false)) {
                getIntent().removeExtra(MainActivity.BAT_DAU_HUONG_DAN);
                thamQuanTuDong.batGps();
            }
        }
    }

    protected void dungHuongDanTheoViTri() {
        if (thamQuanTuDong != null) thamQuanTuDong.dung("Đã dừng hướng dẫn theo vị trí để nghe thủ công.");
    }

    @Override protected void onStop() {
        dongKhungThuyetMinh();
        if (thamQuanTuDong != null) thamQuanTuDong.dung("Đã dừng theo dõi và giọng đọc khi rời màn hình. Bấm Demo hoặc GPS thật để bắt đầu lượt mới.");
        super.onStop();
    }
    protected void apDungKhoangTrongHeThong() {
        android.view.View noiDung = findViewById(android.R.id.content);
        noiDung.setBackgroundColor(android.graphics.Color.parseColor("#F6F8F5"));
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(noiDung, (view, insets) -> {
            androidx.core.graphics.Insets khoangTrong = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int bottom = khoangTrong.bottom;
            if (findViewById(R.id.navTrangChu) != null || this instanceof ChiTietDiaDiemActivity) {
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
