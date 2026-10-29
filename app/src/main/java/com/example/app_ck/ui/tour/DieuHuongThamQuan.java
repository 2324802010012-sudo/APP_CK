package com.example.app_ck.ui.tour;

import android.content.Context;
import android.content.Intent;

/** Chọn giao diện theo mã địa điểm; mã điểm luôn truyền nguyên vẹn. */
public final class DieuHuongThamQuan {
    private DieuHuongThamQuan() {}

    public static Intent taoIntent(Context context, int diaDiem, int diem) {
        Class<?> activity;
        switch (diaDiem) {
            case 1: activity = BaoTangThamQuanActivity.class; break;
            case 2: activity = DaiNoiHueThamQuanActivity.class; break;
            case 3: activity = ConDaoThamQuanActivity.class; break;
            case 4: activity = VanMieuThamQuanActivity.class; break;
            default: activity = ThamQuanActivity.class;
        }
        return new Intent(context, activity).putExtra("viTriDiaDiem", diaDiem)
                .putExtra("idMarker", diem);
    }
}
