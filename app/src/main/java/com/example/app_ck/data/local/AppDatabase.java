package com.example.app_ck.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.data.repository.KhoTaiKhoan;
import com.example.app_ck.data.repository.KhoYeuThich;
import com.example.app_ck.data.repository.KhoDiaDiem;
import com.example.app_ck.data.seed.DuLieuDinhDocLap;
import java.util.ArrayList;

/** Một SQLiteOpenHelper cho toàn ứng dụng. API cũ ủy quyền sang repository để giữ tương thích. */
public class AppDatabase extends SQLiteOpenHelper {
    public static final String DB = "du_lich_tai_khoan.db";
    public static final int VERSION = 15;
    private final KhoTaiKhoan taiKhoan;
    private final KhoYeuThich yeuThich;
    private final KhoDiaDiem diaDiem;
    public AppDatabase(Context context) {
        super(context, DB, null, VERSION);
        taiKhoan=new KhoTaiKhoan(context,this);
        yeuThich=new KhoYeuThich(this);
        diaDiem=new KhoDiaDiem(this);
    }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tai_khoan (id INTEGER PRIMARY KEY AUTOINCREMENT, ten_dang_nhap TEXT NOT NULL UNIQUE, muoi TEXT NOT NULL, mat_khau_bam TEXT NOT NULL, ma_khoi_phuc_bam TEXT)");
        db.execSQL("CREATE TABLE yeu_thich (tai_khoan_id INTEGER NOT NULL, dia_diem_id INTEGER NOT NULL, PRIMARY KEY(tai_khoan_id,dia_diem_id), FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id))");
        NangCapDatabase.thucHien(db, 0);
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion) { NangCapDatabase.thucHien(db,oldVersion); }

    public HoSoTaiKhoan layHoSo(long id) { return taiKhoan.layHoSo(id); }
    public boolean capNhatHoSo(long id,String hoTen,String email) { return taiKhoan.capNhatHoSo(id,hoTen,email); }
    public String doiMatKhauDaDangNhap(long id,String cu,String moi) { return taiKhoan.doiMatKhauDaDangNhap(id,cu,moi); }
    public static String taoMaKhoiPhuc() { return KhoTaiKhoan.taoMaKhoiPhuc(); }
    public boolean dangKy(String ten,String matKhau,String maKhoiPhuc) { return taiKhoan.dangKy(ten,matKhau,maKhoiPhuc); }
    public boolean dangKy(String ten,String matKhau) { return taiKhoan.dangKy(ten,matKhau); }
    public long dangNhap(String ten,String matKhau) { return taiKhoan.dangNhap(ten,matKhau); }
    public String doiMatKhauBangMa(String ten,String ma,String matKhauMoi) { return taiKhoan.doiMatKhauBangMa(ten,ma,matKhauMoi); }
    public boolean capNhatMaKhoiPhuc(long id,String matKhau,String maMoi) { return taiKhoan.capNhatMaKhoiPhuc(id,matKhau,maMoi); }
    public ArrayList<HoSoTaiKhoan> danhSachTaiKhoan(String tuKhoa) { return taiKhoan.danhSachTaiKhoan(tuKhoa); }
    public HoSoTaiKhoan chiTietTaiKhoan(long id) { return taiKhoan.chiTietTaiKhoan(id); }
    public boolean datKhoaTaiKhoan(long id,boolean khoa) { return taiKhoan.datKhoaTaiKhoan(id,khoa); }
    public boolean daYeuThich(long id,int place) { return yeuThich.daYeuThich(id,place); }
    public void datYeuThich(long id,int place,boolean luu) { yeuThich.datYeuThich(id,place,luu); }
    public ArrayList<Integer> danhSachYeuThich(long id) { return yeuThich.danhSachYeuThich(id); }
    public ArrayList<DiaDiem> danhSachDiaDiem() { return diaDiem.danhSachDiaDiem(); }
    public ArrayList<DuLieuDinhDocLap.DiemThamQuan> layDiemDinhDocLap() { return diaDiem.layDiemDinhDocLap(); }
    public String layThuyetMinhTheoId(int id) { return diaDiem.layThuyetMinhTheoId(id); }
}
