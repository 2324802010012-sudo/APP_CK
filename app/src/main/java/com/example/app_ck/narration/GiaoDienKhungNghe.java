package com.example.app_ck.narration;

import android.animation.ValueAnimator;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.example.app_ck.R;
import com.example.app_ck.ui.common.BaseActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Kích thước sheet, sóng âm và hộp đọc nội dung; không giữ TTS, MediaPlayer hoặc dữ liệu phiên. */
final class GiaoDienKhungNghe {
    private final BaseActivity activity;
    private final BottomSheetDialog dialog;
    private final View view;
    private final LinearLayout songAm;
    private ValueAnimator hoatAnhSong;
    private AlertDialog dialogNoiDung;

    GiaoDienKhungNghe(BaseActivity activity, BottomSheetDialog dialog, View view) {
        this.activity = activity;
        this.dialog = dialog;
        this.view = view;
        songAm = view.findViewById(R.id.ngheSongAm);
    }

    void cauHinhDialog(Runnable dongBai) {
        dialog.setCancelable(true);
        dialog.setOnShowListener(d -> {
            float density = activity.getResources()
                    .getDisplayMetrics().density;
            int chieuCaoManHinh = activity.getResources()
                    .getDisplayMetrics().heightPixels;
            View noiDungActivity = activity.findViewById(
                    android.R.id.content
            );
            if (noiDungActivity != null
                    && noiDungActivity.getHeight() > 0) {
                chieuCaoManHinh = Math.min(
                        chieuCaoManHinh,
                        noiDungActivity.getHeight()
                );
            }
            // Chiều cao tự thích ứng theo thiết bị
            int height = Math.min(
                    Math.round(chieuCaoManHinh * 0.94f),
                    Math.round(680 * density)
            );
            if (view.getLayoutParams() != null) {
                view.getLayoutParams().height = height;
                view.requestLayout();
            }
            BottomSheetBehavior<?> behavior =
                    dialog.getBehavior();
            behavior.setFitToContents(true);
            behavior.setSkipCollapsed(true);
            // Không cho kéo Bottom Sheet lên xuống
            behavior.setDraggable(false);
            behavior.setMaxWidth(
                    Math.round(600 * density)
            );
            behavior.setShouldRemoveExpandedCorners(false);
            behavior.setState(
                    BottomSheetBehavior.STATE_EXPANDED
            );
        });
        dialog.setOnDismissListener(d -> {
            if (!dialog.isShowing()) {
                dongNoiDungThuyetMinh();
                dongBai.run();
            }
        });
    }
    void batSongAm() {
        dungSongAm();
        songAm.setAlpha(1f);
        hoatAnhSong = ValueAnimator.ofFloat(
                0f,
                1f
        );
        hoatAnhSong.setDuration(900);
        hoatAnhSong.setRepeatCount(
                ValueAnimator.INFINITE
        );
        hoatAnhSong.setInterpolator(
                new LinearInterpolator()
        );
        hoatAnhSong.addUpdateListener(animation -> {
            float phase =
                    (float) animation.getAnimatedValue();
            for (int i = 0;
                 i < songAm.getChildCount();
                 i++) {
                float scale = (float) (
                        0.45
                                + 0.55 * Math.abs(
                                Math.sin(
                                        phase
                                                * Math.PI
                                                * 2
                                                + i * 0.8
                                )
                        )
                );
                songAm.getChildAt(i)
                        .setScaleY(scale);
            }
        });
        hoatAnhSong.start();
    }
    void dungSongAm() {
        if (hoatAnhSong != null) {
            hoatAnhSong.cancel();
            hoatAnhSong.removeAllUpdateListeners();
            hoatAnhSong = null;
        }
        for (int i = 0;
             i < songAm.getChildCount();
             i++) {
            songAm.getChildAt(i)
                    .setScaleY(1f);
        }
        songAm.setAlpha(0.45f);
    }
    void moNoiDungThuyetMinh() {
        if (!dialog.isShowing()
                || activity.isFinishing()
                || activity.isDestroyed()) {
            return;
        }
        // Không mở trùng nhiều hộp thoại
        if (dialogNoiDung != null
                && dialogNoiDung.isShowing()) {
            return;
        }
        TextView vanBan =
                view.findViewById(R.id.ngheNoiDung);
        TextView tenDiem =
                view.findViewById(R.id.ngheTenDiem);
        String baiThuyetMinh =
                vanBan.getText().toString().trim();
        String tieuDe =
                tenDiem.getText().toString().trim();
        if (TextUtils.isEmpty(baiThuyetMinh)) {
            Toast.makeText(
                    activity,
                    "Chưa có nội dung thuyết minh.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        // Sử dụng nội dung thật của bài đang nghe
        // Không thêm dữ liệu cố định.
        AlertDialog moi =
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(
                                TextUtils.isEmpty(tieuDe)
                                        ? "Nội dung thuyết minh"
                                        : tieuDe
                        )
                        .setIcon(R.drawable.ui_nghe_noi_dung)
                        .setMessage(baiThuyetMinh)
                        .setPositiveButton("Đóng", null)
                        .create();
        dialogNoiDung = moi;
        moi.setOnDismissListener(d -> {
            if (dialogNoiDung == moi) {
                dialogNoiDung = null;
            }
        });
        moi.show();
        // Cải thiện khả năng đọc văn bản
        TextView noiDungHienThi =
                moi.findViewById(android.R.id.message);
        if (noiDungHienThi != null) {
            noiDungHienThi.setTextSize(15);
            float density = activity.getResources()
                    .getDisplayMetrics().density;
            noiDungHienThi.setLineSpacing(
                    5 * density,
                    1.15f
            );
            noiDungHienThi.setTextIsSelectable(true);
        }
    }
    void dongNoiDungThuyetMinh() {
        if (dialogNoiDung != null) {
            AlertDialog cu = dialogNoiDung;
            dialogNoiDung = null;
            cu.dismiss();
        }
    }
}
