package com.example.app_ck.ui.common;

import com.example.app_ck.R;
import com.example.app_ck.ui.home.KhamPhaActivity;
import com.example.app_ck.ui.home.MainActivity;
import com.example.app_ck.ui.map.BanDoActivity;
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

/** Dieu huong 5 man hinh: Kham pha - Yeu thich - Trang chu - Ca nhan - Ban do. */
public final class ThanhDieuHuongDuoi {
    public static final String TAB = "tab_dieu_huong";
    public static final int KHAM_PHA = 0;
    public static final int YEU_THICH = 1;
    public static final int TRANG_CHU = 2;
    public static final int CA_NHAN = 3;
    public static final int BAN_DO = 4;

    // Tuong thich voi ma cu, nhung tab so 4 hien nay la BAN_DO.
    @Deprecated public static final int GOI_Y = BAN_DO;

    private static final int[] IDS = {
            R.id.navKhamPha, R.id.navYeuThich, R.id.navTrangChu,
            R.id.navCaNhan, R.id.navBanDo
    };
    private static final String[] NHAN = {
            "Khám phá", "Yêu thích", "Trang chủ", "Cá nhân", "Bản đồ"
    };

    private ThanhDieuHuongDuoi() {}

    public static void gan(BaseActivity activity, int tabHienTai) {
        activity.apDungKhoangTrongHeThong();
        for (int i = 0; i < IDS.length; i++) {
            View view = activity.findViewById(IDS[i]);
            if (view == null) continue;
            final int tab = i;
            boolean dangChon = tab == tabHienTai;
            view.setSelected(dangChon);
            view.setContentDescription(NHAN[i] + (dangChon ? ", đang chọn" : ""));
            toMau(view, dangChon, tab == TRANG_CHU);
            view.setOnClickListener(v -> mo(activity, tab));
        }
    }

    private static void toMau(View view, boolean dangChon, boolean nutTrangChu) {
        int mau = Color.parseColor(dangChon ? "#286C49" : "#748578");
        if (view instanceof ImageView) {
            ((ImageView) view).setImageTintList(
                    ColorStateList.valueOf(nutTrangChu ? Color.WHITE : mau));
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(mau);
            ((TextView) view).setTypeface(null, dangChon ? Typeface.BOLD : Typeface.NORMAL);
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int j = 0; j < group.getChildCount(); j++) {
                toMau(group.getChildAt(j), dangChon, nutTrangChu);
            }
        }
    }

    public static void mo(BaseActivity activity, int tab) {
        Class<?> lop;
        switch (tab) {
            case KHAM_PHA: lop = KhamPhaActivity.class; break;
            case YEU_THICH: lop = YeuThichActivity.class; break;
            case CA_NHAN: lop = CaNhanActivity.class; break;
            case BAN_DO: lop = BanDoActivity.class; break;
            case TRANG_CHU:
            default: lop = MainActivity.class; break;
        }

        // Khong tao nhieu ban sao Activity khi bam tab dang xem.
        if (lop.isInstance(activity)) {
            if (activity instanceof MainActivity) ((MainActivity) activity).cuonLenTimKiem();
            return;
        }

        Intent intent = new Intent(activity, lop)
                .putExtra(TAB, tab)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        // Giup back stack gon khi di chuyen giua cac tab.
        // MainActivity duoc giu lam man hinh goc.
        if (!(activity instanceof MainActivity)) activity.finish();
    }
}
