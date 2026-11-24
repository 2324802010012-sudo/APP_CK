package com.example.app_ck.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.ui.home.MainActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Theo dõi địa điểm chính tại trang chủ bằng vị trí GPS thật. */
public final class ThamQuanTuDong implements LocationListener {
    public static final int THU_CONG = 0, GPS = 1;
    private final MainActivity activity;
    private final Handler hen = new Handler(Looper.getMainLooper());
    private final LocationManager viTri;
    private final List<DiaDiem> cacDiem;
    private final View bang;
    private BoNhanDienDiaDiem nhanDien;
    private DiaDiem diemDangMoi;
    private androidx.appcompat.app.AlertDialog hoiThoai;
    private int cheDo, phien;
    private long lucNhanGps;
    private Location mauGps;
    private final List<CancellationSignal> yeuCauViTri = new ArrayList<>();

    public ThamQuanTuDong(MainActivity activity) {
        this.activity = activity;
        viTri = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        try (AppDatabase db = new AppDatabase(activity)) { cacDiem = db.danhSachDiaDiem(); }
        ViewGroup cha = activity.findViewById(R.id.khungThuyetMinhTuDong);
        bang = activity.getLayoutInflater().inflate(R.layout.view_tham_quan_tu_dong, cha, false);
        cha.addView(bang);
        ((com.google.android.material.materialswitch.MaterialSwitch) bang.findViewById(R.id.autoGps))
                .setOnClickListener(v -> {
                    if (((com.google.android.material.materialswitch.MaterialSwitch) v).isChecked()) batGps();
                    else dung("Đã dừng hướng dẫn theo vị trí và giọng đọc.");
                });
        bang.findViewById(R.id.autoMoChiTiet).setOnClickListener(v -> moChiTiet(false));
        caiDatThuGonAuto();
    }

    private void chu(int id, String value) { ((TextView) bang.findViewById(id)).setText(value); }
    private void trangThai(String value) { chu(R.id.autoTrangThai, value); }
    private void datCongTacGps(boolean bat) {
        ((com.google.android.material.materialswitch.MaterialSwitch) bang.findViewById(R.id.autoGps))
                .setChecked(bat);
    }

    public void dung(String lyDo) {
        activity.huyLuotLichSu();
        phien++; cheDo = THU_CONG; diemDangMoi = null; mauGps = null;
        if (nhanDien != null) nhanDien.huy();
        if (hoiThoai != null) { hoiThoai.dismiss(); hoiThoai = null; }
        hen.removeCallbacksAndMessages(null);
        for (CancellationSignal yeuCau : yeuCauViTri) yeuCau.cancel();
        yeuCauViTri.clear();
        if (viTri != null) viTri.removeUpdates(this);
        if (activity.mayDocGioiThieu() != null) activity.mayDocGioiThieu().stop();
        datCongTacGps(false);
        chu(R.id.autoCheDo, "GPS chưa bật");
        bang.findViewById(R.id.autoGanNhat).setVisibility(View.GONE);
        bang.findViewById(R.id.autoMoChiTiet).setVisibility(View.GONE);
        trangThai(lyDo);
        hienThiNoiDungAuto(false);
    }

