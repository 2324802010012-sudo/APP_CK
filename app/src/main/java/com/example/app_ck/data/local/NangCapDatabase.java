package com.example.app_ck.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.repository.KhoDuLieuThamQuan;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoNguonThongTin;
import com.example.app_ck.data.repository.KhoToaDoThamQuan;
import com.example.app_ck.data.seed.CauHinhAutoConDao;
import com.example.app_ck.data.seed.DuLieuDinhDocLap;
import com.example.app_ck.data.seed.DuLieuNoiDungThamQuan;
import com.example.app_ck.data.seed.DuLieuSoDoThamQuan;
import com.example.app_ck.data.seed.KhoiTaoDuLieu;
import com.example.app_ck.location.CauHinhAutoDiSan;
import com.example.app_ck.data.seed.DanhMucAnhMacDinh;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tạo/nâng cấp schema, chuyển đổi dữ liệu tour cũ và củng cố tính toàn vẹn.
 * SQLiteOpenHelper bọc onCreate/onUpgrade trong transaction; lỗi sẽ rollback.
 */
public final class NangCapDatabase {
    private NangCapDatabase() {}

    // 1. Điều phối schema và kiểm tra liên kết.
    public static void thucHien(SQLiteDatabase db, int phienBanCu) {
        if (phienBanCu >= 17) {

            boSungAnhMacDinh(db);
            kiemTraLienKet(db);

            return;
        }
        if (phienBanCu == 16) {

            khoiTaoBoDem(db);

            boSungAnhMacDinh(db);

            kiemTraLienKet(db);

            return;
        }
        if (phienBanCu >= 15) {

            cungCoToanVen(db);

            boSungAnhMacDinh(db);

            kiemTraLienKet(db);

            return;
        }
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
        if(phienBanCu==0) chuyenDoiDuLieuThamQuan(db,false);
        else KhoDuLieuThamQuan.nangCap(db);
        KhoNguonThongTin.khoiTao(db);

        boSungAnhMacDinh(db);

        cungCoToanVen(db);
        kiemTraLienKet(db);
    }

