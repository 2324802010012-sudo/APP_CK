package com.example.app_ck.ui.admin;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.home.MainActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminActivity extends BaseActivity {

    private static final String TAG = "AdminDashboard";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (!kiemTraQuyen()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);
        apDungKhoangTrongHeThong();
        ganSuKien();
        capNhatThongKe();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!kiemTraQuyen()) {
            finish();
            return;
        }

        // Du lieu co the da thay doi tai cac trang quan ly.
        if (findViewById(R.id.adminSoDiaDiem) != null) {
            capNhatThongKe();
        }
    }

    private boolean kiemTraQuyen() {
        try (AppDatabase helper = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(
                    this, helper.getReadableDatabase());
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this,
                    "Tài khoản không có quyền quản trị",
                    Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Không xác minh được quyền Admin", e);
            Toast.makeText(this,
                    "Không thể xác minh quyền quản trị",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void ganSuKien() {
        findViewById(R.id.adminDiaDiem)
                .setOnClickListener(v -> moManHinh(
                        QuanLyDiaDiemActivity.class));

        findViewById(R.id.adminDiemThamQuan)
                .setOnClickListener(v -> moManHinh(
                        QuanLyDiemThamQuanActivity.class));

        findViewById(R.id.adminThuyetMinh)
                .setOnClickListener(v -> moManHinh(
                        QuanLyThuyetMinhActivity.class));

        findViewById(R.id.adminGpsAuto)
                .setOnClickListener(v -> moManHinh(
                        QuanLyGpsAutoActivity.class));

        findViewById(R.id.adminTaiKhoan)
                .setOnClickListener(v -> moManHinh(
                        QuanLyTaiKhoanActivity.class));

        findViewById(R.id.adminTrangChu)
                .setOnClickListener(v -> veTrangChu());

        findViewById(R.id.adminDangXuat)
                .setOnClickListener(v -> xacNhanDangXuat());
    }

    private void moManHinh(Class<?> manHinh) {
        if (!kiemTraQuyen()) {
            finish();
            return;
        }
        startActivity(new Intent(this, manHinh));
    }

    private void capNhatThongKe() {
        try (AppDatabase helper = new AppDatabase(this)) {
            SQLiteDatabase db = helper.getReadableDatabase();

            // Luon kiem tra quyen truoc khi doc thong ke.
            PhienDangNhap.yeuCauQuanTri(this, db);

            int soDiaDiem = demBanGhi(db, "dia_diem");
            int soDiem = demBanGhi(
                    db, "diem_tham_quan_chung");
            int soTaiKhoan = demBanGhi(db, "tai_khoan");

            hienSo(R.id.adminSoDiaDiem, soDiaDiem);
            hienSo(R.id.adminSoDiem, soDiem);
            hienSo(R.id.adminSoTaiKhoan, soTaiKhoan);

        } catch (Exception e) {
            Log.e(TAG, "Không tải được thống kê", e);
            hienChu(R.id.adminSoDiaDiem, "—");
            hienChu(R.id.adminSoDiem, "—");
            hienChu(R.id.adminSoTaiKhoan, "—");
        }
    }

    private int demBanGhi(
            SQLiteDatabase db, String tenBang) {
        // Chi su dung ba ten bang co dinh cua ung dung.
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + tenBang, null)) {
            return cursor.moveToFirst()
                    ? cursor.getInt(0) : 0;
        }
    }

    private void hienSo(int id, int soLuong) {
        hienChu(id, String.valueOf(soLuong));
    }

    private void hienChu(int id, String noiDung) {
        TextView view = findViewById(id);
        if (view != null) {
            view.setText(noiDung);
        }
    }

    private void veTrangChu() {
        startActivity(new Intent(
                this, MainActivity.class));
        finish();
    }

    private void xacNhanDangXuat() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất tài khoản?")
                .setMessage(
                        "Bạn có muốn kết thúc phiên quản trị?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Đăng xuất",
                        (dialog, which) -> {
                            PhienDangNhap.dangXuat(this);

                            Intent intent = new Intent(
                                    this, MainActivity.class);
                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);
                            finish();
                        })
                .show();
    }
}
