package com.example.app_ck.ui.admin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public class QuanLyDiaDiemActivity extends BaseActivity {

    private static final String TAG = "QuanLyDiaDiem";

    private static final int MAU_CHU =
            Color.parseColor("#182D22");

    private static final int MAU_PHU =
            Color.parseColor("#687B6E");

    private static final int MAU_XANH =
            Color.parseColor("#2C7351");

    // Danh sach
    private LinearLayout danhSach;
    private EditText timKiem;
    private TextView tong, trong;
    private TextView nutXoaTimKiem, nutXoaLoc;

    private ArrayList<DiaDiem> duLieu = new ArrayList<>();

    // Hai man hinh trong cung Activity
    private View manDanhSach, manBieuMau;

    // Bieu mau
    private EditText ten, diaChi, moTa;
    private EditText viDo, kinhDo;
    private EditText tenNguon, urlNguon;
    private EditText tenAnhDrawable;

    private Spinner danhMuc;
    private ImageView anh;

    // Anh moi chon tu thiet bi
    private Uri anhMoi;

    // Anh dang luu trong SQLite
    private String anhHienTai = "";

    // -1: them moi, >= 0: chinh sua
    private int id = -1;

    // =====================================================
    // CHON ANH TU THIET BI
    // =====================================================

    private final ActivityResultLauncher<String> layAnh =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null || anh == null) {
                            return;
                        }

                        if (tenAnhDrawable != null) {
                            tenAnhDrawable.setText("");
                        }

                        anhMoi = uri;
                        anh.setImageURI(uri);

                        thongBao("Đã chọn ảnh từ điện thoại");
                    }
            );

    // =====================================================
    // KHOI TAO
    // =====================================================

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (!coQuyen()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_quan_ly_dia_diem);
        apDungKhoangTrongHeThong();

        anhXaGiaoDien();
        ganSuKien();

        // Giu lop giao dien dung chung da co trong du an
        AdminGiaoDien.apDung(this);

        cauHinhAnhDrawable();
        taiDuLieu();
    }

    private void anhXaGiaoDien() {

        // Man hinh
        manDanhSach = findViewById(R.id.qlddManDanhSach);
        manBieuMau = findViewById(R.id.qlddManBieuMau);

        // Danh sach
        danhSach = findViewById(R.id.qlddDanhSach);
        timKiem = findViewById(R.id.qlddTimKiem);
        tong = findViewById(R.id.qlddTongSo);
        trong = findViewById(R.id.qlddTrong);

        nutXoaTimKiem = findViewById(
                R.id.qlddXoaTimKiem
        );

        nutXoaLoc = findViewById(
                R.id.qlddXoaLoc
        );

        // Bieu mau
        ten = findViewById(R.id.ddFormTen);
        diaChi = findViewById(R.id.ddFormDiaChi);
        moTa = findViewById(R.id.ddFormMoTa);

        viDo = findViewById(R.id.ddFormViDo);
        kinhDo = findViewById(R.id.ddFormKinhDo);

        tenNguon = findViewById(R.id.ddFormTenNguon);
        urlNguon = findViewById(R.id.ddFormUrlNguon);

        danhMuc = findViewById(R.id.ddFormDanhMuc);
        anh = findViewById(R.id.ddFormAnh);

        tenAnhDrawable = findViewById(
                R.id.ddFormTenAnh
        );
    }

    private void ganSuKien() {

        // Dieu huong
        findViewById(R.id.qlddQuayLai)
                .setOnClickListener(v -> finish());

        findViewById(R.id.qlddThemMoi)
                .setOnClickListener(v -> moBieuMau(-1));

        // Bieu mau
        findViewById(R.id.ddFormQuayLai)
                .setOnClickListener(v -> dongBieuMau());

        findViewById(R.id.ddFormChonAnh)
                .setOnClickListener(
                        v -> layAnh.launch("image/*")
                );

        findViewById(R.id.ddFormLuu)
                .setOnClickListener(v -> luu());

        findViewById(R.id.ddFormXoa)
                .setOnClickListener(v -> xacNhanXoa());

        // Xoa tu khoa
        nutXoaTimKiem.setOnClickListener(
                v -> timKiem.setText("")
        );

        nutXoaLoc.setOnClickListener(
                v -> timKiem.setText("")
        );

        // Tim kiem theo thoi gian thuc
        timKiem.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                nutXoaTimKiem.setVisibility(
                        s.length() > 0
                                ? View.VISIBLE
                                : View.GONE
                );

                hienThi();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // An ban phim khi nhan Done
        timKiem.setOnEditorActionListener(
                (view, actionId, event) -> {

                    if (actionId != EditorInfo.IME_ACTION_DONE) {
                        return false;
                    }

                    InputMethodManager imm =
                            (InputMethodManager)
                                    getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

                    if (imm != null) {
                        imm.hideSoftInputFromWindow(
                                timKiem.getWindowToken(), 0
                        );
                    }

                    timKiem.clearFocus();
                    return true;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!coQuyen()) {
            finish();
            return;
        }

        if (danhSach != null) {
            taiDuLieu();
        }
    }

    // =====================================================
    // PHAN QUYEN ADMIN
    // =====================================================

    private boolean coQuyen() {

        try (AppDatabase helper = new AppDatabase(this)) {

            PhienDangNhap.yeuCauQuanTri(
                    this,
                    helper.getReadableDatabase()
            );

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Không xác minh được quyền Admin", e);
            return false;
        }
    }

    // =====================================================
    // TAI DU LIEU SQLITE
    // =====================================================

    private void taiDuLieu() {

        try (AppDatabase helper = new AppDatabase(this)) {

            PhienDangNhap.yeuCauQuanTri(
                    this,
                    helper.getReadableDatabase()
            );

            duLieu = helper.danhSachDiaDiem();

            hienThi();

        } catch (Exception e) {

            Log.e(TAG, "Không tải được địa điểm", e);
            thongBao("Không tải được danh sách địa điểm");
        }
    }

    // =====================================================
    // TIM KIEM KHONG DAU TIENG VIET
    // =====================================================

    private String chuanHoaTimKiem(String text) {

        if (text == null) {
            return "";
        }

        return Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    // =====================================================
    // HIEN THI DANH SACH DIA DIEM
    // =====================================================

    private void hienThi() {

        if (danhSach == null || timKiem == null) {
            return;
        }

        danhSach.removeAllViews();

        String tuKhoa = chuanHoaTimKiem(
                timKiem.getText().toString()
        );

        int soKetQua = 0;

        for (DiaDiem diem : duLieu) {

            String noiDungTim = chuanHoaTimKiem(
                    s(diem.ten) + " "
                            + s(diem.diaChi) + " "
                            + s(diem.danhMuc)
            );

            if (!noiDungTim.contains(tuKhoa)) {
                continue;
            }

            soKetQua++;

            // THE DIA DIEM
            LinearLayout the = new LinearLayout(this);

            the.setOrientation(LinearLayout.HORIZONTAL);
            the.setGravity(Gravity.CENTER_VERTICAL);

            the.setPadding(
                    dp(9), dp(9), dp(12), dp(9)
            );

            the.setMinimumHeight(dp(80));
            the.setBackgroundResource(R.drawable.ui_card);
            the.setClickable(true);
            the.setFocusable(true);

            the.setContentDescription(
                    "Chỉnh sửa địa điểm " + s(diem.ten)
            );

            // Hieu ung khi cham
            TypedValue hieuUng = new TypedValue();

            if (getTheme().resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    hieuUng,
                    true
            ) && hieuUng.resourceId != 0) {

                the.setForeground(
                        getDrawable(hieuUng.resourceId)
                );
            }

            // ANH DAI DIEN
            ImageView anhDaiDien = new ImageView(this);

            anhDaiDien.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            anhDaiDien.setBackgroundResource(
                    R.drawable.ui_ct_thumb
            );

            anhDaiDien.setClipToOutline(true);

            AnhDiaDiem.hien(
                    anhDaiDien,
                    diem.tenAnh
            );

            the.addView(
                    anhDaiDien,
                    new LinearLayout.LayoutParams(
                            dp(58), dp(58)
                    )
            );

            // THONG TIN
            LinearLayout thongTin =
                    new LinearLayout(this);

            thongTin.setOrientation(
                    LinearLayout.VERTICAL
            );

            LinearLayout.LayoutParams lpThongTin =
                    new LinearLayout.LayoutParams(
                            0, -2, 1f
                    );

            lpThongTin.leftMargin = dp(11);
            lpThongTin.rightMargin = dp(6);

            the.addView(thongTin, lpThongTin);

            // TEN DIA DIEM
            TextView tenDiaDiem = new TextView(this);

            tenDiaDiem.setText(s(diem.ten));
            tenDiaDiem.setTextSize(14.5f);
            tenDiaDiem.setTextColor(MAU_CHU);

            tenDiaDiem.setTypeface(
                    Typeface.create(
                            "sans-serif-medium",
                            Typeface.NORMAL
                    )
            );

            tenDiaDiem.setMaxLines(2);

            tenDiaDiem.setEllipsize(
                    android.text.TextUtils.TruncateAt.END
            );

            thongTin.addView(tenDiaDiem);

            // DIA CHI
            if (!s(diem.diaChi).trim().isEmpty()) {

                TextView tvDiaChi = new TextView(this);

                tvDiaChi.setText(diem.diaChi);
                tvDiaChi.setTextSize(12.5f);
                tvDiaChi.setTextColor(MAU_PHU);

                tvDiaChi.setMaxLines(2);

                tvDiaChi.setEllipsize(
                        android.text.TextUtils.TruncateAt.END
                );

                tvDiaChi.setPadding(
                        0, dp(4), 0, 0
                );

                thongTin.addView(tvDiaChi);
            }

            // DANH MUC
            if (!s(diem.danhMuc).trim().isEmpty()) {

                TextView tvDanhMuc = new TextView(this);

                tvDanhMuc.setText(diem.danhMuc);
                tvDanhMuc.setTextSize(11.5f);
                tvDanhMuc.setTextColor(MAU_XANH);

                tvDanhMuc.setMaxLines(1);

                tvDanhMuc.setEllipsize(
                        android.text.TextUtils.TruncateAt.END
                );

                tvDanhMuc.setPadding(
                        0, dp(4), 0, 0
                );

                thongTin.addView(tvDanhMuc);
            }

            // MUI TEN
            ImageView muiTen = new ImageView(this);

            muiTen.setImageResource(
                    R.drawable.ui_ic_chevron
            );

            muiTen.setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
            );

            the.addView(
                    muiTen,
                    new LinearLayout.LayoutParams(
                            dp(20), dp(20)
                    )
            );

            // MO BIEU MAU CHINH SUA
            the.setOnClickListener(
                    v -> moBieuMau(diem.id)
            );

            LinearLayout.LayoutParams lpThe =
                    new LinearLayout.LayoutParams(
                            -1, -2
                    );

            lpThe.bottomMargin = dp(9);

            danhSach.addView(the, lpThe);
        }

        // CAP NHAT SO LUONG
        if (tuKhoa.isEmpty()) {

            tong.setText(
                    duLieu.size() + " địa điểm"
            );

        } else {

            tong.setText(
                    soKetQua + "/" + duLieu.size()
                            + " địa điểm phù hợp"
            );
        }

        // TRANG THAI RONG
        boolean khongCoKetQua = soKetQua == 0;

        trong.setVisibility(
                khongCoKetQua
                        ? View.VISIBLE
                        : View.GONE
        );

        if (khongCoKetQua) {

            trong.setText(
                    tuKhoa.isEmpty()
                            ? "Chưa có địa điểm nào.\n"
                            + "Nhấn Thêm mới để bắt đầu."
                            : "Không tìm thấy địa điểm "
                            + "phù hợp với từ khóa."
            );
        }

        nutXoaLoc.setVisibility(
                khongCoKetQua && !tuKhoa.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    // =====================================================
    // MO BIEU MAU THEM / SUA
    // =====================================================

    private void moBieuMau(int idDiaDiem) {

        id = idDiaDiem;

        anhMoi = null;
        anhHienTai = "";

        ten.setText("");
        diaChi.setText("");
        moTa.setText("");

        tenNguon.setText("");
        urlNguon.setText("");

        viDo.setText("");
        kinhDo.setText("");

        tenAnhDrawable.setText("");

        anh.setImageResource(
                android.R.drawable.ic_menu_gallery
        );

        taoDanhMuc(null);

        ((TextView) findViewById(R.id.ddFormTieuDe))
                .setText(
                        id < 0
                                ? "Thêm địa điểm"
                                : "Chỉnh sửa địa điểm"
                );

        ((Button) findViewById(R.id.ddFormXoa))
                .setVisibility(
                        id < 0
                                ? View.GONE
                                : View.VISIBLE
                );

        if (id >= 0 && !taiThongTin()) {
            return;
        }

        manDanhSach.setVisibility(View.GONE);
        manBieuMau.setVisibility(View.VISIBLE);
    }

    private void dongBieuMau() {

        manBieuMau.setVisibility(View.GONE);
        manDanhSach.setVisibility(View.VISIBLE);

        id = -1;
        anhMoi = null;
    }

    @Override
    public void onBackPressed() {

        if (manBieuMau != null
                && manBieuMau.getVisibility() == View.VISIBLE) {

            dongBieuMau();

        } else {

            super.onBackPressed();
        }
    }

    // =====================================================
    // DANH MUC TU SQLITE
    // =====================================================

    private void taoDanhMuc(String hienTai) {

        ArrayList<String> items = new ArrayList<>();

        try (
                AppDatabase helper = new AppDatabase(this);
                Cursor cursor = helper.getReadableDatabase()
                        .rawQuery(
                                "SELECT DISTINCT TRIM(danh_muc) "
                                        + "FROM dia_diem "
                                        + "WHERE danh_muc IS NOT NULL "
                                        + "AND TRIM(danh_muc)<>'' "
                                        + "ORDER BY danh_muc COLLATE NOCASE",
                                null
                        )
        ) {

            while (cursor.moveToNext()) {

                String dm = cursor.getString(0);

                if (!items.contains(dm)) {
                    items.add(dm);
                }
            }

        } catch (Exception e) {

            Log.e(TAG, "Không lấy được danh mục", e);
        }

        if (hienTai != null
                && !hienTai.trim().isEmpty()
                && !items.contains(hienTai)) {

            items.add(0, hienTai);
        }

        if (items.isEmpty()) {

            items.add("Chưa có danh mục");
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        items
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        danhMuc.setAdapter(adapter);

        if (hienTai != null && items.contains(hienTai)) {

            danhMuc.setSelection(
                    items.indexOf(hienTai)
            );
        }
    }

    // =====================================================
    // ANH DRAWABLE VA ANH CHON TU MAY
    // =====================================================

    private void cauHinhAnhDrawable() {

        tenAnhDrawable.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable) {

                        String name =
                                editable.toString().trim();

                        if (name.isEmpty()) {
                            return;
                        }

                        int resId = timAnhDrawable(name);

                        if (resId != 0) {

                            anhMoi = null;
                            anh.setImageResource(resId);
                        }
                    }
                }
        );

        tenAnhDrawable.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (!hasFocus) {
                        xemTruocAnhDrawable();
                    }
                }
        );
    }

    private int timAnhDrawable(String name) {

        if (name == null) {
            return 0;
        }

        String value = name.trim();

        if (!value.matches("[a-z][a-z0-9_]*")) {
            return 0;
        }

        return getResources().getIdentifier(
                value,
                "drawable",
                getPackageName()
        );
    }

    private void xemTruocAnhDrawable() {

        String name = value(tenAnhDrawable);

        if (name.isEmpty()) {

            if (anhMoi != null) {

                anh.setImageURI(anhMoi);

            } else {

                AnhDiaDiem.hien(anh, anhHienTai);
            }

            return;
        }

        int resId = timAnhDrawable(name);

        if (resId == 0) {

            tenAnhDrawable.setError(
                    "Không tìm thấy ảnh trong drawable"
            );

            return;
        }

        anh.setImageResource(resId);
        anhMoi = null;

        tenAnhDrawable.setError(null);
    }

    // =====================================================
    // TAI THONG TIN DIA DIEM DE SUA
    // =====================================================

    private boolean taiThongTin() {

        try (AppDatabase db = new AppDatabase(this)) {

            PhienDangNhap.yeuCauQuanTri(
                    this,
                    db.getReadableDatabase()
            );

            DiaDiem found = null;

            for (DiaDiem diem : db.danhSachDiaDiem()) {

                if (diem.id == id) {

                    found = diem;
                    break;
                }
            }

            if (found == null) {

                thongBao("Địa điểm không còn tồn tại");
                return false;
            }

            ten.setText(s(found.ten));
            diaChi.setText(s(found.diaChi));
            moTa.setText(s(found.moTa));

            viDo.setText(
                    String.format(
                            Locale.US,
                            "%.6f",
                            found.viDo
                    )
            );

            kinhDo.setText(
                    String.format(
                            Locale.US,
                            "%.6f",
                            found.kinhDo
                    )
            );

            taoDanhMuc(found.danhMuc);

            anhHienTai = s(found.tenAnh);
            anhMoi = null;

            AnhDiaDiem.hien(anh, anhHienTai);

            if (anhHienTai.startsWith("anh_may:")) {

                tenAnhDrawable.setText("");

            } else {

                tenAnhDrawable.setText(anhHienTai);
            }

            // DOC NGUON THONG TIN CUA DIA DIEM
            try (
                    Cursor cursor = db.getReadableDatabase()
                            .rawQuery(
                                    "SELECT n.ten, n.url "
                                            + "FROM nguon_thong_tin n "
                                            + "JOIN bai_nguon b "
                                            + "ON b.ma_nguon=n.ma "
                                            + "WHERE b.dia_diem_id=? "
                                            + "AND b.diem_id=0 "
                                            + "AND n.ma=? LIMIT 1",
                                    new String[]{
                                            String.valueOf(id),
                                            "admin_dd_" + id
                                    }
                            )
            ) {

                if (cursor.moveToFirst()) {

                    tenNguon.setText(
                            s(cursor.getString(0))
                    );

                    urlNguon.setText(
                            s(cursor.getString(1))
                    );
                }
            }

            return true;

        } catch (Exception e) {

            Log.e(TAG, "Không tải được thông tin địa điểm", e);
            thongBao("Không tải được địa điểm");

            return false;
        }
    }

    // =====================================================
    // LUU DIA DIEM
    // =====================================================

    private void luu() {

        String t = value(ten);

        if (t.isEmpty()) {

            ten.setError(
                    "Tên địa điểm không được để trống"
            );

            ten.requestFocus();
            return;
        }

        double lat, lon;

        try {

            lat = Double.parseDouble(value(viDo));
            lon = Double.parseDouble(value(kinhDo));

        } catch (Exception e) {

            thongBao("Hãy chọn hoặc nhập tọa độ");
            return;
        }

        if (!Double.isFinite(lat)
                || !Double.isFinite(lon)
                || lat < -90 || lat > 90
                || lon < -180 || lon > 180) {

            thongBao("Tọa độ không hợp lệ");
            return;
        }

        if (danhMuc.getSelectedItem() == null
                || "Chưa có danh mục".equals(
                danhMuc.getSelectedItem().toString()
        )) {

            thongBao("Chưa có danh mục trong hệ thống");
            return;
        }

        String nguonTen = value(tenNguon);
        String nguonUrl = value(urlNguon);

        if ((!nguonTen.isEmpty() || !nguonUrl.isEmpty())
                && (nguonTen.isEmpty()
                || !nguonUrl.matches("(?i)^https?://.+"))) {

            thongBao("Nguồn cần có tên và URL hợp lệ");
            return;
        }

        String newName = anhHienTai;
        String tenDrawable = value(tenAnhDrawable);

        // NEU CHON ANH TU MAY, SE SAO CHEP KHI LUU
        if (anhMoi == null && !tenDrawable.isEmpty()) {

            int resId = timAnhDrawable(tenDrawable);

            if (resId == 0) {

                tenAnhDrawable.setError(
                        "Ảnh không tồn tại trong drawable"
                );

                tenAnhDrawable.requestFocus();
                return;
            }

            newName = tenDrawable;
        }

        try (AppDatabase helper = new AppDatabase(this)) {

            SQLiteDatabase db =
                    helper.getWritableDatabase();

            PhienDangNhap.yeuCauQuanTri(this, db);

            int targetId = id;

            if (anhMoi != null) {

                newName = AnhDiaDiem.luuTuMay(
                        this, anhMoi
                );
            }

            ContentValues values = new ContentValues();

            values.put("ten", t);
            values.put("dia_chi", value(diaChi));
            values.put(
                    "danh_muc",
                    danhMuc.getSelectedItem().toString()
            );

            values.put("mo_ta", value(moTa));
            values.put("vi_do", lat);
            values.put("kinh_do", lon);
            values.put("ten_anh", newName);

            boolean saved =
                    KhoDuLieuThamQuan.luuKemNguon(
                            this,
                            db,
                            true,
                            targetId,
                            0,
                            values,
                            nguonTen,
                            nguonUrl
                    );

            if (saved) {

                thongBao(
                        getString(
                                R.string.luu_dia_diem_thanh_cong
                        )
                );

                dongBieuMau();
                taiDuLieu();

            } else {

                thongBao("Không thể lưu địa điểm");
            }

        } catch (SecurityException e) {

            thongBao("Không có quyền quản trị");
            finish();

        } catch (Exception e) {

            Log.e(TAG, "Không lưu được địa điểm", e);
            thongBao("Lỗi lưu địa điểm hoặc ảnh");
        }
    }

    // =====================================================
    // XOA DIA DIEM
    // =====================================================

    private void xacNhanXoa() {

        if (id < 0) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa địa điểm?")
                .setMessage(
                        "Hệ thống chỉ cho xóa khi địa điểm "
                                + "không còn dữ liệu tham quan, "
                                + "yêu thích, lịch sử hoặc liên kết khác."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> xoa()
                )
                .show();
    }

    private void xoa() {

        try (AppDatabase helper = new AppDatabase(this)) {

            boolean ok =
                    KhoDuLieuThamQuan.xoaDiaDiem(
                            this,
                            helper.getWritableDatabase(),
                            id
                    );

            if (ok) {

                thongBao("Đã xóa địa điểm");

                dongBieuMau();
                taiDuLieu();

            } else {

                new MaterialAlertDialogBuilder(this)
                        .setTitle("Không thể xóa")
                        .setMessage(
                                "Địa điểm còn dữ liệu liên quan "
                                        + "hoặc được hệ thống sử dụng. "
                                        + "Không tự động xóa dữ liệu liên kết."
                        )
                        .setPositiveButton(
                                "Đã hiểu", null
                        )
                        .show();
            }

        } catch (Exception e) {

            Log.e(TAG, "Không xóa được địa điểm", e);
            thongBao("Lỗi xóa địa điểm");
        }
    }

    // =====================================================
    // CAC HAM HO TRO
    // =====================================================

    private int dp(int value) {

        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    private String value(EditText editText) {

        return editText.getText()
                .toString()
                .trim();
    }

    private void thongBao(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private String s(String value) {

        return value == null ? "" : value;
    }
}
