package com.example.app_ck.ui.place;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoDiaDiem;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.NguonThongTin;
import com.example.app_ck.ui.tour.DieuHuongThamQuan;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class ChiTietDiaDiemActivity extends BaseActivity {

    // =====================================================
    // KHAI BÁO
    // =====================================================

    public static final String PHAT_TU_VI_TRI =
            "com.example.app_ck.PHAT_TU_VI_TRI";

    public static final String CHE_DO_VI_TRI =
            "com.example.app_ck.CHE_DO_VI_TRI";

    private static final String TAG = "ChiTietDiaDiem";

    private static final int MA_DANG_NHAP_YEU_THICH = 1001;


    private static final int MAU_CHU_DAO =
            Color.parseColor("#2C7351");

    private static final int MAU_TIEU_DE =
            Color.parseColor("#182D22");

    private static final int MAU_CHU_PHU =
            Color.parseColor("#687B6E");

    private static final int MAU_YEU_THICH =
            Color.parseColor("#D84D5B");

    private int viTri;

    private boolean choPhatTuViTri;
    private boolean dangHienThi;

    private DiaDiem diaDiem;

    private final ArrayList<DiemThamQuan> cacDiem =
            new ArrayList<>();

    private TextView nutYeuThich;
    private TextView nutTimYeuThich;

    // =====================================================
    // DỮ LIỆU SLIDER ẢNH
    // =====================================================

    private BoAnhDiaDiem boAnh;

    // =====================================================
    // KHỞI TẠO MÀN HÌNH
    // =====================================================

    @Override
    protected void onCreate(Bundle state) {

        super.onCreate(state);

        setContentView(
                R.layout.activity_chi_tiet_dia_diem
        );

        apDungKhoangTrongHeThong();

        viTri = getIntent().getIntExtra(
                "viTriDiaDiem",
                0
        );

        if (!taiChiTietTuSQLite()) {

            thongBao("Địa điểm không còn khả dụng.");

            finish();

            return;
        }

        choPhatTuViTri =
                state == null
                        && getIntent().getBooleanExtra(
                        PHAT_TU_VI_TRI,
                        false
                );

        getIntent().removeExtra(PHAT_TU_VI_TRI);

        anhXaGiaoDien();

        ganSuKien();

        if (choPhatTuViTri) {

            hienTrangThaiDoc(
                    "Đang chuẩn bị thuyết minh theo vị trí…"
            );
        }
    }

    private void anhXaGiaoDien() {

        boAnh = new BoAnhDiaDiem(this);

        nutYeuThich =
                findViewById(R.id.nutLuuYeuThich);

        nutTimYeuThich =
                findViewById(R.id.nutTimYeuThich);
    }

    // =====================================================
    // SỰ KIỆN GIAO DIỆN
    // =====================================================

    private void ganSuKien() {

        findViewById(R.id.nutQuayLai)
                .setOnClickListener(v -> finish());

        findViewById(R.id.nutXemBanDo)
                .setOnClickListener(v -> moXemBanDo());

        findViewById(R.id.nutChiDuong)
                .setOnClickListener(v -> moChiDuong());

        findViewById(R.id.gioMoCua)
                .setOnClickListener(v ->
                        NguonThongTin.hien(this, viTri, 0)
                );

        findViewById(R.id.nguonChiTiet)
                .setOnClickListener(v ->
                        NguonThongTin.hien(this, viTri, 0)
                );

        findViewById(R.id.nutNgheThuyetMinh)
                .setOnClickListener(v -> docThuyetMinh());

        findViewById(R.id.nutPhatLon)
                .setOnClickListener(v -> docThuyetMinh());

        findViewById(R.id.nutDungThuyetMinh)
                .setOnClickListener(v -> dungThuyetMinh());

        findViewById(R.id.nutBatDauThamQuan)
                .setOnClickListener(v ->
                        moManHinhThamQuan(0)
                );

        findViewById(R.id.nutXemTatCaDiem)
                .setOnClickListener(v ->
                        moManHinhThamQuan(0)
                );

        nutYeuThich.setOnClickListener(v -> doiYeuThich());

        nutTimYeuThich.setOnClickListener(v -> doiYeuThich());

        // Đọc thêm / Thu gọn

        TextView moTa =
                findViewById(R.id.moTaChiTiet);

        findViewById(R.id.nutXemThem)
                .setOnClickListener(v -> {

                    boolean dangRutGon =
                            moTa.getMaxLines() == 3;

                    moTa.setMaxLines(
                            dangRutGon
                                    ? Integer.MAX_VALUE
                                    : 3
                    );

                    moTa.setEllipsize(
                            dangRutGon
                                    ? null
                                    : TextUtils.TruncateAt.END
                    );

                    ((TextView) v).setText(
                            dangRutGon
                                    ? "Thu gọn  ↑"
                                    : "Đọc thêm  ↓"
                    );

                    capNhatNutDocThem();
                });
    }

    // =====================================================
    // ĐỌC DỮ LIỆU SQLITE
    // =====================================================

    private boolean taiChiTietTuSQLite() {

        try (AppDatabase helper = new AppDatabase(this)) {

            diaDiem = KhoDiaDiem.lay(helper.getReadableDatabase(), viTri);

            if (diaDiem == null) {
                return false;
            }

            cacDiem.clear();

            cacDiem.addAll(
                    KhoDuLieuThamQuan.danhSach(
                            helper.getReadableDatabase(),
                            viTri
                    )
            );

            return true;

        } catch (Exception e) {

            Log.e(TAG, "Không đọc được SQLite", e);

            return false;
        }
    }

    // =====================================================
    // HIỂN THỊ THÔNG TIN ĐỊA ĐIỂM
    // =====================================================

    private void hienThiThongTin() {

        if (diaDiem == null) {
            return;
        }

        TextView ten =
                findViewById(R.id.tenChiTiet);

        TextView danhMuc =
                findViewById(R.id.danhMucChiTiet);

        TextView diaChi =
                findViewById(R.id.diaChiChiTiet);

        TextView moTa =
                findViewById(R.id.moTaChiTiet);

        TextView soDiem =
                findViewById(R.id.ctSoDiemThamQuan);

        ten.setText(diaDiem.ten);

        danhMuc.setText(
                TextUtils.isEmpty(diaDiem.danhMuc)
                        ? "ĐIỂM DU LỊCH"
                        : diaDiem.danhMuc.toUpperCase(
                        new java.util.Locale("vi", "VN")
                )
        );

        // Icon vị trí đã được khai báo trong XML.
        // Không chèn thêm ký tự vị trí vào chuỗi.

        diaChi.setText(
                TextUtils.isEmpty(diaDiem.diaChi)
                        ? "Chưa bổ sung địa chỉ"
                        : diaDiem.diaChi
        );

        moTa.setText(
                TextUtils.isEmpty(diaDiem.moTa)
                        ? "Nội dung đang được cập nhật."
                        : diaDiem.moTa
        );

        soDiem.setText(
                cacDiem.size() + " điểm tham quan"
        );

        boAnh.capNhat(diaDiem, cacDiem);

        capNhatNutDocThem();
    }

    private void capNhatNutDocThem() {

        TextView moTa =
                findViewById(R.id.moTaChiTiet);

        TextView nut =
                findViewById(R.id.nutXemThem);

        moTa.post(() -> {

            boolean dangMoRong =
                    moTa.getMaxLines() == Integer.MAX_VALUE;

            boolean canDocThem = dangMoRong;

            if (moTa.getLayout() != null) {

                int soDong =
                        moTa.getLayout().getLineCount();

                if (soDong > 0) {

                    canDocThem = canDocThem
                            || moTa.getLayout()
                            .getEllipsisCount(soDong - 1) > 0;
                }
            }

            nut.setVisibility(
                    canDocThem
                            ? View.VISIBLE
                            : View.GONE
            );
        });
    }

    // =====================================================
    // DANH SÁCH ĐIỂM THAM QUAN
    // =====================================================

    private void taoTheDiem() {

        LinearLayout ds =
                findViewById(R.id.danhSachDiemNoiBat);

        ds.removeAllViews();

        if (cacDiem.isEmpty()) {

            TextView trong = new TextView(this);

            trong.setText(
                    "Các điểm tham quan đang được bổ sung."
            );

            trong.setTextSize(14);

            trong.setTextColor(MAU_CHU_PHU);

            trong.setPadding(
                    0, dp(12),
                    0, dp(12)
            );

            ds.addView(trong);

            return;
        }

        for (DiemThamQuan diem : cacDiem) {

            ds.addView(
                    taoTheDiemThamQuan(diem)
            );
        }
    }

    // =====================================================
    // THẺ ĐIỂM THAM QUAN TỐI GIẢN
    // =====================================================

    private LinearLayout taoTheDiemThamQuan(
            DiemThamQuan diem
    ) {

        LinearLayout the = new LinearLayout(this);

        the.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        dp(224),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        lp.setMarginEnd(dp(13));

        the.setLayoutParams(lp);

        // ẢNH ĐIỂM THAM QUAN

        ImageView anh = new ImageView(this);

        anh.setBackgroundResource(
                R.drawable.ui_ct_thumb
        );

        anh.setClipToOutline(true);

        anh.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        Bitmap bitmap =
                boAnh.docAnhChiTiet(
                        diem.tenAnh,
                        640
                );

        if (bitmap != null) {

            anh.setImageBitmap(bitmap);

        } else {

            anh.setScaleType(
                    ImageView.ScaleType.CENTER_INSIDE
            );

            anh.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        }

        anh.setImportantForAccessibility(
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        the.addView(
                anh,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(112)
                )
        );

        // TÊN ĐIỂM

        TextView ten = new TextView(this);

        ten.setText(diem.ten);

        ten.setTextColor(MAU_TIEU_DE);

        ten.setTextSize(14);

        ten.setTypeface(
                Typeface.create(
                        "sans-serif-medium",
                        Typeface.NORMAL
                )
        );

        ten.setMinLines(2);

        ten.setMaxLines(2);

        ten.setEllipsize(
                TextUtils.TruncateAt.END
        );

        ten.setPadding(
                dp(2),
                dp(8),
                dp(2),
                0
        );

        the.addView(ten);

        // MÔ TẢ NGẮN CỦA ĐIỂM THÀNH PHẦN
        // Hiển thị ngay tại trang chi tiết địa điểm chính để người dùng biết
        // nội dung của từng điểm trước khi bắt đầu hành trình.
        String moTaDiem = TextUtils.isEmpty(diem.moTa)
                ? diem.noiDung
                : diem.moTa;

        if (!TextUtils.isEmpty(moTaDiem)) {

            TextView moTa = new TextView(this);

            moTa.setText(moTaDiem.trim());

            moTa.setTextColor(MAU_CHU_PHU);

            moTa.setTextSize(12);

            moTa.setLineSpacing(dp(2), 1f);

            moTa.setMinLines(2);

            moTa.setMaxLines(2);

            moTa.setEllipsize(TextUtils.TruncateAt.END);

            moTa.setPadding(
                    dp(2),
                    dp(4),
                    dp(2),
                    0
            );

            the.addView(moTa);
        }

        // TRẠNG THÁI THUYẾT MINH

        TextView nghe = new TextView(this);

        boolean coThuyetMinh =
                !TextUtils.isEmpty(diem.noiDung)
                        && !diem.noiDung.trim().isEmpty();

        nghe.setText(
                coThuyetMinh
                        ? "Thuyết minh"
                        : "Khám phá"
        );

        nghe.setTextSize(12);

        nghe.setTextColor(MAU_CHU_PHU);

        if (coThuyetMinh) {

            nghe.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ui_ic_audio,
                    0, 0, 0
            );

            nghe.setCompoundDrawablePadding(dp(5));
        }

        nghe.setPadding(
                dp(2),
                dp(3),
                dp(2),
                dp(5)
        );

        the.addView(nghe);

        ganHieuUngNhan(the);

        the.setClickable(true);

        the.setFocusable(true);

        the.setContentDescription(
                diem.ten
                        + (TextUtils.isEmpty(moTaDiem)
                        ? ""
                        : ", " + moTaDiem)
                        + (coThuyetMinh
                        ? ", có thuyết minh"
                        : "")
        );

        // Giữ nguyên điều hướng đến điểm thành phần

        the.setOnClickListener(v ->
                moManHinhThamQuan(diem.id)
        );

        return the;
    }

    // =====================================================
    // ĐỌC ẢNH TỪ DRAWABLE VÀ ẢNH ADMIN
    // =====================================================

    void ganHieuUngNhan(View view) {

        TypedValue attr = new TypedValue();

        if (getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                attr,
                true
        ) && attr.resourceId != 0) {

            view.setForeground(
                    ContextCompat.getDrawable(
                            this,
                            attr.resourceId
                    )
            );
        }
    }

    // =====================================================
    // BẢN ĐỒ VÀ CHỈ ĐƯỜNG
    // =====================================================

    private void moXemBanDo() {

        moLienKetBanDo(false);
    }

    private void moChiDuong() {

        moLienKetBanDo(true);
    }

    private void moLienKetBanDo(
            boolean chiDuong
    ) {

        if (!coToaDoHopLe()) {

            thongBao(
                    "Địa điểm chưa có tọa độ hợp lệ."
            );

            return;
        }

        String toaDo =
                diaDiem.viDo
                        + ","
                        + diaDiem.kinhDo;

        String url =
                chiDuong

                        ? "https://www.google.com/maps/dir/"
                        + "?api=1&destination="
                        + toaDo

                        : "https://www.google.com/maps/search/"
                        + "?api=1&query="
                        + toaDo;

        try {

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    )
            );

        } catch (ActivityNotFoundException e) {

            thongBao(
                    "Không thể mở bản đồ."
            );
        }
    }

    private boolean coToaDoHopLe() {

        return diaDiem != null
                && Double.isFinite(diaDiem.viDo)
                && Double.isFinite(diaDiem.kinhDo)
                && Math.abs(diaDiem.viDo) <= 90
                && Math.abs(diaDiem.kinhDo) <= 180;
    }

    // =====================================================
    // ĐIỀU HƯỚNG THAM QUAN
    // =====================================================

    private void moManHinhThamQuan(int idDiem) {

        if (boAnh != null) boAnh.dungHenAnh();

        dungThuyetMinh();

        startActivity(
                DieuHuongThamQuan.taoIntent(
                        this,
                        viTri,
                        idDiem
                )
        );
    }

    // =====================================================
    // THUYẾT MINH
    // =====================================================

    private void hienTrangThaiDoc(
            String noiDung
    ) {

        TextView status =
                findViewById(
                        R.id.trangThaiThuyetMinh
                );

        status.setVisibility(View.VISIBLE);

        status.setText(noiDung);
    }

    private void phatSauXacNhanViTri() {

        if (!choPhatTuViTri
                || !dangHienThi
                || isFinishing()
                || isDestroyed()) {

            return;
        }

        choPhatTuViTri = false;

        docThuyetMinh(KhoLichSu.AUTO_MAIN);
    }

    private void docThuyetMinh() {

        docThuyetMinh(
                KhoLichSu.THU_CONG
        );
    }

    private void docThuyetMinh(
            String cheDo
    ) {

        choPhatTuViTri = false;

        moTrinhNghe(
                true,
                cheDo
        );
    }

    @Override
    public void moKhungThuyetMinh(
            boolean phatNgay
    ) {

        choPhatTuViTri = false;

        moTrinhNghe(
                phatNgay,
                KhoLichSu.THU_CONG
        );
    }

    private void moTrinhNghe(
            boolean phatNgay,
            String cheDo
    ) {

        if (!taiChiTietTuSQLite()) {

            thongBao(
                    "Địa điểm không còn khả dụng."
            );

            finish();

            return;
        }

        if (TextUtils.isEmpty(diaDiem.moTa)
                || diaDiem.moTa.trim().isEmpty()) {

            thongBao(
                    "Chưa có nội dung thuyết minh."
            );

            findViewById(R.id.trangThaiThuyetMinh)
                    .setVisibility(View.GONE);

            return;
        }

        hienThiThongTin();

        if (boAnh != null) boAnh.dungHenAnh();

        // Luôn sử dụng ảnh chính cho trình nghe.
        // Không sử dụng nhầm ảnh đang chạy trên slider.

        ImageView anhGoc =
                new ImageView(this);

        Bitmap bitmap =
                boAnh.docAnhChiTiet(
                        diaDiem.tenAnh,
                        1600
                );

        if (bitmap != null) {

            anhGoc.setImageBitmap(bitmap);

        } else {

            AnhDiaDiem.hien(
                    anhGoc,
                    diaDiem.tenAnh
            );
        }

        moKhungThuyetMinh(
                diaDiem.ten,
                diaDiem.moTa,
                diaDiem.ten,
                anhGoc.getDrawable(),
                phatNgay,
                viTri,
                0,
                cheDo
        );

        findViewById(R.id.trangThaiThuyetMinh)
                .setVisibility(View.GONE);
    }

    private void dungThuyetMinh() {

        huyLuotLichSu();

        choPhatTuViTri = false;

        dongKhungThuyetMinh();

        TextView status =
                findViewById(
                        R.id.trangThaiThuyetMinh
                );

        if (status != null
                && status.getVisibility() == View.VISIBLE) {

            status.setText(
                    "Đã dừng thuyết minh."
            );
        }
    }

    // =====================================================
    // YÊU THÍCH
    // =====================================================

    private void capNhatYeuThich() {

        if (nutTimYeuThich == null
                || nutYeuThich == null) {

            return;
        }

        boolean daLuu = false;

        if (PhienDangNhap.daDangNhap(this)) {

            try (AppDatabase db = new AppDatabase(this)) {

                daLuu = db.daYeuThich(
                        PhienDangNhap.id(this),
                        viTri
                );

            } catch (Exception e) {

                Log.e(TAG, "Lỗi đọc yêu thích", e);
            }
        }

        nutYeuThich.setText(
                daLuu
                        ? "♥\nĐã thích"
                        : "♡\nYêu thích"
        );

        nutTimYeuThich.setText(
                daLuu ? "♥" : "♡"
        );

        nutTimYeuThich.setTextColor(
                daLuu
                        ? MAU_YEU_THICH
                        : MAU_CHU_DAO
        );

        nutTimYeuThich.setContentDescription(
                daLuu
                        ? "Bỏ yêu thích"
                        : "Lưu địa điểm yêu thích"
        );

        nutTimYeuThich.setSelected(daLuu);
    }

    private void doiYeuThich() {

        if (!PhienDangNhap.daDangNhap(this)) {

            new MaterialAlertDialogBuilder(this)

                    .setTitle("Yêu cầu đăng nhập")

                    .setMessage(
                            "Đăng nhập để lưu địa điểm yêu thích."
                    )

                    .setPositiveButton(
                            "Đăng nhập",
                            (dialog, which) -> {

                                Intent intent =
                                        new Intent(
                                                this,
                                                DangNhapActivity.class
                                        );

                                startActivityForResult(
                                        intent,
                                        MA_DANG_NHAP_YEU_THICH
                                );
                            }
                    )

                    .setNegativeButton(
                            "Để sau",
                            null
                    )

                    .show();

            return;
        }

        boolean daLuu;

        try (AppDatabase db = new AppDatabase(this)) {

            long idTaiKhoan =
                    PhienDangNhap.id(this);

            daLuu = db.daYeuThich(
                    idTaiKhoan,
                    viTri
            );

            if (!db.datYeuThich(
                    idTaiKhoan,
                    viTri,
                    !daLuu
            )) {

                throw new IllegalStateException(
                        "Không cập nhật được yêu thích"
                );
            }

        } catch (Exception e) {

            Log.e(TAG, "Lỗi cập nhật yêu thích", e);

            thongBao(
                    "Không thể lưu địa điểm."
            );

            return;
        }

        capNhatYeuThich();

        thongBao(
                daLuu
                        ? "Đã bỏ yêu thích"
                        : "Đã thêm vào yêu thích"
        );
    }

    // =====================================================
    // NHẬN KẾT QUẢ ĐĂNG NHẬP
    // =====================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != MA_DANG_NHAP_YEU_THICH
                || resultCode != RESULT_OK
                || !PhienDangNhap.daDangNhap(this)) {

            return;
        }

        try (AppDatabase db = new AppDatabase(this)) {

            if (!db.datYeuThich(
                    PhienDangNhap.id(this),
                    viTri,
                    true
            )) {

                throw new IllegalStateException(
                        "Không lưu được yêu thích"
                );
            }

            capNhatYeuThich();

            thongBao(
                    "Đã thêm vào yêu thích"
            );

        } catch (Exception e) {

            Log.e(TAG, "Lỗi lưu yêu thích", e);

            thongBao(
                    "Không thể lưu địa điểm."
            );
        }
    }

    // =====================================================
    // TIỆN ÍCH
    // =====================================================

    private void thongBao(
            String noiDung
    ) {

        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }

    private int dp(int value) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =====================================================
    // VÒNG ĐỜI ACTIVITY
    // =====================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (isFinishing()) {

            return;
        }

        if (!taiChiTietTuSQLite()) {

            thongBao(
                    "Địa điểm không còn khả dụng."
            );

            finish();

            return;
        }

        dangHienThi = true;


        hienThiThongTin();

        taoTheDiem();

        capNhatYeuThich();

        boAnh.tiepTuc();

        phatSauXacNhanViTri();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (boAnh != null) boAnh.doiFocus(hasFocus);
    }

    @Override
    protected void onPause() {

        dangHienThi = false;

        if (boAnh != null) boAnh.dungHenAnh();

        if (boAnh != null) boAnh.tamDung();

        super.onPause();
    }

    @Override
    protected void onStop() {

        dangHienThi = false;

        if (boAnh != null) boAnh.dungHenAnh();

        dungThuyetMinh();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        if (boAnh != null) boAnh.giaiPhong();

        super.onDestroy();
    }
}
