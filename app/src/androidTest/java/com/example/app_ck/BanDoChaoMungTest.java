package com.example.app_ck;

import android.os.SystemClock;
import android.widget.ScrollView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class BanDoChaoMungTest {
    private void doiZoom(ActivityScenario<WelcomeActivity> scenario, double mucCu, boolean phongTo) {
        AtomicBoolean xong = new AtomicBoolean();
        AtomicReference<Double> hienTai = new AtomicReference<>();
        long han = SystemClock.elapsedRealtime() + 4000;
        do {
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoChaoMung);
                hienTai.set(map.getZoomLevelDouble());
                xong.set(!map.isAnimating() && (phongTo
                        ? hienTai.get() > mucCu + .1 : hienTai.get() < mucCu - .1));
            });
            if (xong.get()) return;
            SystemClock.sleep(50);
        } while (SystemClock.elapsedRealtime() < han);
        fail("Bản đồ chưa đổi mức zoom: " + mucCu + " → " + hienTai.get());
    }

    @Test public void duNamDiemNutThuPhongVaKeoBanDoKhongCuonTrang() {
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            AtomicReference<Double> zoom = new AtomicReference<>();
            AtomicReference<Double> viDo = new AtomicReference<>();
            scenario.onActivity(a -> {
                a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                MapView map = a.findViewById(R.id.banDoChaoMung);
                HashSet<String> ids = new HashSet<>();
                for (org.osmdroid.views.overlay.Overlay o : map.getOverlays()) {
                    if (o instanceof Marker) {
                        Marker m = (Marker)o;
                        ids.add(m.getId());
                        assertEquals(KhoToaDoThamQuan.TOA_DO[Integer.parseInt(m.getId())][0],
                                m.getPosition().getLatitude(), .000001);
                    }
                }
                assertEquals(5, ids.size());
                zoom.set(map.getZoomLevelDouble());
            });
            onView(withId(R.id.chaoPhongToBanDo)).perform(click());
            doiZoom(scenario, zoom.get(), true);
            scenario.onActivity(a -> zoom.set(((MapView)a.findViewById(R.id.banDoChaoMung)).getZoomLevelDouble()));
            onView(withId(R.id.chaoThuNhoBanDo)).perform(click());
            doiZoom(scenario, zoom.get(), false);
            scenario.onActivity(a -> viDo.set(((MapView)a.findViewById(R.id.banDoChaoMung)).getMapCenter().getLatitude()));
            onView(withId(R.id.banDoChaoMung)).perform(swipeUp());
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoChaoMung);
                assertTrue(Math.abs(map.getMapCenter().getLatitude() - viDo.get()) > .01);
                assertEquals(0, ((ScrollView)a.findViewById(R.id.cuonTrangChaoMung)).getScrollY());
            });
        }
    }

    @Test public void taiTaoManHinhGiuTamVaMucZoom() {
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoChaoMung);
                map.getController().setZoom(8.25);
                map.getController().setCenter(new GeoPoint(16.47, 107.58));
            });
            scenario.recreate();
            scenario.onActivity(a -> {
                MapView map = a.findViewById(R.id.banDoChaoMung);
                assertEquals(8.25, map.getZoomLevelDouble(), .01);
                assertEquals(16.47, map.getMapCenter().getLatitude(), .01);
                assertEquals(107.58, map.getMapCenter().getLongitude(), .01);
            });
        }
    }
}
