package com.example.app_ck;
import android.content.Context;
import android.content.SharedPreferences;
public class PhienDangNhap {
    private static SharedPreferences p(Context c) { return c.getSharedPreferences("phien_dang_nhap",Context.MODE_PRIVATE); }
    public static long id(Context c) { return p(c).getLong("id",-1); }
    public static boolean daDangNhap(Context c) { return id(c)>0; }
    public static void luu(Context c,long id,String ten) { p(c).edit().putLong("id",id).putString("ten",ten).apply(); }
    public static String ten(Context c) { return p(c).getString("ten",""); }
    public static void dangXuat(Context c) { p(c).edit().clear().apply(); }
}
