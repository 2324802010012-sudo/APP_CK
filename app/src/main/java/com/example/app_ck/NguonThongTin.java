package com.example.app_ck;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

/** Danh sách trích dẫn được mở riêng để giọng đọc không đọc các URL. */
final class NguonThongTin {
    private NguonThongTin() {}

    static void hien(Activity activity, int diaDiem, int diem) {
        DuLieuNoiDungThamQuan.Bai bai = KhoNoiDungThamQuan.lay(diaDiem, diem);
        android.widget.LinearLayout khung = new android.widget.LinearLayout(activity);
        khung.setOrientation(android.widget.LinearLayout.VERTICAL);
        int le = Math.round(24 * activity.getResources().getDisplayMetrics().density);
        khung.setPadding(le, le / 2, le, le / 2);
        TextView ghiChu = new TextView(activity);
        ghiChu.setText("Đối chiếu: " + DuLieuNoiDungThamQuan.NGAY_DOI_CHIEU
                + "\nBài được biên soạn lại từ các nguồn dưới đây. Gợi ý tham quan do ứng dụng biên soạn."
                + (bai.ghiChu.isEmpty() ? "" : "\n\n" + bai.ghiChu));
        ghiChu.setTextColor(android.graphics.Color.parseColor("#24382D"));
        khung.addView(ghiChu);
        for (String maNguon : bai.nguon) {
            DuLieuNoiDungThamQuan.Nguon s = DuLieuNoiDungThamQuan.NGUON.get(maNguon);
            if (s == null) throw new IllegalStateException("Thiếu nguồn " + maNguon);
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
                .setTitle("Nguồn thông tin – " + bai.ten)
                .setView(cuon).setPositiveButton("Đóng", null).show();
    }
}
