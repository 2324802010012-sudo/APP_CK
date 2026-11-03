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

    // =====================================================
    // KHOI TAO
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (!kiemTraQuyen()) {
            finish();
        }
    }

    // =====================================================
    // KIEM TRA QUYEN ADMIN
    // =====================================================

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

    // =====================================================
    // GAN SU KIEN CAC CHUC NANG
    // =====================================================

    private void ganSuKien() {

        // UC08.1 - UC08.4
        // QUAN LY DIA DIEM DU LICH
        findViewById(R.id.adminDiaDiem)
                .setOnClickListener(v -> {
                    startActivity(
                            new Intent(
                                    AdminActivity.this,
                                    QuanLyDiaDiemActivity.class
                            )
                    );
                });

        // UC08.5 - UC08.7
        // QUAN LY DIEM THAM QUAN
        findViewById(R.id.adminDiemThamQuan)
                .setOnClickListener(v -> {
                    startActivity(
                            new Intent(
                                    AdminActivity.this,
                                    QuanLyDiemThamQuanActivity.class
                            )
                    );
                });

        // UC08.8
        // QUAN LY NOI DUNG THUYET MINH
        findViewById(R.id.adminThuyetMinh)
                .setOnClickListener(v -> {
                    startActivity(
                            new Intent(
                                    AdminActivity.this,
                                    QuanLyThuyetMinhActivity.class
                            )
                    );
                });
        // UC08 - CAU HINH GPS AUTO
        findViewById(R.id.adminGpsAuto)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(
                            AdminActivity.this,
                            QuanLyGpsAutoActivity.class
                    );

                    startActivity(intent);
                });

        // UC08.9 - UC08.12
        // QUAN LY TAI KHOAN
        findViewById(R.id.adminTaiKhoan)
                .setOnClickListener(v -> {
                    startActivity(
                            new Intent(
                                    AdminActivity.this,
                                    QuanLyTaiKhoanActivity.class
                            )
                    );
                });

        // VE TRANG CHU - GIU PHIEN ADMIN
        findViewById(R.id.adminTrangChu)
                .setOnClickListener(v -> veTrangChu());

        // DANG XUAT
        findViewById(R.id.adminDangXuat)
                .setOnClickListener(v -> xacNhanDangXuat());
    }

    // =====================================================
    // THONG BAO CHUA TRIEN KHAI
    // =====================================================

    private void thongBaoChuaTrienKhai(String chucNang) {

        Toast.makeText(
                this,
                chucNang + " sẽ được triển khai ở bước tiếp theo",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =====================================================
    // VE TRANG CHU - GIU PHIEN ADMIN
    // =====================================================

    private void veTrangChu() {

        Intent intent = new Intent(
                AdminActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }

    // =====================================================
    // DANG XUAT
    // =====================================================

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
