package com.example.app_ck.ui.admin;

import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private static final int GREEN = Color.rgb(44, 115, 81);
    private static final int INK = Color.rgb(32, 56, 42);
    private static final int MUTED = Color.rgb(105, 124, 110);

    private final ArrayList<DiaDiem> diaDiem = new ArrayList<>();
    private final ArrayList<DiemThamQuan> diemThamQuan = new ArrayList<>();
    private Spinner spDiaDiem, spDiem;
    private EditText edtLat, edtLon, edtRadius, edtNguon;
    private TextView moTaCheDo, trangThai;
    private SwitchCompat swChoPhep;
    private LinearLayout khungNguon;
    private boolean dangNap = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!coQuyen()) { finish(); return; }
        taoGiaoDien();
        apDungKhoangTrongHeThong();
        napDiaDiem();
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

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }

    private TextView nhan(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(INK);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    private EditText oSo(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setTextColor(INK);
        e.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
        return e;
    }

    private void addLabel(LinearLayout parent, String text) {
        TextView v = nhan(text, 14, true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(16);
        parent.addView(v, p);
    }

    private String text(EditText e) {
        return e.getText().toString().trim();
    }

    private void taoGiaoDien() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(246, 248, 245));
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(68)));

        TextView back = nhan("‹", 32, false);
        back.setGravity(Gravity.CENTER);
        header.addView(back, new LinearLayout.LayoutParams(dp(48), -1));
        back.setContentDescription("Quay về quản trị");
        back.setOnClickListener(v -> finish());

        TextView title = nhan("Cấu hình GPS AUTO", 21, true);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        root.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(8), dp(20), dp(28));
        scroller.addView(content);

        TextView hint = nhan("Chỉ cấu hình dữ liệu GPS thực. Marker X/Y trên ảnh sơ đồ không bị thay đổi.", 13, false);
        hint.setTextColor(MUTED);
        content.addView(hint);

        addLabel(content, "Địa điểm du lịch");
        spDiaDiem = new Spinner(this);
        content.addView(spDiaDiem, new LinearLayout.LayoutParams(-1, dp(52)));

        addLabel(content, "Mốc cấu hình");
        spDiem = new Spinner(this);
        content.addView(spDiem, new LinearLayout.LayoutParams(-1, dp(52)));

        moTaCheDo = nhan("", 13, false);
        moTaCheDo.setTextColor(MUTED);
        LinearLayout.LayoutParams m = new LinearLayout.LayoutParams(-1, -2);
        m.topMargin = dp(8);
        content.addView(moTaCheDo, m);

        swChoPhep = new SwitchCompat(this);
        swChoPhep.setText("Cho phép GPS AUTO tại điểm thành phần");
        swChoPhep.setTextColor(INK);
        LinearLayout.LayoutParams swp = new LinearLayout.LayoutParams(-1, -2);
        swp.topMargin = dp(18);
        content.addView(swChoPhep, swp);

        addLabel(content, "Vĩ độ GPS (latitude)");
        edtLat = oSo("Ví dụ: 10.777000");
        content.addView(edtLat);

        addLabel(content, "Kinh độ GPS (longitude)");
        edtLon = oSo("Ví dụ: 106.695300");
        content.addView(edtLon);

        addLabel(content, "Bán kính kích hoạt (mét)");
        edtRadius = oSo("Ví dụ: 35");
        edtRadius.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        content.addView(edtRadius);

        khungNguon = new LinearLayout(this);
        khungNguon.setOrientation(LinearLayout.VERTICAL);
        addLabel(khungNguon, "Nguồn / ghi chú xác minh tọa độ GPS");
        edtNguon = new EditText(this);
        edtNguon.setTextColor(INK);
        edtNguon.setTextSize(14);
        edtNguon.setSingleLine(false);
        edtNguon.setMinLines(2);
        edtNguon.setHint("Ví dụ: đo GPS thực địa; tài liệu tọa độ đã kiểm chứng");
        khungNguon.addView(edtNguon);
        content.addView(khungNguon);

        trangThai = nhan("", 13, false);
        trangThai.setTextColor(MUTED);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(-1, -2);
        stp.topMargin = dp(20);
        content.addView(trangThai, stp);

        Button save = new Button(this);
        save.setText("Lưu cấu hình GPS AUTO");
        save.setAllCaps(false);
        save.setTextColor(Color.WHITE);
        save.setBackgroundTintList(ColorStateList.valueOf(GREEN));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(54));
        bp.topMargin = dp(20);
        content.addView(save, bp);
        save.setOnClickListener(v -> xacNhanLuu());

        TextView disclaimer = nhan("Lưu cấu hình không tự bật GPS hay phát thuyết minh. Ứng dụng chỉ nhận diện khi người dùng chủ động bật GPS thật. AUTO điểm thành phần cần bộ theo dõi GPS sử dụng cấu hình này.", 12, false);
        disclaimer.setTextColor(MUTED);
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(-1, -2);
        dp2.topMargin = dp(14);
        content.addView(disclaimer, dp2);

        spDiaDiem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                if (!dangNap) napDiem();
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        spDiem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                if (!dangNap) hienCauHinh();
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
    }

    private void napDiaDiem() {
        try (AppDatabase helper = new AppDatabase(this)) {
            diaDiem.clear();
            diaDiem.addAll(helper.danhSachDiaDiem());
            ArrayList<String> names = new ArrayList<>();
            for (DiaDiem d : diaDiem) names.add(d.ten);
            dangNap = true;
            ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDiaDiem.setAdapter(a);
            dangNap = false;
            napDiem();
        } catch (Exception e) {
            Log.e(TAG, "Khong tai duoc dia diem", e);
            bao("Không tải được danh sách địa điểm");
        }
    }

    private DiaDiem diaDiemDangChon() {
        int i = spDiaDiem.getSelectedItemPosition();
        return i >= 0 && i < diaDiem.size() ? diaDiem.get(i) : null;
    }

    private DiemThamQuan diemDangChon() {
        int i = spDiem.getSelectedItemPosition() - 1;
        return i >= 0 && i < diemThamQuan.size() ? diemThamQuan.get(i) : null;
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
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDiem.setAdapter(a);
        spDiem.setSelection(0);
        dangNap = false;
        hienCauHinh();
    }

    private static String so(double number) {
        return Double.isFinite(number) ? String.format(Locale.US, "%.7f", number) : "";
    }

    private void hienCauHinh() {
        DiaDiem d = diaDiemDangChon();
        DiemThamQuan diem = diemDangChon();
        boolean main = diem == null;
        boolean havePlace = d != null;
        swChoPhep.setVisibility(main ? View.GONE : View.VISIBLE);
        khungNguon.setVisibility(main ? View.GONE : View.VISIBLE);
        swChoPhep.setChecked(!main && diem.choPhepAuto);
        edtLat.setText(!havePlace ? "" : so(main ? d.viDo : diem.viDo));
        edtLon.setText(!havePlace ? "" : so(main ? d.kinhDo : diem.kinhDo));
        edtRadius.setText(!havePlace ? "" : so(main ? d.banKinhAuto : diem.banKinh));
        edtNguon.setText(main ? "" : (diem.nguonGps == null ? "" : diem.nguonGps));

        moTaCheDo.setText(main
                ? "AUTO MAIN: Khi người dùng bật GPS thật ở Trang chủ, hệ thống nhận diện địa điểm chính theo tọa độ và bán kính này."
                : "AUTO SUB: Cấu hình tọa độ thật cho điểm thành phần. Không dùng tọa độ X/Y trên ảnh sơ đồ.");
        trangThai.setText(!havePlace ? "Chưa có địa điểm để cấu hình"
                : main ? "Phạm vi GPS MAIN: " + d.banKinhAuto + " m. Cần GPS đủ chính xác và người dùng xác nhận trước khi phát."
                : diem.choPhepAuto ? "Điểm đã được đánh dấu cho phép AUTO trong SQLite. Chưa đồng nghĩa bộ theo dõi AUTO SUB đang chạy."
                : "Điểm đang ở chế độ nghe thủ công (không cho phép AUTO SUB)." );
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
            new AlertDialog.Builder(this)
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
        if (lat == 0 && lon == 0) { bao("Không sử dụng tọa độ GPS 0,0"); return; }
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
        new AlertDialog.Builder(this)
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
                int oldPosition = spDiem.getSelectedItemPosition();
                napDiem();
                if (oldPosition > 0 && oldPosition < spDiem.getCount()) {
                    spDiem.setSelection(oldPosition);
                    hienCauHinh();
                }
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
                // Reload both main point and subpoint values from SQLite without modifying Marker X/Y.
                int diaDiemPosition = spDiaDiem.getSelectedItemPosition();
                int diemPosition = spDiem.getSelectedItemPosition();
                try (AppDatabase dbReload = new AppDatabase(this)) {
                    diaDiem.clear();
                    diaDiem.addAll(dbReload.danhSachDiaDiem());
                }
                if (diaDiemPosition >= 0 && diaDiemPosition < diaDiem.size()) {
                    dangNap = true;
                    spDiaDiem.setSelection(diaDiemPosition);
                    dangNap = false;
                    napDiem();
                    if (diemPosition >= 0 && diemPosition < spDiem.getCount()) {
                        spDiem.setSelection(diemPosition);
                        hienCauHinh();
                    }
                }
            } else bao("Không thể lưu cấu hình GPS AUTO");
        } catch (Exception e) {
            Log.e(TAG, "Loi luu GPS", e);
            bao("Lỗi lưu cấu hình GPS AUTO");
        }
    }

    private void bao(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
