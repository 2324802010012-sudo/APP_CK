package com.example.app_ck;

import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class KhoLichSuTest {
    private SQLiteDatabase taoDb() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        db.execSQL("CREATE TABLE tai_khoan(id INTEGER PRIMARY KEY)");
        db.execSQL("INSERT INTO tai_khoan VALUES(1),(2)");
        db.execSQL("CREATE TABLE dia_diem(id INTEGER PRIMARY KEY)");
        db.execSQL("INSERT INTO dia_diem VALUES(0),(1),(2),(3),(4)");
        KhoLichSu.taoBang(db);
        return db;
    }

    @Test public void dungChuSoHuuThuTuThoiGianVaKhongTrungCallback() {
        try (SQLiteDatabase db = taoDb()) {
            assertTrue(KhoLichSu.ghi(db,1,0,1,KhoLichSu.THU_CONG,100,"luot-1"));
            assertFalse(KhoLichSu.ghi(db,1,0,1,KhoLichSu.THU_CONG,200,"luot-1"));
            assertTrue(KhoLichSu.ghi(db,1,4,6,KhoLichSu.DEMO,300,"luot-2"));
            assertTrue(KhoLichSu.ghi(db,2,3,19,KhoLichSu.AUTO_GPS,400,"luot-1"));
            ArrayList<KhoLichSu.Luot> a=KhoLichSu.doc(db,1), b=KhoLichSu.doc(db,2);
            assertEquals(2,a.size()); assertEquals(1,b.size());
            assertEquals(4,a.get(0).diaDiem); assertEquals(6,a.get(0).diem);
            assertEquals(KhoLichSu.DEMO,a.get(0).cheDo); assertEquals(300,a.get(0).thoiGian);
            assertEquals("Khuê Văn Các",a.get(0).tenDiem);
            assertEquals(0,a.get(1).diaDiem); assertEquals(1,a.get(1).diem);
            assertTrue(KhoLichSu.doc(db,-1).isEmpty());
        }
    }

    @Test public void tuChoiDuLieuKhongHopLeVaTaiKhoanKhongTonTai() {
        try (SQLiteDatabase db=taoDb()) {
            assertFalse(KhoLichSu.ghi(db,-1,0,0,KhoLichSu.THU_CONG,1,"a"));
            assertFalse(KhoLichSu.ghi(db,99,0,0,KhoLichSu.THU_CONG,1,"b"));
            assertFalse(KhoLichSu.ghi(db,1,0,99,KhoLichSu.THU_CONG,1,"c"));
            assertFalse(KhoLichSu.ghi(db,1,99,0,KhoLichSu.THU_CONG,1,"d"));
            assertFalse(KhoLichSu.ghi(db,1,0,0,"KHAC",1,"e"));
            assertFalse(KhoLichSu.ghi(db,1,0,0,KhoLichSu.THU_CONG,0,"f"));
            assertTrue(KhoLichSu.doc(db,1).isEmpty());
        }
    }

    @Test public void xoaRiengMotLuotVaTatCaKhongAnhHuongTaiKhoanKhac() {
        try(SQLiteDatabase db=taoDb()) {
            KhoLichSu.ghi(db,1,0,0,KhoLichSu.THU_CONG,1,"a");
            KhoLichSu.ghi(db,1,1,13,KhoLichSu.THU_CONG,2,"b");
            KhoLichSu.ghi(db,2,2,10,KhoLichSu.AUTO_GPS,3,"c");
            long id=KhoLichSu.doc(db,1).get(0).id;
            KhoLichSu.xoa(db,2,id); assertEquals(2,KhoLichSu.doc(db,1).size());
            KhoLichSu.xoa(db,1,id); assertEquals(1,KhoLichSu.doc(db,1).size());
            KhoLichSu.xoa(db,1,null); assertTrue(KhoLichSu.doc(db,1).isEmpty());
            assertEquals(1,KhoLichSu.doc(db,2).size());
        }
    }

    @Test public void chuanHoaCheDoVaTuChoiAutoSaiCapDiaDiem() {
        try(SQLiteDatabase db=taoDb()) {
            assertTrue(KhoLichSu.ghi(db,1,0,0,"THU_CONG",1,"manual-cu"));
            assertTrue(KhoLichSu.ghi(db,1,2,0,"AUTO_GPS",2,"chinh-cu"));
            assertTrue(KhoLichSu.ghi(db,1,4,6,"AUTO_GPS",3,"con-cu"));
            assertEquals(KhoLichSu.AUTO_SUB,KhoLichSu.doc(db,1).get(0).cheDo);
            assertEquals(KhoLichSu.AUTO_MAIN,KhoLichSu.doc(db,1).get(1).cheDo);
            assertEquals(KhoLichSu.MANUAL,KhoLichSu.doc(db,1).get(2).cheDo);
            assertFalse(KhoLichSu.ghi(db,1,0,1,KhoLichSu.AUTO_MAIN,4,"sai-chinh"));
            assertFalse(KhoLichSu.ghi(db,1,0,0,KhoLichSu.AUTO_SUB,4,"sai-con"));
            assertEquals("Tự động · Địa điểm chính",KhoLichSu.nhanCheDo(KhoLichSu.AUTO_MAIN));
            assertEquals("Tự động · Điểm thành phần",KhoLichSu.nhanCheDo(KhoLichSu.AUTO_SUB));
            assertEquals("Demo · Mô phỏng",KhoLichSu.nhanCheDo(KhoLichSu.DEMO));
        }
    }
}
