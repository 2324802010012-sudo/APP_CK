package com.example.app_ck.ui.auth;
import androidx.appcompat.app.AlertDialog;
import com.example.app_ck.R;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.admin.AdminActivity;
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

    private EditText ten;
    private EditText matKhau;

    private final ActivityResultLauncher<Intent> nhanTaiKhoan =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null) {

                            ten.setText(
                                    result.getData().getStringExtra(
                                            TEN_DANG_NHAP
                                    )
                            );

                            matKhau.setText("");
                            matKhau.requestFocus();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_dang_nhap);
        apDungKhoangTrongHeThong();

        ten = findViewById(R.id.edtTenDangNhap);
        matKhau = findViewById(R.id.edtMatKhau);

        findViewById(R.id.dnQuayLai)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btnDangNhap)
                .setOnClickListener(v -> dangNhap());

        findViewById(R.id.btnChuyenDangKy)
                .setOnClickListener(v ->
                        nhanTaiKhoan.launch(
                                new Intent(
                                        this,
                                        DangKyActivity.class
                                )
                        )
                );

        findViewById(R.id.btnQuenMatKhau)
                .setOnClickListener(v ->
                        nhanTaiKhoan.launch(
                                new Intent(
                                        this,
                                        QuenMatKhauActivity.class
                                ).putExtra(
                                        TEN_DANG_NHAP,
                                        ten.getText().toString().trim()
                                )
                        )
                );

        matKhau.setImeOptions(EditorInfo.IME_ACTION_DONE);

        matKhau.setOnEditorActionListener(
                (v, action, event) -> {
                    if (action == EditorInfo.IME_ACTION_DONE) {
                        dangNhap();
                        return true;
                    }
                    return false;
                }
        );

        if (state == null
                && getIntent().getBooleanExtra(
                MO_DANG_KY, false
        )) {

            nhanTaiKhoan.launch(
                    new Intent(this, DangKyActivity.class)
            );
        }
    }

    private void dangNhap() {

        String t = ten.getText().toString().trim();
        String m = matKhau.getText().toString();

        if (t.isEmpty()) {
            ten.setError("Vui lòng nhập tên đăng nhập");
            ten.requestFocus();
            return;
        }

        if (m.isEmpty()) {
            matKhau.setError("Vui lòng nhập mật khẩu");
            matKhau.requestFocus();
            return;
        }

        try (AppDatabase database = new AppDatabase(this)) {

            // 1. Xác thực tên đăng nhập và mật khẩu.
            long id = database.dangNhap(t, m);


            if (id < 0) {

                // Kiểm tra tài khoản bị Admin khóa.
                // Chỉ trả về true nếu đúng tên đăng nhập và mật khẩu.
                if (database.taiKhoanBiKhoa(t, m)) {

                    matKhau.setText("");

                    new AlertDialog.Builder(this)
                            .setTitle("Tài khoản đã bị khóa")
                            .setMessage(
                                    "Tài khoản của bạn đã bị quản trị viên khóa.\n\n"
                                            + "Bạn hiện không thể đăng nhập hoặc "
                                            + "sử dụng các chức năng yêu cầu tài khoản.\n\n"
                                            + "Vui lòng liên hệ quản trị viên "
                                            + "để được hỗ trợ mở khóa."
                            )
                            .setPositiveButton("Đã hiểu", null)
                            .show();

                    return;
                }

                // Sai tên đăng nhập hoặc mật khẩu.
                matKhau.setError(
                        "Tên đăng nhập hoặc mật khẩu không đúng"
                );
                matKhau.requestFocus();
                return;
            }
            // 2. Lưu phiên đăng nhập hợp lệ.
            PhienDangNhap.luu(
                    this,
                    id,
                    t.toLowerCase(Locale.ROOT)
            );

            // 3. Tự nhận diện vai trò.
            boolean laAdmin = PhienDangNhap.laAdmin(this);

            setResult(RESULT_OK);

            if (laAdmin) {

                // 4A. ADMIN mở thẳng màn hình quản trị.
                Intent intent = new Intent(
                        this,
                        AdminActivity.class
                );

                startActivity(intent);
                finish();
                return;
            }

            // 4B. USER giữ nguyên luồng đăng nhập cũ.
            if (getIntent().getBooleanExtra(
                    VE_TRANG_CHU, false
            )) {

                Intent intent = new Intent(
                        this,
                        MainActivity.class
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
            }

            finish();

        } catch (Exception e) {

            android.util.Log.e(
                    "APP_CK_DANG_NHAP",
                    "Lỗi trong quá trình đăng nhập",
                    e
            );

            Toast.makeText(
                    this,
                    "Không thể đăng nhập. Vui lòng thử lại.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
