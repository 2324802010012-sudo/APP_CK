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
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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

/** Một bộ điều khiển chung, dùng cùng bộ chọn mốc cho DEMO và GPS thật. Chỉ chạy ở tiền cảnh. */
public final class ThamQuanTuDong implements LocationListener {
    static final int THU_CONG = 0, DEMO = 1, GPS = 2;
    private final BaseActivity activity;
    private final NguonThamQuanTuDong nguon;
    private final Handler hen = new Handler(Looper.getMainLooper());
    private final LocationManager viTri;
    private final List<NguonThamQuanTuDong.Diem> diem;
    private final View bang;
    private final Spinner banKinh;
    private final DiaDiem diaDiem;
    private BoChonDiemTuDong boChon = new BoChonDiemTuDong();
    private List<BoChonDiemTuDong.Moc> mocGps = new ArrayList<>();
    private Location gpsMoi;
    private int cheDo, demoIndex, phien;
    private boolean dangChonTuDong;
    private String luotDoc;
    private long lucBatDau, lanDoc;
    private static final int[] CAC_BAN_KINH = {20, 30, 40, 60, 80};

    ThamQuanTuDong(BaseActivity activity, NguonThamQuanTuDong nguon) {
        this.activity = activity;
        this.nguon = nguon;
        diem = nguon.cacDiemTuDong();
        viTri = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        DiaDiem tim = null;
        try (TaiKhoanDb db = new TaiKhoanDb(activity)) {
            for (DiaDiem d : db.danhSachDiaDiem()) if (d.id == nguon.idDiaDiemTuDong()) tim = d;
        }
        diaDiem = tim;
        View danhSach = activity.findViewById(R.id.tqDanhSachKhuVuc);
        ViewGroup cha = (ViewGroup) danhSach.getParent();
        bang = activity.getLayoutInflater().inflate(R.layout.view_tham_quan_tu_dong, cha, false);
        cha.addView(bang, cha.indexOfChild(danhSach));
        banKinh = bang.findViewById(R.id.autoBanKinh);
        banKinh.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"20 mét", "30 mét", "40 mét", "60 mét", "80 mét"}));
        banKinh.setSelection(2);
        banKinh.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (cheDo == GPS) taiMocGps();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        bang.findViewById(R.id.autoDemo).setOnClickListener(v -> batDemo());
        bang.findViewById(R.id.autoGps).setOnClickListener(v -> batGps());
        bang.findViewById(R.id.autoDung).setOnClickListener(v -> dung("Đã dừng tự động; bạn có thể chọn mốc và nghe thủ công."));
        bang.findViewById(R.id.autoDemoTiep).setOnClickListener(v -> {
            if (cheDo == DEMO) { ngatDoc(); demoIndex++; demoTiep(); }
        });
        bang.findViewById(R.id.autoGhiGps).setOnClickListener(v -> ghiGps());
        activity.findViewById(R.id.tqNutPhat).setOnClickListener(v -> {
            dung("Đang nghe thủ công tại mốc đã chọn.");
            docNoiDung(noiDungDangChon(), false);
        });
        activity.findViewById(R.id.tqNutDung).setOnClickListener(v -> dung("Đã dừng phát và AUTO."));
        TextToSpeech mayDoc = nguon.mayDocTuDong();
        if (mayDoc != null) mayDoc.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) {
                hen.post(() -> {
                    if (!id.equals(luotDoc)) return;
                    luotDoc = null;
                    if (cheDo == DEMO) {
                        final int phienDoc = phien;
                        hen.postDelayed(() -> {
                            if (cheDo == DEMO && phienDoc == phien && luotDoc == null) {
                                demoIndex++;
                                demoTiep();
                            }
                        }, 1800);
                    }
                });
            }
            @Override public void onError(String id) {
                hen.post(() -> {
                    if (id.equals(luotDoc)) dung("Không phát được giọng đọc. Kiểm tra TTS tiếng Việt rồi thử lại.");
                });
            }
        });
    }

    private void chu(int id, String value) { ((TextView) bang.findViewById(id)).setText(value); }
    private void trangThai(String value) { chu(R.id.autoTrangThai, value); }
    private int tangDangChon() {
        for (NguonThamQuanTuDong.Diem d : diem) if (d.id == nguon.idDiemDangChon()) return d.tang;
        return -1;
    }
    private String noiDungDangChon() {
        return ((TextView) activity.findViewById(R.id.tqTenKhuVuc)).getText() + ". "
                + ((TextView) activity.findViewById(R.id.tqMoTaKhuVuc)).getText();
    }
    private void ngatDoc() {
        luotDoc = null;
        if (nguon.mayDocTuDong() != null) nguon.mayDocTuDong().stop();
    }
    public void dung(String lyDo) {
        phien++;
        cheDo = THU_CONG;
        hen.removeCallbacksAndMessages(null);
        if (viTri != null) viTri.removeUpdates(this);
        ngatDoc();
        chu(R.id.autoCheDo, "Thủ công");
        bang.findViewById(R.id.autoDemoTiep).setVisibility(View.GONE);
        trangThai(lyDo);
    }
    void chonThuCong() {
        if (dangChonTuDong) return;
        if (cheDo == DEMO) dung("Đã chuyển sang thủ công khi bạn chọn mốc trên sơ đồ.");
        else if (cheDo == GPS) ngatDoc();
    }
    private void batDau(int loai) {
        dung("");
        cheDo = loai;
        boChon = new BoChonDiemTuDong();
        lucBatDau = SystemClock.elapsedRealtime();
        gpsMoi = null;
        chu(R.id.autoCheDo, loai == DEMO ? "DEMO · Vị trí mô phỏng" : "GPS thật · AUTO");
        bang.findViewById(R.id.autoDemoTiep).setVisibility(loai == DEMO ? View.VISIBLE : View.GONE);
    }
    void batDemo() {
        if (diem.isEmpty()) { trangThai("Chưa có điểm tham quan để demo."); return; }
        batDau(DEMO);
        demoIndex = 0;
        demoTiep();
        nhip(phien);
    }
    private void demoTiep() {
        if (demoIndex >= diem.size()) { dung("Đã hoàn thành tuyến demo. Bấm Chạy demo để bắt đầu lại."); return; }
        NguonThamQuanTuDong.Diem d = diem.get(demoIndex);
        dangChonTuDong = true;
        try { nguon.chonDiemTuDong(d.id); } finally { dangChonTuDong = false; }
        chu(R.id.autoViTri, "Mô phỏng đến mốc " + d.id + ": " + d.ten);
        trangThai("Demo " + (demoIndex + 1) + "/" + diem.size() + " · Đang di chuyển đến mốc…");
    }
    private void nhip(int phienNhip) {
        if (cheDo == THU_CONG || phienNhip != phien) return;
        long now = SystemClock.elapsedRealtime();
        if (luotDoc != null && now - lanDoc > 180000) {
            dung("Giọng đọc không phản hồi; hãy kiểm tra TTS và chạy lại.");
            return;
        }
        if (!nguon.sanSangTuDong() && now - lucBatDau > 30000) {
            dung("Chưa có giọng đọc tiếng Việt. Kiểm tra cài đặt TTS của thiết bị rồi bật AUTO lại.");
            return;
        }
        if (cheDo == DEMO && demoIndex < diem.size() && luotDoc == null) {
            NguonThamQuanTuDong.Diem d = diem.get(demoIndex);
            // Lưới tọa độ chỉ dùng trong RAM cho demo, không ghi vào mốc GPS thật.
            List<BoChonDiemTuDong.Moc> giaLap = java.util.Collections.singletonList(
                    new BoChonDiemTuDong.Moc(d.id, d.tang, 0, demoIndex * .002, 40));
            Integer den = boChon.capNhat(giaLap, d.tang, 0, demoIndex * .002, 3, now, now);
            if (den != null) phatMoc(den);
        }
        if (cheDo == GPS && (gpsMoi == null || now - gpsMoi.getElapsedRealtimeNanos() / 1000000 > 15000)) {
            trangThai(now - lucBatDau > 30000 ? "Chưa có GPS đủ mới. Ra nơi thoáng, kiểm tra quyền vị trí và GPS."
                    : "Đang chờ vị trí GPS mới…");
        }
        hen.postDelayed(() -> nhip(phienNhip), 1500);
    }
    void batGps() {
        dung("Đang chuẩn bị GPS thật…");
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            activity.xinQuyenGpsThamQuan();
            return;
        }
        if (viTri == null || !viTri.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            trangThai("Hãy bật dịch vụ vị trí để sử dụng GPS thật.");
            new AlertDialog.Builder(activity).setTitle("Bật GPS")
                    .setMessage("Bật vị trí, quay lại và bấm GPS thật.")
                    .setPositiveButton("Cài đặt vị trí", (d, w) -> activity.startActivity(
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Để sau", null).show();
            return;
        }
        batDau(GPS);
        taiMocGps();
        try {
            viTri.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            if (viTri.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                viTri.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this, Looper.getMainLooper());
            trangThai("GPS thật đang bật. Chọn đúng tầng trên sơ đồ nếu tham quan trong nhà.");
            nhip(phien);
        } catch (SecurityException e) { dung("Quyền vị trí đã thay đổi. Vui lòng cấp lại quyền GPS chính xác."); }
    }
    void nhanQuyen(boolean coQuyen) {
        if (coQuyen) batGps();
        else {
            trangThai("Chưa được cấp vị trí chính xác. Demo vẫn dùng được.");
            new AlertDialog.Builder(activity).setTitle("Cần quyền vị trí chính xác")
                    .setMessage("Bật quyền Vị trí chính xác trong cài đặt ứng dụng để chạy GPS thật.")
                    .setPositiveButton("Cài đặt ứng dụng", (d, w) -> activity.startActivity(new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.parse("package:" + activity.getPackageName()))))
                    .setNegativeButton("Dùng demo", (d, w) -> batDemo()).show();
        }
    }
    private void taiMocGps() {
        try (TaiKhoanDb db = new TaiKhoanDb(activity)) {
            mocGps = new ArrayList<>();
            for (BoChonDiemTuDong.Moc m : KhoToaDoThamQuan.doc(db.getReadableDatabase(), nguon.idDiaDiemTuDong())) {
                mocGps.add(new BoChonDiemTuDong.Moc(m.id, m.tang, m.lat, m.lon,
                        CAC_BAN_KINH[banKinh.getSelectedItemPosition()]));
            }
        }
        if (diaDiem != null) mocGps.add(new BoChonDiemTuDong.Moc(0, -1, diaDiem.viDo, diaDiem.kinhDo, 250));
    }
    @Override public void onLocationChanged(Location location) {
        if (cheDo != GPS) return;
        if (location.isFromMockProvider()) { gpsMoi = null; trangThai("Vị trí giả lập không dùng cho GPS thật. Hãy chọn Chạy demo."); return; }
        gpsMoi = new Location(location);
        double distance = diaDiem == null ? 0 : BoChonDiemTuDong.khoangCach(location.getLatitude(), location.getLongitude(), diaDiem.viDo, diaDiem.kinhDo);
        chu(R.id.autoViTri, String.format(Locale.ROOT, "GPS: %.6f, %.6f · Sai số ±%.0f m · Cách địa điểm %.0f m",
                location.getLatitude(), location.getLongitude(), location.getAccuracy(), distance));
        if (!location.hasAccuracy()) { trangThai("Chưa xác định được độ chính xác GPS."); return; }
        if (luotDoc != null) return;
        Integer den = boChon.capNhat(mocGps, tangDangChon(), location.getLatitude(), location.getLongitude(),
                location.getAccuracy(), location.getElapsedRealtimeNanos() / 1000000, SystemClock.elapsedRealtime());
        if (den != null) phatMoc(den);
        else trangThai(boChon.trangThai + " · " + Math.max(0, mocGps.size() - 1) + " mốc đã ghi GPS");
    }
    private boolean docNoiDung(String noiDung, boolean tuDong) {
        if (!nguon.sanSangTuDong() || nguon.mayDocTuDong() == null) {
            trangThai("Giọng đọc tiếng Việt chưa sẵn sàng. Kiểm tra cài đặt TTS của thiết bị.");
            return false;
        }
        if (noiDung == null || noiDung.trim().isEmpty()) { trangThai("Mốc chưa có nội dung thuyết minh."); return false; }
        String id = "auto_" + phien + "_" + SystemClock.elapsedRealtimeNanos();
        luotDoc = tuDong ? id : null;
        lanDoc = SystemClock.elapsedRealtime();
        int result = nguon.mayDocTuDong().speak(noiDung, TextToSpeech.QUEUE_FLUSH, null, id);
        if (result == TextToSpeech.ERROR) { luotDoc = null; trangThai("Không khởi động được giọng đọc."); return false; }
        return true;
    }
    private void phatMoc(int id) {
        if (!nguon.sanSangTuDong()) { trangThai("Đang chờ giọng đọc tiếng Việt…"); return; }
        if (nguon.mayDocTuDong().isSpeaking()) return;
        String noiDung;
        if (id == 0) noiDung = diaDiem == null ? "" : diaDiem.ten + ". " + diaDiem.moTa;
        else {
            dangChonTuDong = true;
            try { nguon.chonDiemTuDong(id); } finally { dangChonTuDong = false; }
            noiDung = noiDungDangChon();
        }
        if (docNoiDung(noiDung, true)) {
            boChon.danhDauDaPhat(id);
            trangThai(id == 0 ? "Đã đến địa điểm · Đang phát giới thiệu chung"
                    : "Đã đến mốc " + id + " · Đang phát thuyết minh tự động");
        }
    }
    private void ghiGps() {
        long now = SystemClock.elapsedRealtime();
        if (cheDo != GPS || gpsMoi == null || !gpsMoi.hasAccuracy() || gpsMoi.getAccuracy() > 25
                || now - gpsMoi.getElapsedRealtimeNanos() / 1000000 > 15000) {
            trangThai("Bật GPS thật và đợi vị trí mới có sai số ≤25 m trước khi ghi mốc.");
            return;
        }
        if (diaDiem == null || BoChonDiemTuDong.khoangCach(gpsMoi.getLatitude(), gpsMoi.getLongitude(),
                diaDiem.viDo, diaDiem.kinhDo) > (nguon.idDiaDiemTuDong() == 3 ? 8000 : 1500)) {
            trangThai("Bạn đang ở quá xa địa điểm; không thể ghi GPS cho mốc này.");
            return;
        }
        final int id = nguon.idDiemDangChon(), tang = tangDangChon();
        final Location lucGhi = new Location(gpsMoi);
        final int radius = CAC_BAN_KINH[banKinh.getSelectedItemPosition()];
        new AlertDialog.Builder(activity).setTitle("Ghi GPS mốc " + id + "?")
                .setMessage("Chỉ xác nhận khi bạn đang đứng đúng vị trí mốc đang chọn và đúng tầng trên sơ đồ. "
                        + "GPS không tự phân biệt tầng. Bán kính phát: " + radius + " m.")
                .setPositiveButton("Lưu GPS", (d, w) -> {
                    if (cheDo != GPS || SystemClock.elapsedRealtime() - lucGhi.getElapsedRealtimeNanos() / 1000000 > 15000) {
                        trangThai("Vị trí đã cũ. Hãy lấy lại GPS rồi ghi mốc."); return;
                    }
                    try (TaiKhoanDb db = new TaiKhoanDb(activity)) {
                        KhoToaDoThamQuan.luu(db.getWritableDatabase(), nguon.idDiaDiemTuDong(), id, tang,
                                lucGhi.getLatitude(), lucGhi.getLongitude(), lucGhi.getAccuracy(), radius);
                        taiMocGps();
                        trangThai("Đã ghi GPS mốc " + id + ". AUTO sẽ phát khi vào vùng mốc đủ ổn định.");
                    } catch (Exception e) { trangThai("Không lưu được GPS. Vui lòng thử lại."); }
                }).setNegativeButton("Hủy", null).show();
    }
    @Override public void onProviderDisabled(String provider) {
        if (cheDo == GPS && LocationManager.GPS_PROVIDER.equals(provider)) dung("GPS đã tắt. Bật lại vị trí và bấm GPS thật.");
    }
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    int cheDoHienTai() { return cheDo; }
}
