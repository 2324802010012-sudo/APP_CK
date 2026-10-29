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
    public KhoYeuThich(AppDatabase helper) { this.helper=helper; }

    public boolean daYeuThich(long id, int diaDiem) {
        try (Cursor c = helper.getReadableDatabase().rawQuery("SELECT 1 FROM yeu_thich WHERE tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)})) { return c.moveToFirst(); }
    }

    public void datYeuThich(long id, int diaDiem, boolean thich) {
        if (!PhienDangNhap.hoatDong(helper.getReadableDatabase(),id) || !KhoDuLieuThamQuan.coDiaDiem(helper.getReadableDatabase(),diaDiem)) return;
        if (thich) { ContentValues cv = new ContentValues(); cv.put("tai_khoan_id",id); cv.put("dia_diem_id",diaDiem); helper.getWritableDatabase().insertWithOnConflict("yeu_thich",null,cv,SQLiteDatabase.CONFLICT_IGNORE); }
        else helper.getWritableDatabase().delete("yeu_thich","tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)});
    }

    public ArrayList<Integer> danhSachYeuThich(long id) {
        ArrayList<Integer> ds = new ArrayList<>();
        try (Cursor c = helper.getReadableDatabase().query("yeu_thich",new String[]{"dia_diem_id"},"tai_khoan_id=?",new String[]{String.valueOf(id)},null,null,"dia_diem_id ASC")) { while(c.moveToNext()) ds.add(c.getInt(0)); }
        return ds;
    }
}
