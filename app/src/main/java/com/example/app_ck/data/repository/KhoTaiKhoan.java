package com.example.app_ck.data.repository;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.session.PhienDangNhap;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Base64;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Xác thực, hồ sơ và quản lý tài khoản; không giữ vai trò trong bộ nhớ. */
public final class KhoTaiKhoan {
    private final AppDatabase helper;
    private final Context context;
    public KhoTaiKhoan(Context context,AppDatabase helper) { this.context=context; this.helper=helper; }

    public HoSoTaiKhoan layHoSo(long id) {
        if (!PhienDangNhap.coQuyenHoSo(context,helper.getReadableDatabase(),id,true)) return null;
        try (Cursor c = helper.getReadableDatabase().query("tai_khoan", new String[]{"ten_dang_nhap","ho_ten","email","vai_tro","trang_thai","ngay_tao"},
                "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? new HoSoTaiKhoan(id, c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getLong(5)) : null;
        }
    }

    public boolean capNhatHoSo(long id, String hoTen, String email) {
        if (!PhienDangNhap.coQuyenHoSo(context,helper.getReadableDatabase(),id,false)) return false;
        if (hoTen == null || email == null) return false;
        String h = hoTen.trim(), e = email.trim();
        if (id <= 0 || h.length() > 80 || e.length() > 254
                || (!e.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches())) return false;
        ContentValues v = new ContentValues();
        v.put("ho_ten", h); v.put("email", e);
        return helper.getWritableDatabase().update("tai_khoan", v, "id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(id)}) == 1;
    }

    public String doiMatKhauDaDangNhap(long id, String matKhauCu, String matKhauMoi) {
        if (!PhienDangNhap.coQuyenHoSo(context,helper.getReadableDatabase(),id,false)) return null;
        if (id <= 0 || matKhauMoi.length() < 8 || matKhauMoi.equals(matKhauCu)) return null;
        try (Cursor c = helper.getReadableDatabase().query("tai_khoan", new String[]{"muoi","mat_khau_bam"},
                "id=? AND trang_thai='ACTIVE'", new String[]{String.valueOf(id)}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            String hashCu = c.getString(1);
            if (!bangNhau(bam(matKhauCu, Base64.decode(c.getString(0), Base64.NO_WRAP)), hashCu)) return null;
            byte[] salt = muoiMoi();
            String maMoi = taoMaKhoiPhuc();
            ContentValues v = new ContentValues();
            v.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            v.put("mat_khau_bam", bam(matKhauMoi, salt));
            v.put("ma_khoi_phuc_bam", bamMa(maMoi, salt));
            return helper.getWritableDatabase().update("tai_khoan", v, "id=? AND mat_khau_bam=? AND trang_thai='ACTIVE'",
                    new String[]{String.valueOf(id), hashCu}) == 1 ? maMoi : null;
        } catch (Exception e) { return null; }
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

    public boolean dangKy(String ten, String matKhau, String maKhoiPhuc) {
        try {
            byte[] salt = muoiMoi();
            ContentValues cv = new ContentValues();
            cv.put("ten_dang_nhap", chuanHoa(ten));
            cv.put("vai_tro", HoSoTaiKhoan.USER);
            cv.put("trang_thai", HoSoTaiKhoan.ACTIVE);
            cv.put("ngay_tao", System.currentTimeMillis());
            cv.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhau, salt));
            cv.put("ma_khoi_phuc_bam", bamMa(maKhoiPhuc, salt));
            return helper.getWritableDatabase().insert("tai_khoan", null, cv) != -1;
        } catch (Exception ex) { return false; }
    }

    public boolean dangKy(String ten, String matKhau) {
        try {
            byte[] salt = muoiMoi(); ContentValues cv = new ContentValues();
            cv.put("ten_dang_nhap", chuanHoa(ten));
            cv.put("vai_tro", HoSoTaiKhoan.USER);
            cv.put("trang_thai", HoSoTaiKhoan.ACTIVE);
            cv.put("ngay_tao", System.currentTimeMillis());
            cv.put("muoi", Base64.encodeToString(salt, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhau, salt));
            return helper.getWritableDatabase().insert("tai_khoan", null, cv) != -1;
        } catch (Exception ex) { return false; }
    }

    public long dangNhap(String ten, String matKhau) {
        try (Cursor c = helper.getReadableDatabase().query("tai_khoan", new String[]{"id","muoi","mat_khau_bam"}, "ten_dang_nhap=? AND trang_thai='ACTIVE'", new String[]{chuanHoa(ten)}, null,null,null)) {
            if (!c.moveToFirst()) return -1;
            byte[] salt = Base64.decode(c.getString(1), Base64.NO_WRAP);
            return bangNhau(bam(matKhau,salt), c.getString(2)) ? c.getLong(0) : -1;
        } catch (Exception ex) { return -1; }
    }

    public String doiMatKhauBangMa(String ten, String maKhoiPhuc, String matKhauMoi) {
        if (matKhauMoi.length() < 8 || maKhoiPhuc.trim().isEmpty()) return null;
        try (Cursor c = helper.getReadableDatabase().query("tai_khoan", new String[]{"id","muoi","ma_khoi_phuc_bam"}, "ten_dang_nhap=? AND trang_thai='ACTIVE'", new String[]{chuanHoa(ten)},null,null,null)) {
            if (!c.moveToFirst() || c.isNull(2)) return null;
            byte[] saltCu = Base64.decode(c.getString(1), Base64.NO_WRAP);
            if (!bangNhau(bamMa(maKhoiPhuc,saltCu), c.getString(2))) return null;
            byte[] saltMoi = muoiMoi();
            ContentValues cv = new ContentValues();
            cv.put("muoi", Base64.encodeToString(saltMoi, Base64.NO_WRAP));
            cv.put("mat_khau_bam", bam(matKhauMoi,saltMoi));
            String maMoi = taoMaKhoiPhuc();
            cv.put("ma_khoi_phuc_bam", bamMa(maMoi,saltMoi));
            return helper.getWritableDatabase().update("tai_khoan",cv,
                    "id=? AND muoi=? AND ma_khoi_phuc_bam=? AND trang_thai='ACTIVE'",
                    new String[]{String.valueOf(c.getLong(0)),c.getString(1),c.getString(2)}) == 1 ? maMoi : null;
        } catch (Exception ex) { return null; }
    }

    public boolean capNhatMaKhoiPhuc(long id, String matKhauHienTai, String maMoi) {
        if (!PhienDangNhap.coQuyenHoSo(context,helper.getReadableDatabase(),id,false)) return false;
        try (Cursor c = helper.getReadableDatabase().query("tai_khoan",new String[]{"muoi","mat_khau_bam"},"id=? AND trang_thai='ACTIVE'",new String[]{String.valueOf(id)},null,null,null)) {
            if (!c.moveToFirst()) return false;
            byte[] salt = Base64.decode(c.getString(0),Base64.NO_WRAP);
            if (!bangNhau(bam(matKhauHienTai,salt),c.getString(1))) return false;
            ContentValues cv = new ContentValues(); cv.put("ma_khoi_phuc_bam",bamMa(maMoi,salt));
            return helper.getWritableDatabase().update("tai_khoan",cv,"id=? AND mat_khau_bam=? AND trang_thai='ACTIVE'",
                    new String[]{String.valueOf(id),c.getString(1)}) == 1;
        } catch (Exception ex) { return false; }
    }

    public ArrayList<HoSoTaiKhoan> danhSachTaiKhoan(String tuKhoa) {
        return KhoQuanTriTaiKhoan.danhSach(context,helper.getReadableDatabase(),tuKhoa);
    }

    public HoSoTaiKhoan chiTietTaiKhoan(long id) {
        return KhoQuanTriTaiKhoan.chiTiet(context,helper.getReadableDatabase(),id);
    }

    public boolean datKhoaTaiKhoan(long id,boolean khoa) {
        return KhoQuanTriTaiKhoan.datKhoa(context,helper.getWritableDatabase(),id,khoa);
    }
}
