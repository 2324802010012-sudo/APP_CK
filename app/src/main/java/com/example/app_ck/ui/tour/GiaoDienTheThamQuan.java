package com.example.app_ck.ui.tour;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.app_ck.R;
import com.example.app_ck.data.model.DiemThamQuan;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Dựng thẻ của giao diện tour hiện tại; sự kiện chọn và nghe là hai hành động riêng. */
public final class GiaoDienTheThamQuan {
    private static final int XANH = Color.parseColor("#2C7351");
    private static final int TIEU_DE = Color.parseColor("#182D22");
    private static final int CHU_PHU = Color.parseColor("#687B6E");
    private GiaoDienTheThamQuan() {}

    static View tao(
            Activity activity, DiemThamQuan diem, String khuVuc,
            int idDangChon, boolean coAuto, Runnable chon, Runnable nghe) {

        boolean dangChon =
                diem.id == idDangChon;


        boolean coBai =
                coNoiDung(diem.noiDung)
                        || coNoiDung(diem.moTa);

        // The ngoai

        LinearLayout the = new LinearLayout(activity);

        the.setOrientation(
                LinearLayout.HORIZONTAL
        );

        the.setGravity(
                Gravity.CENTER_VERTICAL
        );

        the.setPadding(
                dp(activity, 8), dp(activity, 6), dp(activity, 8), dp(activity, 6)
        );

        the.setMinimumHeight(dp(activity, 70));

        the.setBackground(
                nen(activity,
                        dangChon
                                ? "#E8F2E6"
                                : "#FFFFFF",
                        dangChon
                                ? "#88B18B"
                                : "#E0E9E0",
                        16
                )
        );

        LinearLayout.LayoutParams lpThe =
                new LinearLayout.LayoutParams(
                        -1, -2
                );

        lpThe.bottomMargin = dp(activity, 8);

        the.setLayoutParams(lpThe);

        // Vung bam chon diem

        LinearLayout vungChon =
                new LinearLayout(activity);

        vungChon.setOrientation(
                LinearLayout.HORIZONTAL
        );

        vungChon.setGravity(
                Gravity.CENTER_VERTICAL
        );

        vungChon.setMinimumHeight(dp(activity, 54));

        vungChon.setPadding(
                dp(activity, 4), dp(activity, 6), dp(activity, 8), dp(activity, 6)
        );

        vungChon.setBackground(
                nenCham(activity,
                        dangChon
                                ? "#E8F2E6"
                                : "#FFFFFF",
                        dangChon
                                ? "#E8F2E6"
                                : "#FFFFFF",
                        12
                )
        );

        the.addView(
                vungChon,
                new LinearLayout.LayoutParams(
                        0, -2, 1f
                )
        );

        // So marker

        String soMarker = diem.coMarker()
                ? String.format(
                Locale.ROOT,
                "%02d",
                diem.soHienThi > 0
                        ? diem.soHienThi
                        : diem.id
        )
                : "–";

        TextView so = taoChu(activity,
                soMarker,
                15,
                dangChon ? Color.WHITE : XANH,
                true
        );

        so.setGravity(Gravity.CENTER);

        so.setBackground(
                nen(activity,
                        dangChon
                                ? "#2C7351"
                                : "#EDF5EC",
                        dangChon
                                ? "#2C7351"
                                : "#EDF5EC",
                        12
                )
        );

        vungChon.addView(
                so,
                new LinearLayout.LayoutParams(
                        dp(activity, 40), dp(activity, 40)
                )
        );

        // Khu vuc noi dung

        LinearLayout noiDung =
                new LinearLayout(activity);

        noiDung.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams lpNoiDung =
                new LinearLayout.LayoutParams(
                        0, -2, 1f
                );

        lpNoiDung.leftMargin = dp(activity, 10);

        vungChon.addView(
                noiDung,
                lpNoiDung
        );

        TextView ten = taoChu(activity,
                diem.ten,
                14,
                TIEU_DE,
                true
        );

        noiDung.addView(ten);

        // Trang thai diem

        List<String> nhan = new ArrayList<>();

        if (khuVuc != null
                && !khuVuc.trim().isEmpty()) {

            nhan.add(khuVuc);
        }

        if (coAuto) {

            nhan.add("AUTO");
        }

        if (dangChon) {

            nhan.add("Đang chọn");
        }

        if (!diem.coMarker()) {

            nhan.add("Chọn từ danh sách");
        }

        if (!coBai) {

            nhan.add("Chưa có bài nghe");
        }

        if (!nhan.isEmpty()) {

            TextView trangThai = taoChu(activity,
                    TextUtils.join(" · ", nhan),
                    12,
                    dangChon || coAuto
                            ? XANH
                            : CHU_PHU,
                    false
            );

            trangThai.setPadding(
                    0, dp(activity, 4), 0, 0
            );

            noiDung.addView(trangThai);
        }

        // Bam ten chi chon diem

        vungChon.setClickable(true);
        vungChon.setFocusable(true);
        vungChon.setSelected(dangChon);

        vungChon.setContentDescription(
                "Chọn " + diem.ten
                        + (nhan.isEmpty()
                        ? ""
                        : ", " + TextUtils.join(", ", nhan))
        );

        vungChon.setOnClickListener(v -> {

            chon.run();
        });

        // Nut nghe nhanh doc lap

        ImageButton nutNghe =
                GiaoDienSoDo.taoNut(
                        activity,
                        R.drawable.ui_tour_headphones,
                        "Nghe " + diem.ten,
                        nghe
                );

        nutNghe.setEnabled(coBai);

        nutNghe.setAlpha(
                coBai ? 1f : 0.35f
        );

        LinearLayout.LayoutParams lpNghe =
                new LinearLayout.LayoutParams(
                        dp(activity, 48), dp(activity, 48)
                );

        lpNghe.leftMargin = dp(activity, 4);

        the.addView(
                nutNghe,
                lpNghe
        );

        return the;
    }

    private static boolean coNoiDung(String text) {

        return text != null
                && !text.trim().isEmpty();
    }

    static TextView taoChu(
            Activity activity, String text,
            int size,
            int color,
            boolean dam) {

        TextView tv = new TextView(activity);

        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(color);

        tv.setLineSpacing(
                dp(activity, 3), 1f
        );

        tv.setTypeface(
                Typeface.create(
                        dam
                                ? "sans-serif-medium"
                                : "sans-serif",
                        Typeface.NORMAL
                )
        );

        return tv;
    }

    private static GradientDrawable nen(
            Activity activity, String mau,
            String vien,
            int boGoc) {

        GradientDrawable d =
                new GradientDrawable();

        d.setColor(
                Color.parseColor(mau)
        );

        d.setCornerRadius(
                dp(activity, boGoc)
        );

        d.setStroke(
                dp(activity, 1),
                Color.parseColor(vien)
        );

        return d;
    }

    private static RippleDrawable nenCham(
            Activity activity, String mau,
            String vien,
            int boGoc) {

        return new RippleDrawable(
                ColorStateList.valueOf(
                        Color.parseColor("#192C7351")
                ),
                nen(activity, mau, vien, boGoc),
                null
        );
    }

    private static int dp(Activity activity, float value) {

        return Math.round(
                value * activity.getResources()
                        .getDisplayMetrics().density
        );
    }
}
