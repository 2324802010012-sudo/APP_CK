package com.example.app_ck.ui.common;

import com.example.app_ck.R;
import com.example.app_ck.ui.home.KhamPhaActivity;
import com.example.app_ck.ui.home.MainActivity;
import com.example.app_ck.ui.profile.CaNhanActivity;
import com.example.app_ck.ui.profile.YeuThichActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** Cùng giao diện và cùng hành vi cho năm tab chính, không xếp chồng các bản sao của tab. */
public final class ThanhDieuHuongDuoi {
    public static final String TAB = "tab_dieu_huong";
    public static final int KHAM_PHA = 0, YEU_THICH = 1, TRANG_CHU = 2, CA_NHAN = 3, GOI_Y = 4;
    private static final int[] IDS = {R.id.navKhamPha, R.id.navYeuThich, R.id.navTrangChu, R.id.navCaNhan, R.id.navGoiY};
    private static final String[] NHAN = {"Khám phá", "Yêu thích", "Trang chủ", "Cá nhân", "Gợi ý"};
    private ThanhDieuHuongDuoi() {}

    public static void gan(BaseActivity activity, int tabHienTai) {
        activity.apDungKhoangTrongHeThong();
        for (int i = 0; i < IDS.length; i++) {
            View view = activity.findViewById(IDS[i]);
            if (view == null) continue;
            final int tab = i;
            view.setSelected(i == tabHienTai);
            view.setContentDescription(NHAN[i] + (i == tabHienTai ? ", đang chọn" : ""));
            toMau(view, i == tabHienTai, i == TRANG_CHU);
            view.setOnClickListener(v -> { if (tab == KHAM_PHA || tab != tabHienTai) mo(activity, tab); });
        }
    }

    private static void toMau(View view, boolean chon, boolean nutTrangChu) {
        int mau = Color.parseColor(chon ? "#2C7351" : "#6F806F");
        if (view instanceof ImageView) {
            ((ImageView) view).setImageTintList(ColorStateList.valueOf(nutTrangChu ? Color.WHITE : mau));
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(mau);
            ((TextView) view).setTypeface(null, chon ? Typeface.BOLD : Typeface.NORMAL);
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (view instanceof ViewGroup) for (int j = 0; j < ((ViewGroup) view).getChildCount(); j++)
            toMau(((ViewGroup) view).getChildAt(j), chon, nutTrangChu);
    }

    public static void mo(BaseActivity activity, int tab) {
        if (tab == KHAM_PHA && activity instanceof MainActivity) {
            ((MainActivity)activity).cuonLenTimKiem();
            return;
        }
        Class<?> lop = tab == TRANG_CHU || tab == KHAM_PHA ? MainActivity.class : tab == YEU_THICH ? YeuThichActivity.class
                : tab == CA_NHAN ? CaNhanActivity.class : KhamPhaActivity.class;
        activity.startActivity(new Intent(activity, lop).putExtra(TAB, tab)
                .putExtra(MainActivity.CUON_LEN_TIM_KIEM, tab == KHAM_PHA)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        if (!(activity instanceof MainActivity) && activity.getClass() != lop) activity.finish();
    }
}
