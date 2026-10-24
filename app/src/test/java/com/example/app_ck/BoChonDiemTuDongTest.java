package com.example.app_ck;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class BoChonDiemTuDongTest {
    private final BoChonDiemTuDong.Moc a = new BoChonDiemTuDong.Moc(1, 1, 10, 106, 40);
    @Test public void chiPhatSauKhiOnDinhVaKhongLapLai() {
        BoChonDiemTuDong b = new BoChonDiemTuDong();
        List<BoChonDiemTuDong.Moc> ds = Collections.singletonList(a);
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 1000, 1000));
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 3000, 3000));
        assertEquals(Integer.valueOf(1), b.capNhat(ds, 1, 10, 106, 5, 4000, 4000));
        b.danhDauDaPhat(1);
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 8000, 8000));
    }
    @Test public void khongPhatTuFixCuSaiSoLonVaToaDoLoi() {
        BoChonDiemTuDong b = new BoChonDiemTuDong();
        List<BoChonDiemTuDong.Moc> ds = Collections.singletonList(a);
        assertNull(b.capNhat(ds, 1, 10, 106, 100, 1000, 1000));
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 1000, 20000));
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 40000, 20000));
        assertNull(b.capNhat(ds, 1, Double.NaN, 106, 5, 21000, 21000));
        assertNull(b.capNhat(ds, 1, 91, 106, 5, 22000, 22000));
        assertNull(b.capNhat(ds, 1, 10, 106, Float.NaN, 23000, 23000));
    }
    @Test public void khongNhanHaiFixTrungVaPhaiChonDungTang() {
        BoChonDiemTuDong b = new BoChonDiemTuDong();
        List<BoChonDiemTuDong.Moc> ds = Collections.singletonList(a);
        assertNull(b.capNhat(ds, 2, 10, 106, 5, 1000, 1000));
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 2000, 2000));
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 2000, 6000));
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 7000, 7000));
        assertEquals(Integer.valueOf(1), b.capNhat(ds, 1, 10, 106, 5, 10000, 10000));
    }
    @Test public void uuTienMocCuTheVaTuChoiHaiPhongKhongPhanBietDuoc() {
        BoChonDiemTuDong b = new BoChonDiemTuDong();
        BoChonDiemTuDong.Moc chung = new BoChonDiemTuDong.Moc(0, -1, 10, 106, 250);
        List<BoChonDiemTuDong.Moc> ds = Arrays.asList(chung, a);
        assertNull(b.capNhat(ds, 1, 10, 106, 5, 1000, 1000));
        assertEquals(Integer.valueOf(1), b.capNhat(ds, 1, 10, 106, 5, 4000, 4000));
        BoChonDiemTuDong.Moc phongGan = new BoChonDiemTuDong.Moc(2, 1, 10.00001, 106, 40);
        assertNull(b.capNhat(Arrays.asList(a, phongGan), 1, 10, 106, 5, 5000, 5000));
        assertNull(b.capNhat(Arrays.asList(a, phongGan), 1, 10, 106, 5, 8000, 8000));
    }
    @Test public void khongPhatKhiVungSaiSoVuotBienBanKinh() {
        BoChonDiemTuDong b = new BoChonDiemTuDong();
        List<BoChonDiemTuDong.Moc> ds = Collections.singletonList(a);
        assertNull(b.capNhat(ds, 1, 10.0003, 106, 20, 1000, 1000));
        assertNull(b.capNhat(ds, 1, 10.0003, 106, 20, 4000, 4000));
        assertEquals(111195, BoChonDiemTuDong.khoangCach(0, 0, 1, 0), 2);
    }
    @Test public void demoDiHetTuyenCuaCaNamDiaDiem() {
        for (int soMoc : new int[]{25, 13, 10, 19, 16}) {
            BoChonDiemTuDong b = new BoChonDiemTuDong();
            long now = 1000;
            for (int id = 1; id <= soMoc; id++) {
                double lon = id * .002;
                List<BoChonDiemTuDong.Moc> ds = Collections.singletonList(
                        new BoChonDiemTuDong.Moc(id, -1, 0, lon, 40));
                assertNull(b.capNhat(ds, -1, 0, lon, 3, now, now));
                now += 3000;
                assertEquals(Integer.valueOf(id), b.capNhat(ds, -1, 0, lon, 3, now, now));
                b.danhDauDaPhat(id);
                now += 3000;
            }
        }
    }
}
