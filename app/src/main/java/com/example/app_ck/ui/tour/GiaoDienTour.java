
package com.example.app_ck.ui.tour;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.example.app_ck.R;
import com.example.app_ck.data.model.DiemThamQuan;
import com.google.android.material.appbar.AppBarLayout;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

public final class GiaoDienTour {

    // =====================================================
    // MAU SAC
    // =====================================================

    private static final int XANH =
            Color.parseColor("#2C7351");

    private static final int TIEU_DE =
            Color.parseColor("#182D22");

    private static final int CHU_PHU =
            Color.parseColor("#687B6E");

    private static final int NEN_NUT_SANG =
            Color.parseColor("#EDF5EC");

    private static final int NEN_NUT_TOI =
            Color.parseColor("#88203329");

    private final Activity activity;

    // =====================================================
    // HEADER VA BANNER
    // =====================================================

    private final AppBarLayout appBar;

    private final View hero;
    private final View nenThuGon;

    private final TextView tenDiaDiem;
    private final TextView tenThuGon;
    private final TextView heroThongTin;
    private final TextView nutQuayLai;

    private final ImageButton nutThuGon;

    private int doLechHeader = 0;

    private Boolean giaoDienHeaderGon = null;

    // =====================================================
    // TAB VA TIM KIEM
    // =====================================================

    private final TextView tabSoDo;
    private final TextView tabDanhSach;

    private final View gachSoDo;
    private final View gachDanhSach;

    private final TextView ketQua;
    private final TextView nutXoa;

    private final EditText oTimKiem;

    private final LinearLayout khungSoDo;
    private final LinearLayout khungDanhSach;
    private final LinearLayout danhSach;

    private final NestedScrollView vungCuon;

    private boolean dangXemSoDo = true;

    // =====================================================
    // DU LIEU
    // =====================================================

    private final List<DiemThamQuan> cacDiem =
            new ArrayList<>();

    private int idDangChon = -1;

    private IntConsumer suKienChon;
    private IntPredicate hoTroAuto;
    private IntFunction<String> tenKhuVuc;

    private int idNgheNhanh = Integer.MIN_VALUE;

    // =====================================================
    // SO DO TOAN MAN HINH
    // =====================================================

    private final View thanhDieuKhien;
    private final View khungHuongDan;

    private final View thanhToanManHinh;

    private final TextView tenDiemToanManHinh;

    private boolean dangToanManHinh;

    private View soDoHienTai;

    private ImageButton nutMoRong;

    private int chieuCaoSoDoCu;

    private int viTriCuonCu;

    private int hienThiAutoCu;
    private int hienThiDieuKhienCu;

    private boolean headerGonTruocToanManHinh;

    private CoordinatorLayout.Behavior<?> hanhViCu;

    private OnBackPressedCallback xuLyQuayLai;

    // =====================================================
    // KHOI TAO
    // =====================================================

    public GiaoDienTour(Activity activity) {

        this(activity, null);
    }

