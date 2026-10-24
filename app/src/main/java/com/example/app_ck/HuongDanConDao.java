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
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** AUTO chỉ trong tour Côn Đảo đang mở. Demo không ghi GPS vào dữ liệu thực. */
final class HuongDanConDao implements LocationListener {
    private static final int THU_CONG = 0, GPS = 1, DEMO = 2;
    private static final int[] CAC_BUOC = {500, 150, 60, 20};
    private final ConDaoThamQuanActivity activity;
    private final View bang;
    private final View tuyChonView;
    private final com.google.android.material.bottomsheet.BottomSheetDialog chonCheDo;
    private final Handler hen = new Handler(Looper.getMainLooper());
    private final LocationManager viTri;
    private final List<CauHinhAutoConDao.Diem> cacDiem = CauHinhAutoConDao.cacDiemAuto();
    private BoNhanDienDiemThanhPhan nhanDien;
    private int cheDo, buocDemo, viTriDemo, phien;
    private boolean choLuaChon, choDiemTiep;
    private long lucChuyenDiem;
    private long lucBuoc, lucGps;
    private boolean dangDoc, choQuyen;
    private CauHinhAutoConDao.Diem diemDemo;
    private AlertDialog hoiThoai;

    HuongDanConDao(ConDaoThamQuanActivity activity, ViewGroup cha) {
        this.activity = activity;
        bang = activity.getLayoutInflater().inflate(R.layout.view_auto_con_dao, cha, false);
        cha.addView(bang);
        viTri = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        tuyChonView = activity.getLayoutInflater().inflate(R.layout.sheet_auto_con_dao, cha, false);
        chonCheDo = new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        chonCheDo.setContentView(tuyChonView);
        chonCheDo.setOnShowListener(d -> chonCheDo.getBehavior().setState(
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED));
        tuyChonView.findViewById(R.id.conDaoGps).setOnClickListener(v -> { chonCheDo.dismiss(); batGps(); });
        tuyChonView.findViewById(R.id.conDaoDemo).setOnClickListener(v -> { chonCheDo.dismiss(); batDemo(); });
        bang.findViewById(R.id.conDaoBatAuto).setOnClickListener(v -> {
            if (cheDo != THU_CONG || choQuyen) dung("Đã dừng AUTO và âm thanh.");
            else chonCheDo.show();
        });
        bang.findViewById(R.id.conDaoBuocDemo).setOnClickListener(v -> {
            if (cheDo == DEMO && !dangDoc && !choLuaChon && !choDiemTiep && buocDemo < CAC_BUOC.length - 1) {
                buocDemo++;
                lucBuoc = SystemClock.elapsedRealtime();
            }
        });
    }

    private int soDiemDemo() { return CauHinhAutoConDao.CAC_DIEM.size(); }

    private boolean duocBat(CauHinhAutoConDao.Diem d) {
        return d.choPhepAuto;
    }

    private List<BoChonDiemTuDong.Moc> cacMoc() {
        List<BoChonDiemTuDong.Moc> ds = new ArrayList<>();
        for (CauHinhAutoConDao.Diem d : cacDiem) if (duocBat(d))
            ds.add(new BoChonDiemTuDong.Moc(d.id, -1, d.lat, d.lon, d.banKinh));
        return ds;
    }

    private void chu(int id, String value) {
        View view = bang.findViewById(id);
        if (view == null) view = tuyChonView.findViewById(id);
        ((TextView) view).setText(value);
    }
    private void trangThai(String value) {
        chu(R.id.conDaoTrangThai, value);
        bang.findViewById(R.id.conDaoTrangThai).setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
    }

    void dung(String lyDo) {
        phien++;
        choLuaChon = false;
        choDiemTiep = false;
        cheDo = THU_CONG;
        chonCheDo.dismiss();
        chu(R.id.conDaoBatAuto, "Bật AUTO");
        choQuyen = false;
        dangDoc = false;
        hen.removeCallbacksAndMessages(null);
        if (viTri != null) viTri.removeUpdates(this);
        if (nhanDien != null) nhanDien.huyChoOnDinh();
        if (hoiThoai != null) { hoiThoai.dismiss(); hoiThoai = null; }
        activity.dungDocConDao();
        chu(R.id.conDaoCheDo, "Thủ công · Chưa bật AUTO");
        bang.findViewById(R.id.conDaoBuocDemo).setVisibility(View.GONE);
        trangThai(lyDo);
    }

