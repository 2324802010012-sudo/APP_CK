
package com.example.app_ck.data.model;

public class DiaDiem {

    public int id;
    public float banKinhAuto = 100;
    public String ten;
    public String diaChi;
    public String moTa;
    public String danhMuc;
    public double viDo;
    public double kinhDo;
    public String tenAnh;

    public DiaDiem(
            int id,
            String ten,
            String diaChi,
            String moTa,
            double viDo,
            double kinhDo,
            String tenAnh
    ) {
        this.id = id;
        this.ten = ten;
        this.diaChi = diaChi;
        this.moTa = moTa;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.tenAnh = tenAnh;
    }
}
