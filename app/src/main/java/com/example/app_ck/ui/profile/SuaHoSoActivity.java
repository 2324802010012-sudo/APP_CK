
package com.example.app_ck.ui.profile;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.model.HoSoTaiKhoan;
import com.example.app_ck.ui.auth.BieuMauTaiKhoanActivity;

public class SuaHoSoActivity
        extends BieuMauTaiKhoanActivity {

    private EditText hoTen;
    private EditText email;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (!taoBieuMau(
                "Thông tin cá nhân",
                "Cập nhật thông tin hiển thị "
                        + "trên tài khoản của bạn.",
                "Lưu thay đổi"
        )) {
            return;
        }

        // Tao hai truong nhap
        hoTen = themTruong(
                R.id.hsHoTen,
                "Họ và tên",
                false
        );

        email = themTruong(
                R.id.hsEmail,
                "Email liên hệ",
                false
        );

        email.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        hoanTatBangBanPhim(email);

        hienGhiChu(
                "Họ tên và email có thể để trống. "
                        + "Email chỉ dùng làm thông tin liên hệ, "
                        + "chưa dùng để khôi phục mật khẩu."
        );

        // Lay thong tin tai khoan hien tai
        try (AppDatabase db =
                     new AppDatabase(this)) {

            HoSoTaiKhoan hs =
                    db.layHoSo(taiKhoanId);

            if (hs == null) {
                finish();
                return;
            }

            hienTaiKhoan(hs.tenDangNhap);

            if (state == null) {
                hoTen.setText(
                        hs.hoTen == null
                                ? ""
                                : hs.hoTen
                );

                email.setText(
                        hs.email == null
                                ? ""
                                : hs.email
                );
            }

        } catch (Exception e) {
            Log.e(
                    "SuaHoSoActivity",
                    "Khong tai duoc ho so",
                    e
            );

            Toast.makeText(
                    this,
                    "Không tải được hồ sơ.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        findViewById(R.id.hoSoLuu)
                .setOnClickListener(v -> luuThongTin());
    }

    private void luuThongTin() {
        if (!dungPhien()) {
            return;
        }

        String tenMoi =
                hoTen.getText().toString().trim();

        String emailMoi =
                email.getText().toString().trim();

        // Kiem tra do dai ho ten
        if (tenMoi.length() > 80) {
            hoTen.setError(
                    "Họ tên tối đa 80 ký tự."
            );

            hoTen.requestFocus();
            return;
        }

        // Kiem tra email
        if (emailMoi.length() > 254
                || (!emailMoi.isEmpty()
                && !android.util.Patterns.EMAIL_ADDRESS
                .matcher(emailMoi).matches())) {

            email.setError(
                    "Email không hợp lệ."
            );

            email.requestFocus();
            return;
        }

        try (AppDatabase db =
                     new AppDatabase(this)) {

            boolean thanhCong =
                    db.capNhatHoSo(
                            taiKhoanId,
                            tenMoi,
                            emailMoi
                    );

            if (!thanhCong) {
                Toast.makeText(
                        this,
                        "Không thể cập nhật hồ sơ.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    this,
                    "Đã cập nhật thông tin cá nhân.",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {
            Log.e(
                    "SuaHoSoActivity",
                    "Khong luu duoc ho so",
                    e
            );

            Toast.makeText(
                    this,
                    "Không thể lưu thông tin. "
                            + "Vui lòng thử lại.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
