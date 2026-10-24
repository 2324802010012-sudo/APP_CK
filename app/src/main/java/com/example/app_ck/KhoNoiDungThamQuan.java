package com.example.app_ck;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Cập nhật bài viết từ dữ liệu Java; giữ ID và dữ liệu định vị của các kho sơ đồ. */
final class KhoNoiDungThamQuan {
    private KhoNoiDungThamQuan() {}

    static DuLieuNoiDungThamQuan.Bai lay(int diaDiem, int diem) {
        return DuLieuNoiDungThamQuan.lay(diaDiem, diem);
    }

    static String bai(DuLieuNoiDungThamQuan.Bai d) {
        return d.noiDung();
    }

    /** Gọi sau khi kho của từng tour đã tạo/seed bảng. Chỉ cập nhật cột bài viết. */
    static void dongBo(SQLiteDatabase db, int diaDiem) {
        final String[] bang = {"thuyet_minh", "bao_tang_marker", "dai_noi_hue_diem", "con_dao_diem", "van_mieu_diem"};
        final String[] cot = {"noi_dung", "noi_dung", "mo_ta", "mo_ta", "mo_ta"};
        final String khoa = diaDiem == 0 ? "diem_tham_quan_id" : "id";
        DuLieuNoiDungThamQuan.Bai d = lay(diaDiem, 0);
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS phien_ban_noi_dung (dia_diem_id INTEGER PRIMARY KEY, phien_ban INTEGER NOT NULL)");
            int phienBan = DuLieuNoiDungThamQuan.PHIEN_BAN;
            try (Cursor c = db.rawQuery("SELECT phien_ban FROM phien_ban_noi_dung WHERE dia_diem_id=?",
                    new String[]{String.valueOf(diaDiem)})) {
                if (c.moveToFirst() && c.getInt(0) >= phienBan) { db.setTransactionSuccessful(); return; }
            }
            ContentValues v = new ContentValues();
            v.put("mo_ta", bai(d));
            if (db.update("dia_diem", v, "id=?", new String[]{String.valueOf(diaDiem)}) != 1)
                throw new IllegalStateException("Thiếu địa điểm " + diaDiem);
            for (DuLieuNoiDungThamQuan.Bai p : d.cacDiem) {
                v.clear(); v.put(cot[diaDiem], bai(p));
                if (db.update(bang[diaDiem], v, khoa + "=?", new String[]{String.valueOf(p.id)}) != 1)
                    throw new IllegalStateException("Thiếu điểm " + diaDiem + "/" + p.id);
            }
            v.clear(); v.put("dia_diem_id", diaDiem); v.put("phien_ban", phienBan);
            db.insertWithOnConflict("phien_ban_noi_dung", null, v, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }

    /** Trang chủ cần giới thiệu mới ngay cả khi tour tương ứng chưa từng được mở. */
    static void dongBoGioiThieu(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS phien_ban_noi_dung (dia_diem_id INTEGER PRIMARY KEY, phien_ban INTEGER NOT NULL)");
            int phienBan = DuLieuNoiDungThamQuan.PHIEN_BAN;
            try (Cursor c = db.rawQuery("SELECT phien_ban FROM phien_ban_noi_dung WHERE dia_diem_id=-1", null)) {
                if (c.moveToFirst() && c.getInt(0) >= phienBan) { db.setTransactionSuccessful(); return; }
            }
            for (DuLieuNoiDungThamQuan.Bai d : DuLieuNoiDungThamQuan.DIA_DIEM) {
                ContentValues v = new ContentValues(); v.put("mo_ta", bai(d));
                if (db.update("dia_diem", v, "id=?", new String[]{String.valueOf(d.id)}) != 1)
                    throw new IllegalStateException("Thiếu địa điểm " + d.id);
            }
            ContentValues phien = new ContentValues();
            phien.put("dia_diem_id", -1); phien.put("phien_ban", phienBan);
            db.insertWithOnConflict("phien_ban_noi_dung", null, phien, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
}
