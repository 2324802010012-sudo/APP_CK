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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class QuanLyTaiKhoanActivity extends BaseActivity {

    private static final String TAG = "QuanLyTaiKhoan";

    private static final int XANH = Color.rgb(44, 115, 81);
    private static final int CHU = Color.rgb(32, 56, 42);
    private static final int PHU = Color.rgb(113, 130, 119);
    private static final int VIEN = Color.rgb(232, 237, 232);

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
        apDungKhoangTrongHeThong();

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

                taiDanhSach();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        taiDanhSach();
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

        danhSach.removeAllViews();

        String tuKhoa = timKiem.getText().toString().trim();

        try (AppDatabase helper = new AppDatabase(this)) {

            SQLiteDatabase db = helper.getReadableDatabase();

            ArrayList<HoSoTaiKhoan> ds =
                    KhoQuanTriTaiKhoan.danhSach(
                            this, db, tuKhoa
                    );

            tongSo.setText(ds.size() + " tài khoản");

            thongBaoTrong.setVisibility(
                    ds.isEmpty() ? View.VISIBLE : View.GONE
            );

            for (HoSoTaiKhoan taiKhoan : ds) {
                themDongTaiKhoan(taiKhoan);
            }

        } catch (SecurityException e) {

            Log.w(TAG, "Mất quyền quản trị", e);
            finish();

        } catch (Exception e) {

            Log.e(TAG, "Không tải được tài khoản", e);
            tongSo.setText("Không tải được");
            thongBaoTrong.setText(
                    "Có lỗi khi tải danh sách tài khoản."
            );
            thongBaoTrong.setVisibility(View.VISIBLE);

            Toast.makeText(
                    this,
                    "Không thể tải danh sách tài khoản",
                    Toast.LENGTH_SHORT
            ).show();
        }
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
                nen(Color.rgb(234, 243, 235), 14, 0)
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
                (admin ? "ADMIN" : "USER")
                        + " · "
                        + (active ? "Đang hoạt động" : "Đã khóa");

        TextView trangThaiView = text(
                trangThai,
                12,
                active ? XANH : Color.rgb(167, 76, 61),
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

        TextView muiTen = text(
                "›", 26, PHU, false
        );
        dong.addView(muiTen);

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

        String ngayTao = "Chưa có thông tin";
        if (tk.ngayTao > 0) {
            long millis = tk.ngayTao < 100000000000L
                    ? tk.ngayTao * 1000L
                    : tk.ngayTao;
            ngayTao = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    new Locale("vi", "VN")
            ).format(new Date(millis));
        }

        String noiDung =
                "Tên đăng nhập: " + anNull(tk.tenDangNhap)
                        + "\n\nHọ tên: " + anNull(tk.hoTen)
                        + "\n\nEmail: " + anNull(tk.email)
                        + "\n\nVai trò: " + tk.vaiTro
                        + "\n\nTrạng thái: "
                        + (dangKhoa ? "Đã khóa" : "Đang hoạt động")
                        + "\n\nNgày tạo: " + ngayTao;

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(this)
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

        } else {

            builder.setNeutralButton(
                    "Tài khoản của bạn",
                    (dialog, which) ->
                            thongBao(
                                    "Không thể tự khóa tài khoản quản trị"
                            )
            );
        }

        builder.show();
    }

    // XAC NHAN THAY DOI TRANG THAI
    private void xacNhanDoiTrangThai(
            long taiKhoanId,
            boolean khoa
    ) {

        new MaterialAlertDialogBuilder(this)
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
