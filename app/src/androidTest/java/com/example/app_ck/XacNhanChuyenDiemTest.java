package com.example.app_ck;

import android.content.Intent;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class XacNhanChuyenDiemTest {
    private long taiKhoanCu;
    private String tenCu;
    @Before public void giuPhien() {
        android.content.Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        taiKhoanCu=PhienDangNhap.id(c); tenCu=PhienDangNhap.ten(c);
        PhienDangNhap.dangXuat(c);
    }
    @After public void khoiPhucPhien() {
        android.content.Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        if (taiKhoanCu>0) PhienDangNhap.luu(c,taiKhoanCu,tenCu);
        else PhienDangNhap.dangXuat(c);
    }
    private final Class<?>[] cacTour = {DaiNoiHueThamQuanActivity.class,
            ConDaoThamQuanActivity.class, VanMieuThamQuanActivity.class};

    private Field truong(Object o, String ten) throws Exception {
        for (Class<?> lop=o.getClass(); lop!=null; lop=lop.getSuperclass()) {
            try { Field f=lop.getDeclaredField(ten); f.setAccessible(true); return f; }
            catch (NoSuchFieldException e) { /* Tìm trong lớp cha. */ }
        }
        throw new NoSuchFieldException(ten);
    }
    private Object doc(Object o, String ten) {
        try { return truong(o,ten).get(o); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    private void gan(Object o, String ten, Object value) {
        try { truong(o,ten).set(o,value); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    private void chon(BaseActivity a, int chiSo) {
        String ten=a instanceof DaiNoiHueThamQuanActivity ? "chonChiSo" : "chuyenDiem";
        try { Method m=a.getClass().getDeclaredMethod(ten,int.class); m.setAccessible(true); m.invoke(a,chiSo); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    private void phat(BaseActivity a) {
        Object hd=doc(a,"huongDan");
        // Kiểm tra cả nhánh GPS và Demo mà không sửa vị trí/quyền của thiết bị.
        gan(hd,"cheDo",a instanceof ConDaoThamQuanActivity ? 2 : 1);
        gan(hd,"dangDoc",true);
        boolean ok=a instanceof ConDaoThamQuanActivity ? ((ConDaoThamQuanActivity)a).phatDiemTuDong(1)
                : ((HuongDanTour.ManHinh)a).phatDiemTuDong(1);
        assertTrue(ok);
    }
    private void doiTts(ActivityScenario<BaseActivity> s) {
        long han=SystemClock.elapsedRealtime()+20000;
        AtomicBoolean xong=new AtomicBoolean();
        do {
            s.onActivity(a -> xong.set((Boolean)doc(a,"sanSangDoc")));
            if (xong.get()) return;
            SystemClock.sleep(100);
        } while (SystemClock.elapsedRealtime()<han);
        fail("Giọng đọc chưa sẵn sàng");
    }

    @Test public void dangPhatHoiTruocHuyGiuNguyenXacNhanMoiDungVaChuyenTrenBaTour() {
        for (Class<?> lop:cacTour) {
            try (ActivityScenario<BaseActivity> s=ActivityScenario.launch(new Intent(
                    InstrumentationRegistry.getInstrumentation().getTargetContext(),lop))) {
                doiTts(s);
                final String[] luotCu = new String[1], tenCu = new String[1];
                final Object[] cheDoCu = new Object[1];
                s.onActivity(a -> {
                    a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    phat(a);
                    Object hd=doc(a,"huongDan");
                    Object cheDo=doc(hd,"cheDo");
                    String luot=(String)doc(a,"luotDoc");
                    String ten=((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString();
                    luotCu[0]=luot; tenCu[0]=ten; cheDoCu[0]=cheDo;
                    assertNotNull(luot);
                    chon(a,0); // Chạm đúng điểm đang phát không cắt âm thanh.
                    assertNull(doc(a,"hoiChuyenDiem"));
                    assertEquals(luot,doc(a,"luotDoc"));
                    chon(a,1);
                    AlertDialog hoi=(AlertDialog)doc(a,"hoiChuyenDiem");
                    assertTrue(hoi.isShowing());
                    assertEquals(luot,doc(a,"luotDoc"));
                    assertEquals(cheDo,doc(hd,"cheDo"));
                    assertEquals(ten,((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString());
                    assertNull(doc(a,"khungNghe"));
                    chon(a,2); // Không chồng nhiều hộp thoại hay đổi đích đang hỏi.
                    assertSame(hoi,doc(a,"hoiChuyenDiem"));
                    hoi.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                s.onActivity(a -> {
                    assertEquals(luotCu[0],doc(a,"luotDoc"));
                    assertEquals(cheDoCu[0],doc(doc(a,"huongDan"),"cheDo"));
                    chon(a,1);
                    ((AlertDialog)doc(a,"hoiChuyenDiem")).cancel();
                    assertEquals(luotCu[0],doc(a,"luotDoc"));
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                s.onActivity(a -> {
                    chon(a,1);
                    ((AlertDialog)doc(a,"hoiChuyenDiem")).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
                });
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                s.onActivity(a -> {
                    assertEquals(0,doc(doc(a,"huongDan"),"cheDo"));
                    assertNull(doc(a,"luotDoc"));
                    assertFalse(((TextToSpeech)doc(a,"tts")).isSpeaking());
                    Object khung=doc(a,"khungNghe");
                    BottomSheetDialog nghe=(BottomSheetDialog)doc(khung,"dialog");
                    assertTrue(nghe.isShowing());
                    assertEquals(((TextView)a.findViewById(R.id.tqTenKhuVuc)).getText().toString(),
                            ((TextView)nghe.findViewById(R.id.ngheTenDiem)).getText().toString());
                    assertNotEquals(tenCu[0],((TextView)nghe.findViewById(R.id.ngheTenDiem)).getText().toString());
                    assertNull(doc(khung,"player"));
                });
            }
        }
    }

    @Test public void thuCongChuyenNgayRoiTrangHuyHoiThoaiKhongGiuLenhChuyenCu() {
        for (Class<?> lop:cacTour) {
            try (ActivityScenario<BaseActivity> s=ActivityScenario.launch(new Intent(
                    InstrumentationRegistry.getInstrumentation().getTargetContext(),lop))) {
                doiTts(s);
                s.onActivity(a -> {
                    chon(a,1);
                    assertNull(doc(a,"hoiChuyenDiem"));
                    assertTrue(((BottomSheetDialog)doc(doc(a,"khungNghe"),"dialog")).isShowing());
                    a.dongKhungThuyetMinh();
                    phat(a);
                    chon(a,1);
                    assertTrue(((AlertDialog)doc(a,"hoiChuyenDiem")).isShowing());
                });
                s.moveToState(Lifecycle.State.CREATED);
                s.moveToState(Lifecycle.State.RESUMED);
                s.onActivity(a -> {
                    assertNull(doc(a,"hoiChuyenDiem"));
                    assertEquals(0,doc(doc(a,"huongDan"),"cheDo"));
                    assertNull(doc(a,"luotDoc"));
                    assertFalse(((BottomSheetDialog)doc(doc(a,"khungNghe"),"dialog")).isShowing());
                });
            }
        }
    }
}
