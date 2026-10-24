package com.example.app_ck;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class BoNhanDienDiaDiemTest {
    private DiaDiem diem(int id, double lat) {
        return new DiaDiem(id, "Điểm " + id, "", "Bài " + id, lat, 106, "");
    }
    private BoNhanDienDiaDiem boChon() {
        return new BoNhanDienDiaDiem(Arrays.asList(diem(0, 10), diem(1, 10.01)));
    }

    @Test public void demo50030015090ChiMoiTrong100MetVaSauOnDinh() {
        BoNhanDienDiaDiem b = boChon();
        int[] ds = {500,300,150,90};
        long t = 1000;
        for (int m : ds) {
            assertNull(b.capNhat(10 + m / 111195.0, 106, 3, t, t));
            t += 2000;
        }
        DiaDiem den = b.capNhat(10 + 90 / 111195.0, 106, 3, t+1000, t+1000);
        assertNotNull(den);
        assertEquals(0, den.id);
        assertTrue(b.dangChoTraLoi());
        assertFalse(b.traLoi(1, true));
        assertTrue(b.traLoi(0, true));
        assertFalse(b.traLoi(0, true));
    }

    @Test public void tuChoiKhongHoiLapNhungVanNhanDienDiaDiemKhac() {
        BoNhanDienDiaDiem b = boChon();
        b.capNhat(10,106,3,1000,1000);
        assertEquals(0,b.capNhat(10,106,3,4000,4000).id);
        assertFalse(b.traLoi(0,false));
        assertNull(b.capNhat(10,106,3,8000,8000));
        assertNull(b.capNhat(10.01,106,3,10000,10000));
        assertEquals(1,b.capNhat(10.01,106,3,13000,13000).id);
        assertTrue(b.traLoi(1,true));
    }

    @Test public void uuTienGanNhatKeCaId0VaKhongChoChongLoiMoi() {
        BoNhanDienDiaDiem b = new BoNhanDienDiaDiem(Arrays.asList(diem(0,10),diem(1,10.0005)));
        b.capNhat(10,106,3,1000,1000);
        assertEquals(0,b.capNhat(10,106,3,4000,4000).id);
        assertNull(b.capNhat(10.0005,106,3,8000,8000));
        assertTrue(b.traLoi(0,true));
        assertNull(b.capNhat(10.0005,106,3,10000,10000));
        assertEquals(1,b.capNhat(10.0005,106,3,13000,13000).id);
    }

    @Test public void doiDiemGanNhatPhaiChoOnDinhLai() {
        BoNhanDienDiaDiem b = boChon();
        b.capNhat(10,106,3,1000,1000);
        assertNull(b.capNhat(10.01,106,3,3000,3000));
        assertNull(b.capNhat(10,106,3,5000,5000));
        assertEquals(0,b.capNhat(10,106,3,8000,8000).id);
    }

    @Test public void boQuaFixCuLapSaiSoLonVaHuyCallbackCu() {
        BoNhanDienDiaDiem b = boChon();
        assertNull(b.capNhat(10,106,30,1000,1000));
        assertNull(b.capNhat(10,106,3,1000,20000));
        assertNull(b.capNhat(10,106,3,22000,22000));
        assertNull(b.capNhat(10,106,3,22000,26000));
        assertNull(b.capNhat(10,106,3,27000,27000));
        assertEquals(0,b.capNhat(10,106,3,30000,30000).id);
        b.huy();
        assertFalse(b.traLoi(0,true));
        assertNull(b.capNhat(10.01,106,3,40000,40000));
    }

    @Test public void saiSoVuotBienKhongDuocMoiVaKhoiDongLaiDuocMoiMoi() {
        for (int luot=0;luot<2;luot++) {
            BoNhanDienDiaDiem b=boChon();
            assertNull(b.capNhat(10 + 95 / 111195.0,106,10,1000,1000));
            assertNull(b.capNhat(10 + 95 / 111195.0,106,10,4000,4000));
            assertNull(b.capNhat(10 + 90 / 111195.0,106,3,5000,5000));
            assertEquals(0,b.capNhat(10 + 90 / 111195.0,106,3,8000,8000).id);
            assertTrue(b.traLoi(0,true));
        }
    }
}
