package com.example.app_ck;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Nguồn dữ liệu vận hành chung cho cả 5 tour; bảng tour cũ chỉ phục vụ chuyển đổi. */
public final class KhoDuLieuThamQuan {
    public static final String BANG_DIEM="diem_tham_quan_chung", BANG_SO_DO="so_do_tham_quan";
    private KhoDuLieuThamQuan() {}

    static boolean coBang(SQLiteDatabase db,String ten) {
        try(Cursor c=db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",new String[]{ten})) {
            return c.moveToFirst();
        }
    }
    static boolean coCot(SQLiteDatabase db,String bang,String cot) {
        try(Cursor c=db.rawQuery("PRAGMA table_info("+bang+")",null)) {
            while(c.moveToNext()) if(cot.equals(c.getString(c.getColumnIndexOrThrow("name")))) return true;
            return false;
        }
    }
    static void taoBang(SQLiteDatabase db) {
        if(!coCot(db,"dia_diem","ban_kinh_auto")) db.execSQL("ALTER TABLE dia_diem ADD COLUMN ban_kinh_auto REAL NOT NULL DEFAULT 100 CHECK(ban_kinh_auto>0)");
        if(!coCot(db,"dia_diem","so_do_mac_dinh")) db.execSQL("ALTER TABLE dia_diem ADD COLUMN so_do_mac_dinh TEXT NOT NULL DEFAULT ''");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+BANG_SO_DO+"(dia_diem_id INTEGER NOT NULL,tang INTEGER NOT NULL,"
                +"ten TEXT NOT NULL,ten_anh TEXT NOT NULL,PRIMARY KEY(dia_diem_id,tang),FOREIGN KEY(dia_diem_id) REFERENCES dia_diem(id))");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+BANG_DIEM+"(dia_diem_id INTEGER NOT NULL,id INTEGER NOT NULL CHECK(id>0),"
                +"ten TEXT NOT NULL CHECK(length(trim(ten))>0),mo_ta TEXT NOT NULL DEFAULT '',noi_dung_thuyet_minh TEXT NOT NULL DEFAULT '',"
                +"tang INTEGER NOT NULL DEFAULT 0,thu_tu INTEGER NOT NULL DEFAULT 0,ten_anh TEXT NOT NULL DEFAULT '',"
                +"x_ty_le REAL,y_ty_le REAL,so_hien_thi INTEGER NOT NULL DEFAULT 0,"
                +"cho_phep_auto INTEGER NOT NULL DEFAULT 0 CHECK(cho_phep_auto IN (0,1)),vi_do REAL,kinh_do REAL,ban_kinh REAL,nguon_gps TEXT NOT NULL DEFAULT '',"
                +"CHECK((x_ty_le IS NULL AND y_ty_le IS NULL) OR (x_ty_le IS NOT NULL AND y_ty_le IS NOT NULL AND x_ty_le BETWEEN 0 AND 1 AND y_ty_le BETWEEN 0 AND 1)),"
                +"CHECK(cho_phep_auto=0 OR (vi_do IS NOT NULL AND kinh_do IS NOT NULL AND ban_kinh IS NOT NULL AND vi_do BETWEEN -90 AND 90 AND kinh_do BETWEEN -180 AND 180 AND ban_kinh>0)),"
                +"PRIMARY KEY(dia_diem_id,id),FOREIGN KEY(dia_diem_id) REFERENCES dia_diem(id),"
                +"FOREIGN KEY(dia_diem_id,tang) REFERENCES "+BANG_SO_DO+"(dia_diem_id,tang))");
        db.execSQL("CREATE INDEX IF NOT EXISTS diem_theo_tour ON "+BANG_DIEM+"(dia_diem_id,thu_tu,id)");
    }

    /** Gọi một lần khi tạo DB/nâng lên v15, trong giao dịch của SQLiteOpenHelper. */
    static void nangCap(SQLiteDatabase db) {
        taoBang(db);
        for(DuLieuNoiDungThamQuan.Bai noi:DuLieuNoiDungThamQuan.DIA_DIEM) {
            int place=noi.id;
            if(place==0) for(int tang=1;tang<=6;tang++) themSoDo(db,place,tang,DuLieuDinhDocLap.layTenTang(tang),DuLieuDinhDocLap.layAnhSoDo(tang));
            else if(place==1) for(int tang=0;tang<3;tang++) themSoDo(db,place,tang,tang==0?"Trệt / Ngoài trời":"Lầu "+tang,"so_do_bao_tang_"+tang);
            else themSoDo(db,place,0,noi.ten,place==2?"so_do_dai_noi_hue":place==3?"so_do_nha_tu_con_dao":"so_do_van_mieu_chinh_thuc");
            List<String> ten=new ArrayList<>(); for(DuLieuNoiDungThamQuan.Bai b:noi.cacDiem)ten.add(b.ten);
            CauHinhAutoDiSan cauHinh=place==2?CauHinhAutoDiSan.hue(ten):place==4?CauHinhAutoDiSan.vanMieu(ten):null;
            for(DuLieuNoiDungThamQuan.Bai bai:noi.cacDiem) {
                ContentValues v=new ContentValues();
                v.put("dia_diem_id",place);v.put("id",bai.id);v.put("ten",bai.ten);
                v.put("mo_ta",bai.noiDung());v.put("noi_dung_thuyet_minh",bai.noiDung());
                v.put("thu_tu",bai.id);v.put("so_hien_thi",bai.id);v.put("tang",0);
                if(place==0) {
                    DuLieuDinhDocLap.DiemThamQuan d=DuLieuDinhDocLap.timTheoId(bai.id);
                    v.put("tang",d.tang);v.put("x_ty_le",d.x);v.put("y_ty_le",d.y);v.put("ten_anh",d.hinhAnh);
                } else if(place==1) {
                    float[] d=DuLieuSoDoThamQuan.BAO_TANG[bai.id-1];
                    v.put("tang",(int)d[0]);v.put("x_ty_le",d[1]);v.put("y_ty_le",d[2]);
                } else if(place==2 || place==4) {
                    float[] d=(place==2?DuLieuSoDoThamQuan.HUE:DuLieuSoDoThamQuan.VAN_MIEU)[bai.id-1];
                    v.put("x_ty_le",d[0]);v.put("y_ty_le",d[1]);
                    CauHinhAutoDiSan.Diem gps=cauHinh.cacDiem.get(bai.id-1);
                    gps(v,gps.choPhepAuto,gps.lat,gps.lon,gps.banKinh,gps.nguon);
                } else {
                    CauHinhAutoConDao.Diem d=CauHinhAutoConDao.tim(bai.id);
                    v.put("so_hien_thi",d.soSoDo);
                    if(d.coMarker()){v.put("x_ty_le",d.xAnh);v.put("y_ty_le",d.yAnh);}
                    gps(v,d.choPhepAuto,d.lat,d.lon,d.banKinh,d.nguon);
                }
                db.insertWithOnConflict(BANG_DIEM,null,v,SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
        nhapBangCu(db,0,"diem_tham_quan","mo_ta");
        if(coBang(db,"thuyet_minh")) {
            try(Cursor c=db.rawQuery("SELECT diem_tham_quan_id,noi_dung FROM thuyet_minh",null)) {
                while(c.moveToNext()) { ContentValues v=new ContentValues();v.put("noi_dung_thuyet_minh",c.getString(1));capNhat(db,0,c.getInt(0),v); }
            }
        }
        nhapBangCu(db,1,"bao_tang_marker","noi_dung");
        nhapBangCu(db,2,"dai_noi_hue_diem","mo_ta");
        nhapBangCu(db,3,"con_dao_diem","mo_ta");
        nhapBangCu(db,4,"van_mieu_diem","mo_ta");
        nhapToaDo(db,2,"dai_noi_hue_toa_do");
        nhapToaDo(db,3,"con_dao_toa_do");
        nhapToaDo(db,4,"van_mieu_toa_do");
        for(int place=0;place<5;place++) {
            try(Cursor c=db.rawQuery("SELECT ten_anh FROM "+BANG_SO_DO+" WHERE dia_diem_id=? ORDER BY tang LIMIT 1",new String[]{""+place})) {
                if(c.moveToFirst()){ContentValues v=new ContentValues();v.put("so_do_mac_dinh",c.getString(0));db.update("dia_diem",v,"id=? AND so_do_mac_dinh=''",new String[]{""+place});}
            }
        }
    }
    private static void themSoDo(SQLiteDatabase db,int place,int tang,String ten,String anh) {
        ContentValues v=new ContentValues();v.put("dia_diem_id",place);v.put("tang",tang);v.put("ten",ten);v.put("ten_anh",anh);
        db.insertWithOnConflict(BANG_SO_DO,null,v,SQLiteDatabase.CONFLICT_IGNORE);
    }
    private static void gps(ContentValues v,boolean auto,double lat,double lon,float r,String nguon) {
        v.put("cho_phep_auto",auto?1:0);v.put("nguon_gps",nguon);
        if(Double.isFinite(lat))v.put("vi_do",lat);if(Double.isFinite(lon))v.put("kinh_do",lon);
        if(Float.isFinite(r)&&r>0)v.put("ban_kinh",r);
    }
    private static void nhapBangCu(SQLiteDatabase db,int place,String bang,String bai) {
        if(!coBang(db,bang)||!coCot(db,bang,"ten")||!coCot(db,bang,bai))return;
        try(Cursor c=db.query(bang,null,null,null,null,null,null)) {
            while(c.moveToNext()) {
                int id=c.getInt(c.getColumnIndexOrThrow("id"));
                ContentValues v=new ContentValues();v.put("ten",c.getString(c.getColumnIndexOrThrow("ten")));
                v.put("mo_ta",c.getString(c.getColumnIndexOrThrow(bai)));v.put("noi_dung_thuyet_minh",c.getString(c.getColumnIndexOrThrow(bai)));
                if(place==0){v.remove("noi_dung_thuyet_minh"); v.put("tang",c.getInt(c.getColumnIndexOrThrow("khu_vuc_id")));}
                if(place==1)v.put("tang",c.getInt(c.getColumnIndexOrThrow("tang")));
                if(place==0 || place==1) {
                    double x=c.getDouble(c.getColumnIndexOrThrow(place==0?"toa_do_x":"x"));
                    double y=c.getDouble(c.getColumnIndexOrThrow(place==0?"toa_do_y":"y"));
                    if(x>=0&&x<=1&&y>=0&&y<=1){v.put("x_ty_le",x);v.put("y_ty_le",y);}
                }
                capNhat(db,place,id,v);
            }
        }
    }
    private static void nhapToaDo(SQLiteDatabase db,int place,String bang) {
        if(!coBang(db,bang)||!coCot(db,bang,"x_ty_le"))return;
        try(Cursor c=db.rawQuery("SELECT id,x_ty_le,y_ty_le FROM "+bang,null)) {
            while(c.moveToNext()) {
                int id=c.getInt(0);double x=c.getDouble(1),y=c.getDouble(2);
                if(place==4&&id>=1&&id<=16){float[] cu=DuLieuSoDoThamQuan.VAN_MIEU_CU[id-1];
                    if(Math.abs(x-cu[0])<.00001&&Math.abs(y-cu[1])<.00001){x=DuLieuSoDoThamQuan.VAN_MIEU[id-1][0];y=DuLieuSoDoThamQuan.VAN_MIEU[id-1][1];}}
                if(x>=0&&x<=1&&y>=0&&y<=1)luuToaDo(db,place,id,x,y);
            }
        }
    }
    public static ArrayList<DiemThamQuan> danhSach(SQLiteDatabase db,int place) {
        ArrayList<DiemThamQuan> ds=new ArrayList<>();
        try(Cursor c=db.query(BANG_DIEM,null,"dia_diem_id=?",new String[]{""+place},null,null,"thu_tu,id")) {
            while(c.moveToNext())ds.add(new DiemThamQuan(c));
        }
        return ds;
    }
    public static DiemThamQuan lay(SQLiteDatabase db,int place,int id) {
        try(Cursor c=db.query(BANG_DIEM,null,"dia_diem_id=? AND id=?",new String[]{""+place,""+id},null,null,null)) {
            return c.moveToFirst()?new DiemThamQuan(c):null;
        }
    }
    public static String anhSoDo(SQLiteDatabase db,int place,int tang) {
        try(Cursor c=db.rawQuery("SELECT ten_anh FROM "+BANG_SO_DO+" WHERE dia_diem_id=? AND tang=?",new String[]{""+place,""+tang})) {
            return c.moveToFirst()?c.getString(0):"";
        }
    }
    public static boolean luuToaDo(SQLiteDatabase db,int place,int id,double x,double y) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||x<0||x>1||y<0||y>1)return false;
        ContentValues v=new ContentValues();v.put("x_ty_le",x);v.put("y_ty_le",y);
        return capNhat(db,place,id,v)==1;
    }
    private static int capNhat(SQLiteDatabase db,int place,int id,ContentValues v) {
        return db.update(BANG_DIEM,v,"dia_diem_id=? AND id=?",new String[]{""+place,""+id});
    }
    /** API dữ liệu cho luồng quản trị sau này; nơi gọi phải kiểm tra quyền ADMIN. */
    public static boolean luuDiem(SQLiteDatabase db,int place,int id,ContentValues thayDoi) {
        Set<String> choPhep=new HashSet<>(Arrays.asList("ten","mo_ta","noi_dung_thuyet_minh","tang","thu_tu","ten_anh","x_ty_le","y_ty_le","so_hien_thi","cho_phep_auto","vi_do","kinh_do","ban_kinh","nguon_gps"));
        if(place<0||id<=0||thayDoi==null||!choPhep.containsAll(thayDoi.keySet()))return false;
        ContentValues v=new ContentValues();
        try(Cursor c=db.query(BANG_DIEM,null,"dia_diem_id=? AND id=?",new String[]{""+place,""+id},null,null,null)) {
            if(c.moveToFirst())DatabaseUtils.cursorRowToContentValues(c,v);
        }
        v.putAll(thayDoi);v.put("dia_diem_id",place);v.put("id",id);
        if(!v.containsKey("tang"))v.put("tang",0);
        if(v.getAsString("ten")==null||v.getAsString("ten").trim().isEmpty())return false;
        try(Cursor c=db.rawQuery("SELECT 1 FROM dia_diem d JOIN "+BANG_SO_DO+" s ON s.dia_diem_id=d.id WHERE d.id=? AND s.tang=?",new String[]{""+place,v.getAsString("tang")})) {
            if(!c.moveToFirst())return false;
        }
        try { return db.insertWithOnConflict(BANG_DIEM,null,v,SQLiteDatabase.CONFLICT_REPLACE)!=-1; }
        catch(android.database.sqlite.SQLiteConstraintException e){return false;}
    }
}
