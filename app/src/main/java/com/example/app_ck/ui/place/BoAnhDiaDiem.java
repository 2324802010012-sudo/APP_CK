package com.example.app_ck.ui.place;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.app_ck.R;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.model.DiemThamQuan;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Sở hữu slider, thao tác vuốt, giải mã ảnh và thư viện; không đọc SQLite hay điều khiển audio. */
final class BoAnhDiaDiem {
    private static final String TAG = "BoAnhDiaDiem";
    private static final long THOI_GIAN_CHUYEN_ANH = 4000L;
    private static final int MAU_CHU_DAO = Color.parseColor("#2C7351");
    private static final int MAU_TIEU_DE = Color.parseColor("#182D22");
    private static final int MAU_CHU_PHU = Color.parseColor("#687B6E");
    private final ChiTietDiaDiemActivity activity;
    private DiaDiem diaDiem;
    private List<DiemThamQuan> cacDiem = new ArrayList<>();
    private boolean dangHienThi, dangChamAnh, dangMoThuVien;
    private AlertDialog thuVien;

    BoAnhDiaDiem(ChiTietDiaDiemActivity activity) {
        this.activity = activity;
        anhBia = activity.findViewById(R.id.anhChiTiet);
        demAnhBia = activity.findViewById(R.id.soLuongAnh);
        tenAnhBia = activity.findViewById(R.id.ctTenAnh);
        activity.findViewById(R.id.nutThuVien).setOnClickListener(v -> moThuVien());
        demAnhBia.setOnClickListener(v -> moThuVien());
        anhBia.setOnClickListener(v -> moThuVien());
        ganVuotAnh(anhBia, () -> chuyenAnhBia(-1), () -> chuyenAnhBia(1),
                () -> { dangChamAnh = true; dungHenAnh(); },
                () -> { dangChamAnh = false; batHenAnh(); });
    }

    void capNhat(DiaDiem diaDiem, List<DiemThamQuan> cacDiem) {
        this.diaDiem = diaDiem;
        this.cacDiem = new ArrayList<>(cacDiem);
        capNhatBoAnh();
    }

    void tiepTuc() {
        dangHienThi = true;
        dangChamAnh = false;
        batHenAnh();
    }

    void tamDung() {
        dangHienThi = false;
        dungHenAnh();
        anhBia.animate().cancel();
        anhBia.setAlpha(1f);
    }

    void giaiPhong() {
        tamDung();
        if (thuVien != null) thuVien.dismiss();
    }

