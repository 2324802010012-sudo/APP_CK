package com.example.app_ck.narration;

import java.util.ArrayList;
import java.util.List;

/** Chia bài dài đúng giới hạn TTS và ánh xạ thời gian toàn bài sang từng tệp âm thanh. */
public final class DoanAmThanh {
    public static List<String> chia(String text, int gioiHan) {
        if (gioiHan < 2) throw new IllegalArgumentException("Giới hạn phải ≥ 2");
        List<String> ds = new ArrayList<>();
        for (int start=0; start<text.length();) {
            int end=Math.min(start+gioiHan,text.length());
            if (end<text.length()) {
                int space=text.lastIndexOf(' ',end-1);
                if (space>start+gioiHan/2) end=space+1;
                if (Character.isHighSurrogate(text.charAt(end-1))) end--;
            }
            ds.add(text.substring(start,end)); start=end;
        }
        return ds;
    }

    public static int[] tim(List<Integer> durations,int position) {
        int offset=Math.max(0,position);
        for (int i=0;i<durations.size();i++) {
            if (offset<durations.get(i) || i==durations.size()-1)
                return new int[]{i,Math.min(offset,Math.max(0,durations.get(i)-1))};
            offset-=durations.get(i);
        }
        return new int[]{0,0};
    }
}
