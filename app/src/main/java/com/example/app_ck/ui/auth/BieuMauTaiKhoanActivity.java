
package com.example.app_ck.ui.auth;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.app_ck.R;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public abstract class BieuMauTaiKhoanActivity
        extends BaseActivity {

    protected long taiKhoanId;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        taiKhoanId = state == null
                ? PhienDangNhap.id(this)
                : state.getLong(
                "tai_khoan_dang_sua",
                -1
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);

        out.putLong(
                "tai_khoan_dang_sua",
                taiKhoanId
        );
    }

    protected boolean taoBieuMau(
            String tieuDe,
            String moTa,
            String nut
    ) {
        if (taiKhoanId <= 0
                || taiKhoanId != PhienDangNhap.id(this)) {

            finish();
            return false;
        }

        setContentView(
                R.layout.activity_bieu_mau_tai_khoan
        );

        apDungKhoangTrongHeThong();

        ((TextView) findViewById(
                R.id.hoSoTieuDe
        )).setText(tieuDe);

        ((TextView) findViewById(
                R.id.hoSoMoTa
        )).setText(moTa);

        ((TextView) findViewById(
                R.id.hoSoLuu
        )).setText(nut);

        findViewById(R.id.hoSoQuayLai)
                .setOnClickListener(v -> finish());

        return true;
    }

    // Hien thi ten dang nhap, khong cho sua
    protected void hienTaiKhoan(String tenDangNhap) {
        TextView view = findViewById(
                R.id.hoSoTaiKhoan
        );

        if (view == null) return;

        view.setText(
                "Tên đăng nhập\n@"
                        + tenDangNhap
        );

        view.setVisibility(
                android.view.View.VISIBLE
        );
    }

    protected void hienGhiChu(String noiDung) {
        TextView view = findViewById(
                R.id.hoSoGhiChu
        );

        if (view == null) return;

        view.setText(noiDung);

        view.setVisibility(
                android.view.View.VISIBLE
        );
    }

    protected EditText themTruong(
            int id,
            String nhan,
            boolean matKhau
    ) {
        LinearLayout cha = findViewById(
                R.id.hoSoCacTruong
        );

        TextInputLayout khung =
                new TextInputLayout(this);

        khung.setHint(nhan);

        khung.setBoxBackgroundMode(
                TextInputLayout.BOX_BACKGROUND_OUTLINE
        );

        khung.setBoxStrokeColor(
                Color.parseColor("#2C7351")
        );

        khung.setBoxBackgroundColor(
                Color.WHITE
        );

        khung.setDefaultHintTextColor(
                ColorStateList.valueOf(
                        Color.parseColor("#607467")
                )
        );

        khung.setErrorTextColor(
                ColorStateList.valueOf(
                        Color.parseColor("#B3261E")
                )
        );

        TextInputEditText nhap =
                new TextInputEditText(this);

        nhap.setId(id);

        nhap.setTextColor(
                Color.parseColor("#20382A")
        );

        nhap.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                16
        );

        nhap.setSingleLine(true);

        nhap.setInputType(
                matKhau
                        ? InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
                        : InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        if (matKhau) {
            khung.setEndIconMode(
                    TextInputLayout.END_ICON_PASSWORD_TOGGLE
            );

            // Khong luu mat khau da nhap vao state
            nhap.setSaveEnabled(false);
        }

        khung.addView(
                nhap,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout.LayoutParams thamSo =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        thamSo.bottomMargin = Math.round(
                14 * getResources()
                        .getDisplayMetrics().density
        );

        cha.addView(khung, thamSo);

        return nhap;
    }

    protected void hoanTatBangBanPhim(
            EditText truongCuoi
    ) {
        truongCuoi.setImeOptions(
                EditorInfo.IME_ACTION_DONE
        );

        truongCuoi.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        findViewById(R.id.hoSoLuu)
                                .performClick();
                        return true;
                    }

                    return false;
                }
        );
    }

    protected boolean dungPhien() {
        if (taiKhoanId > 0
                && taiKhoanId == PhienDangNhap.id(this)) {
            return true;
        }

        Toast.makeText(
                this,
                "Phiên đăng nhập đã thay đổi. "
                        + "Vui lòng mở lại hồ sơ.",
                Toast.LENGTH_LONG
        ).show();

        finish();
        return false;
    }
}
