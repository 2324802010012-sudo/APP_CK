package com.example.app_ck.ui.place;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.NguonThongTin;
import com.example.app_ck.ui.tour.DieuHuongThamQuan;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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

    private static final long THOI_GIAN_CHUYEN_ANH = 4000L;

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
    private boolean dangChamAnh;
    private boolean dangMoThuVien;

    private DiaDiem diaDiem;

    private final ArrayList<DiemThamQuan> cacDiem =
            new ArrayList<>();

    private TextView nutYeuThich;
    private TextView nutTimYeuThich;

    // =====================================================
    // DỮ LIỆU SLIDER ẢNH
    // =====================================================

    private ImageView anhBia;

    private TextView demAnhBia;
    private TextView tenAnhBia;

    private final ArrayList<String> boAnh =
            new ArrayList<>();

    private final LinkedHashMap<String, String> tenCacAnh =
            new LinkedHashMap<>();

    private int viTriAnh = 0;

    private final Handler henAnh =
            new Handler(Looper.getMainLooper());

    private final Runnable tuDongChuyenAnh =
            new Runnable() {

                @Override
                public void run() {

                    if (!coTheTuChuyenAnh()) {
                        return;
                    }

                    chuyenAnhBia(1);

                    batHenAnh();
                }
            };

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

        anhBia = findViewById(R.id.anhChiTiet);

        demAnhBia = findViewById(R.id.soLuongAnh);

        tenAnhBia = findViewById(R.id.ctTenAnh);

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

        findViewById(R.id.nutThuVien)
                .setOnClickListener(v -> moThuVien());

        demAnhBia.setOnClickListener(v -> moThuVien());

        anhBia.setOnClickListener(v -> moThuVien());

        nutYeuThich.setOnClickListener(v -> doiYeuThich());

        nutTimYeuThich.setOnClickListener(v -> doiYeuThich());

        // Vuốt ngang ảnh đầu trang

        ganVuotAnh(
                anhBia,
                () -> chuyenAnhBia(-1),
                () -> chuyenAnhBia(1),
                () -> {
                    dangChamAnh = true;
                    dungHenAnh();
                },
                () -> {
                    dangChamAnh = false;
                    batHenAnh();
                }
        );

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

            diaDiem = null;

            for (DiaDiem d : helper.danhSachDiaDiem()) {

                if (d.id == viTri) {

                    diaDiem = d;

                    break;
                }
            }

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

        capNhatBoAnh();

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
    // CHUẨN BỊ DANH SÁCH ẢNH SLIDER
    // =====================================================

    private void capNhatBoAnh() {

        String anhCu = null;

        if (!boAnh.isEmpty()
                && viTriAnh >= 0
                && viTriAnh < boAnh.size()) {

            anhCu = boAnh.get(viTriAnh);
        }

        tenCacAnh.clear();

        tenCacAnh.putAll(
                layDanhSachAnhChiTiet()
        );

        boAnh.clear();

        boAnh.addAll(tenCacAnh.keySet());

        viTriAnh = 0;

        if (anhCu != null) {

            int timThay =
                    boAnh.indexOf(anhCu);

            if (timThay >= 0) {
                viTriAnh = timThay;
            }
        }

        hienAnhBia(false);

        View nutThuVien =
                findViewById(R.id.nutThuVien);

        nutThuVien.setEnabled(!boAnh.isEmpty());

        nutThuVien.setAlpha(
                boAnh.isEmpty() ? 0.5f : 1f
        );

        batHenAnh();
    }

    // =====================================================
    // HIỂN THỊ ẢNH BÌA
    // =====================================================

    private void hienAnhBia(boolean hieuUng) {

        if (anhBia == null) {
            return;
        }

        anhBia.animate().cancel();

        anhBia.setAlpha(1f);

        if (boAnh.isEmpty()) {

            AnhDiaDiem.hien(
                    anhBia,
                    diaDiem.tenAnh
            );

            demAnhBia.setText("0 ảnh");

            tenAnhBia.setText("");

            return;
        }

        String maAnh =
                boAnh.get(viTriAnh);

        Bitmap bitmap =
                docAnhChiTiet(maAnh, 1600);

        if (bitmap != null) {

            anhBia.setImageBitmap(bitmap);

        } else {

            AnhDiaDiem.hien(
                    anhBia,
                    maAnh
            );
        }

        String ten =
                tenCacAnh.get(maAnh);

        anhBia.setContentDescription(ten);

        // Không lặp lại tên địa điểm chính.
        // Chỉ hiện tên khi đang xem ảnh điểm thành phần.

        tenAnhBia.setText(
                TextUtils.equals(
                        maAnh,
                        diaDiem.tenAnh
                )
                        ? ""
                        : ten
        );

        demAnhBia.setText(
                (viTriAnh + 1)
                        + " / "
                        + boAnh.size()
        );

        // Hiệu ứng xuất hiện mềm mại

        if (hieuUng) {

            anhBia.setAlpha(0.45f);

            anhBia.animate()
                    .alpha(1f)
                    .setDuration(350)
                    .start();
        }
    }

    private void chuyenAnhBia(int huong) {

        if (boAnh.size() <= 1) {
            return;
        }

        viTriAnh =
                (viTriAnh + huong + boAnh.size())
                        % boAnh.size();

        hienAnhBia(true);
    }

    // =====================================================
    // TỰ ĐỘNG CHUYỂN ẢNH
    // =====================================================

    private boolean coTheTuChuyenAnh() {

        return dangHienThi
                && !dangChamAnh
                && !dangMoThuVien
                && !isFinishing()
                && !isDestroyed()
                && hasWindowFocus()
                && boAnh.size() > 1;
    }

    private void batHenAnh() {

        henAnh.removeCallbacks(tuDongChuyenAnh);

        if (coTheTuChuyenAnh()) {

            henAnh.postDelayed(
                    tuDongChuyenAnh,
                    THOI_GIAN_CHUYEN_ANH
            );
        }
    }

    private void dungHenAnh() {

        henAnh.removeCallbacks(tuDongChuyenAnh);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {

            batHenAnh();

        } else {

            dungHenAnh();
        }
    }

    // =====================================================
    // XỬ LÝ VUỐT NGANG
    // =====================================================

    private void ganVuotAnh(
            ImageView anh,
            Runnable anhTruoc,
            Runnable anhSau,
            Runnable batDauCham,
            Runnable ketThucCham
    ) {

        final float[] diemCham = new float[2];

        int touchSlop =
                ViewConfiguration.get(this)
                        .getScaledTouchSlop();

        anh.setOnTouchListener((v, event) -> {

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    diemCham[0] = event.getX();

                    diemCham[1] = event.getY();

                    if (batDauCham != null) {

                        batDauCham.run();
                    }

                    return true;

                case MotionEvent.ACTION_MOVE:

                    float dx =
                            event.getX() - diemCham[0];

                    float dy =
                            event.getY() - diemCham[1];

                    // Ưu tiên vuốt ngang, vẫn cho phép
                    // ScrollView cuộn dọc bình thường.

                    if (Math.abs(dx) > touchSlop
                            && Math.abs(dx) > Math.abs(dy)
                            && v.getParent() != null) {

                        v.getParent()
                                .requestDisallowInterceptTouchEvent(true);
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    float khoangX =
                            event.getX() - diemCham[0];

                    float khoangY =
                            event.getY() - diemCham[1];

                    if (Math.abs(khoangX) > dp(45)
                            && Math.abs(khoangX)
                            > Math.abs(khoangY)) {

                        if (khoangX < 0) {

                            anhSau.run();

                        } else {

                            anhTruoc.run();
                        }

                    } else if (Math.abs(khoangX) < touchSlop
                            && Math.abs(khoangY) < touchSlop) {

                        v.performClick();
                    }

                    if (v.getParent() != null) {

                        v.getParent()
                                .requestDisallowInterceptTouchEvent(false);
                    }

                    if (ketThucCham != null) {

                        ketThucCham.run();
                    }

                    return true;

                case MotionEvent.ACTION_CANCEL:

                    if (v.getParent() != null) {

                        v.getParent()
                                .requestDisallowInterceptTouchEvent(false);
                    }

                    if (ketThucCham != null) {

                        ketThucCham.run();
                    }

                    return true;

                default:

                    return true;
            }
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
                        dp(166),
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
                docAnhChiTiet(
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
                        dp(118)
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

    private Bitmap giaiMaAnhChiTiet(
            String tenAnh,
            BitmapFactory.Options options
    ) {

        if (TextUtils.isEmpty(tenAnh)
                || AnhDiaDiem.KHONG_ANH.equals(tenAnh)) {

            return null;
        }

        try {

            if (tenAnh.startsWith("anh_may:")) {

                String tenTep =
                        tenAnh.substring(
                                "anh_may:".length()
                        );

                if (tenTep.isEmpty()
                        || tenTep.contains("/")
                        || tenTep.contains("\\")
                        || tenTep.contains("..")) {

                    return null;
                }

                File thuMuc = new File(
                        getFilesDir(),
                        "anh_dia_diem"
                );

                File tep =
                        new File(thuMuc, tenTep);

                if (!tep.isFile()) {

                    return null;
                }

                return BitmapFactory.decodeFile(
                        tep.getAbsolutePath(),
                        options
                );
            }

            int idAnh =
                    getResources().getIdentifier(
                            tenAnh,
                            "drawable",
                            getPackageName()
                    );

            if (idAnh == 0) {

                return null;
            }

            return BitmapFactory.decodeResource(
                    getResources(),
                    idAnh,
                    options
            );

        } catch (RuntimeException e) {

            Log.e(TAG, "Không thể đọc ảnh", e);

            return null;
        }
    }

    private boolean coAnhChiTiet(String tenAnh) {

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        giaiMaAnhChiTiet(
                tenAnh,
                options
        );

        return options.outWidth > 0
                && options.outHeight > 0;
    }

    private Bitmap docAnhChiTiet(
            String tenAnh,
            int kichThuoc
    ) {

        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inScaled = false;

        options.inJustDecodeBounds = true;

        giaiMaAnhChiTiet(
                tenAnh,
                options
        );

        if (options.outWidth <= 0
                || options.outHeight <= 0) {

            return null;
        }

        options.inSampleSize = 1;

        int kichThuocGoc = Math.max(
                options.outWidth,
                options.outHeight
        );

        while (kichThuocGoc / options.inSampleSize
                > kichThuoc) {

            options.inSampleSize *= 2;
        }

        options.inJustDecodeBounds = false;

        return giaiMaAnhChiTiet(
                tenAnh,
                options
        );
    }

    // =====================================================
    // DANH SÁCH ẢNH KHÔNG TRÙNG LẶP
    // =====================================================

    private LinkedHashMap<String, String>
    layDanhSachAnhChiTiet() {

        LinkedHashMap<String, String> danhSach =
                new LinkedHashMap<>();

        if (diaDiem == null) {

            return danhSach;
        }

        // Ưu tiên ảnh địa điểm chính

        if (coAnhChiTiet(diaDiem.tenAnh)) {

            danhSach.put(
                    diaDiem.tenAnh,
                    diaDiem.ten
            );
        }

        // Bổ sung ảnh điểm thành phần

        for (DiemThamQuan diem : cacDiem) {

            if (!danhSach.containsKey(diem.tenAnh)
                    && coAnhChiTiet(diem.tenAnh)) {

                danhSach.put(
                        diem.tenAnh,
                        diem.ten
                );
            }
        }

        return danhSach;
    }

    // =====================================================
    // THƯ VIỆN ẢNH
    // =====================================================

    private void moThuVien() {

        if (boAnh.isEmpty()) {

            thongBao(
                    "Địa điểm chưa có hình ảnh."
            );

            return;
        }

        dungHenAnh();

        dangMoThuVien = true;

        ArrayList<String> cacAnh =
                new ArrayList<>(boAnh);

        LinkedHashMap<String, String> danhSach =
                new LinkedHashMap<>(tenCacAnh);

        final int[] anhDangXem = {viTriAnh};

        LinearLayout khung = new LinearLayout(this);

        khung.setOrientation(
                LinearLayout.VERTICAL
        );

        khung.setPadding(
                dp(18),
                dp(8),
                dp(18),
                dp(12)
        );

        khung.setBackgroundColor(Color.WHITE);

        // ẢNH LỚN

        ImageView anh = new ImageView(this);

        anh.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        anh.setBackgroundResource(
                R.drawable.ui_ct_thumb
        );

        anh.setClipToOutline(true);

        khung.addView(
                anh,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(280)
                )
        );

        // TÊN ẢNH

        TextView tenAnh = new TextView(this);

        tenAnh.setGravity(Gravity.CENTER);

        tenAnh.setTextSize(16);

        tenAnh.setTextColor(MAU_TIEU_DE);

        tenAnh.setTypeface(
                null,
                Typeface.BOLD
        );

        tenAnh.setPadding(
                0, dp(14),
                0, dp(6)
        );

        khung.addView(tenAnh);

        // BỘ ĐẾM

        TextView demAnh = new TextView(this);

        demAnh.setGravity(Gravity.CENTER);

        demAnh.setTextSize(13);

        demAnh.setTextColor(MAU_CHU_PHU);

        khung.addView(demAnh);

        // HAI NÚT CHUYỂN ẢNH

        LinearLayout thanhNut =
                new LinearLayout(this);

        thanhNut.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout.LayoutParams lpThanh =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                );

        lpThanh.topMargin = dp(16);

        khung.addView(
                thanhNut,
                lpThanh
        );

        TextView nutTruoc =
                taoNutThuVien("‹  Ảnh trước");

        TextView nutSau =
                taoNutThuVien("Ảnh tiếp  ›");

        LinearLayout.LayoutParams lpTruoc =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        lpTruoc.setMarginEnd(dp(8));

        thanhNut.addView(
                nutTruoc,
                lpTruoc
        );

        thanhNut.addView(
                nutSau,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );

        // HIỂN THỊ ẢNH ĐANG CHỌN

        Runnable capNhatAnh = () -> {

            String maAnh =
                    cacAnh.get(anhDangXem[0]);

            Bitmap bitmap =
                    docAnhChiTiet(
                            maAnh,
                            1600
                    );

            if (bitmap != null) {

                anh.setImageBitmap(bitmap);

            } else {

                AnhDiaDiem.hien(anh, maAnh);
            }

            tenAnh.setText(
                    danhSach.get(maAnh)
            );

            anh.setContentDescription(
                    danhSach.get(maAnh)
            );

            demAnh.setText(
                    (anhDangXem[0] + 1)
                            + " / "
                            + cacAnh.size()
            );

            boolean coAnhTruoc =
                    anhDangXem[0] > 0;

            boolean coAnhSau =
                    anhDangXem[0] < cacAnh.size() - 1;

            nutTruoc.setEnabled(coAnhTruoc);

            nutSau.setEnabled(coAnhSau);

            nutTruoc.setAlpha(
                    coAnhTruoc ? 1f : 0.4f
            );

            nutSau.setAlpha(
                    coAnhSau ? 1f : 0.4f
            );
        };

        Runnable anhTruoc = () -> {

            if (anhDangXem[0] > 0) {

                anhDangXem[0]--;

                capNhatAnh.run();
            }
        };

        Runnable anhSau = () -> {

            if (anhDangXem[0]
                    < cacAnh.size() - 1) {

                anhDangXem[0]++;

                capNhatAnh.run();
            }
        };

        nutTruoc.setOnClickListener(v ->
                anhTruoc.run()
        );

        nutSau.setOnClickListener(v ->
                anhSau.run()
        );

        ganVuotAnh(
                anh,
                anhTruoc,
                anhSau,
                null,
                null
        );

        capNhatAnh.run();

        if (cacAnh.size() <= 1) {

            thanhNut.setVisibility(View.GONE);
        }

        ScrollView vungCuon =
                new ScrollView(this);

        vungCuon.addView(khung);

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)

                        .setTitle("Thư viện hình ảnh")

                        .setView(vungCuon)

                        .setPositiveButton(
                                "Đóng",
                                null
                        )

                        .show();

        // Khôi phục slider sau khi đóng thư viện

        dialog.setOnDismissListener(d -> {

            dangMoThuVien = false;

            int viTriMoi =
                    boAnh.indexOf(
                            cacAnh.get(anhDangXem[0])
                    );

            if (viTriMoi >= 0) {

                viTriAnh = viTriMoi;

                hienAnhBia(false);
            }

            batHenAnh();
        });
    }

    private TextView taoNutThuVien(
            String noiDung
    ) {

        TextView nut = new TextView(this);

        nut.setText(noiDung);

        nut.setGravity(Gravity.CENTER);

        nut.setTextSize(13);

        nut.setTextColor(MAU_CHU_DAO);

        nut.setTypeface(
                null,
                Typeface.BOLD
        );

        nut.setBackgroundResource(
                R.drawable.ui_ct_audio_bg
        );

        nut.setClickable(true);

        nut.setFocusable(true);

        ganHieuUngNhan(nut);

        return nut;
    }

    private void ganHieuUngNhan(View view) {

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

        dungHenAnh();

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

        String cheDo =
                KhoLichSu.DEMO.equals(
                        getIntent().getStringExtra(
                                CHE_DO_VI_TRI
                        )
                )
                        ? KhoLichSu.DEMO
                        : KhoLichSu.AUTO_MAIN;

        docThuyetMinh(cheDo);
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

        dungHenAnh();

        // Luôn sử dụng ảnh chính cho trình nghe.
        // Không sử dụng nhầm ảnh đang chạy trên slider.

        ImageView anhGoc =
                new ImageView(this);

        Bitmap bitmap =
                docAnhChiTiet(
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

        dangChamAnh = false;

        hienThiThongTin();

        taoTheDiem();

        capNhatYeuThich();

        batHenAnh();

        phatSauXacNhanViTri();
    }

    @Override
    protected void onPause() {

        dangHienThi = false;

        dungHenAnh();

        if (anhBia != null) {

            anhBia.animate().cancel();

            anhBia.setAlpha(1f);
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        dangHienThi = false;

        dungHenAnh();

        dungThuyetMinh();

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        dungHenAnh();

        super.onDestroy();
    }
}
