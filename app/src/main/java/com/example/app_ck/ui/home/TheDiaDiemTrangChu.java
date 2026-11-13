package com.example.app_ck.ui.home;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.app_ck.R;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.ui.common.AnhDiaDiem;

/** Hien thi the anh cua moi dia diem, ke ca dia diem Admin them. */
public final class TheDiaDiemTrangChu {

    public interface KhiChonDiaDiem {
        void moChiTiet(int idDiaDiem);
    }

    private TheDiaDiemTrangChu() {}

    private static int dp(Activity activity, float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    public static void hienThi(
            Activity activity,
            LinearLayout danhSach,
            DiaDiem[] cacDiaDiem,
            KhiChonDiaDiem suKien
    ) {
        if (activity == null || danhSach == null || cacDiaDiem == null) return;

        danhSach.removeAllViews();

        for (DiaDiem d : cacDiaDiem) {
            if (d == null) continue;

            FrameLayout the = new FrameLayout(activity);
            the.setBackgroundResource(R.drawable.ui_home_feature_card);
            the.setClipToOutline(true);
            the.setElevation(dp(activity, 2));
            the.setClickable(true);
            the.setFocusable(true);
            the.setContentDescription("Xem chi tiết " + d.ten);
            the.setOnClickListener(v -> suKien.moChiTiet(d.id));

            LinearLayout.LayoutParams theParams = new LinearLayout.LayoutParams(
                    dp(activity, 182), dp(activity, 230)
            );
            theParams.setMarginEnd(dp(activity, 12));
            danhSach.addView(the, theParams);

            ImageView anh = new ImageView(activity);
            anh.setScaleType(ImageView.ScaleType.CENTER_CROP);
            anh.setContentDescription("Ảnh " + d.ten);
            AnhDiaDiem.hien(anh, d.tenAnh);
            the.addView(anh, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            View lopPhu = new View(activity);
            lopPhu.setBackgroundResource(R.drawable.ui_home_feature_gradient);
            the.addView(lopPhu, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            LinearLayout thongTin = new LinearLayout(activity);
            thongTin.setOrientation(LinearLayout.VERTICAL);
            thongTin.setPadding(
                    dp(activity, 14), dp(activity, 12),
                    dp(activity, 14), dp(activity, 17)
            );
            FrameLayout.LayoutParams thongTinParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
            );
            the.addView(thongTin, thongTinParams);

            TextView ten = new TextView(activity);
            ten.setText(d.ten);
            ten.setTextSize(17);
            ten.setTextColor(Color.WHITE);
            ten.setTypeface(Typeface.create("serif", Typeface.BOLD));
            ten.setMaxLines(2);
            ten.setEllipsize(TextUtils.TruncateAt.END);
            thongTin.addView(ten, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            TextView danhMuc = new TextView(activity);
            String moTa = d.danhMuc != null && !d.danhMuc.trim().isEmpty()
                    ? d.danhMuc : d.diaChi;
            danhMuc.setText(moTa == null ? "" : moTa);
            danhMuc.setTextSize(12);
            danhMuc.setTextColor(Color.parseColor("#E2ECE5"));
            danhMuc.setMaxLines(1);
            danhMuc.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams moTaParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            moTaParams.topMargin = dp(activity, 5);
            thongTin.addView(danhMuc, moTaParams);
        }
    }
}
