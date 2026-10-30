package com.example.app_ck.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.home.MainActivity;

public class AdminActivity extends BaseActivity {

    private static final String TAG = "APP_CK_ADMIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chỉ tài khoản ADMIN được mở màn hình.
        if (!kiemTraQuyen()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);
        apDungKhoangTrongHeThong();

        ganSuKien();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Kiểm tra lại khi quay về màn hình Admin.
        if (!kiemTraQuyen()) {
            finish();
        }
    }

    // KIEM TRA QUYEN ADMIN TU SQLITE
    private boolean kiemTraQuyen() {

        try (AppDatabase helper = new AppDatabase(this)) {

            PhienDangNhap.yeuCauQuanTri(
                    this,
                    helper.getReadableDatabase()
            );

            return true;

        } catch (SecurityException e) {

            Toast.makeText(
                    this,
                    "Tài khoản không có quyền quản trị",
                    Toast.LENGTH_SHORT
            ).show();

            return false;

        } catch (Exception e) {

            Log.e(TAG, "Lỗi kiểm tra quyền Admin", e);

            Toast.makeText(
                    this,
                    "Không thể xác minh quyền quản trị",
                    Toast.LENGTH_LONG
            ).show();

            return false;
        }
    }

    // GAN SU KIEN GIAO DIEN
    private void ganSuKien() {

        // UC08.1 - UC08.4
        findViewById(R.id.adminDiaDiem)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        this,
                                        QuanLyDiaDiemActivity.class
                                )
                        )
                );

        // UC08.5 - UC08.7
        findViewById(R.id.adminDiemThamQuan)
                .setOnClickListener(v ->
                        thongBaoChuaTrienKhai(
                                "Quản lý điểm tham quan"
                        )
                );

        // UC08.8
        findViewById(R.id.adminThuyetMinh)
                .setOnClickListener(v ->
                        thongBaoChuaTrienKhai(
                                "Quản lý nội dung thuyết minh"
                        )
                );

        // CAU HINH GPS AUTO
        findViewById(R.id.adminGpsAuto)
                .setOnClickListener(v ->
                        thongBaoChuaTrienKhai(
                                "Cấu hình GPS AUTO"
                        )
                );

        // UC08.9 - UC08.12
        // Mot muc quan ly tai khoan duy nhat.
        findViewById(R.id.adminTaiKhoan)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        this,
                                        QuanLyTaiKhoanActivity.class
                                )
                        )
                );

        // VE TRANG CHU, GIU PHIEN ADMIN
        findViewById(R.id.adminTrangChu)
                .setOnClickListener(v ->
                        veTrangChu()
                );

        // DANG XUAT
        findViewById(R.id.adminDangXuat)
                .setOnClickListener(v ->
                        xacNhanDangXuat()
                );
    }

    // MO QUAN LY TAI KHOAN
    private void moQuanLyTaiKhoan() {

        if (!kiemTraQuyen()) {
            finish();
            return;
        }

        Intent intent = new Intent(
                AdminActivity.this,
                QuanLyTaiKhoanActivity.class
        );

        startActivity(intent);
    }

    // THONG BAO CAC CHUC NANG CHUA TRIEN KHAI
    private void thongBaoChuaTrienKhai(String chucNang) {

        Toast.makeText(
                this,
                chucNang + " sẽ được triển khai ở bước tiếp theo",
                Toast.LENGTH_SHORT
        ).show();
    }

    // VE TRANG CHU NHUNG KHONG DANG XUAT
    private void veTrangChu() {

        Intent intent = new Intent(
                AdminActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }

    // DANG XUAT TAI KHOAN ADMIN
    private void xacNhanDangXuat() {

        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất tài khoản Admin?")
                .setMessage(
                        "Bạn có chắc muốn kết thúc phiên quản trị?"
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Đăng xuất",
                        (dialog, which) -> {

                            // Xoa phien dang nhap.
                            // Khong xoa tai khoan hay du lieu SQLite.
                            PhienDangNhap.dangXuat(this);

                            Intent intent = new Intent(
                                    AdminActivity.this,
                                    MainActivity.class
                            );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();
                        }
                )
                .show();
    }
}