    public void batGps() {
        dung("Đang chuẩn bị GPS…");
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            datCongTacGps(true);
            activity.xinQuyenGpsThamQuan(); return;
        }
        if (viTri == null || !viTri.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trangThai("Đã có quyền vị trí. Hãy bật GPS để nhận diện các địa điểm ở gần.");
            hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                    .setTitle("Bật dịch vụ vị trí")
                    .setMessage("Cần GPS để nhận diện địa điểm ở gần bạn.")
                    .setPositiveButton("Cài đặt vị trí", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Để sau", null).show();
            return;
        }
        if (cacDiem.isEmpty()) { trangThai("Chưa có dữ liệu địa điểm."); return; }
        cheDo = GPS; nhanDien = new BoNhanDienDiaDiem(cacDiem); lucNhanGps = 0;
        datCongTacGps(true);
        chu(R.id.autoCheDo, "GPS đang bật");
        hienThiNoiDungAuto(true);
        try {
            viTri.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            if (viTri.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                viTri.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this, Looper.getMainLooper());
                layViTriMoi(LocationManager.NETWORK_PROVIDER);
            }
            layViTriMoi(LocationManager.GPS_PROVIDER);
            trangThai("Đang tìm địa điểm gần nhất. GPS đủ chính xác mới đề nghị phát thuyết minh.");
            nhip(phien);
        } catch (SecurityException e) { dung("Quyền vị trí đã thay đổi. Bấm Bật GPS để cấp lại."); }
    }

    /** Lấy mẫu mới ngay khi bật GPS, không phải chờ chu kỳ cập nhật đầu tiên của thiết bị. */
    @SuppressWarnings("MissingPermission")
    private void layViTriMoi(String nguon) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || viTri == null) return;
        int phienYeuCau = phien;
        CancellationSignal huy = new CancellationSignal();
        yeuCauViTri.add(huy);
        viTri.getCurrentLocation(nguon, huy, activity.getMainExecutor(), location -> {
            yeuCauViTri.remove(huy);
            if (location != null && phienYeuCau == phien && cheDo == GPS
                    && !activity.isFinishing() && !activity.isDestroyed()) {
                onLocationChanged(location);
            }
        });
    }

    public void nhanQuyen(boolean coQuyen) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (coQuyen) { batGps(); return; }
        dung("Chưa có quyền vị trí chính xác. Bạn vẫn có thể khám phá và nghe thủ công.");
        hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle("Cần quyền vị trí chính xác")
                .setMessage("Hãy cấp quyền vị trí trong cài đặt ứng dụng để dùng hướng dẫn GPS.")
                .setPositiveButton("Cài đặt ứng dụng", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:" + activity.getPackageName()))))
                .setNegativeButton("Tiếp tục khám phá", null).show();
    }

    private void nhip(int phienNhip) {
        if (phienNhip != phien || cheDo != GPS) return;
        long now = SystemClock.elapsedRealtime();
        if (lucNhanGps == 0 || now - lucNhanGps > 15000) {
            nhanDien.capNhat(Double.NaN, 0, Float.NaN, now, now);
            trangThai(lucNhanGps == 0 ? "Đã bật GPS, chưa nhận được vị trí. Hãy ra nơi thoáng và chờ thiết bị xác định."
                    : "Chưa có cập nhật vị trí trong 15 giây. Hãy kiểm tra GPS hoặc nghe thủ công.");
        }
        hen.postDelayed(() -> nhip(phienNhip), 1000);
    }

    @Override public void onLocationChanged(Location location) {
        if (cheDo != GPS) return;
        long now = SystemClock.elapsedRealtime();
        if (mauGps != null && !mauGps.isFromMockProvider()
                && mauGps.getElapsedRealtimeNanos() > 0
                && location.getElapsedRealtimeNanos() < mauGps.getElapsedRealtimeNanos()) return;
        lucNhanGps = now; mauGps = new Location(location);
        hienThiMauGps(location);
        // Chấp nhận vị trí Extended Controls trong bản debug để kiểm thử.
        // Bản release vẫn loại GPS giả lập.
        if ((!laBanDebug() && location.isFromMockProvider()) || !location.hasAccuracy()) {
            nhanDien.capNhat(Double.NaN, 0, Float.NaN, now, now);
            trangThai("Vị trí chưa có độ chính xác. Đang chờ GPS mới…");
            return;
        }
        long lucDo = location.getElapsedRealtimeNanos() / 1000000;
        if (nhanDien.dangChoTraLoi()) return;
        DiaDiem den = nhanDien.capNhat(location.getLatitude(), location.getLongitude(), location.getAccuracy(), lucDo, now);
        if (den == null) {
            if (Float.isFinite(location.getAccuracy()) && location.getAccuracy() > 25) {
                trangThai(String.format(Locale.ROOT,
                        "GPS đang bật nhưng sai số ±%.0f m, cần ≤25 m để nhận diện.", location.getAccuracy()));
            } else trangThai(nhanDien.trangThai());
            return;
        }
        diemDangMoi = den;
        hienLoiMoi(BoChonDiemTuDong.khoangCach(location.getLatitude(), location.getLongitude(), den.viDo, den.kinhDo));
    }

    private void hienThiMauGps(Location location) {
        DiaDiem gan = null; double min = Double.MAX_VALUE;
        for (DiaDiem d : cacDiem) if (Double.isFinite(d.viDo) && Double.isFinite(d.kinhDo)) {
            double kc = BoChonDiemTuDong.khoangCach(location.getLatitude(), location.getLongitude(), d.viDo, d.kinhDo);
            if (kc < min) { min = kc; gan = d; }
        }
        if (gan == null) return;
        TextView oGanNhat = bang.findViewById(R.id.autoGanNhat);
        String kc = min < 1000 ? String.format(Locale.ROOT, "%.0f m", min) : String.format(Locale.ROOT, "%.2f km", min / 1000);
        oGanNhat.setText("Gần nhất: " + gan.ten + "\n" + kc + " · Đường chim bay");
        oGanNhat.setVisibility(View.VISIBLE);
    }

    private boolean laBanDebug() {
        return (activity.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void hienLoiMoi(double distance) {
        final int phienHoi = phien, id = diemDangMoi.id;
        trangThai("Bạn đang ở gần " + diemDangMoi.ten + ". Đang chờ bạn xác nhận.");
        bang.findViewById(R.id.autoMoChiTiet).setVisibility(View.VISIBLE);
        hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle("Bạn đang ở gần " + diemDangMoi.ten)
                .setMessage(String.format(Locale.ROOT, "Khoảng cách: %.0f m (phạm vi %.0f m).\n\nBạn có muốn bắt đầu nghe thuyết minh không?", distance, diemDangMoi.banKinhAuto))
                .setPositiveButton("Bắt đầu", (d, w) -> { if (phienHoi == phien && nhanDien.traLoi(id, true)) moChiTiet(true); })
                .setNegativeButton("Bỏ qua", (d, w) -> { if (phienHoi == phien) nhanDien.traLoi(id, false); })
                .create();
        hoiThoai.show();
    }

    private void moChiTiet(boolean phatSauXacNhan) {
        if (diemDangMoi == null) return;
        int id = diemDangMoi.id;
        dung("Đang mở chi tiết " + diemDangMoi.ten + ".");
        activity.startActivity(new Intent(activity, ChiTietDiaDiemActivity.class)
                .putExtra("viTriDiaDiem", id)
                .putExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI, phatSauXacNhan)
                .putExtra(ChiTietDiaDiemActivity.CHE_DO_VI_TRI, KhoLichSu.AUTO_MAIN));
    }

    private void hienThiNoiDungAuto(boolean moRong) {
        bang.findViewById(R.id.autoNoiDungMoRong).setVisibility(moRong ? View.VISIBLE : View.GONE);
    }
    private void caiDatThuGonAuto() {
        hienThiNoiDungAuto(false);
    }
    @Override public void onProviderDisabled(String provider) { if (cheDo == GPS && LocationManager.GPS_PROVIDER.equals(provider)) dung("GPS đã tắt. Bật lại vị trí rồi thử lại."); }
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
}
