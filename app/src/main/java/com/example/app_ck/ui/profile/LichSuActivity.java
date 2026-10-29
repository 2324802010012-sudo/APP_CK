package com.example.app_ck.ui.profile;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoNoiDungThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.ThanhDieuHuongDuoi;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.tour.DieuHuongThamQuan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LichSuActivity extends BaseActivity {
    private LinearLayout danhSach;
    private final ActivityResultLauncher<Intent> dangNhap = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> hienThi());

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_lich_su);
        apDungKhoangTrongHeThong();
        danhSach = findViewById(R.id.lsDanhSach);
        findViewById(R.id.lsQuayLai).setOnClickListener(v -> finish());
        findViewById(R.id.lsXoaTatCa).setOnClickListener(v -> xacNhanXoa(null));
        if (state == null && !PhienDangNhap.daDangNhap(this)) moDangNhap();
    }

    @Override protected void onResume() { super.onResume(); hienThi(); }
    private void moDangNhap() { dangNhap.launch(new Intent(this, DangNhapActivity.class)); }
    private void chu(int id, String text) { ((TextView)findViewById(id)).setText(text); }

    private void trangThai(String ten, String moTa, String nut, Runnable hanhDong) {
        findViewById(R.id.lsTrangThai).setVisibility(View.VISIBLE);
        chu(R.id.lsTieuDeTrangThai, ten); chu(R.id.lsMoTaTrangThai, moTa); chu(R.id.lsNutTrangThai, nut);
        findViewById(R.id.lsNutTrangThai).setOnClickListener(v -> hanhDong.run());
    }

    private void hienThi() {
        if (danhSach == null) return;
        danhSach.removeAllViews();
        findViewById(R.id.lsXoaTatCa).setVisibility(View.GONE);
        chu(R.id.lsTongSo, "Các lượt nghe đã bắt đầu · Mới nhất trước");
        if (!PhienDangNhap.daDangNhap(this)) {
            chu(R.id.lsTaiKhoan, "Lịch sử riêng của bạn");
            trangThai("Đăng nhập để xem lịch sử", "Lượt nghe khi chưa đăng nhập không được gán vào tài khoản về sau.",
                    "Đăng nhập", this::moDangNhap);
            return;
        }
        chu(R.id.lsTaiKhoan, "Tài khoản: " + PhienDangNhap.ten(this));
        try (AppDatabase db = new AppDatabase(this)) {
            ArrayList<KhoLichSu.Luot> ds = KhoLichSu.doc(db.getReadableDatabase(), PhienDangNhap.id(this));
            chu(R.id.lsTongSo, ds.size() + " lượt nghe · Mới nhất trước");
            findViewById(R.id.lsTrangThai).setVisibility(ds.isEmpty() ? View.VISIBLE : View.GONE);
            findViewById(R.id.lsXoaTatCa).setVisibility(ds.isEmpty() ? View.GONE : View.VISIBLE);
            if (ds.isEmpty()) trangThai("Chưa có lịch sử nghe", "Hãy nghe giới thiệu địa điểm hoặc thuyết minh trên sơ đồ. Lượt nghe sẽ xuất hiện tại đây khi âm thanh bắt đầu.",
                    "Khám phá địa điểm", () -> ThanhDieuHuongDuoi.mo(this, ThanhDieuHuongDuoi.KHAM_PHA));
            SimpleDateFormat dinhDang = new SimpleDateFormat("HH:mm · dd/MM/yyyy", new Locale("vi", "VN"));
            for (KhoLichSu.Luot luot : ds) {
                View the = getLayoutInflater().inflate(R.layout.item_lich_su, danhSach, false);
                ((TextView)the.findViewById(R.id.lsTenDiaDiem)).setText(luot.tenDiaDiem);
                ((TextView)the.findViewById(R.id.lsTenDiem)).setText(luot.tenDiem);
                ((TextView)the.findViewById(R.id.lsCheDo)).setText(KhoLichSu.nhanCheDo(luot.cheDo));
                ((TextView)the.findViewById(R.id.lsThoiGian)).setText(dinhDang.format(new Date(luot.thoiGian)));
                long chuSoHuu = PhienDangNhap.id(this);
                the.setOnClickListener(v -> {
                    if (chuSoHuu != PhienDangNhap.id(this)) { hienThi(); return; }
                    try { startActivity(taoIntentMoLai(this, luot)); }
                    catch (IllegalArgumentException e) {
                        Toast.makeText(this, "Điểm tham quan không còn khả dụng.", Toast.LENGTH_SHORT).show();
                    }
                });
                the.findViewById(R.id.lsXoaLuot).setOnClickListener(v -> xacNhanXoa(luot.id));
                danhSach.addView(the);
            }
        } catch (Exception e) {
            android.util.Log.e("LichSuActivity", "Không đọc được lịch sử", e);
            danhSach.removeAllViews();
            findViewById(R.id.lsXoaTatCa).setVisibility(View.GONE);
            trangThai("Chưa tải được lịch sử", "Vui lòng thử tải lại danh sách.", "Thử lại", this::hienThi);
        }
    }

    public static Intent taoIntentMoLai(Context context, KhoLichSu.Luot luot) {
        try (AppDatabase helper = new AppDatabase(context)) {
            KhoNoiDungThamQuan.lay(helper.getReadableDatabase(), luot.diaDiem, luot.diem);
        }
        if (luot.diem == 0) return new Intent(context, ChiTietDiaDiemActivity.class)
                .putExtra("viTriDiaDiem", luot.diaDiem);
        return DieuHuongThamQuan.taoIntent(context, luot.diaDiem, luot.diem);
    }

    private void xacNhanXoa(Long id) {
        final long taiKhoan = PhienDangNhap.id(this);
        if (taiKhoan <= 0) { hienThi(); return; }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(id == null ? "Xóa toàn bộ lịch sử?" : "Xóa lượt nghe?")
                .setMessage("Chỉ xóa lịch sử của tài khoản hiện tại. Các địa điểm yêu thích vẫn được giữ lại.")
                .setPositiveButton("Xóa", (d, w) -> {
                    if (taiKhoan != PhienDangNhap.id(this)) { hienThi(); return; }
                    try (AppDatabase db = new AppDatabase(this)) { KhoLichSu.xoa(db.getWritableDatabase(), taiKhoan, id); }
                    catch (Exception e) { Toast.makeText(this, "Không xóa được lịch sử. Hãy thử lại.", Toast.LENGTH_SHORT).show(); }
                    hienThi();
                }).setNegativeButton("Hủy", null).show();
    }
}