    private boolean batDau(int loai) {
        dung("");
        List<BoChonDiemTuDong.Moc> ds = cacMoc();
        if (loai == GPS && ds.isEmpty()) { trangThai("Chưa có điểm hỗ trợ AUTO. Bạn có thể nghe thủ công trên sơ đồ."); return false; }
        nhanDien = new BoNhanDienDiemThanhPhan(ds);
        cheDo = loai;
        chu(R.id.conDaoBatAuto, "Tắt AUTO");
        lucGps = 0;
        chu(R.id.conDaoCheDo, loai == GPS ? "GPS thật · AUTO các điểm đã chọn" : "DEMO · Vị trí mô phỏng");
        bang.findViewById(R.id.conDaoBuocDemo).setVisibility(loai == DEMO ? View.VISIBLE : View.GONE);
        return true;
    }

    private void batDemo() {
        if (!batDau(DEMO)) return;
        viTriDemo = 0;
        batDauDiemDemo();
        nhip();
    }

    private void batDauDiemDemo() {
        diemDemo = CauHinhAutoConDao.CAC_DIEM.get(viTriDemo);
        buocDemo = 0;
        lucBuoc = SystemClock.elapsedRealtime();
        choDiemTiep = false;
        choLuaChon = false;
        trangThai("Demo toàn tuyến · Điểm " + (viTriDemo + 1) + "/" + soDiemDemo() + ": " + diemDemo.ten
                + ". " + (duocBat(diemDemo) ? "Sẽ tự phát khi đến gần." : "Sẽ thông báo để bạn chọn nghe hoặc bỏ qua."));
    }

    private void tiepTucDemo() {
        if (cheDo != DEMO) return;
        choLuaChon = false;
        choDiemTiep = true;
        lucChuyenDiem = SystemClock.elapsedRealtime();
        trangThai("Đã xử lý điểm " + (viTriDemo + 1) + "/" + soDiemDemo() + ". "
                + (viTriDemo == soDiemDemo() - 1 ? "Đang kết thúc hành trình…" : "Đang di chuyển đến điểm tiếp theo…"));
    }

    private void thongBaoDiemThuCong() {
        if (cheDo != DEMO || choLuaChon) return;
        choLuaChon = true;
        activity.hienThiDiemDemo(diemDemo.id);
        int phienHoi = phien;
        trangThai("Đã đến " + diemDemo.ten + ". Điểm nghe thủ công; đang chờ bạn chọn.");
        hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Đã đến " + diemDemo.ten)
                .setMessage("DEMO · Điểm " + (viTriDemo + 1) + "/" + soDiemDemo() + "\n\n"
                        + "Điểm này nghe thủ công, không tự phát âm thanh. Bạn muốn nghe thuyết minh hay tiếp tục đến điểm kế tiếp?")
                .setPositiveButton("Nghe thuyết minh", (d, w) -> {
                    if (phienHoi == phien && cheDo == DEMO) phatTrongDemo(false);
                })
                .setNegativeButton("Bỏ qua", (d, w) -> { if (phienHoi == phien) tiepTucDemo(); })
                .setOnCancelListener(d -> { if (phienHoi == phien) tiepTucDemo(); }).create();
        hoiThoai.setCanceledOnTouchOutside(false);
        hoiThoai.show();
    }

    private void phatTrongDemo(boolean tuDong) {
        choLuaChon = false;
        if (!activity.phatDiemTuDong(diemDemo.id)) {
            thongBaoLoiDoc();
            return;
        }
        dangDoc = true;
        nhanDien.daPhat(diemDemo.id);
        trangThai((tuDong ? "Đang tự động phát: " : "Đang phát theo lựa chọn: ") + diemDemo.ten
                + ". Nghe xong sẽ tiếp tục hành trình.");
    }

