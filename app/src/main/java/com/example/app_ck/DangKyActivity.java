package com.example.app_ck;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DangKyActivity extends BaseActivity {
    private EditText ten, matKhau, nhapLai;
    private String maDaTao, tenDaTao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        apDungKhoangTrongHeThong();

        ten = findViewById(R.id.dkTenDangNhap);
        matKhau = findViewById(R.id.dkMatKhau);
        nhapLai = findViewById(R.id.dkNhapLaiMatKhau);
        TextView nutDangKy = findViewById(R.id.dkNutDangKy);
        TextView nutDangNhap = findViewById(R.id.dkNutDangNhap);
        TextView nutQuayLai = findViewById(R.id.dkNutQuayLai);

        nutDangKy.setOnClickListener(v -> dangKy());
        nutDangNhap.setOnClickListener(v -> finish());
        nutQuayLai.setOnClickListener(v -> finish());
        if (savedInstanceState != null) {
            maDaTao = savedInstanceState.getString("ma_da_tao");
            tenDaTao = savedInstanceState.getString("ten_da_tao");
            if (maDaTao != null) hienMaKhoiPhuc();
        }
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
                maDaTao = maKhoiPhuc;
                tenDaTao = t;
                hienMaKhoiPhuc();
            } else {
                ten.setError("Tên đăng nhập đã tồn tại hoặc không thể tạo tài khoản");
                ten.requestFocus();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Không thể đăng ký. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }
    private void hienMaKhoiPhuc() {
        AlertDialog hopThoai = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Đăng ký thành công")
                .setMessage("Mã khôi phục tài khoản của bạn:\n\n" + maDaTao
                        + "\n\nHãy lưu mã này để dùng khi quên mật khẩu. Sau đó đăng nhập bằng tài khoản vừa tạo.")
                .setCancelable(false)
                .setPositiveButton("Tôi đã lưu mã", (dialog, which) -> {
                    setResult(RESULT_OK, new android.content.Intent()
                            .putExtra(DangNhapActivity.TEN_DANG_NHAP, tenDaTao));
                    finish();
                }).show();
        ((TextView) hopThoai.findViewById(android.R.id.message)).setTextIsSelectable(true);
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("ma_da_tao", maDaTao);
        out.putString("ten_da_tao", tenDaTao);
    }
}
