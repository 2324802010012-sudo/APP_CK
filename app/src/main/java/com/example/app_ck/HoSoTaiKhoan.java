package com.example.app_ck;

public final class HoSoTaiKhoan {
    public final long id;
    public final String tenDangNhap, hoTen, email;
    public HoSoTaiKhoan(long id, String tenDangNhap, String hoTen, String email) {
        this.id = id; this.tenDangNhap = tenDangNhap; this.hoTen = hoTen; this.email = email;
    }
    public String tenHienThi() { return hoTen == null || hoTen.trim().isEmpty() ? tenDangNhap : hoTen; }
}
