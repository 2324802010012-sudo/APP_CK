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

/** AUTO chọn lọc trong một tour đang mở. Demo không ghi GPS vào dữ liệu thực. */
final class HuongDanTour implements LocationListener {
    boolean dangPhatTuDong() { return cheDo != THU_CONG && dangDoc; }

    String cheDoLichSu() {
        return cheDo == DEMO ? KhoLichSu.DEMO : cheDo == GPS ? KhoLichSu.AUTO_SUB : KhoLichSu.THU_CONG;
    }
    private static final int THU_CONG = 0, GPS = 1, DEMO = 2;
    private static final int[] CAC_BUOC = {500, 150, 60, 20};
    interface ManHinh {
        void xinQuyenTour();
        void dungDocTour();
        void hienThiDiemDemo(int id);
        boolean phatDiemTuDong(int id);
    }
    private final BaseActivity activity;
    private final ManHinh manHinh;
    private final CauHinhAutoDiSan cauHinh;
    private final View bang;
    private final View tuyChonView;
    private final com.google.android.material.bottomsheet.BottomSheetDialog chonCheDo;
    private final Handler hen = new Handler(Looper.getMainLooper());
    private final LocationManager viTri;
    private final List<CauHinhAutoDiSan.Diem> cacDiem = new ArrayList<>();
    private BoNhanDienDiemThanhPhan nhanDien;
    private int cheDo, buocDemo, viTriDemo, phien;
    private boolean choLuaChon, choDiemTiep;
    private long lucChuyenDiem;
    private long lucBuoc, lucGps;
    private boolean dangDoc, choQuyen;
    private CauHinhAutoDiSan.Diem diemDemo;
    private AlertDialog hoiThoai;

    HuongDanTour(BaseActivity activity, ViewGroup cha, CauHinhAutoDiSan cauHinh) {
        this.activity = activity;
        this.manHinh = (ManHinh) activity;
        this.cauHinh = cauHinh;
        for (CauHinhAutoDiSan.Diem d : cauHinh.cacDiem) if (d.choPhepAuto) cacDiem.add(d);
        bang = activity.getLayoutInflater().inflate(R.layout.view_auto_tour, cha, false);
        cha.addView(bang);
        ((TextView) bang.findViewById(R.id.tourPhamVi)).setText(
                "AUTO có chọn lọc · " + cacDiem.size() + "/" + cauHinh.cacDiem.size()
                + " điểm trong " + cauHinh.ten + ". Bạn luôn có thể chọn bất kỳ điểm nào trên sơ đồ để nghe thủ công.");
        viTri = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        tuyChonView = activity.getLayoutInflater().inflate(R.layout.sheet_auto_tour, cha, false);
        chonCheDo = new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
        chonCheDo.setContentView(tuyChonView);
        ((TextView) tuyChonView.findViewById(R.id.tourMoTaDemo)).setText(
                "Tự đi qua đủ " + cauHinh.cacDiem.size() + " điểm " + cauHinh.ten
                + ": AUTO tự phát, điểm thường hiện thông báo chọn nghe hoặc bỏ qua. Không cần quyền vị trí.");
        ((TextView) tuyChonView.findViewById(R.id.tourMoTaGps)).setText(
                "Chỉ tự phát tại " + cacDiem.size() + " điểm được cấu hình trong " + cauHinh.ten
                + ": " + cauHinh.tenCacDiemTuDong() + ".\n\n"
                + "Vị trí phải đủ chính xác và ổn định. Nếu không phân biệt được các điểm, hãy chọn điểm trên sơ đồ để nghe. "
                + "Các điểm còn lại nghe thủ công. Tọa độ tham chiếu cần kiểm chứng tại di tích.");
        chonCheDo.setOnShowListener(d -> chonCheDo.getBehavior().setState(
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED));
        tuyChonView.findViewById(R.id.tourGps).setOnClickListener(v -> { chonCheDo.dismiss(); batGps(); });
        tuyChonView.findViewById(R.id.tourDemo).setOnClickListener(v -> { chonCheDo.dismiss(); batDemo(); });
        bang.findViewById(R.id.tourBatAuto).setOnClickListener(v -> {
            if (cheDo != THU_CONG || choQuyen) dung("Đã dừng AUTO và âm thanh.");
            else chonCheDo.show();
        });
        bang.findViewById(R.id.tourBuocDemo).setOnClickListener(v -> {
            if (cheDo == DEMO && !dangDoc && !choLuaChon && !choDiemTiep && buocDemo < CAC_BUOC.length - 1) {
                buocDemo++;
                lucBuoc = SystemClock.elapsedRealtime();
            }
        });
    }

    private int soDiemDemo() { return cauHinh.cacDiem.size(); }

    private boolean duocBat(CauHinhAutoDiSan.Diem d) {
        return d.choPhepAuto;
    }

    private List<BoChonDiemTuDong.Moc> cacMoc() {
        return cauHinh.cacMocTuDong();
    }

