package com.example.app_ck;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DangKyActivity extends AppCompatActivity {
    private EditText ten, matKhau, nhapLai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        ten = findViewById(R.id.dkTenDangNhap);
        matKhau = findViewById(R.id.dkMatKhau);
        nhapLai = findViewById(R.id.dkNhapLaiMatKhau);
        TextView nutDangKy = findViewById(R.id.dkNutDangKy);
        TextView nutDangNhap = findViewById(R.id.dkNutDangNhap);
        TextView nutQuayLai = findViewById(R.id.dkNutQuayLai);

        nutDangKy.setOnClickListener(v -> dangKy());
        nutDangNhap.setOnClickListener(v -> finish());
        nutQuayLai.setOnClickListener(v -> finish());
    }

    private void dangKy() {
        String t = ten.getText().toString().trim();
        String m = matKhau.getText().toString();
        String xacNhan = nhapLai.getText().toString();

        if (!t.matches("[A-Za-z0-9_]{3,30}")) {
            ten.setError("Tên đăng nhập gồm 3–30 ký tự chữ, số hoặc _");
            ten.requestFocus();
            return;
        }
        if (m.length() < 8) {
            matKhau.setError("Mật khẩu phải có ít nhất 8 ký tự");
            matKhau.requestFocus();
            return;
        }
        if (!m.equals(xacNhan)) {
            nhapLai.setError("Mật khẩu nhập lại không khớp");
            nhapLai.requestFocus();
            return;
        }
        String maKhoiPhuc = TaiKhoanDb.taoMaKhoiPhuc();
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {
            if (db.dangKy(t, m, maKhoiPhuc)) {
                new AlertDialog.Builder(this)
                        .setTitle("Đăng ký thành công")
                        .setMessage("Mã khôi phục tài khoản của bạn:\n\n" + maKhoiPhuc
                                + "\n\nHãy lưu mã này ở nơi an toàn để dùng khi quên mật khẩu. Không chia sẻ mã với người khác.")
                        .setCancelable(false)
                        .setPositiveButton("Tôi đã lưu mã", (dialog, which) -> finish())
                        .show();
            } else {
                ten.setError("Tên đăng nhập đã tồn tại hoặc không thể tạo tài khoản");
                ten.requestFocus();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Không thể đăng ký. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }
}
