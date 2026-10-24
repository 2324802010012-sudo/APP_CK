package com.example.app_ck;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class AutoDiSanTest {
    private List<String> ten(int n) {
        List<String> ds = new ArrayList<>();
        for (int i=1; i<=n; i++) ds.add("Điểm " + i);
        return ds;
    }

    @Test public void moiMocAutoCoTheNhanDienSauOnDinhKhongPhatLai() {
        for (CauHinhAutoDiSan tour : new CauHinhAutoDiSan[]{
                CauHinhAutoDiSan.hue(ten(10)), CauHinhAutoDiSan.vanMieu(ten(16))}) {
            List<BoChonDiemTuDong.Moc> mocs = new ArrayList<>();
            for (CauHinhAutoDiSan.Diem d : tour.cacDiem) if (d.choPhepAuto)
                mocs.add(new BoChonDiemTuDong.Moc(d.id,-1,d.lat,d.lon,d.banKinh));
            BoNhanDienDiemThanhPhan bo = new BoNhanDienDiemThanhPhan(mocs);
            long t = 100000;
            for (CauHinhAutoDiSan.Diem d : tour.cacDiem) if (d.choPhepAuto) {
                assertTrue(d.nguon.startsWith("https://www.openstreetmap.org/way/"));
                assertNull(bo.capNhat(d.lat,d.lon,3,t,t));
                assertEquals(Integer.valueOf(d.id),bo.capNhat(d.lat,d.lon,3,t+3000,t+3000));
                bo.daPhat(d.id);
                assertNull(bo.capNhat(d.lat,d.lon,3,t+6000,t+6000));
                t+=10000;
            }
        }
    }

    @Test public void vanMieuVungNhoKhongPhatKhiSaiSoBaoTrumVung() {
        CauHinhAutoDiSan tour=CauHinhAutoDiSan.vanMieu(ten(16));
        CauHinhAutoDiSan.Diem d=tour.cacDiem.get(3);
        BoNhanDienDiemThanhPhan bo=new BoNhanDienDiemThanhPhan(java.util.Collections.singletonList(
                new BoChonDiemTuDong.Moc(d.id,-1,d.lat,d.lon,d.banKinh)));
        assertNull(bo.capNhat(d.lat,d.lon,20,100000,100000));
        assertNull(bo.capNhat(d.lat,d.lon,20,104000,104000));
        assertFalse(tour.cacDiem.get(6).choPhepAuto); // Nhà bia không nhận diện GPS.
        assertFalse(tour.cacDiem.get(9).choPhepAuto); // Điện Đại Thành thủ công.
    }
}
