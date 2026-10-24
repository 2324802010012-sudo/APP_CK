package com.example.app_ck;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class KhoDuLieuConDaoTest {
    @Test public void nangCapBayDiemCuKhongMatBangKhacVaKhongTrungKhiChayLai() {
        try (SQLiteDatabase db=SQLiteDatabase.create(null)) {
            db.execSQL("CREATE TABLE con_dao_diem(id INTEGER PRIMARY KEY,ten TEXT NOT NULL,mo_ta TEXT NOT NULL)");
            for (int id=1;id<=7;id++) db.execSQL("INSERT INTO con_dao_diem VALUES(?, 'Cu', 'Noi dung cu')",new Object[]{id});
            db.execSQL("CREATE TABLE du_lieu_khac(id INTEGER PRIMARY KEY,ten TEXT)");
            db.execSQL("INSERT INTO du_lieu_khac VALUES(1,'Giu nguyen')");
            KhoDuLieuConDao.dongBo(db);
            KhoDuLieuConDao.dongBo(db);
            try (Cursor c=db.rawQuery("SELECT COUNT(*) FROM con_dao_diem",null)) { c.moveToFirst(); assertEquals(19,c.getInt(0)); }
            try (Cursor c=db.rawQuery("SELECT id,ten,mo_ta FROM con_dao_diem ORDER BY id",null)) {
                for (CauHinhAutoConDao.Diem d:CauHinhAutoConDao.CAC_DIEM) {
                    assertTrue(c.moveToNext()); assertEquals(d.id,c.getInt(0));
                    assertEquals(d.ten,c.getString(1)); assertEquals(d.noiDung,c.getString(2));
                }
            }
            try (Cursor c=db.rawQuery("SELECT ten FROM du_lieu_khac WHERE id=1",null)) {
                assertTrue(c.moveToFirst()); assertEquals("Giu nguyen",c.getString(0));
            }
        }
    }
}
