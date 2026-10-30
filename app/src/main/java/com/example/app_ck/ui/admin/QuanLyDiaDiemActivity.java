package com.example.app_ck.ui.admin;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.database.DatabaseUtils;
import android.text.TextUtils;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import java.util.ArrayList;
import java.util.Locale;

public class QuanLyDiaDiemActivity extends BaseActivity {
    private LinearLayout danhSach;
    private EditText timKiem;
    private TextView tong, trong;
    private ArrayList<DiaDiem> duLieu = new ArrayList<>();
    private View manDanhSach, manBieuMau;
    private EditText ten, diaChi, moTa, viDo, kinhDo, tenNguon, urlNguon;
    private Spinner danhMuc;
    private ImageView anh;
    private Uri anhMoi;
    private String anhHienTai = "";
    private int id = -1;


    private final ActivityResultLauncher<String> layAnh = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && anh != null) { anhMoi = uri; anh.setImageURI(uri); }
            });

    private int dp(int x) { return Math.round(x * getResources().getDisplayMetrics().density); }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!coQuyen()) { finish(); return; }
        setContentView(R.layout.activity_quan_ly_dia_diem);
        apDungKhoangTrongHeThong();
        manDanhSach = findViewById(R.id.qlddManDanhSach);
        manBieuMau = findViewById(R.id.qlddManBieuMau);
        ten = findViewById(R.id.ddFormTen);
        diaChi = findViewById(R.id.ddFormDiaChi);
        moTa = findViewById(R.id.ddFormMoTa);
        viDo = findViewById(R.id.ddFormViDo);
        kinhDo = findViewById(R.id.ddFormKinhDo);
        danhMuc = findViewById(R.id.ddFormDanhMuc);
        anh = findViewById(R.id.ddFormAnh);
        tenNguon = findViewById(R.id.ddFormTenNguon);
        urlNguon = findViewById(R.id.ddFormUrlNguon);
        findViewById(R.id.ddFormQuayLai).setOnClickListener(v -> dongBieuMau());
        findViewById(R.id.ddFormChonAnh).setOnClickListener(v -> layAnh.launch("image/*"));
        findViewById(R.id.ddFormLuu).setOnClickListener(v -> luu());
        findViewById(R.id.ddFormXoa).setOnClickListener(v -> xacNhanXoa());
        danhSach = findViewById(R.id.qlddDanhSach);
        timKiem = findViewById(R.id.qlddTimKiem);
        tong = findViewById(R.id.qlddTongSo);
        trong = findViewById(R.id.qlddTrong);
        findViewById(R.id.qlddQuayLai).setOnClickListener(v -> finish());
        findViewById(R.id.qlddThemMoi).setOnClickListener(v -> moBieuMau(-1));
        timKiem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { hienThi(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        taiDuLieu();
    }
    @Override protected void onResume() {
        super.onResume();
        if (!coQuyen()) { finish(); return; }
        if (danhSach != null) taiDuLieu();
    }
    private boolean coQuyen() {
        try (AppDatabase helper = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, helper.getReadableDatabase()); return true;
        } catch (Exception e) { return false; }
    }
    private void taiDuLieu() {
        try (AppDatabase db = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, db.getReadableDatabase());
            duLieu = db.danhSachDiaDiem(); hienThi();
        } catch (Exception e) { Toast.makeText(this, "Không tải được danh sách địa điểm", Toast.LENGTH_SHORT).show(); }
    }
    private void hienThi() {
        if (danhSach == null) return;
        danhSach.removeAllViews();
        String key = timKiem.getText().toString().trim().toLowerCase(Locale.ROOT);
        int count = 0;
        for (DiaDiem d : duLieu) {
            String haystack = (s(d.ten) + " " + s(d.diaChi) + " " + s(d.danhMuc)).toLowerCase(Locale.ROOT);
            if (!haystack.contains(key)) continue;
            count++;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(14), dp(14), dp(14));
            GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.WHITE);
            bg.setCornerRadius(dp(16)); bg.setStroke(dp(1), Color.rgb(232,237,232));
            row.setBackground(bg); row.setClickable(true); row.setFocusable(true);
            TextView icon = new TextView(this); icon.setText("⌖"); icon.setTextSize(25);
            icon.setTextColor(Color.rgb(44,115,81)); icon.setGravity(Gravity.CENTER);
            row.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(40)));
            LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(12),0,0,0);
            TextView title = new TextView(this); title.setText(s(d.ten)); title.setTextSize(15);
            title.setTextColor(Color.rgb(32,56,42)); title.setTypeface(null, Typeface.BOLD);
            TextView desc = new TextView(this); desc.setText(s(d.diaChi)); desc.setTextSize(12);
            desc.setTextColor(Color.rgb(113,130,119)); desc.setPadding(0,dp(5),0,0);
            info.addView(title); info.addView(desc);
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            TextView arrow = new TextView(this); arrow.setText("›"); arrow.setTextSize(26);
            arrow.setTextColor(Color.GRAY); row.addView(arrow);
            row.setOnClickListener(v -> moBieuMau(d.id));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(10);
            danhSach.addView(row,lp);
        }
        tong.setText("DANH SÁCH · " + count + " ĐỊA ĐIỂM");
        trong.setVisibility(count==0?View.VISIBLE:View.GONE);
    }
    // Biểu mẫu nằm trong cùng Activity, không mở SuaDiaDiemActivity.
    private void moBieuMau(int idDiaDiem) {
        id = idDiaDiem;
        anhMoi = null;
        anhHienTai = "";
        ten.setText(""); diaChi.setText(""); moTa.setText("");
        tenNguon.setText(""); urlNguon.setText("");
        viDo.setText(""); kinhDo.setText("");
        anh.setImageResource(android.R.drawable.ic_menu_gallery);
        taoDanhMuc(null);
        ((TextView)findViewById(R.id.ddFormTieuDe)).setText(id < 0 ? "Thêm địa điểm" : "Chỉnh sửa địa điểm");
        ((Button)findViewById(R.id.ddFormXoa)).setVisibility(id < 0 ? View.GONE : View.VISIBLE);
        if (id >= 0 && !taiThongTin()) return;
        manDanhSach.setVisibility(View.GONE);
        manBieuMau.setVisibility(View.VISIBLE);
    }

    private void dongBieuMau() {
        manBieuMau.setVisibility(View.GONE);
        manDanhSach.setVisibility(View.VISIBLE);
        id = -1;
        anhMoi = null;
    }

    @Override public void onBackPressed() {
        if (manBieuMau != null && manBieuMau.getVisibility() == View.VISIBLE) {
            dongBieuMau();
        } else { super.onBackPressed(); }
    }

    // Lay danh muc THUC TE tu bang dia_diem. Khong hardcode danh muc moi.
    private void taoDanhMuc(String hienTai) {
        ArrayList<String> items = new ArrayList<>();
        try (AppDatabase helper = new AppDatabase(this);
             Cursor c = helper.getReadableDatabase().rawQuery(
                     "SELECT DISTINCT TRIM(danh_muc) FROM dia_diem " +
                     "WHERE danh_muc IS NOT NULL AND TRIM(danh_muc)<>'' " +
                     "ORDER BY danh_muc COLLATE NOCASE", null)) {
            while (c.moveToNext()) {
                String dm = c.getString(0);
                if (!items.contains(dm)) items.add(dm);
            }
        } catch (Exception e) {
            android.util.Log.e("APP_CK_DIA_DIEM", "Khong lay duoc danh muc", e);
        }
        if (hienTai != null && !hienTai.trim().isEmpty() && !items.contains(hienTai))
            items.add(0, hienTai);
        // Khong them danh muc tuy y, chi su dung trong DB; khi chua co thi thong bao.
        if (items.isEmpty()) items.add("Chưa có danh mục");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        danhMuc.setAdapter(adapter);
        if (hienTai != null && items.contains(hienTai)) danhMuc.setSelection(items.indexOf(hienTai));
    }

    private boolean taiThongTin() {
        try (AppDatabase db = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, db.getReadableDatabase());
            DiaDiem found = null;
            for (DiaDiem d : db.danhSachDiaDiem()) if (d.id == id) { found = d; break; }
            if (found == null) { thongBao("Địa điểm không còn tồn tại"); return false; }
            ten.setText(s(found.ten)); diaChi.setText(s(found.diaChi)); moTa.setText(s(found.moTa));
            viDo.setText(String.format(Locale.US,"%.6f",found.viDo));
            kinhDo.setText(String.format(Locale.US,"%.6f",found.kinhDo));
            taoDanhMuc(found.danhMuc); anhHienTai = s(found.tenAnh);
            AnhDiaDiem.hien(anh, anhHienTai);
            // Nguon quan tri rieng; khong ghi de nguon mac dinh cua project.
            try (Cursor c = db.getReadableDatabase().rawQuery(
                    "SELECT n.ten,n.url FROM nguon_thong_tin n " +
                    "JOIN bai_nguon b ON b.ma_nguon=n.ma " +
                    "WHERE b.dia_diem_id=? AND b.diem_id=0 AND n.ma=? LIMIT 1",
                    new String[]{String.valueOf(id), "admin_dd_" + id})) {
                if (c.moveToFirst()) { tenNguon.setText(s(c.getString(0))); urlNguon.setText(s(c.getString(1))); }
            }
            return true;
        } catch (Exception e) { thongBao("Không tải được địa điểm"); return false; }
    }

    private void luu() {
        String t = value(ten);
        if (t.isEmpty()) { ten.setError("Tên địa điểm không được để trống"); ten.requestFocus(); return; }
        double lat, lon;
        try { lat = Double.parseDouble(value(viDo)); lon = Double.parseDouble(value(kinhDo)); }
        catch (Exception e) { thongBao("Hãy chọn hoặc nhập tọa độ"); return; }
        if (!Double.isFinite(lat) || !Double.isFinite(lon) || lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            thongBao("Tọa độ không hợp lệ"); return;
        }
        if (danhMuc.getSelectedItem() == null || "Chưa có danh mục".equals(danhMuc.getSelectedItem().toString())) {
            thongBao("Chưa có danh mục trong hệ thống; vui lòng bổ sung dữ liệu danh mục trước"); return;
        }
        String nguonTen = value(tenNguon), nguonUrl = value(urlNguon);
        if ((!nguonTen.isEmpty() || !nguonUrl.isEmpty()) &&
                (nguonTen.isEmpty() || !nguonUrl.matches("(?i)^https?://.+"))) {
            thongBao("Nguồn cần có tên và địa chỉ http(s) hợp lệ"); return;
        }
        try (AppDatabase helper = new AppDatabase(this)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            PhienDangNhap.yeuCauQuanTri(this, db);
            int targetId = id;
            if (targetId < 0) {
                try (Cursor c = db.rawQuery("SELECT COALESCE(MAX(id), -1) + 1 FROM dia_diem",null)) {
                    if (!c.moveToFirst()) throw new IllegalStateException("Không xác định được ID");
                    targetId = c.getInt(0);
                }
            }
            String newName = anhHienTai;
            if (anhMoi != null) newName = AnhDiaDiem.luuTuMay(this, anhMoi);
            ContentValues v = new ContentValues();
            v.put("ten",t); v.put("dia_chi",value(diaChi));
            v.put("danh_muc",danhMuc.getSelectedItem().toString());
            v.put("mo_ta",value(moTa)); v.put("vi_do",lat); v.put("kinh_do",lon);
            v.put("ten_anh",newName);
            boolean saved = KhoDuLieuThamQuan.luuDiaDiem(this,db,targetId,v);
            if (saved) {
                boolean nguonOk = luuNguon(db, targetId, nguonTen, nguonUrl);
                thongBao(nguonOk ? "Đã lưu địa điểm" : "Đã lưu địa điểm, nhưng không lưu được nguồn");
                dongBieuMau(); taiDuLieu();
            } else thongBao("Không thể lưu địa điểm");
        } catch (SecurityException e) { thongBao("Không có quyền quản trị"); finish(); }
        catch (Exception e) { android.util.Log.e("APP_CK_DIA_DIEM","Không lưu được",e); thongBao("Lỗi lưu địa điểm hoặc ảnh"); }
    }

    private boolean luuNguon(SQLiteDatabase db, int diaDiemId, String ten, String url) {
        if (ten.isEmpty() && url.isEmpty()) return true; // Giữ nguyên nguồn cũ khi để trống.
        String ma = "admin_dd_" + diaDiemId;
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(this, db);
            ContentValues n = new ContentValues();
            n.put("ma", ma); n.put("ten", ten); n.put("url", url); n.put("co_quan", ten);
            db.insertWithOnConflict("nguon_thong_tin", null, n, SQLiteDatabase.CONFLICT_REPLACE);
            ContentValues b = new ContentValues();
            b.put("dia_diem_id", diaDiemId); b.put("diem_id", 0);
            b.put("ma_nguon", ma); b.put("thu_tu", 999);
            db.insertWithOnConflict("bai_nguon", null, b, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful(); return true;
        } catch (Exception e) {
            android.util.Log.e("APP_CK_DIA_DIEM", "Khong luu duoc nguon", e);
            return false;
        } finally { db.endTransaction(); }
    }

    private void xacNhanXoa() {
        if (id < 0) return;
        new MaterialAlertDialogBuilder(this).setTitle("Xóa địa điểm?")
                .setMessage("Hệ thống chỉ cho xóa khi địa điểm không còn dữ liệu tham quan, yêu thích, lịch sử hoặc liên kết khác.")
                .setNegativeButton("Hủy",null)
                .setPositiveButton("Xóa",(dialog,which)-> xoa())
                .show();
    }
    private void xoa() {
        try (AppDatabase helper = new AppDatabase(this)) {
            if (KhoDuLieuThamQuan.xoaDiaDiem(this,helper.getWritableDatabase(),id)) {
                thongBao("Đã xóa địa điểm"); dongBieuMau(); taiDuLieu();
            } else {
                new MaterialAlertDialogBuilder(this).setTitle("Không thể xóa")
                        .setMessage("Địa điểm còn dữ liệu liên quan hoặc được hệ thống sử dụng. Không tự động xóa dữ liệu liên kết.")
                        .setPositiveButton("Đã hiểu",null).show();
            }
        } catch (Exception e) { thongBao("Lỗi xóa địa điểm"); }
    }
    
    private String value(EditText e) { return e.getText().toString().trim(); }
    private void thongBao(String msg) { Toast.makeText(this,msg,Toast.LENGTH_SHORT).show(); }
    private String s(String t) { return t==null?"":t; }
}
