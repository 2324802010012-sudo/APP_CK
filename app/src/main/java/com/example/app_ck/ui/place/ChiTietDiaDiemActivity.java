package com.example.app_ck.ui.place;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.NguonThongTin;
import com.example.app_ck.ui.tour.DieuHuongThamQuan;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import android.util.Log;

public class ChiTietDiaDiemActivity extends BaseActivity {
    public static final String PHAT_TU_VI_TRI = "com.example.app_ck.PHAT_TU_VI_TRI";
    public static final String CHE_DO_VI_TRI = "com.example.app_ck.CHE_DO_VI_TRI";
    private boolean choPhatTuViTri, dangHienThi;
    private int viTri;

    private TextView nutYeuThich;
    private TextView nutTimYeuThich;
    private final androidx.activity.result.ActivityResultLauncher<Intent> dangNhapDeLuu =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    ketQua -> {
                        if (ketQua.getResultCode() == RESULT_OK && PhienDangNhap.daDangNhap(this)) {
                            try (AppDatabase db = new AppDatabase(this)) {
                                db.datYeuThich(PhienDangNhap.id(this), viTri, true);
                                capNhatYeuThich();
                            } catch (Exception e) {
                                Toast.makeText(this, "Không thể lưu địa điểm. Vui lòng thử lại.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private DiaDiem diaDiem;
    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private int anhDiaDiem = android.R.drawable.ic_menu_gallery;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_chi_tiet_dia_diem);
        apDungKhoangTrongHeThong();

        // Nhận vị trí địa điểm từ màn hình trước
        viTri = getIntent().getIntExtra("viTriDiaDiem", 0);

        if (!taiChiTietTuSQLite()) {
            thongBao("Địa điểm không còn khả dụng.");
            finish();
            return;
        }

        // Ý định chỉ dùng cho lần mở đã được xác nhận, không phát lại khi quay về/xoay màn hình.
        choPhatTuViTri = state == null && getIntent().getBooleanExtra(PHAT_TU_VI_TRI, false);
        getIntent().removeExtra(PHAT_TU_VI_TRI);
        if (choPhatTuViTri) hienTrangThaiDoc("Đã xác nhận theo vị trí · Đang chuẩn bị giọng đọc…");

        findViewById(R.id.nguonChiTiet).setOnClickListener(v -> NguonThongTin.hien(this, viTri, 0));

        hienThiThongTin();

        ((TextView) findViewById(R.id.soLuongAnh))
                .setText("▣  1/1");

        ((TextView) findViewById(R.id.gioMoCua))
                .setText("◷  Thông tin tham quan ↗");

        findViewById(R.id.gioMoCua).setOnClickListener(v -> NguonThongTin.hien(this, viTri, 0));

        // Quay lại
        findViewById(R.id.nutQuayLai)
                .setOnClickListener(v -> finish());

        // Chỉ đường đến địa điểm lớn bằng Google Maps
        findViewById(R.id.nutChiDuong)
                .setOnClickListener(v -> moChiDuong());

        // Xem vị trí trên Google Maps
        // Nút này cần được thêm vào file XML
        android.view.View nutBanDo =
                findViewById(R.id.nutXemBanDo);

        if (nutBanDo != null) {
            nutBanDo.setOnClickListener(v -> moXemBanDo());
        }

        // Tại chi tiết chỉ nghe chủ động; GPS/demo được theo dõi ở trang chủ.
        findViewById(R.id.nutNgheThuyetMinh).setOnClickListener(v -> docThuyetMinh());
        findViewById(R.id.nutPhatLon).setOnClickListener(v -> docThuyetMinh());
        findViewById(R.id.nutDungThuyetMinh).setOnClickListener(v -> dungThuyetMinh());

        // Bắt đầu tham quan các địa điểm nhỏ
        findViewById(R.id.nutBatDauThamQuan)
                .setOnClickListener(v ->
                        moManHinhThamQuan(0)
                );

        // Xem tất cả điểm tham quan
        findViewById(R.id.nutXemTatCaDiem)
                .setOnClickListener(v ->
                        moManHinhThamQuan(0)
                );

        // Thư viện ảnh
        findViewById(R.id.nutThuVien)
                .setOnClickListener(v -> moThuVien());

        findViewById(R.id.anhChiTiet)
                .setOnClickListener(v -> moThuVien());

        // Xem thêm / Thu gọn mô tả
        TextView moTa = findViewById(R.id.moTaChiTiet);

        findViewById(R.id.nutXemThem)
                .setOnClickListener(v -> {
                    boolean dangRutGon =
                            moTa.getMaxLines() == 3;

                    moTa.setMaxLines(
                            dangRutGon ? Integer.MAX_VALUE : 3
                    );

                    ((TextView) v).setText(
                            dangRutGon
                                    ? "Thu gọn  ⌃"
                                    : "Xem thêm  ⌄"
                    );
                });

        // Chức năng yêu thích
        nutYeuThich =
                findViewById(R.id.nutLuuYeuThich);

        nutTimYeuThich =
                findViewById(R.id.nutTimYeuThich);

        capNhatYeuThich();

        nutYeuThich.setOnClickListener(v ->
                doiYeuThich()
        );

        nutTimYeuThich.setOnClickListener(v ->
                doiYeuThich()
        );

        // Tạo danh sách các điểm tham quan nổi bật
        taoTheDiem();

    }

    private boolean taiChiTietTuSQLite() {
        try (AppDatabase helper = new AppDatabase(this)) {
            diaDiem = null;
            for (DiaDiem d : helper.danhSachDiaDiem()) {
                if (d.id == viTri) { diaDiem = d; break; }
            }
            if (diaDiem == null) return false;
            cacDiem.clear();
            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(helper.getReadableDatabase(), viTri));
            int resource = getResources().getIdentifier(diaDiem.tenAnh == null ? "" : diaDiem.tenAnh,
                    "drawable", getPackageName());
            anhDiaDiem = resource == 0 ? android.R.drawable.ic_menu_gallery : resource;
            return true;
        } catch (Exception e) {
            Log.e("ChiTietDiaDiem", "Không đọc được SQLite", e);
            return false;
        }
    }

    private void hienThiThongTin() {
        ((ImageView) findViewById(R.id.anhChiTiet)).setImageResource(anhDiaDiem);
        ((TextView) findViewById(R.id.tenChiTiet)).setText(diaDiem.ten);
        ((TextView) findViewById(R.id.danhMucChiTiet)).setText(diaDiem.danhMuc);
        ((TextView) findViewById(R.id.diaChiChiTiet)).setText(
                "⌖  " + (diaDiem.diaChi == null ? "Chưa bổ sung địa chỉ" : diaDiem.diaChi));
        ((TextView) findViewById(R.id.moTaChiTiet)).setText(diaDiem.moTa);
    }

    // Hiển thị thông báo
    private void thongBao(String noiDung) {
        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    // Xem vị trí của địa điểm trên Google Maps
    private void moXemBanDo() {

        String toaDo =
                diaDiem.viDo + "," +
                        diaDiem.kinhDo;

        Uri uri = Uri.parse(
                "https://www.google.com/maps/search/" +
                        "?api=1&query=" + toaDo
        );

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri
        );

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            thongBao("Không thể mở bản đồ.");
        }
    }

