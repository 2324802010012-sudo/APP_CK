package com.example.app_ck.data.repository;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/** Yêu thích theo ID tài khoản và địa điểm, không dùng vị trí danh sách. */
public final class KhoYeuThich {
    private final AppDatabase helper;
    private final android.content.Context context;
    public KhoYeuThich(android.content.Context context,AppDatabase helper) { this.context=context;this.helper=helper; }

    public boolean daYeuThich(long id, int diaDiem) {
        if(!PhienDangNhap.coQuyenHoSo(context,helper.getReadableDatabase(),id,false)) return false;
        try (Cursor c = helper.getReadableDatabase().rawQuery("SELECT 1 FROM yeu_thich WHERE tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)})) { return c.moveToFirst(); }
    }

    public boolean datYeuThich(long id, int diaDiem, boolean thich) {
        SQLiteDatabase db=helper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (!PhienDangNhap.coQuyenHoSo(context,db,id,false) || !KhoDuLieuThamQuan.coDiaDiem(db,diaDiem)) return false;
            if(thich) {
                ContentValues cv=new ContentValues();cv.put("tai_khoan_id",id);cv.put("dia_diem_id",diaDiem);
                db.insertWithOnConflict("yeu_thich",null,cv,SQLiteDatabase.CONFLICT_IGNORE);
                if(!daYeuThich(id,diaDiem)) return false;
            } else db.delete("yeu_thich","tai_khoan_id=? AND dia_diem_id=?",new String[]{""+id,""+diaDiem});
            db.setTransactionSuccessful();return true;
        } finally {db.endTransaction();}
    }

    public ArrayList<Integer> danhSachYeuThich(long id) {
        ArrayList<Integer> ds = new ArrayList<>();
        if(!PhienDangNhap.coQuyenHoSo(context,helper.getReadableDatabase(),id,false)) return ds;
        try (Cursor c = helper.getReadableDatabase().query("yeu_thich",new String[]{"dia_diem_id"},"tai_khoan_id=?",new String[]{String.valueOf(id)},null,null,"dia_diem_id ASC")) { while(c.moveToNext()) ds.add(c.getInt(0)); }
        return ds;
    }
}
