package com.example.app_ck.ui.admin;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.FrameLayout;
import java.io.File;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.Locale;

/** UC08.5–UC08.7: quản lý điểm tham quan trong cùng Activity; giữ nguyên GPS AUTO khi sửa. */
public class QuanLyDiemThamQuanActivity extends BaseActivity {
    private final ArrayList<DiaDiem> cacDiaDiem = new ArrayList<>();
    private final ArrayList<DiemThamQuan> cacDiem = new ArrayList<>();
    private LinearLayout khungDanhSach, danhSach;
    private View khungBieuMau;
    private Spinner chonDiaDiem;
    private TextView soLuong, tieuDe, tieuDeBieuMau;
    private EditText timKiem, ten, tang, thuTu, soHienThi, moTa, thuyetMinh, tenAnh, x, y;
    private Button xoa;
    private int idDiaDiem = -1;
    private int idDangSua = -1;
    private boolean doiSpinner = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!coQuyenAdmin()) { finish(); return; }
        setContentView(R.layout.activity_quan_ly_diem_tham_quan);
        apDungKhoangTrongHeThong();
        anhXa();
        findViewById(R.id.dtQuayLai).setOnClickListener(v -> {
            if (khungBieuMau.getVisibility() == View.VISIBLE) hienDanhSach(); else finish();
        });
        findViewById(R.id.dtThemMoi).setOnClickListener(v -> moBieuMau(null));
        findViewById(R.id.dtHuy).setOnClickListener(v -> hienDanhSach());
        findViewById(R.id.dtLuu).setOnClickListener(v -> luu());
        findViewById(R.id.dtChonMarker).setOnClickListener(v -> moSoDoChonMarker());
        findViewById(R.id.dtXoa).setOnClickListener(v -> xacNhanXoa());
        timKiem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { veDanhSach(); }
            @Override public void afterTextChanged(Editable e) {}
        });
        chonDiaDiem.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                if (doiSpinner || pos < 0 || pos >= cacDiaDiem.size()) return;
                idDiaDiem = cacDiaDiem.get(pos).id;
                taiDiem();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        taiDiaDiem();
    }

    @Override protected void onResume() {
        super.onResume();
        if (!coQuyenAdmin()) { finish(); return; }
        if (danhSach != null && khungBieuMau.getVisibility() != View.VISIBLE) taiDiem();
    }

    private void anhXa() {
        khungDanhSach = findViewById(R.id.dtKhungDanhSach);
        khungBieuMau = findViewById(R.id.dtKhungBieuMau);
        danhSach = findViewById(R.id.dtDanhSach);
        chonDiaDiem = findViewById(R.id.dtChonDiaDiem);
        soLuong = findViewById(R.id.dtSoLuong);
        tieuDe = findViewById(R.id.dtTieuDe);
        tieuDeBieuMau = findViewById(R.id.dtTieuDeBieuMau);
        timKiem = findViewById(R.id.dtTimKiem);
        ten = findViewById(R.id.dtTen);
        tang = findViewById(R.id.dtTang);
        thuTu = findViewById(R.id.dtThuTu);
        soHienThi = findViewById(R.id.dtSoHienThi);
        moTa = findViewById(R.id.dtMoTa);
        thuyetMinh = findViewById(R.id.dtThuyetMinh);
        tenAnh = findViewById(R.id.dtTenAnh);
        x = findViewById(R.id.dtX);
        y = findViewById(R.id.dtY);
        xoa = findViewById(R.id.dtXoa);
    }

    private boolean coQuyenAdmin() {
        try (AppDatabase h = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, h.getReadableDatabase());
            return true;
        } catch (Exception e) {
            thongBao("Không thể xác minh quyền ADMIN");
            return false;
        }
    }

    private void taiDiaDiem() {
        try (AppDatabase h = new AppDatabase(this)) {
            cacDiaDiem.clear(); cacDiaDiem.addAll(h.danhSachDiaDiem());
            ArrayList<String> tenDiaDiem = new ArrayList<>();
            int chon = 0;
            for (int i = 0; i < cacDiaDiem.size(); i++) {
                DiaDiem d = cacDiaDiem.get(i);
                tenDiaDiem.add(d.ten);
                if (d.id == idDiaDiem) chon = i;
            }
            doiSpinner = true;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, tenDiaDiem);
            chonDiaDiem.setAdapter(adapter);
            if (!cacDiaDiem.isEmpty()) {
                chonDiaDiem.setSelection(chon);
                idDiaDiem = cacDiaDiem.get(chon).id;
            } else idDiaDiem = -1;
            doiSpinner = false;
            taiDiem();
        } catch (Exception e) { android.util.Log.e("APP_CK_ADMIN_DIEM", "Không tải được địa điểm", e); thongBao("Không tải được địa điểm du lịch"); }
    }

    private void taiDiem() {
        if (danhSach == null) return;
        cacDiem.clear();
        if (idDiaDiem >= 0) try (AppDatabase h = new AppDatabase(this)) {
            cacDiem.addAll(KhoDuLieuThamQuan.danhSach(h.getReadableDatabase(), idDiaDiem));
        } catch (Exception e) { android.util.Log.e("APP_CK_ADMIN_DIEM", "Không tải được điểm", e); thongBao("Không tải được điểm tham quan"); }
        veDanhSach();
    }

    private void veDanhSach() {
        if (danhSach == null) return;
        danhSach.removeAllViews();
        String filter = timKiem.getText().toString().trim().toLowerCase(Locale.ROOT);
        int dem = 0;
        for (DiemThamQuan d : cacDiem) {
            if (!(d.ten == null ? "" : d.ten.toLowerCase(Locale.ROOT)).contains(filter)
                    && !String.valueOf(d.id).contains(filter)) continue;
            dem++;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL); card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            card.setBackgroundResource(R.drawable.ui_card);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(10);
            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            TextView nhan = new TextView(this);
            nhan.setText(d.ten); nhan.setTextColor(Color.rgb(32, 56, 42)); nhan.setTextSize(15);
            nhan.setTypeface(null, android.graphics.Typeface.BOLD);
            content.addView(nhan);
            TextView phu = new TextView(this);
            phu.setText("Mã " + d.id + " · Tầng/khu " + d.tang + (d.coMarker() ? " · Có Marker" : " · Chưa đặt Marker"));
            phu.setTextColor(Color.rgb(113, 130, 119)); phu.setTextSize(12);
            content.addView(phu);
            card.addView(content, new LinearLayout.LayoutParams(0, -2, 1));
            TextView chevron = new TextView(this);
            chevron.setText("›"); chevron.setTextColor(Color.rgb(44, 115, 81)); chevron.setTextSize(26);
            card.addView(chevron);
            card.setOnClickListener(v -> moBieuMau(d));
            danhSach.addView(card, lp);
        }
        soLuong.setText("DANH SÁCH · " + dem + " ĐIỂM");
        if (cacDiaDiem.isEmpty() || dem == 0) {
            TextView none = new TextView(this);
            none.setText(cacDiaDiem.isEmpty() ? "Chưa có địa điểm chính" : "Chưa có điểm tham quan phù hợp");
            none.setGravity(Gravity.CENTER); none.setTextColor(Color.rgb(113,130,119));
            none.setPadding(dp(12), dp(40), dp(12), dp(40)); danhSach.addView(none);
        }
    }

    private void moBieuMau(DiemThamQuan d) {
        if (idDiaDiem < 0) { thongBao("Cần tạo địa điểm du lịch chính trước"); return; }
        idDangSua = d == null ? -1 : d.id;
        tieuDe.setText(d == null ? "Thêm điểm tham quan" : "Chỉnh sửa điểm tham quan");
        tieuDeBieuMau.setText(d == null ? "Thêm điểm tham quan" : "Chỉnh sửa · Mã " + d.id);
        ten.setText(d == null ? "" : d.ten);
        tang.setText(d == null ? "0" : String.valueOf(d.tang));
        thuTu.setText(d == null ? String.valueOf(cacDiem.size() + 1) : String.valueOf(d.thuTu));
        soHienThi.setText(d == null ? "0" : String.valueOf(d.soHienThi));
        moTa.setText(d == null ? "" : d.moTa);
        thuyetMinh.setText(d == null ? "" : d.noiDung);
        tenAnh.setText(d == null ? "" : d.tenAnh);
        x.setText(d == null || !d.coMarker() ? "" : String.valueOf(d.x));
        y.setText(d == null || !d.coMarker() ? "" : String.valueOf(d.y));
        xoa.setVisibility(d == null ? View.GONE : View.VISIBLE);
        khungDanhSach.setVisibility(View.GONE); khungBieuMau.setVisibility(View.VISIBLE);
        khungBieuMau.scrollTo(0, 0);
    }

    private void hienDanhSach() {
        khungBieuMau.setVisibility(View.GONE); khungDanhSach.setVisibility(View.VISIBLE);
        tieuDe.setText("Quản lý điểm tham quan"); idDangSua = -1;
        taiDiem();
    }

    private String val(EditText e) { return e.getText().toString().trim(); }
    private void luu() {
        if (!coQuyenAdmin()) { finish(); return; }
        String tenMoi = val(ten);
        if (tenMoi.isEmpty()) { ten.setError("Vui lòng nhập tên điểm tham quan"); ten.requestFocus(); return; }
        int tangMoi, thuTuMoi, soMoi;
        try {
            tangMoi = Integer.parseInt(val(tang));
            thuTuMoi = Integer.parseInt(val(thuTu));
            soMoi = Integer.parseInt(val(soHienThi));
        } catch (NumberFormatException ex) { thongBao("Tầng, thứ tự, số hiển thị phải là số nguyên"); return; }
        boolean coX = !val(x).isEmpty(), coY = !val(y).isEmpty();
        if (coX != coY) { thongBao("Phải nhập cả X và Y hoặc để trống cả hai"); return; }
        Double xx = null, yy = null;
        if (coX) try {
            xx = Double.parseDouble(val(x)); yy = Double.parseDouble(val(y));
            if (!Double.isFinite(xx) || !Double.isFinite(yy) || xx < 0 || xx > 1 || yy < 0 || yy > 1) {
                thongBao("X/Y phải nằm trong khoảng 0–1"); return;
            }
        } catch (NumberFormatException ex) { thongBao("Tọa độ Marker không hợp lệ"); return; }
        ContentValues v = new ContentValues();
        v.put("ten", tenMoi); v.put("tang", tangMoi); v.put("thu_tu", thuTuMoi);
        v.put("so_hien_thi", soMoi); v.put("mo_ta", val(moTa));
        v.put("noi_dung_thuyet_minh", val(thuyetMinh)); v.put("ten_anh", val(tenAnh));
        if (xx == null) { v.putNull("x_ty_le"); v.putNull("y_ty_le"); }
        else { v.put("x_ty_le", xx); v.put("y_ty_le", yy); }
        try (AppDatabase h = new AppDatabase(this)) {
            SQLiteDatabase db = h.getWritableDatabase();
            int id = idDangSua;
            if (id < 0) {
                try (Cursor c = db.rawQuery("SELECT COALESCE(MAX(id),0)+1 FROM diem_tham_quan_chung WHERE dia_diem_id=?",
                        new String[]{String.valueOf(idDiaDiem)})) {
                    if (!c.moveToFirst()) { thongBao("Không tạo được mã điểm"); return; }
                    id = c.getInt(0);
                }
            }
            if (KhoDuLieuThamQuan.luuDiem(this, db, idDiaDiem, id, v)) {
                thongBao(idDangSua < 0 ? "Đã thêm điểm tham quan" : "Đã cập nhật điểm tham quan");
                hienDanhSach();
            } else thongBao("Không thể lưu. Kiểm tra thông tin hoặc liên kết tầng/khu vực.");
        } catch (SecurityException ex) { thongBao("Bạn không có quyền quản trị"); finish(); }
        catch (Exception ex) { android.util.Log.e("APP_CK_ADMIN_DIEM", "Lỗi lưu điểm", ex); thongBao("Không thể lưu điểm tham quan"); }
    }

    // Mo anh so do dung tang/khu va chon vi tri marker bang cham.
    private void moSoDoChonMarker() {
        if (!coQuyenAdmin()) { finish(); return; }
        if (idDiaDiem < 0) { thongBao("Chưa chọn địa điểm chính"); return; }
        int tangChon;
        try { tangChon = Integer.parseInt(val(tang)); }
        catch (NumberFormatException e) { tang.setError("Nhập tầng/khu vực hợp lệ"); return; }

        String maAnh;
        try (AppDatabase h = new AppDatabase(this)) {
            maAnh = KhoDuLieuThamQuan.anhSoDo(
                    h.getReadableDatabase(), idDiaDiem, tangChon);
        } catch (Exception e) {
            android.util.Log.e("APP_CK_MARKER", "Không đọc được sơ đồ", e);
            thongBao("Không tải được sơ đồ tham quan"); return;
        }

        if (maAnh == null || maAnh.trim().isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Chưa có ảnh sơ đồ")
                    .setMessage("Tầng/khu " + tangChon + " chưa có ảnh trong bảng so_do_tham_quan. "
                            + "Hãy bổ sung sơ đồ đúng tầng trước khi đặt Marker. "
                            + "Bạn vẫn có thể để trống X/Y và lưu điểm tham quan.")
                    .setPositiveButton("Đã hiểu", null).show();
            return;
        }

        Bitmap bitmap = docAnhSoDo(maAnh);
        if (bitmap == null) {
            thongBao("Không tìm thấy ảnh sơ đồ: " + maAnh);
            return;
        }

        SoDoChonMarkerView banDo = new SoDoChonMarkerView(this, bitmap);
        try {
            if (!val(x).isEmpty() && !val(y).isEmpty()) {
                double xx = Double.parseDouble(val(x));
                double yy = Double.parseDouble(val(y));
                if (Double.isFinite(xx) && Double.isFinite(yy)
                        && xx >= 0 && xx <= 1 && yy >= 0 && yy <= 1)
                    banDo.setMarker(xx, yy);
            }
        } catch (NumberFormatException ignored) { }

        LinearLayout khung = new LinearLayout(this);
        khung.setOrientation(LinearLayout.VERTICAL);
        khung.setPadding(dp(12), dp(8), dp(12), dp(8));
        TextView chiDan = new TextView(this);
        chiDan.setText("Chạm vào ảnh sơ đồ để đặt Marker. Điểm chọn chỉ được lưu khi bạn nhấn Lưu điểm tham quan.");
        chiDan.setTextColor(Color.rgb(32, 56, 42));
        chiDan.setTextSize(13);
        khung.addView(chiDan);
        khung.addView(banDo, new LinearLayout.LayoutParams(-1, dp(400)));
        TextView toaDo = new TextView(this);
        toaDo.setTextColor(Color.rgb(44,115,81));
        toaDo.setTextSize(13);
        toaDo.setPadding(0, dp(8), 0, dp(3));
        Runnable capNhat = () -> toaDo.setText(banDo.hasMarker()
                ? String.format(Locale.US, "X: %.5f    Y: %.5f", banDo.getXRatio(), banDo.getYRatio())
                : "Chưa đặt Marker");
        banDo.setOnMarkerChangedListener(capNhat);
        capNhat.run();
        khung.addView(toaDo);
        new AlertDialog.Builder(this)
                .setTitle("Chọn Marker · Tầng/khu " + tangChon)
                .setView(khung)
                .setNegativeButton("Hủy", (d,w) -> bitmap.recycle())
                .setPositiveButton("Xác nhận", (d,w) -> {
                    if (banDo.hasMarker()) {
                        x.setText(String.format(Locale.US,"%.6f", banDo.getXRatio()));
                        y.setText(String.format(Locale.US,"%.6f", banDo.getYRatio()));
                    } else thongBao("Chưa chọn vị trí trên sơ đồ");
                    bitmap.recycle();
                }).show();
    }

    private Bitmap docAnhSoDo(String maAnh) {
        try {
            if (maAnh.startsWith("anh_may:")) {
                String name = maAnh.substring("anh_may:".length());
                if (name.contains("/") || name.contains("\\") || name.contains("..")) return null;
                File f = new File(new File(getFilesDir(),"anh_dia_diem"),name);
                return f.isFile() ? BitmapFactory.decodeFile(f.getAbsolutePath()) : null;
            }
            int id = getResources().getIdentifier(maAnh, "drawable", getPackageName());
            return id == 0 ? null : BitmapFactory.decodeResource(getResources(), id);
        } catch (Exception ex) {
            android.util.Log.e("APP_CK_MARKER", "Lỗi đọc ảnh sơ đồ", ex);
            return null;
        }
    }

    private void xacNhanXoa() {
        if (idDangSua <= 0) return;
        final int id = idDangSua;
        new AlertDialog.Builder(this)
                .setTitle("Xóa điểm tham quan?")
                .setMessage("Điểm có lịch sử nghe sẽ không được xóa để bảo vệ dữ liệu người dùng. Bạn xác nhận xóa điểm này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (AppDatabase h = new AppDatabase(this)) {
                        boolean ok = KhoDuLieuThamQuan.xoaDiem(this, h.getWritableDatabase(), idDiaDiem, id);
                        if (ok) { thongBao("Đã xóa điểm tham quan"); hienDanhSach(); }
                        else thongBao("Không thể xóa điểm: có thể đã có lịch sử nghe");
                    } catch (SecurityException ex) { thongBao("Bạn không có quyền quản trị"); finish(); }
                    catch (Exception ex) { android.util.Log.e("APP_CK_ADMIN_DIEM", "Lỗi xóa điểm", ex); thongBao("Không thể xóa điểm tham quan"); }
                }).show();
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private void thongBao(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
