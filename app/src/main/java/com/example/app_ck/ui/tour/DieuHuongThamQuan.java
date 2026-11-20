package com.example.app_ck.ui.tour;

import android.content.Context;
import android.content.Intent;

/** Một đường vào tour chung; giữ nguyên tên extras của các Intent đã tồn tại. */
public final class DieuHuongThamQuan {
    public static final String DIA_DIEM = "viTriDiaDiem";
    public static final String DIEM = "idMarker";
    private DieuHuongThamQuan() {}

    public static Intent taoIntent(Context context, int diaDiem, int diem) {
        return new Intent(context, ThamQuanActivity.class).putExtra(DIA_DIEM, diaDiem)
                .putExtra(DIEM, diem);
    }
}