    private void chu(int id, String value) {
        View view = bang.findViewById(id);
        if (view == null) view = tuyChonView.findViewById(id);
        ((TextView) view).setText(value);
    }
    private void trangThai(String value) {
        chu(R.id.tourTrangThai, value);
        bang.findViewById(R.id.tourTrangThai).setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
    }

    void dung(String lyDo) {
        phien++;
        choLuaChon = false;
        choDiemTiep = false;
        cheDo = THU_CONG;
        chonCheDo.dismiss();
        chu(R.id.tourBatAuto, "Bật AUTO");
        choQuyen = false;
        dangDoc = false;
        hen.removeCallbacksAndMessages(null);
        if (viTri != null) viTri.removeUpdates(this);
        if (nhanDien != null) nhanDien.huyChoOnDinh();
        if (hoiThoai != null) { hoiThoai.dismiss(); hoiThoai = null; }
        manHinh.dungDocTour();
        chu(R.id.tourCheDo, "Thủ công · Chưa bật AUTO");
        bang.findViewById(R.id.tourBuocDemo).setVisibility(View.GONE);
        trangThai(lyDo);
    }

    private boolean batDau(int loai) {
        dung("");
        List<BoChonDiemTuDong.Moc> ds = cacMoc();
        if (loai == GPS && ds.isEmpty()) { trangThai("Chưa có điểm hỗ trợ AUTO. Bạn có thể nghe thủ công trên sơ đồ."); return false; }
        nhanDien = new BoNhanDienDiemThanhPhan(ds);
        cheDo = loai;
        chu(R.id.tourBatAuto, "Tắt AUTO");
        lucGps = 0;
        chu(R.id.tourCheDo, loai == GPS ? "GPS thật · AUTO các điểm đã chọn" : "DEMO · Vị trí mô phỏng");
        bang.findViewById(R.id.tourBuocDemo).setVisibility(loai == DEMO ? View.VISIBLE : View.GONE);
        return true;
    }

    private void batDemo() {
        if (!batDau(DEMO)) return;
        viTriDemo = 0;
        batDauDiemDemo();
        nhip();
    }

    private void batDauDiemDemo() {
        diemDemo = cauHinh.cacDiem.get(viTriDemo);
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
        manHinh.hienThiDiemDemo(diemDemo.id);
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
        if (!manHinh.phatDiemTuDong(diemDemo.id)) {
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
            chu(R.id.tourBatAuto, "Hủy bật AUTO");
            manHinh.xinQuyenTour();
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
            trangThai("Đã bật AUTO trong tour " + cauHinh.ten + ". GPS đủ chính xác và ổn định mới phát; điểm sát nhau vẫn chọn thủ công trên sơ đồ.");
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
                if (viTriDemo == cauHinh.cacDiem.size()) {
                    dung("Đã hoàn thành demo " + soDiemDemo() + "/" + soDiemDemo() + " điểm " + cauHinh.ten + ". Bạn có thể chạy lại hoặc nghe thủ công.");
                    chu(R.id.tourCheDo, "DEMO · Đã hoàn thành " + soDiemDemo() + "/" + soDiemDemo() + " điểm");
                    return;
                }
                batDauDiemDemo();
            }
            if (!dangDoc && !choLuaChon && !choDiemTiep) {
                if (now - lucBuoc >= 2000 && buocDemo < CAC_BUOC.length - 1) { buocDemo++; lucBuoc = now; }
                int distance = buocDemo == CAC_BUOC.length - 1
                        ? Math.min(CAC_BUOC[buocDemo], (int) diemDemo.banKinh / 3) : CAC_BUOC[buocDemo];
                chu(R.id.tourCheDo, "DEMO · " + (viTriDemo + 1) + "/" + soDiemDemo() + " · " + diemDemo.ten + " · Còn " + distance + " m");
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
        CauHinhAutoDiSan.Diem gan = null;
        double min = Double.MAX_VALUE;
        for (CauHinhAutoDiSan.Diem d : cacDiem) if (duocBat(d)) {
            double distance = BoChonDiemTuDong.khoangCach(location.getLatitude(), location.getLongitude(), d.lat, d.lon);
            if (distance < min) { min = distance; gan = d; }
        }
        chu(R.id.tourCheDo, String.format(Locale.ROOT, "GPS thật · %s · %.0f m", gan == null ? "Đang tìm vị trí" : gan.ten, min));
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
        if (!manHinh.phatDiemTuDong(id)) {
            trangThai("Giọng đọc tiếng Việt chưa sẵn sàng. Hãy kiểm tra TTS rồi nghe lại; chưa ghi nhận điểm đã phát.");
            return;
        }
        dangDoc = true;
        nhanDien.daPhat(id);
        trangThai("Đang tự động phát: " + tenDiem(id) + ". Chọn marker hoặc Dừng để chuyển sang thủ công.");
    }

    private String tenDiem(int id) {
        for (CauHinhAutoDiSan.Diem d : cacDiem) if (d.id == id) return d.ten;
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
