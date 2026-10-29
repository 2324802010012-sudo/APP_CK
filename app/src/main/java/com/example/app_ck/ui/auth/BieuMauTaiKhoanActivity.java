package com.example.app_ck.ui.auth;

import com.example.app_ck.R;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;

import android.graphics.Color;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public abstract class BieuMauTaiKhoanActivity extends BaseActivity {
    protected long taiKhoanId;
    @Override protected void onCreate(android.os.Bundle state) {
        super.onCreate(state);
        taiKhoanId = state == null ? PhienDangNhap.id(this) : state.getLong("tai_khoan_dang_sua", -1);
    }
    @Override protected void onSaveInstanceState(android.os.Bundle out) {
        super.onSaveInstanceState(out);
        out.putLong("tai_khoan_dang_sua", taiKhoanId);
    }
    protected boolean taoBieuMau(String tieuDe, String moTa, String nut) {
        if (taiKhoanId <= 0 || taiKhoanId != PhienDangNhap.id(this)) { finish(); return false; }
        setContentView(R.layout.activity_bieu_mau_tai_khoan);
        apDungKhoangTrongHeThong();
        ((TextView) findViewById(R.id.hoSoTieuDe)).setText(tieuDe);
        ((TextView) findViewById(R.id.hoSoMoTa)).setText(moTa);
        ((TextView) findViewById(R.id.hoSoLuu)).setText(nut);
        findViewById(R.id.hoSoQuayLai).setOnClickListener(v -> finish());
        return true;
    }
    protected EditText themTruong(int id, String nhan, boolean matKhau) {
        LinearLayout cha = findViewById(R.id.hoSoCacTruong);
        TextInputLayout khung = new TextInputLayout(this);
        khung.setHint(nhan);
        khung.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        khung.setBoxStrokeColor(Color.parseColor("#2C7351"));
        khung.setBoxBackgroundColor(Color.WHITE);
        khung.setDefaultHintTextColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#607467")));
        khung.setErrorTextColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#B3261E")));
        TextInputEditText nhap = new TextInputEditText(this);
        nhap.setId(id);
        nhap.setTextColor(Color.parseColor("#20382A"));
        nhap.setTextSize(16);
        nhap.setSingleLine(true);
        nhap.setInputType(InputType.TYPE_CLASS_TEXT | (matKhau ? InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_TEXT_FLAG_CAP_WORDS));
        if (matKhau) khung.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        khung.addView(nhap, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = Math.round(18 * getResources().getDisplayMetrics().density);
        cha.addView(khung, lp);
        return nhap;
    }
    protected void hoanTatBangBanPhim(EditText nhap) {
        nhap.setImeOptions(EditorInfo.IME_ACTION_DONE);
        nhap.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_DONE) { findViewById(R.id.hoSoLuu).performClick(); return true; }
            return false;
        });
    }
    protected boolean dungPhien() {
        if (taiKhoanId > 0 && taiKhoanId == PhienDangNhap.id(this)) return true;
        Toast.makeText(this, "Phiên đăng nhập đã thay đổi. Vui lòng mở lại hồ sơ.", Toast.LENGTH_LONG).show();
        finish(); return false;
    }
}
