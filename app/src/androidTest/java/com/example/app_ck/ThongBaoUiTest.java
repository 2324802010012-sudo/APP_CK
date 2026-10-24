package com.example.app_ck;

import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ThongBaoUiTest {
    @Test public void xacNhanDongBoNutXanhVaNoiDungTiengViet() {
        try(ActivityScenario<VanMieuThamQuanActivity> s=ActivityScenario.launch(VanMieuThamQuanActivity.class)) {
            s.onActivity(a->{
                AlertDialog dialog=new MaterialAlertDialogBuilder(a)
                        .setTitle("Bạn đang ở gần Văn Miếu")
                        .setMessage("Bạn có muốn bắt đầu nghe thuyết minh về địa điểm này không?")
                        .setPositiveButton("Bắt đầu",(d,w)->{})
                        .setNegativeButton("Bỏ qua",(d,w)->{}).show();
                MaterialButton positive=(MaterialButton)dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                MaterialButton negative=(MaterialButton)dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                assertEquals(Color.parseColor("#4F7C5A"),positive.getBackgroundTintList().getDefaultColor());
                assertEquals(Color.WHITE,positive.getCurrentTextColor());
                assertEquals(Color.parseColor("#4F7C5A"),negative.getCurrentTextColor());
                assertFalse(positive.isAllCaps());
                dialog.dismiss();
            });
        }
    }
}
