package com.example.app_ck;

import com.example.app_ck.data.seed.CauHinhAutoConDao;

import org.junit.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class CauHinhConDaoTest {
    @Test public void du19Diem18MarkerVa12MocGpsKhongTrungId() {
        assertEquals(19, CauHinhAutoConDao.CAC_DIEM.size());
        Set<Integer> ids=new HashSet<>(), so=new HashSet<>();
        int auto=0;
        for (CauHinhAutoConDao.Diem d:CauHinhAutoConDao.CAC_DIEM) {
            assertTrue(ids.add(d.id));
            assertTrue(d.ten, d.noiDung.split("\\s+").length>=40);
            assertTrue(d.nguonNoiDung.startsWith("https://"));
            if (d.coMarker()) {
                assertTrue(so.add(d.soSoDo));
                assertTrue(d.xAnh>=0 && d.xAnh<=1 && d.yAnh>=0 && d.yAnh<=1);
            }
            if (d.choPhepAuto) {
                auto++;
                assertTrue(Double.isFinite(d.lat) && Double.isFinite(d.lon));
                assertTrue(d.nguon.startsWith("https://www.openstreetmap.org/"));
            }
        }
        assertEquals(18, so.size());
        assertEquals(12, auto);
        assertFalse(CauHinhAutoConDao.tim(7).coMarker());
    }

    @Test public void soTrenSoDoKhacIdVaChuongCopPhapLaDiemRieng() {
        int[] so={5,6,25,29,28,3,0,1,24,27,30,31,4,21,18,35,2,32,26};
        for (int id=1;id<=19;id++) assertEquals(so[id-1],CauHinhAutoConDao.tim(id).soSoDo);
        assertEquals("Trại Phú Hải",CauHinhAutoConDao.tim(1).ten);
        assertEquals("Nhà Chúa Đảo",CauHinhAutoConDao.tim(6).ten);
        assertEquals("Bảo tàng Côn Đảo",CauHinhAutoConDao.tim(7).ten);
        assertFalse(CauHinhAutoConDao.tim(19).choPhepAuto);
        assertNotEquals(CauHinhAutoConDao.tim(3).noiDung,CauHinhAutoConDao.tim(19).noiDung);
    }
}
