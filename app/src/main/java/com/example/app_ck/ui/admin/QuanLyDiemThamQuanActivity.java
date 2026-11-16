package com.example.app_ck.ui.admin;

import android.content.ContentValues;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.ViewGroup;
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

    private LinearLayout khungDanhSach;
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

    private final ActivityResultLauncher<String> chonAnhDiem =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) return;
                        anhDiemMoi = uri;
                        tenAnh.setText("");
                        anhDiemXemTruoc.setImageURI(uri);
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

        anhXa();
        QuanLySoDo quanLySoDo=new QuanLySoDo(this);
        findViewById(R.id.dtQuanLySoDo).setOnClickListener(v->quanLySoDo.mo(idDiaDiem,this::taiDiem));

        taoKhuVucAnhVaNguonDiem();

        // Quay lai
        findViewById(R.id.dtQuayLai)
                .setOnClickListener(v -> {

                    if (khungBieuMau.getVisibility()
                            == View.VISIBLE) {
                        hienDanhSach();
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
                        v -> hienDanhSach()
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

        taiDiaDiem();
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (khungBieuMau != null
                                && khungBieuMau.getVisibility() == View.VISIBLE) {

                            hienDanhSach();

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
                findViewById(R.id.dtKhungDanhSach);

        khungBieuMau =
                findViewById(R.id.dtKhungBieuMau);

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

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            danhSachTen
                    );

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

        if (idDiaDiem >= 0) {

            try (AppDatabase helper =
                         new AppDatabase(this)) {

                cacDiem.addAll(
                        KhoDuLieuThamQuan.danhSach(
                                helper.getReadableDatabase(),
                                idDiaDiem
                        )
                );

            } catch (Exception e) {

                Log.e(TAG, "Loi tai diem", e);

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

        if (danhSach == null) {
            return;
        }

        danhSach.removeAllViews();

        String filter = val(timKiem)
                .toLowerCase(Locale.ROOT);

        int dem = 0;

        for (DiemThamQuan d : cacDiem) {

            String tenDiem = d.ten == null
                    ? ""
                    : d.ten.toLowerCase(Locale.ROOT);

            if (!tenDiem.contains(filter)
                    && !String.valueOf(d.id)
                    .contains(filter)) {
                continue;
            }

            dem++;

            LinearLayout card =
                    new LinearLayout(this);

            card.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            card.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            card.setPadding(
                    dp(14), dp(12),
                    dp(14), dp(12)
            );

            card.setBackgroundResource(
                    R.drawable.ui_card
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            -1, -2
                    );

            cardParams.bottomMargin = dp(10);

            LinearLayout noiDung =
                    new LinearLayout(this);

            noiDung.setOrientation(
                    LinearLayout.VERTICAL
            );

            TextView nhan = new TextView(this);

            nhan.setText(d.ten);
            nhan.setTextColor(MAU_CHU);
            nhan.setTextSize(15);

            nhan.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

            noiDung.addView(nhan);

            TextView phu = new TextView(this);

            phu.setText(
                    "Mã " + d.id
                            + " · Tầng/khu " + d.tang
                            + (d.coMarker()
                            ? " · Có Marker"
                            : " · Chưa đặt Marker")
            );

            phu.setTextColor(MAU_PHU);
            phu.setTextSize(12);

            noiDung.addView(phu);

            card.addView(
                    noiDung,
                    new LinearLayout.LayoutParams(
                            0, -2, 1
                    )
            );

            TextView muiTen = new TextView(this);

            muiTen.setText("›");
            muiTen.setTextColor(MAU_XANH);
            muiTen.setTextSize(26);

            card.addView(muiTen);

            card.setOnClickListener(
                    v -> moBieuMau(d)
            );

            danhSach.addView(
                    card,
                    cardParams
            );
        }

        soLuong.setText(
                "DANH SÁCH · " + dem + " ĐIỂM"
        );

        if (cacDiaDiem.isEmpty() || dem == 0) {

            TextView trong = new TextView(this);

            trong.setText(
                    cacDiaDiem.isEmpty()
                            ? "Chưa có địa điểm chính"
                            : "Chưa có điểm tham quan phù hợp"
            );

            trong.setGravity(Gravity.CENTER);
            trong.setTextColor(MAU_PHU);

            trong.setPadding(
                    dp(12),
                    dp(40),
                    dp(12),
                    dp(40)
            );

            danhSach.addView(trong);
        }
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

        khungBieuMau.scrollTo(0, 0);
    }

    private void hienDanhSach() {

        khungBieuMau.setVisibility(View.GONE);

        khungDanhSach.setVisibility(View.VISIBLE);

        tieuDe.setText(
                "Quản lý điểm tham quan"
        );

        idDangSua = -1;

        taiDiem();
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

        int tangMoi;
        int thuTuMoi;
        int soMoi;

        try {

            tangMoi = Integer.parseInt(
                    val(tang)
            );

            thuTuMoi = Integer.parseInt(
                    val(thuTu)
            );

            soMoi = Integer.parseInt(
                    val(soHienThi)
            );

        } catch (NumberFormatException e) {

            thongBao(
                    "Tầng, thứ tự và số Marker phải là số nguyên"
            );

            return;
        }

        boolean coX = !val(x).isEmpty();
        boolean coY = !val(y).isEmpty();

        if (coX != coY) {

            thongBao(
                    "Phải nhập cả X và Y hoặc để trống cả hai"
            );

            return;
        }

        Double xx = null;
        Double yy = null;

        if (coX) {

            try {

                xx = Double.parseDouble(
                        val(x)
                );

                yy = Double.parseDouble(
                        val(y)
                );

                if (!Double.isFinite(xx)
                        || !Double.isFinite(yy)
                        || xx < 0 || xx > 1
                        || yy < 0 || yy > 1) {

                    thongBao(
                            "X/Y phải nằm trong khoảng 0–1"
                    );

                    return;
                }

            } catch (NumberFormatException e) {

                thongBao(
                        "Tọa độ Marker không hợp lệ"
                );

                return;
            }
        }

        String tenNguonMoi = val(oTenNguonDiem);
        String urlNguonMoi = val(oUrlNguonDiem);
        if (!tenNguonMoi.isEmpty() || !urlNguonMoi.isEmpty()) {
            if (tenNguonMoi.isEmpty()
                    || !urlNguonMoi.matches("(?i)^https?://[^\\s]+$")) {
                oUrlNguonDiem.setError("Nhập tên nguồn và URL http(s) hợp lệ");
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
        ViewGroup cha = (ViewGroup) tenAnh.getParent();
        if (!(cha instanceof LinearLayout)) {
            Log.e(TAG, "dtTenAnh khong nam trong LinearLayout");
            return;
        }

        LinearLayout layout = (LinearLayout) cha;
        int viTri = layout.indexOfChild(tenAnh) + 1;

        // Nhan ro: o dtTenAnh hien co KHONG phai ten anh so do.
        TextView ghiChu = new TextView(this);
        ghiChu.setText("Ảnh RIÊNG của điểm tham quan. Không nhập tên ảnh sơ đồ tại đây. "
                + "Ví dụ: anh_cong_chinh_hoa_lo (không có .jpg/.png).");
        ghiChu.setTextSize(12);
        ghiChu.setTextColor(MAU_PHU);
        ghiChu.setPadding(0, dp(5), 0, dp(6));
        layout.addView(ghiChu, viTri++);

        anhDiemXemTruoc = new ImageView(this);
        anhDiemXemTruoc.setScaleType(ImageView.ScaleType.CENTER_CROP);
        anhDiemXemTruoc.setBackgroundColor(Color.rgb(239, 244, 239));
        anhDiemXemTruoc.setImageResource(android.R.drawable.ic_menu_gallery);
        LinearLayout.LayoutParams paramsAnh =
                new LinearLayout.LayoutParams(-1, dp(180));
        paramsAnh.topMargin = dp(8);
        paramsAnh.bottomMargin = dp(6);
        layout.addView(anhDiemXemTruoc, viTri++, paramsAnh);

        Button nutXem = new Button(this);
        nutXem.setText("Xem trước ảnh drawable");
        nutXem.setAllCaps(false);
        nutXem.setOnClickListener(v -> {
            String ma = val(tenAnh);
            int idAnh = ma.matches("[a-z][a-z0-9_]*")
                    ? getResources().getIdentifier(ma, "drawable", getPackageName()) : 0;
            if (idAnh == 0) {
                tenAnh.setError("Tên ảnh không tồn tại trong drawable");
                return;
            }
            anhDiemMoi = null;
            anhDiemXemTruoc.setImageResource(idAnh);
        });
        layout.addView(nutXem, viTri++);

        Button nutMay = new Button(this);
        nutMay.setText("Chọn ảnh riêng từ điện thoại");
        nutMay.setAllCaps(false);
        nutMay.setOnClickListener(v -> chonAnhDiem.launch("image/*"));
        layout.addView(nutMay, viTri++);
        Button nutBoAnh = new Button(this);

        nutBoAnh.setText("Bỏ ảnh riêng của điểm");
        nutBoAnh.setAllCaps(false);

        nutBoAnh.setOnClickListener(v -> {

            anhDiemMoi = null;

            tenAnh.setText(
                    AnhDiaDiem.KHONG_ANH
            );

            anhDiemXemTruoc.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );
        });

        layout.addView(nutBoAnh, viTri++);
        TextView nhanNguon = new TextView(this);
        nhanNguon.setText("Nguồn riêng của điểm tham quan");
        nhanNguon.setTextSize(15);
        nhanNguon.setTextColor(MAU_XANH);
        nhanNguon.setPadding(0, dp(18), 0, dp(5));
        layout.addView(nhanNguon, viTri++);

        oTenNguonDiem = new EditText(this);
        oTenNguonDiem.setSingleLine(true);
        oTenNguonDiem.setHint("Tên tài liệu / cơ quan cung cấp");
        layout.addView(oTenNguonDiem, viTri++);

        oUrlNguonDiem = new EditText(this);
        oUrlNguonDiem.setSingleLine(true);
        oUrlNguonDiem.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        oUrlNguonDiem.setHint("https://...");
        layout.addView(oUrlNguonDiem, viTri++);

        TextView nhanGiaiThich = new TextView(this);
        nhanGiaiThich.setText("Nguồn gắn với điểm thành phần này, không thay nguồn của địa điểm chính.");
        nhanGiaiThich.setTextSize(12);
        nhanGiaiThich.setTextColor(MAU_PHU);
        layout.addView(nhanGiaiThich, viTri);
    }

    private void hienAnhDiemDangChon() {
        if (anhDiemXemTruoc == null) return;
        if (anhDiemMoi != null) {
            anhDiemXemTruoc.setImageURI(anhDiemMoi);
            return;
        }
        String ma = val(tenAnh);
        if (ma.isEmpty()) ma = anhDiemDaLuu;
        AnhDiaDiem.hien(anhDiemXemTruoc, ma);
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

            new AlertDialog.Builder(this)
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
                        dp(400)
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

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "Chọn Marker · Tầng/khu "
                                        + tangChon
                        )
                        .setView(khung)
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

        new AlertDialog.Builder(this)
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

        Button button = new Button(this);

        button.setText(noiDung);
        button.setAllCaps(false);

        button.setTextColor(MAU_XANH);

        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        Color.rgb(228, 240, 230)
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
}
