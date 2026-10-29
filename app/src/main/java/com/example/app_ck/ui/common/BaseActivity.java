
package com.example.app_ck.ui.common;

import com.example.app_ck.R;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.location.ThamQuanTuDong;
import com.example.app_ck.narration.KhungNgheThuyetMinh;
import com.example.app_ck.narration.LuotNgheLichSu;
import com.example.app_ck.ui.home.MainActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {
    private KhungNgheThuyetMinh khungNghe;
    private androidx.appcompat.app.AlertDialog hoiChuyenDiem;
    private int diaDiemNguon = -1, diemNguon;
    private String maLuotLichSu;
    private LuotNgheLichSu luotLichSu;

    public void chuanBiLichSu(String ma, int diaDiem, int diem, String cheDo) {
        maLuotLichSu = ma;
        luotLichSu = new LuotNgheLichSu(this, diaDiem, diem, cheDo);
    }

    public void batDauLichSu(String ma) {
        runOnUiThread(() -> {
            if (ma != null && ma.equals(maLuotLichSu) && luotLichSu != null
                    && getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))
                luotLichSu.batDau(this);
        });
    }

    public void huyLuotLichSu() { maLuotLichSu = null; luotLichSu = null; }

    protected void ganNguonThongTin(int diaDiem, int diem) {
        diaDiemNguon = diaDiem;
        diemNguon = diem;
        android.view.View nut = findViewById(R.id.tqNguonThongTin);
        if (nut != null) {
            nut.setVisibility(android.view.View.VISIBLE);
            nut.setOnClickListener(v -> moNguonThongTin());
        }
    }

    public void moNguonThongTin() {
        if (diaDiemNguon >= 0) NguonThongTin.hien(this, diaDiemNguon, diemNguon);
    }
    /** Màn hình con dừng AUTO/TTS trước khi mở trình nghe thủ công. */
    protected void dungAmThanhThamQuan() {}

    public void moKhungThuyetMinh(boolean phatNgay) {
        android.widget.TextView ten = findViewById(R.id.tqTenKhuVuc);
        android.widget.TextView bai = findViewById(R.id.tqMoTaKhuVuc);
        android.widget.TextView diaDiem = findViewById(R.id.tqTenDiaDiem);
        android.widget.ImageView anh = findViewById(R.id.tqAnhDiaDiem);
        if (ten == null || bai == null || isFinishing()) return;
        moKhungThuyetMinh(ten.getText().toString(), bai.getText().toString(),
                diaDiem.getText().toString(), anh.getDrawable(), phatNgay,
                diaDiemNguon, diemNguon, KhoLichSu.THU_CONG);
    }

    public void moKhungThuyetMinh(String ten, String bai, String diaDiem,
            android.graphics.drawable.Drawable anh, boolean phatNgay, int idDiaDiem, int idDiem, String cheDo) {
        if (isFinishing() || isDestroyed()) return;
        dungAmThanhThamQuan();
        ganNguonThongTin(idDiaDiem, idDiem);
        if (khungNghe == null) khungNghe = new KhungNgheThuyetMinh(this);
        khungNghe.mo(ten, bai, diaDiem, anh, phatNgay, idDiaDiem, idDiem, cheDo);
    }

    public void dongKhungThuyetMinh() { if (khungNghe != null) khungNghe.dong(); }

    protected void xacNhanChuyenDiem(boolean autoDangPhat, String tenDiem, Runnable chuyenDiem) {
        if (isFinishing() || isDestroyed()) return;
        if (hoiChuyenDiem != null && hoiChuyenDiem.isShowing()) return;
        if (!autoDangPhat) {
            chuyenDiem.run();
            return;
        }
        hoiChuyenDiem = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Chuyển điểm tham quan?")
                .setMessage("AUTO đang phát thuyết minh. Bạn có muốn dừng AUTO và chuyển sang điểm “"
                        + tenDiem + "” để nghe thủ công không?")
                .setNegativeButton("Tiếp tục nghe", null)
                .setPositiveButton("Chuyển điểm", (dialog, which) -> {
                    if (!isFinishing() && !isDestroyed()
                            && getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))
                        chuyenDiem.run();
                }).create();
        androidx.appcompat.app.AlertDialog dangHoi = hoiChuyenDiem;
        dangHoi.setOnDismissListener(dialog -> {
            if (hoiChuyenDiem == dangHoi) hoiChuyenDiem = null;
        });
        hoiChuyenDiem.show();
    }

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

    public void xinQuyenGpsThamQuan() {
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

    public void dungHuongDanTheoViTri() {
        if (thamQuanTuDong != null) thamQuanTuDong.dung("Đã dừng hướng dẫn theo vị trí để nghe thủ công.");
    }

    @Override protected void onStop() {
        if (hoiChuyenDiem != null) hoiChuyenDiem.dismiss();
        huyLuotLichSu();
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
