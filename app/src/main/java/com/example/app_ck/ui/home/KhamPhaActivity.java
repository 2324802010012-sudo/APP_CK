package com.example.app_ck.ui.home;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.ThanhDieuHuongDuoi;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.profile.CaNhanActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KhamPhaActivity extends BaseActivity {

    private EditText timKiem;
    private LinearLayout danhSach, danhMuc;
    private View khungNoiBat, trangThai;

    private List<DiaDiem> tatCa = new ArrayList<>();
    private List<DiaDiem> goiY = new ArrayList<>();

    private final Set<Integer> daLuu = new HashSet<>();

    private String locDanhMuc;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_kham_pha);

        ThanhDieuHuongDuoi.gan(
                this,
                ThanhDieuHuongDuoi.KHAM_PHA
        );

        timKiem = findViewById(R.id.kpTimKiem);
        danhSach = findViewById(R.id.kpDanhSach);
        danhMuc = findViewById(R.id.kpDanhMuc);
        khungNoiBat = findViewById(R.id.kpNoiBat);
        trangThai = findViewById(R.id.kpTrangThai);

        if (state != null) {
            locDanhMuc = state.getString("loc_danh_muc");
        }

        // Tim kiem khi nhap.
        timKiem.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start,
                    int count, int after
            ) {}

            @Override
            public void onTextChanged(
                    CharSequence s, int start,
                    int before, int count
            ) {
                hienThi();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        timKiem.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                        return false;
                    }

                    InputMethodManager banPhim =
                            (InputMethodManager)
                                    getSystemService(INPUT_METHOD_SERVICE);

                    if (banPhim != null) {
                        banPhim.hideSoftInputFromWindow(
                                timKiem.getWindowToken(),
                                0
                        );
                    }

                    timKiem.clearFocus();

                    return true;
                }
        );

        // Mo bo loc danh muc.
        findViewById(R.id.kpNutLoc)
                .setOnClickListener(v -> moBoLoc());

        // Xoa tim kiem va bo loc.
        findViewById(R.id.kpThuLai)
                .setOnClickListener(v -> {

                    locDanhMuc = null;
                    timKiem.setText("");

                    taoDanhMuc();
                    hienThi();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        taiDuLieu();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        taiDuLieu();
    }

    private int dp(float giaTri) {
        return Math.round(
                giaTri * getResources()
                        .getDisplayMetrics().density
        );
    }

    private void chu(int id, String noiDung) {

        TextView view = findViewById(id);

        if (view != null) {
            view.setText(noiDung);
        }
    }

    // DOC DIA DIEM VA YEU THICH TU SQLITE
    private void taiDuLieu() {

        try (AppDatabase db = new AppDatabase(this)) {

            tatCa = db.danhSachDiaDiem();

            daLuu.clear();

            long taiKhoanId = PhienDangNhap.id(this);

            if (taiKhoanId > 0) {

                daLuu.addAll(
                        db.danhSachYeuThich(taiKhoanId)
                );
            }

            goiY = GoiYDiaDiem.deXuat(
                    tatCa,
                    daLuu
            );

            taoDanhMuc();
            hienThi();

        } catch (Exception e) {

            android.util.Log.e(
                    "KhamPhaActivity",
                    "Khong tai duoc du lieu",
                    e
            );

            tatCa = new ArrayList<>();
            goiY = new ArrayList<>();
            daLuu.clear();

            danhSach.removeAllViews();

            khungNoiBat.setVisibility(View.GONE);
            trangThai.setVisibility(View.VISIBLE);

            chu(
                    R.id.kpThongBao,
                    "Không tải được địa điểm. Vui lòng thử lại."
            );

            chu(
                    R.id.kpSoLuong,
                    "Chưa tải được danh sách"
            );

            Toast.makeText(
                    this,
                    "Không thể đọc dữ liệu địa điểm",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // TAO CAC CHIP DANH MUC
    private void taoDanhMuc() {

        danhMuc.removeAllViews();

        Set<String> cacLoai = new LinkedHashSet<>();

        for (DiaDiem d : tatCa) {

            if (d.danhMuc != null
                    && !d.danhMuc.trim().isEmpty()) {

                cacLoai.add(d.danhMuc);
            }
        }

        if (locDanhMuc != null
                && !cacLoai.contains(locDanhMuc)) {

            locDanhMuc = null;
        }

        themDanhMuc(null);

        for (String loai : cacLoai) {
            themDanhMuc(loai);
        }
    }

    private void themDanhMuc(String loai) {

        boolean dangChon = Objects.equals(
                loai,
                locDanhMuc
        );

        TextView nut = new TextView(this);

        nut.setText(
                loai == null ? "Tất cả" : loai
        );

        nut.setTextSize(12);

        nut.setTextColor(
                Color.parseColor(
                        dangChon ? "#FFFFFF" : "#486454"
                )
        );

        nut.setGravity(Gravity.CENTER);

        nut.setPadding(
                dp(16), 0,
                dp(16), 0
        );

        GradientDrawable nen = new GradientDrawable();

        nen.setColor(
                Color.parseColor(
                        dangChon ? "#286C49" : "#ECF2EC"
                )
        );

        nen.setCornerRadius(dp(22));

        nut.setBackground(nen);
        nut.setSelected(dangChon);

        String nhan = loai == null ? "Tất cả" : loai;

        nut.setContentDescription(
                nhan + (dangChon ? ", đang chọn" : "")
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(38)
                );

        params.setMarginEnd(dp(8));

        danhMuc.addView(nut, params);

        nut.setOnClickListener(v -> {

            locDanhMuc = loai;

            taoDanhMuc();
            hienThi();
        });
    }

    // HOI THOAI LOC DANH MUC
    private void moBoLoc() {

        List<String> cacLoai = new ArrayList<>();

        cacLoai.add("Tất cả");

        for (DiaDiem d : tatCa) {

            if (d.danhMuc != null
                    && !d.danhMuc.trim().isEmpty()
                    && !cacLoai.contains(d.danhMuc)) {

                cacLoai.add(d.danhMuc);
            }
        }

        int dangChon = 0;

        if (locDanhMuc != null) {

            int viTri = cacLoai.indexOf(locDanhMuc);

            if (viTri >= 0) {
                dangChon = viTri;
            }
        }

        new com.google.android.material.dialog
                .MaterialAlertDialogBuilder(this)
                .setTitle("Lọc địa điểm")
                .setSingleChoiceItems(
                        cacLoai.toArray(new String[0]),
                        dangChon,
                        (dialog, viTri) -> {

                            locDanhMuc = viTri == 0
                                    ? null
                                    : cacLoai.get(viTri);

                            taoDanhMuc();
                            hienThi();

                            dialog.dismiss();
                        }
                )
                .setNegativeButton("Đóng", null)
                .show();
    }

    // MO CHI TIET DIA DIEM
    private void moChiTiet(DiaDiem diaDiem) {

        startActivity(
                new Intent(
                        this,
                        ChiTietDiaDiemActivity.class
                ).putExtra(
                        "viTriDiaDiem",
                        diaDiem.id
                )
        );
    }

    // GAN GIAO DIEN TRAI TIM
    private void capNhatTim(
            ImageButton nutTim,
            DiaDiem diaDiem
    ) {

        boolean yeuThich = daLuu.contains(
                diaDiem.id
        );

        nutTim.setImageResource(
                yeuThich
                        ? R.drawable.ui_kp_tim_da_luu
                        : R.drawable.ui_kp_tim_chua_luu
        );

        nutTim.setContentDescription(
                yeuThich
                        ? "Bỏ yêu thích " + diaDiem.ten
                        : "Lưu yêu thích " + diaDiem.ten
        );

        nutTim.setSelected(yeuThich);

        nutTim.setOnClickListener(v ->
                doiTrangThaiYeuThich(
                        diaDiem,
                        nutTim
                )
        );
    }

    // LUU / BO YEU THICH
    private void doiTrangThaiYeuThich(
            DiaDiem diaDiem,
            ImageButton nutTim
    ) {

        long taiKhoanId = PhienDangNhap.id(this);

        // Chua dang nhap thi yeu cau dang nhap.
        if (taiKhoanId <= 0) {

            new com.google.android.material.dialog
                    .MaterialAlertDialogBuilder(this)
                    .setTitle("Lưu địa điểm yêu thích")
                    .setMessage(
                            "Bạn cần đăng nhập để lưu "
                                    + diaDiem.ten
                                    + " vào bộ sưu tập."
                    )
                    .setNegativeButton("Để sau", null)
                    .setPositiveButton(
                            "Đăng nhập",
                            (dialog, which) ->
                                    startActivity(
                                            new Intent(
                                                    this,
                                                    DangNhapActivity.class
                                            )
                                    )
                    )
                    .show();

            return;
        }

        boolean dangLuu = daLuu.contains(diaDiem.id);
        boolean luuMoi = !dangLuu;

        try (AppDatabase db = new AppDatabase(this)) {

            boolean thanhCong = db.datYeuThich(
                    taiKhoanId,
                    diaDiem.id,
                    luuMoi
            );

            if (!thanhCong) {
                throw new IllegalStateException(
                        "Khong luu duoc yeu thich"
                );
            }

            if (luuMoi) {
                daLuu.add(diaDiem.id);
            } else {
                daLuu.remove(diaDiem.id);
            }

            // Doi bieu tuong ngay, khong lam
            // nhay vi tri danh sach dang xem.
            capNhatTim(nutTim, diaDiem);

            Toast.makeText(
                    this,
                    luuMoi
                            ? "Đã thêm vào yêu thích"
                            : "Đã bỏ khỏi yêu thích",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            android.util.Log.e(
                    "KhamPhaActivity",
                    "Khong cap nhat duoc yeu thich",
                    e
            );

            Toast.makeText(
                    this,
                    "Không thể cập nhật yêu thích.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // HIEN THI MAN HINH KHAM PHA
    private void hienThi() {

        if (danhSach == null || timKiem == null) {
            return;
        }

        danhSach.removeAllViews();

        String tuKhoa = timKiem.getText().toString().trim();

        boolean dangLoc =
                !tuKhoa.isEmpty()
                        || locDanhMuc != null;

        // Goi y dau tien khi khong tim kiem / loc.
        DiaDiem noiBat = null;

        if (!dangLoc && !goiY.isEmpty()) {

            noiBat = goiY.get(0);

        } else if (!dangLoc
                && daLuu.isEmpty()
                && !tatCa.isEmpty()) {

            noiBat = tatCa.get(0);
        }

        khungNoiBat.setVisibility(
                noiBat == null ? View.GONE : View.VISIBLE
        );

        if (noiBat != null) {

            final DiaDiem diaDiem = noiBat;

            ImageView anh = findViewById(
                    R.id.kpAnhNoiBat
            );

            AnhDiaDiem.hien(
                    anh,
                    diaDiem.tenAnh
            );

            anh.setContentDescription(
                    "Ảnh " + diaDiem.ten
            );

            chu(R.id.kpTenNoiBat, diaDiem.ten);

            chu(
                    R.id.kpDiaChiNoiBat,
                    diaDiem.diaChi == null
                            ? ""
                            : diaDiem.diaChi
            );

            chu(
                    R.id.kpNhanNoiBat,
                    daLuu.isEmpty()
                            ? "GỢI Ý KHÁM PHÁ"
                            : "CÓ THỂ BẠN QUAN TÂM"
            );

            View theNoiBat = findViewById(
                    R.id.kpCardNoiBat
            );

            theNoiBat.setOnClickListener(
                    v -> moChiTiet(diaDiem)
            );

            capNhatTim(
                    findViewById(R.id.kpTimNoiBat),
                    diaDiem
            );
        }

        int soLuong = 0;

        for (DiaDiem d : tatCa) {

            // Khong hien trung diem noi bat
            // khi dang xem mac dinh.
            if (noiBat != null && d.id == noiBat.id) {
                continue;
            }

            if (!GoiYDiaDiem.khop(
                    d,
                    tuKhoa,
                    locDanhMuc
            )) {
                continue;
            }

            View the = getLayoutInflater().inflate(
                    R.layout.item_kham_pha,
                    danhSach,
                    false
            );

            TextView ten = the.findViewById(
                    R.id.kpItemTen
            );

            TextView loai = the.findViewById(
                    R.id.kpItemDanhMuc
            );

            TextView diaChi = the.findViewById(
                    R.id.kpItemDiaChi
            );

            ImageView anh = the.findViewById(
                    R.id.kpItemAnh
            );

            ImageButton nutTim = the.findViewById(
                    R.id.kpItemTim
            );

            ten.setText(d.ten);

            loai.setText(
                    d.danhMuc == null ? "" : d.danhMuc
            );

            diaChi.setText(
                    d.diaChi == null ? "" : d.diaChi
            );

            AnhDiaDiem.hien(
                    anh,
                    d.tenAnh
            );

            capNhatTim(nutTim, d);

            // Bam vao the de mo chi tiet.
            the.setContentDescription(
                    "Xem chi tiết " + d.ten
            );

            the.setOnClickListener(
                    v -> moChiTiet(d)
            );

            danhSach.addView(the);

            soLuong++;
        }

        int tongHienThi = soLuong
                + (noiBat == null ? 0 : 1);

        chu(
                R.id.kpSoLuong,
                tongHienThi + " điểm đến"
        );

        trangThai.setVisibility(
                tongHienThi == 0
                        ? View.VISIBLE
                        : View.GONE
        );

        chu(
                R.id.kpThongBao,
                tatCa.isEmpty()
                        ? "Chưa có địa điểm. Hãy thử tải lại."
                        : "Không có kết quả phù hợp. Hãy thử từ khóa hoặc danh mục khác."
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);

        out.putString(
                "loc_danh_muc",
                locDanhMuc
        );
    }
}
