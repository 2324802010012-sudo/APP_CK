package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.seed.KhoDuLieuConDao;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoToaDoThamQuan;
import com.example.app_ck.data.seed.CauHinhAutoConDao;
import com.example.app_ck.location.CauHinhAutoDiSan;

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
                    assertEquals(d.id <= 7 ? "Cu" : d.ten,c.getString(1));
                    assertEquals(d.id <= 7 ? "Noi dung cu" : d.noiDung,c.getString(2));
                }
            }
            try (Cursor c=db.rawQuery("SELECT ten FROM du_lieu_khac WHERE id=1",null)) {
                assertTrue(c.moveToFirst()); assertEquals("Giu nguyen",c.getString(0));
            }
        }
    }

    @Test public void cauHinhAutoDocDuLieuChungVaKiemTraQuyenTaiLucGhi() {
        android.content.Context base = androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation().getTargetContext();
        android.content.Context context = new android.content.ContextWrapper(base) {
            @Override public android.content.SharedPreferences getSharedPreferences(String name, int mode) {
                return base.getSharedPreferences("auto_sqlite_test_" + name, mode);
            }
        };
        android.content.SharedPreferences phien = context.getSharedPreferences("phien_dang_nhap", 0);
        try (SQLiteDatabase db = SQLiteDatabase.create(null); AppDatabase helper = new AppDatabase(context)) {
            helper.onCreate(db);
            db.execSQL("INSERT INTO tai_khoan(id,ten_dang_nhap,muoi,mat_khau_bam,vai_tro,trang_thai) "
                    + "VALUES(77,'auto_admin','salt','hash','ADMIN','ACTIVE')");
            phien.edit().putLong("id", 77).commit();
            android.content.ContentValues values = new android.content.ContentValues();
            values.put("ten", "Điểm mới ngoài danh mục Java");
            values.put("mo_ta", "Mô tả mới");
            values.put("noi_dung_thuyet_minh", "Bài thuyết minh mới");
            values.put("thu_tu", 0);
            assertTrue(KhoDuLieuThamQuan.luuDiem(context, db, 3, 901, values));
            assertTrue(KhoToaDoThamQuan.luu(context, db, 3, 901, 0, 8.70, 106.63, 5, 45));
            CauHinhAutoDiSan config = CauHinhAutoDiSan.tuSQLite(db, 3);
            CauHinhAutoDiSan.Diem moi = null;
            for (CauHinhAutoDiSan.Diem d : config.cacDiem) if (d.id == 901) moi = d;
            assertNotNull(moi);
            assertEquals("Điểm mới ngoài danh mục Java", moi.ten);
            assertTrue(moi.choPhepAuto);
            assertEquals(45, moi.banKinh, 0);
            assertEquals(8.70, moi.lat, 0);
            assertNull("Cùng mã ở địa điểm khác phải độc lập", KhoDuLieuThamQuan.lay(db, 2, 901));
            assertTrue(KhoLichSu.ghi(db, 77, 3, 901, KhoLichSu.MANUAL, 1000, "diem-moi-901"));
            assertEquals("Điểm mới ngoài danh mục Java", KhoLichSu.doc(db, 77).get(0).tenDiem);
            db.execSQL("UPDATE tai_khoan SET vai_tro='USER' WHERE id=77");
            try {
                KhoToaDoThamQuan.luu(context, db, 3, 901, 0, 8.71, 106.64, 5, 30);
                fail("USER không được thay cấu hình AUTO dù phiên còn lưu cùng ID");
            } catch (SecurityException expected) { }
            assertEquals(8.70, KhoDuLieuThamQuan.lay(db, 3, 901).viDo, 0);
            try {
                KhoToaDoThamQuan.luu(db, 3, 901, 0, 8.71, 106.64, 5, 30);
                fail("API cũ không có phiên phải từ chối");
            } catch (SecurityException expected) { }
        } finally {
            phien.edit().clear().commit();
        }
    }
}
