package com.example.app_ck.ui.tour;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.app_ck.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import java.util.function.BooleanSupplier;

/** Banner thu gọn giữ nguyên ngưỡng, màu sắc và animation của màn hình hiện tại. */
final class BannerTour {
    private static final int XANH = Color.parseColor("#2C7351");
    private static final int NEN_NUT_SANG = Color.parseColor("#EDF5EC");
    private static final int NEN_NUT_TOI = Color.parseColor("#88203329");
    private final Activity activity;
    private final AppBarLayout appBar;
    private final BooleanSupplier dangToanManHinh;
    private final Runnable anBanPhim;
    private final View hero;
    private final View nenThuGon;

    private final TextView tenDiaDiem;
    private final TextView tenThuGon;
    private final TextView nutQuayLai;

    private final ImageButton nutThuGon;

    private int doLechHeader = 0;

    private Boolean giaoDienHeaderGon = null;

    BannerTour(Activity activity, BooleanSupplier dangToanManHinh, Runnable anBanPhim) {
        this.activity = activity;
        this.dangToanManHinh = dangToanManHinh;
        this.anBanPhim = anBanPhim;


        appBar = activity.findViewById(R.id.tqAppBar);

        hero = activity.findViewById(R.id.tqHero);

        nenThuGon = activity.findViewById(
                R.id.tqNenThuGon
        );

        tenDiaDiem = activity.findViewById(
                R.id.tqTenDiaDiem
        );

        tenThuGon = activity.findViewById(
                R.id.tqTenThuGon
        );



        nutQuayLai = activity.findViewById(
                R.id.tqNutQuayLai
        );

        nutThuGon = activity.findViewById(
                R.id.tqNutThuGon
        );

        khoiTaoHeader();
    }

    private void khoiTaoHeader() {
        CollapsingToolbarLayout collapsingHeader =
                activity.findViewById(R.id.tqCollapsingHeader);

        collapsingHeader.setContentScrim(null);
        collapsingHeader.setStatusBarScrim(null);
        collapsingHeader.setScrimsShown(false, false);
        // Dong bo ten tu Activity sang header nho

        tenThuGon.setText(
                tenDiaDiem.getText()
        );

        tenDiaDiem.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        tenThuGon.setText(s);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        // Tu dong thu gon khi cuon

        appBar.addOnOffsetChangedListener(
                (AppBarLayout.OnOffsetChangedListener)
                        (bar, offset) -> {

                            if (dangToanManHinh.getAsBoolean()) return;

                            doLechHeader = offset;

                            capNhatHeader(
                                    bar.getTotalScrollRange(),
                                    offset
                            );
                        }
        );

        // Cho phep nguoi dung tu thu gon / mo rong

        nutThuGon.setOnClickListener(v -> {

            anBanPhim.run();

            appBar.setExpanded(
                    dangGon(),
                    true
            );
        });

        capNhatHeader(1, 0);
    }

    boolean dangGon() {

        int range = appBar.getTotalScrollRange();

        return range > 0
                && Math.abs(doLechHeader) >= range / 2f;
    }

    private void capNhatHeader(
            int range,
            int offset) {

        float tiLe = range <= 0
                ? 0f
                : Math.min(
                1f,
                Math.abs(offset) / (float) range
        );

        // Chuyen doi mem giua banner va header nho

        float alphaThuGon = Math.max(
                0f,
                Math.min(
                        1f,
                        (tiLe - 0.35f) / 0.45f
                )
        );

        hero.setAlpha(
                1f - alphaThuGon
        );

        nenThuGon.setAlpha(alphaThuGon);

        tenThuGon.setAlpha(alphaThuGon);

        tenThuGon.setVisibility(
                alphaThuGon > 0.01f
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        // Tranh doc trung lap ten bang TalkBack

        hero.setImportantForAccessibility(
                alphaThuGon >= 0.5f
                        ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        : View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        );

        tenThuGon.setImportantForAccessibility(
                alphaThuGon >= 0.5f
                        ? View.IMPORTANT_FOR_ACCESSIBILITY_YES
                        : View.IMPORTANT_FOR_ACCESSIBILITY_NO
        );

        boolean gon = tiLe >= 0.5f;

        // Chi doi mau nut khi trang thai thay doi

        if (giaoDienHeaderGon == null
                || giaoDienHeaderGon != gon) {

            giaoDienHeaderGon = gon;

            int mauIcon = gon
                    ? XANH
                    : Color.WHITE;

            int mauNen = gon
                    ? NEN_NUT_SANG
                    : NEN_NUT_TOI;

            nutQuayLai.setBackgroundTintList(
                    ColorStateList.valueOf(mauNen)
            );

            nutQuayLai.setCompoundDrawableTintList(
                    ColorStateList.valueOf(mauIcon)
            );

            nutThuGon.setBackgroundTintList(
                    ColorStateList.valueOf(mauNen)
            );

            nutThuGon.setImageTintList(
                    ColorStateList.valueOf(mauIcon)
            );

            nutThuGon.setRotation(
                    gon ? 180f : 0f
            );

            nutThuGon.setContentDescription(
                    gon
                            ? "Mở rộng ảnh bìa"
                            : "Thu gọn ảnh bìa"
            );
        }
    }

}
