package com.example.app_ck;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class BoNhanDienDiemThanhPhanTest {
    private List<BoChonDiemTuDong.Moc> mocConDao() {
        List<BoChonDiemTuDong.Moc> ds = new ArrayList<>();
        for (CauHinhAutoConDao.Diem d : CauHinhAutoConDao.cacDiemAuto())
            ds.add(new BoChonDiemTuDong.Moc(d.id, -1, d.lat, d.lon, d.banKinh));
        return ds;
    }

    @Test public void demoDungToanBoDanhSachAutoVaKhongLapLai() {
        for (CauHinhAutoConDao.Diem d : CauHinhAutoConDao.cacDiemAuto()) {
            BoNhanDienDiemThanhPhan b = new BoNhanDienDiemThanhPhan(mocConDao());
            long t = 1000;
            for (int distance : new int[]{500, 150, 60, 20}) {
                assertNull(b.capNhat(d.lat + distance / 111195.0, d.lon, 3, t, t));
                t += 2000;
            }
            assertEquals(Integer.valueOf(d.id), b.capNhat(d.lat + 20 / 111195.0, d.lon, 3, t + 1000, t + 1000));
            b.daPhat(d.id);
            assertNull(b.capNhat(d.lat, d.lon, 3, t + 5000, t + 5000));
        }
    }

    @Test public void diemThuCongVaDiemTatKhongDuocTuPhat() {
        assertFalse(CauHinhAutoConDao.CAC_DIEM.get(4).choPhepAuto);
        assertFalse(CauHinhAutoConDao.tim(19).choPhepAuto);
        BoNhanDienDiemThanhPhan b = new BoNhanDienDiemThanhPhan(Collections.emptyList());
        CauHinhAutoConDao.Diem d = CauHinhAutoConDao.CAC_DIEM.get(0);
        assertNull(b.capNhat(d.lat, d.lon, 3, 1000, 1000));
        assertNull(b.capNhat(d.lat, d.lon, 3, 5000, 5000));
    }

    @Test public void khongPhatKhiVungSaiSoCoTheChamHaiDiem() {
        // Tâm B cách A 50 m; ở A, chỉ một vùng chứa trọn sai số, nhưng B vẫn có thể bị nhầm.
        List<BoChonDiemTuDong.Moc> ds = Arrays.asList(
                new BoChonDiemTuDong.Moc(1, -1, 0, 0, 35),
                new BoChonDiemTuDong.Moc(2, -1, 50 / 111195.0, 0, 35));
        BoNhanDienDiemThanhPhan b = new BoNhanDienDiemThanhPhan(ds);
        assertNull(b.capNhat(0, 0, 20, 1000, 1000));
        assertNull(b.capNhat(0, 0, 20, 4000, 4000));
        assertTrue(b.trangThai().contains("Hãy chọn điểm trên sơ đồ"));
        assertNull(b.capNhat(0, 0, 3, 5000, 5000));
        assertEquals(Integer.valueOf(1), b.capNhat(0, 0, 3, 8000, 8000));
    }

    @Test public void fixCuSaiSoLonVaHuyChoPhaiOnDinhLai() {
        CauHinhAutoConDao.Diem d = CauHinhAutoConDao.CAC_DIEM.get(0);
        BoNhanDienDiemThanhPhan b = new BoNhanDienDiemThanhPhan(mocConDao());
        assertNull(b.capNhat(d.lat, d.lon, 3, 1000, 1000));
        assertNull(b.capNhat(d.lat, d.lon, 30, 4000, 4000));
        assertNull(b.capNhat(d.lat, d.lon, 3, 5000, 5000));
        b.huyChoOnDinh();
        assertNull(b.capNhat(d.lat, d.lon, 3, 9000, 9000));
        assertNull(b.capNhat(d.lat, d.lon, 3, 1000, 10000));
        assertNull(b.capNhat(d.lat, d.lon, 3, 12000, 12000));
        assertEquals(Integer.valueOf(d.id), b.capNhat(d.lat, d.lon, 3, 15000, 15000));
        assertNull(b.capNhat(d.lat, d.lon, 3, 1000, 20000));
    }

    @Test public void khongNhanToaDoCuaTourKhac() {
        BoNhanDienDiemThanhPhan b = new BoNhanDienDiemThanhPhan(mocConDao());
        assertNull(b.capNhat(10.77702, 106.69540, 3, 1000, 1000));
        assertNull(b.capNhat(10.77702, 106.69540, 3, 5000, 5000));
    }
}
