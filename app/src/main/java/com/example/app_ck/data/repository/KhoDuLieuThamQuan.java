package com.example.app_ck.data.repository;

import com.example.app_ck.data.local.ChuyenDoiDuLieuThamQuan;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.session.PhienDangNhap;

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

    public static boolean coBang(SQLiteDatabase db,String ten) {
        try(Cursor c=db.rawQuery("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?",new String[]{ten})) {
            return c.moveToFirst();
        }
    }
    public static boolean coCot(SQLiteDatabase db,String bang,String cot) {
        try(Cursor c=db.rawQuery("PRAGMA table_info("+bang+")",null)) {
            while(c.moveToNext()) if(cot.equals(c.getString(c.getColumnIndexOrThrow("name")))) return true;
            return false;
        }
    }
    public static void taoBang(SQLiteDatabase db) {
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

    /** Chỉ gọi trong transaction tạo/nâng cấp. Không dùng làm đồng bộ khi mở app. */
    public static void nangCap(SQLiteDatabase db) { ChuyenDoiDuLieuThamQuan.thucHien(db); }

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
    @Deprecated public static boolean luuToaDo(SQLiteDatabase db,int place,int id,double x,double y) {
        throw new SecurityException("Thay đổi marker yêu cầu phiên ADMIN");
    }
    public static boolean luuToaDo(android.content.Context context,SQLiteDatabase db,int place,int id,double x,double y) {
        if(!Double.isFinite(x)||!Double.isFinite(y)||x<0||x>1||y<0||y>1)return false;
        ContentValues v=new ContentValues();v.put("x_ty_le",x);v.put("y_ty_le",y);
        return luuDiem(context,db,place,id,v);
    }
    private static int capNhat(SQLiteDatabase db,int place,int id,ContentValues v) {
        return db.update(BANG_DIEM,v,"dia_diem_id=? AND id=?",new String[]{""+place,""+id});
    }
    public static boolean coDiaDiem(SQLiteDatabase db, int place) {
        try (Cursor c=db.rawQuery("SELECT 1 FROM dia_diem WHERE id=?",new String[]{""+place})) {
            return c.moveToFirst();
        }
    }
    public static java.util.LinkedHashMap<Integer,String> cacTang(SQLiteDatabase db, int place) {
        java.util.LinkedHashMap<Integer,String> ds=new java.util.LinkedHashMap<>();
        try (Cursor c=db.rawQuery("SELECT tang,ten FROM " + BANG_SO_DO + " WHERE dia_diem_id=? ORDER BY tang",new String[]{""+place})) {
            while (c.moveToNext()) ds.put(c.getInt(0),c.getString(1));
        }
        return ds;
    }

    /** API cũ không có danh tính phiên không được phép ghi quản trị. */
    @Deprecated public static boolean luuDiem(SQLiteDatabase db,int place,int id,ContentValues v) {
        throw new SecurityException("Dùng luuDiem(Context, SQLiteDatabase, ...) để kiểm tra phiên ADMIN");
    }

    public static boolean luuDiem(android.content.Context context, SQLiteDatabase db,
                                  int place, int id, ContentValues thayDoi) {
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(context,db);
            Set<String> choPhep=new HashSet<>(Arrays.asList("ten","mo_ta","noi_dung_thuyet_minh","tang","thu_tu","ten_anh","x_ty_le","y_ty_le","so_hien_thi","cho_phep_auto","vi_do","kinh_do","ban_kinh","nguon_gps"));
            if (place<0 || id<=0 || thayDoi==null || thayDoi.size()==0 || !choPhep.containsAll(thayDoi.keySet()) || !coDiaDiem(db,place)) return false;
            ContentValues v=new ContentValues();
            boolean tonTai=false;
            try (Cursor c=db.query(BANG_DIEM,null,"dia_diem_id=? AND id=?",new String[]{""+place,""+id},null,null,null)) {
                if (c.moveToFirst()) { DatabaseUtils.cursorRowToContentValues(c,v); tonTai=true; }
            }
            v.putAll(thayDoi); v.put("dia_diem_id",place); v.put("id",id);
            if (!v.containsKey("tang")) v.put("tang",0);
            String ten=v.getAsString("ten");
            if (ten==null || ten.trim().isEmpty()) return false;
            v.put("ten",ten.trim());
            // Điểm chưa có ảnh sơ đồ vẫn xuất hiện ở danh sách cùng tầng tương ứng.
            ContentValues soDo=new ContentValues(); soDo.put("dia_diem_id",place); soDo.put("tang",v.getAsInteger("tang"));
            soDo.put("ten","Khu vực " + v.getAsInteger("tang")); soDo.put("ten_anh", "");
            db.insertWithOnConflict(BANG_SO_DO,null,soDo,SQLiteDatabase.CONFLICT_IGNORE);
            boolean thanhCong=tonTai ? capNhat(db,place,id,v)==1 : db.insert(BANG_DIEM,null,v)!=-1;
            if (thanhCong) db.setTransactionSuccessful();
            return thanhCong;
        } catch (android.database.sqlite.SQLiteConstraintException | IllegalArgumentException ex) { return false; }
        finally { db.endTransaction(); }
    }

    public static boolean xoaDiem(android.content.Context context, SQLiteDatabase db,int place,int id) {
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(context,db);
            if (dem(db,"SELECT COUNT(*) FROM lich_su_nghe WHERE dia_diem_id=? AND diem_id=?",new String[]{""+place,""+id})>0) return false;
            if (coBang(db,"bai_nguon")) db.delete("bai_nguon","dia_diem_id=? AND diem_id=?",new String[]{""+place,""+id});
            if (coBang(db,"ghi_chu_nguon")) db.delete("ghi_chu_nguon","dia_diem_id=? AND diem_id=?",new String[]{""+place,""+id});
            boolean ok=db.delete(BANG_DIEM,"dia_diem_id=? AND id=?",new String[]{""+place,""+id})==1;
            if (ok) db.setTransactionSuccessful();
            return ok;
        } finally { db.endTransaction(); }
    }

    public static boolean luuDiaDiem(android.content.Context context, SQLiteDatabase db,int id,ContentValues thayDoi) {
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(context,db);
            Set<String> cot=new HashSet<>(Arrays.asList("ten","dia_chi","mo_ta","danh_muc","vi_do","kinh_do","ten_anh","ban_kinh_auto","so_do_mac_dinh"));
            if (id<0 || thayDoi==null || thayDoi.size()==0 || !cot.containsAll(thayDoi.keySet())) return false;
            ContentValues v=new ContentValues(); boolean tonTai=false;
            try (Cursor c=db.query("dia_diem",null,"id=?",new String[]{""+id},null,null,null)) {
                if (c.moveToFirst()) { DatabaseUtils.cursorRowToContentValues(c,v); tonTai=true; }
            }
            v.putAll(thayDoi); v.put("id",id);
            if (v.getAsString("ten")==null || v.getAsString("ten").trim().isEmpty()
                    || !soHopLe(v,"vi_do",-90,90) || !soHopLe(v,"kinh_do",-180,180)
                    || (v.containsKey("ban_kinh_auto") && !soHopLe(v,"ban_kinh_auto",0.01,100000))) return false;
            boolean ok=tonTai ? db.update("dia_diem",v,"id=?",new String[]{""+id})==1 : db.insert("dia_diem",null,v)!=-1;
            if (ok) db.setTransactionSuccessful();
            return ok;
        } catch (android.database.sqlite.SQLiteConstraintException | IllegalArgumentException ex) { return false; }
        finally { db.endTransaction(); }
    }
    public static boolean xoaDiaDiem(android.content.Context context, SQLiteDatabase db,int id) {
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(context,db);
            String[] a={""+id};
            if (dem(db,"SELECT COUNT(*) FROM " + BANG_DIEM + " WHERE dia_diem_id=?",a)>0
                    || dem(db,"SELECT COUNT(*) FROM lich_su_nghe WHERE dia_diem_id=?",a)>0
                    || dem(db,"SELECT COUNT(*) FROM yeu_thich WHERE dia_diem_id=?",a)>0) return false;
            if (coBang(db,"khu_vuc") && dem(db,"SELECT COUNT(*) FROM khu_vuc WHERE dia_diem_id=?",a)>0) return false;
            db.delete(BANG_SO_DO,"dia_diem_id=?",a);
            if (coBang(db,"bai_nguon")) db.delete("bai_nguon","dia_diem_id=?",a);
            if (coBang(db,"ghi_chu_nguon")) db.delete("ghi_chu_nguon","dia_diem_id=?",a);
            boolean ok=db.delete("dia_diem","id=?",a)==1;
            if (ok) db.setTransactionSuccessful();
            return ok;
        } finally { db.endTransaction(); }
    }
    public static boolean luuSoDo(android.content.Context context, SQLiteDatabase db,int place,int tang,String ten,String tenAnh) {
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(context,db);
            if (!coDiaDiem(db,place) || ten==null || ten.trim().isEmpty() || tenAnh==null) return false;
            ContentValues v=new ContentValues(); v.put("dia_diem_id",place); v.put("tang",tang); v.put("ten",ten.trim()); v.put("ten_anh",tenAnh);
            String[] a={""+place,""+tang};
            boolean tonTai=dem(db,"SELECT COUNT(*) FROM " + BANG_SO_DO + " WHERE dia_diem_id=? AND tang=?",a)>0;
            boolean ok=tonTai ? db.update(BANG_SO_DO,v,"dia_diem_id=? AND tang=?",a)==1 : db.insert(BANG_SO_DO,null,v)!=-1;
            if (ok) db.setTransactionSuccessful();
            return ok;
        } finally { db.endTransaction(); }
    }
    public static boolean xoaSoDo(android.content.Context context, SQLiteDatabase db,int place,int tang) {
        db.beginTransaction();
        try {
            PhienDangNhap.yeuCauQuanTri(context,db);
            String[] a={""+place,""+tang};
            if (dem(db,"SELECT COUNT(*) FROM " + BANG_DIEM + " WHERE dia_diem_id=? AND tang=?",a)>0) return false;
            boolean ok=db.delete(BANG_SO_DO,"dia_diem_id=? AND tang=?",a)==1;
            if (ok) db.setTransactionSuccessful();
            return ok;
        } finally { db.endTransaction(); }
    }
    public static boolean luuThuyetMinh(android.content.Context context,SQLiteDatabase db,int place,int point,String noiDung) {
        if (noiDung==null) return false;
        ContentValues v=new ContentValues(); v.put(point==0 ? "mo_ta" : "noi_dung_thuyet_minh",noiDung);
        return point==0 ? luuDiaDiem(context,db,place,v) : luuDiem(context,db,place,point,v);
    }
    public static boolean luuCauHinhAuto(android.content.Context context, SQLiteDatabase db,
            int place,int point,boolean choPhep,double lat,double lon,float radius,String nguon) {
        ContentValues v=new ContentValues(); v.put("vi_do",lat); v.put("kinh_do",lon);
        if (point==0) { v.put("ban_kinh_auto",radius); return luuDiaDiem(context,db,place,v); }
        v.put("cho_phep_auto",choPhep ? 1 : 0); v.put("ban_kinh",radius); v.put("nguon_gps",nguon==null ? "" : nguon);
        return luuDiem(context,db,place,point,v);
    }
    private static long dem(SQLiteDatabase db,String sql,String[] a) {
        try (Cursor c=db.rawQuery(sql,a)) { c.moveToFirst(); return c.getLong(0); }
    }
    private static boolean soHopLe(ContentValues v,String cot,double min,double max) {
        Double n=v.getAsDouble(cot); return n!=null && Double.isFinite(n) && n>=min && n<=max;
    }
}
