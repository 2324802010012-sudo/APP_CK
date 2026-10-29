package com.example.app_ck.data.model;

public final class HoSoTaiKhoan {
    public static final String USER = "USER", ADMIN = "ADMIN", ACTIVE = "ACTIVE", LOCKED = "LOCKED";
    public final long id;
    public final String tenDangNhap, hoTen, email;
    public final String vaiTro, trangThai;
    public final long ngayTao;
    public HoSoTaiKhoan(long id, String tenDangNhap, String hoTen, String email) {
        this(id, tenDangNhap, hoTen, email, USER, ACTIVE, 0);
    }
    public HoSoTaiKhoan(long id, String tenDangNhap, String hoTen, String email,
                        String vaiTro, String trangThai, long ngayTao) {
        this.id = id; this.tenDangNhap = tenDangNhap; this.hoTen = hoTen; this.email = email;
        this.vaiTro = vaiTro; this.trangThai = trangThai; this.ngayTao = ngayTao;
    }
    public String tenHienThi() { return hoTen == null || hoTen.trim().isEmpty() ? tenDangNhap : hoTen; }
    public boolean hoatDong() { return ACTIVE.equals(trangThai); }
}
