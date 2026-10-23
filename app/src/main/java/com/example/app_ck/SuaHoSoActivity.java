package com.example.app_ck;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

public class SuaHoSoActivity extends BieuMauTaiKhoanActivity {
    private EditText hoTen, email;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!taoBieuMau("Thông tin cá nhân", "Tên đăng nhập được giữ nguyên. Email liên hệ không dùng để đăng nhập hoặc khôi phục mật khẩu.", "Lưu thay đổi")) return;
        hoTen = themTruong(R.id.hsHoTen, "Họ và tên (không bắt buộc)", false);
        email = themTruong(R.id.hsEmail, "Email liên hệ (không bắt buộc)", false);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        hoanTatBangBanPhim(email);
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            HoSoTaiKhoan hs = db.layHoSo(taiKhoanId);
            if (hs == null) { finish(); return; }
            if (state == null) { hoTen.setText(hs.hoTen); email.setText(hs.email); }
        }
        findViewById(R.id.hoSoLuu).setOnClickListener(v -> luu());
    }
    private void luu() {
        if (!dungPhien()) return;
        String h = hoTen.getText().toString().trim(), e = email.getText().toString().trim();
        if (h.length() > 80) { hoTen.setError("Họ tên tối đa 80 ký tự"); hoTen.requestFocus(); return; }
        if (e.length() > 254 || (!e.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches())) {
            email.setError("Email không hợp lệ"); email.requestFocus(); return;
        }
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            if (db.capNhatHoSo(taiKhoanId, h, e)) {
                setResult(RESULT_OK); finish();
            } else Toast.makeText(this, "Không thể lưu hồ sơ. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) { Toast.makeText(this, "Không thể lưu hồ sơ.", Toast.LENGTH_LONG).show(); }
    }
}
