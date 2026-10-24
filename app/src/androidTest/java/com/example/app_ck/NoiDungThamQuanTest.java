package com.example.app_ck;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.HashSet;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoiDungThamQuanTest {
    private static final int[] SO_DIEM = {25, 13, 10, 19, 16};
    private static final String[] BANG = {"thuyet_minh", "bao_tang_marker", "dai_noi_hue_diem", "con_dao_diem", "van_mieu_diem"};

    @Test public void duNamDiaDiemTamMuoiBaDiemVaNguonChinhThuc() {
        assertEquals(5, DuLieuNoiDungThamQuan.DIA_DIEM.size());
        for (int id = 0; id < 5; id++) {
            DuLieuNoiDungThamQuan.Bai diaDiem = KhoNoiDungThamQuan.lay(id, 0);
            assertEquals(SO_DIEM[id], diaDiem.cacDiem.size());
            for (int diem = 0; diem <= SO_DIEM[id]; diem++) {
                DuLieuNoiDungThamQuan.Bai bai = KhoNoiDungThamQuan.lay(id, diem);
                assertFalse(bai.gioiThieu.trim().isEmpty());
                assertFalse(bai.diemNhan.trim().isEmpty());
                assertFalse(bai.thamQuan.trim().isEmpty());
                assertTrue(KhoNoiDungThamQuan.bai(bai).length() < 3500);
                assertFalse(bai.nguon.isEmpty());
                HashSet<String> khongTrung = new HashSet<>();
                for (String maNguon : bai.nguon) {
                    assertTrue(khongTrung.add(maNguon));
                    DuLieuNoiDungThamQuan.Nguon s = DuLieuNoiDungThamQuan.NGUON.get(maNguon);
                    assertNotNull(s);
                    assertTrue(s.url.startsWith("https://"));
                    assertFalse(s.coQuan.isEmpty());
                }
            }
        }
    }

    private SQLiteDatabase taoDuLieuCu() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        db.execSQL("CREATE TABLE dia_diem(id INTEGER PRIMARY KEY, mo_ta TEXT, vi_do REAL, kinh_do REAL)");
        db.execSQL("CREATE TABLE tai_khoan(id INTEGER PRIMARY KEY, mat_khau_bam TEXT)");
        db.execSQL("INSERT INTO tai_khoan VALUES(42,'KhongDoi')");
        db.execSQL("CREATE TABLE yeu_thich(tai_khoan_id INTEGER,dia_diem_id INTEGER)");
        db.execSQL("INSERT INTO yeu_thich VALUES(42,4)");
        for (int id = 0; id < 5; id++) {
            db.execSQL("INSERT INTO dia_diem VALUES(?,'Cu',12.34,56.78)", new Object[]{id});
            String khoa = id == 0 ? "diem_tham_quan_id" : "id";
            String cot = id < 2 ? "noi_dung" : "mo_ta";
            db.execSQL("CREATE TABLE " + BANG[id] + "(" + khoa + " INTEGER PRIMARY KEY," + cot + " TEXT,x REAL,y REAL)");
            for (int p = 1; p <= SO_DIEM[id]; p++)
                db.execSQL("INSERT INTO " + BANG[id] + " VALUES(?,'Cu',0.123,0.456)", new Object[]{p});
        }
        return db;
    }

    @Test public void capNhatTaiChoChayLaiKhongTrungVaGiuTaiKhoanYeuThichToaDo() throws Exception {
        try (SQLiteDatabase db = taoDuLieuCu()) {
            for (int lap = 0; lap < 2; lap++) {
                KhoNoiDungThamQuan.dongBoGioiThieu(db);
                for (int id = 0; id < 5; id++) KhoNoiDungThamQuan.dongBo(db, id);
            }
            for (int id = 0; id < 5; id++) {
                String khoa = id == 0 ? "diem_tham_quan_id" : "id";
                String cot = id < 2 ? "noi_dung" : "mo_ta";
                try (Cursor c = db.rawQuery("SELECT " + khoa + "," + cot + ",x,y FROM " + BANG[id] + " ORDER BY " + khoa, null)) {
                    assertEquals(SO_DIEM[id], c.getCount());
                    while (c.moveToNext()) {
                        assertEquals(KhoNoiDungThamQuan.bai(KhoNoiDungThamQuan.lay(id, c.getInt(0))), c.getString(1));
                        assertEquals(0.123, c.getDouble(2), 0.00001);
                        assertEquals(0.456, c.getDouble(3), 0.00001);
                    }
                }
            }
            try (Cursor c = db.rawQuery("SELECT mat_khau_bam FROM tai_khoan WHERE id=42", null)) {
                assertTrue(c.moveToFirst()); assertEquals("KhongDoi", c.getString(0));
            }
            try (Cursor c = db.rawQuery("SELECT tai_khoan_id,dia_diem_id FROM yeu_thich", null)) {
                assertEquals(1, c.getCount()); assertTrue(c.moveToFirst());
                assertEquals(42, c.getInt(0)); assertEquals(4, c.getInt(1));
            }
            try (Cursor c = db.rawQuery("SELECT vi_do,kinh_do FROM dia_diem", null)) {
                while (c.moveToNext()) { assertEquals(12.34,c.getDouble(0),0.00001); assertEquals(56.78,c.getDouble(1),0.00001); }
            }
        }
    }

    @Test public void thieuDiemThiHoanTacCaBaiVaPhienBan() {
        try (SQLiteDatabase db = taoDuLieuCu()) {
            db.delete("van_mieu_diem", "id=16", null);
            try { KhoNoiDungThamQuan.dongBo(db, 4); fail("Cần báo thiếu điểm"); }
            catch (IllegalStateException expected) { assertTrue(expected.getMessage().contains("4/16")); }
            try (Cursor c = db.rawQuery("SELECT mo_ta FROM van_mieu_diem WHERE id=1", null)) {
                assertTrue(c.moveToFirst()); assertEquals("Cu", c.getString(0));
            }
            // CREATE TABLE cũng được hoàn tác trong giao dịch thất bại.
            try (Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE name='phien_ban_noi_dung'", null)) {
                assertEquals(0, c.getCount());
            }
        }
    }
}
