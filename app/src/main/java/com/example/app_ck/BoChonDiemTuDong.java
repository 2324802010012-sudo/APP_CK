package com.example.app_ck;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Lọc GPS, chọn mốc gần nhất, chống phát lại và chờ ổn định trước khi phát. */
public final class BoChonDiemTuDong {
    public static final class Moc {
        public final int id, tang;
        public final double lat, lon;
        public final float banKinh;
        public Moc(int id, int tang, double lat, double lon, float banKinh) {
            this.id = id; this.tang = tang; this.lat = lat; this.lon = lon; this.banKinh = banKinh;
        }
    }
    private final Set<Integer> daPhat = new HashSet<>();
    private int ungVien = -1;
    private long batDau, lanCuoi = -1;
    public String trangThai = "Đang chờ vị trí";

    public static double khoangCach(double lat1, double lon1, double lat2, double lon2) {
        double a = Math.pow(Math.sin(Math.toRadians(lat2 - lat1) / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(Math.toRadians(lon2 - lon1) / 2), 2);
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(Math.max(0, 1 - a)));
    }

    public Integer capNhat(List<Moc> moc, int tang, double lat, double lon,
                           float accuracy, long lucDo, long bayGio) {
        if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180
                || !Float.isFinite(accuracy) || accuracy < 0 || accuracy > 25
                || lucDo > bayGio || bayGio - lucDo > 15000 || lucDo <= lanCuoi) {
            ungVien = -1;
            trangThai = "Vị trí cũ hoặc chưa đủ chính xác (cần ≤25 m)";
            return null;
        }
        // Mất cập nhật quá lâu thì phải chờ ổn định lại từ đầu.
        if (lanCuoi >= 0 && lucDo - lanCuoi > 15000) ungVien = -1;
        lanCuoi = lucDo;
        Moc gan = null;
        double min = Double.MAX_VALUE, thuHai = Double.MAX_VALUE;
        // Mốc phòng đã ghi GPS ưu tiên hơn giới thiệu chung (ID 0).
        for (Moc m : moc) {
            if (m.id == 0 || (m.tang != -1 && m.tang != tang)) continue;
            double d = khoangCach(lat, lon, m.lat, m.lon);
            if (d + accuracy > m.banKinh) continue;
            if (d < min) { thuHai = min; min = d; gan = m; }
            else thuHai = Math.min(thuHai, d);
        }
        if (gan != null && thuHai - min <= accuracy) {
            ungVien = -1;
            trangThai = "Các mốc quá gần nhau; hãy chọn thủ công trên sơ đồ";
            return null;
        }
        if (gan == null) for (Moc m : moc) {
            if (m.id == 0 && khoangCach(lat, lon, m.lat, m.lon) + accuracy <= m.banKinh) gan = m;
        }
        if (gan == null) {
            ungVien = -1;
            trangThai = "Chưa vào vùng nhận diện địa điểm";
            return null;
        }
        if (daPhat.contains(gan.id)) {
            ungVien = -1;
            trangThai = "Đã phát mốc này trong lượt tham quan; có thể nghe lại thủ công";
            return null;
        }
        if (ungVien != gan.id) { ungVien = gan.id; batDau = lucDo; }
        if (lucDo - batDau < 2500) {
            trangThai = "Đã vào vùng phát, đang chờ vị trí ổn định…";
            return null;
        }
        trangThai = "Đã đến mốc " + gan.id;
        return gan.id;
    }

    public void danhDauDaPhat(int id) { daPhat.add(id); ungVien = -1; }
}
