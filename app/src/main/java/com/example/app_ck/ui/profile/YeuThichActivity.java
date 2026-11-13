package com.example.app_ck.ui.profile;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.ThanhDieuHuongDuoi;
import com.example.app_ck.ui.home.KhamPhaActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YeuThichActivity extends BaseActivity {
    private LinearLayout danhSach;
    private final ActivityResultLauncher<Intent> dangNhap = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> hienThi());


    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_yeu_thich);
        apDungKhoangTrongHeThong();

        danhSach = findViewById(R.id.ytDanhSach);

        // Gan thanh dieu huong chung
        ThanhDieuHuongDuoi.gan(
                this,
                ThanhDieuHuongDuoi.YEU_THICH
        );

        // Dang nhap va dang ky khi nguoi dung muon
        findViewById(R.id.ytDangNhap)
                .setOnClickListener(v -> moDangNhap(false));

        findViewById(R.id.ytDangKy)
                .setOnClickListener(v -> moDangNhap(true));

        // Mo man hinh kham pha khi bo suu tap trong
        findViewById(R.id.ytKhamPha)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        this,
                                        KhamPhaActivity.class
                                )
                        )
                );
    }


    @Override protected void onResume() {
        super.onResume();
        hienThi();
    }

    private void moDangNhap(boolean dangKy) {
        dangNhap.launch(new Intent(this, DangNhapActivity.class)
                .putExtra(DangNhapActivity.MO_DANG_KY, dangKy));
    }

    private void chu(int id, String noiDung) {
        ((TextView) findViewById(id)).setText(noiDung);
    }


    private void hienThi() {

        if (danhSach == null) return;

        danhSach.removeAllViews();

        boolean daDangNhap =
                PhienDangNhap.daDangNhap(this);

        View trangThai = findViewById(
                R.id.ytTrangThai
        );

        View nutDangNhap = findViewById(
                R.id.ytDangNhap
        );

        View nutDangKy = findViewById(
                R.id.ytDangKy
        );

        Button nutKhamPha = findViewById(
                R.id.ytKhamPha
        );

        nutDangNhap.setVisibility(
                daDangNhap ? View.GONE : View.VISIBLE
        );

        nutDangKy.setVisibility(
                daDangNhap ? View.GONE : View.VISIBLE
        );

        nutKhamPha.setVisibility(View.GONE);

        trangThai.setVisibility(View.GONE);

        // Chua dang nhap
        if (!daDangNhap) {

            chu(
                    R.id.ytTongSo,
                    "Bộ sưu tập của bạn"
            );

            chu(
                    R.id.ytTieuDeTrangThai,
                    "Lưu lại điểm đến yêu thích"
            );

            chu(
                    R.id.ytMoTaTrangThai,
                    "Đăng nhập để xem và quản lý những địa điểm bạn đã lưu."
            );

            trangThai.setVisibility(View.VISIBLE);
            return;
        }

        // Mac dinh nut kham pha dung cho bo suu tap trong
        nutKhamPha.setText("Khám phá địa điểm");

        nutKhamPha.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                KhamPhaActivity.class
                        )
                )
        );

        try (AppDatabase db = new AppDatabase(this)) {

            Map<Integer, DiaDiem> theoId =
                    new HashMap<>();

            for (DiaDiem d : db.danhSachDiaDiem()) {
                theoId.put(d.id, d);
            }

            ArrayList<Integer> cacId =
                    db.danhSachYeuThich(
                            PhienDangNhap.id(this)
                    );

            int soLuong = 0;

            for (int id : cacId) {

                DiaDiem d = theoId.get(id);

                if (d == null) continue;

                themThe(d);
                soLuong++;
            }

            chu(
                    R.id.ytTongSo,
                    soLuong + " địa điểm đã lưu"
            );

            if (soLuong == 0) {

                chu(
                        R.id.ytTieuDeTrangThai,
                        "Chưa có địa điểm yêu thích"
                );

                chu(
                        R.id.ytMoTaTrangThai,
                        "Khám phá và chạm vào trái tim để lưu những điểm đến bạn yêu thích."
                );

                nutKhamPha.setVisibility(View.VISIBLE);
                trangThai.setVisibility(View.VISIBLE);

            } else {

                trangThai.setVisibility(View.GONE);
            }

        } catch (Exception e) {

            Log.e(
                    "YeuThichActivity",
                    "Khong tai duoc yeu thich",
                    e
            );

            danhSach.removeAllViews();

            chu(
                    R.id.ytTongSo,
                    "Chưa tải được danh sách"
            );

            chu(
                    R.id.ytTieuDeTrangThai,
                    "Không thể tải địa điểm"
            );

            chu(
                    R.id.ytMoTaTrangThai,
                    "Vui lòng kiểm tra và thử lại."
            );

            nutKhamPha.setText("Thử lại");

            nutKhamPha.setOnClickListener(
                    v -> hienThi()
            );

            nutKhamPha.setVisibility(View.VISIBLE);
            trangThai.setVisibility(View.VISIBLE);
        }
    }



    private void themThe(DiaDiem d) {

        View the = getLayoutInflater().inflate(
                R.layout.item_yeu_thich,
                danhSach,
                false
        );

        TextView ten = the.findViewById(
                R.id.ytTenDiaDiem
        );

        ImageView anh = the.findViewById(
                R.id.ytAnhDiaDiem
        );

        View nutTim = the.findViewById(
                R.id.ytBoLuu
        );

        ten.setText(d.ten);

        com.example.app_ck.ui.common.AnhDiaDiem.hien(
                anh,
                d.tenAnh
        );

        the.setContentDescription(
                "Xem chi tiết " + d.ten
        );

        the.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                ChiTietDiaDiemActivity.class
                        ).putExtra(
                                "viTriDiaDiem",
                                d.id
                        )
                )
        );

        nutTim.setContentDescription(
                "Bỏ yêu thích " + d.ten
        );

        nutTim.setOnClickListener(v -> xacNhanBo(d));

        danhSach.addView(the);
    }


    private void xacNhanBo(DiaDiem d) {
        long taiKhoan = PhienDangNhap.id(this);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Bỏ lưu địa điểm?")
                .setMessage("Bỏ " + d.ten + " khỏi danh sách yêu thích?")
                .setPositiveButton("Bỏ lưu", (dialog, which) -> {
                    if (taiKhoan <= 0 || taiKhoan != PhienDangNhap.id(this)) {
                        hienThi();
                        return;
                    }
                    try (AppDatabase db = new AppDatabase(this)) {
                        if(!db.datYeuThich(taiKhoan, d.id, false))
                            throw new IllegalStateException("Phiên hoặc địa điểm không còn hợp lệ");
                        hienThi();
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể bỏ lưu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Giữ lại", null).show();
    }
}