    // Chỉ đường từ vị trí hiện tại đến địa điểm
    private void moChiDuong() {

        String toaDo =
                diaDiem.viDo + "," +
                        diaDiem.kinhDo;

        Uri uri = Uri.parse(
                "https://www.google.com/maps/dir/" +
                        "?api=1&destination=" + toaDo
        );

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri
        );

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            thongBao(
                    "Không tìm thấy ứng dụng mở bản đồ."
            );
        }
    }

    private void hienTrangThaiDoc(String text) {
        TextView status = findViewById(R.id.trangThaiThuyetMinh);
        status.setVisibility(android.view.View.VISIBLE);
        status.setText(text);
    }

    private void phatSauXacNhanViTri() {
        if (!choPhatTuViTri || !dangHienThi || isFinishing() || isDestroyed()) return;
        choPhatTuViTri = false;
        docThuyetMinh(KhoLichSu.DEMO.equals(getIntent().getStringExtra(CHE_DO_VI_TRI))
                ? KhoLichSu.DEMO : KhoLichSu.AUTO_MAIN);
    }

    private void docThuyetMinh() {
        docThuyetMinh(KhoLichSu.THU_CONG);
    }

    private void docThuyetMinh(String cheDo) {
        choPhatTuViTri = false;
        moTrinhNghe(true, cheDo);
    }

    @Override public void moKhungThuyetMinh(boolean phatNgay) {
        choPhatTuViTri = false;
        moTrinhNghe(phatNgay, KhoLichSu.THU_CONG);
    }

    private void moTrinhNghe(boolean phatNgay, String cheDo) {
        if (!taiChiTietTuSQLite()) {
            thongBao("Địa điểm không còn khả dụng.");
            finish();
            return;
        }
        hienThiThongTin();
        moKhungThuyetMinh(diaDiem.ten, diaDiem.moTa, diaDiem.ten,
                ((ImageView) findViewById(R.id.anhChiTiet)).getDrawable(),
                phatNgay, viTri, 0, cheDo);
        findViewById(R.id.trangThaiThuyetMinh).setVisibility(android.view.View.GONE);
    }

    private void dungThuyetMinh() {
        huyLuotLichSu();
        choPhatTuViTri = false;
        dongKhungThuyetMinh();
        TextView status = findViewById(R.id.trangThaiThuyetMinh);
        if (status.getVisibility() == android.view.View.VISIBLE) status.setText("Đã dừng thuyết minh.");
    }

    // Tạo các thẻ điểm tham quan nổi bật
    private void taoTheDiem() {

        LinearLayout ds =
                findViewById(R.id.danhSachDiemNoiBat);

        ds.removeAllViews();

        if (cacDiem.isEmpty()) {
            TextView trong = new TextView(this);
            trong.setText("Các điểm tham quan đang được bổ sung.");
            trong.setPadding(dp(10), dp(16), dp(10), dp(16));
            ds.addView(trong);
        }

        for (DiemThamQuan diem : cacDiem) {

            final int idDiem = diem.id;

            LinearLayout the = new LinearLayout(this);
            the.setOrientation(LinearLayout.VERTICAL);

            the.setPadding(
                    dp(10),
                    dp(10),
                    dp(10),
                    dp(10)
            );

            the.setBackgroundResource(
                    R.drawable.nen_tien_ich_xanh
            );

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            dp(170),
                            dp(158)
                    );

            lp.setMargins(0, 0, dp(10), 0);
            ds.addView(the, lp);

            // Ảnh của điểm tham quan
            ImageView anh = new ImageView(this);

            int anhDiem = getResources().getIdentifier(diem.tenAnh == null ? "" : diem.tenAnh,
                    "drawable", getPackageName());
            anh.setImageResource(anhDiem == 0 ? anhDiaDiem : anhDiem);
            anh.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            the.addView(
                    anh,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(82)
                    )
            );

            // Tên điểm tham quan
            TextView ten = new TextView(this);

            ten.setText(diem.ten);
            ten.setTextColor(
                    Color.parseColor("#123247")
            );

            ten.setTextSize(13);
            ten.setTypeface(null, Typeface.BOLD);
            ten.setMaxLines(2);

            the.addView(ten);

            // Nút nghe giới thiệu
            TextView nghe = new TextView(this);

            nghe.setText("▶  Nghe giới thiệu");
            nghe.setTextColor(
                    Color.parseColor("#008E6C")
            );

            nghe.setTextSize(12);
            nghe.setGravity(Gravity.CENTER_VERTICAL);

            the.addView(nghe);

            // Chọn điểm thì mở màn hình tham quan
            the.setOnClickListener(v -> {
                moManHinhThamQuan(idDiem);
            });
        }
    }

    // Chuyển dp sang pixel
    private int dp(int value) {
        return (int) (
                value *
                        getResources().getDisplayMetrics().density
                        + 0.5f
        );
    }

    private void moManHinhThamQuan(int idDiem) {
        dungThuyetMinh();
        startActivity(DieuHuongThamQuan.taoIntent(this, viTri, idDiem));
    }

    // Hiển thị thư viện ảnh
    private void moThuVien() {

        ImageView anh = new ImageView(this);
        anh.setImageResource(anhDiaDiem);
        anh.setAdjustViewBounds(true);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Hình ảnh – " + diaDiem.ten)
                .setView(anh)
                .setMessage(
                        "Hiện có 1 ảnh địa điểm. " +
                                "Chưa bổ sung phim tư liệu."
                )
                .setPositiveButton("Đóng", null)
                .show();
    }

    // Cập nhật giao diện yêu thích
    @Override
    protected void onResume() {
        super.onResume();
        if (isFinishing()) return;
        if (!taiChiTietTuSQLite()) {
            thongBao("Địa điểm không còn khả dụng.");
            finish();
            return;
        }
        hienThiThongTin();
        taoTheDiem();
        dangHienThi = true;
        phatSauXacNhanViTri();

        if (nutYeuThich != null) {
            capNhatYeuThich();
        }
    }

    private void capNhatYeuThich() {

        boolean daLuu = false;

        if (PhienDangNhap.daDangNhap(this)) {

            try (AppDatabase db = new AppDatabase(this)) {

                daLuu = db.daYeuThich(
                        PhienDangNhap.id(this),
                        viTri
                );
            }
        }

        nutYeuThich.setText(
                daLuu ? "♥\nĐã thích" : "♡\nYêu thích"
        );

        nutTimYeuThich.setText(
                daLuu ? "♥" : "♡"
        );
    }

    // Thêm hoặc xóa điểm yêu thích
    private void doiYeuThich() {

        if (!PhienDangNhap.daDangNhap(this)) {

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Yêu cầu đăng nhập")
                    .setMessage(
                            "Bạn cần đăng nhập để thêm hoặc " +
                                    "xóa địa điểm yêu thích."
                    )
                    .setPositiveButton(
                            "Đăng nhập",
                            (d, w) -> dangNhapDeLuu.launch(
                                    new Intent(
                                            this,
                                            DangNhapActivity.class
                                    )
                            )
                    )
                    .setNegativeButton("Để sau", null)
                    .show();

            return;
        }

        boolean daLuu;

        try (AppDatabase db = new AppDatabase(this)) {

            long id = PhienDangNhap.id(this);

            daLuu = db.daYeuThich(
                    id,
                    viTri
            );

            db.datYeuThich(
                    id,
                    viTri,
                    !daLuu
            );
        }

        capNhatYeuThich();

        thongBao(
                daLuu
                        ? "Đã bỏ yêu thích"
                        : "Đã thêm vào yêu thích"
        );
    }

    @Override
    protected void onStop() {
        dangHienThi = false;
        dungThuyetMinh();
        super.onStop();
    }

}
