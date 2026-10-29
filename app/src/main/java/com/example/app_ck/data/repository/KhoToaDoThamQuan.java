package com.example.app_ck.data.repository;

import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.location.BoChonDiemTuDong;

import android.content.ContentValues;
import android.content.Context;
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

    public static void nangCap(SQLiteDatabase db) {
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
            db.update("dia_diem", values, "id=? AND (vi_do IS NULL OR kinh_do IS NULL)",
                    new String[]{String.valueOf(id)});
        }
    }

    public static List<BoChonDiemTuDong.Moc> doc(SQLiteDatabase db, int diaDiem) {
        List<BoChonDiemTuDong.Moc> ds = new ArrayList<>();
        for (DiemThamQuan d : KhoDuLieuThamQuan.danhSach(db, diaDiem)) {
            if (d.choPhepAuto && Double.isFinite(d.viDo) && Math.abs(d.viDo) <= 90
                    && Double.isFinite(d.kinhDo) && Math.abs(d.kinhDo) <= 180
                    && Float.isFinite(d.banKinh) && d.banKinh > 0) {
                ds.add(new BoChonDiemTuDong.Moc(d.id, d.tang, d.viDo, d.kinhDo, d.banKinh));
            }
        }
        return ds;
    }

    /** API cũ không có phiên đăng nhập không được thay cấu hình AUTO. */
    @Deprecated
    public static void luu(SQLiteDatabase db, int diaDiem, int marker, int tang,
                           double lat, double lon, float accuracy, float radius) {
        throw new SecurityException("Cần phiên ADMIN để lưu cấu hình GPS");
    }

    /** Lưu phép đo vào dữ liệu chung qua cùng điểm kiểm tra quyền của quản trị. */
    public static boolean luu(Context context, SQLiteDatabase db, int diaDiem, int marker, int tang,
                              double lat, double lon, float accuracy, float radius) {
        if (diaDiem < 0 || marker < 1 || !Double.isFinite(lat)
                || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180
                || !Float.isFinite(accuracy) || accuracy < 0 || accuracy > 25
                || !Float.isFinite(radius) || radius < 15 || radius > 100) {
            throw new IllegalArgumentException("Tọa độ hoặc độ chính xác không hợp lệ");
        }
        DiemThamQuan d = KhoDuLieuThamQuan.lay(db, diaDiem, marker);
        if (d == null || (tang >= 0 && tang != d.tang)) return false;
        return KhoDuLieuThamQuan.luuCauHinhAuto(context, db, diaDiem, marker, true,
                lat, lon, radius, "Khảo sát GPS, độ chính xác " + accuracy + " m");
    }
}
