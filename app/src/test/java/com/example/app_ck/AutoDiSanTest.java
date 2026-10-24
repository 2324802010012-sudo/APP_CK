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
            List<BoChonDiemTuDong.Moc> mocs = tour.cacMocTuDong();
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

    @Test public void chiQuetDungDanhSachDiemDuocChoPhepTrongTungTour() {
        CauHinhAutoDiSan[] tours = {CauHinhAutoDiSan.hue(ten(10)), CauHinhAutoDiSan.vanMieu(ten(16))};
        int[][] ids = {{1, 2, 3, 4, 7, 8, 9}, {4, 6, 11}};
        for (int i = 0; i < tours.length; i++) {
            CauHinhAutoDiSan tour = tours[i];
            List<BoChonDiemTuDong.Moc> mocs = tour.cacMocTuDong();
            assertEquals(ids[i].length, mocs.size());
            for (int j = 0; j < ids[i].length; j++) assertEquals(ids[i][j], mocs.get(j).id);
            for (CauHinhAutoDiSan.Diem d : tour.cacDiem) {
                boolean coMoc = mocs.stream().anyMatch(m -> m.id == d.id);
                assertEquals(coMoc, tour.choPhepAuto(d.id));
                if (!coMoc) assertEquals("Nghe thủ công", tour.nhanCheDo(d.id));
            }
            assertFalse(tour.choPhepAuto(999));
        }
    }

    @Test public void coToaDoVanPhaiDuocChoPhepRiengMoiBatAuto() {
        String nguon = "https://www.openstreetmap.org/way/702185155";
        assertFalse(new CauHinhAutoDiSan.Diem(1, "Phòng sát nhau", false,
                21.02858, 105.83589, 15, nguon).choPhepAuto);
        assertTrue(new CauHinhAutoDiSan.Diem(2, "Mốc ngoài trời", true,
                21.02858, 105.83589, 15, nguon).choPhepAuto);
        assertFalse(new CauHinhAutoDiSan.Diem(3, "Chưa có mốc", true,
                Double.NaN, Double.NaN, 15, nguon).choPhepAuto);
        assertFalse(new CauHinhAutoDiSan.Diem(4, "Mốc lỗi", true,
                91, 105, 15, nguon).choPhepAuto);
    }
}
