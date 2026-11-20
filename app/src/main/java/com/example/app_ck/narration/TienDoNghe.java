package com.example.app_ck.narration;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.session.PhienDangNhap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/** Lưu tiến độ theo tài khoản, cặp địa điểm–điểm và phiên bản nội dung. */
public final class TienDoNghe {
    private final Context context;
    private final SharedPreferences luuTru;
    private String ma;
    private long taiKhoan;

    public TienDoNghe(Context context) {
        this.context = context.getApplicationContext();
        luuTru = this.context.getSharedPreferences("tien_do_thuyet_minh", Context.MODE_PRIVATE);
    }

    public int mo(int diaDiem, int diem, String cheDo, String noiDung) {
        ma = null;
        taiKhoan = PhienDangNhap.id(context);
        if (taiKhoan > 0 && diaDiem >= 0 && diem >= 0 && KhoLichSu.THU_CONG.equals(cheDo)) {
            ma = taiKhoan + "_" + diaDiem + "_" + diem + "_" + maHoaNoiDung(noiDung);
        }
        return ma == null ? 0 : luuTru.getInt(ma, 0);
    }

    public boolean dungPhien() {
        return ma != null && taiKhoan == PhienDangNhap.id(context);
    }

    public void luu(int viTri) {
        if (!dungPhien()) return;
        if (viTri >= 3000) luuTru.edit().putInt(ma, viTri).apply();
        else luuTru.edit().remove(ma).apply();
    }

    public void xoa() {
        if (dungPhien()) luuTru.edit().remove(ma).apply();
    }

    private static String maHoaNoiDung(String text) {
        try {
            byte[] data = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : data) result.append(String.format(Locale.ROOT, "%02x", b & 0xff));
            return result.toString();
        } catch (Exception e) {
            return Integer.toHexString(text.hashCode()) + "_" + text.length();
        }
    }
}
