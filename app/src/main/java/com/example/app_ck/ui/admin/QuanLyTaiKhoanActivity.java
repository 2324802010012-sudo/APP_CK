package com.example.app_ck.ui.admin;

import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.data.repository.KhoQuanTriTaiKhoan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.util.VanBanTimKiem;
import android.widget.ScrollView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class QuanLyTaiKhoanActivity extends BaseActivity {

    private static final String TAG = "QuanLyTaiKhoan";

    private static final int XANH = Color.rgb(44, 115, 81);
    private static final int CHU = Color.parseColor("#182D22");
    private static final int PHU = Color.parseColor("#687B6E");
    private static final int VIEN = Color.parseColor("#DFE9E1");
    private final ArrayList<HoSoTaiKhoan> duLieu = new ArrayList<>();

    private LinearLayout danhSach;
    private TextView tongSo;
    private TextView thongBaoTrong;
    private TextView xoaTimKiem;
    private EditText timKiem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!coQuyenAdmin()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_quan_ly_tai_khoan);
        AdminGiaoDien.apDung(this);
        apDungKhoangTrongHeThong();
        AdminGiaoDien.hoTroBanPhim(this);

        danhSach = findViewById(R.id.qltkDanhSach);
        tongSo = findViewById(R.id.qltkTongSo);
        thongBaoTrong = findViewById(R.id.qltkTrong);
        timKiem = findViewById(R.id.qltkTimKiem);
        xoaTimKiem = findViewById(R.id.qltkXoaTimKiem);

        findViewById(R.id.qltkQuayLai)
                .setOnClickListener(v -> finish());

        xoaTimKiem.setOnClickListener(v ->
                timKiem.setText("")
        );

        timKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start,
                    int before, int count) {

                xoaTimKiem.setVisibility(
                        s.length() > 0 ? View.VISIBLE : View.GONE
                );

                veDanhSach();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!coQuyenAdmin()) {
            finish();
            return;
        }

        if (danhSach != null) {
            taiDanhSach();
        }
    }

    // KIEM TRA QUYEN QUAN TRI TU SQLITE
    private boolean coQuyenAdmin() {
        try (AppDatabase helper = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(
                    this,
                    helper.getReadableDatabase()
            );
            return true;
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Bạn không có quyền quản lý tài khoản",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }
    }

    // LAY DANH SACH TU REPOSITORY
    private void taiDanhSach() {
        if (danhSach == null) return;

        try (AppDatabase helper = new AppDatabase(this)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            duLieu.clear();
            duLieu.addAll(KhoQuanTriTaiKhoan.danhSach(this, db, ""));
            thongBaoTrong.setOnClickListener(null);
            thongBaoTrong.setClickable(false);
            veDanhSach();

        } catch (SecurityException e) {

            Log.w(TAG, "Mất quyền quản trị", e);
            finish();

        } catch (Exception e) {

            Log.e(TAG, "Không tải được tài khoản", e);
            duLieu.clear();
            tongSo.setText("Không tải được");
            danhSach.removeAllViews();
            thongBaoTrong.setText(
                    "Không tải được tài khoản. Chạm để thử lại."
            );
            thongBaoTrong.setVisibility(View.VISIBLE);
            thongBaoTrong.setOnClickListener(v -> taiDanhSach());

            Toast.makeText(
                    this,
                    "Không thể tải danh sách tài khoản",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void veDanhSach() {
        if (!coQuyenAdmin()) { finish(); return; }
        ScrollView scroll = findViewById(R.id.qltkCuonDanhSach);
        int viTri = scroll.getScrollY();
        danhSach.removeAllViews();
        String key = VanBanTimKiem.chuanHoa(timKiem.getText().toString());
        int count = 0;
        // Cùng ba trường mà repository cho phép; ký tự %, _ vẫn được tìm như văn bản.
        for (HoSoTaiKhoan tk : duLieu) {
            if (!VanBanTimKiem.chuanHoa(tk.tenDangNhap).contains(key)
                    && !VanBanTimKiem.chuanHoa(tk.hoTen).contains(key)
                    && !VanBanTimKiem.chuanHoa(tk.email).contains(key)) continue;
            themDongTaiKhoan(tk);
            count++;
        }
        tongSo.setText(key.isEmpty() ? count + " tài khoản" : count + "/" + duLieu.size() + " tài khoản phù hợp");
        thongBaoTrong.setText(duLieu.isEmpty() ? "Chưa có tài khoản" : "Không tìm thấy tài khoản phù hợp");
        thongBaoTrong.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
        scroll.post(() -> scroll.scrollTo(0, viTri));
    }

    // TAO MOT DONG TAI KHOAN
    private void themDongTaiKhoan(HoSoTaiKhoan tk) {

        LinearLayout dong = new LinearLayout(this);
        dong.setOrientation(LinearLayout.HORIZONTAL);
        dong.setGravity(Gravity.CENTER_VERTICAL);
        dong.setPadding(dp(14), dp(14), dp(14), dp(14));
        dong.setBackground(nen(
                Color.WHITE, 16, VIEN
        ));
        dong.setClickable(true);
        dong.setFocusable(true);
        AdminGiaoDien.hieuUngNhan(dong);
        dong.setContentDescription(
                "Xem chi tiết tài khoản " + tk.tenDangNhap
        );

        LinearLayout.LayoutParams dongParams =
                new LinearLayout.LayoutParams(
                        -1, -2
                );
        dongParams.bottomMargin = dp(10);

        // AVATAR
        TextView avatar = text(
                chuDau(tk.tenHienThi()),
                16, XANH, true
        );
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(
                nen(Color.parseColor("#EDF5EC"), 14, 0)
        );

        dong.addView(
                avatar,
                new LinearLayout.LayoutParams(
                        dp(48), dp(48)
                )
        );

        // TEN, USERNAME, TRANG THAI
        LinearLayout noiDung = new LinearLayout(this);
        noiDung.setOrientation(LinearLayout.VERTICAL);
        noiDung.setPadding(dp(12), 0, dp(8), 0);

        TextView ten = text(
                tk.tenHienThi(), 15, CHU, true
        );
        noiDung.addView(ten);

        TextView username = text(
                "@" + tk.tenDangNhap,
                12, PHU, false
        );
        username.setPadding(0, dp(3), 0, 0);
        noiDung.addView(username);

        boolean admin = HoSoTaiKhoan.ADMIN.equals(tk.vaiTro);
        boolean active = tk.hoatDong();

        String trangThai =
                (admin ? "Admin" : "Khách hàng")
                        + " · "
                        + (active ? "Đang hoạt động" : "Đã khóa");

        TextView trangThaiView = text(
                trangThai,
                12,
                active ? XANH : Color.parseColor("#B5534A"),
                false
        );
        trangThaiView.setPadding(0, dp(5), 0, 0);
        noiDung.addView(trangThaiView);

        dong.addView(
                noiDung,
                new LinearLayout.LayoutParams(
                        0, -2, 1
                )
        );

        android.widget.ImageView muiTen = new android.widget.ImageView(this);
        muiTen.setImageResource(R.drawable.ui_ic_chevron);
        muiTen.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        dong.addView(muiTen, new LinearLayout.LayoutParams(dp(20), dp(20)));

        dong.setOnClickListener(v ->
                xemChiTiet(tk.id)
        );

        danhSach.addView(dong, dongParams);
    }

    // XEM CHI TIET TAI KHOAN
    private void xemChiTiet(long taiKhoanId) {
        HoSoTaiKhoan tk;

        try (AppDatabase helper = new AppDatabase(this)) {
            tk = KhoQuanTriTaiKhoan.chiTiet(
                    this,
                    helper.getReadableDatabase(),
                    taiKhoanId
            );
        } catch (SecurityException e) {
            finish();
            return;
        } catch (Exception e) {
            thongBao("Không thể xem thông tin tài khoản");
            return;
        }

        if (tk == null) {
            thongBao("Tài khoản không còn tồn tại");
            taiDanhSach();
            return;
        }

        boolean dangKhoa =
                HoSoTaiKhoan.LOCKED.equals(tk.trangThai);

        long idHienTai = PhienDangNhap.id(this);
        boolean laChinhMinh = tk.id == idHienTai;

        String noiDung =
                "Họ tên: " + anNull(tk.hoTen)
                        + "\n\nTên đăng nhập: " + anNull(tk.tenDangNhap)
                        + "\n\nVai trò: " + (HoSoTaiKhoan.ADMIN.equals(tk.vaiTro) ? "Admin" : "Khách hàng")
                        + "\n\nTrạng thái: "
                        + (dangKhoa ? "Đã khóa" : "Đang hoạt động")
                        + (laChinhMinh ? "\n\nĐây là tài khoản của bạn, không thể tự khóa." : "");

        MaterialAlertDialogBuilder builder =
                    new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                        .setTitle("Chi tiết tài khoản")
                        .setMessage(noiDung)
                        .setNegativeButton("Đóng", null);

        if (!laChinhMinh) {

            builder.setPositiveButton(
                    dangKhoa ? "Mở khóa" : "Khóa tài khoản",
                    (dialog, which) ->
                            xacNhanDoiTrangThai(
                                    tk.id,
                                    !dangKhoa
                            )
            );

        }
        AlertDialog detail = builder.show();
        if (!laChinhMinh && !dangKhoa) AdminGiaoDien.toMauNguyHiem(detail);
    }

    // XAC NHAN THAY DOI TRANG THAI
    private void xacNhanDoiTrangThai(
            long taiKhoanId,
            boolean khoa
    ) {

        AlertDialog confirm = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                .setTitle(
                        khoa ? "Khóa tài khoản?" : "Mở khóa tài khoản?"
                )
                .setMessage(
                        khoa
                                ? "Tài khoản sẽ không thể đăng nhập khi bị khóa."
                                : "Tài khoản sẽ được phép đăng nhập trở lại."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        khoa ? "Khóa" : "Mở khóa",
                        (dialog, which) ->
                                doiTrangThai(taiKhoanId, khoa)
                )
                .show();
        if (khoa) AdminGiaoDien.toMauNguyHiem(confirm);
    }

    // GHI THAY DOI VAO SQLITE
    private void doiTrangThai(long taiKhoanId, boolean khoa) {

        try (AppDatabase helper = new AppDatabase(this)) {

            SQLiteDatabase db = helper.getWritableDatabase();

            boolean thanhCong =
                    KhoQuanTriTaiKhoan.datKhoa(
                            this,
                            db,
                            taiKhoanId,
                            khoa
                    );

            if (thanhCong) {
                thongBao(
                        khoa
                                ? "Đã khóa tài khoản"
                                : "Đã mở khóa tài khoản"
                );
                taiDanhSach();
            } else {
                thongBao("Không thể thay đổi trạng thái tài khoản");
            }

        } catch (SecurityException e) {

            thongBao("Bạn không có quyền thực hiện thao tác");
            finish();

        } catch (Exception e) {

            Log.e(TAG, "Lỗi cập nhật tài khoản", e);
            thongBao("Có lỗi khi cập nhật tài khoản");
        }
    }

    private String anNull(String s) {
        return s == null || s.trim().isEmpty()
                ? "Chưa cập nhật"
                : s;
    }

    private String chuDau(String ten) {
        if (ten == null || ten.trim().isEmpty()) return "U";
        return ten.trim().substring(0, 1).toUpperCase(
                new Locale("vi", "VN")
        );
    }

    private void thongBao(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    private TextView text(
            String noiDung,
            int coChu,
            int mau,
            boolean dam
    ) {
        TextView t = new TextView(this);
        t.setText(noiDung);
        t.setTextSize(coChu);
        t.setTextColor(mau);
        if (dam) {
            t.setTypeface(null, Typeface.BOLD);
        }
        return t;
    }

    private GradientDrawable nen(
            int mau,
            int banKinh,
            int mauVien
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(mau);
        drawable.setCornerRadius(dp(banKinh));

        if (mauVien != 0) {
            drawable.setStroke(dp(1), mauVien);
        }

        return drawable;
    }
}
