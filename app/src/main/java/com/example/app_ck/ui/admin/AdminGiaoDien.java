package com.example.app_ck.ui.admin;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

import com.example.app_ck.R;

public final class AdminGiaoDien {

    private static final int XANH =
            Color.parseColor("#2C7351");

    private static final int CHU =
            Color.parseColor("#182D22");

    private static final int PHU =
            Color.parseColor("#687B6E");

    private static final int VIEN =
            Color.parseColor("#DFE9E1");

    private AdminGiaoDien() {
    }

    /** Chỉ áp dụng cho bốn màn quản trị mới; không đổi inset của màn khác. */
    public static void hoTroBanPhim(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View root = activity.findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
            View description = activity.findViewById(R.id.adminDescription);
            if (description != null) description.setVisibility(
                    insets.isVisible(WindowInsetsCompat.Type.ime()) ? View.GONE : View.VISIBLE);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static void anBanPhim(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus == null) return;
        InputMethodManager input = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null) input.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        focus.clearFocus();
    }

    public static ArrayAdapter<String> adapter(Context context, List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_admin_spinner, values);
        adapter.setDropDownViewResource(R.layout.item_admin_spinner_dropdown);
        return adapter;
    }

    public static void header(Activity activity, String title, String description, Runnable back) {
        ((TextView) activity.findViewById(R.id.adminHeading)).setText(title);
        ((TextView) activity.findViewById(R.id.adminDescription)).setText(description);
        activity.findViewById(R.id.adminBack).setOnClickListener(v -> back.run());
    }

    public static void hoiBoThayDoi(Context context, Runnable discard, Runnable cancel) {
        androidx.appcompat.app.AlertDialog confirm = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_Admin_Dialog)
                .setTitle("Thay đổi chưa được lưu")
                .setMessage("Bạn muốn bỏ các thay đổi vừa nhập?")
                .setNegativeButton("Tiếp tục sửa", (dialog, which) -> { if (cancel != null) cancel.run(); })
                .setPositiveButton("Bỏ thay đổi", (dialog, which) -> discard.run())
                .setOnCancelListener(dialog -> { if (cancel != null) cancel.run(); })
                .show();
        toMauNguyHiem(confirm);
    }

    public static void toMauNguyHiem(androidx.appcompat.app.AlertDialog dialog) {
        Button positive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        positive.setTextColor(dialog.getContext().getColor(R.color.admin_danger));
        positive.setBackgroundTintList(ColorStateList.valueOf(dialog.getContext().getColor(R.color.admin_danger_background)));
    }

    public static void hieuUngNhan(View view) {
        android.util.TypedValue value = new android.util.TypedValue();
        view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
        view.setForeground(androidx.appcompat.content.res.AppCompatResources.getDrawable(view.getContext(), value.resourceId));
    }

    public static void apDung(Activity activity) {
        View root = activity.findViewById(
                android.R.id.content);

        if (root != null) {
            xuLyView(activity, root);
        }
    }

    private static void xuLyView(
            Activity activity, View view) {

        if (view instanceof EditText) {
            chuanHoaOThongTin(
                    activity, (EditText) view);

        } else if (view instanceof Button) {
            chuanHoaNut(
                    activity, (Button) view);

        } else if (view instanceof Spinner) {
            view.setMinimumHeight(dp(activity, 48));

        } else if (view instanceof TextView
                && view.isClickable()) {
            view.setMinimumHeight(dp(activity, 48));
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                xuLyView(activity, group.getChildAt(i));
            }
        }
    }

    private static void chuanHoaOThongTin(
            Context context, EditText input) {

        // Cac o tim kiem da co khung ngoai rieng.
        // Khong chen them vien ben trong khung tim.
        int id = input.getId();

        if (id == R.id.qlddTimKiem
                || id == R.id.dtTimKiem
                || id == R.id.qltkTimKiem) {
            input.setTextColor(CHU);
            input.setHintTextColor(PHU);
            return;
        }

        StateListDrawable nen = new StateListDrawable();

        nen.addState(
                new int[]{android.R.attr.state_focused},
                taoNen(context, XANH)
        );

        nen.addState(
                new int[]{},
                taoNen(context, VIEN)
        );

        input.setBackground(nen);
        input.setBackgroundTintList(null);

        int ngang = dp(context, 14);
        int doc = dp(context, 12);

        input.setPadding(
                ngang, doc, ngang, doc
        );

        input.setMinimumHeight(dp(context, 52));

        input.setTextColor(CHU);
        input.setHintTextColor(PHU);
    }

    private static void chuanHoaNut(
            Context context, Button button) {

        button.setAllCaps(false);
        button.setMinimumHeight(dp(context, 48));

        button.setTypeface(
                Typeface.create(
                        "sans-serif-medium",
                        Typeface.NORMAL
                )
        );

        // Khong ghi de backgroundTint da cai dat
        // vi nut Xoa, Huy, Luu co y nghia rieng.
    }

    private static GradientDrawable taoNen(
            Context context, int mauVien) {

        GradientDrawable nen = new GradientDrawable();

        nen.setColor(Color.WHITE);
        nen.setCornerRadius(dp(context, 14));

        nen.setStroke(dp(context, 1.5f), mauVien);

        return nen;
    }

    private static int dp(
            Context context, float giaTri) {

        return Math.round(
                giaTri * context.getResources()
                        .getDisplayMetrics().density
        );
    }
}
