package com.example.app_ck.ui.admin;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.app_ck.R;
import androidx.appcompat.widget.SwitchCompat;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * UC08 - Cau hinh GPS AUTO cho dia diem chinh va diem thanh phan.
 * Chi ghi SQLite qua KhoDuLieuThamQuan; khong bat GPS hay gia lap vi tri.
 * AUTO SUB la cau hinh du lieu: runtime can doc cho_phep_auto de kich hoat thuc te.
 */
public class QuanLyGpsAutoActivity extends BaseActivity {
    private static final String TAG = "APP_CK_AUTO_ADMIN";

    private final ArrayList<DiaDiem> diaDiem = new ArrayList<>();
    private final ArrayList<DiemThamQuan> diemThamQuan = new ArrayList<>();
    private Spinner spDiaDiem, spDiem;
    private EditText edtLat, edtLon, edtRadius, edtNguon;
    private TextView moTaCheDo, trangThai, doiTuong, trangThaiLuu;
    private Button luu;
    private int selectedPlaceId = -1, selectedPointId;
    private String initial = "";
    private boolean loadingFields;
    private SwitchCompat swChoPhep;
    private LinearLayout khungNguon;
    private boolean dangNap = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!coQuyen()) { finish(); return; }
        taoGiaoDien();
        AdminGiaoDien.apDung(this);
        apDungKhoangTrongHeThong();
        AdminGiaoDien.hoTroBanPhim(this);
        if (state != null) {
            selectedPlaceId = state.getInt("admin_place", -1);
            selectedPointId = state.getInt("admin_point", 0);
        }
        napDiaDiem();
        if (state != null && luu.isEnabled()) {
            loadingFields = true;
            String[] values = state.getStringArray("admin_values");
            EditText[] fields = {edtLat, edtLon, edtRadius, edtNguon};
            if (values != null) for (int i = 0; i < fields.length; i++) fields[i].setText(values[i]);
            swChoPhep.setChecked(state.getBoolean("admin_auto"));
            initial = state.getString("admin_initial", initial);
            loadingFields = false;
            capNhatTrangThai();
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { hoiBoThayDoi(QuanLyGpsAutoActivity.this::finish, null); }
        });
    }

    private boolean coQuyen() {
        try (AppDatabase helper = new AppDatabase(this)) {
            PhienDangNhap.yeuCauQuanTri(this, helper.getReadableDatabase());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Khong co quyen quan tri", e);
            Toast.makeText(this, "Yêu cầu tài khoản Admin đang hoạt động", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private String text(EditText e) { return e.getText().toString().trim(); }

    private void taoGiaoDien() {
        setContentView(R.layout.activity_quan_ly_gps_auto);
        AdminGiaoDien.header(this, "Cấu hình GPS AUTO", "Thiết lập vùng kích hoạt thuyết minh",
                () -> hoiBoThayDoi(this::finish, null));
        spDiaDiem = findViewById(R.id.gpsDiaDiem);
        spDiem = findViewById(R.id.gpsDiem);
        edtLat = findViewById(R.id.gpsViDo);
        edtLon = findViewById(R.id.gpsKinhDo);
        edtRadius = findViewById(R.id.gpsBanKinh);
        edtNguon = findViewById(R.id.gpsNguon);
        swChoPhep = findViewById(R.id.gpsChoPhep);
        khungNguon = findViewById(R.id.gpsKhungNguon);
        moTaCheDo = findViewById(R.id.gpsMoTa);
        trangThai = findViewById(R.id.gpsTrangThai);
        doiTuong = findViewById(R.id.gpsDoiTuong);
        trangThaiLuu = findViewById(R.id.gpsTrangThaiLuu);
        luu = findViewById(R.id.gpsLuu);
        luu.setOnClickListener(v -> xacNhanLuu());
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int f) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { if (!loadingFields) capNhatTrangThai(); }
            @Override public void afterTextChanged(android.text.Editable e) {}
        };
        for (EditText field : new EditText[]{edtLat, edtLon, edtRadius, edtNguon}) field.addTextChangedListener(watcher);
        swChoPhep.setOnCheckedChangeListener((button, checked) -> { if (!loadingFields) capNhatTrangThai(); });
        spDiaDiem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                if (dangNap || position < 0 || position >= diaDiem.size()) return;
                int nextId = diaDiem.get(position).id;
                if (nextId == selectedPlaceId) return;
                hoiBoThayDoi(() -> { selectedPlaceId = nextId; selectedPointId = 0; napDiem(); },
                        QuanLyGpsAutoActivity.this::khoiPhucLuaChon);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        spDiem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                if (dangNap || position < 0 || position > diemThamQuan.size()) return;
                int nextId = position == 0 ? 0 : diemThamQuan.get(position - 1).id;
                if (nextId == selectedPointId) return;
                hoiBoThayDoi(() -> { selectedPointId = nextId; hienCauHinh(); },
                        QuanLyGpsAutoActivity.this::khoiPhucLuaChon);
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void khoiPhucLuaChon() {
        dangNap = true;
        for (int i = 0; i < diaDiem.size(); i++) if (diaDiem.get(i).id == selectedPlaceId) spDiaDiem.setSelection(i);
        int pointPosition = 0;
        for (int i = 0; i < diemThamQuan.size(); i++) if (diemThamQuan.get(i).id == selectedPointId) pointPosition = i + 1;
        spDiem.setSelection(pointPosition);
        dangNap = false;
    }

    private String snapshot() {
        return android.text.TextUtils.join("\u001f", new String[]{edtLat.getText().toString(), edtLon.getText().toString(),
                edtRadius.getText().toString(), edtNguon.getText().toString(), String.valueOf(swChoPhep.isChecked())});
    }

    private void hoiBoThayDoi(Runnable proceed, Runnable cancel) {
        if (initial.equals(snapshot())) proceed.run();
        else AdminGiaoDien.hoiBoThayDoi(this, proceed, cancel);
    }

    private void capNhatTrangThai() {
        boolean main = selectedPointId == 0;
        boolean enabled = diaDiemDangChon() != null;
        boolean auto = main || swChoPhep.isChecked();
        trangThai.setTextColor(getColor(enabled && auto ? R.color.admin_primary : R.color.admin_secondary));
        trangThaiLuu.setText(!enabled ? "Chưa có địa điểm để cấu hình" : initial.equals(snapshot()) ? "Đã lưu cấu hình" : "Chưa lưu");
        trangThai.setText(!enabled ? "Chưa có dữ liệu" :
                (main ? "GPS MAIN" : auto ? "AUTO đang bật" : "AUTO đang tắt")
                        + (text(edtRadius).isEmpty() ? " · Chưa có bán kính" : " · Bán kính " + text(edtRadius) + " m"));
        // Khi tắt AUTO, quy trình lưu chỉ đổi cờ cho phép; không ghi các ô vị trí.
        for (EditText field : new EditText[]{edtLat, edtLon, edtRadius, edtNguon}) field.setEnabled(enabled && auto);
        swChoPhep.setEnabled(enabled);
        luu.setEnabled(enabled);
    }

    private void napDiaDiem() {
        try (AppDatabase helper = new AppDatabase(this)) {
            diaDiem.clear();
            diaDiem.addAll(helper.danhSachDiaDiem());
            ArrayList<String> names = new ArrayList<>();
            for (DiaDiem d : diaDiem) names.add(d.ten);
            dangNap = true;
            if (names.isEmpty()) names.add("Chưa có địa điểm");
            ArrayAdapter<String> a = AdminGiaoDien.adapter(this, names);
            spDiaDiem.setAdapter(a);
            boolean found = false;
            for (DiaDiem place : diaDiem) if (place.id == selectedPlaceId) found = true;
            if (!found) { selectedPlaceId = diaDiem.isEmpty() ? -1 : diaDiem.get(0).id; selectedPointId = 0; }
            spDiaDiem.setEnabled(!diaDiem.isEmpty());
            khoiPhucLuaChon();
            dangNap = false;
            napDiem();
        } catch (Exception e) {
            Log.e(TAG, "Khong tai duoc dia diem", e);
            bao("Không tải được danh sách địa điểm");
        }
    }

    private DiaDiem diaDiemDangChon() {
        for (DiaDiem place : diaDiem) if (place.id == selectedPlaceId) return place;
        return null;
    }

    private DiemThamQuan diemDangChon() {
        for (DiemThamQuan point : diemThamQuan) if (point.id == selectedPointId) return point;
        return null;
    }

    private void napDiem() {
        DiaDiem d = diaDiemDangChon();
        diemThamQuan.clear();
        ArrayList<String> names = new ArrayList<>();
        names.add("Địa điểm chính · AUTO MAIN");
        if (d != null) {
            try (AppDatabase helper = new AppDatabase(this)) {
                diemThamQuan.addAll(KhoDuLieuThamQuan.danhSach(helper.getReadableDatabase(), d.id));
            } catch (Exception e) {
                Log.e(TAG, "Khong tai duoc diem", e);
                bao("Không tải được điểm thành phần");
            }
        }
        for (DiemThamQuan item : diemThamQuan) {
            names.add("#" + item.id + " · " + item.ten);
        }
        dangNap = true;
        ArrayAdapter<String> a = AdminGiaoDien.adapter(this, names);
        spDiem.setAdapter(a);
        if (selectedPointId > 0 && diemDangChon() == null) selectedPointId = 0;
        khoiPhucLuaChon();
        spDiem.setEnabled(d != null);
        dangNap = false;
        hienCauHinh();
    }

    private static String so(double number) {
        return Double.isFinite(number) ? Double.toString(number) : "";
    }

    private void hienCauHinh() {
        DiaDiem d = diaDiemDangChon();
        DiemThamQuan diem = diemDangChon();
        boolean main = selectedPointId == 0;
        boolean havePlace = d != null && (main || diem != null);
        loadingFields = true;
        swChoPhep.setVisibility(main ? View.GONE : View.VISIBLE);
        khungNguon.setVisibility(main ? View.GONE : View.VISIBLE);
        swChoPhep.setChecked(!main && diem != null && diem.choPhepAuto);
        edtLat.setText(!havePlace ? "" : so(main ? d.viDo : diem.viDo));
        edtLon.setText(!havePlace ? "" : so(main ? d.kinhDo : diem.kinhDo));
        float radius = !havePlace ? Float.NaN : main ? d.banKinhAuto : diem.banKinh;
        edtRadius.setText(Float.isFinite(radius) ? Float.toString(radius) : "");
        edtNguon.setText(main || diem == null ? "" : diem.nguonGps);
        for (EditText field : new EditText[]{edtLat, edtLon, edtRadius, edtNguon}) field.setError(null);
        doiTuong.setText(!havePlace ? "Chưa có địa điểm" : main ? d.ten : diem.ten);
        moTaCheDo.setText(main
                ? "Địa điểm chính · Nhận diện khi người dùng bật GPS thật."
                : "Điểm thành phần · Bật AUTO cần tọa độ đã xác minh.");
        initial = snapshot();
        loadingFields = false;
        capNhatTrangThai();
    }

    private double docSo(EditText e, String ten, double min, double max) {
        try {
            double v = Double.parseDouble(text(e));
            if (!Double.isFinite(v) || v < min || v > max) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException ex) {
            e.setError(ten + " phải từ " + min + " đến " + max);
            e.requestFocus();
            return Double.NaN;
        }
    }

    private void xacNhanLuu() {
        DiaDiem place = diaDiemDangChon();
        if (place == null) { bao("Chưa chọn địa điểm"); return; }
        DiemThamQuan diem = diemDangChon();
        boolean main = diem == null;
        boolean enabled = !main && swChoPhep.isChecked();

        if (!main && !enabled) {
            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                    .setTitle("Tắt GPS AUTO cho điểm thành phần?")
                    .setMessage("Chỉ cập nhật trạng thái cho phép AUTO thành TẮT. Tọa độ GPS đã lưu và Marker X/Y sẽ được giữ nguyên.")
                    .setNegativeButton("Hủy", null)
                    .setPositiveButton("Lưu trạng thái TẮT", (d, which) -> luuDiemTat(place.id, diem.id))
                    .show();
            return;
        }
        double lat = docSo(edtLat, "Vĩ độ", -90, 90);
        if (!Double.isFinite(lat)) return;
        double lon = docSo(edtLon, "Kinh độ", -180, 180);
        if (!Double.isFinite(lon)) return;
        if (lat == 0 && lon == 0) { edtLat.setError("Không sử dụng tọa độ GPS 0,0"); edtLat.requestFocus(); return; }
        double r = docSo(edtRadius, "Bán kính", 0.1, 10000);
        if (!Double.isFinite(r)) return;
        if (!main && text(edtNguon).isEmpty()) {
            edtNguon.setError("Hãy ghi rõ nguồn hoặc cách xác minh tọa độ GPS");
            edtNguon.requestFocus();
            return;
        }
        int pointId = main ? 0 : diem.id;
        String name = main ? place.ten : diem.ten;
        String message = String.format(Locale.US,
                "Mốc: %s\nVĩ độ: %.7f\nKinh độ: %.7f\nBán kính: %.1f m\n\nXác nhận lưu cấu hình GPS thực?",
                name, lat, lon, r);
        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Admin_Dialog)
                .setTitle(main ? "Lưu GPS địa điểm chính?" : "Bật GPS AUTO cho điểm này?")
                .setMessage(message)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (dialog, which) ->
                        luuGps(place.id, pointId, lat, lon, (float) r, main ? "" : text(edtNguon)))
                .show();
    }

    private void luuDiemTat(int placeId, int pointId) {
        try (AppDatabase helper = new AppDatabase(this)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            PhienDangNhap.yeuCauQuanTri(this, db);
            android.content.ContentValues v = new android.content.ContentValues();
            v.put("cho_phep_auto", 0);
            boolean ok = KhoDuLieuThamQuan.luuDiem(this, db, placeId, pointId, v);
            if (ok) {
                bao("Đã tắt cho phép AUTO; giữ nguyên tọa độ đã lưu");
                AdminGiaoDien.anBanPhim(this);
                napDiaDiem();
            } else bao("Không lưu được trạng thái AUTO");
        } catch (Exception e) {
            Log.e(TAG, "Khong luu duoc trang thai", e);
            bao("Không lưu được trạng thái GPS AUTO");
        }
    }

    private void luuGps(int placeId, int pointId, double lat, double lon, float radius, String source) {
        try (AppDatabase helper = new AppDatabase(this)) {
            SQLiteDatabase db = helper.getWritableDatabase();
            PhienDangNhap.yeuCauQuanTri(this, db);
            boolean ok = KhoDuLieuThamQuan.luuCauHinhAuto(
                    this, db, placeId, pointId, pointId > 0, lat, lon, radius, source);
            if (ok) {
                bao(pointId == 0 ? "Đã cập nhật GPS địa điểm chính" : "Đã lưu cấu hình AUTO điểm thành phần");
                AdminGiaoDien.anBanPhim(this);
                napDiaDiem();
            } else bao("Không thể lưu cấu hình GPS AUTO");
        } catch (Exception e) {
            Log.e(TAG, "Loi luu GPS", e);
            bao("Lỗi lưu cấu hình GPS AUTO");
        }
    }

    private void bao(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override protected void onResume() {
        super.onResume();
        if (!coQuyen()) finish();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("admin_place", selectedPlaceId);
        state.putInt("admin_point", selectedPointId);
        state.putStringArray("admin_values", new String[]{edtLat.getText().toString(), edtLon.getText().toString(),
                edtRadius.getText().toString(), edtNguon.getText().toString()});
        state.putBoolean("admin_auto", swChoPhep.isChecked());
        state.putString("admin_initial", initial);
    }

}