    public GiaoDienTour(
            Activity activity,
            Bundle state) {

        this.activity = activity;

        // Header

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

        heroThongTin = activity.findViewById(
                R.id.tqHeroThongTin
        );

        nutQuayLai = activity.findViewById(
                R.id.tqNutQuayLai
        );

        nutThuGon = activity.findViewById(
                R.id.tqNutThuGon
        );

        // Tab

        tabSoDo = activity.findViewById(R.id.tqTabSoDo);

        tabDanhSach = activity.findViewById(
                R.id.tqTabDanhSach
        );

        gachSoDo = activity.findViewById(
                R.id.tqGachSoDo
        );

        gachDanhSach = activity.findViewById(
                R.id.tqGachDanhSach
        );

        // Tim kiem

        ketQua = activity.findViewById(
                R.id.tqKetQuaTimKiem
        );

        nutXoa = activity.findViewById(
                R.id.tqXoaTimKiem
        );

        oTimKiem = activity.findViewById(
                R.id.tqTimDiem
        );

        // Noi dung

        khungSoDo = activity.findViewById(
                R.id.tqKhungSoDo
        );

        khungDanhSach = activity.findViewById(
                R.id.tqKhungDanhSach
        );

        danhSach = activity.findViewById(
                R.id.tqDanhSachDiem
        );

        vungCuon = activity.findViewById(
                R.id.tqVungCuon
        );

        // Toan man hinh

        thanhDieuKhien = activity.findViewById(
                R.id.tqThanhDieuKhien
        );

        khungHuongDan = activity.findViewById(
                R.id.tqKhungHuongDan
        );

        thanhToanManHinh = activity.findViewById(
                R.id.tqThanhToanManHinh
        );

        tenDiemToanManHinh = activity.findViewById(
                R.id.tqTenDiemToanManHinh
        );

        // GiaoDienSoDo se truy cap qua tag nay

        vungCuon.setTag(this);

        ganSuKien();
        khoiTaoHeader();
        khoiTaoToanManHinh();
        capNhatIconNguon();

        // Khoi phuc tab

        chuyenTab(
                state == null
                        || state.getBoolean(
                        "tourTabSoDo", true
                )
        );

        // Khoi phuc trang thai banner

        boolean headerGon = state != null
                && state.getBoolean(
                "tourHeaderGon", false
        );

        appBar.setExpanded(!headerGon, false);
    }

    public static GiaoDienTour lay(Context context) {

        while (context instanceof ContextWrapper
                && !(context instanceof Activity)) {

            Context base = ((ContextWrapper) context)
                    .getBaseContext();

            if (base == context) return null;

            context = base;
        }

        if (!(context instanceof Activity)) return null;

        View view = ((Activity) context)
                .findViewById(R.id.tqVungCuon);

        if (view == null) return null;

        Object tag = view.getTag();

        return tag instanceof GiaoDienTour
                ? (GiaoDienTour) tag
                : null;
    }

    public void luuTrangThai(Bundle state) {

        state.putBoolean(
                "tourTabSoDo",
                dangXemSoDo
        );

        state.putBoolean(
                "tourHeaderGon",
                dangToanManHinh
                        ? headerGonTruocToanManHinh
                        : headerDangGon()
        );
    }

    // =====================================================
    // HEADER THU GON
    // =====================================================

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

                            if (dangToanManHinh) return;

                            doLechHeader = offset;