    public static void themCot(SQLiteDatabase db, String bang, String cot, String khaiBao) {
        if (!KhoDuLieuThamQuan.coCot(db, bang, cot))
            db.execSQL("ALTER TABLE " + bang + " ADD COLUMN " + cot + " " + khaiBao);
    }
    private static void boSungAnhMacDinh(
            SQLiteDatabase db
    ) {

        for (int idDiaDiem = 0; idDiaDiem < 5; idDiaDiem++) {

            int soDiem =
                    DanhMucAnhMacDinh.soDiem(idDiaDiem);

            for (int idDiem = 1; idDiem <= soDiem; idDiem++) {

                String tenAnh = DanhMucAnhMacDinh.lay(
                        idDiaDiem,
                        idDiem
                );

                if (tenAnh.isEmpty()) {
                    continue;
                }

                // Chi bo sung cho ban ghi chua co anh.
                // Khong ghi de anh Admin da luu.
                db.execSQL(
                        "UPDATE " + KhoDuLieuThamQuan.BANG_DIEM
                                + " SET ten_anh=? "
                                + "WHERE dia_diem_id=? AND id=? "
                                + "AND (ten_anh IS NULL "
                                + "OR trim(ten_anh)='')",
                        new Object[]{
                                tenAnh,
                                idDiaDiem,
                                idDiem
                        }
                );
            }
        }
    }
    private static void kiemTraLienKet(SQLiteDatabase db) {
        kiemTraKhoaNgoai(db);
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

    // 2. Chuyển bảng tour cũ sang dữ liệu tham quan dùng chung.
    public static void chuyenDoiDuLieuThamQuan(SQLiteDatabase db) {
        chuyenDoiDuLieuThamQuan(db,true);
    }

    public static void chuyenDoiDuLieuThamQuan(SQLiteDatabase db,boolean nhapDuLieuCu) {
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
                v.put(
                        "ten_anh",
                        DanhMucAnhMacDinh.lay(place, bai.id)
                );
                if (place==0) {
                    DuLieuDinhDocLap.DiemThamQuan d = DuLieuDinhDocLap.timTheoId(bai.id);
                    v.put("tang", d.tang);
                    v.put("x_ty_le", d.x);
                    v.put("y_ty_le", d.y);
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
        if(nhapDuLieuCu) {
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
        }
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
                int cotBai = c.getColumnIndex(bai);
                if (cotBai >= 0 && !c.isNull(cotBai)) {
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

    // 3. Bộ đếm ID, cách ly dữ liệu mồ côi và ràng buộc khóa ngoại.
    private static void khoiTaoBoDem(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS bo_dem_ma(loai TEXT NOT NULL,pham_vi INTEGER NOT NULL,"
                + "gia_tri INTEGER NOT NULL,PRIMARY KEY(loai,pham_vi))");
        db.execSQL("INSERT OR IGNORE INTO bo_dem_ma VALUES('dia_diem',0,4)");
        db.execSQL("UPDATE bo_dem_ma SET gia_tri=MAX(gia_tri,(SELECT COALESCE(MAX(id),4) FROM dia_diem)) WHERE loai='dia_diem' AND pham_vi=0");
        db.execSQL("INSERT OR IGNORE INTO bo_dem_ma SELECT 'diem',dia_diem_id,MAX(id) FROM diem_tham_quan_chung GROUP BY dia_diem_id");
    }

    private static void cungCoToanVen(SQLiteDatabase db) {
        khoiTaoBoDem(db);
        db.execSQL("CREATE TABLE IF NOT EXISTS du_lieu_cach_ly (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "bang_goc TEXT NOT NULL,du_lieu TEXT NOT NULL,ly_do TEXT NOT NULL,thoi_gian INTEGER NOT NULL)");
        // Giữ điểm có địa điểm hợp lệ nhưng thiếu tầng bằng một sơ đồ trống.
        db.execSQL("INSERT OR IGNORE INTO so_do_tham_quan(dia_diem_id,tang,ten,ten_anh) "
                + "SELECT p.dia_diem_id,p.tang,'Khu vực '||p.tang,'' FROM diem_tham_quan_chung p "
                + "JOIN dia_diem d ON d.id=p.dia_diem_id");
        cachLy(db,"yeu_thich","NOT EXISTS(SELECT 1 FROM dia_diem d WHERE d.id=yeu_thich.dia_diem_id) OR "
                + "NOT EXISTS(SELECT 1 FROM tai_khoan t WHERE t.id=yeu_thich.tai_khoan_id)");
        db.execSQL("UPDATE lich_su_nghe SET diem_id=NULL WHERE diem_id=0 AND che_do<>'AUTO_SUB'");
        cachLy(db,"lich_su_nghe","length(trim(ma_luot))=0 OR diem_id<=0 OR "
                + "NOT EXISTS(SELECT 1 FROM tai_khoan t WHERE t.id=lich_su_nghe.tai_khoan_id) OR "
                + "NOT EXISTS(SELECT 1 FROM dia_diem d WHERE d.id=lich_su_nghe.dia_diem_id)");
        cachLy(db,"lich_su_nghe","diem_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM diem_tham_quan_chung p "
                + "WHERE p.dia_diem_id=lich_su_nghe.dia_diem_id AND p.id=lich_su_nghe.diem_id)");
        for (String bang : new String[]{"bai_nguon","ghi_chu_nguon"}) {
            cachLy(db,bang,"NOT EXISTS(SELECT 1 FROM dia_diem d WHERE d.id="+bang+".dia_diem_id) OR "
                    + "(diem_id<>0 AND NOT EXISTS(SELECT 1 FROM diem_tham_quan_chung p WHERE "
                    + "p.dia_diem_id="+bang+".dia_diem_id AND p.id="+bang+".diem_id))");
        }
        // Bao gồm cả bảng cũ: giữ snapshot JSON trước khi loại bản ghi vi phạm.
        while (true) {
            List<String[]> loi = new ArrayList<>();
            try (Cursor c=db.rawQuery("PRAGMA foreign_key_check",null)) {
                while(c.moveToNext()) loi.add(new String[]{c.getString(0),c.getString(1)});
            }
            if(loi.isEmpty()) break;
            for(String[] l:loi) cachLy(db,l[0],"rowid="+Long.parseLong(l[1]));
        }
        // Một điểm mồ côi vừa được cách ly có thể để lại các liên kết chưa có FK ở v15.
        cachLy(db,"lich_su_nghe","diem_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM diem_tham_quan_chung p "
                + "WHERE p.dia_diem_id=lich_su_nghe.dia_diem_id AND p.id=lich_su_nghe.diem_id)");
        for(String bang:new String[]{"bai_nguon","ghi_chu_nguon"}) cachLy(db,bang,
                "NOT EXISTS(SELECT 1 FROM dia_diem d WHERE d.id="+bang+".dia_diem_id) OR "
                + "(diem_id<>0 AND NOT EXISTS(SELECT 1 FROM diem_tham_quan_chung p WHERE "
                + "p.dia_diem_id="+bang+".dia_diem_id AND p.id="+bang+".diem_id))");
        db.execSQL("CREATE TABLE yeu_thich_v16(tai_khoan_id INTEGER NOT NULL,dia_diem_id INTEGER NOT NULL,"
                + "PRIMARY KEY(tai_khoan_id,dia_diem_id),FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id),"
                + "FOREIGN KEY(dia_diem_id) REFERENCES dia_diem(id))");
        db.execSQL("INSERT INTO yeu_thich_v16 SELECT tai_khoan_id,dia_diem_id FROM yeu_thich");
        db.execSQL("DROP TABLE yeu_thich");
        db.execSQL("ALTER TABLE yeu_thich_v16 RENAME TO yeu_thich");
        db.execSQL("CREATE INDEX IF NOT EXISTS yeu_thich_dia_diem ON yeu_thich(dia_diem_id)");

        // Điểm tổng quan dùng NULL, điểm thành phần có khóa ghép theo địa điểm.
        long maLichSuCu=0;
        try(Cursor c=db.rawQuery("SELECT seq FROM sqlite_sequence WHERE name='lich_su_nghe'",null)) {
            if(c.moveToFirst()) maLichSuCu=c.getLong(0);
        }
        db.execSQL("CREATE TABLE lich_su_nghe_v16(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "tai_khoan_id INTEGER NOT NULL,dia_diem_id INTEGER NOT NULL,diem_id INTEGER CHECK(diem_id>0),"
                + "ten_dia_diem TEXT NOT NULL,ten_diem TEXT NOT NULL,che_do TEXT NOT NULL "
                + "CHECK(che_do IN ('MANUAL','AUTO_MAIN','AUTO_SUB','DEMO')) "
                + "CHECK(che_do!='AUTO_MAIN' OR diem_id IS NULL) "
                + "CHECK(che_do!='AUTO_SUB' OR (diem_id IS NOT NULL AND diem_id>0)),"
                + "thoi_gian INTEGER NOT NULL CHECK(thoi_gian>0),ma_luot TEXT NOT NULL CHECK(length(trim(ma_luot))>0),"
                + "UNIQUE(tai_khoan_id,ma_luot),FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id),"
                + "FOREIGN KEY(dia_diem_id) REFERENCES dia_diem(id),"
                + "FOREIGN KEY(dia_diem_id,diem_id) REFERENCES diem_tham_quan_chung(dia_diem_id,id))");
        db.execSQL("INSERT INTO lich_su_nghe_v16 SELECT id,tai_khoan_id,dia_diem_id,diem_id,ten_dia_diem,"
                + "ten_diem,che_do,thoi_gian,ma_luot FROM lich_su_nghe");
        db.execSQL("DROP TABLE lich_su_nghe");
        db.execSQL("ALTER TABLE lich_su_nghe_v16 RENAME TO lich_su_nghe");
        ContentValues sequence=new ContentValues();sequence.put("seq",maLichSuCu);
        if(db.update("sqlite_sequence",sequence,"name='lich_su_nghe' AND seq<?",new String[]{""+maLichSuCu})==0) {
            try(Cursor c=db.rawQuery("SELECT 1 FROM sqlite_sequence WHERE name='lich_su_nghe'",null)) {
                if(!c.moveToFirst()) {sequence.put("name","lich_su_nghe");db.insertOrThrow("sqlite_sequence",null,sequence);}
            }
        }
        db.execSQL("CREATE INDEX lich_su_theo_tai_khoan ON lich_su_nghe(tai_khoan_id,thoi_gian DESC,id DESC)");
        db.execSQL("CREATE INDEX lich_su_theo_diem ON lich_su_nghe(dia_diem_id,diem_id)");
        db.execSQL("CREATE INDEX IF NOT EXISTS bai_nguon_ma ON bai_nguon(ma_nguon)");

        for(String bang:new String[]{"bai_nguon","ghi_chu_nguon"}) {
            for(String suKien:new String[]{"INSERT","UPDATE"}) {
                db.execSQL("CREATE TRIGGER IF NOT EXISTS "+bang+"_kiem_tra_"+suKien+" BEFORE "+suKien+" ON "+bang
                        + " WHEN NOT EXISTS(SELECT 1 FROM dia_diem WHERE id=NEW.dia_diem_id) OR "
                        + "(NEW.diem_id<>0 AND NOT EXISTS(SELECT 1 FROM diem_tham_quan_chung "
                        + "WHERE dia_diem_id=NEW.dia_diem_id AND id=NEW.diem_id)) "
                        + "BEGIN SELECT RAISE(ABORT,'Nguon khong co dia diem/diem tham quan'); END");
            }
        }
        db.execSQL("CREATE TRIGGER IF NOT EXISTS diem_xoa_nguon AFTER DELETE ON diem_tham_quan_chung BEGIN "
                + "DELETE FROM bai_nguon WHERE dia_diem_id=OLD.dia_diem_id AND diem_id=OLD.id; "
                + "DELETE FROM ghi_chu_nguon WHERE dia_diem_id=OLD.dia_diem_id AND diem_id=OLD.id; END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS dia_diem_xoa_ghi_chu AFTER DELETE ON dia_diem BEGIN "
                + "DELETE FROM ghi_chu_nguon WHERE dia_diem_id=OLD.id; END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS diem_giu_khoa_nguon BEFORE UPDATE OF dia_diem_id,id ON diem_tham_quan_chung "
                + "WHEN (NEW.dia_diem_id<>OLD.dia_diem_id OR NEW.id<>OLD.id) AND "
                + "(EXISTS(SELECT 1 FROM bai_nguon WHERE dia_diem_id=OLD.dia_diem_id AND diem_id=OLD.id) OR "
                + "EXISTS(SELECT 1 FROM ghi_chu_nguon WHERE dia_diem_id=OLD.dia_diem_id AND diem_id=OLD.id)) "
                + "BEGIN SELECT RAISE(ABORT,'Khong doi khoa diem dang co nguon'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS dia_diem_giu_khoa_ghi_chu BEFORE UPDATE OF id ON dia_diem "
                + "WHEN NEW.id<>OLD.id AND EXISTS(SELECT 1 FROM ghi_chu_nguon WHERE dia_diem_id=OLD.id) "
                + "BEGIN SELECT RAISE(ABORT,'Khong doi khoa dia diem dang co ghi chu'); END");
        // Ràng buộc cả ghi SQL trực tiếp; SQLite cũ không hỗ trợ thêm CHECK bằng ALTER.
        for(String suKien:new String[]{"INSERT","UPDATE"}) {
            db.execSQL("CREATE TRIGGER IF NOT EXISTS dia_diem_hop_le_"+suKien+" BEFORE "+suKien+" ON dia_diem "
                    + "WHEN length(trim(NEW.ten))=0 OR NEW.vi_do NOT BETWEEN -90 AND 90 "
                    + "OR NEW.kinh_do NOT BETWEEN -180 AND 180 OR NEW.ban_kinh_auto<=0 "
                    + "BEGIN SELECT RAISE(ABORT,'Dia diem khong hop le'); END");
        }
        kiemTraKhoaNgoai(db);
    }

    private static void cachLy(SQLiteDatabase db,String bang,String dieuKien) {
        String ten="\""+bang.replace("\"","\"\"")+"\"";
        try(Cursor c=db.rawQuery("SELECT rowid AS _dong_cach_ly_,* FROM "+ten+" WHERE "+dieuKien,null)) {
            while(c.moveToNext()) {
                JSONObject json=new JSONObject();
                try {
                    for(int i=1;i<c.getColumnCount();i++) {
                        Object v;
                        switch(c.getType(i)) {
                            case Cursor.FIELD_TYPE_NULL: v=JSONObject.NULL; break;
                            case Cursor.FIELD_TYPE_INTEGER: v=c.getLong(i); break;
                            case Cursor.FIELD_TYPE_FLOAT: v=c.getDouble(i); break;
                            case Cursor.FIELD_TYPE_BLOB: v=Base64.encodeToString(c.getBlob(i),Base64.NO_WRAP); break;
                            default: v=c.getString(i);
                        }
                        json.put(c.getColumnName(i),v);
                    }
                } catch(org.json.JSONException e) { throw new IllegalStateException("Không lưu được dữ liệu gốc",e); }
                ContentValues v=new ContentValues();v.put("bang_goc",bang);v.put("du_lieu",json.toString());
                v.put("ly_do",dieuKien);v.put("thoi_gian",System.currentTimeMillis());
                db.insertOrThrow("du_lieu_cach_ly",null,v);
            }
        }
        db.delete(ten,dieuKien,null);
    }

    public static void kiemTraKhoaNgoai(SQLiteDatabase db) {
        try(Cursor c=db.rawQuery("PRAGMA foreign_key_check",null)) {
            if(c.moveToFirst()) throw new IllegalStateException("Vi phạm khóa ngoại: "+c.getString(0)+"/"+c.getLong(1));
        }
    }
}
