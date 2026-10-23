package com.example.app_ck;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/** GPS WGS84 của địa điểm; tuyệt đối không suy ra GPS từ tọa độ pixel trên ảnh. */
public final class KhoToaDoThamQuan {
    private KhoToaDoThamQuan() {}
    public static final double[][] TOA_DO = {
            {10.77702, 106.69540}, {10.779475, 106.692132},
            {16.4689726, 107.5781266}, {8.68907, 106.61484}, {21.02861, 105.83556}
    };

    static void nangCap(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS gps_diem_tham_quan ("
                + "dia_diem_id INTEGER NOT NULL, marker_id INTEGER NOT NULL, tang INTEGER NOT NULL,"
                + "vi_do REAL NOT NULL CHECK(vi_do BETWEEN -90 AND 90),"
                + "kinh_do REAL NOT NULL CHECK(kinh_do BETWEEN -180 AND 180),"
                + "ban_kinh REAL NOT NULL CHECK(ban_kinh BETWEEN 15 AND 100),"
                + "do_chinh_xac REAL NOT NULL, ngay_cap_nhat INTEGER NOT NULL,"
                + "PRIMARY KEY(dia_diem_id,marker_id))");
        for (int id = 0; id < TOA_DO.length; id++) {
            ContentValues values = new ContentValues();
            values.put("vi_do", TOA_DO[id][0]);
            values.put("kinh_do", TOA_DO[id][1]);
            db.update("dia_diem", values, "id=?", new String[]{String.valueOf(id)});
        }
    }

    public static List<BoChonDiemTuDong.Moc> doc(SQLiteDatabase db, int diaDiem) {
        List<BoChonDiemTuDong.Moc> ds = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT marker_id,tang,vi_do,kinh_do,ban_kinh "
                + "FROM gps_diem_tham_quan WHERE dia_diem_id=? ORDER BY marker_id",
                new String[]{String.valueOf(diaDiem)})) {
            while (c.moveToNext()) ds.add(new BoChonDiemTuDong.Moc(c.getInt(0), c.getInt(1),
                    c.getDouble(2), c.getDouble(3), c.getFloat(4)));
        }
        return ds;
    }

    public static void luu(SQLiteDatabase db, int diaDiem, int marker, int tang,
                           double lat, double lon, float accuracy, float radius) {
        if (diaDiem < 0 || diaDiem > 4 || marker < 1 || !Double.isFinite(lat)
                || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180
                || !Float.isFinite(accuracy) || accuracy < 0 || accuracy > 25
                || !Float.isFinite(radius) || radius < 15 || radius > 100) {
            throw new IllegalArgumentException("Tọa độ hoặc độ chính xác không hợp lệ");
        }
        ContentValues v = new ContentValues();
        v.put("dia_diem_id", diaDiem); v.put("marker_id", marker); v.put("tang", tang);
        v.put("vi_do", lat); v.put("kinh_do", lon); v.put("ban_kinh", radius);
        v.put("do_chinh_xac", accuracy); v.put("ngay_cap_nhat", System.currentTimeMillis());
        db.insertWithOnConflict("gps_diem_tham_quan", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
