package com.example.app_ck;

import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.narration.LuotNgheLichSu;
import com.example.app_ck.session.PhienDangNhap;
import com.example.app_ck.ui.common.BaseActivity;
import com.example.app_ck.ui.home.MainActivity;
import com.example.app_ck.ui.place.ChiTietDiaDiemActivity;
import com.example.app_ck.ui.profile.CaNhanActivity;
import com.example.app_ck.ui.profile.LichSuActivity;
import com.example.app_ck.ui.tour.BaoTangThamQuanActivity;
import com.example.app_ck.ui.tour.ConDaoThamQuanActivity;
import com.example.app_ck.ui.tour.DaiNoiHueThamQuanActivity;
import com.example.app_ck.ui.tour.ThamQuanActivity;
import com.example.app_ck.ui.tour.VanMieuThamQuanActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import static org.junit.Assert.*;
import static com.example.app_ck.UiKiemThu.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.Espresso.pressBack;

@RunWith(AndroidJUnit4.class)
public class LichSuFlowTest {
    private Context context;
    private long a, b, idCu;
    private String tenA, tenCu;
    private static final String MAT_KHAU = "LichSu123!";

    @Before public void taoTaiKhoanRieng() {
        context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        idCu=PhienDangNhap.id(context); tenCu=PhienDangNhap.ten(context);
        tenA="history_"+Long.toHexString(System.nanoTime());
        UiKiemThu.bat(context);
        try(AppDatabase db=new AppDatabase(context)) {
            assertTrue(db.dangKy(tenA,MAT_KHAU,"MA_HISTORY"));
            assertTrue(db.dangKy(tenA+"_b",MAT_KHAU,"MA_HISTORY"));
            a=db.dangNhap(tenA,MAT_KHAU); b=db.dangNhap(tenA+"_b",MAT_KHAU);
        }
        PhienDangNhap.luu(context,a,tenA);
    }

