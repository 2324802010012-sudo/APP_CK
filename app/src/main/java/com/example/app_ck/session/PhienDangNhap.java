package com.example.app_ck.session;

import com.example.app_ck.data.local.AppDatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/** Chỉ lưu ID phiên. Vai trò và trạng thái luôn được kiểm tra lại trong SQLite. */
public final class PhienDangNhap {
    private PhienDangNhap() {}
    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("phien_dang_nhap", Context.MODE_PRIVATE);
    }
    public static long id(Context c) {
        long id = p(c).getLong("id", -1);
        if (id <= 0) return -1;
        try (AppDatabase helper = new AppDatabase(c)) {
            if (hoatDong(helper.getReadableDatabase(), id)) return id;
        }
        dangXuat(c);
        return -1;
    }
    public static boolean hoatDong(SQLiteDatabase db, long id) {
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM tai_khoan WHERE id=? AND trang_thai='ACTIVE'",
                new String[]{String.valueOf(id)})) {
            return cursor.moveToFirst();
        }
    }
    public static boolean daDangNhap(Context c) { return id(c) > 0; }
    public static void luu(Context c, long id, String ten) {
        try (AppDatabase helper = new AppDatabase(c)) {
            if (!hoatDong(helper.getReadableDatabase(), id)) {
                dangXuat(c);
                return;
            }
        }
        p(c).edit().putLong("id", id).putString("ten", ten).apply();
    }
    public static String ten(Context c) { return id(c) > 0 ? p(c).getString("ten", "") : ""; }
    public static void dangXuat(Context c) { p(c).edit().clear().apply(); }

    public static boolean coQuyenHoSo(Context context, SQLiteDatabase db, long taiKhoan, boolean choPhepAdmin) {
        long phien = p(context).getLong("id", -1);
        try (Cursor c=db.rawQuery("SELECT 1 FROM tai_khoan WHERE id=? AND trang_thai='ACTIVE' "
                + "AND (id=? OR (?=1 AND vai_tro='ADMIN'))",new String[]{""+phien,""+taiKhoan,choPhepAdmin ? "1" : "0"})) {
            return taiKhoan>0 && c.moveToFirst();
        }
    }

    /** Kiểm tra tại thao tác ghi, dùng cùng connection/giao dịch với thao tác đó. */
    public static long yeuCauQuanTri(Context c, SQLiteDatabase db) {
        long id = p(c).getLong("id", -1);
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM tai_khoan WHERE id=? "
                + "AND vai_tro='ADMIN' AND trang_thai='ACTIVE'", new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) return id;
        }
        throw new SecurityException("Thao tác này yêu cầu tài khoản ADMIN đang hoạt động");
    }
    public static boolean laAdmin(Context context) {
        long taiKhoanId = id(context);

        if (taiKhoanId <= 0) {
            return false;
        }

        try (AppDatabase helper = new AppDatabase(context)) {
            yeuCauQuanTri(
                    context,
                    helper.getReadableDatabase()
            );
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
}
