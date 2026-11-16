package com.example.app_ck.ui.common;

import android.view.View;
import android.widget.ImageView;

public final class BoHienThiAnh {

    private BoHienThiAnh() {}

    // Hien thi anh dai dien cua dia diem chinh.
    public static void hienAnhDiaDiem(
            ImageView anh,
            String tenAnh
    ) {

        if (anh == null) return;

        AnhDiaDiem.hien(anh, tenAnh);

        anh.setVisibility(View.VISIBLE);
    }

    // Hien thi anh rieng cua diem tham quan.
    public static void hienAnhDiem(
            ImageView anh,
            String tenAnh
    ) {

        if (anh == null) return;

        boolean thanhCong =
                AnhDiaDiem.ganNeuCo(anh, tenAnh);

        if (thanhCong) {

            anh.setVisibility(View.VISIBLE);

        } else {

            anh.setImageDrawable(null);
            anh.setVisibility(View.GONE);
        }
    }

    // Dung cho the diem noi bat.
    // Neu diem khong co anh rieng,
    // hien thi anh cua dia diem chinh.
    public static void hienAnhDiemCoDuPhong(
            ImageView anh,
            String tenAnhDiem,
            String tenAnhDiaDiem
    ) {

        if (anh == null) return;

        boolean coAnh =
                AnhDiaDiem.ganNeuCo(
                        anh,
                        tenAnhDiem
                );

        if (coAnh) {

            anh.setVisibility(View.VISIBLE);

        } else {

            hienAnhDiaDiem(
                    anh,
                    tenAnhDiaDiem
            );
        }
    }
}