package com.example.app_ck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class TaiKhoanDb extends SQLiteOpenHelper {
    private static final String DB = "du_lich_tai_khoan.db";
    private static final int VERSION = 2;
    public TaiKhoanDb(Context context) { super(context, DB, null, VERSION); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tai_khoan (id INTEGER PRIMARY KEY AUTOINCREMENT, ten_dang_nhap TEXT NOT NULL UNIQUE, muoi TEXT NOT NULL, mat_khau_bam TEXT NOT NULL, ma_khoi_phuc_bam TEXT)");
        db.execSQL("CREATE TABLE yeu_thich (tai_khoan_id INTEGER NOT NULL, dia_diem_id INTEGER NOT NULL, PRIMARY KEY(tai_khoan_id,dia_diem_id), FOREIGN KEY(tai_khoan_id) REFERENCES tai_khoan(id))");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE tai_khoan ADD COLUMN ma_khoi_phuc_bam TEXT");
    }
    private String chuanHoa(String ten) { return ten.trim().toLowerCase(Locale.ROOT); }
    private String bam(String input, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(input.toCharArray(), salt, 120000, 256);
        try { return Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded(), Base64.NO_WRAP); }
        finally { spec.clearPassword(); }
    }
    private byte[] muoiMoi() { byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt); return salt; }
    private String bamMa(String code, byte[] salt) throws Exception { return bam(code.trim().toUpperCase(Locale.ROOT), salt); }
    private boolean bangNhau(String a, String b) {
        byte[] x = Base64.decode(a, Base64.NO_WRAP), y = Base64.decode(b, Base64.NO_WRAP);
        if (x.length != y.length) return false;
        int diff = 0; for (int i=0; i<x.length; i++) diff |= x[i] ^ y[i];
        return diff == 0;
    }
    public static String taoMaKhoiPhuc() {
        byte[] b = new byte[12]; new SecureRandom().nextBytes(b);
        return Base64.encodeToString(b, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }
    // Dung phuong thuc 3 tham so khi dang ky moi.
    public boolean dangKy(String ten, String matKhau, String maKhoiPhuc) {
        try {
            byte[] salt = muoiMoi();
            ContentValues cv = new ContentValues();
            cv.put("ten_dang_nhap", chuanHoa(ten));
            cv.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhau, salt));
            cv.put("ma_khoi_phuc_bam", bamMa(maKhoiPhuc, salt));
            return getWritableDatabase().insert("tai_khoan", null, cv) != -1;
        } catch (Exception ex) { return false; }
    }
    // Giu tuong thich code cu; tai khoan tao bang ham nay KHONG co ma khoi phuc.
    public boolean dangKy(String ten, String matKhau) {
        try {
            byte[] salt = muoiMoi(); ContentValues cv = new ContentValues();
            cv.put("ten_dang_nhap", chuanHoa(ten));
            cv.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhau, salt));
            return getWritableDatabase().insert("tai_khoan", null, cv) != -1;
        } catch (Exception ex) { return false; }
    }
    public long dangNhap(String ten, String matKhau) {
        try (Cursor c = getReadableDatabase().query("tai_khoan", new String[]{"id","muoi","mat_khau_bam"}, "ten_dang_nhap=?", new String[]{chuanHoa(ten)}, null,null,null)) {
            if (!c.moveToFirst()) return -1;
            byte[] salt = Base64.decode(c.getString(1), Base64.NO_WRAP);
            return bangNhau(bam(matKhau,salt), c.getString(2)) ? c.getLong(0) : -1;
        } catch (Exception ex) { return -1; }
    }
    public String doiMatKhauBangMa(String ten, String maKhoiPhuc, String matKhauMoi) {
        if (matKhauMoi.length() < 8 || maKhoiPhuc.trim().isEmpty()) return null;
        try (Cursor c = getReadableDatabase().query("tai_khoan", new String[]{"id","muoi","ma_khoi_phuc_bam"}, "ten_dang_nhap=?", new String[]{chuanHoa(ten)},null,null,null)) {
            if (!c.moveToFirst() || c.isNull(2)) return null;
            byte[] saltCu = Base64.decode(c.getString(1), Base64.NO_WRAP);
            if (!bangNhau(bamMa(maKhoiPhuc,saltCu), c.getString(2))) return null;
            byte[] saltMoi = muoiMoi();
            ContentValues cv = new ContentValues();
            cv.put("muoi", Base64.encodeToString(saltMoi, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhauMoi,saltMoi));
            String maMoi = taoMaKhoiPhuc();
            cv.put("ma_khoi_phuc_bam", bamMa(maMoi,saltMoi));
            return getWritableDatabase().update("tai_khoan",cv,"id=?",new String[]{String.valueOf(c.getLong(0))}) == 1 ? maMoi : null;
        } catch (Exception ex) { return null; }
    }
    public boolean capNhatMaKhoiPhuc(long id, String matKhauHienTai, String maMoi) {
        try (Cursor c = getReadableDatabase().query("tai_khoan",new String[]{"muoi","mat_khau_bam"},"id=?",new String[]{String.valueOf(id)},null,null,null)) {
            if (!c.moveToFirst()) return false;
            byte[] salt = Base64.decode(c.getString(0),Base64.NO_WRAP);
            if (!bangNhau(bam(matKhauHienTai,salt),c.getString(1))) return false;
            ContentValues cv = new ContentValues(); cv.put("ma_khoi_phuc_bam",bamMa(maMoi,salt));
            return getWritableDatabase().update("tai_khoan",cv,"id=?",new String[]{String.valueOf(id)}) == 1;
        } catch (Exception ex) { return false; }
    }
    public boolean daYeuThich(long id, int diaDiem) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM yeu_thich WHERE tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)})) { return c.moveToFirst(); }
    }
    public void datYeuThich(long id, int diaDiem, boolean thich) {
        if (thich) { ContentValues cv = new ContentValues(); cv.put("tai_khoan_id",id); cv.put("dia_diem_id",diaDiem); getWritableDatabase().insertWithOnConflict("yeu_thich",null,cv,SQLiteDatabase.CONFLICT_IGNORE); }
        else getWritableDatabase().delete("yeu_thich","tai_khoan_id=? AND dia_diem_id=?",new String[]{String.valueOf(id),String.valueOf(diaDiem)});
    }
    public ArrayList<Integer> danhSachYeuThich(long id) {
        ArrayList<Integer> ds = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("yeu_thich",new String[]{"dia_diem_id"},"tai_khoan_id=?",new String[]{String.valueOf(id)},null,null,"dia_diem_id ASC")) { while(c.moveToNext()) ds.add(c.getInt(0)); }
        return ds;
    }
}
