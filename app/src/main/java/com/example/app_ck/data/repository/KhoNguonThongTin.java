package com.example.app_ck.data.repository;

import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.seed.DuLieuNoiDungThamQuan;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/** Trích dẫn tách khỏi nội dung đọc; Java chỉ cung cấp dữ liệu khởi tạo. */
public final class KhoNguonThongTin {
    private KhoNguonThongTin() {}

    public static final class Nguon {
        public final String ten, url, coQuan;
        public Nguon(String ten, String url, String coQuan) {
            this.ten = ten; this.url = url; this.coQuan = coQuan;
        }
    }

    public static void khoiTao(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS nguon_thong_tin(ma TEXT PRIMARY KEY,ten TEXT NOT NULL,url TEXT NOT NULL,co_quan TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS bai_nguon(dia_diem_id INTEGER NOT NULL,diem_id INTEGER NOT NULL,ma_nguon TEXT NOT NULL,thu_tu INTEGER NOT NULL,PRIMARY KEY(dia_diem_id,diem_id,ma_nguon),FOREIGN KEY(dia_diem_id) REFERENCES dia_diem(id),FOREIGN KEY(ma_nguon) REFERENCES nguon_thong_tin(ma))");
        db.execSQL("CREATE TABLE IF NOT EXISTS ghi_chu_nguon(dia_diem_id INTEGER NOT NULL,diem_id INTEGER NOT NULL,ghi_chu TEXT NOT NULL,ngay_doi_chieu TEXT NOT NULL,PRIMARY KEY(dia_diem_id,diem_id))");
        for (java.util.Map.Entry<String, DuLieuNoiDungThamQuan.Nguon> e : DuLieuNoiDungThamQuan.NGUON.entrySet()) {
            ContentValues v = new ContentValues();
            v.put("ma", e.getKey()); v.put("ten", e.getValue().ten);
            v.put("url", e.getValue().url); v.put("co_quan", e.getValue().coQuan);
            db.insertWithOnConflict("nguon_thong_tin", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        }
        for (BaiThuyetMinh place : DuLieuNoiDungThamQuan.DIA_DIEM) {
            themBai(db, place.id, 0, place);
            for (BaiThuyetMinh point : place.cacDiem) themBai(db, place.id, point.id, point);
        }
    }

    private static void themBai(SQLiteDatabase db, int place, int point, BaiThuyetMinh bai) {
        int order = 0;
        for (String source : bai.nguon) {
            ContentValues v = new ContentValues();
            v.put("dia_diem_id", place); v.put("diem_id", point);
            v.put("ma_nguon", source); v.put("thu_tu", order++);
            db.insertWithOnConflict("bai_nguon", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        }
        ContentValues v = new ContentValues();
        v.put("dia_diem_id", place); v.put("diem_id", point);
        v.put("ghi_chu", bai.ghiChu); v.put("ngay_doi_chieu", DuLieuNoiDungThamQuan.NGAY_DOI_CHIEU);
        db.insertWithOnConflict("ghi_chu_nguon", null, v, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static List<Nguon> danhSach(SQLiteDatabase db, int place, int point) {
        List<Nguon> result = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT n.ten,n.url,n.co_quan FROM bai_nguon b JOIN nguon_thong_tin n ON n.ma=b.ma_nguon WHERE b.dia_diem_id=? AND b.diem_id=? ORDER BY b.thu_tu", new String[]{""+place,""+point})) {
            while (c.moveToNext()) result.add(new Nguon(c.getString(0), c.getString(1), c.getString(2)));
        }
        return result;
    }

    public static String ghiChu(SQLiteDatabase db, int place, int point) {
        try (Cursor c = db.rawQuery("SELECT ngay_doi_chieu,ghi_chu FROM ghi_chu_nguon WHERE dia_diem_id=? AND diem_id=?", new String[]{""+place,""+point})) {
            return c.moveToFirst() ? "Đối chiếu: " + c.getString(0) + "\n" + c.getString(1)
                    : "Chưa có trích dẫn cho nội dung này.";
        }
    }
}
