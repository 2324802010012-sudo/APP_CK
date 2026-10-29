package com.example.app_ck.ui.common;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.BaiThuyetMinh;
import com.example.app_ck.data.repository.KhoNguonThongTin;
import com.example.app_ck.data.repository.KhoNoiDungThamQuan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

/** Danh sách trích dẫn được mở riêng để giọng đọc không đọc các URL. */
public final class NguonThongTin {
    private NguonThongTin() {}

    public static void hien(Activity activity, int diaDiem, int diem) {
        final String ten, ghiChuNguon;
        final java.util.List<KhoNguonThongTin.Nguon> cacNguon;
        try (AppDatabase helper = new AppDatabase(activity)) {
            BaiThuyetMinh bai = KhoNoiDungThamQuan.lay(helper.getReadableDatabase(), diaDiem, diem);
            ten = bai.ten;
            ghiChuNguon = KhoNguonThongTin.ghiChu(helper.getReadableDatabase(), diaDiem, diem);
            cacNguon = KhoNguonThongTin.danhSach(helper.getReadableDatabase(), diaDiem, diem);
        }
        android.widget.LinearLayout khung = new android.widget.LinearLayout(activity);
        khung.setOrientation(android.widget.LinearLayout.VERTICAL);
        int le = Math.round(24 * activity.getResources().getDisplayMetrics().density);
        khung.setPadding(le, le / 2, le, le / 2);
        TextView ghiChu = new TextView(activity);
        ghiChu.setText(ghiChuNguon);
        ghiChu.setTextColor(android.graphics.Color.parseColor("#24382D"));
        khung.addView(ghiChu);
        for (KhoNguonThongTin.Nguon s : cacNguon) {
            TextView link = new TextView(activity);
            link.setText("\n" + s.ten + " ↗\n" + s.coQuan + "\n" + s.url);
            link.setTextColor(android.graphics.Color.parseColor("#176D53"));
            link.setPadding(0, le / 2, 0, le / 2);
            link.setFocusable(true);
            link.setOnClickListener(v -> {
                try { activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(s.url))); }
                catch (ActivityNotFoundException e) {
                    Toast.makeText(activity, "Chưa có trình duyệt để mở nguồn.", Toast.LENGTH_SHORT).show();
                }
            });
            khung.addView(link);
        }
        android.widget.ScrollView cuon = new android.widget.ScrollView(activity);
        cuon.addView(khung);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                .setTitle("Nguồn thông tin – " + ten)
                .setView(cuon).setPositiveButton("Đóng", null).show();
    }
}
