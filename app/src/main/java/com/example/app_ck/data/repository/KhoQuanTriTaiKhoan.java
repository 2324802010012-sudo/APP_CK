package com.example.app_ck.data.repository;

import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.session.PhienDangNhap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/** API quản trị trả hồ sơ đã lọc, không bao giờ trả salt, hash hay mã khôi phục. */
public final class KhoQuanTriTaiKhoan {
    private static final String[] COT = {"id","ten_dang_nhap","ho_ten","email","vai_tro","trang_thai","ngay_tao"};
    private KhoQuanTriTaiKhoan() {}

    public static ArrayList<HoSoTaiKhoan> danhSach(Context context,SQLiteDatabase db,String tuKhoa) {
        PhienDangNhap.yeuCauQuanTri(context,db);
        String key=tuKhoa==null ? "" : tuKhoa.trim();
        key="%"+key.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
        ArrayList<HoSoTaiKhoan> ds=new ArrayList<>();
        try (Cursor c=db.query("tai_khoan",COT,"ten_dang_nhap LIKE ? ESCAPE '\\' OR ho_ten LIKE ? ESCAPE '\\' OR email LIKE ? ESCAPE '\\'",
                new String[]{key,key,key},null,null,"ten_dang_nhap,id")) {
            while(c.moveToNext()) ds.add(hoSo(c));
        }
        return ds;
    }
    public static HoSoTaiKhoan chiTiet(Context context,SQLiteDatabase db,long id) {
        PhienDangNhap.yeuCauQuanTri(context,db);
        try (Cursor c=db.query("tai_khoan",COT,"id=?",new String[]{""+id},null,null,null)) {
            return c.moveToFirst() ? hoSo(c) : null;
        }
    }
    public static boolean datKhoa(Context context,SQLiteDatabase db,long id,boolean khoa) {
        db.beginTransaction();
        try {
            long nguoiThucHien=PhienDangNhap.yeuCauQuanTri(context,db);
            // Không khóa chính phiên đang quản trị: phải còn người có thể mở khóa tài khoản.
            if (id<=0 || (khoa && id==nguoiThucHien)) return false;
            ContentValues v=new ContentValues(); v.put("trang_thai",khoa ? HoSoTaiKhoan.LOCKED : HoSoTaiKhoan.ACTIVE);
            boolean ok=db.update("tai_khoan",v,"id=?",new String[]{""+id})==1;
            if(ok) db.setTransactionSuccessful();
            return ok;
        } finally { db.endTransaction(); }
    }
    private static HoSoTaiKhoan hoSo(Cursor c) {
        return new HoSoTaiKhoan(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getLong(6));
    }
}
