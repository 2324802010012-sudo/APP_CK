package com.example.app_ck;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/** Lịch sử nghe theo tài khoản; điểm thành phần luôn đi cùng ID địa điểm lớn. */
final class KhoLichSu {
    static final String MANUAL = "MANUAL", AUTO_MAIN = "AUTO_MAIN", AUTO_SUB = "AUTO_SUB", DEMO = "DEMO";
    // Giữ đầu vào cũ cho các điểm gọi đang dùng chung; dữ liệu lưu luôn được chuẩn hóa.
    static final String THU_CONG = MANUAL, AUTO_GPS = "AUTO_GPS";
    private KhoLichSu() {}

    static void taoBang(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS lich_su_nghe (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "tai_khoan_id INTEGER NOT NULL, dia_diem_id INTEGER NOT NULL, diem_id INTEGER, "
                + "ten_dia_diem TEXT NOT NULL, ten_diem TEXT NOT NULL, che_do TEXT NOT NULL "
                + "CHECK(che_do IN ('MANUAL','AUTO_MAIN','AUTO_SUB','DEMO')) "
                + "CHECK(che_do!='AUTO_MAIN' OR diem_id IS NULL) "
                + "CHECK(che_do!='AUTO_SUB' OR (diem_id IS NOT NULL AND diem_id>0)), thoi_gian INTEGER NOT NULL CHECK(thoi_gian>0), "
                + "ma_luot TEXT NOT NULL, UNIQUE(tai_khoan_id,ma_luot), "
                + "FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id), "
                + "FOREIGN KEY(dia_diem_id) REFERENCES dia_diem(id))");
        db.execSQL("CREATE INDEX IF NOT EXISTS lich_su_theo_tai_khoan ON lich_su_nghe(tai_khoan_id,thoi_gian DESC,id DESC)");
    }

    /** SQLiteOpenHelper gọi trong giao dịch nâng cấp: giữ ID, thời gian và mã chống trùng. */
    static void nangCapTu13(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE lich_su_nghe RENAME TO lich_su_nghe_v13");
        taoBang(db);
        db.execSQL("INSERT INTO lich_su_nghe(id,tai_khoan_id,dia_diem_id,diem_id,ten_dia_diem,ten_diem,che_do,thoi_gian,ma_luot) "
                + "SELECT id,tai_khoan_id,dia_diem_id,NULLIF(diem_id,0),ten_dia_diem,ten_diem,"
                + "CASE che_do WHEN 'THU_CONG' THEN 'MANUAL' WHEN 'AUTO_GPS' THEN "
                + "CASE WHEN diem_id IS NULL OR diem_id=0 THEN 'AUTO_MAIN' ELSE 'AUTO_SUB' END "
                + "ELSE che_do END,thoi_gian,ma_luot FROM lich_su_nghe_v13");
        db.execSQL("DROP TABLE lich_su_nghe_v13");
        // Chỉ mục cũ được đổi bảng cùng dữ liệu và bị xóa ở bước trên.
        taoBang(db);
    }

    private static String chuanHoaCheDo(String cheDo, int diem) {
        if ("THU_CONG".equals(cheDo)) return MANUAL;
        if (AUTO_GPS.equals(cheDo)) return diem == 0 ? AUTO_MAIN : AUTO_SUB;
        return cheDo;
    }

    static boolean ghi(SQLiteDatabase db, long taiKhoan, int diaDiem, int diem,
            String cheDo, long thoiGian, String maLuot) {
        cheDo = chuanHoaCheDo(cheDo, diem);
        if (taiKhoan <= 0 || thoiGian <= 0 || maLuot == null || maLuot.trim().isEmpty()
                || !(MANUAL.equals(cheDo) || AUTO_MAIN.equals(cheDo) || AUTO_SUB.equals(cheDo) || DEMO.equals(cheDo))
                || (AUTO_MAIN.equals(cheDo) && diem != 0) || (AUTO_SUB.equals(cheDo) && diem <= 0)) return false;
        DuLieuNoiDungThamQuan.Bai noi, bai;
        try { noi = KhoNoiDungThamQuan.lay(diaDiem, 0); bai = KhoNoiDungThamQuan.lay(diaDiem, diem); }
        catch (IllegalArgumentException e) { return false; }
        try (Cursor c = db.rawQuery("SELECT 1 FROM tai_khoan t JOIN dia_diem d ON d.id=? WHERE t.id=?",
                new String[]{String.valueOf(diaDiem), String.valueOf(taiKhoan)})) {
            if (!c.moveToFirst()) return false;
        }
        ContentValues v = new ContentValues();
        v.put("tai_khoan_id", taiKhoan); v.put("dia_diem_id", diaDiem);
        if (diem == 0) v.putNull("diem_id"); else v.put("diem_id", diem);
        v.put("ten_dia_diem", noi.ten); v.put("ten_diem", diem == 0 ? "Giới thiệu tổng quan" : bai.ten);
        v.put("che_do", cheDo); v.put("thoi_gian", thoiGian); v.put("ma_luot", maLuot);
        return db.insertWithOnConflict("lich_su_nghe", null, v, SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    static final class Luot {
        final long id, thoiGian;
        final int diaDiem, diem;
        final String tenDiaDiem, tenDiem, cheDo;
        Luot(Cursor c) {
            id=c.getLong(0); diaDiem=c.getInt(1); diem=c.isNull(2)?0:c.getInt(2);
            tenDiaDiem=c.getString(3); tenDiem=c.getString(4); cheDo=c.getString(5); thoiGian=c.getLong(6);
        }
    }

    static ArrayList<Luot> doc(SQLiteDatabase db, long taiKhoan) {
        ArrayList<Luot> ds = new ArrayList<>();
        if (taiKhoan <= 0) return ds;
        try (Cursor c = db.rawQuery("SELECT id,dia_diem_id,diem_id,ten_dia_diem,ten_diem,che_do,thoi_gian "
                + "FROM lich_su_nghe WHERE tai_khoan_id=? ORDER BY thoi_gian DESC,id DESC",
                new String[]{String.valueOf(taiKhoan)})) {
            while (c.moveToNext()) ds.add(new Luot(c));
        }
        return ds;
    }

    static void xoa(SQLiteDatabase db, long taiKhoan, Long id) {
        if (taiKhoan <= 0) return;
        db.delete("lich_su_nghe", "tai_khoan_id=?" + (id == null ? "" : " AND id=?"),
                id == null ? new String[]{String.valueOf(taiKhoan)}
                        : new String[]{String.valueOf(taiKhoan), String.valueOf(id)});
    }

    static String nhanCheDo(String cheDo) {
        return AUTO_MAIN.equals(cheDo) ? "Tự động · Địa điểm chính"
                : AUTO_SUB.equals(cheDo) ? "Tự động · Điểm thành phần"
                : DEMO.equals(cheDo) ? "Demo · Mô phỏng" : "Nghe thủ công";
    }
}