                            capNhatHeader(
                                    bar.getTotalScrollRange(),
                                    offset
                            );
                        }
        );

        // Cho phep nguoi dung tu thu gon / mo rong

        nutThuGon.setOnClickListener(v -> {

            anBanPhim();

            appBar.setExpanded(
                    headerDangGon(),
                    true
            );
        });

        capNhatHeader(1, 0);
    }

    private boolean headerDangGon() {

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

    // =====================================================
    // SU KIEN
    // =====================================================

    private void ganSuKien() {

        tabSoDo.setOnClickListener(
                v -> chuyenTab(true)
        );

        tabDanhSach.setOnClickListener(
                v -> chuyenTab(false)
        );

        nutXoa.setOnClickListener(
                v -> oTimKiem.setText("")
        );

        oTimKiem.addTextChangedListener(
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

                        nutXoa.setVisibility(
                                s.length() == 0
                                        ? View.GONE
                                        : View.VISIBLE
                        );

                        hienThiDanhSach();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        oTimKiem.setOnEditorActionListener(
                (v, action, event) -> {

                    if (action
                            == EditorInfo.IME_ACTION_DONE) {

                        anBanPhim();

                        return true;
                    }

                    return false;
                }
        );
    }

    // =====================================================
    // TAB GACH CHAN
    // =====================================================

    private void chuyenTab(boolean soDo) {

        if (dangToanManHinh && !soDo) {

            thoatToanManHinh();
        }

        boolean daDoiTab =
                dangXemSoDo != soDo;

        dangXemSoDo = soDo;

        khungSoDo.setVisibility(
                soDo ? View.VISIBLE : View.GONE
        );

        khungDanhSach.setVisibility(
                soDo ? View.GONE : View.VISIBLE
        );

        capNhatTab(
                tabSoDo,
                gachSoDo,
                soDo
        );

        capNhatTab(
                tabDanhSach,
                gachDanhSach,
                !soDo
        );

        if (soDo) anBanPhim();

        if (daDoiTab) {

            vungCuon.post(
                    () -> vungCuon.scrollTo(0, 0)
            );
        }
    }

    private void capNhatTab(
            TextView tab,
            View gach,
            boolean dangChon) {

        tab.setTextColor(
                dangChon ? XANH : CHU_PHU
        );

        tab.setTypeface(
                Typeface.create(
                        dangChon
                                ? "sans-serif-medium"
                                : "sans-serif",
                        Typeface.NORMAL
                )
        );

        gach.setVisibility(
                dangChon
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        tab.setSelected(dangChon);
    }

    // =====================================================
    // NGUON THAM KHAO
    // =====================================================

    private void capNhatIconNguon() {

        TextView nguon = activity.findViewById(
                R.id.tqNguonThongTin
        );

        if (nguon == null) return;

        nguon.setText("Nguồn");

        nguon.setGravity(Gravity.CENTER);

        nguon.setPadding(
                dp(2), dp(3), dp(2), dp(2)
        );

        nguon.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                R.drawable.ui_tour_source,
                0,
                0
        );

        nguon.setCompoundDrawablePadding(
                dp(3)
        );

        nguon.setContentDescription(
                "Xem nguồn thông tin điểm tham quan"
        );
    }

    // =====================================================
    // NHAN DU LIEU TU ACTIVITY
    // =====================================================

    public void capNhat(
            List<DiemThamQuan> duLieu,
            int idDangChon,
            IntPredicate hoTroAuto,
            IntFunction<String> tenKhuVuc,
            IntConsumer suKienChon) {

        cacDiem.clear();

        if (duLieu != null) {

            for (DiemThamQuan diem : duLieu) {

                if (diem != null) {

                    cacDiem.add(diem);
                }
            }
        }

        this.idDangChon = idDangChon;
        this.hoTroAuto = hoTroAuto;
        this.tenKhuVuc = tenKhuVuc;
        this.suKienChon = suKienChon;

        // So diem duoc lay dong tu SQLite

        heroThongTin.setText(
                cacDiem.isEmpty()
                        ? "Tham quan tương tác"
                        : cacDiem.size()
                        + " điểm tham quan · Thuyết minh di sản"
        );

        tenThuGon.setText(
                tenDiaDiem.getText()
        );

        hienThiDanhSach();
        capNhatIconNguon();
        capNhatTenToanManHinh();

        // Neu khu vuc moi khong co so do,
        // thoat toan man hinh de tranh giao dien rong

        if (dangToanManHinh
                && !soDoConHienThi()) {

            thoatToanManHinh();
        }
    }

    // =====================================================
    // TIM KIEM
    // =====================================================

    private void hienThiDanhSach() {

        danhSach.removeAllViews();

        String tuKhoa = chuanHoa(
                oTimKiem.getText().toString()
        );

        int dem = 0;

        for (DiemThamQuan diem : cacDiem) {

            String khuVuc = tenKhuVuc == null
                    ? ""
                    : tenKhuVuc.apply(diem.tang);

            int so = diem.soHienThi > 0
                    ? diem.soHienThi
                    : diem.id;

            String chuoiTim = chuanHoa(
                    diem.ten + " "
                            + (khuVuc == null ? "" : khuVuc)
                            + " "
                            + (diem.coMarker() ? so : "")
            );

            if (!chuoiTim.contains(tuKhoa)) {

                continue;
            }

            danhSach.addView(
                    taoTheDiem(diem, khuVuc)
            );

            dem++;
        }

        ketQua.setText(
                tuKhoa.isEmpty()
                        ? cacDiem.size()
                        + " điểm tham quan"
                        : dem + "/" + cacDiem.size()
                        + " điểm phù hợp"
        );

        if (dem == 0) {

            TextView rong = taoChu(
                    cacDiem.isEmpty()
                            ? "Chưa có điểm tham quan"
                            : "Không tìm thấy điểm phù hợp",
                    14,
                    CHU_PHU,
                    false
            );

            rong.setGravity(Gravity.CENTER);

            rong.setPadding(
                    dp(12), dp(30), dp(12), dp(30)
            );

            danhSach.addView(rong);
        }
    }

    // =====================================================
    // THE DIEM THAM QUAN
    // =====================================================

    private View taoTheDiem(
            DiemThamQuan diem,
            String khuVuc) {

        boolean dangChon =
                diem.id == idDangChon;

        boolean coAuto =
                hoTroAuto != null
                        && hoTroAuto.test(diem.id);

        boolean coBai =
                coNoiDung(diem.noiDung)
                        || coNoiDung(diem.moTa);

        // The ngoai

        LinearLayout the = new LinearLayout(activity);

        the.setOrientation(
                LinearLayout.HORIZONTAL
        );

        the.setGravity(
                Gravity.CENTER_VERTICAL
        );

        the.setPadding(
                dp(8), dp(6), dp(8), dp(6)
        );

        the.setMinimumHeight(dp(70));

        the.setBackground(
                nen(
                        dangChon
                                ? "#E8F2E6"
                                : "#FFFFFF",
                        dangChon
                                ? "#88B18B"
                                : "#E0E9E0",
                        16
                )
        );

        LinearLayout.LayoutParams lpThe =
                new LinearLayout.LayoutParams(
                        -1, -2
                );

        lpThe.bottomMargin = dp(8);

        the.setLayoutParams(lpThe);

        // Vung bam chon diem

        LinearLayout vungChon =
                new LinearLayout(activity);

        vungChon.setOrientation(
                LinearLayout.HORIZONTAL
        );

        vungChon.setGravity(
                Gravity.CENTER_VERTICAL
        );

        vungChon.setMinimumHeight(dp(54));

        vungChon.setPadding(
                dp(4), dp(6), dp(8), dp(6)
        );

        vungChon.setBackground(
                nenCham(
                        dangChon
                                ? "#E8F2E6"
                                : "#FFFFFF",
                        dangChon
                                ? "#E8F2E6"
                                : "#FFFFFF",
                        12
                )
        );

        the.addView(
                vungChon,
                new LinearLayout.LayoutParams(
                        0, -2, 1f
                )
        );

        // So marker

        String soMarker = diem.coMarker()
                ? String.format(
                Locale.ROOT,
                "%02d",
                diem.soHienThi > 0
                        ? diem.soHienThi
                        : diem.id
        )
                : "–";

        TextView so = taoChu(
                soMarker,
                15,
                dangChon ? Color.WHITE : XANH,
                true
        );

        so.setGravity(Gravity.CENTER);

        so.setBackground(
                nen(
                        dangChon
                                ? "#2C7351"
                                : "#EDF5EC",
                        dangChon
                                ? "#2C7351"
                                : "#EDF5EC",
                        12
                )
        );

        vungChon.addView(
                so,
                new LinearLayout.LayoutParams(
                        dp(40), dp(40)
                )
        );

        // Khu vuc noi dung

        LinearLayout noiDung =
                new LinearLayout(activity);

        noiDung.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams lpNoiDung =
                new LinearLayout.LayoutParams(
                        0, -2, 1f
                );

        lpNoiDung.leftMargin = dp(10);

        vungChon.addView(
                noiDung,
                lpNoiDung
        );

        TextView ten = taoChu(
                diem.ten,
                14,
                TIEU_DE,
                true
        );

        noiDung.addView(ten);

        // Trang thai diem

        List<String> nhan = new ArrayList<>();

        if (khuVuc != null
                && !khuVuc.trim().isEmpty()) {

            nhan.add(khuVuc);
        }

        if (coAuto) {

            nhan.add("AUTO");
        }

        if (dangChon) {

            nhan.add("Đang chọn");
        }

        if (!diem.coMarker()) {

            nhan.add("Chọn từ danh sách");
        }

        if (!coBai) {

            nhan.add("Chưa có bài nghe");
        }

        if (!nhan.isEmpty()) {

            TextView trangThai = taoChu(
                    TextUtils.join(" · ", nhan),
                    12,
                    dangChon || coAuto
                            ? XANH
                            : CHU_PHU,
                    false
            );

            trangThai.setPadding(
                    0, dp(4), 0, 0
            );

            noiDung.addView(trangThai);
        }

        // Bam ten chi chon diem

        vungChon.setClickable(true);
        vungChon.setFocusable(true);
        vungChon.setSelected(dangChon);

        vungChon.setContentDescription(
                "Chọn " + diem.ten
                        + (nhan.isEmpty()
                        ? ""
                        : ", " + TextUtils.join(", ", nhan))
        );

        vungChon.setOnClickListener(v -> {

            anBanPhim();

            if (suKienChon != null) {

                suKienChon.accept(diem.id);
            }
        });

        // Nut nghe nhanh doc lap

        ImageButton nutNghe =
                GiaoDienSoDo.taoNut(
                        activity,
                        R.drawable.ui_tour_headphones,
                        "Nghe " + diem.ten,
                        () -> ngheNhanh(diem.id)
                );

        nutNghe.setEnabled(coBai);

        nutNghe.setAlpha(
                coBai ? 1f : 0.35f
        );

        LinearLayout.LayoutParams lpNghe =
                new LinearLayout.LayoutParams(
                        dp(48), dp(48)
                );

        lpNghe.leftMargin = dp(4);

        the.addView(
                nutNghe,
                lpNghe
        );

        return the;
    }

    private boolean coNoiDung(String text) {

        return text != null
                && !text.trim().isEmpty();
    }

    // =====================================================
    // NGHE NHANH
    // =====================================================

    private void ngheNhanh(int id) {

        if (activity.isFinishing()
                || activity.isDestroyed()) {

            return;
        }

        anBanPhim();

        // Neu dang chon diem nay,
        // su dung nut nghe hien tai

        if (id == idDangChon) {

            View nutPhat = activity.findViewById(
                    R.id.tqNutPhat
            );

            if (nutPhat != null
                    && nutPhat.isEnabled()) {

                nutPhat.performClick();
            }

            return;
        }

        if (suKienChon == null) return;

        // Giu nguyen co che xac nhan AUTO
        // trong cac Activity da cap nhat

        idNgheNhanh = id;

        try {

            suKienChon.accept(id);

        } finally {

            idNgheNhanh = Integer.MIN_VALUE;
        }
    }

    public boolean dangYeuCauNghe(int id) {

        return idNgheNhanh == id;
    }

    // =====================================================
    // KHOI TAO TOAN MAN HINH
    // =====================================================

    private void khoiTaoToanManHinh() {

        activity.findViewById(
                R.id.tqThoatToanManHinh
        ).setOnClickListener(
                v -> thoatToanManHinh()
        );

        if (activity instanceof ComponentActivity) {

            ComponentActivity owner =
                    (ComponentActivity) activity;

            xuLyQuayLai =
                    new OnBackPressedCallback(false) {

                        @Override
                        public void handleOnBackPressed() {

                            thoatToanManHinh();
                        }
                    };

            owner.getOnBackPressedDispatcher()
                    .addCallback(
                            owner,
                            xuLyQuayLai
                    );
        }

        // Dieu chinh chieu cao khi doi kich thuoc

        vungCuon.addOnLayoutChangeListener(
                (v, l, t, r, b, ol, ot, or, ob) -> {

                    if (dangToanManHinh) {

                        vungCuon.post(
                                this::capNhatChieuCaoSoDo
                        );
                    }
                }
        );

        khungSoDo.addOnLayoutChangeListener(
                (v, l, t, r, b, ol, ot, or, ob) -> {

                    if (dangToanManHinh) {

                        vungCuon.post(
                                this::capNhatChieuCaoSoDo
                        );
                    }
                }
        );
    }

    public void dangKySoDo(
            View soDo,
            ImageButton nutMoRong) {

        this.soDoHienTai = soDo;
        this.nutMoRong = nutMoRong;

        ViewGroup.LayoutParams lp =
                soDo.getLayoutParams();

        chieuCaoSoDoCu = lp == null
                ? dp(320)
                : lp.height;

        if (nutMoRong != null) {

            nutMoRong.setVisibility(
                    dangToanManHinh
                            ? View.GONE
                            : View.VISIBLE
            );
        }

        if (dangToanManHinh) {

            soDo.post(
                    this::capNhatChieuCaoSoDo
            );
        }
    }

    private boolean soDoConHienThi() {

        if (soDoHienTai == null) {

            return false;
        }

        View view = soDoHienTai;

        while (view != null) {

            if (view == khungSoDo) {

                return true;
            }

            if (!(view.getParent()
                    instanceof View)) {

                return false;
            }

            view = (View) view.getParent();
        }

        return false;
    }

    // =====================================================
    // BAT TOAN MAN HINH
    // =====================================================

    public void batToanManHinh() {

        if (dangToanManHinh
                || !dangXemSoDo
                || !soDoConHienThi()) {

            return;
        }

        anBanPhim();

        viTriCuonCu =
                vungCuon.getScrollY();

        headerGonTruocToanManHinh =
                headerDangGon();

        hienThiAutoCu =
                khungHuongDan.getVisibility();

        hienThiDieuKhienCu =
                thanhDieuKhien.getVisibility();

        dangToanManHinh = true;

        // Tam ngung behavior cuon cua AppBarLayout

        CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams)
                        vungCuon.getLayoutParams();

        hanhViCu = lp.getBehavior();

        lp.setBehavior(null);

        vungCuon.setLayoutParams(lp);

        vungCuon.setNestedScrollingEnabled(false);

        // An cac thanh khong can thiet

        appBar.setVisibility(View.GONE);

        khungHuongDan.setVisibility(View.GONE);

        thanhDieuKhien.setVisibility(View.GONE);

        thanhToanManHinh.setVisibility(View.VISIBLE);

        if (nutMoRong != null) {

            nutMoRong.setVisibility(View.GONE);
        }

        if (xuLyQuayLai != null) {

            xuLyQuayLai.setEnabled(true);
        }

        capNhatTenToanManHinh();

        vungCuon.requestLayout();

        vungCuon.post(() -> {

            capNhatChieuCaoSoDo();

            vungCuon.scrollTo(0, 0);
        });
    }

    private void capNhatChieuCaoSoDo() {

        if (!dangToanManHinh
                || !soDoConHienThi()
                || vungCuon.getHeight() <= 0
                || khungSoDo.getHeight() <= 0
                || soDoHienTai.getHeight() <= 0) {

            return;
        }

        int khongGianKhac = Math.max(
                0,
                khungSoDo.getHeight()
                        - soDoHienTai.getHeight()
        );

        View noiDungCuon =
                vungCuon.getChildAt(0);

        int padding = noiDungCuon == null
                ? 0
                : noiDungCuon.getPaddingTop()
                + noiDungCuon.getPaddingBottom();

        int chieuCaoMoi = Math.max(
                dp(160),
                vungCuon.getHeight()
                        - khongGianKhac
                        - padding
                        - dp(10)
        );

        ViewGroup.LayoutParams lp =
                soDoHienTai.getLayoutParams();

        if (lp != null
                && lp.height != chieuCaoMoi) {

            lp.height = chieuCaoMoi;

            soDoHienTai.setLayoutParams(lp);
        }
    }

    private void capNhatTenToanManHinh() {

        for (DiemThamQuan diem : cacDiem) {

            if (diem.id == idDangChon) {

                tenDiemToanManHinh.setText(
                        diem.ten
                );

                return;
            }
        }

        tenDiemToanManHinh.setText(
                "Chọn điểm trên sơ đồ"
        );
    }

    // =====================================================
    // THOAT TOAN MAN HINH
    // =====================================================

    public void thoatToanManHinh() {

        if (!dangToanManHinh) return;

        dangToanManHinh = false;

        if (xuLyQuayLai != null) {

            xuLyQuayLai.setEnabled(false);
        }

        thanhToanManHinh.setVisibility(
                View.GONE
        );

        khungHuongDan.setVisibility(
                hienThiAutoCu
        );

        thanhDieuKhien.setVisibility(
                hienThiDieuKhienCu
        );

        if (nutMoRong != null) {

            nutMoRong.setVisibility(View.VISIBLE);
        }

        // Khoi phuc chieu cao so do

        if (soDoHienTai != null) {

            ViewGroup.LayoutParams lp =
                    soDoHienTai.getLayoutParams();

            if (lp != null) {

                lp.height = chieuCaoSoDoCu;

                soDoHienTai.setLayoutParams(lp);
            }
        }

        // Khoi phuc header va behavior cuon

        appBar.setVisibility(View.VISIBLE);

        CoordinatorLayout.LayoutParams lp =
                (CoordinatorLayout.LayoutParams)
                        vungCuon.getLayoutParams();

        lp.setBehavior(
                hanhViCu == null
                        ? new AppBarLayout.ScrollingViewBehavior()
                        : hanhViCu
        );

        vungCuon.setLayoutParams(lp);

        vungCuon.setNestedScrollingEnabled(true);

        appBar.setExpanded(
                !headerGonTruocToanManHinh,
                false
        );

        vungCuon.post(
                () -> vungCuon.scrollTo(
                        0, viTriCuonCu
                )
        );
    }

    // =====================================================
    // TIEN ICH
    // =====================================================

    private String chuanHoa(String text) {

        if (text == null) return "";

        return Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    private void anBanPhim() {

        InputMethodManager imm =
                (InputMethodManager)
                        activity.getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null
                && oTimKiem.getWindowToken() != null) {

            imm.hideSoftInputFromWindow(
                    oTimKiem.getWindowToken(),
                    0
            );
        }

        oTimKiem.clearFocus();
    }

    private TextView taoChu(
            String text,
            int size,
            int color,
            boolean dam) {

        TextView tv = new TextView(activity);

        tv.setText(text);
        tv.setTextSize(size);
        tv.setTextColor(color);

        tv.setLineSpacing(
                dp(3), 1f
        );

        tv.setTypeface(
                Typeface.create(
                        dam
                                ? "sans-serif-medium"
                                : "sans-serif",
                        Typeface.NORMAL
                )
        );

        return tv;
    }

    private GradientDrawable nen(
            String mau,
            String vien,
            int boGoc) {

        GradientDrawable d =
                new GradientDrawable();

        d.setColor(
                Color.parseColor(mau)
        );

        d.setCornerRadius(
                dp(boGoc)
        );

        d.setStroke(
                dp(1),
                Color.parseColor(vien)
        );

        return d;
    }

    private RippleDrawable nenCham(
            String mau,
            String vien,
            int boGoc) {

        return new RippleDrawable(
                ColorStateList.valueOf(
                        Color.parseColor("#192C7351")
                ),
                nen(mau, vien, boGoc),
                null
        );
    }

    private int dp(float value) {

        return Math.round(
                value * activity.getResources()
                        .getDisplayMetrics().density
        );
    }
}
