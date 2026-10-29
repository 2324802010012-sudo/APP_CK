package com.example.app_ck;

import com.example.app_ck.data.repository.KhoNoiDungThamQuan;
import com.example.app_ck.data.seed.DuLieuNoiDungThamQuan;
import com.example.app_ck.narration.KhungNgheThuyetMinh;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.tour.BaoTangThamQuanActivity;
import com.example.app_ck.ui.tour.ConDaoThamQuanActivity;
import com.example.app_ck.ui.tour.DaiNoiHueThamQuanActivity;
import com.example.app_ck.ui.tour.ThamQuanActivity;
import com.example.app_ck.ui.tour.VanMieuThamQuanActivity;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class NguonThongTinFlowTest {
    private com.google.android.material.bottomsheet.BottomSheetDialog layKhung(BaseActivity a) {
        try {
            java.lang.reflect.Field f = BaseActivity.class.getDeclaredField("khungNghe");
            f.setAccessible(true);
            java.lang.reflect.Field d = KhungNgheThuyetMinh.class.getDeclaredField("dialog");
            d.setAccessible(true);
            return (com.google.android.material.bottomsheet.BottomSheetDialog)d.get(f.get(a));
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private void doiKhungMoXong(ActivityScenario<BaseActivity> scenario) {
        long han = android.os.SystemClock.elapsedRealtime() + 5000;
        java.util.concurrent.atomic.AtomicBoolean xong = new java.util.concurrent.atomic.AtomicBoolean();
        do {
            scenario.onActivity(a -> {
                try {
                    java.lang.reflect.Field f = BaseActivity.class.getDeclaredField("khungNghe");
                    f.setAccessible(true);
                    Object khung = f.get(a);
                    java.lang.reflect.Field d = KhungNgheThuyetMinh.class.getDeclaredField("dialog");
                    d.setAccessible(true);
                    com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                            (com.google.android.material.bottomsheet.BottomSheetDialog)d.get(khung);
                    xong.set(dialog.getWindow().getDecorView().hasWindowFocus()
                            && dialog.getBehavior().getState() == com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            });
            if (xong.get()) return;
            android.os.SystemClock.sleep(50);
        } while (android.os.SystemClock.elapsedRealtime() < han);
        fail("Khung nghe chưa mở xong");
    }

    @Test public void chiTietHienDuDanhSachVaMoDungDiemCuoiCuaNamDiaDiem() {
        Context context = ApplicationProvider.getApplicationContext();
        int[] soDiem = {25, 13, 10, 19, 16};
        for (int i = 0; i < 5; i++) {
            final int id = i;
            Intent intent = new Intent(context, ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem", id);
            try (ActivityScenario<ChiTietDiaDiemActivity> scenario = ActivityScenario.launch(intent)) {
                scenario.onActivity(a -> {
                    a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    assertEquals(KhoNoiDungThamQuan.bai(DuLieuNoiDungThamQuan.lay(id,0)),
                            ((TextView)a.findViewById(R.id.moTaChiTiet)).getText().toString());
                    android.widget.LinearLayout ds = a.findViewById(R.id.danhSachDiemNoiBat);
                    assertEquals(soDiem[id], ds.getChildCount());
                    ds.getChildAt(soDiem[id] - 1).performClick();
                });
                onView(withId(R.id.tqMoTaKhuVuc)).check(matches(withText(
                        KhoNoiDungThamQuan.bai(DuLieuNoiDungThamQuan.lay(id,soDiem[id])))));
                pressBack();
            }
        }
    }

    @Test public void namTourDocBaiMoiVaMoNguonDungDiem() {
        Context context = ApplicationProvider.getApplicationContext();
        Class<?>[] lop = {ThamQuanActivity.class, BaoTangThamQuanActivity.class,
                DaiNoiHueThamQuanActivity.class, ConDaoThamQuanActivity.class, VanMieuThamQuanActivity.class};
        int[] cuoi = {25, 13, 10, 19, 16};
        for (int i = 0; i < lop.length; i++) {
            final int id = i;
            Intent intent = new Intent(context, lop[i]).putExtra("idMarker", cuoi[i]);
            try (ActivityScenario<BaseActivity> scenario = ActivityScenario.launch(intent)) {
                scenario.onActivity(a -> {
                    a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    assertFalse(a.isFinishing());
                    TextView bai = a.findViewById(R.id.tqMoTaKhuVuc);
                    assertEquals(KhoNoiDungThamQuan.bai(DuLieuNoiDungThamQuan.lay(id, cuoi[id])), bai.getText().toString());
                    a.moKhungThuyetMinh(false);
                });
                doiKhungMoXong(scenario);
                onView(withId(R.id.ngheNoiDung)).inRoot(UiKiemThu.hoiThoai()).check(matches(withText(
                        KhoNoiDungThamQuan.bai(DuLieuNoiDungThamQuan.lay(id, cuoi[id])))));
                scenario.onActivity(a -> assertTrue(layKhung(a).findViewById(R.id.ngheNguonThongTin).performClick()));
                String tieuDeNguon = "Nguồn thông tin – " + DuLieuNoiDungThamQuan.lay(id,cuoi[id]).ten;
                onView(withText(tieuDeNguon)).inRoot(allOf(UiKiemThu.hoiThoai(),
                        withDecorView(hasDescendant(withText(tieuDeNguon)))))
                        .check(matches(isDisplayed()));
                pressBack();
                scenario.onActivity(BaseActivity::dongKhungThuyetMinh);
            }
        }
    }
}
