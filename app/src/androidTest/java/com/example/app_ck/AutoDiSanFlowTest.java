package com.example.app_ck;

import android.location.Location;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class AutoDiSanFlowTest {
    @Before public void capQuyenChoKiemThuGps() {
        if (android.os.Build.VERSION.SDK_INT < 28) {
            org.junit.Assume.assumeTrue("Kiểm thử cấp quyền cần Android 9+",false);
            return;
        }
        android.app.UiAutomation ui=InstrumentationRegistry.getInstrumentation().getUiAutomation();
        String pkg=InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
        ui.grantRuntimePermission(pkg,android.Manifest.permission.ACCESS_COARSE_LOCATION);
        ui.grantRuntimePermission(pkg,android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private Object truong(Object a,String ten) {
        try { Field f=a.getClass().getDeclaredField(ten); f.setAccessible(true); return f.get(a); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private <T extends BaseActivity> String chu(ActivityScenario<T> s,int id) {
        AtomicReference<String> value=new AtomicReference<>();
        s.onActivity(a->value.set(((TextView)a.findViewById(id)).getText().toString()));
        return value.get();
    }
    private <T extends BaseActivity> void doi(ActivityScenario<T> s,int id,String text,long ms) {
        long end=SystemClock.elapsedRealtime()+ms;
        do { if(chu(s,id).contains(text))return; SystemClock.sleep(100); }
        while(SystemClock.elapsedRealtime()<end);
        fail("Chờ "+text+" nhưng nhận "+chu(s,id));
    }
    private <T extends BaseActivity> void sanSang(ActivityScenario<T> s) {
        long end=SystemClock.elapsedRealtime()+15000;
        AtomicReference<Boolean> ready=new AtomicReference<>(false);
        do {
            s.onActivity(a->{
                a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if(Boolean.TRUE.equals(truong(a,"sanSangDoc"))) {
                    ((TextToSpeech)truong(a,"tts")).setSpeechRate(3f);
                    ready.set(true);
                }
            });
            if(ready.get())return;
            SystemClock.sleep(100);
        } while(SystemClock.elapsedRealtime()<end);
        fail("Chưa sẵn sàng TTS tiếng Việt");
    }
    private <T extends BaseActivity> void bat(ActivityScenario<T> s,int nut) {
        s.onActivity(a->a.findViewById(R.id.tourBatAuto).performClick());
        long end=SystemClock.elapsedRealtime()+10000;
        AtomicReference<Boolean> focus=new AtomicReference<>(false);
        do {
            s.onActivity(a->{
                android.app.Dialog dialog=(android.app.Dialog)truong(truong(a,"huongDan"),"chonCheDo");
                focus.set(dialog.isShowing() && dialog.getWindow().getDecorView().hasWindowFocus());
            });
            if(focus.get())break;
            SystemClock.sleep(100);
        } while(SystemClock.elapsedRealtime()<end);
        assertTrue("Bảng chọn phải nhận focus trước khi bấm",focus.get());
        onView(withId(nut)).inRoot(UiKiemThu.hoiThoai()).perform(scrollTo(),click());
    }
    private <T extends BaseActivity> CauHinhAutoDiSan config(ActivityScenario<T> s) {
        AtomicReference<CauHinhAutoDiSan> value=new AtomicReference<>();
        s.onActivity(a->value.set((CauHinhAutoDiSan)truong(truong(a,"huongDan"),"cauHinh")));
        return value.get();
    }
    private <T extends BaseActivity> void demo(Class<T> cls) {
        try(ActivityScenario<T> s=ActivityScenario.launch(cls)) {
            sanSang(s);
            CauHinhAutoDiSan tour=config(s);
            bat(s,R.id.tourDemo);
            boolean daNgheThuCong=false;
            for(CauHinhAutoDiSan.Diem d:tour.cacDiem) {
                String step=d.id+"/"+tour.cacDiem.size();
                doi(s,R.id.tourCheDo,step,10000);
                s.onActivity(a->{for(int i=0;i<3;i++)a.findViewById(R.id.tourBuocDemo).performClick();});
                if(d.choPhepAuto) {
                    doi(s,R.id.tourTrangThai,"Đang tự động phát:",15000);
                    assertEquals(d.ten,chu(s,R.id.tqTenKhuVuc));
                } else {
                    doi(s,R.id.tourTrangThai,"Điểm nghe thủ công",10000);
                    assertEquals(d.ten,chu(s,R.id.tqTenKhuVuc));
                    SystemClock.sleep(700);
                    assertTrue(chu(s,R.id.tourTrangThai).contains("Điểm nghe thủ công"));
                    onView(withText(daNgheThuCong?"Bỏ qua":"Nghe thuyết minh"))
                            .inRoot(UiKiemThu.hoiThoai()).perform(click());
                    daNgheThuCong=true;
                }
                doi(s,R.id.tourTrangThai,"Đã xử lý điểm "+step,60000);
            }
            doi(s,R.id.tourTrangThai,"Đã hoàn thành demo "+tour.cacDiem.size()+"/"+tour.cacDiem.size(),10000);
            assertEquals("Bật AUTO",chu(s,R.id.tourBatAuto));
        }
    }
    @Test public void hueDemoDu10DiemChoDocXongVaHoiThuCong() { demo(DaiNoiHueThamQuanActivity.class); }
    @Test public void vanMieuDemoDu16DiemChoDocXongVaHoiThuCong() { demo(VanMieuThamQuanActivity.class); }

    private <T extends BaseActivity> void gps(Class<T> cls, double latNgoaiTour,double lonNgoaiTour) {
        try(ActivityScenario<T> s=ActivityScenario.launch(cls)) {
            sanSang(s);
            bat(s,R.id.tourGps);
            assertTrue(chu(s,R.id.tourCheDo).startsWith("GPS thật"));
            // Bơm các mẫu vào callback để kiểm thử bộ lọc, không đổi GPS của thiết bị.
            for(int i=0;i<4;i++) { gui(s,latNgoaiTour,lonNgoaiTour,3,0); SystemClock.sleep(1000); }
            assertFalse(chu(s,R.id.tourTrangThai).startsWith("Đang tự động phát:"));
            CauHinhAutoDiSan.Diem diem=null;
            for(CauHinhAutoDiSan.Diem d:config(s).cacDiem) if(d.choPhepAuto){diem=d;break;}
            assertNotNull(diem);
            for(int i=0;i<4;i++) { gui(s,diem.lat,diem.lon,100,0); SystemClock.sleep(1000); }
            assertFalse(chu(s,R.id.tourTrangThai).startsWith("Đang tự động phát:"));
            gui(s,diem.lat,diem.lon,3,20000);
            assertFalse(chu(s,R.id.tourTrangThai).startsWith("Đang tự động phát:"));
            for(int i=0;i<4;i++) { gui(s,diem.lat,diem.lon,3,0); SystemClock.sleep(1000); }
            doi(s,R.id.tourTrangThai,"Đang tự động phát:",5000);
            assertEquals(diem.ten,chu(s,R.id.tqTenKhuVuc));
            s.onActivity(a->a.findViewById(R.id.tqNutTiep).performClick());
            assertEquals("Thủ công · Chưa bật AUTO",chu(s,R.id.tourCheDo));
            assertFalse((Boolean)layController(s,"dangDoc"));
        }
    }
    private <T extends BaseActivity> Object layController(ActivityScenario<T> s,String key) {
        AtomicReference<Object> value=new AtomicReference<>();
        s.onActivity(a->value.set(truong(truong(a,"huongDan"),key)));
        return value.get();
    }
    private <T extends BaseActivity> void gui(ActivityScenario<T> s,double lat,double lon,float accuracy,long age) {
        s.onActivity(a->{
            Location l=new Location("gps"); l.setLatitude(lat); l.setLongitude(lon); l.setAccuracy(accuracy);
            l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos()-age*1000000);
            ((HuongDanTour)truong(a,"huongDan")).onLocationChanged(l);
        });
    }
    @Test public void hueGpsLocViTriCuSaiSoVaTourKhac() { gps(DaiNoiHueThamQuanActivity.class,21.02858265,105.83588945); }
    @Test public void vanMieuGpsLocViTriCuSaiSoVaTourKhac() { gps(VanMieuThamQuanActivity.class,16.46768245,107.5791834); }

    @Test public void haiTourDungKhiRoiManHinhVaXoay() {
        kiemTraDung(DaiNoiHueThamQuanActivity.class);
        kiemTraDung(VanMieuThamQuanActivity.class);
    }
    private <T extends BaseActivity> void kiemTraDung(Class<T> cls) {
        try(ActivityScenario<T> s=ActivityScenario.launch(cls)) {
            bat(s,R.id.tourDemo);
            s.onActivity(a->a.startActivity(new android.content.Intent(a,ChiTietDiaDiemActivity.class)
                    .putExtra("viTriDiaDiem",2)));
            android.app.Activity child=doiManHinh(ChiTietDiaDiemActivity.class,androidx.test.runner.lifecycle.Stage.RESUMED);
            doiManHinh(cls,androidx.test.runner.lifecycle.Stage.STOPPED);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(child::finish);
            doiManHinh(cls,androidx.test.runner.lifecycle.Stage.RESUMED);
            assertEquals("Thủ công · Chưa bật AUTO",chu(s,R.id.tourCheDo));
            SystemClock.sleep(3500);
            assertEquals(0,layController(s,"cheDo"));
            s.recreate();
            assertEquals("Thủ công · Chưa bật AUTO",chu(s,R.id.tourCheDo));
        }
    }
    private android.app.Activity doiManHinh(Class<?> cls,androidx.test.runner.lifecycle.Stage stage) {
        AtomicReference<android.app.Activity> result=new AtomicReference<>();
        long end=SystemClock.elapsedRealtime()+10000;
        do {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
                for(android.app.Activity a:androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage))
                    if(cls.isInstance(a))result.set(a);
            });
            if(result.get()!=null)return result.get();
            SystemClock.sleep(100);
        } while(SystemClock.elapsedRealtime()<end);
        throw new AssertionError("Không đạt trạng thái "+stage+" của "+cls.getSimpleName());
    }
}
