package com.example.app_ck.data.seed;

import com.example.app_ck.data.seed.CauHinhAutoConDao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Seed bảng cũ chỉ phục vụ chuyển đổi; không được dùng như nguồn vận hành tour. */
public final class KhoDuLieuConDao {
    private static final int PHIEN_BAN = 2;
    private KhoDuLieuConDao() {}

    public static void dongBo(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS con_dao_diem (id INTEGER PRIMARY KEY, "
                    + "ten TEXT NOT NULL, mo_ta TEXT NOT NULL, phien_ban INTEGER NOT NULL DEFAULT 0)");
            boolean coPhienBan = false;
            try (Cursor c = db.rawQuery("PRAGMA table_info(con_dao_diem)", null)) {
                while (c.moveToNext()) if ("phien_ban".equals(c.getString(c.getColumnIndexOrThrow("name")))) coPhienBan = true;
            }
            if (!coPhienBan) db.execSQL("ALTER TABLE con_dao_diem ADD COLUMN phien_ban INTEGER NOT NULL DEFAULT 0");
            for (CauHinhAutoConDao.Diem d : CauHinhAutoConDao.CAC_DIEM) {
                ContentValues v = new ContentValues();
                v.put("id", d.id); v.put("ten", d.ten); v.put("mo_ta", d.noiDung); v.put("phien_ban", PHIEN_BAN);
                db.insertWithOnConflict("con_dao_diem", null, v, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
}
