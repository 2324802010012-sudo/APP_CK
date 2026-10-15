package com.example.app_ck;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class WelcomeActivity extends AppCompatActivity {
    private Button nutBatDau;
    private TextView nutDangNhap;
    @Override
    protected void onCreate(Bundle trangThaiDaLuu) {
        super.onCreate(trangThaiDaLuu);
        setContentView(R.layout.activity_welcome);
        nutBatDau = findViewById(R.id.btnBatDau);
        nutDangNhap = findViewById(R.id.btnDangNhap);
        nutBatDau.setOnClickListener(v -> {
            Intent chuyenTrang = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(chuyenTrang);
            finish();
        });
        nutDangNhap.setOnClickListener(v -> {
            Toast.makeText(WelcomeActivity.this, "Chức năng đăng nhập đang được xây dựng", Toast.LENGTH_SHORT).show();
        });
    }
}
