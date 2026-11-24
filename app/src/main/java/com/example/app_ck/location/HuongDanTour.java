package com.example.app_ck.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
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
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import com.example.app_ck.R;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.ui.common.BaseActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** AUTO trong một tour đang mở, chỉ dùng vị trí GPS thật. */
public class HuongDanTour implements LocationListener {
    private static final int THU_CONG = 0, GPS = 1;

    public interface ManHinh {
        void xinQuyenTour();
        void dungDocTour();
        void hienThiDiemTuDong(int id);
        boolean phatDiemTuDong(int id);
    }

    private final BaseActivity activity;
    private final ManHinh manHinh;
    private final CauHinhAutoDiSan cauHinh;
    private final View bang;
    private final LocationManager viTri;
    private final List<CauHinhAutoDiSan.Diem> cacDiem = new ArrayList<>();
    private final Handler hen = new Handler(Looper.getMainLooper());
    private BoNhanDienDiemThanhPhan nhanDien;
    private int cheDo, phien;
    private boolean dangDoc, choQuyen;
    private long lucGps;
    private androidx.appcompat.app.AlertDialog hoiThoai;

    public HuongDanTour(BaseActivity activity, ViewGroup cha, CauHinhAutoDiSan cauHinh) {
        this.activity = activity;
        this.manHinh = (ManHinh) activity;
        this.cauHinh = cauHinh;
        for (CauHinhAutoDiSan.Diem d : cauHinh.cacDiem) if (d.choPhepAuto) cacDiem.add(d);
        bang = activity.getLayoutInflater().inflate(R.layout.view_auto_tour, cha, false);
        cha.addView(bang);
        viTri = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        ((com.google.android.material.materialswitch.MaterialSwitch) bang.findViewById(R.id.tourBatAuto))
                .setOnClickListener(v -> {
            if (((com.google.android.material.materialswitch.MaterialSwitch) v).isChecked()) batGps();
            else dung("Đã dừng hướng dẫn GPS và âm thanh.");
        });
    }

    public boolean dangPhatTuDong() { return cheDo == GPS && dangDoc; }

    /** Giữ tương thích với tour Côn Đảo; cờ mô phỏng cũ không còn được dùng. */
    public HuongDanTour(BaseActivity activity, ViewGroup cha, CauHinhAutoDiSan cauHinh,
                         boolean boQuaCauHinhMoPhong) {
        this(activity, cha, cauHinh);
    }

    public String cheDoLichSu() { return cheDo == GPS ? KhoLichSu.AUTO_SUB : KhoLichSu.THU_CONG; }

    private void chu(int id, String value) { ((TextView) bang.findViewById(id)).setText(value); }
    private void trangThai(String value) {
        chu(R.id.tourTrangThai, value);
        bang.findViewById(R.id.tourTrangThai).setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
    }
    private void datCongTacGps(boolean bat) {
        ((com.google.android.material.materialswitch.MaterialSwitch) bang.findViewById(R.id.tourBatAuto))
                .setChecked(bat);
    }

    public void dung(String lyDo) {
        phien++; cheDo = THU_CONG; choQuyen = false; dangDoc = false; lucGps = 0;
        hen.removeCallbacksAndMessages(null);
        if (viTri != null) viTri.removeUpdates(this);
        if (nhanDien != null) nhanDien.huyChoOnDinh();
        if (hoiThoai != null) { hoiThoai.dismiss(); hoiThoai = null; }
        manHinh.dungDocTour();
        datCongTacGps(false);
        chu(R.id.tourCheDo, "GPS chưa bật");
        trangThai(lyDo);
    }

    private List<BoChonDiemTuDong.Moc> cacMoc() { return cauHinh.cacMocTuDong(); }

