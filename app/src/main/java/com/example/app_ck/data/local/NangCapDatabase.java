package com.example.app_ck.data.local;

import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoNguonThongTin;
import com.example.app_ck.data.repository.KhoToaDoThamQuan;
import com.example.app_ck.data.seed.KhoiTaoDuLieu;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** SQLiteOpenHelper bọc cả onCreate/onUpgrade trong transaction; lỗi kiểm chứng sẽ rollback. */
public final class NangCapDatabase {
    private NangCapDatabase() {}

    public static void thucHien(SQLiteDatabase db, int phienBanCu) {
        KhoiTaoDuLieu.taoBangDuLich(db);
        themCot(db, "tai_khoan", "ma_khoi_phuc_bam", "TEXT");
        themCot(db, "tai_khoan", "ho_ten", "TEXT NOT NULL DEFAULT ''");
        themCot(db, "tai_khoan", "email", "TEXT NOT NULL DEFAULT ''");
        themCot(db, "tai_khoan", "vai_tro", "TEXT NOT NULL DEFAULT 'USER' CHECK(vai_tro IN ('USER','ADMIN'))");
        themCot(db, "tai_khoan", "trang_thai", "TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(trang_thai IN ('ACTIVE','LOCKED'))");
        themCot(db, "tai_khoan", "ngay_tao", "INTEGER NOT NULL DEFAULT 0");
        // Không biết ngày đăng ký của dữ liệu cũ: 0 có nghĩa là chưa xác định, không bịa ngày.
        themCot(db, "dia_diem", "danh_muc", "TEXT");
        KhoiTaoDuLieu.themDuLieuDiaDiem(db);
        KhoiTaoDuLieu.themDuLieuSoDoDinhDocLap(db);
        KhoiTaoDuLieu.themThuyetMinhDinhDocLap(db);
        if (!KhoDuLieuThamQuan.coBang(db, "gps_diem_tham_quan")) KhoToaDoThamQuan.nangCap(db);
        if (phienBanCu == 13) KhoLichSu.nangCapTu13(db);
        else KhoLichSu.taoBang(db);
        KhoDuLieuThamQuan.nangCap(db);
        KhoNguonThongTin.khoiTao(db);
        kiemTraLienKet(db);
    }

    public static void themCot(SQLiteDatabase db, String bang, String cot, String khaiBao) {
        if (!KhoDuLieuThamQuan.coCot(db, bang, cot))
            db.execSQL("ALTER TABLE " + bang + " ADD COLUMN " + cot + " " + khaiBao);
    }

    private static void kiemTraLienKet(SQLiteDatabase db) {
        // Bảng cũ được giữ nguyên để đối chiếu. Chỉ thêm ràng buộc ở dữ liệu chung đã kiểm chứng;
        // không bật foreign_keys toàn DB trước khi xử lý các tham chiếu lịch sử có thể mồ côi.
        try (Cursor c = db.rawQuery("SELECT p.dia_diem_id,p.id FROM " + KhoDuLieuThamQuan.BANG_DIEM + " p "
                + "LEFT JOIN dia_diem d ON d.id=p.dia_diem_id LEFT JOIN " + KhoDuLieuThamQuan.BANG_SO_DO
                + " s ON s.dia_diem_id=p.dia_diem_id AND s.tang=p.tang "
                + "WHERE d.id IS NULL OR s.dia_diem_id IS NULL LIMIT 1", null)) {
            if (c.moveToFirst()) throw new IllegalStateException("Liên kết điểm/sơ đồ không hợp lệ: "
                    + c.getInt(0) + "/" + c.getInt(1));
        }
        try (Cursor c = db.rawQuery("PRAGMA quick_check", null)) {
            if (!c.moveToFirst() || !"ok".equals(c.getString(0)))
                throw new IllegalStateException("Kiểm tra toàn vẹn database không đạt");
        }
    }
}
