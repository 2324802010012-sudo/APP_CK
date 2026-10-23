package com.example.app_ck;

import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class BanDoVaGpsThamQuanTest {
    @Test public void banDoCoDuNamMarkerDungToaDoVaNutChonDungId() {
        try (ActivityScenario<BanDoActivity> scenario = ActivityScenario.launch(BanDoActivity.class)) {
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoTuongTac);
                ArrayList<Marker> markers = new ArrayList<>();
                for (org.osmdroid.views.overlay.Overlay o : map.getOverlays()) if (o instanceof Marker) markers.add((Marker) o);
                assertEquals(5, markers.size());
                int[] nuts = {R.id.nutChonDinhDocLap, R.id.nutChonBaoTang, R.id.nutChonDaiNoiHue,
                        R.id.nutChonConDao, R.id.nutChonVanMieu};
                try (TaiKhoanDb db = new TaiKhoanDb(a)) {
                    for (DiaDiem d : db.danhSachDiaDiem()) {
                        Marker marker = markers.stream().filter(m -> d.ten.equals(m.getTitle())).findFirst().orElseThrow(AssertionError::new);
                        assertEquals(KhoToaDoThamQuan.TOA_DO[d.id][0], marker.getPosition().getLatitude(), .0000001);
                        assertEquals(KhoToaDoThamQuan.TOA_DO[d.id][1], marker.getPosition().getLongitude(), .0000001);
                        a.findViewById(nuts[d.id]).performClick();
                        assertEquals(d.ten, ((TextView) a.findViewById(R.id.tenDiaDiemBanDo)).getText().toString());
                    }
                }
            });
        }
    }

    @Test public void caNamSoDoChayDemoChuyenMocDungVaDungKhiRaNen() {
        Class<?>[] classes = {ThamQuanActivity.class, BaoTangThamQuanActivity.class,
                DaiNoiHueThamQuanActivity.class, ConDaoThamQuanActivity.class, VanMieuThamQuanActivity.class};
        int[] soDiem = {25, 13, 10, 7, 16};
        for (int i = 0; i < classes.length; i++) {
            @SuppressWarnings("unchecked") Class<BaseActivity> lop = (Class<BaseActivity>) classes[i];
            final int idDiaDiem = i;
            try (ActivityScenario<BaseActivity> scenario = ActivityScenario.launch(lop)) {
                scenario.onActivity(a -> {
                    NguonThamQuanTuDong nguon = (NguonThamQuanTuDong) a;
                    assertEquals(idDiaDiem, nguon.idDiaDiemTuDong());
                    assertEquals(soDiem[idDiaDiem], nguon.cacDiemTuDong().size());
                });
                onView(withId(R.id.autoDemo)).perform(scrollTo(), click());
                onView(withId(R.id.autoCheDo)).check(matches(withText("DEMO · Vị trí mô phỏng")));
                onView(withId(R.id.autoDemoTiep)).perform(scrollTo(), click());
                scenario.onActivity(a -> assertEquals(2, ((NguonThamQuanTuDong) a).idDiemDangChon()));
                onView(withId(R.id.autoGhiGps)).perform(scrollTo(), click());
                onView(withId(R.id.autoTrangThai)).check(matches(withText(
                        "Bật GPS thật và đợi vị trí mới có sai số ≤25 m trước khi ghi mốc.")));
                scenario.moveToState(Lifecycle.State.CREATED);
                scenario.moveToState(Lifecycle.State.RESUMED);
                onView(withId(R.id.autoCheDo)).check(matches(withText("Thủ công")));
            }
        }
    }

    @Test public void chonMarkerThuCongDungDemoVaKhongGhiGpsGia() {
        try (ActivityScenario<DaiNoiHueThamQuanActivity> scenario = ActivityScenario.launch(DaiNoiHueThamQuanActivity.class)) {
            final int[] soMocCu = new int[1];
            scenario.onActivity(a -> {
                try (TaiKhoanDb db = new TaiKhoanDb(a)) {
                    soMocCu[0] = KhoToaDoThamQuan.doc(db.getReadableDatabase(), 2).size();
                }
            });
            onView(withId(R.id.autoDemo)).perform(scrollTo(), click());
            scenario.onActivity(a -> a.chonDiemTuDong(3));
            onView(withId(R.id.autoCheDo)).check(matches(withText("Thủ công")));
            scenario.onActivity(a -> {
                assertEquals(3, a.idDiemDangChon());
                try (TaiKhoanDb db = new TaiKhoanDb(a)) {
                    assertEquals(soMocCu[0], KhoToaDoThamQuan.doc(db.getReadableDatabase(), 2).size());
                }
            });
        }
    }
}
