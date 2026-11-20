package com.example.app_ck.data.repository;

import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.model.DiaDiem;

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
        DiaDiem diaDiemChinh = KhoDiaDiem.lay(db, diaDiem);
        if (diaDiemChinh == null) throw new IllegalArgumentException("Không có địa điểm: " + diaDiem);
        ArrayList<BaiThuyetMinh> cacDiem = new ArrayList<>();
        for (DiemThamQuan d : KhoDuLieuThamQuan.danhSach(db,diaDiem))
            cacDiem.add(BaiThuyetMinh.tuSqlite(d.id,d.ten,d.noiDung,new String[0]));
        return BaiThuyetMinh.tuSqlite(diaDiem,diaDiemChinh.ten,
                diaDiemChinh.moTa == null ? "" : diaDiemChinh.moTa,
                new String[0],cacDiem.toArray(new BaiThuyetMinh[0]));
    }
    public static String bai(BaiThuyetMinh d) { return d.noiDung(); }

}
