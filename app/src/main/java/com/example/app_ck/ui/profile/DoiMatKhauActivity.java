
package com.example.app_ck.ui.profile;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.ui.auth.BieuMauTaiKhoanActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DoiMatKhauActivity
        extends BieuMauTaiKhoanActivity {

    private EditText matKhauCu;
    private EditText matKhauMoi;
    private EditText xacNhanMatKhau;

    private AlertDialog dialogMa;

    // Giu ma qua thao tac xoay man hinh,
    // khong ghi ma vao Bundle hay SQLite.
    public static class MaKhoiPhucModel
            extends ViewModel {

        String maKhoiPhuc;
    }

    private MaKhoiPhucModel maModel;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (!taoBieuMau(
                "Đổi mật khẩu",
                "Sử dụng mật khẩu mới để bảo vệ "
                        + "tài khoản của bạn.",
                "Cập nhật mật khẩu"
        )) {
            return;
        }

        maModel = new ViewModelProvider(this)
                .get(MaKhoiPhucModel.class);

        matKhauCu = themTruong(
                R.id.hsMatKhauCu,
                "Mật khẩu hiện tại",
                true
        );

        matKhauMoi = themTruong(
                R.id.hsMatKhauMoi,
                "Mật khẩu mới",
                true
        );

        xacNhanMatKhau = themTruong(
                R.id.hsXacNhan,
                "Nhập lại mật khẩu mới",
                true
        );

        hienGhiChu(
                "Mật khẩu mới cần tối thiểu 8 ký tự "
                        + "và khác mật khẩu hiện tại. "
                        + "Sau khi đổi thành công, ứng dụng "
                        + "sẽ cấp mã khôi phục mới."
        );

        hoanTatBangBanPhim(xacNhanMatKhau);

        findViewById(R.id.hoSoLuu)
                .setOnClickListener(v -> doiMatKhau());

        // Neu xoay man hinh khi ma dang hien,
        // mo lai dialog ma van giu trong bo nho.
        if (maModel.maKhoiPhuc != null) {
            findViewById(R.id.hoSoLuu)
                    .post(this::hienThiMaKhoiPhuc);
        }
    }

    private void doiMatKhau() {
        if (!dungPhien()) {
            return;
        }

        String cu =
                matKhauCu.getText().toString();

        String moi =
                matKhauMoi.getText().toString();

        String xacNhan =
                xacNhanMatKhau.getText().toString();

        if (cu.isEmpty()) {
            matKhauCu.setError(
                    "Vui lòng nhập mật khẩu hiện tại."
            );

            matKhauCu.requestFocus();
            return;
        }

        if (moi.length() < 8) {
            matKhauMoi.setError(
                    "Mật khẩu tối thiểu 8 ký tự."
            );

            matKhauMoi.requestFocus();
            return;
        }

        if (moi.equals(cu)) {
            matKhauMoi.setError(
                    "Mật khẩu mới phải khác mật khẩu cũ."
            );

            matKhauMoi.requestFocus();
            return;
        }

        if (!moi.equals(xacNhan)) {
            xacNhanMatKhau.setError(
                    "Mật khẩu xác nhận không khớp."
            );

            xacNhanMatKhau.requestFocus();
            return;
        }

        try (AppDatabase db =
                     new AppDatabase(this)) {

            String maMoi =
                    db.doiMatKhauDaDangNhap(
                            taiKhoanId,
                            cu,
                            moi
                    );

            if (maMoi == null) {
                matKhauCu.setError(
                        "Mật khẩu hiện tại không đúng "
                                + "hoặc không thể cập nhật."
                );

                matKhauCu.requestFocus();
                return;
            }

            // Xoa noi dung nhap sau khi thanh cong
            matKhauCu.setText("");
            matKhauMoi.setText("");
            xacNhanMatKhau.setText("");

            maModel.maKhoiPhuc = maMoi;

            hienThiMaKhoiPhuc();

        } catch (Exception e) {
            android.util.Log.e(
                    "DoiMatKhauActivity",
                    "Khong the doi mat khau",
                    e
            );

            Toast.makeText(
                    this,
                    "Không thể đổi mật khẩu. "
                            + "Vui lòng thử lại.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void hienThiMaKhoiPhuc() {
        if (isFinishing()
                || isDestroyed()
                || maModel == null
                || maModel.maKhoiPhuc == null) {
            return;
        }

        if (dialogMa != null
                && dialogMa.isShowing()) {
            return;
        }

        View giaoDien =
                getLayoutInflater().inflate(
                        R.layout.dialog_ma_khoi_phuc,
                        null
                );

        TextView hienMa = giaoDien.findViewById(
                R.id.mkMa
        );

        hienMa.setText(
                maModel.maKhoiPhuc
        );

        String maHienTai =
                maModel.maKhoiPhuc;

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle(
                                "Đổi mật khẩu thành công"
                        )
                        .setView(giaoDien)
                        .setCancelable(false)
                        .setPositiveButton(
                                "Tôi đã lưu mã",
                                (d, which) -> {
                                    maModel.maKhoiPhuc = null;

                                    setResult(RESULT_OK);
                                    finish();
                                }
                        )
                        .create();

        dialogMa = dialog;

        giaoDien.findViewById(R.id.mkSaoChep)
                .setOnClickListener(v ->
                        saoChepMa(maHienTai)
                );

        dialog.setOnDismissListener(d -> {
            if (dialogMa == dialog) {
                dialogMa = null;
            }
        });

        dialog.show();

        dialog.setCanceledOnTouchOutside(false);

        // Ngan anh chup man hinh dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SECURE
            );
        }
    }

    private void saoChepMa(String ma) {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE
                );

        if (clipboard == null) {
            Toast.makeText(
                    this,
                    "Không thể truy cập bộ nhớ tạm.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ClipData clip =
                ClipData.newPlainText(
                        "Mã khôi phục",
                        ma
                );

        // Android 13+: an noi dung nhay cam
        // trong phan xem truoc clipboard.
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU) {

            PersistableBundle extras =
                    new PersistableBundle();

            extras.putBoolean(
                    ClipDescription.EXTRA_IS_SENSITIVE,
                    true
            );

            clip.getDescription().setExtras(
                    extras
            );
        }

        clipboard.setPrimaryClip(clip);

        Toast.makeText(
                this,
                "Đã sao chép mã. "
                        + "Hãy lưu ở nơi an toàn.",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {
        if (dialogMa != null) {
            dialogMa.dismiss();
            dialogMa = null;
        }

        super.onDestroy();
    }
}
