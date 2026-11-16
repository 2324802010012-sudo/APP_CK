
package com.example.app_ck.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.DiaDiem;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.data.repository.KhoNoiDungThamQuan;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.auth.DangNhapActivity;
import com.example.app_ck.ui.common.AnhDiaDiem;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.common.ThanhDieuHuongDuoi;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.tour.DieuHuongThamQuan;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class LichSuActivity extends BaseActivity {

    private static final String TAG = "LichSuActivity";

    private LinearLayout danhSach;

    private final ActivityResultLauncher<Intent> dangNhap =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> hienThi()
            );

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_lich_su);
        apDungKhoangTrongHeThong();

        danhSach = findViewById(R.id.lsDanhSach);

        findViewById(R.id.lsQuayLai)
                .setOnClickListener(v -> finish());

        findViewById(R.id.lsXoaTatCa)
                .setOnClickListener(v -> xacNhanXoa(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        hienThi();
    }

    private void moDangNhap() {
        dangNhap.launch(
                new Intent(this, DangNhapActivity.class)
        );
    }

    private void chu(int id, String noiDung) {
        TextView view = findViewById(id);

        if (view != null) {
            view.setText(noiDung);
        }
    }

    private void hienThiTrangThai(
            String tieuDe,
            String moTa,
            String nut,
            Runnable hanhDong
    ) {
        findViewById(R.id.lsTrangThai)
                .setVisibility(View.VISIBLE);

        chu(R.id.lsTieuDeTrangThai, tieuDe);
        chu(R.id.lsMoTaTrangThai, moTa);
        chu(R.id.lsNutTrangThai, nut);

        findViewById(R.id.lsNutTrangThai)
                .setOnClickListener(
                        v -> hanhDong.run()
                );
    }

    private void hienThi() {
        if (danhSach == null) {
            return;
        }

        danhSach.removeAllViews();

        findViewById(R.id.lsXoaTatCa)
                .setVisibility(View.GONE);

        findViewById(R.id.lsTrangThai)
                .setVisibility(View.GONE);

        if (!PhienDangNhap.daDangNhap(this)) {
            chu(
                    R.id.lsTaiKhoan,
                    "Lịch sử riêng của tài khoản"
            );

            chu(R.id.lsTongSo, "");

            hienThiTrangThai(
                    "Đăng nhập để xem lịch sử",
                    "Đăng nhập để xem lại những lượt "
                            + "thuyết minh đã lưu trên thiết bị.",
                    "Đăng nhập",
                    this::moDangNhap
            );

            return;
        }

        final long taiKhoanId =
                PhienDangNhap.id(this);

        chu(
                R.id.lsTaiKhoan,
                "Các lượt nghe của "
                        + PhienDangNhap.ten(this)
        );

        try (AppDatabase db = new AppDatabase(this)) {

            ArrayList<KhoLichSu.Luot> luotNghe =
                    KhoLichSu.doc(
                            this,
                            db.getReadableDatabase(),
                            taiKhoanId
                    );

            chu(
                    R.id.lsTongSo,
                    luotNghe.size()
                            + " lượt nghe · Mới nhất trước"
            );

            if (luotNghe.isEmpty()) {
                hienThiTrangThai(
                        "Chưa có lịch sử nghe",
                        "Hãy khám phá một địa điểm và "
                                + "bắt đầu thuyết minh. "
                                + "Lượt nghe sẽ được lưu tại đây.",
                        "Khám phá địa điểm",
                        () -> ThanhDieuHuongDuoi.mo(
                                this,
                                ThanhDieuHuongDuoi.KHAM_PHA
                        )
                );

                return;
            }

            findViewById(R.id.lsXoaTatCa)
                    .setVisibility(View.VISIBLE);

            // Lay anh dia diem mot lan tu SQLite
            HashMap<Integer, String> anhTheoId =
                    new HashMap<>();

            for (DiaDiem d : db.danhSachDiaDiem()) {
                anhTheoId.put(d.id, d.tenAnh);
            }

            SimpleDateFormat dinhDang =
                    new SimpleDateFormat(
                            "dd/MM/yyyy · HH:mm",
                            new Locale("vi", "VN")
                    );

            for (KhoLichSu.Luot luot : luotNghe) {
                themTheLichSu(
                        luot,
                        anhTheoId.get(luot.diaDiem),
                        taiKhoanId,
                        dinhDang
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Không tải được lịch sử", e);

            danhSach.removeAllViews();

            findViewById(R.id.lsXoaTatCa)
                    .setVisibility(View.GONE);

            chu(R.id.lsTongSo, "");

            hienThiTrangThai(
                    "Chưa tải được lịch sử",
                    "Có lỗi khi đọc dữ liệu. "
                            + "Vui lòng thử lại.",
                    "Thử lại",
                    this::hienThi
            );
        }
    }

    private void themTheLichSu(
            KhoLichSu.Luot luot,
            String tenAnh,
            long taiKhoanId,
            SimpleDateFormat dinhDang
    ) {
        View the = getLayoutInflater().inflate(
                R.layout.item_lich_su,
                danhSach,
                false
        );

        ImageView anh = the.findViewById(
                R.id.lsAnhDiaDiem
        );

        AnhDiaDiem.hien(anh, tenAnh);

        ((TextView) the.findViewById(
                R.id.lsTenDiaDiem
        )).setText(luot.tenDiaDiem);

        ((TextView) the.findViewById(
                R.id.lsTenDiem
        )).setText(luot.tenDiem);

        ((TextView) the.findViewById(
                R.id.lsCheDo
        )).setText(
                KhoLichSu.nhanCheDo(luot.cheDo)
        );

        ((TextView) the.findViewById(
                R.id.lsThoiGian
        )).setText(
                dinhDang.format(
                        new Date(luot.thoiGian)
                )
        );

        the.setContentDescription(
                "Mở điểm tham quan "
                        + luot.tenDiaDiem
                        + ", "
                        + luot.tenDiem
        );

        // Bam vao the de mo lai dia diem
        the.setOnClickListener(v ->
                moLaiDiaDiem(luot, taiKhoanId)
        );

        // Ba cham dung nghia la menu tuy chon
        View nutMenu = the.findViewById(
                R.id.lsXoaLuot
        );

        nutMenu.setOnClickListener(v ->
                moMenuLichSu(
                        nutMenu,
                        luot,
                        taiKhoanId
                )
        );

        danhSach.addView(the);
    }

    private void moMenuLichSu(
            View nutMenu,
            KhoLichSu.Luot luot,
            long taiKhoanId
    ) {
        PopupMenu menu = new PopupMenu(
                this,
                nutMenu
        );

        menu.getMenu().add(
                0, 1, 0, "Mở điểm tham quan"
        );

        menu.getMenu().add(
                0, 2, 1, "Xóa lượt nghe"
        );

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                moLaiDiaDiem(luot, taiKhoanId);
                return true;
            }

            if (item.getItemId() == 2) {
                if (taiKhoanId != PhienDangNhap.id(this)) {
                    hienThi();
                    return true;
                }

                xacNhanXoa(luot.id);
                return true;
            }

            return false;
        });

        menu.show();
    }

    private void moLaiDiaDiem(
            KhoLichSu.Luot luot,
            long taiKhoanId
    ) {
        if (taiKhoanId != PhienDangNhap.id(this)) {
            hienThi();
            return;
        }

        try {
            startActivity(
                    taoIntentMoLai(this, luot)
            );
        } catch (Exception e) {
            Log.e(TAG, "Không mở được điểm tham quan", e);

            Toast.makeText(
                    this,
                    "Điểm tham quan không còn khả dụng.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Giu tuong thich voi Trang chu
    public static Intent taoIntentMoLai(
            Context context,
            KhoLichSu.Luot luot
    ) {
        try (AppDatabase db =
                     new AppDatabase(context)) {

            KhoNoiDungThamQuan.lay(
                    db.getReadableDatabase(),
                    luot.diaDiem,
                    luot.diem
            );
        }

        if (luot.diem == 0) {
            return new Intent(
                    context,
                    ChiTietDiaDiemActivity.class
            ).putExtra(
                    "viTriDiaDiem",
                    luot.diaDiem
            );
        }

        return DieuHuongThamQuan.taoIntent(
                context,
                luot.diaDiem,
                luot.diem
        );
    }

    private void xacNhanXoa(Long idLuot) {
        final long taiKhoanId =
                PhienDangNhap.id(this);

        if (taiKhoanId <= 0) {
            hienThi();
            return;
        }

        boolean xoaTatCa = idLuot == null;

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        xoaTatCa
                                ? "Xóa toàn bộ lịch sử?"
                                : "Xóa lượt nghe?"
                )
                .setMessage(
                        xoaTatCa
                                ? "Tất cả lượt nghe của tài khoản "
                                + "này sẽ bị xóa. Thao tác không thể "
                                + "hoàn tác."
                                : "Lượt nghe này sẽ được xóa khỏi "
                                + "lịch sử của bạn."
                )
                .setNegativeButton("Hủy", null)
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {
                            if (taiKhoanId !=
                                    PhienDangNhap.id(this)) {
                                hienThi();
                                return;
                            }

                            try (AppDatabase db =
                                         new AppDatabase(this)) {

                                KhoLichSu.xoa(
                                        this,
                                        db.getWritableDatabase(),
                                        taiKhoanId,
                                        idLuot
                                );

                                Toast.makeText(
                                        this,
                                        "Đã cập nhật lịch sử.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } catch (Exception e) {
                                Log.e(
                                        TAG,
                                        "Không xóa được lịch sử",
                                        e
                                );

                                Toast.makeText(
                                        this,
                                        "Không thể xóa lịch sử.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            hienThi();
                        }
                )
                .show();
    }
}
