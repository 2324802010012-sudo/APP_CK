package com.example.app_ck.location;

import com.example.app_ck.ui.tour.ConDaoThamQuanActivity;

import android.view.ViewGroup;

/** Adapter giao diện Côn Đảo; toàn bộ nhận diện GPS, demo và điều khiển âm thanh dùng chung. */
public final class HuongDanConDao extends HuongDanTour {
    public HuongDanConDao(ConDaoThamQuanActivity activity, ViewGroup cha, CauHinhAutoDiSan cauHinh) {
        super(activity, cha, cauHinh, true);
    }
}
