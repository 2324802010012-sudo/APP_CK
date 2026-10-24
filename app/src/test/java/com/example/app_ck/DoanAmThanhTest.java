package com.example.app_ck;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class DoanAmThanhTest {
    @Test public void chiaBaiDaiKhongMatChuVaKhongCatDoiUnicode() {
        String text="Một câu chuyện Việt Nam 🌿. ".repeat(600);
        List<String> ds=DoanAmThanh.chia(text,127);
        assertTrue(ds.size()>1);
        assertEquals(text,String.join("",ds));
        for(String d:ds) {
            assertTrue(d.length()<=127);
            assertFalse(Character.isHighSurrogate(d.charAt(d.length()-1)));
            assertFalse(Character.isLowSurrogate(d.charAt(0)));
        }
    }
    @Test public void tuaToanBaiDungBienCacTep() {
        List<Integer> ds=Arrays.asList(1000,2200,800);
        assertArrayEquals(new int[]{0,0},DoanAmThanh.tim(ds,-1));
        assertArrayEquals(new int[]{1,0},DoanAmThanh.tim(ds,1000));
        assertArrayEquals(new int[]{1,1200},DoanAmThanh.tim(ds,2200));
        assertArrayEquals(new int[]{2,0},DoanAmThanh.tim(ds,3200));
        assertArrayEquals(new int[]{2,799},DoanAmThanh.tim(ds,9999));
    }
}
