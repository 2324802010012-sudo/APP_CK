package com.example.app_ck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yeu_thich);
        apDungKhoangTrongHeThong();
        danhSach = findViewById(R.id.ytDanhSach);
        findViewById(R.id.ytQuayLai).setOnClickListener(v -> finish());
        ThanhDieuHuongDuoi.gan(this, ThanhDieuHuongDuoi.YEU_THICH);
        findViewById(R.id.ytDangNhap).setOnClickListener(v -> moDangNhap(false));
        findViewById(R.id.ytDangKy).setOnClickListener(v -> moDangNhap(true));
        findViewById(R.id.ytKhamPha).setOnClickListener(v -> veTrangChu());
        findViewById(R.id.ytDangXuat).setOnClickListener(v ->
                new AlertDialog.Builder(this).setTitle("Đăng xuất?")
                        .setMessage("Địa điểm đã lưu sẽ được giữ lại trong tài khoản của bạn.")
                        .setPositiveButton("Đăng xuất", (d, w) -> {
                            PhienDangNhap.dangXuat(this);
                            hienThi();
                        }).setNegativeButton("Hủy", null).show());
        if (state == null && !PhienDangNhap.daDangNhap(this)) moDangNhap(false);
    }

    @Override protected void onResume() {
        super.onResume();
        hienThi();
    }

    private void moDangNhap(boolean dangKy) {
        dangNhap.launch(new Intent(this, DangNhapActivity.class)
                .putExtra(DangNhapActivity.MO_DANG_KY, dangKy));
    }

    private void veTrangChu() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }

    private void chu(int id, String noiDung) {
        ((TextView) findViewById(id)).setText(noiDung);
    }

    private void hienThi() {
        if (danhSach == null) return;
        danhSach.removeAllViews();
        boolean daDangNhap = PhienDangNhap.daDangNhap(this);
        findViewById(R.id.ytDangXuat).setVisibility(daDangNhap ? View.VISIBLE : View.GONE);
        findViewById(R.id.ytDangNhap).setVisibility(daDangNhap ? View.GONE : View.VISIBLE);
        findViewById(R.id.ytDangKy).setVisibility(daDangNhap ? View.GONE : View.VISIBLE);
        findViewById(R.id.ytKhamPha).setVisibility(daDangNhap ? View.VISIBLE : View.GONE);
        findViewById(R.id.ytTrangThai).setVisibility(View.VISIBLE);
        if (!daDangNhap) {
            chu(R.id.ytTongSo, "Những điểm đến dành riêng cho bạn");
            chu(R.id.ytTaiKhoan, "Đăng nhập để xem bộ sưu tập");
            chu(R.id.ytTieuDeTrangThai, "Lưu lại hành trình của bạn");
            chu(R.id.ytMoTaTrangThai, "Đăng nhập hoặc tạo tài khoản để xem và quản lý các địa điểm yêu thích.");
            return; // Không đọc danh sách khi chưa đăng nhập.
        }

        chu(R.id.ytTaiKhoan, "Tài khoản: " + PhienDangNhap.ten(this));
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            Map<Integer, DiaDiem> theoId = new HashMap<>();
            for (DiaDiem d : db.danhSachDiaDiem()) theoId.put(d.id, d);
            ArrayList<Integer> cacId = db.danhSachYeuThich(PhienDangNhap.id(this));
            int soLuong = 0;
            for (int id : cacId) {
                DiaDiem d = theoId.get(id);
                if (d == null) continue;
                themThe(d);
                soLuong++;
            }
            chu(R.id.ytTongSo, soLuong + " địa điểm đã lưu");
            findViewById(R.id.ytTrangThai).setVisibility(soLuong == 0 ? View.VISIBLE : View.GONE);
            chu(R.id.ytTieuDeTrangThai, "Chưa có địa điểm yêu thích");
            chu(R.id.ytMoTaTrangThai, "Chạm vào trái tim ở trang chi tiết để lưu điểm đến cho hành trình tiếp theo.");
        } catch (Exception e) {
            Log.e("YeuThichActivity", "Không thể tải yêu thích", e);
            danhSach.removeAllViews();
            chu(R.id.ytTongSo, "Chưa tải được bộ sưu tập");
            chu(R.id.ytTieuDeTrangThai, "Không thể tải danh sách");
            chu(R.id.ytMoTaTrangThai, "Vui lòng thử tải lại danh sách yêu thích.");
            chu(R.id.ytKhamPha, "Thử lại");
            findViewById(R.id.ytKhamPha).setOnClickListener(v -> hienThi());
            return;
        }
        chu(R.id.ytKhamPha, "Khám phá địa điểm");
        findViewById(R.id.ytKhamPha).setOnClickListener(v -> veTrangChu());
    }

    private void themThe(DiaDiem d) {
        View the = getLayoutInflater().inflate(R.layout.item_yeu_thich, danhSach, false);
        ((TextView) the.findViewById(R.id.ytTenDiaDiem)).setText(d.ten);
        ((TextView) the.findViewById(R.id.ytDanhMuc)).setText(d.danhMuc);
        ((TextView) the.findViewById(R.id.ytDiaChi)).setText(d.diaChi);
        int anh = d.tenAnh == null ? 0 : getResources().getIdentifier(d.tenAnh, "drawable", getPackageName());
        ((ImageView) the.findViewById(R.id.ytAnhDiaDiem)).setImageResource(
                anh == 0 ? android.R.drawable.ic_menu_gallery : anh);
        the.setOnClickListener(v -> startActivity(new Intent(this, ChiTietDiaDiemActivity.class)
                .putExtra("viTriDiaDiem", d.id)));
        the.findViewById(R.id.ytBoLuu).setContentDescription("Bỏ yêu thích " + d.ten);
        the.findViewById(R.id.ytBoLuu).setOnClickListener(v -> xacNhanBo(d));
        danhSach.addView(the);
    }

    private void xacNhanBo(DiaDiem d) {
        long taiKhoan = PhienDangNhap.id(this);
        new AlertDialog.Builder(this).setTitle("Bỏ lưu địa điểm?")
                .setMessage("Bỏ " + d.ten + " khỏi danh sách yêu thích?")
                .setPositiveButton("Bỏ lưu", (dialog, which) -> {
                    if (taiKhoan <= 0 || taiKhoan != PhienDangNhap.id(this)) {
                        hienThi();
                        return;
                    }
                    try (TaiKhoanDb db = new TaiKhoanDb(this)) {
                        db.datYeuThich(taiKhoan, d.id, false);
                        hienThi();
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể bỏ lưu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Giữ lại", null).show();
    }
}
