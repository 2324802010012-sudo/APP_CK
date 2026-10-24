package com.example.app_ck;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;

@RunWith(AndroidJUnit4.class)
public class KhungNgheFlowTest {
    private Object truong(Object target,String name) {
        for(Class<?> cls=target.getClass();cls!=null;cls=cls.getSuperclass()) {
            try {Field f=cls.getDeclaredField(name);f.setAccessible(true);return f.get(target);}
            catch(NoSuchFieldException e) { /* Thử lớp cha. */ }
            catch(IllegalAccessException e) {throw new AssertionError(e);}
        }
        throw new AssertionError(name);
    }
    private <T extends BaseActivity> Object lay(ActivityScenario<T> s,String key) {
        AtomicReference<Object> value=new AtomicReference<>();
        s.onActivity(a->value.set(truong(truong(a,"khungNghe"),key)));
        return value.get();
    }
    private <T extends BaseActivity> String chu(ActivityScenario<T> s,int id) {
        AtomicReference<String> value=new AtomicReference<>();
        s.onActivity(a->{Dialog d=(Dialog)truong(truong(a,"khungNghe"),"dialog");
            value.set(((TextView)d.findViewById(id)).getText().toString());});
        return value.get();
    }
    private <T extends BaseActivity> void doi(ActivityScenario<T> s,int id,String text,long ms) {
        long end=SystemClock.elapsedRealtime()+ms;
        do {if(chu(s,id).contains(text))return;SystemClock.sleep(100);}while(SystemClock.elapsedRealtime()<end);
        fail("Chờ "+text+" nhưng nhận "+chu(s,id));
    }
    private <T extends BaseActivity> void doiFocus(ActivityScenario<T> s) {
        long end=SystemClock.elapsedRealtime()+5000;
        do {
            AtomicReference<Boolean> focused=new AtomicReference<>(false);
            s.onActivity(a->{Dialog d=(Dialog)truong(truong(a,"khungNghe"),"dialog");
                focused.set(d.isShowing() && d.getWindow().getDecorView().hasWindowFocus()
                        && ((com.google.android.material.bottomsheet.BottomSheetDialog)d).getBehavior().getState()
                        ==com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);});
            if(focused.get())return;SystemClock.sleep(100);
        }while(SystemClock.elapsedRealtime()<end);
        fail("Khung nghe chưa nhận focus");
    }

