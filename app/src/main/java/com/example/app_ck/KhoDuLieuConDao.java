package com.example.app_ck;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Nâng cấp dữ liệu Côn Đảo tại chỗ, giữ ID 1–7 và các bảng tài khoản/yêu thích. */
final class KhoDuLieuConDao {
    private static final int PHIEN_BAN = 2;
    private KhoDuLieuConDao() {}

    static void dongBo(SQLiteDatabase db) {
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
                db.update("con_dao_diem", v, "id=? AND phien_ban<?",
                        new String[]{String.valueOf(d.id), String.valueOf(PHIEN_BAN)});
            }
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
}
