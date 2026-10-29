package com.example.app_ck.ui.home;

import com.example.app_ck.data.model.DiaDiem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class GoiYDiaDiem {
    private GoiYDiaDiem() {}
    public static String chuanHoa(String text) {
        return Normalizer.normalize(text == null ? "" : text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').trim();
    }
    private static Set<String> chuDe(String text) {
        Set<String> ds = new HashSet<>();
        for (String s : chuanHoa(text).split("[–—-]")) if (!s.trim().isEmpty()) ds.add(s.trim());
        return ds;
    }
    private static int diem(DiaDiem d, Set<String> quanTam) {
        Set<String> giao = chuDe(d.danhMuc);
        giao.retainAll(quanTam);
        return giao.size();
    }
    public static List<DiaDiem> deXuat(List<DiaDiem> tatCa, Set<Integer> daLuu) {
        Set<String> quanTam = new HashSet<>();
        for (DiaDiem d : tatCa) if (daLuu.contains(d.id)) quanTam.addAll(chuDe(d.danhMuc));
        List<DiaDiem> ketQua = new ArrayList<>();
        for (DiaDiem d : tatCa) if (!daLuu.contains(d.id)) ketQua.add(d);
        ketQua.sort(Comparator.<DiaDiem>comparingInt(d -> diem(d, quanTam)).reversed().thenComparingInt(d -> d.id));
        return ketQua;
    }
    public static boolean khop(DiaDiem d, String tuKhoa, String danhMuc) {
        String vanBan = chuanHoa(d.ten + " " + d.diaChi + " " + (d.moTa == null ? "" : d.moTa));
        return vanBan.contains(chuanHoa(tuKhoa)) && (danhMuc == null || danhMuc.equals(d.danhMuc));
    }
}
