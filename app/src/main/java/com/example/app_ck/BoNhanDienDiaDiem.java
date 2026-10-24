package com.example.app_ck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Nhận diện điểm chính gần nhất, không phụ thuộc sơ đồ/marker bên trong. */
public final class BoNhanDienDiaDiem {
    private final List<DiaDiem> cacDiem;
    private final Map<Integer, LoiMoiThuyetMinh> loiMoi = new HashMap<>();
    private DiaDiem ganNhat, dangMoi;
    private boolean daDung;
    private long lanCuoi = -1;
    private String trangThai = "Đang chờ vị trí";

    public BoNhanDienDiaDiem(List<DiaDiem> ds) {
        cacDiem = new ArrayList<>();
        for (DiaDiem d : ds) {
            if (!Double.isFinite(d.viDo) || !Double.isFinite(d.kinhDo)
                    || Math.abs(d.viDo) > 90 || Math.abs(d.kinhDo) > 180) continue;
            cacDiem.add(d);
            loiMoi.put(d.id, new LoiMoiThuyetMinh(d.viDo, d.kinhDo));
        }
    }

    public DiaDiem capNhat(double lat, double lon, float accuracy, long lucDo, long now) {
        if (daDung || dangMoi != null) return null;
        if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180
                || !Float.isFinite(accuracy) || accuracy < 0 || accuracy > 25
                || lucDo > now || now - lucDo > 15000 || lucDo <= lanCuoi) {
            huyOnDinh(now);
            trangThai = "Vị trí cũ hoặc chưa đủ chính xác (cần ≤25 m)";
            return null;
        }
        lanCuoi = lucDo;
        DiaDiem gan = null;
        double min = Double.MAX_VALUE;
        for (DiaDiem d : cacDiem) {
            double distance = BoChonDiemTuDong.khoangCach(lat, lon, d.viDo, d.kinhDo);
            if (distance < min) { min = distance; gan = d; }
        }
        if (gan == null) { trangThai = "Chưa có địa điểm để nhận diện"; return null; }
        if (ganNhat != null && gan.id != ganNhat.id) huyOnDinh(now);
        ganNhat = gan;
        LoiMoiThuyetMinh m = loiMoi.get(gan.id);
        if (m.daMoi()) {
            trangThai = "Đã xử lý lời mời cho " + gan.ten + " trong lượt này";
            return null;
        }
        if (m.capNhat(lat, lon, accuracy, lucDo, now)) {
            dangMoi = gan;
            trangThai = "Đang chờ xác nhận";
            return gan;
        }
        trangThai = m.trangThai();
        return null;
    }

    private void huyOnDinh(long now) {
        if (ganNhat != null) loiMoi.get(ganNhat.id).capNhat(Double.NaN, 0, Float.NaN, now, now);
    }

    public boolean traLoi(int id, boolean dongY) {
        if (daDung || dangMoi == null || dangMoi.id != id) return false;
        boolean duocPhat = loiMoi.get(id).traLoi(dongY);
        dangMoi = null;
        return duocPhat;
    }

    public void huy() {
        daDung = true;
        dangMoi = null;
        for (LoiMoiThuyetMinh m : loiMoi.values()) m.huy();
    }
    public DiaDiem ganNhat() { return ganNhat; }
    public boolean dangChoTraLoi() { return dangMoi != null; }
    public String trangThai() { return trangThai; }
}
