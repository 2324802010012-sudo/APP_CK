package com.example.app_ck.data.repository;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.seed.DuLieuDinhDocLap;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/** Đọc danh sách địa điểm và các API tương thích tour từ SQLite chung. */
public final class KhoDiaDiem {
    private static final String[] CAC_COT = {"id", "ten", "dia_chi", "mo_ta",
            "vi_do", "kinh_do", "ten_anh", "danh_muc", "ban_kinh_auto"};
    private final AppDatabase helper;
    public KhoDiaDiem(AppDatabase helper) { this.helper=helper; }

    public ArrayList<DiaDiem> danhSachDiaDiem() {

        ArrayList<DiaDiem> danhSach =
                new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "dia_diem",
                CAC_COT,
                null,
                null,
                null,
                null,
                "id ASC"
        )) {

            while (cursor.moveToNext()) {

                danhSach.add(docDiaDiem(cursor));
            }
        }

        return danhSach;
    }

    public static DiaDiem lay(SQLiteDatabase db, int id) {
        try (Cursor cursor = db.query("dia_diem", CAC_COT, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? docDiaDiem(cursor) : null;
        }
    }

    private static DiaDiem docDiaDiem(Cursor cursor) {
        DiaDiem diaDiem = new DiaDiem(cursor.getInt(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getDouble(4),
                cursor.getDouble(5), cursor.getString(6));
        diaDiem.danhMuc = cursor.getString(7);
        diaDiem.banKinhAuto = cursor.getFloat(8);
        return diaDiem;
    }

    public ArrayList<DuLieuDinhDocLap.DiemThamQuan> layDiemDinhDocLap() {
        ArrayList<DuLieuDinhDocLap.DiemThamQuan> ds = new ArrayList<>();
        for (DiemThamQuan d : KhoDuLieuThamQuan.danhSach(helper.getReadableDatabase(), 0)) {
            DuLieuDinhDocLap.DiemThamQuan diem = new DuLieuDinhDocLap.DiemThamQuan(
                    d.id, d.ten, d.tang, (float)d.x, (float)d.y);
            diem.moTa = d.moTa; diem.hinhAnh = d.tenAnh;
            ds.add(diem);
        }
        return ds;
    }

    public String layThuyetMinhTheoId(int diemThamQuanId) {
        DiemThamQuan d = KhoDuLieuThamQuan.lay(helper.getReadableDatabase(), 0, diemThamQuanId);
        return d == null ? null : d.noiDung;
    }
}
