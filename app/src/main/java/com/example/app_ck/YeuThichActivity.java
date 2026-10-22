package com.example.app_ck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YeuThichActivity extends BaseActivity {

    private static final String TAG = "YeuThichActivity";
    private static final int SO_DIA_DIEM = 5;

    private LinearLayout khung;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hienThi();
    }

    private void hienThi() {

        khung = DangNhapActivity.khungGiaoDien(
                this,
                "Địa điểm yêu thích",
                "Danh sách được lưu riêng cho tài khoản của bạn"
        );

        if (!PhienDangNhap.daDangNhap(this)) {

            DangNhapActivity.nut(
                    khung,
                    "Đăng nhập",
                    () -> startActivity(
                            new Intent(
                                    this,
                                    DangNhapActivity.class
                            )
                    )
            );

            return;
        }

        TextView taiKhoan = new TextView(this);

        taiKhoan.setText(
                "Tài khoản: " + PhienDangNhap.ten(this)
        );

        taiKhoan.setTextSize(14);
        taiKhoan.setPadding(0, 8, 0, 18);

        khung.addView(taiKhoan);

        ArrayList<Integer> danhSachYeuThich;

        Map<Integer, String> tenTheoId =
                new HashMap<>();

        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            danhSachYeuThich =
                    db.danhSachYeuThich(
                            PhienDangNhap.id(this)
                    );

            // Đọc tên của 5 địa điểm từ SQLite.
            for (DiaDiem diaDiem : db.danhSachDiaDiem()) {

                if (diaDiem.id >= 0
                        && diaDiem.id < SO_DIA_DIEM
                        && diaDiem.ten != null) {

                    tenTheoId.put(
                            diaDiem.id,
                            diaDiem.ten
                    );
                }
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Không đọc được danh sách yêu thích",
                    e
            );

            Toast.makeText(
                    this,
                    "Không thể tải danh sách yêu thích.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int soDiaDiemHopLe = 0;

        for (int idDiaDiem : danhSachYeuThich) {

            final String tenDiaDiem =
                    tenTheoId.get(idDiaDiem);

            if (tenDiaDiem == null
                    || tenDiaDiem.trim().isEmpty()) {
                continue;
            }

            soDiaDiemHopLe++;

            // Mở trang chi tiết địa điểm.
            DangNhapActivity.nut(
                    khung,
                    "♥  " + tenDiaDiem,
                    () -> {

                        Intent intent = new Intent(
                                this,
                                ChiTietDiaDiemActivity.class
                        );

                        intent.putExtra(
                                "viTriDiaDiem",
                                idDiaDiem
                        );

                        startActivity(intent);
                    }
            );

            // Xóa địa điểm khỏi danh sách yêu thích.
            DangNhapActivity.nut(
                    khung,
                    "Bỏ yêu thích: " + tenDiaDiem,
                    () -> xacNhanBoYeuThich(
                            idDiaDiem,
                            tenDiaDiem
                    )
            );
        }

        if (soDiaDiemHopLe == 0) {

            TextView trong = new TextView(this);

            trong.setText(
                    "Bạn chưa lưu địa điểm nào."
            );

            trong.setPadding(
                    0,
                    30,
                    0,
                    20
            );

            khung.addView(trong);
        }

        // Đăng xuất tài khoản.
        DangNhapActivity.nut(
                khung,
                "Đăng xuất",
                () -> new AlertDialog.Builder(this)
                        .setMessage(
                                "Bạn muốn đăng xuất?"
                        )
                        .setPositiveButton(
                                "Đăng xuất",
                                (dialog, which) -> {

                                    PhienDangNhap.dangXuat(this);
                                    hienThi();
                                }
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .show()
        );
    }

    private void xacNhanBoYeuThich(
            int idDiaDiem,
            String tenDiaDiem
    ) {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Xóa khỏi yêu thích"
                )
                .setMessage(
                        "Bỏ " + tenDiaDiem
                                + " khỏi yêu thích?"
                )
                .setPositiveButton(
                        "Bỏ",
                        (dialog, which) -> {

                            try (TaiKhoanDb db =
                                         new TaiKhoanDb(this)) {

                                db.datYeuThich(
                                        PhienDangNhap.id(this),
                                        idDiaDiem,
                                        false
                                );

                                hienThi();

                            } catch (Exception e) {

                                Log.e(
                                        TAG,
                                        "Không thể bỏ yêu thích",
                                        e
                                );

                                Toast.makeText(
                                        this,
                                        "Không thể bỏ yêu thích.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }
}
