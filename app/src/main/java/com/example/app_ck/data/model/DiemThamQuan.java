package com.example.app_ck.data.model;

import android.database.Cursor;

/** ID điểm chỉ duy nhất trong một địa điểm; x/y là tỷ lệ ảnh, GPS là WGS84 riêng. */
public final class DiemThamQuan {
    public final int diaDiemId, id, tang, thuTu, soHienThi;
    public final String ten, moTa, noiDung, tenAnh, nguonGps;
    public final double x, y, viDo, kinhDo;
    public final float banKinh;
    public final boolean choPhepAuto;

    public DiemThamQuan(Cursor c) {
        diaDiemId=c.getInt(c.getColumnIndexOrThrow("dia_diem_id"));
        id=c.getInt(c.getColumnIndexOrThrow("id"));
        tang=c.getInt(c.getColumnIndexOrThrow("tang"));
        thuTu=c.getInt(c.getColumnIndexOrThrow("thu_tu"));
        soHienThi=c.getInt(c.getColumnIndexOrThrow("so_hien_thi"));
        ten=c.getString(c.getColumnIndexOrThrow("ten"));
        moTa=c.getString(c.getColumnIndexOrThrow("mo_ta"));
        noiDung=c.getString(c.getColumnIndexOrThrow("noi_dung_thuyet_minh"));
        tenAnh=c.getString(c.getColumnIndexOrThrow("ten_anh"));
        nguonGps=c.getString(c.getColumnIndexOrThrow("nguon_gps"));
        x=so(c,"x_ty_le"); y=so(c,"y_ty_le");
        viDo=so(c,"vi_do"); kinhDo=so(c,"kinh_do");
        banKinh=(float)so(c,"ban_kinh");
        choPhepAuto=c.getInt(c.getColumnIndexOrThrow("cho_phep_auto"))==1;
    }
    private static double so(Cursor c,String ten) {
        int i=c.getColumnIndexOrThrow(ten); return c.isNull(i)?Double.NaN:c.getDouble(i);
    }
    public boolean coMarker() { return Double.isFinite(x)&&Double.isFinite(y)&&x>=0&&x<=1&&y>=0&&y<=1; }
}
