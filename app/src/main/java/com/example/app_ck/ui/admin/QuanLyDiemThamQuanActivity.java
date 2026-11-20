package com.example.app_ck.ui.admin;

import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.AnhDiaDiem;
import androidx.activity.OnBackPressedCallback;
import com.example.app_ck.util.VanBanTimKiem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * UC08.5 - UC08.7: Quan ly diem tham quan.
 *
 * Chuc nang:
 * - Chon dia diem du lich chinh
 * - Danh sach, tim kiem, them/sua/xoa diem thanh phan
 * - Su dung so do da luu trong SQLite theo dia diem va tang/khu vuc
 * - Hien thi anh RIENG cho tung diem thanh phan, khong trung voi anh so do
 * - Gan nguon thong tin RIENG vao bai_nguon/nguon_thong_tin
 * - Cham tren so do de dat Marker X/Y va phong to thu nho
 *
 * Du lieu van hanh dung SQLite chung.
 * Khong ghi de cau hinh GPS AUTO khi sua diem.
 */
public class QuanLyDiemThamQuanActivity
        extends BaseActivity {

    private static final String TAG = "APP_CK_ADMIN_DIEM";

    private static final int MAU_XANH =
            Color.rgb(44, 115, 81);

    private static final int MAU_CHU =
            Color.rgb(32, 56, 42);

    private static final int MAU_PHU =
            Color.rgb(113, 130, 119);

    // =====================================================
    // DU LIEU
    // =====================================================

    private final ArrayList<DiaDiem> cacDiaDiem =
            new ArrayList<>();

    private final ArrayList<DiemThamQuan> cacDiem =
            new ArrayList<>();

    private View khungDanhSach;
    private LinearLayout danhSach;

    private View khungBieuMau;
    private Spinner chonDiaDiem;

    private TextView soLuong;
    private TextView tieuDe;
    private TextView tieuDeBieuMau;

    private EditText timKiem;
    private EditText ten;
    private EditText tang;
    private EditText thuTu;
    private EditText soHienThi;
    private EditText moTa;
    private EditText thuyetMinh;
    private EditText tenAnh;
    private EditText x;
    private EditText y;

    private Button xoa;

    private int idDiaDiem = -1;
    private int idDangSua = -1;

    private boolean doiSpinner = false;

    // Anh RIENG diem thanh phan. Khong phai anh so do.
    private ImageView anhDiemXemTruoc;
    private EditText oTenNguonDiem, oUrlNguonDiem;
    private Uri anhDiemMoi;
    private String anhDiemDaLuu = "";
    private String trangThaiBieuMauBanDau = "";
    private int viTriDanhSach;
    private String loiTaiDiem;
    private final java.util.Map<Integer, String> tenCacTang = new java.util.LinkedHashMap<>();
    private final ActivityResultLauncher<String[]> chonAnhDiem =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) return;
                        getContentResolver().takePersistableUriPermission(uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        anhDiemMoi = uri;
                        tenAnh.setText("");
                        hienAnhDiemDangChon();
                    }
            );


    // =====================================================
    // ON CREATE
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!coQuyenAdmin()) {
            finish();
            return;
        }

        setContentView(
                R.layout.activity_quan_ly_diem_tham_quan
        );

        apDungKhoangTrongHeThong();
        AdminGiaoDien.hoTroBanPhim(this);

        anhXa();
        QuanLySoDo quanLySoDo=new QuanLySoDo(this);
        findViewById(R.id.dtQuanLySoDo).setOnClickListener(v->quanLySoDo.mo(idDiaDiem,this::taiDiem));

        taoKhuVucAnhVaNguonDiem();
        AdminGiaoDien.apDung(this);
        // Quay lai
        findViewById(R.id.dtQuayLai)
                .setOnClickListener(v -> {

                    if (khungBieuMau.getVisibility()
                            == View.VISIBLE) {
                        hoiTruocKhiDongBieuMau();
                    } else {
                        finish();
                    }
                });

        // Them moi
        findViewById(R.id.dtThemMoi)
                .setOnClickListener(
                        v -> moBieuMau(null)
                );

        // Huy
        findViewById(R.id.dtHuy)
                .setOnClickListener(
                        v -> hoiTruocKhiDongBieuMau()
                );

        // Luu diem
        findViewById(R.id.dtLuu)
                .setOnClickListener(v -> luu());

        // Cham dat Marker
        findViewById(R.id.dtChonMarker)
                .setOnClickListener(
                        v -> moSoDoChonMarker()
                );

        // Xoa diem
        findViewById(R.id.dtXoa)
                .setOnClickListener(
                        v -> xacNhanXoa()
                );

        // Tim kiem
        timKiem.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        veDanhSach();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable e
                    ) {}
                }
        );
        View nutXoaTim = findViewById(R.id.dtXoaTim);

        nutXoaTim.setOnClickListener(
                v -> timKiem.setText("")
        );

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

                nutXoaTim.setVisibility(
                        s.length() == 0
                                ? View.GONE
                                : View.VISIBLE
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // Chon dia diem chinh
        chonDiaDiem.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long itemId
                    ) {
                        if (doiSpinner
                                || position < 0
                                || position >= cacDiaDiem.size()) {
                            return;
                        }

                        idDiaDiem =
                                cacDiaDiem.get(position).id;

                        taiDiem();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {}
                }
        );

        if (savedInstanceState != null) idDiaDiem = savedInstanceState.getInt("admin_place", -1);
        taiDiaDiem();
        if (savedInstanceState != null && savedInstanceState.getBoolean("admin_form")) {
            int pointId = savedInstanceState.getInt("admin_point", -1);
            DiemThamQuan point = null;
            for (DiemThamQuan candidate : cacDiem) if (candidate.id == pointId) point = candidate;
            if (pointId < 0 || point != null) {
                moBieuMau(point);
                String[] values = savedInstanceState.getStringArray("admin_values");
                EditText[] fields = cacTruong();
                if (values != null) for (int i = 0; i < fields.length; i++) fields[i].setText(values[i]);
                String uri = savedInstanceState.getString("admin_image");
                anhDiemMoi = uri == null ? null : Uri.parse(uri);
                trangThaiBieuMauBanDau = savedInstanceState.getString("admin_initial", layTrangThaiBieuMau());
                viTriDanhSach = savedInstanceState.getInt("admin_scroll", 0);
                if (savedInstanceState.getBoolean("admin_advanced")) {
                    findViewById(R.id.dtAnhNangCao).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.dtMoAnhNangCao)).setText("Ẩn tùy chọn ảnh nâng cao  ▴");
                }
                hienAnhDiemDangChon();
            }
        }
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (khungBieuMau != null
                                && khungBieuMau.getVisibility() == View.VISIBLE) {

                            hoiTruocKhiDongBieuMau();

                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!coQuyenAdmin()) {
            finish();
            return;
        }

        if (danhSach != null
                && khungBieuMau != null
                && khungBieuMau.getVisibility()
                != View.VISIBLE) {
            taiDiem();
        }
    }

    // =====================================================
    // ANH XA
    // =====================================================

    private void anhXa() {

        khungDanhSach =
                findViewById(R.id.dtCuonDanhSach);

        // An/hien ca vung nhap va thanh Luu co dinh
        khungBieuMau =
                findViewById(R.id.dtManBieuMau);

        danhSach =
                findViewById(R.id.dtDanhSach);

        chonDiaDiem =
                findViewById(R.id.dtChonDiaDiem);

        soLuong =
                findViewById(R.id.dtSoLuong);

        tieuDe =
                findViewById(R.id.dtTieuDe);

        tieuDeBieuMau =
                findViewById(R.id.dtTieuDeBieuMau);

        timKiem =
                findViewById(R.id.dtTimKiem);

        ten =
                findViewById(R.id.dtTen);

        tang =
                findViewById(R.id.dtTang);

        thuTu =
                findViewById(R.id.dtThuTu);

        soHienThi =
                findViewById(R.id.dtSoHienThi);

        moTa =
                findViewById(R.id.dtMoTa);

        thuyetMinh =
                findViewById(R.id.dtThuyetMinh);

        tenAnh =
                findViewById(R.id.dtTenAnh);

        x =
                findViewById(R.id.dtX);

        y =
                findViewById(R.id.dtY);

        xoa =
                findViewById(R.id.dtXoa);
    }

    // =====================================================
    // KIEM TRA ADMIN
    // =====================================================

    private boolean coQuyenAdmin() {

        try (AppDatabase helper =
                     new AppDatabase(this)) {

            PhienDangNhap.yeuCauQuanTri(
                    this,
                    helper.getReadableDatabase()
            );

            return true;

        } catch (Exception e) {

            Log.e(TAG, "Khong co quyen ADMIN", e);

            thongBao(
                    "Không thể xác minh quyền ADMIN"
            );

            return false;
        }
    }

    // =====================================================
    // TAI DIA DIEM CHINH
    // =====================================================

    private void taiDiaDiem() {

        try (AppDatabase helper =
                     new AppDatabase(this)) {

            cacDiaDiem.clear();

            cacDiaDiem.addAll(
                    helper.danhSachDiaDiem()
            );

            ArrayList<String> danhSachTen =
                    new ArrayList<>();

            int chon = 0;

            for (int i = 0; i < cacDiaDiem.size(); i++) {

                DiaDiem d = cacDiaDiem.get(i);

                danhSachTen.add(d.ten);

                if (d.id == idDiaDiem) {
                    chon = i;
                }
            }

            doiSpinner = true;

            ArrayAdapter<String> adapter = AdminGiaoDien.adapter(this, danhSachTen);

            chonDiaDiem.setAdapter(adapter);

            if (!cacDiaDiem.isEmpty()) {

                chonDiaDiem.setSelection(chon);

                idDiaDiem =
                        cacDiaDiem.get(chon).id;

            } else {

                idDiaDiem = -1;
            }

            doiSpinner = false;

            taiDiem();

        } catch (Exception e) {

            Log.e(TAG, "Loi tai dia diem", e);

            thongBao(
                    "Không tải được địa điểm du lịch"
            );
        }
    }

    // =====================================================
    // TAI DIEM THAM QUAN
    // =====================================================

    private void taiDiem() {

        if (danhSach == null) {
            return;
        }

        cacDiem.clear();
        tenCacTang.clear();
        loiTaiDiem = null;

        if (idDiaDiem >= 0) {

            try (AppDatabase helper =
                         new AppDatabase(this)) {

                cacDiem.addAll(
                        KhoDuLieuThamQuan.danhSach(
                                helper.getReadableDatabase(),
                                idDiaDiem
                        )
                );
                tenCacTang.putAll(KhoDuLieuThamQuan.cacTang(helper.getReadableDatabase(), idDiaDiem));

            } catch (Exception e) {

                Log.e(TAG, "Loi tai diem", e);
                loiTaiDiem = "Không tải được điểm tham quan. Chạm để thử lại.";

                thongBao(
                        "Không tải được điểm tham quan"
                );
            }
        }

        veDanhSach();
    }

    // =====================================================
    // HIEN DANH SACH DIEM
    // =====================================================


    private void veDanhSach() {

        if (danhSach == null) return;

        danhSach.removeAllViews();
        ScrollView scroll = findViewById(R.id.dtCuonDanhSach);
        int oldScroll = scroll.getScrollY();

        String tuKhoa = chuanHoaTim(val(timKiem));

        int dem = 0;

        for (DiemThamQuan d : cacDiem) {

            String chuoiTim = chuanHoaTim(
                    (d.ten == null ? "" : d.ten)
                            + " " + d.id
                            + " " + d.tang + " " + tenTang(d.tang)
                            + " " + d.soHienThi
            );

            if (!chuoiTim.contains(tuKhoa)) {
                continue;
            }

            dem++;

            // Khung the
            LinearLayout the = new LinearLayout(this);

            the.setOrientation(LinearLayout.HORIZONTAL);
            the.setGravity(Gravity.CENTER_VERTICAL);
            the.setPadding(
                    dp(9), dp(10), dp(12), dp(10)
            );

            the.setMinimumHeight(dp(82));
            the.setBackgroundResource(R.drawable.ui_ct_action_bg);
            the.setClickable(true);
            the.setFocusable(true);
            AdminGiaoDien.hieuUngNhan(the);

            the.setContentDescription(
                    "Chỉnh sửa điểm " + d.ten
            );

            LinearLayout.LayoutParams lpThe =
                    new LinearLayout.LayoutParams(-1, -2);

            lpThe.bottomMargin = dp(9);

            // Anh diem hoac so marker
            if (d.tenAnh != null
                    && !d.tenAnh.trim().isEmpty()
                    && !AnhDiaDiem.KHONG_ANH.equals(d.tenAnh)) {

                ImageView hinh = new ImageView(this);

                hinh.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

                hinh.setBackgroundResource(
                        R.drawable.ui_ct_thumb
                );

                hinh.setClipToOutline(true);

                AnhDiaDiem.hien(hinh, d.tenAnh);

                the.addView(
                        hinh,
                        new LinearLayout.LayoutParams(
                                dp(58), dp(58)
                        )
                );

            } else {

                TextView ma = new TextView(this);

                int so = d.soHienThi > 0
                        ? d.soHienThi
                        : d.id;

                ma.setText(
                        String.format(Locale.ROOT, "%02d", so)
                );

                ma.setTextSize(16);
                ma.setTextColor(MAU_XANH);
                ma.setGravity(Gravity.CENTER);

                ma.setTypeface(
                        android.graphics.Typeface.create(
                                "sans-serif-medium",
                                android.graphics.Typeface.NORMAL
                        )
                );

                ma.setBackgroundResource(
                        R.drawable.ui_ct_audio_bg
                );

                the.addView(
                        ma,
                        new LinearLayout.LayoutParams(
                                dp(58), dp(58)
                        )
                );
            }

            // Noi dung the
            LinearLayout thongTin = new LinearLayout(this);

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

            TextView tenDiem = new TextView(this);

            tenDiem.setText(d.ten);
            tenDiem.setTextSize(14.5f);
            tenDiem.setTextColor(
                    Color.parseColor("#182D22")
            );

            tenDiem.setTypeface(
                    android.graphics.Typeface.create(
                            "sans-serif-medium",
                            android.graphics.Typeface.NORMAL
                    )
            );


            thongTin.addView(tenDiem);

            TextView thongTinPhu = new TextView(this);

            thongTinPhu.setText(
                    "Mã " + d.id
                            + " · " + tenTang(d.tang)
            );

            thongTinPhu.setTextSize(12);
            thongTinPhu.setTextColor(
                    Color.parseColor("#687B6E")
            );

            thongTinPhu.setPadding(
                    0, dp(3), 0, 0
            );

            thongTin.addView(thongTinPhu);

            TextView trangThai = new TextView(this);

            boolean coThuyetMinh =
                    d.noiDung != null
                            && !d.noiDung.trim().isEmpty();

            trangThai.setText(
                    (d.coMarker()
                            ? "● Có marker"
                            : "○ Chưa có marker")
                            + " · "
                            + (coThuyetMinh
                            ? "Có bài nghe"
                            : "Chưa có bài nghe")
            );

            trangThai.setTextSize(12);

            trangThai.setTextColor(
                    d.coMarker()
                            ? MAU_XANH
                            : Color.parseColor("#687B6E")
            );

            trangThai.setPadding(
                    0, dp(4), 0, 0
            );

            thongTin.addView(trangThai);

            // Mui ten
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

            the.setOnClickListener(
                    v -> moBieuMau(d)
            );

            danhSach.addView(the, lpThe);
        }

        // Cap nhat ket qua tim kiem
        soLuong.setText(
                tuKhoa.isEmpty()
                        ? cacDiem.size() + " điểm tham quan"
                        : dem + "/" + cacDiem.size()
                        + " điểm phù hợp"
        );

        if (dem == 0) {

            TextView rong = new TextView(this);

            rong.setText(
                    loiTaiDiem != null ? loiTaiDiem : cacDiaDiem.isEmpty()
                            ? "Chưa có địa điểm du lịch"
                            : tuKhoa.isEmpty()
                            ? "Chưa có điểm tham quan.\n"
                            + "Nhấn Thêm điểm để bắt đầu."
                            : "Không tìm thấy điểm phù hợp."
            );

            rong.setTextColor(
                    Color.parseColor("#687B6E")
            );

            rong.setTextSize(14);
            rong.setGravity(Gravity.CENTER);

            rong.setPadding(
                    dp(16), dp(40),
                    dp(16), dp(40)
            );

            danhSach.addView(rong);
            if (loiTaiDiem != null) {
                rong.setOnClickListener(v -> taiDiem());
                rong.setMinimumHeight(dp(48));
                AdminGiaoDien.hieuUngNhan(rong);
            }
        }
        scroll.post(() -> scroll.scrollTo(0, oldScroll));
    }

    // =====================================================
    // MO BIEU MAU THEM / SUA
    // =====================================================

    private void moBieuMau(DiemThamQuan d) {

        if (idDiaDiem < 0) {
            thongBao(
                    "Cần tạo địa điểm du lịch chính trước"
            );
            return;
        }

        idDangSua = d == null ? -1 : d.id;
        viTriDanhSach = ((ScrollView) findViewById(R.id.dtCuonDanhSach)).getScrollY();
        for (EditText field : cacTruong()) field.setError(null);
        findViewById(R.id.dtAnhNangCao).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.dtMoAnhNangCao)).setText("Ảnh có sẵn trong dự án  ▾");

        tieuDe.setText(
                d == null
                        ? "Thêm điểm tham quan"
                        : "Chỉnh sửa điểm tham quan"
        );

        tieuDeBieuMau.setText(
                d == null
                        ? "Thêm điểm tham quan"
                        : "Chỉnh sửa · Mã " + d.id
        );

        ten.setText(d == null ? "" : d.ten);

        tang.setText(
                d == null
                        ? "0"
                        : String.valueOf(d.tang)
        );

        thuTu.setText(
                d == null
                        ? String.valueOf(cacDiem.size() + 1)
                        : String.valueOf(d.thuTu)
        );

        soHienThi.setText(
                d == null
                        ? "0"
                        : String.valueOf(d.soHienThi)
        );

        moTa.setText(
                d == null ? "" : d.moTa
        );

        thuyetMinh.setText(
                d == null ? "" : d.noiDung
        );

        anhDiemMoi = null;
        anhDiemDaLuu = d == null || d.tenAnh == null ? "" : d.tenAnh;
        tenAnh.setText(anhDiemDaLuu.startsWith("anh_may:") ? "" : anhDiemDaLuu);
        hienAnhDiemDangChon();
        taiNguonRiengCuaDiem(d == null ? -1 : d.id);

        x.setText(
                d == null || !d.coMarker()
                        ? ""
                        : String.valueOf(d.x)
        );

        y.setText(
                d == null || !d.coMarker()
                        ? ""
                        : String.valueOf(d.y)
        );

        xoa.setVisibility(
                d == null ? View.GONE : View.VISIBLE
        );

        khungDanhSach.setVisibility(View.GONE);
        khungBieuMau.setVisibility(View.VISIBLE);
        trangThaiBieuMauBanDau = layTrangThaiBieuMau();

        ScrollView vungCuon = findViewById(
                R.id.dtKhungBieuMau
        );

        vungCuon.post(() -> vungCuon.scrollTo(0, 0));
    }

    private void hienDanhSach() {
        AdminGiaoDien.anBanPhim(this);

        khungBieuMau.setVisibility(View.GONE);

        khungDanhSach.setVisibility(View.VISIBLE);

        tieuDe.setText(
                "Điểm tham quan"
        );

        idDangSua = -1;

        taiDiem();
        ScrollView scroll = findViewById(R.id.dtCuonDanhSach);
        scroll.post(() -> scroll.scrollTo(0, viTriDanhSach));
    }



    // =====================================================
    // LUU DIEM THAM QUAN
    // =====================================================

    private void luu() {

        if (!coQuyenAdmin()) {
            finish();
            return;
        }

        String tenMoi = val(ten);

        if (tenMoi.isEmpty()) {

            ten.setError(
                    "Vui lòng nhập tên điểm tham quan"
            );

            ten.requestFocus();
            return;
        }

        Integer tangMoi = docSoNguyen(tang, "Tầng/khu vực");
        if (tangMoi == null) return;
        Integer thuTuMoi = docSoNguyen(thuTu, "Thứ tự");
        if (thuTuMoi == null) return;
        Integer soMoi = docSoNguyen(soHienThi, "Số marker");
        if (soMoi == null) return;

        boolean coX = !val(x).isEmpty();
        boolean coY = !val(y).isEmpty();

        if (coX != coY) {

            EditText missing = coX ? y : x;
            missing.setError("Nhập cả X và Y hoặc để trống cả hai");
            missing.requestFocus();

            return;
        }

        Double xx = null;
        Double yy = null;

        if (coX) {
            xx = docTyLeSoDo(x);
            if (xx == null) return;
            yy = docTyLeSoDo(y);
            if (yy == null) return;
        }

        String tenNguonMoi = val(oTenNguonDiem);
        String urlNguonMoi = val(oUrlNguonDiem);
        if (!tenNguonMoi.isEmpty() || !urlNguonMoi.isEmpty()) {
            if (tenNguonMoi.isEmpty()) {
                oTenNguonDiem.setError("Nhập tên nguồn tham khảo");
                oTenNguonDiem.requestFocus();
                return;
            }
            if (!urlNguonMoi.matches("(?i)^https?://[^\\s]+$")) {
                oUrlNguonDiem.setError("Nhập URL http(s) hợp lệ");
                oUrlNguonDiem.requestFocus();
                return;
            }
        }

        String tenAnhMoi = anhDiemDaLuu;
        if (anhDiemMoi == null && !val(tenAnh).isEmpty()) {
            tenAnhMoi = val(tenAnh);
        }
        // Truong hop anh moi tu may duoc sao chep truoc khi ghi SQLite.


        // Ho tro ten drawable hoac anh_may: da luu.
        if (anhDiemMoi == null
                && !tenAnhMoi.isEmpty()
                && !tenAnhMoi.startsWith("anh_may:")
                && !AnhDiaDiem.KHONG_ANH.equals(tenAnhMoi)) {

            int anhId = getResources().getIdentifier(
                    tenAnhMoi,
                    "drawable",
                    getPackageName()
            );

            if (anhId == 0) {
                findViewById(R.id.dtAnhNangCao).setVisibility(View.VISIBLE);
                tenAnh.setError(
                        "Không tìm thấy ảnh trong drawable"
                );
                tenAnh.requestFocus();
                return;
            }
        }

        ContentValues v = new ContentValues();

        v.put("ten", tenMoi);
        v.put("tang", tangMoi);
        v.put("thu_tu", thuTuMoi);
        v.put("so_hien_thi", soMoi);

        v.put("mo_ta", val(moTa));

        v.put(
                "noi_dung_thuyet_minh",
                val(thuyetMinh)
        );

        v.put("ten_anh", tenAnhMoi);

        if (xx == null) {

            v.putNull("x_ty_le");
            v.putNull("y_ty_le");

        } else {

            v.put("x_ty_le", xx);
            v.put("y_ty_le", yy);
        }

        try (AppDatabase helper =
                     new AppDatabase(this)) {

            SQLiteDatabase db =
                    helper.getWritableDatabase();

            int maDiem = idDangSua;

            if (anhDiemMoi != null) {
                try {
                    tenAnhMoi = AnhDiaDiem.luuTuMay(this, anhDiemMoi);
                    v.put("ten_anh", tenAnhMoi);
                } catch (Exception e) {
                    Log.e(TAG, "Khong sao chep duoc anh diem", e);
                    thongBao("Không lưu được ảnh điểm thành phần");
                    return;
                }
            }

            boolean thanhCong = KhoDuLieuThamQuan.luuKemNguon(
                    this, db, false, idDiaDiem, maDiem, v, tenNguonMoi, urlNguonMoi);

            if (thanhCong) {
                thongBao(getString(R.string.luu_diem_thanh_cong));
                hienDanhSach();

            } else {

                thongBao(
                        "Không thể lưu điểm tham quan"
                );
            }

        } catch (SecurityException e) {

            thongBao(
                    "Bạn không có quyền quản trị"
            );

            finish();

        } catch (Exception e) {

            Log.e(TAG, "Loi luu diem", e);

            thongBao(
                    "Không thể lưu điểm tham quan"
            );
        }
    }

    // =====================================================
    // ANH RIENG + NGUON RIENG CUA DIEM THANH PHAN
    // =====================================================


    private void taoKhuVucAnhVaNguonDiem() {

        anhDiemXemTruoc = findViewById(
                R.id.dtAnhDiemXemTruoc
        );

        oTenNguonDiem = findViewById(
                R.id.dtNguonTenDiem
        );

        oUrlNguonDiem = findViewById(
                R.id.dtNguonUrlDiem
        );

        View khungNangCao = findViewById(
                R.id.dtAnhNangCao
        );

        TextView nutNangCao = findViewById(
                R.id.dtMoAnhNangCao
        );

        // Chon anh rieng tu dien thoai

        findViewById(R.id.dtChonAnhDiem)
                .setOnClickListener(
                        v -> chonAnhDiem.launch(new String[]{"image/*"})
                );

        anhDiemXemTruoc.setOnClickListener(
                v -> chonAnhDiem.launch(new String[]{"image/*"})
        );

        anhDiemXemTruoc.setClickable(true);
        anhDiemXemTruoc.setFocusable(true);

        // Bo anh cua diem, khong bo anh so do

        findViewById(R.id.dtBoAnhDiem)
                .setOnClickListener(v -> {

                    anhDiemMoi = null;

                    tenAnh.setText(
                            AnhDiaDiem.KHONG_ANH
                    );

                    hienAnhDiemDangChon();
                });

        // Tuy chon drawable nang cao

        nutNangCao.setOnClickListener(v -> {

            boolean mo =
                    khungNangCao.getVisibility()
                            == View.GONE;

            khungNangCao.setVisibility(
                    mo ? View.VISIBLE : View.GONE
            );

            nutNangCao.setText(
                    mo
                            ? "Ẩn tùy chọn ảnh nâng cao  ▴"
                            : "Ảnh có sẵn trong dự án  ▾"
            );
        });

        // Xem truoc drawable

        findViewById(R.id.dtXemAnhDrawable)
                .setOnClickListener(v -> {

                    String ma = val(tenAnh);

                    if (!ma.matches("[a-z][a-z0-9_]*")) {

                        tenAnh.setError(
                                "Tên drawable không hợp lệ"
                        );

                        tenAnh.requestFocus();
                        return;
                    }

                    int resId = getResources().getIdentifier(
                            ma,
                            "drawable",
                            getPackageName()
                    );

                    if (resId == 0) {

                        tenAnh.setError(
                                "Không tìm thấy ảnh trong drawable"
                        );

                        tenAnh.requestFocus();
                        return;
                    }

                    anhDiemMoi = null;
                    tenAnh.setError(null);

                    anhDiemXemTruoc.setPadding(0, 0, 0, 0);

                    anhDiemXemTruoc.setScaleType(
                            ImageView.ScaleType.CENTER_CROP
                    );

                    anhDiemXemTruoc.setImageResource(resId);
                });
    }


    private void hienAnhDiemDangChon() {

        if (anhDiemXemTruoc == null) {
            return;
        }

        // Anh vua chon tu thiet bi

        if (anhDiemMoi != null) {

            anhDiemXemTruoc.clearColorFilter();

            anhDiemXemTruoc.setPadding(0, 0, 0, 0);

            anhDiemXemTruoc.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            anhDiemXemTruoc.setImageURI(anhDiemMoi);

            return;
        }

        String ma = val(tenAnh);

        if (ma.isEmpty()) {
            ma = anhDiemDaLuu;
        }

        // Chua co anh hoac da chon bo anh

        if (ma == null
                || ma.isEmpty()
                || AnhDiaDiem.KHONG_ANH.equals(ma)) {

            anhDiemXemTruoc.setScaleType(
                    ImageView.ScaleType.CENTER_INSIDE
            );

            anhDiemXemTruoc.setPadding(
                    dp(22), dp(22), dp(22), dp(22)
            );

            anhDiemXemTruoc.setColorFilter(
                    Color.parseColor("#8DAA98")
            );

            anhDiemXemTruoc.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            return;
        }

        // Anh da luu / anh drawable

        anhDiemXemTruoc.clearColorFilter();

        anhDiemXemTruoc.setPadding(0, 0, 0, 0);

        anhDiemXemTruoc.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        AnhDiaDiem.hien(
                anhDiemXemTruoc,
                ma
        );
    }

    private String maNguonDiem(int diaDiemId, int diemId) {
        return "admin_diem_" + diaDiemId + "_" + diemId;
    }

    private void taiNguonRiengCuaDiem(int diemId) {
        if (oTenNguonDiem == null || oUrlNguonDiem == null) return;
        oTenNguonDiem.setText("");
        oUrlNguonDiem.setText("");
        if (diemId <= 0) return;
        try (AppDatabase helper = new AppDatabase(this);
             Cursor c = helper.getReadableDatabase().rawQuery(
                     "SELECT n.ten,n.url FROM nguon_thong_tin n "
                             + "JOIN bai_nguon b ON b.ma_nguon=n.ma "
                             + "WHERE b.dia_diem_id=? AND b.diem_id=? AND n.ma=? LIMIT 1",
                     new String[]{String.valueOf(idDiaDiem), String.valueOf(diemId),
                             maNguonDiem(idDiaDiem, diemId)})) {
            if (c.moveToFirst()) {
                oTenNguonDiem.setText(c.getString(0));
                oUrlNguonDiem.setText(c.getString(1));
            }
        } catch (Exception e) {
            Log.e(TAG, "Loi tai nguon diem", e);
            thongBao("Không tải được nguồn riêng của điểm");
        }
    }

    // =====================================================
    // DOC ANH SO DO
    // =====================================================

    private Bitmap docAnhSoDo(String maAnh) {

        if (maAnh == null
                || maAnh.trim().isEmpty()) {
            return null;
        }

        try {

            if (maAnh.startsWith("anh_may:")) {

                String tenFile =
                        maAnh.substring(
                                "anh_may:".length()
                        );

                if (tenFile.isEmpty()
                        || tenFile.contains("/")
                        || tenFile.contains("\\")
                        || tenFile.contains("..")) {
                    return null;
                }

                File file = new File(
                        new File(
                                getFilesDir(),
                                "anh_dia_diem"
                        ),
                        tenFile
                );

                if (!file.isFile()) {
                    return null;
                }

                return BitmapFactory.decodeFile(
                        file.getAbsolutePath()
                );
            }

            if (!maAnh.matches(
                    "[a-z][a-z0-9_]*"
            )) {
                return null;
            }

            int resId =
                    getResources().getIdentifier(
                            maAnh,
                            "drawable",
                            getPackageName()
                    );

            if (resId == 0) {
                return null;
            }

            return BitmapFactory.decodeResource(
                    getResources(),
                    resId
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Loi doc anh so do",
                    e
            );

            return null;
        }
    }

    // =====================================================
    // CHAM TREN SO DO DE DAT MARKER
    // =====================================================

    private void moSoDoChonMarker() {

        if (!coQuyenAdmin()) {
            finish();
            return;
        }

        if (idDiaDiem < 0) {

            thongBao(
                    "Chưa chọn địa điểm chính"
            );

            return;
        }

        int tangChon;

        try {

            tangChon = Integer.parseInt(
                    val(tang)
            );

        } catch (NumberFormatException e) {

            tang.setError(
                    "Nhập tầng/khu vực hợp lệ"
            );

            return;
        }

        String maAnh;

        try (AppDatabase helper =
                     new AppDatabase(this)) {

            maAnh =
                    KhoDuLieuThamQuan.anhSoDo(
                            helper.getReadableDatabase(),
                            idDiaDiem,
                            tangChon
                    );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Khong doc duoc so do",
                    e
            );

            thongBao(
                    "Không tải được sơ đồ tham quan"
            );

            return;
        }

        if (maAnh == null
                || maAnh.trim().isEmpty()) {

            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                    .setTitle(
                            "Chưa có ảnh sơ đồ"
                    )
                    .setMessage(
                            "Tầng/khu " + tangChon
                                    + " chưa có ảnh sơ đồ. "
                                    + "Ảnh sơ đồ chưa được liên kết trong dữ liệu ứng dụng. "
                                    + "Hãy kiểm tra bản ghi so_do_tham_quan cho địa điểm và tầng/khu vực này. "
                                    + "Dữ liệu hiện có không bị xóa."
                    )
                    .setPositiveButton(
                            "Đã hiểu", null
                    )
                    .show();

            return;
        }

        Bitmap bitmap =
                docAnhSoDo(maAnh);

        if (bitmap == null) {

            thongBao(
                    "Không tìm thấy ảnh sơ đồ: "
                            + maAnh
            );

            return;
        }

        SoDoChonMarkerView banDo =
                new SoDoChonMarkerView(
                        this,
                        bitmap
                );

        // Neu Marker da co toa do
        // thi hien thi vi tri cu
        try {

            if (!val(x).isEmpty()
                    && !val(y).isEmpty()) {

                double xx =
                        Double.parseDouble(
                                val(x)
                        );

                double yy =
                        Double.parseDouble(
                                val(y)
                        );

                if (Double.isFinite(xx)
                        && Double.isFinite(yy)
                        && xx >= 0 && xx <= 1
                        && yy >= 0 && yy <= 1) {

                    banDo.setMarker(xx, yy);
                }
            }

        } catch (NumberFormatException ignored) {}

        LinearLayout khung =
                new LinearLayout(this);

        khung.setOrientation(
                LinearLayout.VERTICAL
        );

        khung.setPadding(
                dp(12), dp(8),
                dp(12), dp(8)
        );

        TextView chiDan =
                new TextView(this);

        chiDan.setText(
                "Chạm vào ảnh sơ đồ để đặt Marker. " +
                        "Nhấn Xác nhận để đưa tọa độ về biểu mẫu."
        );

        chiDan.setTextColor(MAU_CHU);
        chiDan.setTextSize(13);

        khung.addView(chiDan);
