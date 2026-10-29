package com.example.app_ck.data.repository;

import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.model.DiemThamQuan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/** Nội dung vận hành chỉ đọc SQLite. Seed Java chỉ được dùng khi tạo/chuyển đổi dữ liệu. */
public final class KhoNoiDungThamQuan {
    private KhoNoiDungThamQuan() {}

    public static BaiThuyetMinh lay(SQLiteDatabase db, int diaDiem, int diem) {
        if (diem != 0) {
            DiemThamQuan d = KhoDuLieuThamQuan.lay(db, diaDiem, diem);
            if (d == null) throw new IllegalArgumentException("Không có nội dung: " + diaDiem + "/" + diem);
            return BaiThuyetMinh.tuSqlite(d.id,d.ten,d.noiDung,new String[0]);
        }
        try (Cursor c = db.rawQuery("SELECT ten,COALESCE(mo_ta,'') FROM dia_diem WHERE id=?",
                new String[]{String.valueOf(diaDiem)})) {
            if (!c.moveToFirst()) throw new IllegalArgumentException("Không có địa điểm: " + diaDiem);
            ArrayList<BaiThuyetMinh> cacDiem = new ArrayList<>();
            for (DiemThamQuan d : KhoDuLieuThamQuan.danhSach(db,diaDiem))
                cacDiem.add(BaiThuyetMinh.tuSqlite(d.id,d.ten,d.noiDung,new String[0]));
            return BaiThuyetMinh.tuSqlite(diaDiem,c.getString(0),c.getString(1),
                    new String[0],cacDiem.toArray(new BaiThuyetMinh[0]));
        }
    }
    public static String bai(BaiThuyetMinh d) { return d.noiDung(); }

}
