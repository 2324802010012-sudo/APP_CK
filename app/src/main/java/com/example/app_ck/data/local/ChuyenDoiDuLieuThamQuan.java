package com.example.app_ck.data.local;

import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.seed.CauHinhAutoConDao;
import com.example.app_ck.data.seed.DuLieuDinhDocLap;
import com.example.app_ck.data.seed.DuLieuNoiDungThamQuan;
import com.example.app_ck.data.seed.DuLieuSoDoThamQuan;
import com.example.app_ck.location.CauHinhAutoDiSan;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Chuyển bảng tour cũ sang khóa (địa điểm, điểm). Không xóa bảng gốc để đối chiếu. */
public final class ChuyenDoiDuLieuThamQuan {
    private ChuyenDoiDuLieuThamQuan() {}

    public static void thucHien(SQLiteDatabase db) {
        Set<String> diemDaCo=khoaDaCo(db,KhoDuLieuThamQuan.BANG_DIEM,"id");
        Set<String> soDoDaCo=khoaDaCo(db,KhoDuLieuThamQuan.BANG_SO_DO,"tang");
        KhoDuLieuThamQuan.taoBang(db);
        for (BaiThuyetMinh noi : DuLieuNoiDungThamQuan.DIA_DIEM) {
            int place = noi.id;
            if (place == 0) for (int tang=1; tang<=6; tang++) soDo(db, place, tang,
                    DuLieuDinhDocLap.layTenTang(tang), DuLieuDinhDocLap.layAnhSoDo(tang), false);
            else if (place == 1) for (int tang=0; tang<3; tang++) soDo(db, place, tang,
                    tang == 0 ? "Trệt / Ngoài trời" : "Lầu " + tang, "so_do_bao_tang_" + tang, false);
            else soDo(db, place, 0, noi.ten, place==2 ? "so_do_dai_noi_hue" : place==3
                    ? "so_do_nha_tu_con_dao" : "so_do_van_mieu_chinh_thuc", false);
            List<String> ten = new ArrayList<>();
            for (BaiThuyetMinh bai : noi.cacDiem) ten.add(bai.ten);
            CauHinhAutoDiSan cauHinh = place==2 ? CauHinhAutoDiSan.hue(ten)
                    : place==4 ? CauHinhAutoDiSan.vanMieu(ten) : null;
            for (BaiThuyetMinh bai : noi.cacDiem) {
                ContentValues v = new ContentValues();
                v.put("dia_diem_id", place); v.put("id", bai.id); v.put("ten", bai.ten);
                v.put("mo_ta", bai.noiDung()); v.put("noi_dung_thuyet_minh", bai.noiDung());
                v.put("thu_tu", bai.id); v.put("so_hien_thi", bai.id); v.put("tang", 0);
                if (place==0) {
                    DuLieuDinhDocLap.DiemThamQuan d = DuLieuDinhDocLap.timTheoId(bai.id);
                    v.put("tang", d.tang); v.put("x_ty_le", d.x); v.put("y_ty_le", d.y); v.put("ten_anh", d.hinhAnh);
                } else if (place==1) {
                    float[] d = DuLieuSoDoThamQuan.BAO_TANG[bai.id-1];
                    v.put("tang", (int)d[0]); v.put("x_ty_le", d[1]); v.put("y_ty_le", d[2]);
                } else if (place==2 || place==4) {
                    float[] d = (place==2 ? DuLieuSoDoThamQuan.HUE : DuLieuSoDoThamQuan.VAN_MIEU)[bai.id-1];
                    v.put("x_ty_le", d[0]); v.put("y_ty_le", d[1]);
                    CauHinhAutoDiSan.Diem gps = cauHinh.cacDiem.get(bai.id-1);
                    gps(v, gps.choPhepAuto, gps.lat, gps.lon, gps.banKinh, gps.nguon);
                } else {
                    CauHinhAutoConDao.Diem d = CauHinhAutoConDao.tim(bai.id);
                    v.put("so_hien_thi", d.soSoDo);
                    if (d.coMarker()) { v.put("x_ty_le", d.xAnh); v.put("y_ty_le", d.yAnh); }
                    gps(v, d.choPhepAuto, d.lat, d.lon, d.banKinh, d.nguon);
                }
                db.insertWithOnConflict(KhoDuLieuThamQuan.BANG_DIEM, null, v, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
        nhapSoDoCu(db,soDoDaCo);
        nhapDiemCu(db, 0, "diem_tham_quan", "mo_ta",diemDaCo);
        nhapThuyetMinhCu(db,diemDaCo);
        nhapDiemCu(db, 1, "bao_tang_marker", "noi_dung",diemDaCo);
        nhapDiemCu(db, 2, "dai_noi_hue_diem", "mo_ta",diemDaCo);
        nhapDiemCu(db, 3, "con_dao_diem", "mo_ta",diemDaCo);
        nhapDiemCu(db, 4, "van_mieu_diem", "mo_ta",diemDaCo);
        nhapToaDo(db, 2, "dai_noi_hue_toa_do",diemDaCo);
        nhapToaDo(db, 3, "con_dao_toa_do",diemDaCo);
        nhapToaDo(db, 4, "van_mieu_toa_do",diemDaCo);
        nhapGps(db,diemDaCo);
        try (Cursor c = db.rawQuery("SELECT dia_diem_id,ten_anh FROM " + KhoDuLieuThamQuan.BANG_SO_DO
                + " ORDER BY dia_diem_id,tang", null)) {
            while (c.moveToNext()) {
                ContentValues v = new ContentValues(); v.put("so_do_mac_dinh", c.getString(1));
                db.update("dia_diem", v, "id=? AND so_do_mac_dinh=''", new String[]{c.getString(0)});
            }
        }
    }

    private static Set<String> khoaDaCo(SQLiteDatabase db,String bang,String cot) {
        Set<String> ds=new HashSet<>();
        if (!KhoDuLieuThamQuan.coBang(db,bang)) return ds;
        try(Cursor c=db.rawQuery("SELECT dia_diem_id,"+cot+" FROM "+bang,null)) {
            while(c.moveToNext()) ds.add(c.getInt(0)+"/"+c.getInt(1));
        }
        return ds;
    }

    private static boolean co(SQLiteDatabase db, String bang, String cot) {
        return KhoDuLieuThamQuan.coBang(db, bang) && KhoDuLieuThamQuan.coCot(db, bang, cot);
    }
    private static String chu(Cursor c, String cot) {
        int i=c.getColumnIndex(cot); return i<0 || c.isNull(i) ? "" : c.getString(i);
    }
    private static int so(Cursor c, String cot, int macDinh) {
        int i=c.getColumnIndex(cot); return i<0 || c.isNull(i) ? macDinh : c.getInt(i);
    }
    private static void soDo(SQLiteDatabase db, int place, int tang, String ten, String anh, boolean capNhat) {
        ContentValues v=new ContentValues(); v.put("dia_diem_id",place); v.put("tang",tang);
        v.put("ten",ten); v.put("ten_anh",anh);
        if (capNhat && db.update(KhoDuLieuThamQuan.BANG_SO_DO,v,"dia_diem_id=? AND tang=?",
                new String[]{""+place,""+tang})==1) return;
        db.insertWithOnConflict(KhoDuLieuThamQuan.BANG_SO_DO,null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }
    private static void nhapSoDoCu(SQLiteDatabase db,Set<String> daCo) {
        if (!co(db,"khu_vuc","dia_diem_id")) return;
        try (Cursor c=db.query("khu_vuc",null,null,null,null,null,null)) {
            while (c.moveToNext()) {
                int place=so(c,"dia_diem_id",0), tang=so(c,"tang",so(c,"id",0));
                if (!KhoDuLieuThamQuan.coDiaDiem(db,place) || daCo.contains(place+"/"+tang)) continue;
                soDo(db,place,tang,chu(c,"ten"),chu(c,"ten_anh_so_do"),true);
            }
        }
    }
    private static int[] khuVuc(SQLiteDatabase db, int id) {
        try (Cursor c=db.rawQuery("SELECT dia_diem_id,COALESCE(tang,id) FROM khu_vuc WHERE id=?",new String[]{""+id})) {
            return c.moveToFirst() ? new int[]{c.getInt(0),c.getInt(1)} : new int[]{0,id};
        }
    }
    private static void nhapDiemCu(SQLiteDatabase db, int placeMacDinh, String bang, String bai,Set<String> daCo) {
        if (!co(db,bang,"id")) return;
        try (Cursor c=db.query(bang,null,null,null,null,null,null)) {
            while (c.moveToNext()) {
                int id=so(c,"id",0), place=placeMacDinh, tang=so(c,"tang",0);
                if (placeMacDinh==0) { int[] kv=khuVuc(db,so(c,"khu_vuc_id",1)); place=kv[0]; tang=kv[1]; }
                if (id<=0 || !KhoDuLieuThamQuan.coDiaDiem(db,place) || daCo.contains(place+"/"+id)) continue;
                DiemThamQuan cu=KhoDuLieuThamQuan.lay(db,place,id);
                ContentValues v=new ContentValues();
                v.put("dia_diem_id",place); v.put("id",id); v.put("tang",tang);
                String ten=chu(c,"ten");
                v.put("ten",ten.trim().isEmpty() ? cu==null ? "Điểm " + id : cu.ten : ten);
                v.put("thu_tu",cu==null ? id : cu.thuTu); v.put("so_hien_thi",cu==null ? id : cu.soHienThi);
                if (c.getColumnIndex(bai)>=0 && !c.isNull(c.getColumnIndex(bai))) {
                    v.put("mo_ta",chu(c,bai));
                    if (placeMacDinh!=0) v.put("noi_dung_thuyet_minh",chu(c,bai));
                }
                if (c.getColumnIndex("ten_anh")>=0) v.put("ten_anh",chu(c,"ten_anh"));
                marker(c,v,placeMacDinh==0 ? "toa_do_x" : "x",placeMacDinh==0 ? "toa_do_y" : "y");
                soDo(db,place,tang,"Khu vực " + tang,"",false);
                if (cu==null) {
                    if (db.insert(KhoDuLieuThamQuan.BANG_DIEM,null,v)==-1)
                        throw new IllegalStateException("Không nhập được điểm " + place + "/" + id);
                } else db.update(KhoDuLieuThamQuan.BANG_DIEM,v,"dia_diem_id=? AND id=?",new String[]{""+place,""+id});
                if (KhoDuLieuThamQuan.lay(db,place,id)==null)
                    throw new IllegalStateException("Thiếu điểm sau chuyển đổi " + place + "/" + id);
            }
        }
    }
    private static void marker(Cursor c, ContentValues v, String cotX, String cotY) {
        int a=c.getColumnIndex(cotX), b=c.getColumnIndex(cotY);
        if (a<0 || b<0 || c.isNull(a) || c.isNull(b)) return;
        double x=c.getDouble(a), y=c.getDouble(b);
        if (Double.isFinite(x) && Double.isFinite(y) && x>=0 && x<=1 && y>=0 && y<=1) {
            v.put("x_ty_le",x); v.put("y_ty_le",y);
        }
    }
    private static void nhapThuyetMinhCu(SQLiteDatabase db,Set<String> daCo) {
        if (!co(db,"thuyet_minh","noi_dung")) return;
        try (Cursor c=db.rawQuery("SELECT t.diem_tham_quan_id,t.noi_dung,COALESCE(k.dia_diem_id,0) "
                + "FROM thuyet_minh t LEFT JOIN diem_tham_quan p ON p.id=t.diem_tham_quan_id "
                + "LEFT JOIN khu_vuc k ON k.id=p.khu_vuc_id",null)) {
            while (c.moveToNext()) {
                if(daCo.contains(c.getInt(2)+"/"+c.getInt(0))) continue;
                ContentValues v=new ContentValues(); v.put("noi_dung_thuyet_minh",c.isNull(1) ? "" : c.getString(1));
                db.update(KhoDuLieuThamQuan.BANG_DIEM,v,"dia_diem_id=? AND id=?",new String[]{c.getString(2),c.getString(0)});
            }
        }
    }
    private static void nhapToaDo(SQLiteDatabase db, int place, String bang,Set<String> daCo) {
        if (!co(db,bang,"x_ty_le") || !co(db,bang,"y_ty_le")) return;
        try (Cursor c=db.query(bang,null,null,null,null,null,null)) {
            while (c.moveToNext()) {
                if(daCo.contains(place+"/"+so(c,"id",0))) continue;
                ContentValues v=new ContentValues(); marker(c,v,"x_ty_le","y_ty_le");
                if (v.size()>0) db.update(KhoDuLieuThamQuan.BANG_DIEM,v,"dia_diem_id=? AND id=?",
                        new String[]{""+place,chu(c,"id")});
            }
        }
    }
    private static void gps(ContentValues v, boolean auto, double lat, double lon, float r, String nguon) {
        boolean hopLe=Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat)<=90 && Math.abs(lon)<=180
                && Float.isFinite(r) && r>0;
        v.put("cho_phep_auto",auto && hopLe ? 1 : 0); v.put("nguon_gps",nguon);
        if (Double.isFinite(lat)) v.put("vi_do",lat);
        if (Double.isFinite(lon)) v.put("kinh_do",lon);
        if (Float.isFinite(r) && r>0) v.put("ban_kinh",r);
    }
    private static void nhapGps(SQLiteDatabase db,Set<String> daCo) {
        if (!co(db,"gps_diem_tham_quan","marker_id")) return;
        try (Cursor c=db.query("gps_diem_tham_quan",null,null,null,null,null,null)) {
            while (c.moveToNext()) {
                if(daCo.contains(so(c,"dia_diem_id",0)+"/"+so(c,"marker_id",0))) continue;
                ContentValues v=new ContentValues();
                gps(v,true,c.getDouble(c.getColumnIndexOrThrow("vi_do")),c.getDouble(c.getColumnIndexOrThrow("kinh_do")),
                        c.getFloat(c.getColumnIndexOrThrow("ban_kinh")),"Khảo sát GPS");
                if (v.getAsInteger("cho_phep_auto")!=1) continue;
                db.update(KhoDuLieuThamQuan.BANG_DIEM,v,"dia_diem_id=? AND id=?",
                        new String[]{chu(c,"dia_diem_id"),chu(c,"marker_id")});
            }
        }
    }
}
