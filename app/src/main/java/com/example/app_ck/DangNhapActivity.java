package com.example.app_ck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DangNhapActivity extends BaseActivity {

    private EditText ten;
    private EditText matKhau;

    private TextView btnDangNhap;
    private TextView btnChuyenDangKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Su dung giao dien XML moi
        setContentView(R.layout.activity_dang_nhap);
        findViewById(R.id.btnQuenMatKhau)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(
                            this,
                            QuenMatKhauActivity.class
                    );
                    startActivity(intent);
                });
        // Anh xa giao dien
        ten = findViewById(R.id.edtTenDangNhap);
        matKhau = findViewById(R.id.edtMatKhau);

        btnDangNhap = findViewById(R.id.btnDangNhap);
        btnChuyenDangKy = findViewById(R.id.btnChuyenDangKy);

        // Xu ly dang nhap
        btnDangNhap.setOnClickListener(v -> xuLyDangNhap());

        // Chuyen sang man hinh dang ky
        btnChuyenDangKy.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DangNhapActivity.this,
                    DangKyActivity.class
            );
            startActivity(intent);
        });
    }

    private void xuLyDangNhap() {

        String t = ten.getText().toString().trim();
        String m = matKhau.getText().toString();

        // Kiem tra thong tin
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

        // Kiem tra tai khoan trong SQLite
        try (TaiKhoanDb db = new TaiKhoanDb(this)) {

            long id = db.dangNhap(t, m);

            if (id < 0) {
                Toast.makeText(
                        this,
                        "Tên đăng nhập hoặc mật khẩu không đúng",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Luu phien dang nhap
            PhienDangNhap.luu(
                    this,
                    id,
                    t.toLowerCase(Locale.ROOT)
            );

            Toast.makeText(
                    this,
                    "Đăng nhập thành công!",
                    Toast.LENGTH_SHORT
            ).show();

            // Quay lai man hinh truoc
            finish();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Không thể đăng nhập. Vui lòng thử lại.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // ========================================
    // CAC HAM DUNG CHUNG CHO DANG KY
    // ========================================

    static LinearLayout khungGiaoDien(
            AppCompatActivity a,
            String t,
            String phu
    ) {
        ScrollView sc = new ScrollView(a);
        sc.setFillViewport(true);
        sc.setBackgroundColor(Color.parseColor("#F7FAF4"));

        LinearLayout g = new LinearLayout(a);
        g.setOrientation(LinearLayout.VERTICAL);
        g.setPadding(40, 70, 40, 30);

        sc.addView(g);
        a.setContentView(sc);

        TextView title = new TextView(a);
        title.setText(t);
        title.setTextSize(26);
        title.setTextColor(Color.rgb(36, 56, 45));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, Typeface.BOLD);
        g.addView(title);

        TextView sub = new TextView(a);
        sub.setText(phu);
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        sub.setTextColor(Color.parseColor("#6F806F"));
        sub.setPadding(0, 12, 0, 32);
        g.addView(sub);

        return g;
    }

    static EditText nhap(
            LinearLayout g,
            String hint,
            boolean password
    ) {
        EditText e = new EditText(g.getContext());
        e.setHint(hint);
        e.setSingleLine(true);

        e.setInputType(
                password
                        ? InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        : InputType.TYPE_CLASS_TEXT
        );

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        lp.bottomMargin = 18;
        g.addView(e, lp);

        return e;
    }

    static void nut(
            LinearLayout g,
            String text,
            Runnable action
    ) {
        Button b = new Button(g.getContext());
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(Color.WHITE);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackgroundResource(R.drawable.nen_dn_button);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        lp.topMargin = 14;
        g.addView(b, lp);

        b.setOnClickListener(v -> action.run());
    }

}
