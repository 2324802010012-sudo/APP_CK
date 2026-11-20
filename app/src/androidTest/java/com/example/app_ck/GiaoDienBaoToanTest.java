package com.example.app_ck;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.tour.BaoTangThamQuanActivity;
import com.example.app_ck.ui.tour.ConDaoThamQuanActivity;
import com.example.app_ck.ui.tour.DaiNoiHueThamQuanActivity;
import com.example.app_ck.ui.tour.GiaoDienTour;
import com.example.app_ck.ui.tour.ThamQuanActivity;
import com.example.app_ck.ui.tour.VanMieuThamQuanActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Cùng test APK có thể chụp bản gốc và bản refactor để đối chiếu hình ảnh. */
@RunWith(AndroidJUnit4.class)
public class GiaoDienBaoToanTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final String moc = InstrumentationRegistry.getArguments().getString("anh_moc","sau");

    @Test public void chupNamTourTabBannerToanManHinhVaTrinhNghe() {
        Class<?>[] lop = {ThamQuanActivity.class,BaoTangThamQuanActivity.class,
                DaiNoiHueThamQuanActivity.class,ConDaoThamQuanActivity.class,VanMieuThamQuanActivity.class};
        for (int i = 0; i < lop.length; i++) {
            final int place = i;
            Intent intent = new Intent(context,lop[i]).putExtra("viTriDiaDiem",place)
                    .putExtra("idMarker",place == 1 ? 10 : 1);
            try (ActivityScenario<BaseActivity> scenario = ActivityScenario.launch(intent)) {
                onDinh();
                scenario.onActivity(a -> chup(a.findViewById(android.R.id.content),"tour_" + place + "_so_do"));
                scenario.onActivity(a -> a.findViewById(R.id.tqTabDanhSach).performClick());
                onDinh();
                scenario.onActivity(a -> chup(a.findViewById(android.R.id.content),"tour_" + place + "_danh_sach"));
                scenario.onActivity(a -> {
                    a.findViewById(R.id.tqTabSoDo).performClick();
                    GiaoDienTour.lay(a).batToanManHinh();
                });
                onDinh();
                scenario.onActivity(a -> {
                    assertEquals(View.VISIBLE,a.findViewById(R.id.tqThanhToanManHinh).getVisibility());
                    chup(a.findViewById(android.R.id.content),"tour_" + place + "_toan_man_hinh");
                    GiaoDienTour.lay(a).thoatToanManHinh();
                    a.findViewById(R.id.tqNutThuGon).performClick();
                });
                onDinh();
                scenario.onActivity(a -> {
                    chup(a.findViewById(android.R.id.content),"tour_" + place + "_banner_gon");
                    a.moKhungThuyetMinh(false);
                });
                onDinh();
                scenario.onActivity(a -> {
                    BottomSheetDialog nghe = (BottomSheetDialog)UiKiemThu.truong(UiKiemThu.truong(a,"khungNghe"),"dialog");
                    assertTrue(nghe.isShowing());
                    chup(nghe.getWindow().getDecorView(),"tour_" + place + "_trinh_nghe");
                });
            }
        }
    }

    @Test public void chupChiTietVaThuVienAnhSauKhiTachSlider() {
        try (ActivityScenario<ChiTietDiaDiemActivity> scenario = ActivityScenario.launch(
                new Intent(context,ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem",2))) {
            onDinh();
            scenario.onActivity(a -> {
                chup(a.findViewById(android.R.id.content),"chi_tiet");
                a.findViewById(R.id.nutThuVien).performClick();
            });
            onDinh();
            UiKiemThu.onView(androidx.test.espresso.matcher.ViewMatchers.withText("Thư viện hình ảnh"))
                    .inRoot(UiKiemThu.hoiThoai()).perform(new androidx.test.espresso.ViewAction() {
                        @Override public org.hamcrest.Matcher<View> getConstraints() {
                            return androidx.test.espresso.matcher.ViewMatchers.isDisplayed();
                        }
                        @Override public String getDescription() { return "Chụp thư viện ảnh"; }
                        @Override public void perform(androidx.test.espresso.UiController controller, View view) {
                            chup(view.getRootView(),"thu_vien");
                        }
                    });
            androidx.test.espresso.Espresso.pressBack();
        }
    }

    private void onDinh() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(600);
    }

    private void chup(View view, String ten) {
        assertTrue(view.getWidth() > 0 && view.getHeight() > 0);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        File thuMuc = new File(context.getExternalFilesDir(null),"refactor_" + moc);
        assertTrue(thuMuc.isDirectory() || thuMuc.mkdirs());
        try (FileOutputStream out = new FileOutputStream(new File(thuMuc,ten + ".png"))) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        } catch (java.io.IOException e) { throw new AssertionError(e); }
        finally { bitmap.recycle(); }
    }
}
