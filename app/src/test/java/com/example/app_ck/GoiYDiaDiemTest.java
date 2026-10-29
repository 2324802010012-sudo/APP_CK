package com.example.app_ck;

import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.ui.home.GoiYDiaDiem;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.*;

public class GoiYDiaDiemTest {
    private DiaDiem diem(int id, String ten, String loai) {
        DiaDiem d = new DiaDiem(id, ten, "Hà Nội", "Kiến trúc và lịch sử", 21, 105, "anh");
        d.danhMuc = loai; return d;
    }
    @Test public void timKhongDauCaTenDiaChiVaMoTa() {
        DiaDiem d = diem(1, "Bảo tàng Chứng tích", "Bảo tàng – lịch sử");
        assertTrue(GoiYDiaDiem.khop(d, "chung tich", null));
        assertTrue(GoiYDiaDiem.khop(d, "ha noi", null));
        assertTrue(GoiYDiaDiem.khop(d, "kien truc", null));
        assertFalse(GoiYDiaDiem.khop(d, "bien", null));
        assertFalse(GoiYDiaDiem.khop(d, "", "Di sản"));
    }
    @Test public void uuTienChuDeDaLuuVaLoaiDiemDaLuu() {
        DiaDiem khac = diem(0, "Huế", "Di sản – kiến trúc");
        DiaDiem daLuu = diem(1, "Bảo tàng", "Bảo tàng – lịch sử");
        DiaDiem cungChuDe = diem(4, "Văn Miếu", "Văn hóa – lịch sử");
        List<DiaDiem> tatCa = Arrays.asList(khac, daLuu, cungChuDe);
        List<DiaDiem> goiY = GoiYDiaDiem.deXuat(tatCa, Collections.singleton(1));
        assertEquals(2, goiY.size());
        assertEquals(4, goiY.get(0).id);
        assertEquals(0, goiY.get(1).id);
        assertEquals(3, tatCa.size());
    }
    @Test public void khachCoDanhSachOnDinhVaLuuHetThiRong() {
        List<DiaDiem> ds = Arrays.asList(diem(2, "Huế", null), diem(0, "Dinh", "Lịch sử"));
        assertEquals(0, GoiYDiaDiem.deXuat(ds, Collections.emptySet()).get(0).id);
        assertTrue(GoiYDiaDiem.deXuat(ds, new HashSet<>(Arrays.asList(0, 2))).isEmpty());
    }
}