    @Test public void chonSoThuCongMoDungNoiDungAnhVaDongTrenCaNamTour() {
        kiemTra(ThamQuanActivity.class);
        kiemTra(BaoTangThamQuanActivity.class);
        kiemTra(DaiNoiHueThamQuanActivity.class);
        kiemTra(ConDaoThamQuanActivity.class);
        kiemTra(VanMieuThamQuanActivity.class);
    }
    private <T extends BaseActivity> void kiemTra(Class<T> cls) {
        try(ActivityScenario<T> s=ActivityScenario.launch(cls)) {
            AtomicReference<String> expected=new AtomicReference<>();
            s.onActivity(a->{
                a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                a.findViewById(R.id.tqNutTiep).performClick();
                expected.set(((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                Dialog d=(Dialog)truong(truong(a,"khungNghe"),"dialog");
                assertTrue(d.isShowing());
                assertEquals(expected.get(),((TextView)d.findViewById(R.id.ngheTenDiem)).getText().toString());
                assertFalse(((TextView)d.findViewById(R.id.ngheNoiDung)).getText().toString().isEmpty());
                assertNotNull(((android.widget.ImageView)d.findViewById(R.id.ngheAnh)).getDrawable());
                assertNull(truong(truong(a,"khungNghe"),"player")); // Chọn điểm chưa tự bật loa.
            });
            doiFocus(s);
            onView(withId(R.id.ngheDong)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            long hanDong=SystemClock.elapsedRealtime()+3000;
            while(((Dialog)lay(s,"dialog")).isShowing() && SystemClock.elapsedRealtime()<hanDong)SystemClock.sleep(50);
            assertFalse(((Dialog)lay(s,"dialog")).isShowing());
            s.onActivity(a->a.findViewById(R.id.tqNutPhat).performClick());
            doi(s,R.id.ngheTrangThai,"Đang phát thuyết minh",45000);
            assertEquals(expected.get(),chu(s,R.id.ngheTenDiem));
            onView(withId(R.id.ngheDong)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertNull(lay(s,"player"));
            assertEquals(0,((java.util.List<?>)lay(s,"files")).size());
        }
    }

    @Test public void tuaAmThanhThatTamDungTiepTucDungVaXoaCacheKhiDong() {
        try(ActivityScenario<VanMieuThamQuanActivity> s=ActivityScenario.launch(VanMieuThamQuanActivity.class)) {
            s.onActivity(a->a.findViewById(R.id.tqNutPhat).performClick());
            doi(s,R.id.ngheTrangThai,"Đang phát thuyết minh",45000);
            doiFocus(s);
            onView(withId(R.id.nghePhatTamDung)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            doi(s,R.id.ngheTrangThai,"Đã tạm dừng",5000);
            assertFalse(((MediaPlayer)lay(s,"player")).isPlaying());
            onView(withId(R.id.ngheThanhTua)).inRoot(UiKiemThu.hoiThoai()).perform(
                    new GeneralClickAction(Tap.SINGLE,v->{int[] loc=new int[2];v.getLocationOnScreen(loc);
                        return new float[]{loc[0]+v.getPaddingLeft()+.7f*(v.getWidth()-v.getPaddingLeft()-v.getPaddingRight()),
                                loc[1]+v.getHeight()/2f};},Press.FINGER,0,0));
            long end=SystemClock.elapsedRealtime()+5000;
            do {
                if(Boolean.TRUE.equals(lay(s,"daChuanBi")) && (Integer)lay(s,"viTriDung")>0)break;
                SystemClock.sleep(100);
            }while(SystemClock.elapsedRealtime()<end);
            assertFalse(((MediaPlayer)lay(s,"player")).isPlaying());
            int duration=(Integer)lay(s,"tong");
            int pos=((MediaPlayer)lay(s,"player")).getCurrentPosition();
            assertTrue("Tua thực tế tới 70%: "+pos+"/"+duration,pos>duration*.6 && pos<duration*.8);
            onView(withId(R.id.nghePhatTamDung)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            doi(s,R.id.ngheTrangThai,"Đang phát thuyết minh",5000);
            onView(withId(R.id.ngheDung)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertEquals("00:00",chu(s,R.id.ngheDaPhat));
            assertNull(lay(s,"player"));
            onView(withId(R.id.nghePhatTamDung)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            doi(s,R.id.ngheTrangThai,"Đang phát thuyết minh",5000);
            assertTrue(((MediaPlayer)lay(s,"player")).getCurrentPosition()<2000);
            java.util.List<java.io.File> tep=new java.util.ArrayList<>((java.util.List<java.io.File>)lay(s,"files"));
            onView(withId(R.id.ngheDong)).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertNull(lay(s,"player"));
            for(java.io.File f:tep)assertFalse(f.exists());
            s.recreate();
            assertNull("Xoay không tự mở hoặc phát lại",truongTrongScenario(s,"khungNghe"));
        }
    }
    private <T extends BaseActivity> Object truongTrongScenario(ActivityScenario<T> s,String key) {
        AtomicReference<Object> result=new AtomicReference<>();s.onActivity(a->result.set(truong(a,key)));return result.get();
    }

    @Test public void baiDaiTuaSangDoanKhacVaRoiTrangDungAmThanh() {
        try(ActivityScenario<VanMieuThamQuanActivity> s=ActivityScenario.launch(VanMieuThamQuanActivity.class)) {
            StringBuilder bai=new StringBuilder();
            for(int i=0;i<66;i++)bai.append("Đây là nội dung giới thiệu về một điểm tham quan. ");
            s.onActivity(a->a.moKhungThuyetMinh(true));
            doi(s,R.id.ngheTrangThai,"Đang phát thuyết minh",45000);
            s.onActivity(a->{
                // Tăng tốc trong kiểm thử để không chờ tổng hợp một bài dài vài phút.
                android.speech.tts.TextToSpeech tts=(android.speech.tts.TextToSpeech)truong(truong(a,"khungNghe"),"tts");
                tts.setSpeechRate(3f);
                ((TextView)a.findViewById(R.id.tqMoTaKhuVuc)).setText(bai.toString());
                a.moKhungThuyetMinh(true);
            });
            doi(s,R.id.ngheTrangThai,"Đang phát thuyết minh",120000);
            assertTrue(((java.util.List<?>)lay(s,"durations")).size()>1);
            doiFocus(s);
            java.util.List<Integer> lengths=(java.util.List<Integer>)lay(s,"durations");
            float target=(lengths.get(0)+lengths.get(1)*.5f)/(Integer)lay(s,"tong");
            onView(withId(R.id.ngheThanhTua)).inRoot(UiKiemThu.hoiThoai()).perform(
                    new GeneralClickAction(Tap.SINGLE,v->{int[] loc=new int[2];v.getLocationOnScreen(loc);
                        return new float[]{loc[0]+v.getPaddingLeft()+target*(v.getWidth()-v.getPaddingLeft()-v.getPaddingRight()),
                                loc[1]+v.getHeight()/2f};},Press.FINGER,0,0));
            long han=SystemClock.elapsedRealtime()+5000;
            while(SystemClock.elapsedRealtime()<han) {
                if((Integer)lay(s,"doan")>0 && (Boolean)lay(s,"daChuanBi")
                        && ((MediaPlayer)lay(s,"player")).isPlaying())break;
                SystemClock.sleep(100);
            }
            assertTrue((Integer)lay(s,"doan")>0);
            assertTrue((Integer)lay(s,"viTriDung")>(Integer)lay(s,"tong")*.7);
            assertTrue(((MediaPlayer)lay(s,"player")).isPlaying());
            s.recreate();
            assertNull(truongTrongScenario(s,"khungNghe"));
        }
    }
}
