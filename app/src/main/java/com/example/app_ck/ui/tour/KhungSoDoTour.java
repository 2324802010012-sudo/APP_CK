package com.example.app_ck.ui.tour;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.app_ck.R;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.data.model.DuLieuTour;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/** Sở hữu view/bitmap sơ đồ và thanh tầng; lựa chọn, xác nhận AUTO thuộc Activity. */
final class KhungSoDoTour {
    private final Activity activity;
    private final LinearLayout khung;
    private final CauHinhTour cauHinh;
    private final IntConsumer chonDiem;
    private final IntConsumer chonTang;
    private DuLieuTour duLieuDaVe;
    private int tangDaVe = Integer.MIN_VALUE;
    private SoDoHueView soDoHue;

    KhungSoDoTour(Activity activity, CauHinhTour cauHinh,
                  IntConsumer chonDiem, IntConsumer chonTang) {
        this.activity = activity;
        this.khung = activity.findViewById(R.id.tqKhungSoDo);
        this.cauHinh = cauHinh;
        this.chonDiem = chonDiem;
        this.chonTang = chonTang;
    }

    void capNhat(DuLieuTour duLieu, int tang, int idDangChon) {
        if (duLieuDaVe == duLieu && tangDaVe == tang) {
            if (soDoHue != null) soDoHue.chonMarker(idDangChon);
            else AnhSoDoTour.capNhatMarker(khung, idDangChon);
            return;
        }
        giaiPhong();
        duLieuDaVe = duLieu;
        tangDaVe = tang;
        if (cauHinh.hienThanhTang) taoThanhTang(duLieu, tang);
        List<DiemThamQuan> markers = new ArrayList<>();
        for (DiemThamQuan diem : duLieu.cacDiem) {
            if (diem.tang == tang && diem.coMarker()) markers.add(diem);
        }
        String tenAnh = duLieu.anhSoDo.get(tang);
        if (cauHinh.soDoHue) taoSoDoHue(tenAnh, markers, idDangChon);
        else AnhSoDoTour.themSoDo(activity, khung, duLieu.diaDiem.id,
                tenAnh, markers, idDangChon, chonDiem::accept);
    }

    private void taoThanhTang(DuLieuTour duLieu, int tangDangXem) {
        HorizontalScrollView thanhCuon = new HorizontalScrollView(activity);
        thanhCuon.setHorizontalScrollBarEnabled(false);
        LinearLayout thanhTang = new LinearLayout(activity);
        thanhTang.setOrientation(LinearLayout.HORIZONTAL);
        thanhCuon.addView(thanhTang);
        khung.addView(thanhCuon);
        for (int tang : duLieu.cacTang.keySet()) {
            boolean dangXem = tangDangXem == tang;
            TextView nutTang = new TextView(activity);
            nutTang.setText(duLieu.tenTang(tang));
            nutTang.setTextSize(13);
            nutTang.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            nutTang.setGravity(Gravity.CENTER);
            nutTang.setMinHeight(dp(48));
            nutTang.setPadding(dp(16), dp(10), dp(16), dp(10));
            nutTang.setTextColor(dangXem ? Color.WHITE : Color.parseColor("#2C7351"));
            GradientDrawable nen = new GradientDrawable();
            nen.setColor(Color.parseColor(dangXem ? "#2C7351" : "#FFFFFF"));
            nen.setCornerRadius(dp(14));
            nen.setStroke(dp(1), Color.parseColor(dangXem ? "#2C7351" : "#E0E9E0"));
            nutTang.setBackground(nen);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(8);
            lp.bottomMargin = dp(6);
            thanhTang.addView(nutTang, lp);
            nutTang.setSelected(dangXem);
            if (dangXem) nutTang.post(() -> thanhCuon.smoothScrollTo(nutTang.getLeft(), 0));
            nutTang.setOnClickListener(v -> chonTang.accept(tang));
        }
    }

    private void taoSoDoHue(String tenAnh, List<DiemThamQuan> markers, int idDangChon) {
        TextView tieuDe = new TextView(activity);
        tieuDe.setText("Sơ đồ tham quan");
        tieuDe.setTextSize(17);
        tieuDe.setTextColor(Color.parseColor("#182D22"));
        tieuDe.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tieuDe.setPadding(dp(3), dp(8), dp(3), dp(12));
        khung.addView(tieuDe);
        Bitmap anh = AnhSoDoTour.docAnhSoDo(activity, tenAnh);
        if (anh == null) {
            TextView thieu = new TextView(activity);
            thieu.setText("Chưa có sơ đồ. Bạn vẫn có thể chọn điểm trong tab Danh sách.");
            thieu.setTextSize(14);
            thieu.setTextColor(Color.parseColor("#687B6E"));
            thieu.setPadding(dp(12), dp(20), dp(12), dp(20));
            khung.addView(thieu);
            return;
        }
        SoDoHueView view = new SoDoHueView(activity, anh, markers, idDangChon, chonDiem);
        soDoHue = view;
        float manHinhDp = activity.getResources().getDisplayMetrics().heightPixels
                / activity.getResources().getDisplayMetrics().density;
        int chieuCao = Math.round(Math.max(240, Math.min(400, manHinhDp * .45f)));
        GiaoDienSoDo.themVao(khung, view, dp(chieuCao),
                () -> view.doiZoom(1f / 1.5f), view::datLai, () -> view.doiZoom(1.5f));
    }

    void giaiPhong() {
        // Tách view khỏi cây trước khi trả bitmap; view đã giải phóng không còn giữ ảnh.
        List<View> cu = new ArrayList<>();
        for (int i = 0; i < khung.getChildCount(); i++) cu.add(khung.getChildAt(i));
        khung.removeAllViews();
        for (View view : cu) AnhSoDoTour.giaiPhong(view);
        soDoHue = null;
        duLieuDaVe = null;
        tangDaVe = Integer.MIN_VALUE;
    }

    private int dp(float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
