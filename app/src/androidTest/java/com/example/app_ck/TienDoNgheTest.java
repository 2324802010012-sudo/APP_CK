package com.example.app_ck;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.app_ck.data.local.AppDatabase;
import com.example.app_ck.data.repository.KhoLichSu;
import com.example.app_ck.narration.TienDoNghe;
import com.example.app_ck.session.PhienDangNhap;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TienDoNgheTest {
    @Test public void tienDoGiuKhoaCuVaKhongGhiCheoPhienNoiDungHoacCheDo() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        long phienCu = PhienDangNhap.id(context);
        String tenCu = PhienDangNhap.ten(context);
        String ten = "progress_" + Long.toHexString(System.nanoTime());
        long id = -1;
        try (AppDatabase helper = new AppDatabase(context)) {
            assertTrue(helper.dangKy(ten,"Progress123!","PROGRESS_RECOVERY"));
            id = helper.dangNhap(ten,"Progress123!");
            PhienDangNhap.luu(context,id,ten);
            TienDoNghe tienDo = new TienDoNghe(context);
            assertEquals(0,tienDo.mo(0,1,KhoLichSu.THU_CONG,"Bài gốc"));
            tienDo.luu(12000);
            assertEquals(12000,new TienDoNghe(context).mo(0,1,KhoLichSu.THU_CONG,"Bài gốc"));
            assertEquals(0,new TienDoNghe(context).mo(1,1,KhoLichSu.THU_CONG,"Bài gốc"));
            assertEquals(0,new TienDoNghe(context).mo(0,1,KhoLichSu.THU_CONG,"Bài đã sửa"));
            for (String cheDo : new String[]{KhoLichSu.DEMO,KhoLichSu.AUTO_SUB,KhoLichSu.AUTO_MAIN}) {
                TienDoNghe tuDong = new TienDoNghe(context);
                assertEquals(0,tuDong.mo(0,1,cheDo,"Bài gốc"));
                assertFalse(tuDong.dungPhien());
                tuDong.luu(99000);
            }
            PhienDangNhap.dangXuat(context);
            assertFalse(tienDo.dungPhien());
            tienDo.luu(30000);
            tienDo.xoa();
            PhienDangNhap.luu(context,id,ten);
            assertEquals(12000,tienDo.mo(0,1,KhoLichSu.THU_CONG,"Bài gốc"));
            tienDo.luu(2000);
            assertEquals(0,tienDo.mo(0,1,KhoLichSu.THU_CONG,"Bài gốc"));
            tienDo.luu(6000);
            tienDo.xoa();
            assertEquals(0,tienDo.mo(0,1,KhoLichSu.THU_CONG,"Bài gốc"));
        } finally {
            if (id > 0) {
                android.content.SharedPreferences prefs = context.getSharedPreferences("tien_do_thuyet_minh",Context.MODE_PRIVATE);
                android.content.SharedPreferences.Editor editor = prefs.edit();
                for (String key : prefs.getAll().keySet()) if (key.startsWith(id + "_")) editor.remove(key);
                editor.apply();
                try (AppDatabase helper = new AppDatabase(context)) {
                    helper.getWritableDatabase().delete("tai_khoan","id=?",new String[]{String.valueOf(id)});
                }
            }
            if (phienCu > 0) PhienDangNhap.luu(context,phienCu,tenCu);
            else PhienDangNhap.dangXuat(context);
        }
    }
}
