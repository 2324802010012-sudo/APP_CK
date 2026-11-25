
package com.example.app_ck.ui.tour;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
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
import com.example.app_ck.R;
import com.example.app_ck.data.model.DiemThamQuan;
import com.google.android.material.appbar.AppBarLayout;

import static com.example.app_ck.util.VanBanTimKiem.chuanHoa;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

public final class GiaoDienTour {

    // =====================================================
    // MAU SAC
    // =====================================================

    private static final int XANH =
            Color.parseColor("#2C7351");

    private static final int CHU_PHU = Color.parseColor("#687B6E");

    private final Activity activity;

    // =====================================================
    // HEADER VA BANNER
    // =====================================================

    private final AppBarLayout appBar;

    private final BannerTour banner;
    private final TextView heroThongTin;

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
    private final View thongTinSoDo;

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

        appBar = activity.findViewById(R.id.tqAppBar);
        heroThongTin = activity.findViewById(R.id.tqHeroThongTin);
        banner = new BannerTour(activity, () -> dangToanManHinh, this::anBanPhim);

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

        thongTinSoDo = activity.findViewById(R.id.tqThongTinSoDo);

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
                        : banner.dangGon()
        );
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

        thongTinSoDo.setVisibility(
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
                    GiaoDienTheThamQuan.tao(activity, diem, khuVuc, idDangChon,
                            hoTroAuto != null && hoTroAuto.test(diem.id),
                            () -> { anBanPhim(); if (suKienChon != null) suKienChon.accept(diem.id); },
                            () -> ngheNhanh(diem.id))
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

            TextView rong = GiaoDienTheThamQuan.taoChu(activity,
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
                banner.dangGon();

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

    private int dp(float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
