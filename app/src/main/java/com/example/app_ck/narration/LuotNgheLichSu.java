package com.example.app_ck.narration;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.session.PhienDangNhap;

import android.content.Context;
import java.util.UUID;

/** Một lượt nghe: chụp tài khoản lúc yêu cầu phát, chỉ ghi một lần khi âm thanh bắt đầu. */
public final class LuotNgheLichSu {
    private final long taiKhoan;
    private final int diaDiem, diem;
    private final String cheDo, maLuot = UUID.randomUUID().toString();
    private boolean daXuLy;

    public LuotNgheLichSu(Context context, int diaDiem, int diem, String cheDo) {
        taiKhoan = PhienDangNhap.id(context);
        this.diaDiem = diaDiem; this.diem = diem; this.cheDo = cheDo;
    }

    public void batDau(Context context) {
        if (daXuLy) return;
        daXuLy = true;
        if (taiKhoan <= 0 || PhienDangNhap.id(context) != taiKhoan) return;
        try (AppDatabase db = new AppDatabase(context)) {
            KhoLichSu.ghi(db.getWritableDatabase(), taiKhoan, diaDiem, diem, cheDo,
                    System.currentTimeMillis(), maLuot);
        } catch (Exception e) {
            android.util.Log.e("LichSuNghe", "Không lưu được lượt nghe", e);
        }
    }
}
