package com.example.app_ck;

import java.util.Collections;

/** Nhận diện đến gần địa điểm chính; luôn chờ đồng ý trước khi phát. */
public final class LoiMoiThuyetMinh {
    public static final float BAN_KINH = 100;
    private final BoChonDiemTuDong boChon = new BoChonDiemTuDong();
    private final BoChonDiemTuDong.Moc diaDiem;
    private boolean daMoi, choTraLoi, daDongY;

    public LoiMoiThuyetMinh(double viDo, double kinhDo) {
        diaDiem = new BoChonDiemTuDong.Moc(0, -1, viDo, kinhDo, BAN_KINH);
    }

    public boolean capNhat(double lat, double lon, float accuracy, long lucDo, long bayGio) {
        if (daMoi) return false;
        Integer den = boChon.capNhat(Collections.singletonList(diaDiem), -1,
                lat, lon, accuracy, lucDo, bayGio);
        if (den == null) return false;
        daMoi = true;
        choTraLoi = true;
        return true;
    }

    /** Chỉ chấp nhận một câu trả lời cho lời mời đang mở. */
    public boolean traLoi(boolean dongY) {
        if (!choTraLoi) return false;
        choTraLoi = false;
        daDongY = dongY;
        return dongY;
    }

    public void huy() { daMoi = true; choTraLoi = false; daDongY = false; }
    public boolean daMoi() { return daMoi; }
    public boolean daDongY() { return daDongY; }
    public String trangThai() { return boChon.trangThai; }
}
