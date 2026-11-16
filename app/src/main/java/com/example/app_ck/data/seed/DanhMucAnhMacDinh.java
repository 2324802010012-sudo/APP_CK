package com.example.app_ck.data.seed;

public final class DanhMucAnhMacDinh {

    // ID dia diem: 0=Dinh, 1=Bao tang, 2=Hue,
    // 3=Con Dao, 4=Van Mieu.
    // Vi tri trong mang = ID diem tham quan - 1.
    private static final String[][] ANH = {

            // 0. DINH DOC LAP - 25 DIEM
            {
                    "anh_phong_noi_cac",
                    "anh_phong_dai_yen",
                    "dinh_cau_thang_trung_tam",
                    "dinh_phong_khanh_tiet",
                    "dinh_hoi_dong_an_ninh",
                    "dinh_phong_lam_viec_tong_thong",
                    "dinh_phong_khach_tong_thong",
                    "dinh_phong_khach_pho_tong_thong",
                    "dinh_phong_trinh_quoc_thu",
                    "dinh_khu_gia_dinh_tong_thong",
                    "dinh_thu_vien",
                    "dinh_phong_khach_phu_nhan",
                    "dinh_phong_chieu_phim",
                    "dinh_phong_giai_tri",
                    "dinh_tu_phuong_vo_su_lau",
                    "dinh_truc_thang_uh1",
                    "dinh_phong_tham_muu_tac_chien",
                    "dinh_phong_thong_tin_lien_lac",
                    "dinh_phong_ngu_tong_thong",
                    "dinh_phong_truc_chien_tong_thong",
                    "dinh_nha_bep",
                    "dinh_xe_jeep",
                    "dinh_xe_mercedes",
                    "dinh_phim_tu_lieu",
                    "dinh_phong_tap_ban"
            },

            // 1. BAO TANG CHUNG TICH CHIEN TRANH - 13 DIEM
            {
                    "bao_tang_nhung_su_that_lich_su",
                    "bao_tang_hoi_niem",
                    "bao_tang_chien_tranh_hoa_binh",
                    "bao_tang_chat_doc_da_cam_chien_tranh",
                    "bao_tang_bo_cau_trang",
                    "bao_tang_toi_ac_chien_tranh",
                    "bao_tang_hau_qua_chat_doc_da_cam",
                    "bao_tang_tranh_thieu_nhi_da_cam",
                    "bao_tang_phong_hop",
                    "bao_tang_the_gioi_ung_ho_viet_nam",
                    "bao_tang_trien_lam_ngan_ngay",
                    "bao_tang_che_do_lao_tu",
                    "bao_tang_trung_bay_ngoai_troi"
            },

            // 2. DAI NOI HUE - 10 DIEM
            {
                    "hue_ngo_mon",
                    "hue_dien_thai_hoa",
                    "hue_nen_dien_can_chanh",
                    "hue_dien_kien_trung",
                    "hue_duyet_thi_duong",
                    "hue_thai_binh_lau",
                    "hue_vuon_co_ha",
                    "hue_cung_dien_tho",
                    "hue_cung_truong_sanh",
                    "hue_hau_ho"
            },

            // 3. NHA TU CON DAO - 19 DIEM
            {
                    "con_dao_trai_phu_hai",
                    "con_dao_trai_phu_son",
                    "con_dao_trai_phu_tuong",
                    "con_dao_trai_phu_binh",
                    "con_dao_trai_phu_an",
                    "con_dao_nha_chua_dao",
                    "con_dao_bao_tang",
                    "con_dao_cau_tau_914",
                    "con_dao_trai_phu_tho",
                    "con_dao_trai_phu_phong",
                    "con_dao_trai_phu_hung",
                    "con_dao_trai_9",
                    "con_dao_khu_dieu_tra",
                    "con_dao_cau_ma_thien_lanh",
                    "con_dao_chuong_bo",
                    "con_dao_so_lo_voi",
                    "con_dao_nha_cong_quan",
                    "con_dao_nghia_trang_hang_duong",
                    "con_dao_chuong_cop_phap"
            },

            // 4. VAN MIEU - QUOC TU GIAM - 16 DIEM
            {
                    "van_mieu_phuong_dinh_ho_van",
                    "van_mieu_bia_ha_ma",
                    "van_mieu_nghi_mon",
                    "van_mieu_cong_van_mieu",
                    "van_mieu_cong_dai_trung",
                    "van_mieu_khue_van_cac",
                    "van_mieu_nha_bia_tien_si",
                    "van_mieu_gieng_thien_quang",
                    "van_mieu_cong_dai_thanh",
                    "van_mieu_dien_dai_thanh",
                    "van_mieu_cong_thai_hoc",
                    "van_mieu_nha_thai_hoc",
                    "van_mieu_lau_trong",
                    "van_mieu_lau_chuong",
                    "van_mieu_cong_ra",
                    "van_mieu_nha_bat_giac"
            }
    };

    private DanhMucAnhMacDinh() {}

    public static String lay(int idDiaDiem, int idDiem) {

        if (idDiaDiem < 0 || idDiaDiem >= ANH.length) {
            return "";
        }

        String[] danhSach = ANH[idDiaDiem];

        if (idDiem <= 0 || idDiem > danhSach.length) {
            return "";
        }

        return danhSach[idDiem - 1];
    }

    public static int soDiem(int idDiaDiem) {

        if (idDiaDiem < 0 || idDiaDiem >= ANH.length) {
            return 0;
        }

        return ANH[idDiaDiem].length;
    }

    public static int tongSoAnh() {

        int tong = 0;

        for (String[] danhSach : ANH) {
            tong += danhSach.length;
        }

        return tong;
    }
}