package com.example.app_ck;

import java.util.ArrayList;
import java.util.List;

/** Chỉ xét danh sách AUTO của tour đang mở; vùng sai số không được chạm hai mốc. */
public final class BoNhanDienDiemThanhPhan {
    private final List<BoChonDiemTuDong.Moc> cacMoc;
    private final BoChonDiemTuDong boChon = new BoChonDiemTuDong();
    private String trangThai = "Đang chờ vị trí";

    public BoNhanDienDiemThanhPhan(List<BoChonDiemTuDong.Moc> cacMoc) {
        this.cacMoc = new ArrayList<>(cacMoc);
    }

    public Integer capNhat(double lat, double lon, float saiSo, long lucDo, long now) {
        if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90
                || Math.abs(lon) > 180 || !Float.isFinite(saiSo) || saiSo < 0 || saiSo > 25
                || lucDo > now || now - lucDo > 15000) {
            huyChoOnDinh();
            trangThai = "Vị trí chưa đủ chính xác hoặc đã cũ. Hãy chọn điểm trên sơ đồ để nghe.";
            return null;
        }
        int coTheDen = 0;
        for (BoChonDiemTuDong.Moc moc : cacMoc) {
            if (BoChonDiemTuDong.khoangCach(lat, lon, moc.lat, moc.lon) - saiSo <= moc.banKinh)
                coTheDen++;
        }
        if (coTheDen > 1) {
            huyChoOnDinh();
            trangThai = "Không phân biệt được các điểm. Hãy chọn điểm trên sơ đồ để nghe.";
            return null;
        }
        Integer den = boChon.capNhat(cacMoc, -1, lat, lon, saiSo, lucDo, now);
        trangThai = boChon.trangThai;
        return den;
    }

    public void huyChoOnDinh() {
        boChon.capNhat(cacMoc, -1, Double.NaN, 0, Float.NaN, 0, 0);
    }

    public void daPhat(int id) { boChon.danhDauDaPhat(id); }
    public String trangThai() { return trangThai; }
}