    private void batGps() {
        dung("Đang chuẩn bị GPS…");
        if (cacMoc().isEmpty()) { trangThai("Chưa có điểm hỗ trợ AUTO. Bạn có thể nghe thủ công trên sơ đồ."); return; }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            choQuyen = true; datCongTacGps(true); manHinh.xinQuyenTour(); return;
        }
        if (viTri == null || !viTri.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trangThai("Bật dịch vụ vị trí, quay lại rồi bấm Bật hướng dẫn GPS.");
            hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                    .setTitle("Bật dịch vụ vị trí")
                    .setMessage("Cần GPS để tự nhận diện các điểm ngoài trời.")
                    .setPositiveButton("Cài đặt vị trí", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Để sau", null).show();
            return;
        }
        cheDo = GPS; nhanDien = new BoNhanDienDiemThanhPhan(cacMoc()); lucGps = 0;
        datCongTacGps(true);
        chu(R.id.tourCheDo, "GPS đang bật · AUTO các điểm đã chọn");
        try {
            viTri.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            if (viTri.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                viTri.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            trangThai("Đã bật AUTO trong tour " + cauHinh.ten + ". GPS đủ chính xác và ổn định mới phát.");
            nhip(phien);
        } catch (SecurityException e) { dung("Quyền vị trí đã thay đổi. Bấm Bật GPS để cấp lại."); }
    }

    public void nhanQuyen(boolean coQuyen) {
        if (!choQuyen || !activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) return;
        choQuyen = false;
        if (coQuyen) { batGps(); return; }
        dung("Chưa có quyền vị trí chính xác. Hãy chọn điểm trên sơ đồ để nghe thủ công.");
        hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle("Cần quyền vị trí chính xác")
                .setMessage("Nếu đã từ chối nhiều lần, hãy mở cài đặt ứng dụng để cấp lại quyền.")
                .setPositiveButton("Cài đặt ứng dụng", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:" + activity.getPackageName()))))
                .setNegativeButton("Nghe thủ công", null).show();
    }

    private void nhip(int phienNhip) {
        if (phienNhip != phien || cheDo != GPS) return;
        long now = SystemClock.elapsedRealtime();
        if (lucGps == 0 || now - lucGps > 15000) {
            nhanDien.huyChoOnDinh();
            if (!dangDoc) trangThai("Đang chờ GPS mới và đủ chính xác. Hãy chọn điểm trên sơ đồ nếu vị trí yếu.");
        }
        hen.postDelayed(() -> nhip(phienNhip), 1000);
    }

    @Override public void onLocationChanged(Location location) {
        if (cheDo != GPS) return;
        // Emulator gửi vị trí từ Extended Controls dưới dạng mock. Chỉ bản debug
        // được dùng mẫu đó để kiểm thử; APK release vẫn đòi GPS thật.
        if ((!laBanDebug() && location.isFromMockProvider()) || !location.hasAccuracy()) {
            nhanDien.huyChoOnDinh();
            if (!dangDoc) trangThai("Vị trí thiếu độ chính xác. Hãy chọn điểm thủ công.");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long lucDo = location.getElapsedRealtimeNanos() / 1000000;
        if (lucDo <= now && now - lucDo <= 15000) lucGps = lucDo;
        CauHinhAutoDiSan.Diem gan = null; double min = Double.MAX_VALUE;
        for (CauHinhAutoDiSan.Diem d : cacDiem) {
            double kc = BoChonDiemTuDong.khoangCach(location.getLatitude(), location.getLongitude(), d.lat, d.lon);
            if (kc < min) { min = kc; gan = d; }
        }
        chu(R.id.tourCheDo, String.format(Locale.ROOT, "GPS đang bật · %s · %.0f m",
                gan == null ? "Đang tìm vị trí" : gan.ten, min));
        if (dangDoc) { nhanDien.huyChoOnDinh(); return; }
        Integer id = nhanDien.capNhat(location.getLatitude(), location.getLongitude(), location.getAccuracy(), lucDo, now);
        if (id == null) { trangThai(nhanDien.trangThai()); return; }
        manHinh.hienThiDiemTuDong(id);
        if (!manHinh.phatDiemTuDong(id)) {
            trangThai("Giọng đọc tiếng Việt chưa sẵn sàng. Hãy kiểm tra TTS rồi nghe lại thủ công.");
            return;
        }
        dangDoc = true; nhanDien.daPhat(id);
        trangThai("Đang tự động phát: " + tenDiem(id) + ". Chọn marker hoặc Dừng để chuyển sang thủ công.");
    }

    private String tenDiem(int id) {
        for (CauHinhAutoDiSan.Diem d : cacDiem) if (d.id == id) return d.ten;
        return "";
    }

    private boolean laBanDebug() {
        return (activity.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public void docXong(boolean thanhCong) {
        if (cheDo != GPS) return;
        dangDoc = false; nhanDien.huyChoOnDinh();
        trangThai(thanhCong ? "Đã đọc xong. Tiếp tục đến điểm AUTO khác; điểm vừa nghe không tự phát lại trong lượt."
                : "Không phát được bài. Chọn điểm và bấm Nghe thuyết minh để thử lại thủ công.");
    }
    @Override public void onProviderDisabled(String provider) { if (cheDo == GPS && LocationManager.GPS_PROVIDER.equals(provider)) dung("GPS đã tắt. Bật lại vị trí rồi thử lại."); }
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
}
