package com.example.app_ck.ui.profile;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.ui.auth.BieuMauTaiKhoanActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

public class DoiMatKhauActivity extends BieuMauTaiKhoanActivity {
    private EditText cu, moi, xacNhan;
    private String maMoi;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!taoBieuMau("Đổi mật khẩu", "Xác nhận mật khẩu hiện tại để bảo vệ tài khoản. Đổi thành công sẽ cấp mã khôi phục mới.", "Cập nhật mật khẩu")) return;
        cu = themTruong(R.id.hsMatKhauCu, "Mật khẩu hiện tại", true);
        moi = themTruong(R.id.hsMatKhauMoi, "Mật khẩu mới, tối thiểu 8 ký tự", true);
        xacNhan = themTruong(R.id.hsXacNhan, "Nhập lại mật khẩu mới", true);
        hoanTatBangBanPhim(xacNhan);
        findViewById(R.id.hoSoLuu).setOnClickListener(v -> doi());
        if (state != null) { maMoi = state.getString("ma_moi"); if (maMoi != null) hienMa(); }
    }
    private void doi() {
        if (!dungPhien()) return;
        String c = cu.getText().toString(), m = moi.getText().toString();
        if (c.isEmpty()) { cu.setError("Nhập mật khẩu hiện tại"); cu.requestFocus(); return; }
        if (m.length() < 8) { moi.setError("Mật khẩu tối thiểu 8 ký tự"); moi.requestFocus(); return; }
        if (m.equals(c)) { moi.setError("Mật khẩu mới phải khác mật khẩu hiện tại"); moi.requestFocus(); return; }
        if (!m.equals(xacNhan.getText().toString())) { xacNhan.setError("Mật khẩu nhập lại không khớp"); xacNhan.requestFocus(); return; }
        try (AppDatabase db = new AppDatabase(this)) {
            maMoi = db.doiMatKhauDaDangNhap(taiKhoanId, c, m);
            if (maMoi == null) { cu.setError("Mật khẩu hiện tại không đúng hoặc không thể cập nhật"); cu.requestFocus(); return; }
            cu.setText(""); moi.setText(""); xacNhan.setText("");
            hienMa();
        } catch (Exception e) { Toast.makeText(this, "Không thể đổi mật khẩu. Vui lòng thử lại.", Toast.LENGTH_LONG).show(); }
    }
    private void hienMa() {
        AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this).setTitle("Đổi mật khẩu thành công")
                .setMessage("Mã khôi phục mới của bạn:\n\n" + maMoi + "\n\nHãy lưu mã này ở nơi an toàn. Mã cũ đã hết hiệu lực.")
                .setCancelable(false).setPositiveButton("Tôi đã lưu mã", (d, w) -> { setResult(RESULT_OK); finish(); }).show();
        ((TextView) dialog.findViewById(android.R.id.message)).setTextIsSelectable(true);
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putString("ma_moi", maMoi); }
}
