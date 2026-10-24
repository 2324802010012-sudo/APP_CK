package com.example.app_ck;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Theo dõi địa điểm chính tại trang chủ; hỏi xác nhận trước khi đọc. */
public final class ThamQuanTuDong implements LocationListener {
    static final int THU_CONG = 0, DEMO = 1, GPS = 2;
    private static final int[] KHOANG_CACH_DEMO = {500, 300, 150, 90};
    private final MainActivity activity;
    private final Handler hen = new Handler(Looper.getMainLooper());
    private final LocationManager viTri;
    private final List<DiaDiem> cacDiem;
    private final View bang;
    private final Spinner chonDemo;
    private BoNhanDienDiaDiem nhanDien;
    private DiaDiem diemDangMoi, diemDemo;
    private AlertDialog hoiThoai;
    private int cheDo, phien, buocDemo;
    private long lucBuocDemo, lucGpsMoi;

    ThamQuanTuDong(MainActivity activity) {
        this.activity = activity;
        viTri = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        try (TaiKhoanDb db = new TaiKhoanDb(activity)) { cacDiem = db.danhSachDiaDiem(); }
        ViewGroup cha = activity.findViewById(R.id.khungThuyetMinhTuDong);
        bang = activity.getLayoutInflater().inflate(R.layout.view_tham_quan_tu_dong, cha, false);
        cha.addView(bang);
        chonDemo = bang.findViewById(R.id.autoChonDiaDiemDemo);
        List<String> ten = new ArrayList<>();
        for (DiaDiem d : cacDiem) ten.add(d.ten);
        chonDemo.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, ten));
        chonDemo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int position, long id) {
                if (cheDo == DEMO && diemDemo != null && diemDemo.id != cacDiem.get(position).id)
                    dung("Đã đổi điểm demo. Bấm Chạy demo để bắt đầu tuyến mới.");
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        bang.findViewById(R.id.autoDemo).setOnClickListener(v -> batDemo());
        bang.findViewById(R.id.autoGps).setOnClickListener(v -> batGps());
        bang.findViewById(R.id.autoDung).setOnClickListener(v -> dung("Đã dừng hướng dẫn theo vị trí và giọng đọc."));
        bang.findViewById(R.id.autoDemoDenGan).setOnClickListener(v -> {
            if (cheDo == DEMO && buocDemo < KHOANG_CACH_DEMO.length - 1) {
                buocDemo++;
                lucBuocDemo = SystemClock.elapsedRealtime();
            }
        });
        bang.findViewById(R.id.autoMoChiTiet).setOnClickListener(v -> moChiTiet(false));
    }

    private void moChiTiet(boolean phatSauXacNhan) {
        if (diemDangMoi == null) return;
        int id = diemDangMoi.id;
        dung("Đang mở chi tiết " + diemDangMoi.ten + ".");
        activity.startActivity(new Intent(activity, ChiTietDiaDiemActivity.class)
                .putExtra("viTriDiaDiem", id)
                .putExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI, phatSauXacNhan));
    }

    private void chu(int id, String value) { ((TextView) bang.findViewById(id)).setText(value); }
    private void trangThai(String value) { chu(R.id.autoTrangThai, value); }
    public void dung(String lyDo) {
        phien++;
        cheDo = THU_CONG;
        if (nhanDien != null) nhanDien.huy();
        if (hoiThoai != null) { hoiThoai.dismiss(); hoiThoai = null; }
        hen.removeCallbacksAndMessages(null);
        if (viTri != null) viTri.removeUpdates(this);
        if (activity.mayDocGioiThieu() != null) activity.mayDocGioiThieu().stop();
        chu(R.id.autoCheDo, "Chưa bật hướng dẫn");
        chu(R.id.autoViTri, "Đã tắt theo dõi vị trí");
        bang.findViewById(R.id.autoDemoDenGan).setVisibility(View.GONE);
        trangThai(lyDo);
    }
    private boolean batDau(int loai) {
        dung("");
        if (cacDiem.isEmpty()) { trangThai("Chưa có dữ liệu địa điểm."); return false; }
        cheDo = loai;
        nhanDien = new BoNhanDienDiaDiem(cacDiem);
        diemDangMoi = null;
        lucGpsMoi = 0;
        bang.findViewById(R.id.autoMoChiTiet).setVisibility(View.GONE);
        chu(R.id.autoCheDo, loai == DEMO ? "DEMO · Vị trí mô phỏng" : "GPS thật · Hướng dẫn theo vị trí");
        bang.findViewById(R.id.autoDemoDenGan).setVisibility(loai == DEMO ? View.VISIBLE : View.GONE);
        return true;
    }
    void batDemo() {
        if (!batDau(DEMO)) return;
        diemDemo = cacDiem.get(chonDemo.getSelectedItemPosition());
        buocDemo = 0;
        lucBuocDemo = SystemClock.elapsedRealtime();
        trangThai("Mô phỏng đến " + diemDemo.ten + ": 500 → 300 → 150 → 90 m. Chưa phát âm thanh.");
        nhip(phien);
    }
    private void nhip(int phienNhip) {
        if (phienNhip != phien || cheDo == THU_CONG) return;
        long now = SystemClock.elapsedRealtime();
        if (cheDo == DEMO && diemDangMoi == null) {
            if (now - lucBuocDemo >= 2000 && buocDemo < KHOANG_CACH_DEMO.length - 1) {
                buocDemo++;
                lucBuocDemo = now;
            }
            int distance = KHOANG_CACH_DEMO[buocDemo];
            chu(R.id.autoViTri, "DEMO · " + diemDemo.ten + " · Còn " + distance + " m\n500 → 300 → 150 → 90 m");
            nhanViTri(diemDemo.viDo + distance / 111195.0, diemDemo.kinhDo, 3, now, now);
        }
        if (cheDo == GPS && !nhanDien.dangChoTraLoi()
                && (lucGpsMoi == 0 || now - lucGpsMoi > 15000))
            trangThai("Đang chờ vị trí chính xác. Trong phạm vi 100 m, ứng dụng sẽ hỏi Bắt đầu hoặc Bỏ qua.");
        hen.postDelayed(() -> nhip(phienNhip), 1000);
    }
    void batGps() {
        dung("Đang chuẩn bị GPS thật…");
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.xinQuyenGpsThamQuan();
            return;
        }
        if (viTri == null || !viTri.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trangThai("Đã có quyền vị trí. Bật GPS để nhận diện các địa điểm ở gần; demo vẫn dùng được.");
            hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Bật dịch vụ vị trí")
                    .setMessage("Bật GPS, quay lại trang chủ và bấm GPS thật để tiếp tục.")
                    .setPositiveButton("Cài đặt vị trí", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Để sau", null).show();
            return;
        }
        if (!batDau(GPS)) return;
        try {
            viTri.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            if (viTri.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                viTri.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            trangThai("Đang tìm địa điểm gần nhất trong 5 nơi. Chỉ phát sau khi bạn bấm Bắt đầu.");
            nhip(phien);
        } catch (SecurityException e) { dung("Quyền vị trí đã thay đổi. Bấm GPS thật để cấp lại."); }
    }
    void nhanQuyen(boolean coQuyen) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (coQuyen) batGps();
        else {
            dung("Chưa có quyền vị trí chính xác. Bạn vẫn có thể khám phá hoặc chạy demo; bấm GPS thật để cấp lại quyền.");
            hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Chưa có quyền vị trí chính xác")
                    .setMessage("Bạn vẫn dùng được trang chủ và demo. Nếu đã từ chối nhiều lần, hãy cấp quyền trong cài đặt ứng dụng.")
                    .setPositiveButton("Cài đặt ứng dụng", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:" + activity.getPackageName()))))
                    .setNegativeButton("Tiếp tục khám phá", null).show();
        }
    }
    @Override public void onLocationChanged(Location location) {
        if (cheDo != GPS) return;
        long now = SystemClock.elapsedRealtime();
        if (location.isFromMockProvider() || !location.hasAccuracy()) {
            lucGpsMoi = 0;
            nhanDien.capNhat(Double.NaN, 0, Float.NaN, now, now);
            trangThai(location.isFromMockProvider() ? "GPS giả lập không dùng ở chế độ thật. Hãy chọn Chạy demo."
                    : "Vị trí chưa có độ chính xác. Đang chờ GPS mới…");
            return;
        }
        long lucDo = location.getElapsedRealtimeNanos() / 1000000;
        if (lucDo <= now && now - lucDo <= 15000) lucGpsMoi = lucDo;
        nhanViTri(location.getLatitude(), location.getLongitude(), location.getAccuracy(), lucDo, now);
    }
    private void nhanViTri(double lat, double lon, float accuracy, long lucDo, long now) {
        if (nhanDien.dangChoTraLoi()) return;
        DiaDiem den = nhanDien.capNhat(lat, lon, accuracy, lucDo, now);
        if (cheDo == GPS && nhanDien.ganNhat() != null) {
            DiaDiem gan = nhanDien.ganNhat();
            double distance = BoChonDiemTuDong.khoangCach(lat, lon, gan.viDo, gan.kinhDo);
            chu(R.id.autoViTri, String.format(Locale.ROOT, "GPS: %.6f, %.6f · Sai số ±%.0f m\nGần nhất: %s · %.0f m", lat, lon, accuracy, gan.ten, distance));
        }
        if (den != null) {
            diemDangMoi = den;
            hienLoiMoi(BoChonDiemTuDong.khoangCach(lat, lon, den.viDo, den.kinhDo));
        } else trangThai(nhanDien.trangThai() + " · Bán kính nhận diện 100 m");
    }
    private void hienLoiMoi(double distance) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        final int phienHoi = phien, id = diemDangMoi.id;
        trangThai("Bạn đang ở gần " + diemDangMoi.ten + ". Đang chờ bạn xác nhận.");
        bang.findViewById(R.id.autoDemoDenGan).setVisibility(View.GONE);
        bang.findViewById(R.id.autoMoChiTiet).setVisibility(View.VISIBLE);
        hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Bạn đang ở gần " + diemDangMoi.ten)
                .setMessage((cheDo == DEMO ? "DEMO · Vị trí mô phỏng\n\n" : "")
                        + String.format(Locale.ROOT, "Khoảng cách: %.0f m (phạm vi 100 m).\n\n", distance)
                        + "Bạn có muốn bắt đầu nghe thuyết minh về địa điểm này không?")
                .setPositiveButton("Bắt đầu", (d, w) -> {
                    if (phienHoi != phien || !nhanDien.traLoi(id, true)) return;
                    moChiTiet(true);
                })
                .setNegativeButton("Bỏ qua", (d, w) -> boQua(phienHoi, id))
                .setOnCancelListener(d -> boQua(phienHoi, id)).create();
        hoiThoai.setCanceledOnTouchOutside(false);
        hoiThoai.show();
    }
    private void boQua(int phienHoi, int id) {
        if (phienHoi != phien) return;
        nhanDien.traLoi(id, false);
        trangThai("Đã bỏ qua " + diemDangMoi.ten + ". Không phát âm thanh và không hỏi lại địa điểm này trong lượt hiện tại.");
    }
    @Override public void onProviderDisabled(String provider) {
        if (cheDo == GPS && LocationManager.GPS_PROVIDER.equals(provider)) dung("GPS đã tắt. Bật lại vị trí và bấm GPS thật.");
    }
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
}
