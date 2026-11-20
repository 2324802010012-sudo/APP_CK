package com.example.app_ck;

import com.example.app_ck.util.VanBanTimKiem;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class VanBanTimKiemTest {
    @Test public void khongPhanBietDauHoaThuongVaKhoangTrang() {
        assertEquals("dai noi hue", VanBanTimKiem.chuanHoa("  ĐẠI  NỘI\nHuế  "));
        assertEquals("dinh doc lap", VanBanTimKiem.chuanHoa("Dinh Độc Lập"));
        assertEquals("", VanBanTimKiem.chuanHoa(null));
    }

    @Test public void goiYGiuQuyTacKhoangTrangCu() {
        assertEquals("van  mieu", VanBanTimKiem.boDau(" Văn  Miếu "));
        assertEquals("hue", VanBanTimKiem.boDau("Hue\u0301"));
    }
}
