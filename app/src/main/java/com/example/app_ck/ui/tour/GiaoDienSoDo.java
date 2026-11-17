
package com.example.app_ck.ui.tour;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.app_ck.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class GiaoDienSoDo {

    private static final int XANH = Color.parseColor("#2C7351");
    private static final int VANG = Color.parseColor("#A66C25");
    private static final int CHU_PHU = Color.parseColor("#687B6E");

    private GiaoDienSoDo() {
    }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources()
                .getDisplayMetrics().density);
    }

    // VE MARKER DONG BO

    public static void veMarker(
            Context context,
            Canvas canvas,
            Paint paint,
            float x,
            float y,
            int so,
            boolean dangChon,
            boolean matDoCao,
            float zoom) {

        float tyLe = Math.max(1f, zoom);

        float radius = dp(context, matDoCao ? 9f : 11f)
                / tyLe;

        if (dangChon) {
            radius *= 1.22f;
        }

        float vien = dp(context, 1.7f) / tyLe;

        paint.setStyle(Paint.Style.FILL);

        // Vong sang cua marker dang chon

        if (dangChon) {

            paint.setColor(Color.parseColor("#33A66C25"));

            canvas.drawCircle(
                    x, y, radius * 1.4f, paint
            );
        }

        // Vien trang

        paint.setColor(Color.WHITE);

        canvas.drawCircle(x, y, radius, paint);

        // Mau marker

        paint.setColor(dangChon ? VANG : XANH);

        canvas.drawCircle(
                x, y, Math.max(0, radius - vien), paint
        );

        // So marker

        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(
                radius * (so >= 10 ? 1.03f : 1.18f)
        );

        canvas.drawText(
                String.valueOf(so),
                x,
                y - (paint.ascent() + paint.descent()) / 2f,
                paint
        );
    }

    // VUNG CHAM KHONG PHU THUOC KICH THUOC MARKER

    public static float banKinhCham(
            Context context, float zoom) {

        return dp(context, 28f) / Math.max(1f, zoom);
    }

    // THEM SO DO VA THANH CONG CU

    public static void themVao(
            LinearLayout cha,
            View soDo,
            int chieuCao,
            Runnable thuNho,
            Runnable datLai,
            Runnable phongTo) {

        Context context = cha.getContext();

        soDo.setBackgroundResource(R.drawable.ui_ct_thumb);

        soDo.setClipToOutline(true);

        cha.addView(
                soDo,
                new LinearLayout.LayoutParams(-1, chieuCao)
        );

        LinearLayout thanh = new LinearLayout(context);

        thanh.setOrientation(LinearLayout.HORIZONTAL);
        thanh.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams lpThanh =
                new LinearLayout.LayoutParams(-1, -2);

        lpThanh.topMargin = dp(context, 8);

        cha.addView(thanh, lpThanh);

        ImageButton nutTru = taoNut(
                context,
                R.drawable.ui_tour_minus,
                "Thu nhỏ sơ đồ",
                thuNho
        );

        ImageButton nutDatLai = taoNut(
                context,
                R.drawable.ui_tour_reset,
                "Đặt lại sơ đồ",
                datLai
        );

        ImageButton nutCong = taoNut(
                context,
                R.drawable.ui_tour_plus,
                "Phóng to sơ đồ",
                phongTo
        );

        themNut(thanh, nutTru);
        themNut(thanh, nutDatLai);
        themNut(thanh, nutCong);

        GiaoDienTour tour = GiaoDienTour.lay(context);

        ImageButton nutMoRong = null;

        if (tour != null) {

            nutMoRong = taoNut(
                    context,
                    R.drawable.ui_tour_expand,
                    "Mở sơ đồ toàn màn hình",
                    tour::batToanManHinh
            );

            themNut(thanh, nutMoRong);
        }

        themChuGiai(cha);

        if (tour != null) {

            tour.dangKySoDo(soDo, nutMoRong);
        }
    }

    private static void themNut(
            LinearLayout thanh,
            ImageButton nut) {

        Context context = thanh.getContext();

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0, dp(context, 48), 1f
                );

        lp.setMargins(
                dp(context, 3),
                0,
                dp(context, 3),
                0
        );

        thanh.addView(nut, lp);
    }

    public static ImageButton taoNut(
            Context context,
            int icon,
            String moTa,
            Runnable lenh) {

        ImageButton nut = new ImageButton(context);

        nut.setImageResource(icon);

        nut.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        nut.setBackgroundResource(R.drawable.ui_ct_audio_bg);

        int padding = dp(context, 12);

        nut.setPadding(
                padding, padding, padding, padding
        );

        nut.setContentDescription(moTa);

        nut.setClickable(true);
        nut.setFocusable(true);

        if (Build.VERSION.SDK_INT >= 26) {

            nut.setTooltipText(moTa);
        }

        TypedValue value = new TypedValue();

        if (context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                value,
                true
        ) && value.resourceId != 0) {

            nut.setForeground(
                    context.getDrawable(value.resourceId)
            );
        }

        nut.setOnClickListener(v -> lenh.run());

        return nut;
    }

    // CHU GIAI NGAN GON

    private static void themChuGiai(LinearLayout cha) {

        Context context = cha.getContext();

        LinearLayout hang = new LinearLayout(context);

        hang.setOrientation(LinearLayout.HORIZONTAL);
        hang.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, -2);

        lp.topMargin = dp(context, 3);

        cha.addView(hang, lp);

        hang.addView(
                taoChuGiai(context, "Điểm tham quan", XANH),
                new LinearLayout.LayoutParams(0, -2, 1f)
        );

        hang.addView(
                taoChuGiai(context, "Đang chọn", VANG),
                new LinearLayout.LayoutParams(0, -2, 1f)
        );

        ImageButton thongTin = taoNut(
                context,
                R.drawable.ui_tour_info,
                "Hướng dẫn sử dụng sơ đồ",
                () -> moHuongDan(context)
        );

        thongTin.setBackgroundColor(Color.TRANSPARENT);

        hang.addView(
                thongTin,
                new LinearLayout.LayoutParams(
                        dp(context, 48),
                        dp(context, 48)
                )
        );
    }

    private static TextView taoChuGiai(
            Context context, String text, int color) {

        TextView tv = new TextView(context);

        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(CHU_PHU);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable cham = new GradientDrawable();

        cham.setShape(GradientDrawable.OVAL);
        cham.setColor(color);

        int size = dp(context, 10);

        cham.setBounds(0, 0, size, size);

        tv.setCompoundDrawablesRelative(
                cham, null, null, null
        );

        tv.setCompoundDrawablePadding(dp(context, 6));

        return tv;
    }

    private static void moHuongDan(Context context) {

        new MaterialAlertDialogBuilder(context)
                .setTitle("Sử dụng sơ đồ")
                .setMessage(
                        "• Chạm vào marker để chọn điểm tham quan.\n\n"
                                + "• Nhấn nút Nghe thuyết minh để nghe điểm đang chọn.\n\n"
                                + "• Dùng hai ngón tay hoặc các nút để thu phóng.\n\n"
                                + "• Khi đã phóng to, kéo để di chuyển sơ đồ.\n\n"
                                + "• Tọa độ trên sơ đồ độc lập với GPS thực tế.\n\n"
                                + "• Một số ảnh sơ đồ gốc có số được in sẵn."
                )
                .setPositiveButton("Đã hiểu", null)
                .show();
    }
}