// Thanh dieu khien phong to / thu nho
        LinearLayout thanhZoom = new LinearLayout(this);
        thanhZoom.setOrientation(LinearLayout.HORIZONTAL);
        thanhZoom.setGravity(Gravity.CENTER);

        Button nutCong = taoNut("+");
        Button nutTru = taoNut("−");
        Button nutDatLai = taoNut("Đặt lại");

        thanhZoom.addView(nutTru,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        thanhZoom.addView(nutDatLai,
                new LinearLayout.LayoutParams(0, dp(48), 2));

        thanhZoom.addView(nutCong,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        khung.addView(thanhZoom);

        nutCong.setOnClickListener(v -> banDo.zoomIn());
        nutTru.setOnClickListener(v -> banDo.zoomOut());
        nutDatLai.setOnClickListener(v -> banDo.resetZoom());
        khung.addView(
                banDo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(Math.max(180, Math.min(360,
                                (int) (getResources().getDisplayMetrics().heightPixels
                                        / getResources().getDisplayMetrics().density) - 320)))
                )
        );

        TextView toaDo =
                new TextView(this);

        toaDo.setTextColor(MAU_XANH);
        toaDo.setTextSize(13);

        toaDo.setPadding(
                0, dp(8), 0, dp(3)
        );

        Runnable capNhat = () -> {

            if (banDo.hasMarker()) {

                toaDo.setText(
                        String.format(
                                Locale.US,
                                "X: %.6f    Y: %.6f",
                                banDo.getXRatio(),
                                banDo.getYRatio()
                        )
                );

            } else {

                toaDo.setText(
                        "Chưa đặt Marker"
                );
            }
        };

        banDo.setOnMarkerChangedListener(
                capNhat
        );

        capNhat.run();

        khung.addView(toaDo);

        ScrollView vungChon = new ScrollView(this);
        vungChon.addView(khung);
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                        .setTitle(
                                "Chọn Marker · Tầng/khu "
                                        + tangChon
                        )
                        .setView(vungChon)
                        .setNegativeButton(
                                "Hủy", null
                        )
                        .setPositiveButton(
                                "Xác nhận", null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                if (!banDo.hasMarker()) {

                    thongBao(
                            "Chưa chọn vị trí trên sơ đồ"
                    );

                    return;
                }

                x.setText(
                        String.format(
                                Locale.US,
                                "%.6f",
                                banDo.getXRatio()
                        )
                );

                y.setText(
                        String.format(
                                Locale.US,
                                "%.6f",
                                banDo.getYRatio()
                        )
                );

                dialog.dismiss();
            });
        });

        dialog.setOnDismissListener(d -> {

            // View khong con su dung Bitmap
            // sau khi hop thoai dong.
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        });

        dialog.show();
    }

    // =====================================================
    // XOA DIEM THAM QUAN
    // =====================================================

    private void xacNhanXoa() {

        if (idDangSua <= 0) {
            return;
        }

        final int maDiem = idDangSua;

        AlertDialog confirm = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                .setTitle(
                        "Xóa điểm tham quan?"
                )
                .setMessage(
                        "Điểm có lịch sử nghe sẽ không được xóa "
                                + "để bảo vệ dữ liệu người dùng. "
                                + "Bạn xác nhận xóa điểm này?"
                )
                .setNegativeButton(
                        "Hủy", null
                )
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {

                            try (AppDatabase helper =
                                         new AppDatabase(this)) {

                                boolean thanhCong =
                                        KhoDuLieuThamQuan.xoaDiem(
                                                this,
                                                helper.getWritableDatabase(),
                                                idDiaDiem,
                                                maDiem
                                        );

                                if (thanhCong) {

                                    thongBao(
                                            "Đã xóa điểm tham quan"
                                    );

                                    hienDanhSach();

                                } else {

                                    thongBao(
                                            "Không thể xóa điểm. "
                                                    + "Có thể điểm đã có lịch sử nghe."
                                    );
                                }

                            } catch (SecurityException e) {

                                thongBao(
                                        "Bạn không có quyền quản trị"
                                );

                                finish();

                            } catch (Exception e) {

                                Log.e(
                                        TAG,
                                        "Loi xoa diem",
                                        e
                                );

                                thongBao(
                                        "Không thể xóa điểm tham quan"
                                );
                            }
                        }
                )
                .show();
        AdminGiaoDien.toMauNguyHiem(confirm);
    }

    // =====================================================
    // HAM HO TRO
    // =====================================================

    private TextView taoNhan(String noiDung) {

        TextView view = new TextView(this);

        view.setText(noiDung);
        view.setTextSize(13);
        view.setTextColor(MAU_XANH);

        view.setPadding(
                0, dp(12), 0, dp(4)
        );

        return view;
    }

    private Button taoNut(String noiDung) {

        com.google.android.material.button.MaterialButton button =
                new com.google.android.material.button.MaterialButton(this);
        button.setCornerRadius(dp(14));
        button.setMinimumHeight(dp(48));
        button.setTextSize(14);

        button.setText(noiDung);
        button.setAllCaps(false);

        button.setTextColor(MAU_XANH);

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.parseColor("#EDF5EC")
                )
        );

        return button;
    }

    private String val(EditText editText) {

        return editText.getText()
                .toString()
                .trim();
    }

    private int dp(int n) {

        return Math.round(
                n * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void thongBao(String noiDung) {

        Toast.makeText(
                this,
                noiDung,
                Toast.LENGTH_SHORT
        ).show();
    }
    private String chuanHoaTim(String chuoi) {
        return VanBanTimKiem.chuanHoa(chuoi);
    }

    private String layTrangThaiBieuMau() {

        return TextUtils.join(
                "\u001F",
                new String[]{
                        ten.getText().toString(), tang.getText().toString(),
                        thuTu.getText().toString(), soHienThi.getText().toString(),
                        moTa.getText().toString(), thuyetMinh.getText().toString(),
                        tenAnh.getText().toString(), x.getText().toString(), y.getText().toString(),
                        oTenNguonDiem.getText().toString(), oUrlNguonDiem.getText().toString(),
                        anhDiemMoi == null
                                ? ""
                                : anhDiemMoi.toString()
                }
        );
    }

    private void hoiTruocKhiDongBieuMau() {

        if (layTrangThaiBieuMau().equals(
                trangThaiBieuMauBanDau
        )) {

            hienDanhSach();
            return;
        }

        AdminGiaoDien.hoiBoThayDoi(this, this::hienDanhSach, null);
    }

    private Integer docSoNguyen(EditText field, String label) {
        try { return Integer.valueOf(val(field)); }
        catch (NumberFormatException e) {
            field.setError(label + " phải là số nguyên");
            field.requestFocus();
            return null;
        }
    }

    private Double docTyLeSoDo(EditText field) {
        try {
            double value = Double.parseDouble(val(field));
            if (!Double.isFinite(value) || value < 0 || value > 1) throw new NumberFormatException();
            return value;
        } catch (NumberFormatException e) {
            field.setError("Tọa độ sơ đồ phải từ 0 đến 1");
            field.requestFocus();
            return null;
        }
    }

    private String tenTang(int floor) {
        String name = tenCacTang.get(floor);
        return name == null || name.trim().isEmpty() ? "Tầng/khu " + floor : name;
    }

    private EditText[] cacTruong() {
        return new EditText[]{ten, tang, thuTu, soHienThi, moTa, thuyetMinh, tenAnh,
                x, y, oTenNguonDiem, oUrlNguonDiem};
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("admin_place", idDiaDiem);
        state.putInt("admin_point", idDangSua);
        state.putBoolean("admin_form", khungBieuMau.getVisibility() == View.VISIBLE);
        String[] values = new String[cacTruong().length];
        for (int i = 0; i < values.length; i++) values[i] = cacTruong()[i].getText().toString();
        state.putStringArray("admin_values", values);
        state.putString("admin_image", anhDiemMoi == null ? null : anhDiemMoi.toString());
        state.putString("admin_initial", trangThaiBieuMauBanDau);
        state.putBoolean("admin_advanced", findViewById(R.id.dtAnhNangCao).getVisibility() == View.VISIBLE);
        state.putInt("admin_scroll", khungBieuMau.getVisibility() == View.VISIBLE
                ? viTriDanhSach : ((ScrollView) findViewById(R.id.dtCuonDanhSach)).getScrollY());
    }
}
