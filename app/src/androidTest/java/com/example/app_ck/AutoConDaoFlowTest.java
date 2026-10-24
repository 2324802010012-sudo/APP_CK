package com.example.app_ck;

import android.os.SystemClock;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class AutoConDaoFlowTest {
    private ActivityScenario<ConDaoThamQuanActivity> mo() {
        ActivityScenario<ConDaoThamQuanActivity> s = ActivityScenario.launch(ConDaoThamQuanActivity.class);
        s.onActivity(a -> {
            a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            assertEquals("Thủ công · Chưa bật AUTO", ((TextView) a.findViewById(R.id.conDaoCheDo)).getText().toString());
            assertNull("Tùy chọn demo chỉ nằm trong bảng chọn chế độ", a.findViewById(R.id.conDaoDemo));
        });
        return s;
    }

    private String chu(ActivityScenario<ConDaoThamQuanActivity> s, int id) {
        AtomicReference<String> value = new AtomicReference<>();
        s.onActivity(a -> value.set(((TextView) a.findViewById(id)).getText().toString()));
        return value.get();
    }

    private void batDemo(ActivityScenario<ConDaoThamQuanActivity> s, boolean denGan) {
        s.onActivity(a -> a.findViewById(R.id.conDaoBatAuto).performClick());
        onView(withId(R.id.conDaoDemo)).perform(scrollTo(), click());
        if (denGan) denGan(s);
    }

    private void denGan(ActivityScenario<ConDaoThamQuanActivity> s) {
        s.onActivity(a -> {
            for (int i = 0; i < 3; i++) a.findViewById(R.id.conDaoBuocDemo).performClick();
        });
    }

    private void doiChu(ActivityScenario<ConDaoThamQuanActivity> s, int id, String value, long timeout) {
        long han = SystemClock.elapsedRealtime() + timeout;
        String status;
        do {
            status = chu(s, id);
            if (status.contains(value)) return;
            SystemClock.sleep(100);
        } while (SystemClock.elapsedRealtime() < han);
        fail("Chờ '" + value + "' nhưng nhận '" + status + "'");
    }

    @Test public void demoDiHetDanhMucChoDocXongVaHoiDiemThuCong() {
        try (ActivityScenario<ConDaoThamQuanActivity> s = mo()) {
            tangTocGiongDocTrongKiemThu(s);
            batDemo(s, false);
            for (int i = 0; i < CauHinhAutoConDao.CAC_DIEM.size(); i++) {
                final int id = i + 1;
                doiChu(s, R.id.conDaoCheDo, id + "/" + CauHinhAutoConDao.CAC_DIEM.size(), 10000);
                denGan(s);
                if (!CauHinhAutoConDao.tim(id).choPhepAuto) {
                    doiChu(s, R.id.conDaoTrangThai, "Điểm nghe thủ công", 10000);
                    assertEquals(CauHinhAutoConDao.CAC_DIEM.get(i).ten, chu(s, R.id.tqTenKhuVuc));
                    // Hộp thoại phải chờ xác nhận, không tự đi tiếp hay tự đọc điểm thường.
                    SystemClock.sleep(1500);
                    assertTrue(chu(s, R.id.conDaoTrangThai).contains("Điểm nghe thủ công"));
                    onView(withText(id == 5 ? "Nghe thuyết minh" : "Bỏ qua"))
                            .inRoot(UiKiemThu.hoiThoai()).perform(click());
                    if (id == 5) doiChu(s, R.id.conDaoTrangThai, "Đang phát theo lựa chọn:", 5000);
                } else {
                    doiChu(s, R.id.conDaoTrangThai, "Đang tự động phát:", 15000);
                    assertEquals(CauHinhAutoConDao.CAC_DIEM.get(i).ten, chu(s, R.id.tqTenKhuVuc));
                }
                doiChu(s, R.id.conDaoTrangThai, "Đã xử lý điểm " + id + "/" + CauHinhAutoConDao.CAC_DIEM.size(), 45000);
            }
            doiChu(s, R.id.conDaoTrangThai, "Đã hoàn thành demo 19/19", 10000);
            assertEquals("Bật AUTO", chu(s, R.id.conDaoBatAuto));
            SystemClock.sleep(3000);
            assertTrue(chu(s, R.id.conDaoTrangThai).contains("Đã hoàn thành demo 19/19"));
        }
    }

    private void tangTocGiongDocTrongKiemThu(ActivityScenario<ConDaoThamQuanActivity> s) {
        long han=SystemClock.elapsedRealtime()+15000;
        java.util.concurrent.atomic.AtomicBoolean sanSang=new java.util.concurrent.atomic.AtomicBoolean();
        do {
            s.onActivity(a -> {
                try {
                    java.lang.reflect.Field field=ConDaoThamQuanActivity.class.getDeclaredField("tts");
                    field.setAccessible(true);
                    android.speech.tts.TextToSpeech tts=(android.speech.tts.TextToSpeech)field.get(a);
                    sanSang.set(tts!=null && tts.setSpeechRate(3f)==android.speech.tts.TextToSpeech.SUCCESS);
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            });
            if(sanSang.get()) return;
            SystemClock.sleep(100);
        } while(SystemClock.elapsedRealtime()<han);
        fail("TTS chưa sẵn sàng để kiểm thử toàn tuyến");
    }

    @Test public void menuChiCoDemoGpsVaVanChonDiemThuCongDuoc() {
        try (ActivityScenario<ConDaoThamQuanActivity> s = mo()) {
            s.onActivity(a -> a.findViewById(R.id.conDaoBatAuto).performClick());
            onView(withId(R.id.conDaoDemo)).check(androidx.test.espresso.assertion.ViewAssertions.matches(isDisplayed()));
            onView(withId(R.id.conDaoGps)).check(androidx.test.espresso.assertion.ViewAssertions.matches(isDisplayed()));
            onView(withText("Demo thường · Chọn marker để nghe"))
                    .check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist());
            onView(withText("Tùy chọn điểm AUTO"))
                    .check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist());
            androidx.test.espresso.Espresso.pressBack();
            s.onActivity(a -> a.findViewById(R.id.tqNutTiep).performClick());
            assertEquals("Trại Phú Sơn", chu(s, R.id.tqTenKhuVuc));
            assertEquals("Thủ công · Chưa bật AUTO", chu(s, R.id.conDaoCheDo));
        }
    }

    @Test public void chonThuCongHuyLuotDangChoTuDong() {
        try (ActivityScenario<ConDaoThamQuanActivity> s = mo()) {
            batDemo(s, true);
            s.onActivity(a -> a.findViewById(R.id.tqNutTiep).performClick());
            assertEquals("Trại Phú Sơn", chu(s, R.id.tqTenKhuVuc));
            assertEquals("Thủ công · Chưa bật AUTO", chu(s, R.id.conDaoCheDo));
            SystemClock.sleep(4500);
            assertEquals("Trại Phú Sơn", chu(s, R.id.tqTenKhuVuc));
            assertFalse(chu(s, R.id.conDaoTrangThai).startsWith("Đang tự động phát:"));
        }
    }

    @Test public void roiManHinhVaXoayKhongTuBatLai() {
        try (ActivityScenario<ConDaoThamQuanActivity> s = mo()) {
            batDemo(s, true);
            s.onActivity(a -> a.startActivity(new android.content.Intent(a, ChiTietDiaDiemActivity.class)
                    .putExtra("viTriDiaDiem", 3)));
            android.app.Activity chiTiet = doiManHinh(ChiTietDiaDiemActivity.class);
            doiTrangThai(ConDaoThamQuanActivity.class, androidx.test.runner.lifecycle.Stage.STOPPED);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(chiTiet::finish);
            doiManHinh(ConDaoThamQuanActivity.class);
            assertEquals("Thủ công · Chưa bật AUTO", chu(s, R.id.conDaoCheDo));
            SystemClock.sleep(4000);
            assertFalse(chu(s, R.id.conDaoTrangThai).startsWith("Đang tự động phát:"));
            s.recreate();
            assertEquals("Thủ công · Chưa bật AUTO", chu(s, R.id.conDaoCheDo));
        }
    }

    private android.app.Activity doiManHinh(Class<? extends android.app.Activity> loai) {
        return doiTrangThai(loai, androidx.test.runner.lifecycle.Stage.RESUMED);
    }

    private android.app.Activity doiTrangThai(Class<? extends android.app.Activity> loai,
                                             androidx.test.runner.lifecycle.Stage stage) {
        AtomicReference<android.app.Activity> ketQua = new AtomicReference<>();
        long han = SystemClock.elapsedRealtime() + 10000;
        do {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                for (android.app.Activity a : androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(stage))
                    if (loai.isInstance(a)) ketQua.set(a);
            });
            if (ketQua.get() != null) return ketQua.get();
            SystemClock.sleep(100);
        } while (SystemClock.elapsedRealtime() < han);
        throw new AssertionError("Không mở được " + loai.getSimpleName());
    }
}
