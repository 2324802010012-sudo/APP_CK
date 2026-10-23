package com.example.app_ck;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KhamPhaActivity extends BaseActivity {
    private EditText timKiem;
    private LinearLayout danhSach, danhMuc;
    private List<DiaDiem> tatCa = new ArrayList<>(), hienCo = new ArrayList<>();
    private String locDanhMuc;
    private int tab;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_kham_pha);
        apDungKhoangTrongHeThong();
        timKiem = findViewById(R.id.kpTimKiem);
        danhSach = findViewById(R.id.kpDanhSach);
        danhMuc = findViewById(R.id.kpDanhMuc);
        if (state != null) locDanhMuc = state.getString("loc_danh_muc");
        timKiem.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { hienThi(); }
            public void afterTextChanged(Editable s) {}
        });
        timKiem.setOnEditorActionListener((v, action, event) -> {
            if (action == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(timKiem.getWindowToken(), 0);
                timKiem.clearFocus(); return true;
            }
            return false;
        });
    }
    @Override protected void onResume() { super.onResume(); taiDuLieu(); }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); locDanhMuc = null; timKiem.setText(""); taiDuLieu();
    }
    private void chu(int id, String text) { ((TextView) findViewById(id)).setText(text); }
    private void taiDuLieu() {
        tab = getIntent().getIntExtra(ThanhDieuHuongDuoi.TAB, ThanhDieuHuongDuoi.KHAM_PHA) == ThanhDieuHuongDuoi.GOI_Y
                ? ThanhDieuHuongDuoi.GOI_Y : ThanhDieuHuongDuoi.KHAM_PHA;
        ThanhDieuHuongDuoi.gan(this, tab);
        chu(R.id.kpTieuDe, tab == ThanhDieuHuongDuoi.GOI_Y ? "Gợi ý dành cho bạn" : "Khám phá điểm đến");
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            tatCa = db.danhSachDiaDiem();
            Set<Integer> daLuu = PhienDangNhap.daDangNhap(this)
                    ? new HashSet<>(db.danhSachYeuThich(PhienDangNhap.id(this))) : new HashSet<>();
            hienCo = tab == ThanhDieuHuongDuoi.GOI_Y ? GoiYDiaDiem.deXuat(tatCa, daLuu) : tatCa;
            chu(R.id.kpMoTa, tab != ThanhDieuHuongDuoi.GOI_Y ? "Tìm hiểu lịch sử, văn hóa và lên ý tưởng cho hành trình tiếp theo."
                    : daLuu.isEmpty() ? "Những điểm đến để bắt đầu. Lưu địa điểm yêu thích để nhận gợi ý phù hợp hơn."
                    : "Ưu tiên điểm chưa lưu có chủ đề tương tự các địa điểm bạn yêu thích.");
            taoDanhMuc(); hienThi();
        } catch (Exception e) {
            danhSach.removeAllViews();
            chu(R.id.kpSoLuong, "Chưa tải được danh sách");
            findViewById(R.id.kpTrangThai).setVisibility(View.VISIBLE);
            chu(R.id.kpThongBao, "Không thể tải dữ liệu. Vui lòng thử lại.");
            chu(R.id.kpThuLai, "Thử lại");
            findViewById(R.id.kpThuLai).setOnClickListener(v -> taiDuLieu());
        }
    }
    private void taoDanhMuc() {
        danhMuc.removeAllViews();
        Set<String> cacLoai = new LinkedHashSet<>();
        for (DiaDiem d : hienCo) if (d.danhMuc != null && !d.danhMuc.isEmpty()) cacLoai.add(d.danhMuc);
        if (locDanhMuc != null && !cacLoai.contains(locDanhMuc)) locDanhMuc = null;
        themDanhMuc(null);
        for (String loai : cacLoai) themDanhMuc(loai);
    }
    private void themDanhMuc(String loai) {
        boolean chon = java.util.Objects.equals(loai, locDanhMuc);
        Button nut = new Button(this);
        nut.setText(loai == null ? "Tất cả" : loai); nut.setAllCaps(false); nut.setTextSize(12);
        nut.setSelected(chon);
        nut.setTextColor(Color.parseColor(chon ? "#FFFFFF" : "#2C7351"));
        nut.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(chon ? "#2C7351" : "#EAF1E8")));
        danhMuc.addView(nut);
        nut.setOnClickListener(v -> { locDanhMuc = loai; taoDanhMuc(); hienThi(); });
    }
    private void hienThi() {
        if (danhSach == null) return;
        danhSach.removeAllViews(); int so = 0;
        for (DiaDiem d : hienCo) if (GoiYDiaDiem.khop(d, timKiem.getText().toString(), locDanhMuc)) {
            View the = getLayoutInflater().inflate(R.layout.item_yeu_thich, danhSach, false);
            ((TextView) the.findViewById(R.id.ytTenDiaDiem)).setText(d.ten);
            ((TextView) the.findViewById(R.id.ytDanhMuc)).setText(d.danhMuc);
            ((TextView) the.findViewById(R.id.ytDiaChi)).setText(d.diaChi);
            the.findViewById(R.id.ytBoLuu).setVisibility(View.GONE);
            int anh = d.tenAnh == null ? 0 : getResources().getIdentifier(d.tenAnh, "drawable", getPackageName());
            ((ImageView) the.findViewById(R.id.ytAnhDiaDiem)).setImageResource(anh == 0 ? android.R.drawable.ic_menu_gallery : anh);
            the.setOnClickListener(v -> startActivity(new Intent(this, ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem", d.id)));
            danhSach.addView(the); so++;
        }
        chu(R.id.kpSoLuong, so + (tab == ThanhDieuHuongDuoi.GOI_Y ? " địa điểm gợi ý" : " địa điểm khám phá"));
        findViewById(R.id.kpTrangThai).setVisibility(so == 0 ? View.VISIBLE : View.GONE);
        chu(R.id.kpThongBao, hienCo.isEmpty() && tab == ThanhDieuHuongDuoi.GOI_Y
                ? "Bạn đã lưu tất cả điểm đến. Hãy mở bộ sưu tập hoặc xem lại các địa điểm đã khám phá."
                : "Chưa tìm thấy điểm đến phù hợp. Thử từ khóa hoặc danh mục khác.");
        chu(R.id.kpThuLai, "Xem tất cả địa điểm");
        findViewById(R.id.kpThuLai).setOnClickListener(v -> {
            if (tab == ThanhDieuHuongDuoi.GOI_Y) ThanhDieuHuongDuoi.mo(this, ThanhDieuHuongDuoi.KHAM_PHA);
            else { locDanhMuc = null; timKiem.setText(""); taoDanhMuc(); hienThi(); }
        });
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putString("loc_danh_muc", locDanhMuc); }
}
