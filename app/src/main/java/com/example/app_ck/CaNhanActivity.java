package com.example.app_ck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CaNhanActivity extends BaseActivity {
    private final ActivityResultLauncher<Intent> dangNhap = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> hienThi());
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_ca_nhan);
        apDungKhoangTrongHeThong();
        ThanhDieuHuongDuoi.gan(this, ThanhDieuHuongDuoi.CA_NHAN);
        findViewById(R.id.cnDangNhap).setOnClickListener(v -> moDangNhap(false));
        findViewById(R.id.cnDangKy).setOnClickListener(v -> moDangNhap(true));
        findViewById(R.id.cnSoYeuThich).setOnClickListener(v -> ThanhDieuHuongDuoi.mo(this, ThanhDieuHuongDuoi.YEU_THICH));
        findViewById(R.id.cnSoDiaDiem).setOnClickListener(v -> ThanhDieuHuongDuoi.mo(this, ThanhDieuHuongDuoi.KHAM_PHA));
        ganHang(R.id.cnSuaHoSo, R.drawable.bieu_tuong_ca_nhan, "Thông tin cá nhân", "Cập nhật họ tên và email liên hệ",
                () -> startActivity(new Intent(this, SuaHoSoActivity.class)));
        ganHang(R.id.cnYeuThich, R.drawable.ui_ic_heart, "Địa điểm yêu thích", "Xem bộ sưu tập của bạn",
                () -> ThanhDieuHuongDuoi.mo(this, ThanhDieuHuongDuoi.YEU_THICH));
        ganHang(R.id.cnDoiMatKhau, R.drawable.ui_ic_lock, "Đổi mật khẩu", "Xác thực bằng mật khẩu hiện tại",
                () -> startActivity(new Intent(this, DoiMatKhauActivity.class)));
        ganHang(R.id.cnHuongDan, R.drawable.ui_ic_route, "Thông tin ứng dụng", "Tài khoản, dữ liệu và mã khôi phục",
                () -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("APP_CK · Khám phá Việt Nam")
                        .setMessage("Khám phá địa điểm, lưu yêu thích và tham quan qua sơ đồ, GPS, thuyết minh.\n\n"
                                + "Tài khoản và hồ sơ lưu trên thiết bị, chưa đồng bộ giữa các máy. Email liên hệ chưa được xác minh và không dùng để khôi phục mật khẩu. Hãy giữ mã khôi phục được cấp khi đăng ký hoặc đổi mật khẩu.")
                        .setPositiveButton("Đã hiểu", null).show());
        findViewById(R.id.cnDangXuat).setOnClickListener(v -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Đăng xuất?").setMessage("Các địa điểm yêu thích và hồ sơ vẫn được giữ trong tài khoản của bạn.")
                .setPositiveButton("Đăng xuất", (d, w) -> { PhienDangNhap.dangXuat(this); hienThi(); })
                .setNegativeButton("Ở lại", null).show());
    }
    private void moDangNhap(boolean dangKy) {
        dangNhap.launch(new Intent(this, DangNhapActivity.class).putExtra(DangNhapActivity.MO_DANG_KY, dangKy));
    }
    private void ganHang(int id, int icon, String title, String detail, Runnable action) {
        View row = findViewById(id);
        ((ImageView) row.findViewById(R.id.cnActionIcon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.cnActionTitle)).setText(title);
        ((TextView) row.findViewById(R.id.cnActionDetail)).setText(detail);
        row.setOnClickListener(v -> action.run());
    }
    private void chu(int id, String text) { ((TextView) findViewById(id)).setText(text); }
    @Override protected void onResume() { super.onResume(); hienThi(); }
    private void hienThi() {
        if (findViewById(R.id.cnTen) == null) return;
        boolean signed = PhienDangNhap.daDangNhap(this);
        HoSoTaiKhoan hs = null;
        int saved = 0, total;
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            Set<Integer> valid = new HashSet<>();
            for (DiaDiem d : db.danhSachDiaDiem()) valid.add(d.id);
            total = valid.size();
            if (signed) {
                hs = db.layHoSo(PhienDangNhap.id(this));
                if (hs == null) { PhienDangNhap.dangXuat(this); signed = false; }
                else for (int id : db.danhSachYeuThich(hs.id)) if (valid.contains(id)) saved++;
            }
        } catch (Exception e) { Toast.makeText(this, "Không tải được hồ sơ. Vui lòng mở lại màn hình.", Toast.LENGTH_LONG).show(); return; }
        findViewById(R.id.cnKhach).setVisibility(signed ? View.GONE : View.VISIBLE);
        findViewById(R.id.cnDaDangNhap).setVisibility(signed ? View.VISIBLE : View.GONE);
        String ten = signed ? hs.tenHienThi() : "Chào bạn!";
        chu(R.id.cnTen, ten);
        chu(R.id.cnTenDangNhap, signed ? "@" + hs.tenDangNhap : "Khám phá với tư cách khách");
        chu(R.id.cnTrangThai, signed ? "Đã đăng nhập" : "Đăng nhập để lưu hành trình");
        String[] tu = ten.trim().split("\\s+");
        String chuDau = signed ? tu[0].substring(0, 1) + (tu.length > 1 ? tu[tu.length - 1].substring(0, 1) : "") : "K";
        chu(R.id.cnAvatar, chuDau.toUpperCase(Locale.ROOT));
        chu(R.id.cnSoYeuThich, signed ? saved + "\nĐịa điểm đã lưu" : "Yêu thích\nĐăng nhập để xem");
        chu(R.id.cnSoDiaDiem, total + "\nĐiểm đến khám phá");
    }
}
