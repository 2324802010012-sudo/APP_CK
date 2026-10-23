package com.example.app_ck;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
public class WelcomeActivity extends BaseActivity {
    private Button nutBatDau;
    private TextView nutDangNhap;
    @Override
    protected void onCreate(Bundle trangThaiDaLuu) {
        super.onCreate(trangThaiDaLuu);
        setContentView(R.layout.activity_welcome);
        apDungKhoangTrongHeThong();
        nutBatDau = findViewById(R.id.btnBatDau);
        nutDangNhap = findViewById(R.id.btnDangNhap);
        nutBatDau.setOnClickListener(v -> {
            Intent chuyenTrang = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(chuyenTrang);
            finish();
        });
        nutDangNhap.setOnClickListener(v -> {
            if (PhienDangNhap.daDangNhap(this)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, DangNhapActivity.class)
                        .putExtra(DangNhapActivity.VE_TRANG_CHU, true));
            }
        });
    }
}