    @After public void donTaiKhoanThu() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            HashSet<Activity> ds=new HashSet<>();
            for(Stage stage:Stage.values()) if(stage!=Stage.DESTROYED)
                ds.addAll(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(stage));
            for(Activity activity:ds) activity.finish();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        UiKiemThu.tat(context);
        try(AppDatabase db=new AppDatabase(context)) {
            KhoLichSu.xoa(db.getWritableDatabase(),a,null); KhoLichSu.xoa(db.getWritableDatabase(),b,null);
            db.getWritableDatabase().delete("yeu_thich","tai_khoan_id IN (?,?)",new String[]{String.valueOf(a),String.valueOf(b)});
            db.getWritableDatabase().delete("tai_khoan","ten_dang_nhap IN (?,?)",new String[]{tenA,tenA+"_b"});
        }
        if(idCu>0)PhienDangNhap.luu(context,idCu,tenCu); else PhienDangNhap.dangXuat(context);
    }

    private ArrayList<KhoLichSu.Luot> doc(long id) {
        try(AppDatabase db=new AppDatabase(context)) { return KhoLichSu.doc(db.getReadableDatabase(),id); }
    }

    @Test public void khachMoTuTrangChuDangNhapVaXemTrangThaiRong() {
        PhienDangNhap.dangXuat(context);
        try(ActivityScenario<MainActivity> s=ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.nutLichSu)).perform(scrollTo(),click());
            onView(withId(R.id.edtTenDangNhap)).perform(replaceText(tenA));
            onView(withId(R.id.edtMatKhau)).perform(replaceText(MAT_KHAU),closeSoftKeyboard());
            onView(withId(R.id.btnDangNhap)).perform(click());
            onView(withText("Chưa có lịch sử nghe")).check(matches(isDisplayed()));
            assertEquals(a,PhienDangNhap.id(context)); assertTrue(doc(a).isEmpty());
        }
    }

    @Test public void moLichSuTuCaNhanVaKhamPhaRongQuayVeTimKiemTrangChu() {
        try(ActivityScenario<CaNhanActivity> s=ActivityScenario.launch(CaNhanActivity.class)) {
            onView(withId(R.id.cnLichSu)).perform(scrollTo(),click());
            onView(withText("Chưa có lịch sử nghe")).check(matches(isDisplayed()));
            onView(withId(R.id.lsNutTrangThai)).perform(scrollTo(),click());
            onView(withId(R.id.oTimKiem)).check(matches(isDisplayed()));
        }
    }

    @Test public void nhanCheDoDongBoVaXoaMotLuotCoXacNhan() {
        try(AppDatabase db=new AppDatabase(context)) {
            KhoLichSu.ghi(db.getWritableDatabase(),a,2,0,KhoLichSu.AUTO_MAIN,1,"chinh");
            KhoLichSu.ghi(db.getWritableDatabase(),a,4,6,KhoLichSu.AUTO_SUB,2,"thanh-phan");
        }
        try(ActivityScenario<LichSuActivity> s=ActivityScenario.launch(LichSuActivity.class)) {
            s.onActivity(x -> {
                LinearLayout ds=x.findViewById(R.id.lsDanhSach);
                assertEquals("Tự động · Điểm thành phần",((TextView)ds.getChildAt(0).findViewById(R.id.lsCheDo)).getText().toString());
                assertEquals("Tự động · Địa điểm chính",((TextView)ds.getChildAt(1).findViewById(R.id.lsCheDo)).getText().toString());
                ds.getChildAt(0).findViewById(R.id.lsXoaLuot).performClick();
            });
            onView(withText("Hủy")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertEquals(2,doc(a).size());
            s.onActivity(x -> ((LinearLayout)x.findViewById(R.id.lsDanhSach)).getChildAt(0).findViewById(R.id.lsXoaLuot).performClick());
            onView(withText("Xóa")).inRoot(UiKiemThu.hoiThoai()).perform(click());
            assertEquals(1,doc(a).size()); assertEquals(KhoLichSu.AUTO_MAIN,doc(a).get(0).cheDo);
        }
    }

    @Test public void moDungDiemXoaCoXacNhanVaKhongMatYeuThichHayLichSuNguoiKhac() {
        try(AppDatabase db=new AppDatabase(context)) {
            KhoLichSu.ghi(db.getWritableDatabase(),a,0,0,KhoLichSu.THU_CONG,1,"a1");
            KhoLichSu.ghi(db.getWritableDatabase(),a,4,6,KhoLichSu.DEMO,2,"a2");
            KhoLichSu.ghi(db.getWritableDatabase(),b,3,19,KhoLichSu.AUTO_GPS,3,"b1");
            db.datYeuThich(a,4,true);
        }
        try(ActivityScenario<LichSuActivity> s=ActivityScenario.launch(LichSuActivity.class)) {
            s.onActivity(x -> {
                LinearLayout ds=x.findViewById(R.id.lsDanhSach);
                assertEquals(2,ds.getChildCount());
                assertEquals("Khuê Văn Các",((TextView)ds.getChildAt(0).findViewById(R.id.lsTenDiem)).getText().toString());
                ds.getChildAt(0).performClick();
            });
            onView(withId(R.id.tqTenKhuVuc)).check(matches(withText("Khuê Văn Các")));
            assertEquals(2,doc(a).size()); // Mở lại chưa nghe không tạo lịch sử.
            pressBack();
            onView(withId(R.id.lsXoaTatCa)).perform(click());
            onView(withText("Hủy")).perform(click());
            assertEquals(2,doc(a).size());
            onView(withId(R.id.lsXoaTatCa)).perform(click());
            onView(withText("Xóa")).perform(click());
            onView(withText("Chưa có lịch sử nghe")).check(matches(isDisplayed()));
            assertTrue(doc(a).isEmpty()); assertEquals(1,doc(b).size());
            try(AppDatabase db=new AppDatabase(context)) { assertTrue(db.daYeuThich(a,4)); }
            PhienDangNhap.dangXuat(context);
            s.recreate();
            onView(withText("Đăng nhập để xem lịch sử")).check(matches(isDisplayed()));
        }
    }

    @Test public void khongGhiLucChuanBiKhongGhiTrungVaKhongGanLuotKhachChoTaiKhoanMoi() {
        PhienDangNhap.dangXuat(context);
        LuotNgheLichSu khach=new LuotNgheLichSu(context,0,1,KhoLichSu.THU_CONG);
        PhienDangNhap.luu(context,a,tenA); khach.batDau(context);
        assertTrue(doc(a).isEmpty());
        LuotNgheLichSu luot=new LuotNgheLichSu(context,3,19,KhoLichSu.AUTO_GPS);
        assertTrue(doc(a).isEmpty());
        luot.batDau(context); luot.batDau(context); assertEquals(1,doc(a).size());
        LuotNgheLichSu cho=new LuotNgheLichSu(context,2,4,KhoLichSu.DEMO);
        PhienDangNhap.luu(context,b,tenA+"_b"); cho.batDau(context);
        assertTrue(doc(b).isEmpty()); assertEquals(1,doc(a).size());
    }

    @Test public void callbackCuVaLuotDaHuyKhongDuocGhi() {
        try(ActivityScenario<BaoTangThamQuanActivity> s=ActivityScenario.launch(BaoTangThamQuanActivity.class)) {
            s.onActivity(x -> {
                x.chuanBiLichSu("dang-cho",2,4,KhoLichSu.AUTO_GPS);
                x.batDauLichSu("callback-cu");
                assertTrue(doc(a).isEmpty());
                x.huyLuotLichSu(); x.batDauLichSu("dang-cho"); assertTrue(doc(a).isEmpty());
                x.chuanBiLichSu("moi",4,6,KhoLichSu.DEMO);
                x.batDauLichSu("moi"); x.batDauLichSu("moi");
            });
            assertEquals(1,doc(a).size()); assertEquals(KhoLichSu.DEMO,doc(a).get(0).cheDo);
        }
    }

    @Test public void intentMoLaiDuNamNoiVaDiemThanhPhanKhongTuPhat() {
        String[] lop={"ThamQuanActivity","BaoTangThamQuanActivity","DaiNoiHueThamQuanActivity","ConDaoThamQuanActivity","VanMieuThamQuanActivity"};
        int[] cuoi={25,13,10,19,16};
        try(AppDatabase db=new AppDatabase(context)) {
            for(int i=0;i<5;i++) {
                KhoLichSu.ghi(db.getWritableDatabase(),a,i,0,KhoLichSu.THU_CONG,10+i,"noi"+i);
                KhoLichSu.Luot tong=null;
                for(KhoLichSu.Luot luot:doc(a)) if(luot.diaDiem==i && luot.diem==0) tong=luot;
                assertNotNull(tong);
                Intent moTong=LichSuActivity.taoIntentMoLai(context,tong);
                assertEquals(ChiTietDiaDiemActivity.class.getName(),moTong.getComponent().getClassName());
                assertEquals(i,moTong.getIntExtra("viTriDiaDiem",-1));
                assertFalse(moTong.getBooleanExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI,false));
                KhoLichSu.ghi(db.getWritableDatabase(),a,i,cuoi[i],KhoLichSu.THU_CONG,100+i,"diem"+i);
                Intent moDiem=LichSuActivity.taoIntentMoLai(context,doc(a).get(0));
                assertEquals("com.example.app_ck."+lop[i],moDiem.getComponent().getClassName());
                assertEquals(cuoi[i],moDiem.getIntExtra("idMarker",-1));
            }
        }
    }

    private Object truong(Object o, String ten) {
        for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass()) {
            try { Field f=c.getDeclaredField(ten); f.setAccessible(true); return f.get(o); }
            catch(NoSuchFieldException e) { /* Tìm ở lớp cha. */ }
            catch(IllegalAccessException e) { throw new AssertionError(e); }
        }
        throw new AssertionError(ten);
    }

    @Test public void ttsBaTourGhiDungDiemVaPhanBietGpsVoiDemo() throws Exception {
        Class<?>[] lop={DaiNoiHueThamQuanActivity.class,ConDaoThamQuanActivity.class,VanMieuThamQuanActivity.class};
        int soLuot=0;
        for(int noi=0;noi<lop.length;noi++) {
            for(int cheDo=1;cheDo<=2;cheDo++) {
                final int trangThai=cheDo;
                try(ActivityScenario<BaseActivity> s=ActivityScenario.launch(new Intent(context,lop[noi]))) {
                    java.util.concurrent.atomic.AtomicBoolean sanSang=new java.util.concurrent.atomic.AtomicBoolean();
                    long han=SystemClock.elapsedRealtime()+15000;
                    do {
                        s.onActivity(x -> sanSang.set((Boolean)truong(x,"sanSangDoc")));
                        if(sanSang.get())break;
                        SystemClock.sleep(100);
                    } while(SystemClock.elapsedRealtime()<han);
                    assertTrue("TTS phải sẵn sàng",sanSang.get());
                    s.onActivity(x -> {
                        try {
                            Object hd=truong(x,"huongDan");
                            Field mode=hd.getClass().getDeclaredField("cheDo"); mode.setAccessible(true); mode.setInt(hd,trangThai);
                            boolean ok=x instanceof DaiNoiHueThamQuanActivity
                                    ? ((DaiNoiHueThamQuanActivity)x).phatDiemTuDong(1)
                                    : x instanceof ConDaoThamQuanActivity
                                    ? ((ConDaoThamQuanActivity)x).phatDiemTuDong(1)
                                    : ((VanMieuThamQuanActivity)x).phatDiemTuDong(1);
                            assertTrue(ok);
                        } catch(ReflectiveOperationException e) { throw new AssertionError(e); }
                    });
                    doiSoLuot(++soLuot);
                    KhoLichSu.Luot luot=doc(a).get(0);
                    assertEquals(noi+2,luot.diaDiem); assertEquals(1,luot.diem);
                    assertEquals(cheDo==1?KhoLichSu.AUTO_SUB:KhoLichSu.DEMO,luot.cheDo);
                }
            }
        }
    }

    private void doiSoLuot(int so) {
        long han=SystemClock.elapsedRealtime()+30000;
        while(SystemClock.elapsedRealtime()<han) {
            if(doc(a).size()==so)return;
            SystemClock.sleep(100);
        }
        fail("Chờ "+so+" lượt nhưng có "+doc(a).size());
    }

    @Test public void chiTietNamDiaDiemMoDungBaiVaAnhChuaPhatKhongGhiLichSu() {
        for(int id=0;id<5;id++) {
            final int noi=id;
            try(ActivityScenario<ChiTietDiaDiemActivity> s=ActivityScenario.launch(
                    new Intent(context,ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem",id))) {
                s.onActivity(x -> {
                    assertNull(truong(x,"khungNghe"));
                    x.moKhungThuyetMinh(false);
                    Object khung=truong(x,"khungNghe");
                    BottomSheetDialog d=(BottomSheetDialog)truong(khung,"dialog");
                    assertTrue(d.isShowing());
                    assertEquals(((TextView)x.findViewById(R.id.tenChiTiet)).getText().toString(),
                            ((TextView)d.findViewById(R.id.ngheTenDiem)).getText().toString());
                    assertEquals(((TextView)x.findViewById(R.id.moTaChiTiet)).getText().toString(),
                            ((TextView)d.findViewById(R.id.ngheNoiDung)).getText().toString());
                    assertNotNull(((android.widget.ImageView)d.findViewById(R.id.ngheAnh)).getDrawable());
                    assertEquals(noi,truong(khung,"idDiaDiem"));
                    assertEquals(0,truong(khung,"idDiem"));
                    assertNull(truong(khung,"player"));
                    x.findViewById(R.id.nutDungThuyetMinh).performClick();
                    assertFalse(d.isShowing());
                });
            }
        }
        assertTrue(doc(a).isEmpty());
    }

    @Test public void chiTietNgheThuCongTamDungTuaNgheLaiVaGhiLichSuTongQuan() {
        try(ActivityScenario<ChiTietDiaDiemActivity> s=ActivityScenario.launch(
                new Intent(context,ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem",0))) {
            s.onActivity(x -> x.findViewById(R.id.nutNgheThuyetMinh).performClick());
            doiSoLuot(1);
            s.onActivity(x -> {
                Object khung=truong(x,"khungNghe");
                BottomSheetDialog d=(BottomSheetDialog)truong(khung,"dialog");
                MediaPlayer player=(MediaPlayer)truong(khung,"player");
                assertTrue(player.isPlaying());
                d.findViewById(R.id.nghePhatTamDung).performClick();
                assertFalse(player.isPlaying());
                android.widget.SeekBar tua=d.findViewById(R.id.ngheThanhTua);
                assertTrue(tua.isEnabled());
                d.findViewById(R.id.nghePhatTamDung).performClick();
                assertTrue(player.isPlaying());
                x.findViewById(R.id.nutDungThuyetMinh).performClick();
                assertFalse(d.isShowing());
                assertNull(truong(khung,"player"));
                x.findViewById(R.id.nutPhatLon).performClick();
            });
            doiSoLuot(2);
            for(KhoLichSu.Luot luot:doc(a)) {
                assertEquals(0,luot.diaDiem); assertEquals(0,luot.diem);
                assertEquals(KhoLichSu.THU_CONG,luot.cheDo);
            }
            s.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
            s.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
            s.onActivity(x -> {
                Object khung=truong(x,"khungNghe");
                assertFalse(((BottomSheetDialog)truong(khung,"dialog")).isShowing());
                assertNull(truong(khung,"player"));
            });
        }
    }

    @Test public void chiTietTuViTriGiuCheDoGpsDemoVaKhongTuPhatLaiKhiXoay() {
        String[] cheDo={KhoLichSu.AUTO_MAIN,KhoLichSu.DEMO};
        for(int i=0;i<cheDo.length;i++) {
            try(ActivityScenario<ChiTietDiaDiemActivity> s=ActivityScenario.launch(
                    new Intent(context,ChiTietDiaDiemActivity.class).putExtra("viTriDiaDiem",2)
                            .putExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI,true)
                            .putExtra(ChiTietDiaDiemActivity.CHE_DO_VI_TRI,cheDo[i]))) {
                doiSoLuot(i+1);
                KhoLichSu.Luot luot=doc(a).get(0);
                assertEquals(2,luot.diaDiem); assertEquals(0,luot.diem);
                assertEquals(cheDo[i],luot.cheDo);
                s.recreate();
                s.onActivity(x -> {
                    assertFalse(x.getIntent().hasExtra(ChiTietDiaDiemActivity.PHAT_TU_VI_TRI));
                    assertNull(truong(x,"khungNghe"));
                });
                assertEquals(i+1,doc(a).size());
            }
        }
    }

    @Test public void mediaPlayerBatDauMoiGhiPauseTiepTucKhongTrungDungNgheLaiTaoLuotMoi() {
        Intent intent=new Intent(context,ThamQuanActivity.class).putExtra("idMarker",1);
        try(ActivityScenario<ThamQuanActivity> s=ActivityScenario.launch(intent)) {
            s.onActivity(x -> {
                x.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                x.moKhungThuyetMinh(false);
            });
            assertTrue(doc(a).isEmpty());
            s.onActivity(x -> {
                ((TextView)x.findViewById(R.id.tqMoTaKhuVuc)).setText("Khi nghe bài thuyết minh, ứng dụng ghi lại thời gian và địa điểm. Tạm dừng rồi tiếp tục vẫn thuộc cùng một lượt nghe.");
                x.moKhungThuyetMinh(true);
            });
            doiSoLuot(1);
            s.onActivity(x -> {
                Object khung=truong(x,"khungNghe");
                BottomSheetDialog dialog=(BottomSheetDialog)truong(khung,"dialog");
                MediaPlayer player=(MediaPlayer)truong(khung,"player");
                assertTrue(player.isPlaying());
                dialog.findViewById(R.id.nghePhatTamDung).performClick();
                assertFalse(player.isPlaying());
                dialog.findViewById(R.id.nghePhatTamDung).performClick();
                assertTrue(player.isPlaying());
            });
            assertEquals(1,doc(a).size());
            s.onActivity(x -> ((BottomSheetDialog)truong(truong(x,"khungNghe"),"dialog")).findViewById(R.id.ngheDung).performClick());
            s.onActivity(x -> ((BottomSheetDialog)truong(truong(x,"khungNghe"),"dialog")).findViewById(R.id.nghePhatTamDung).performClick());
            doiSoLuot(2);
            assertEquals(0,doc(a).get(0).diaDiem); assertEquals(1,doc(a).get(0).diem);
            assertEquals(KhoLichSu.THU_CONG,doc(a).get(0).cheDo);
        }
    }
}
