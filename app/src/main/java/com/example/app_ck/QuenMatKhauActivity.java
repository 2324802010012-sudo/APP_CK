package com.example.app_ck;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class QuenMatKhauActivity extends BaseActivity {
    private EditText ten, ma, matKhauMoi, xacNhan;
    private String maDaDoi, tenDaDoi;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_quen_mat_khau);
        apDungKhoangTrongHeThong();
        ten = findViewById(R.id.qmkTenDangNhap);
        ma = findViewById(R.id.qmkMaKhoiPhuc);
        matKhauMoi = findViewById(R.id.qmkMatKhauMoi);
        xacNhan = findViewById(R.id.qmkXacNhan);
        findViewById(R.id.qmkQuayLai).setOnClickListener(v -> finish());
        findViewById(R.id.qmkXacNhanDoi).setOnClickListener(v -> doiMatKhau());
        if (state == null) ten.setText(getIntent().getStringExtra(DangNhapActivity.TEN_DANG_NHAP));
        else {
            maDaDoi = state.getString("ma_da_doi");
            tenDaDoi = state.getString("ten_da_doi");
            if (maDaDoi != null) hienMaMoi();
        }
    }
    private void doiMatKhau() {
        String t = ten.getText().toString().trim();
        String k = ma.getText().toString().trim();
        String m = matKhauMoi.getText().toString();
        if (t.isEmpty()) { ten.setError("Nhập tên đăng nhập"); ten.requestFocus(); return; }
        if (k.isEmpty()) { ma.setError("Nhập mã khôi phục"); ma.requestFocus(); return; }
        if (m.length() < 8) { matKhauMoi.setError("Mật khẩu tối thiểu 8 ký tự"); matKhauMoi.requestFocus(); return; }
        if (!m.equals(xacNhan.getText().toString())) { xacNhan.setError("Mật khẩu không khớp"); xacNhan.requestFocus(); return; }
        String maMoi;
        try (TaiKhoanDb db = new TaiKhoanDb(this)) { maMoi = db.doiMatKhauBangMa(t,k,m); }
        catch (Exception e) {
            Toast.makeText(this, "Không thể đổi mật khẩu. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return;
        }
        if (maMoi == null) {
            Toast.makeText(this,"Không thể khôi phục. Kiểm tra tên đăng nhập và mã khôi phục.",Toast.LENGTH_LONG).show();
            return;
        }
        maDaDoi = maMoi;
        tenDaDoi = t;
        if (PhienDangNhap.ten(this).equalsIgnoreCase(t)) PhienDangNhap.dangXuat(this);
        hienMaMoi();
    }

    private void hienMaMoi() {
        AlertDialog hopThoai = new AlertDialog.Builder(this)
            .setTitle("Đổi mật khẩu thành công")
            .setMessage("Mã khôi phục mới của bạn:\n\n" + maDaDoi + "\n\nHãy lưu mã này ở nơi an toàn. Mã cũ đã hết hiệu lực. Đăng nhập lại bằng mật khẩu mới.")
            .setCancelable(false)
            .setPositiveButton("Tôi đã lưu mã", (d,w) -> {
                setResult(RESULT_OK, new android.content.Intent()
                        .putExtra(DangNhapActivity.TEN_DANG_NHAP, tenDaDoi));
                finish();
            })
            .show();
        ((TextView) hopThoai.findViewById(android.R.id.message)).setTextIsSelectable(true);
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("ma_da_doi", maDaDoi);
        out.putString("ten_da_doi", tenDaDoi);
    }
}
