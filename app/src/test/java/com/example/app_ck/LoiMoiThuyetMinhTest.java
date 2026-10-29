package com.example.app_ck;

import com.example.app_ck.location.LoiMoiThuyetMinh;

import org.junit.Test;
import static org.junit.Assert.*;

public class LoiMoiThuyetMinhTest {
    private LoiMoiThuyetMinh loiMoi() { return new LoiMoiThuyetMinh(10, 106); }

    @Test public void banKinhCauHinhThayDoiVungMoiVaVanChoXacNhan() {
        LoiMoiThuyetMinh hep = new LoiMoiThuyetMinh(10, 106, 20);
        LoiMoiThuyetMinh rong = new LoiMoiThuyetMinh(10, 106, 200);
        assertFalse(hep.capNhat(10.0008, 106, 3, 1000, 1000));
        assertFalse(hep.capNhat(10.0008, 106, 3, 4000, 4000));
        assertFalse(rong.capNhat(10.0008, 106, 3, 1000, 1000));
        assertTrue(rong.capNhat(10.0008, 106, 3, 4000, 4000));
        assertFalse(rong.daDongY());
        assertTrue(rong.traLoi(true));
    }

    @Test public void denGanChiMoiKhongTuDongYPhat() {
        LoiMoiThuyetMinh m = loiMoi();
        assertFalse(m.capNhat(10, 106, 5, 1000, 1000));
        assertTrue(m.capNhat(10, 106, 5, 4000, 4000));
        assertTrue(m.daMoi());
        assertFalse(m.daDongY());
        assertTrue(m.traLoi(true));
        assertTrue(m.daDongY());
        assertFalse(m.traLoi(true));
    }

    @Test public void tuChoiKhongPhatVaKhongHoiLapKeCaRaVaoLai() {
        LoiMoiThuyetMinh m = loiMoi();
        m.capNhat(10, 106, 5, 1000, 1000);
        assertTrue(m.capNhat(10, 106, 5, 4000, 4000));
        assertFalse(m.traLoi(false));
        assertFalse(m.daDongY());
        assertFalse(m.capNhat(10.01, 106, 5, 8000, 8000));
        assertFalse(m.capNhat(10, 106, 5, 12000, 12000));
        assertFalse(m.capNhat(10, 106, 5, 16000, 16000));
        assertFalse(m.traLoi(true));
    }

    @Test public void ngoaiVungSaiSoLonVaFixCuKhongDuocMoi() {
        LoiMoiThuyetMinh m = loiMoi();
        assertFalse(m.capNhat(10.006, 106, 5, 1000, 1000));
        assertFalse(m.capNhat(10.006, 106, 5, 4000, 4000));
        assertFalse(m.capNhat(10, 106, 26, 8000, 8000));
        assertFalse(m.capNhat(10, 106, 5, 9000, 25000));
        assertFalse(m.capNhat(10, 106, 5, 30000, 25000));
        assertFalse(m.capNhat(Double.NaN, 106, 5, 26000, 26000));
        assertFalse(m.daMoi());
        assertFalse(m.traLoi(true));
    }

    @Test public void phaiOnDinhLienTucKhongDemFixTrungHayThoiGianNgoaiVung() {
        LoiMoiThuyetMinh m = loiMoi();
        assertFalse(m.capNhat(10, 106, 5, 1000, 1000));
        assertFalse(m.capNhat(10, 106, 5, 1000, 4000));
        assertFalse(m.capNhat(10.006, 106, 5, 5000, 5000));
        assertFalse(m.capNhat(10, 106, 5, 8000, 8000));
        assertFalse(m.capNhat(10, 106, 5, 10000, 10000));
        assertTrue(m.capNhat(10, 106, 5, 11000, 11000));
    }

    @Test public void matGpsLauPhaiChoOnDinhLai() {
        LoiMoiThuyetMinh m = loiMoi();
        assertFalse(m.capNhat(10, 106, 5, 1000, 1000));
        assertFalse(m.capNhat(10, 106, 5, 30000, 30000));
        assertTrue(m.capNhat(10, 106, 5, 33000, 33000));
    }

    @Test public void huyKhiRoiManHinhKhongNhanDongYCu() {
        LoiMoiThuyetMinh m = loiMoi();
        m.capNhat(10, 106, 5, 1000, 1000);
        m.capNhat(10, 106, 5, 4000, 4000);
        m.huy();
        assertFalse(m.traLoi(true));
        assertFalse(m.daDongY());
        assertFalse(m.capNhat(10, 106, 5, 9000, 9000));
    }

    @Test public void caNamDiaDiemChiNhanDienMocLonVaKhoiDongLaiCoTheMoiLai() {
        for (double[] p : new double[][]{{10.77702,106.69540}, {10.779475,106.692132},
                {16.4689726,107.5781266}, {8.68907,106.61484}, {21.02861,105.83556}}) {
            for (int luot = 0; luot < 2; luot++) {
                LoiMoiThuyetMinh m = new LoiMoiThuyetMinh(p[0], p[1]);
                assertFalse(m.capNhat(p[0]+.006, p[1], 3, 1000, 1000));
                assertFalse(m.capNhat(p[0]+.0008, p[1], 3, 2000, 2000));
                assertTrue(m.capNhat(p[0]+.0008, p[1], 3, 5000, 5000));
                assertFalse(m.daDongY());
                assertTrue(m.traLoi(true));
                assertFalse(m.capNhat(p[0], p[1], 3, 9000, 9000));
            }
        }
    }
}