    private void thongBao(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private ImageView anhBia;
    private TextView demAnhBia;
    private TextView tenAnhBia;
    private final ArrayList<String> boAnh =
            new ArrayList<>();
    private final LinkedHashMap<String, String> tenCacAnh =
            new LinkedHashMap<>();
    private int viTriAnh = 0;
    private final Handler henAnh =
            new Handler(Looper.getMainLooper());
    private final Runnable tuDongChuyenAnh =
            new Runnable() {
                @Override
                public void run() {
                    if (!coTheTuChuyenAnh()) {
                        return;
                    }
                    chuyenAnhBia(1);
                    batHenAnh();
                }
            };
    private void capNhatBoAnh() {
        String anhCu = null;
        if (!boAnh.isEmpty()
                && viTriAnh >= 0
                && viTriAnh < boAnh.size()) {
            anhCu = boAnh.get(viTriAnh);
        }
        tenCacAnh.clear();
        tenCacAnh.putAll(
                layDanhSachAnhChiTiet()
        );
        boAnh.clear();
        boAnh.addAll(tenCacAnh.keySet());
        viTriAnh = 0;
        if (anhCu != null) {
            int timThay =
                    boAnh.indexOf(anhCu);
            if (timThay >= 0) {
                viTriAnh = timThay;
            }
        }
        hienAnhBia(false);
        View nutThuVien =
                activity.findViewById(R.id.nutThuVien);
        nutThuVien.setEnabled(!boAnh.isEmpty());
        nutThuVien.setAlpha(
                boAnh.isEmpty() ? 0.5f : 1f
        );
        batHenAnh();
    }
    // =====================================================
    // HIỂN THỊ ẢNH BÌA
    // =====================================================
    private void hienAnhBia(boolean hieuUng) {
        if (anhBia == null) {
            return;
        }
        anhBia.animate().cancel();
        anhBia.setAlpha(1f);
        if (boAnh.isEmpty()) {
            AnhDiaDiem.hien(
                    anhBia,
                    diaDiem.tenAnh
            );
            demAnhBia.setText("0 ảnh");
            tenAnhBia.setText("");
            return;
        }
        String maAnh =
                boAnh.get(viTriAnh);
        Bitmap bitmap =
                docAnhChiTiet(maAnh, 1600);
        if (bitmap != null) {
            anhBia.setImageBitmap(bitmap);
        } else {
            AnhDiaDiem.hien(
                    anhBia,
                    maAnh
            );
        }
        String ten =
                tenCacAnh.get(maAnh);
        anhBia.setContentDescription(ten);
        // Không lặp lại tên địa điểm chính.
        // Chỉ hiện tên khi đang xem ảnh điểm thành phần.
        tenAnhBia.setText(
                TextUtils.equals(
                        maAnh,
                        diaDiem.tenAnh
                )
                        ? ""
                        : ten
        );
        demAnhBia.setText(
                (viTriAnh + 1)
                        + " / "
                        + boAnh.size()
        );
        // Hiệu ứng xuất hiện mềm mại
        if (hieuUng) {
            anhBia.setAlpha(0.45f);
            anhBia.animate()
                    .alpha(1f)
                    .setDuration(350)
                    .start();
        }
    }
    private void chuyenAnhBia(int huong) {
        if (boAnh.size() <= 1) {
            return;
        }
        viTriAnh =
                (viTriAnh + huong + boAnh.size())
                        % boAnh.size();
        hienAnhBia(true);
    }
    // =====================================================
    // TỰ ĐỘNG CHUYỂN ẢNH
    // =====================================================
    private boolean coTheTuChuyenAnh() {
        return dangHienThi
                && !dangChamAnh
                && !dangMoThuVien
                && !activity.isFinishing()
                && !activity.isDestroyed()
                && activity.hasWindowFocus()
                && boAnh.size() > 1;
    }
    void batHenAnh() {
        henAnh.removeCallbacks(tuDongChuyenAnh);
        if (coTheTuChuyenAnh()) {
            henAnh.postDelayed(
                    tuDongChuyenAnh,
                    THOI_GIAN_CHUYEN_ANH
            );
        }
    }
    void dungHenAnh() {
        henAnh.removeCallbacks(tuDongChuyenAnh);
    }
    void doiFocus(boolean hasFocus) {
        if (hasFocus) {
            batHenAnh();
        } else {
            dungHenAnh();
        }
    }
    // =====================================================
    // XỬ LÝ VUỐT NGANG
    // =====================================================
    private void ganVuotAnh(
            ImageView anh,
            Runnable anhTruoc,
            Runnable anhSau,
            Runnable batDauCham,
            Runnable ketThucCham
    ) {
        final float[] diemCham = new float[2];
        int touchSlop =
                ViewConfiguration.get(activity)
                        .getScaledTouchSlop();
        anh.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    diemCham[0] = event.getX();
                    diemCham[1] = event.getY();
                    if (batDauCham != null) {
                        batDauCham.run();
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx =
                            event.getX() - diemCham[0];
                    float dy =
                            event.getY() - diemCham[1];
                    // Ưu tiên vuốt ngang, vẫn cho phép
                    // ScrollView cuộn dọc bình thường.
                    if (Math.abs(dx) > touchSlop
                            && Math.abs(dx) > Math.abs(dy)
                            && v.getParent() != null) {
                        v.getParent()
                                .requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    float khoangX =
                            event.getX() - diemCham[0];
                    float khoangY =
                            event.getY() - diemCham[1];
                    if (Math.abs(khoangX) > dp(45)
                            && Math.abs(khoangX)
                            > Math.abs(khoangY)) {
                        if (khoangX < 0) {
                            anhSau.run();
                        } else {
                            anhTruoc.run();
                        }
                    } else if (Math.abs(khoangX) < touchSlop
                            && Math.abs(khoangY) < touchSlop) {
                        v.performClick();
                    }
                    if (v.getParent() != null) {
                        v.getParent()
                                .requestDisallowInterceptTouchEvent(false);
                    }
                    if (ketThucCham != null) {
                        ketThucCham.run();
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    if (v.getParent() != null) {
                        v.getParent()
                                .requestDisallowInterceptTouchEvent(false);
                    }
                    if (ketThucCham != null) {
                        ketThucCham.run();
                    }
                    return true;
                default:
                    return true;
            }
        });
    }
    private Bitmap giaiMaAnhChiTiet(
            String tenAnh,
            BitmapFactory.Options options
    ) {
        if (TextUtils.isEmpty(tenAnh)
                || AnhDiaDiem.KHONG_ANH.equals(tenAnh)) {
            return null;
        }
        try {
            if (tenAnh.startsWith("anh_may:")) {
                String tenTep =
                        tenAnh.substring(
                                "anh_may:".length()
                        );
                if (tenTep.isEmpty()
                        || tenTep.contains("/")
                        || tenTep.contains("\\")
                        || tenTep.contains("..")) {
                    return null;
                }
                File thuMuc = new File(
                        activity.getFilesDir(),
                        "anh_dia_diem"
                );
                File tep =
                        new File(thuMuc, tenTep);
                if (!tep.isFile()) {
                    return null;
                }
                return BitmapFactory.decodeFile(
                        tep.getAbsolutePath(),
                        options
                );
            }
            int idAnh =
                    activity.getResources().getIdentifier(
                            tenAnh,
                            "drawable",
                            activity.getPackageName()
                    );
            if (idAnh == 0) {
                return null;
            }
            return BitmapFactory.decodeResource(
                    activity.getResources(),
                    idAnh,
                    options
            );
        } catch (RuntimeException e) {
            Log.e(TAG, "Không thể đọc ảnh", e);
            return null;
        }
    }
    private boolean coAnhChiTiet(String tenAnh) {
        BitmapFactory.Options options =
                new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        giaiMaAnhChiTiet(
                tenAnh,
                options
        );
        return options.outWidth > 0
                && options.outHeight > 0;
    }
    Bitmap docAnhChiTiet(
            String tenAnh,
            int kichThuoc
    ) {
        BitmapFactory.Options options =
                new BitmapFactory.Options();
        options.inScaled = false;
        options.inJustDecodeBounds = true;
        giaiMaAnhChiTiet(
                tenAnh,
                options
        );
        if (options.outWidth <= 0
                || options.outHeight <= 0) {
            return null;
        }
        options.inSampleSize = 1;
        int kichThuocGoc = Math.max(
                options.outWidth,
                options.outHeight
        );
        while (kichThuocGoc / options.inSampleSize
                > kichThuoc) {
            options.inSampleSize *= 2;
        }
        options.inJustDecodeBounds = false;
        return giaiMaAnhChiTiet(
                tenAnh,
                options
        );
    }
    // =====================================================
    // DANH SÁCH ẢNH KHÔNG TRÙNG LẶP
    // =====================================================
    private LinkedHashMap<String, String>
    layDanhSachAnhChiTiet() {
        LinkedHashMap<String, String> danhSach =
                new LinkedHashMap<>();
        if (diaDiem == null) {
            return danhSach;
        }
        // Ưu tiên ảnh địa điểm chính
        if (coAnhChiTiet(diaDiem.tenAnh)) {
            danhSach.put(
                    diaDiem.tenAnh,
                    diaDiem.ten
            );
        }
        // Bổ sung ảnh điểm thành phần
        for (DiemThamQuan diem : cacDiem) {
            if (!danhSach.containsKey(diem.tenAnh)
                    && coAnhChiTiet(diem.tenAnh)) {
                danhSach.put(
                        diem.tenAnh,
                        diem.ten
                );
            }
        }
        return danhSach;
    }
    // =====================================================
    // THƯ VIỆN ẢNH
    // =====================================================
    private void moThuVien() {
        if (boAnh.isEmpty()) {
            thongBao(
                    "Địa điểm chưa có hình ảnh."
            );
            return;
        }
        dungHenAnh();
        dangMoThuVien = true;
        ArrayList<String> cacAnh =
                new ArrayList<>(boAnh);
        LinkedHashMap<String, String> danhSach =
                new LinkedHashMap<>(tenCacAnh);
        final int[] anhDangXem = {viTriAnh};
        LinearLayout khung = new LinearLayout(activity);
        khung.setOrientation(
                LinearLayout.VERTICAL
        );
        khung.setPadding(
                dp(18),
                dp(8),
                dp(18),
                dp(12)
        );
        khung.setBackgroundColor(Color.WHITE);
        // ẢNH LỚN
        ImageView anh = new ImageView(activity);
        anh.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );
        anh.setBackgroundResource(
                R.drawable.ui_ct_thumb
        );
        anh.setClipToOutline(true);
        khung.addView(
                anh,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(280)
                )
        );
        // TÊN ẢNH
        TextView tenAnh = new TextView(activity);
        tenAnh.setGravity(Gravity.CENTER);
        tenAnh.setTextSize(16);
        tenAnh.setTextColor(MAU_TIEU_DE);
        tenAnh.setTypeface(
                null,
                Typeface.BOLD
        );
        tenAnh.setPadding(
                0, dp(14),
                0, dp(6)
        );
        khung.addView(tenAnh);
        // BỘ ĐẾM
        TextView demAnh = new TextView(activity);
        demAnh.setGravity(Gravity.CENTER);
        demAnh.setTextSize(13);
        demAnh.setTextColor(MAU_CHU_PHU);
        khung.addView(demAnh);
        // HAI NÚT CHUYỂN ẢNH
        LinearLayout thanhNut =
                new LinearLayout(activity);
        thanhNut.setOrientation(
                LinearLayout.HORIZONTAL
        );
        LinearLayout.LayoutParams lpThanh =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48)
                );
        lpThanh.topMargin = dp(16);
        khung.addView(
                thanhNut,
                lpThanh
        );
        TextView nutTruoc =
                taoNutThuVien("‹  Ảnh trước");
        TextView nutSau =
                taoNutThuVien("Ảnh tiếp  ›");
        LinearLayout.LayoutParams lpTruoc =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );
        lpTruoc.setMarginEnd(dp(8));
        thanhNut.addView(
                nutTruoc,
                lpTruoc
        );
        thanhNut.addView(
                nutSau,
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                )
        );
        // HIỂN THỊ ẢNH ĐANG CHỌN
        Runnable capNhatAnh = () -> {
            String maAnh =
                    cacAnh.get(anhDangXem[0]);
            Bitmap bitmap =
                    docAnhChiTiet(
                            maAnh,
                            1600
                    );
            if (bitmap != null) {
                anh.setImageBitmap(bitmap);
            } else {
                AnhDiaDiem.hien(anh, maAnh);
            }
            tenAnh.setText(
                    danhSach.get(maAnh)
            );
            anh.setContentDescription(
                    danhSach.get(maAnh)
            );
            demAnh.setText(
                    (anhDangXem[0] + 1)
                            + " / "
                            + cacAnh.size()
            );
            boolean coAnhTruoc =
                    anhDangXem[0] > 0;
            boolean coAnhSau =
                    anhDangXem[0] < cacAnh.size() - 1;
            nutTruoc.setEnabled(coAnhTruoc);
            nutSau.setEnabled(coAnhSau);
            nutTruoc.setAlpha(
                    coAnhTruoc ? 1f : 0.4f
            );
            nutSau.setAlpha(
                    coAnhSau ? 1f : 0.4f
            );
        };
        Runnable anhTruoc = () -> {
            if (anhDangXem[0] > 0) {
                anhDangXem[0]--;
                capNhatAnh.run();
            }
        };
        Runnable anhSau = () -> {
            if (anhDangXem[0]
                    < cacAnh.size() - 1) {
                anhDangXem[0]++;
                capNhatAnh.run();
            }
        };
        nutTruoc.setOnClickListener(v ->
                anhTruoc.run()
        );
        nutSau.setOnClickListener(v ->
                anhSau.run()
        );
        ganVuotAnh(
                anh,
                anhTruoc,
                anhSau,
                null,
                null
        );
        capNhatAnh.run();
        if (cacAnh.size() <= 1) {
            thanhNut.setVisibility(View.GONE);
        }
        ScrollView vungCuon =
                new ScrollView(activity);
        vungCuon.addView(khung);
        AlertDialog dialog =
                new MaterialAlertDialogBuilder(activity)
                        .setTitle("Thư viện hình ảnh")
                        .setView(vungCuon)
                        .setPositiveButton(
                                "Đóng",
                                null
                        )
                        .show();
        thuVien = dialog;
        // Khôi phục slider sau khi đóng thư viện
        dialog.setOnDismissListener(d -> {
            dangMoThuVien = false;
            if (thuVien == dialog) thuVien = null;
            int viTriMoi =
                    boAnh.indexOf(
                            cacAnh.get(anhDangXem[0])
                    );
            if (viTriMoi >= 0) {
                viTriAnh = viTriMoi;
                hienAnhBia(false);
            }
            batHenAnh();
        });
    }
    private TextView taoNutThuVien(
            String noiDung
    ) {
        TextView nut = new TextView(activity);
        nut.setText(noiDung);
        nut.setGravity(Gravity.CENTER);
        nut.setTextSize(13);
        nut.setTextColor(MAU_CHU_DAO);
        nut.setTypeface(
                null,
                Typeface.BOLD
        );
        nut.setBackgroundResource(
                R.drawable.ui_ct_audio_bg
        );
        nut.setClickable(true);
        nut.setFocusable(true);
        activity.ganHieuUngNhan(nut);
        return nut;
    }
}