    private void thongBaoLoiDoc() {
        choLuaChon = true;
        int phienHoi = phien;
        trangThai("Chưa phát được thuyết minh. Đang chờ thử lại hoặc bỏ qua điểm này.");
        hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Chưa phát được thuyết minh")
                .setMessage("Kiểm tra giọng đọc tiếng Việt. Bạn có thể thử lại hoặc bỏ qua để tiếp tục demo.")
                .setPositiveButton("Thử lại", (d, w) -> { if (phienHoi == phien && cheDo == DEMO) phatTrongDemo(false); })
                .setNegativeButton("Bỏ qua", (d, w) -> { if (phienHoi == phien) tiepTucDemo(); })
                .setOnCancelListener(d -> { if (phienHoi == phien) tiepTucDemo(); }).create();
        hoiThoai.setCanceledOnTouchOutside(false);
        hoiThoai.show();
    }

    private void batGps() {
        dung("Đang chuẩn bị GPS thật…");
        if (cacMoc().isEmpty()) { trangThai("Chưa có điểm hỗ trợ AUTO. Bạn có thể nghe thủ công trên sơ đồ."); return; }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            choQuyen = true;
            chu(R.id.conDaoBatAuto, "Hủy bật AUTO");
            activity.xinQuyenConDao();
            return;
        }
        if (viTri == null || !viTri.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trangThai("Bật dịch vụ vị trí, quay lại rồi bấm GPS thật. Demo vẫn sử dụng được.");
            hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Bật dịch vụ vị trí")
                    .setMessage("Cần GPS để tự nhận diện các khu di tích ngoài trời.")
                    .setPositiveButton("Cài đặt vị trí", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Để sau", null).show();
            return;
        }
        if (!batDau(GPS)) return;
        try {
            viTri.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            if (viTri.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                viTri.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            trangThai("Đã bật AUTO trong tour Côn Đảo. Khi ở trong vùng 35 m đủ ổn định, bài tương ứng sẽ tự phát.");
            nhip();
        } catch (SecurityException e) { dung("Quyền vị trí thay đổi. Bấm GPS thật để cấp lại."); }
    }

    void nhanQuyen(boolean coQuyen) {
        if (!choQuyen || !activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) return;
        choQuyen = false;
        if (coQuyen) batGps();
        else {
            dung("Chưa có quyền vị trí chính xác. Bạn vẫn chạy demo hoặc chọn marker để nghe được.");
            hoiThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity).setTitle("Cần quyền vị trí chính xác")
                    .setMessage("Nếu đã từ chối nhiều lần, hãy mở cài đặt ứng dụng để cấp lại quyền.")
                    .setPositiveButton("Cài đặt ứng dụng", (d, w) -> activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:" + activity.getPackageName()))))
                    .setNegativeButton("Nghe thủ công", null).show();
        }
    }

    private void nhip() {
        if (cheDo == THU_CONG) return;
        long now = SystemClock.elapsedRealtime();
        if (cheDo == DEMO) {
            if (choDiemTiep && now - lucChuyenDiem >= 2000) {
                viTriDemo++;
                if (viTriDemo == CauHinhAutoConDao.CAC_DIEM.size()) {
                    dung("Đã hoàn thành demo " + soDiemDemo() + "/" + soDiemDemo() + " điểm Côn Đảo. Bạn có thể chạy lại hoặc nghe thủ công.");
                    chu(R.id.conDaoCheDo, "DEMO · Đã hoàn thành " + soDiemDemo() + "/" + soDiemDemo() + " điểm");
                    return;
                }
                batDauDiemDemo();
            }
            if (!dangDoc && !choLuaChon && !choDiemTiep) {
                if (now - lucBuoc >= 2000 && buocDemo < CAC_BUOC.length - 1) { buocDemo++; lucBuoc = now; }
                int distance = CAC_BUOC[buocDemo];
                chu(R.id.conDaoCheDo, "DEMO · " + (viTriDemo + 1) + "/" + soDiemDemo() + " · " + diemDemo.ten + " · Còn " + distance + " m");
                if (duocBat(diemDemo)) {
                    nhanViTri(diemDemo.lat + distance / 111195.0, diemDemo.lon, 3, now, now);
                } else if (buocDemo == CAC_BUOC.length - 1) {
                    // Điểm thủ công chỉ mô phỏng bước di chuyển, không tạo GPS giả cho điểm chưa có tọa độ.
                    thongBaoDiemThuCong();
                }
            }
        } else if (lucGps == 0 || now - lucGps > 15000) {
            nhanDien.huyChoOnDinh();
            if (!dangDoc) trangThai("Đang chờ GPS mới và đủ chính xác. Hãy chọn điểm trên sơ đồ để nghe nếu vị trí yếu.");
        }
        if (cheDo != THU_CONG) hen.postDelayed(this::nhip, 1000);
    }

    @Override public void onLocationChanged(Location location) {
        if (cheDo != GPS) return;
        if (location.isFromMockProvider() || !location.hasAccuracy()) {
            nhanDien.huyChoOnDinh();
            if (!dangDoc) trangThai("Vị trí thiếu độ chính xác hoặc là GPS giả lập. Hãy dùng Demo hoặc chọn điểm thủ công.");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        long lucDo = location.getElapsedRealtimeNanos() / 1000000;
        if (lucDo <= now && now - lucDo <= 15000) lucGps = lucDo;
        CauHinhAutoConDao.Diem gan = null;
        double min = Double.MAX_VALUE;
        for (CauHinhAutoConDao.Diem d : cacDiem) if (duocBat(d)) {
            double distance = BoChonDiemTuDong.khoangCach(location.getLatitude(), location.getLongitude(), d.lat, d.lon);
            if (distance < min) { min = distance; gan = d; }
        }
        chu(R.id.conDaoCheDo, String.format(Locale.ROOT, "GPS thật · %s · %.0f m", gan == null ? "Đang tìm vị trí" : gan.ten, min));
        nhanViTri(location.getLatitude(), location.getLongitude(), location.getAccuracy(), lucDo, now);
    }

    private void nhanViTri(double lat, double lon, float accuracy, long lucDo, long now) {
        if (dangDoc) { nhanDien.huyChoOnDinh(); return; }
        Integer id = nhanDien.capNhat(lat, lon, accuracy, lucDo, now);
        if (id == null) { trangThai(nhanDien.trangThai()); return; }
        if (cheDo == DEMO) {
            if (id == diemDemo.id) phatTrongDemo(true);
            return;
        }
        if (!activity.phatDiemTuDong(id)) {
            trangThai("Giọng đọc tiếng Việt chưa sẵn sàng. Hãy kiểm tra TTS rồi nghe lại; chưa ghi nhận điểm đã phát.");
            return;
        }
        dangDoc = true;
        nhanDien.daPhat(id);
        trangThai("Đang tự động phát: " + tenDiem(id) + ". Chọn marker hoặc Dừng để chuyển sang thủ công.");
    }

    private String tenDiem(int id) {
        for (CauHinhAutoConDao.Diem d : cacDiem) if (d.id == id) return d.ten;
        return "";
    }

    void docXong(boolean thanhCong) {
        if (cheDo == THU_CONG) return;
        dangDoc = false;
        nhanDien.huyChoOnDinh();
        if (cheDo == DEMO) {
            if (thanhCong) tiepTucDemo();
            else thongBaoLoiDoc();
            return;
        }
        trangThai(thanhCong ? "Đã đọc xong. Tiếp tục đến điểm AUTO khác; điểm vừa nghe không tự phát lại trong lượt."
                : "Không phát được bài. Chọn điểm và bấm Nghe thuyết minh để thử lại thủ công.");
    }

    @Override public void onProviderDisabled(String provider) {
        if (cheDo == GPS && LocationManager.GPS_PROVIDER.equals(provider)) dung("GPS đã tắt. Bật lại hoặc dùng Demo.");
    }
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
}
