
package com.example.app_ck.ui.tour;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.widget.TextView;

import com.example.app_ck.R;

public final class GiaoDienTheThamQuan {

    private GiaoDienTheThamQuan() {
    }

    private static int dp(Context context, int value) {
        return Math.round(
                value * context.getResources()
                        .getDisplayMetrics().density
        );
    }

    private static int mau(String value) {
        return Color.parseColor(value);
    }

    private static RippleDrawable nen(
            Context context,
            String mauNen,
            String mauVien,
            int boGoc
    ) {

        GradientDrawable shape = new GradientDrawable();

        shape.setColor(mau(mauNen));

        shape.setCornerRadius(dp(context, boGoc));

        shape.setStroke(
                dp(context, 1),
                mau(mauVien)
        );

        return new RippleDrawable(
                ColorStateList.valueOf(mau("#192C7351")),
                shape,
                null
        );
    }

    public static void ganThe(
            TextView the,
            boolean dangChon
    ) {

        Context context = the.getContext();

        the.setBackground(
                nen(
                        context,
                        dangChon ? "#EAF3E8" : "#FFFFFF",
                        dangChon ? "#85B18E" : "#E0E9E0",
                        16
                )
        );

        the.setTextColor(
                mau(dangChon ? "#2C7351" : "#182D22")
        );

        the.setTypeface(
                Typeface.create(
                        "sans-serif-medium",
                        Typeface.NORMAL
                )
        );

        the.setTextSize(14);

        the.setLineSpacing(dp(context, 4), 1f);

        the.setMinHeight(dp(context, 70));

        the.setGravity(Gravity.CENTER_VERTICAL);

        the.setPadding(
                dp(context, 16),
                dp(context, 14),
                dp(context, 16),
                dp(context, 14)
        );

        the.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ui_ic_chevron,
                0
        );

        the.setCompoundDrawablePadding(dp(context, 10));

        the.setSelected(dangChon);

        the.setClickable(true);

        the.setFocusable(true);
    }

    public static void ganNutZoom(TextView nut) {

        Context context = nut.getContext();

        nut.setBackgroundTintList(null);

        nut.setBackground(
                nen(context, "#EDF5EC", "#E0E9E0", 12)
        );

        nut.setTextColor(mau("#2C7351"));

        nut.setTextSize(12);

        nut.setTypeface(
                Typeface.create(
                        "sans-serif-medium",
                        Typeface.NORMAL
                )
        );

        nut.setAllCaps(false);

        nut.setGravity(Gravity.CENTER);

        nut.setMinHeight(dp(context, 48));

        nut.setPadding(
                dp(context, 6),
                dp(context, 6),
                dp(context, 6),
                dp(context, 6)
        );
    }
}
