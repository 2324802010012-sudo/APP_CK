package com.example.app_ck.ui.auth;

import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.home.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.Locale;

public class DangNhapActivity extends BaseActivity {
    public static final String VE_TRANG_CHU = "ve_trang_chu";
    public static final String MO_DANG_KY = "mo_dang_ky";
    public static final String TEN_DANG_NHAP = "ten_dang_nhap";
    private EditText ten, matKhau;
    private final ActivityResultLauncher<Intent> nhanTaiKhoan = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ten.setText(result.getData().getStringExtra(TEN_DANG_NHAP));
                    matKhau.setText("");
                    matKhau.requestFocus();
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_dang_nhap);
        apDungKhoangTrongHeThong();
        ten = findViewById(R.id.edtTenDangNhap);
        matKhau = findViewById(R.id.edtMatKhau);
        findViewById(R.id.dnQuayLai).setOnClickListener(v -> finish());
        findViewById(R.id.btnDangNhap).setOnClickListener(v -> dangNhap());
        findViewById(R.id.btnChuyenDangKy).setOnClickListener(v ->
                nhanTaiKhoan.launch(new Intent(this, DangKyActivity.class)));
        findViewById(R.id.btnQuenMatKhau).setOnClickListener(v ->
                nhanTaiKhoan.launch(new Intent(this, QuenMatKhauActivity.class)
                        .putExtra(TEN_DANG_NHAP, ten.getText().toString().trim())));
        matKhau.setImeOptions(EditorInfo.IME_ACTION_DONE);
        matKhau.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) { dangNhap(); return true; }
            return false;
        });
        if (state == null && getIntent().getBooleanExtra(MO_DANG_KY, false)) {
            nhanTaiKhoan.launch(new Intent(this, DangKyActivity.class));
        }
    }

    private void dangNhap() {
        String t = ten.getText().toString().trim();
        String m = matKhau.getText().toString();
        if (t.isEmpty()) { ten.setError("Vui lòng nhập tên đăng nhập"); ten.requestFocus(); return; }
        if (m.isEmpty()) { matKhau.setError("Vui lòng nhập mật khẩu"); matKhau.requestFocus(); return; }
        try (AppDatabase db = new AppDatabase(this)) {
            long id = db.dangNhap(t, m);
            if (id < 0) {
                matKhau.setError("Tên đăng nhập hoặc mật khẩu không đúng");
                matKhau.requestFocus();
                return;
            }
            PhienDangNhap.luu(this, id, t.toLowerCase(Locale.ROOT));
            setResult(RESULT_OK);
            if (getIntent().getBooleanExtra(VE_TRANG_CHU, false)) {
                startActivity(new Intent(this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Không thể đăng nhập. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
        }
    }
}
