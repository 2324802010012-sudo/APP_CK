package com.example.app_ck;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Bài giới thiệu và nguồn biên soạn trong Java; SQLite lưu nội dung trên thiết bị. */
final class DuLieuNoiDungThamQuan {
    static final int PHIEN_BAN = 1;
    static final String NGAY_DOI_CHIEU = "2026-10-24";
    static final String NGUYEN_TAC = "Biên soạn lại, không sao chép bài nguồn. Gợi ý tham quan là hướng dẫn của ứng dụng. ID, tên và tầng theo sơ đồ hiện có; ghi chú nêu rõ khi nguồn chỉ xác nhận bối cảnh chung. Giờ, vé và mở phòng cần kiểm tra trực tiếp với đơn vị quản lý.";
    static final Map<String, Nguon> NGUON = taoNguon();
    static final List<Bai> DIA_DIEM = Collections.unmodifiableList(Arrays.asList(
            dinhDocLap(), baoTangChungTich(), daiNoiHue(), conDao(), vanMieu()));

    private DuLieuNoiDungThamQuan() {}

    static final class Nguon {
        final String ten, url, coQuan;
        Nguon(String ten, String url, String coQuan) {
            this.ten = ten;
            this.url = url;
            this.coQuan = coQuan;
        }
    }

    static final class Bai {
        final int id;
        final String ten, gioiThieu, diemNhan, thamQuan, ghiChu;
        final List<String> nguon;
        final List<Bai> cacDiem;

        Bai(int id, String ten, String gioiThieu, String diemNhan, String thamQuan,
                String ghiChu, String[] nguon, Bai... cacDiem) {
            this.id = id;
            this.ten = ten;
            this.gioiThieu = gioiThieu;
            this.diemNhan = diemNhan;
            this.thamQuan = thamQuan;
            this.ghiChu = ghiChu;
            this.nguon = Collections.unmodifiableList(Arrays.asList(nguon.clone()));
            this.cacDiem = Collections.unmodifiableList(Arrays.asList(cacDiem.clone()));
        }

        String noiDung() {
            return "Giới thiệu\n" + gioiThieu
                    + "\n\nĐiểm đáng chú ý\n" + diemNhan
                    + "\n\nGợi ý tham quan\n" + thamQuan
                    + (ghiChu.isEmpty() ? "" : "\n\nLưu ý về thông tin\n" + ghiChu);
        }
    }

    static Bai lay(int diaDiem, int diem) {
        for (Bai d : DIA_DIEM) {
            if (d.id != diaDiem) continue;
            if (diem == 0) return d;
            for (Bai p : d.cacDiem) if (p.id == diem) return p;
        }
        throw new IllegalArgumentException("Không có nội dung: " + diaDiem + "/" + diem);
    }

    private static Map<String, Nguon> taoNguon() {
        Map<String, Nguon> ds = new LinkedHashMap<>();
        ds.put("dinh_tong", new Nguon("Di tích lịch sử Dinh Độc Lập", "https://dinhdoclap.gov.vn/di-tich-lich-su-dinh-doc-lap/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_kt", new Nguon("Kiến trúc Dinh Độc Lập", "https://dinhdoclap.gov.vn/kien-truc-dinh-doc-lap/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_anh", new Nguon("Dấu ấn sau 50 năm giải phóng", "https://dinhdoclap.gov.vn/anh-dinh-doc-lap-noi-luu-giu-dau-an-sau-50-nam-giai-phong/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_1", new Nguon("Phòng Nội các", "https://dinhdoclap.gov.vn/diem-dung-chan-so-01-phong-noi-cac-cabinet-room/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_2", new Nguon("Phòng Đại yến", "https://dinhdoclap.gov.vn/diem-dung-chan-so-02-phong-dai-yen-state-banqueting-hall/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_4", new Nguon("Phòng Khánh tiết", "https://dinhdoclap.gov.vn/diem-dung-chan-so-04-phong-khanh-tiet-conference-hall/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_5", new Nguon("Phòng Hội đồng An ninh Quốc gia", "https://dinhdoclap.gov.vn/diem-dung-chan-so-05-phong-hoi-dong-an-ninh-quoc-gia-national-security-council-chamber/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_6", new Nguon("Phòng làm việc Tổng thống", "https://dinhdoclap.gov.vn/diem-dung-chan-so-06-phong-lam-viec-cua-tong-thong-presidential-office/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_tu", new Nguon("Tủ sơn mài phòng Tiếp khách Tổng thống", "https://dinhdoclap.gov.vn/nhung-net-ve-mai-lan-cuc-truc-tren-tu-phong-tiep-khach-tong-thong/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_tranh", new Nguon("Bình Ngô Đại Cáo trên tranh sơn mài", "https://dinhdoclap.gov.vn/binh-ngo-dai-cao-tu-ang-hung-van-den-sac-voc-son-mai/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_thuy_mac", new Nguon("Tranh Việt Nam Quốc Tổ", "https://dinhdoclap.gov.vn/hanh-trinh-60-nam-cua-buc-tranh-thuy-mac-viet-nam-quoc-to/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_bom", new Nguon("Dấu tích cuộc ném bom ngày 08/04/1975", "https://dinhdoclap.gov.vn/dau-tich-su-kien-nem-bom-dinh-doc-lap-ngay-08-04-1975/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_hoc", new Nguon("Học sinh tham quan các không gian Dinh", "https://dinhdoclap.gov.vn/doan-hoc-sinh-truong-tieu-hoc-minh-dao-tham-quan-di-tich-lich-su-dinh-doc-lap/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_thamquan", new Nguon("Hướng dẫn tham quan", "https://dinhdoclap.gov.vn/tour-guide/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("dinh_tin", new Nguon("Thông báo của di tích", "https://dinhdoclap.gov.vn/tin-tuc/", "Hội trường Thống Nhất – Di tích lịch sử Dinh Độc Lập"));
        ds.put("vm_ls", new Nguon("Lịch sử di tích", "https://vanmieu.gov.vn/vi/introduction/site-history", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_khach", new Nguon("Thông tin cho du khách", "https://vanmieu.gov.vn/vi/introduction/visitor-information", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_tienan", new Nguon("Khu Tiền Án", "https://vanmieu.gov.vn/vi/introduction/areas/inner-temple/front-court", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_vuon", new Nguon("Vườn Giám", "https://vanmieu.gov.vn/vi/introduction/areas/giam-garden", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_bia", new Nguon("Vườn bia Tiến sĩ và Giếng Thiên Quang", "https://vanmieu.gov.vn/vi/doctoral-stelae-garden", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_1", new Nguon("Phương Đình", "https://vanmieu.gov.vn/vi/visit/architecture/phuong-dinh", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_4", new Nguon("Cổng Văn Miếu", "https://vanmieu.gov.vn/vi/visit/architecture/van-mieu-gate", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_5", new Nguon("Cổng Đại Trung", "https://vanmieu.gov.vn/vi/visit/architecture/dai-trung-gate", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_6", new Nguon("Khuê Văn Các", "https://vanmieu.gov.vn/vi/visit/architecture/khue-van-pavilion", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_9", new Nguon("Cổng Đại Thành", "https://vanmieu.gov.vn/vi/visit/architecture/dai-thanh-gate", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_10", new Nguon("Điện Đại Thành", "https://vanmieu.gov.vn/vi/visit/architecture/dien-dai-thanh", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_12", new Nguon("Nhà Thái Học", "https://vanmieu.gov.vn/vi/visit/architecture/thai-hoc-house", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("vm_lau", new Nguon("Lầu Chuông – Lầu Trống", "https://vanmieu.gov.vn/vi/visit/architecture/bell-drum-tower", "Trung tâm Hoạt động VHKH Văn Miếu – Quốc Tử Giám"));
        ds.put("bt_tong", new Nguon("Giới thiệu và thông báo bảo tàng", "https://baotangchungtichchientranh.vn/", "Bảo tàng Chứng tích Chiến tranh"));
        ds.put("bt_ds", new Nguon("Các chuyên đề thường xuyên", "https://baotangchungtichchientranh.vn/vi/trung-bay-va-su-kien/trung-bay-thuong-xuyen", "Bảo tàng Chứng tích Chiến tranh"));
        ds.put("bt_ngan", new Nguon("Triển lãm chuyên đề", "https://baotangchungtichchientranh.vn/vi/trung-bay-va-su-kien/trung-bay-chuyen-de", "Bảo tàng Chứng tích Chiến tranh"));
        ds.put("bt_1", new Nguon("Những sự thật lịch sử", "https://baotangchungtichchientranh.vn/vi/trung-bay-va-su-kien/trung-bay/nhung-su-that-lich-su", "Bảo tàng Chứng tích Chiến tranh"));
        ds.put("bt_2", new Nguon("Hồi niệm", "https://baotangchungtichchientranh.vn/vi/trung-bay-va-su-kien/trung-bay/hoi-niem", "Bảo tàng Chứng tích Chiến tranh"));
        ds.put("bt_4", new Nguon("Ảnh Goro Nakamura", "https://baotangchungtichchientranh.vn/vi/trung-bay-va-su-kien/trung-bay/chat-doc-da-cam-trong-chien-tranh-o-viet-nam", "Bảo tàng Chứng tích Chiến tranh"));
        ds.put("hue_tong", new Nguon("Quần thể di tích Cố đô Huế", "https://dsvh.gov.vn/quan-the-di-tich-co-do-hue-475", "Cục Di sản văn hóa"));
        ds.put("hue_1", new Nguon("Ngọ Môn", "https://huetourism.gov.vn/ngo-mon-bieu-tuong-vinh-hang-cua-co-do-hue/?pid=MjM3NTN8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_2", new Nguon("Điện Thái Hòa", "https://huetourism.gov.vn/Dien-Thai-Hoa.html/?pid=MjAzMDN8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_trungtu", new Nguon("Trùng tu Thái Hòa và khởi công Cần Chánh", "https://huetourism.gov.vn/khanh-thanh-dien-thai-hoa-sau-3-nam-trung-tu/?pid=MjM0ODV8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_4", new Nguon("Phục hồi điện Kiến Trung", "https://huetourism.gov.vn/dien-kien-trung-chuan-bi-mo-cua-don-du-khach-vao-tham-quan-trong-dip-tet-nguyen-dan/?pid=MjMxMTN8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_6", new Nguon("Thái Bình Lâu", "https://huetourism.gov.vn/thai-binh-lau-chon-nghi-ngoi-cua-cac-bac-de-vuong/?pid=MjUwNDB8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_7", new Nguon("Vườn Cơ Hạ", "https://huetourism.gov.vn/Hoa-lan-ba-mien-khoe-sac-tai-vuon-Ngu-uyen-Kinh-thanh-Hue.html/?pid=MTkyNjh8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_8", new Nguon("Trang trí cung Diên Thọ", "https://huetourism.gov.vn/Nghe-thuat-trang-tri-cung-Dien-Tho.html/?pid=MjA0ODl8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_9", new Nguon("Giếng cổ và phân bố cung điện", "https://huetourism.gov.vn/HE-THONG-GIENG-CO-TRONG-KIEN-TRUC-CUNG-DINH-HUE--LOAI-HINH-VA-DAC-DIEM-PHAN-BO.html/?pid=MjA3OTJ8Y3NkbGRs0", "Sở Du lịch thành phố Huế"));
        ds.put("hue_5", new Nguon("Nhà hát Duyệt Thị Đường", "https://vietnamtourism.vn/index.php/tourism/items/454/2", "Cục Du lịch Quốc gia Việt Nam"));
        ds.put("cd_1", new Nguon("Trại Phú Hải", "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-hai-trai-2/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_3", new Nguon("Trại Phú Tường", "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-tuong-trai-4/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_4", new Nguon("Trại Phú Bình", "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-binh-trai-7/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_5", new Nguon("Trại Phú An", "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-an-trai-6/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_6", new Nguon("Nhà Chúa Đảo", "https://trungtambaotonditichquocgiacondao.vn/di-tich-nha-chua-dao/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_8", new Nguon("Cầu tàu 914", "https://trungtambaotonditichquocgiacondao.vn/di-tich-cau-tau-914/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_10", new Nguon("Trại Phú Phong", "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-phong-trai-5/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_11", new Nguon("Trại Phú Hưng", "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-hung-trai-8/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_13", new Nguon("Khu điều tra", "https://trungtambaotonditichquocgiacondao.vn/di-tich-khu-dieu-tra/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_14", new Nguon("Cầu Ma Thiên Lãnh", "https://trungtambaotonditichquocgiacondao.vn/di-tich-cau-ma-thien-lanh/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_15", new Nguon("Khu biệt lập Chuồng Bò", "https://trungtambaotonditichquocgiacondao.vn/di-tich-khu-biet-lap-chuong-bo/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_16", new Nguon("Sở Lò Vôi", "https://trungtambaotonditichquocgiacondao.vn/di-tich-so-lo-voi/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_18", new Nguon("Nghĩa trang Hàng Dương", "https://trungtambaotonditichquocgiacondao.vn/nghia-trang-hang-duong-cd/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_19", new Nguon("Chuồng Cọp Pháp", "https://trungtambaotonditichquocgiacondao.vn/di-tich-chuong-cop-phap/", "Trung tâm Bảo tồn Di tích Quốc gia Côn Đảo"));
        ds.put("cd_tong", new Nguon("Hệ thống Nhà tù Côn Đảo", "https://www.condao.com.vn/vi/news/tai-nguyen-du-lich/nha-tu-con-dao-2750.html", "Cổng thông tin du lịch Côn Đảo – Ban quản lý du lịch Côn Đảo"));
        ds.put("cd_2", new Nguon("Trại Phú Sơn", "https://condao.com.vn/vi/news/di-tich-lich-su/diem-tham-quan-di-tich-trai-phu-son-con-dao-75.html", "Ban quản lý du lịch Côn Đảo"));
        ds.put("cd_9", new Nguon("Trại Phú Thọ", "https://condao.com.vn/en/news/historical-sites/phu-tho-prison-relic-con-dao-607.html", "Ban quản lý du lịch Côn Đảo"));
        ds.put("cd_17", new Nguon("Nhà Công Quán", "https://condao.com.vn/vi/news/di-tich-lich-su/diem-tham-quan-di-tich-nha-cong-quan-con-dao-74.html", "Ban quản lý du lịch Côn Đảo"));
        ds.put("cd_7", new Nguon("Chủ tịch nước thăm Bảo tàng Côn Đảo", "https://bvhttdl.gov.vn/Pages/chi-tiet.aspx?url=%2Fchu-tich-nuoc-vo-van-thuong-tham-bao-tang-con-dao-va-di-tich-lich-su-trai-phu-hai-20230720081030742.htm", "Bộ Văn hóa, Thể thao và Du lịch"));
        return Collections.unmodifiableMap(ds);
    }

    private static Bai dinhDocLap() {
        return new Bai(
                0,
                "Dinh Độc Lập",
                "Dinh Độc Lập tại 135 Nam Kỳ Khởi Nghĩa, Thành phố Hồ Chí Minh, gắn với sự kiện ngày 30/4/1975 và quá trình thống nhất đất nước. Công trình hiện nay do kiến trúc sư Ngô Viết Thụ thiết kế, khởi công ngày 1/7/1962 và khánh thành ngày 31/10/1966.",
                "Kiến trúc kết hợp giải pháp hiện đại với mỹ thuật Á Đông. Tuyến tham quan trong ứng dụng gồm phòng nghi lễ, nơi làm việc, không gian gia đình, sân thượng và khu hầm. Dinh được xếp hạng di tích quốc gia đặc biệt năm 2009.",
                "Nên xem tổng thể mặt đứng trước khi đi vào các phòng. Dùng sơ đồ để chọn đúng tầng và nghe từng bài; GPS không phân biệt được các phòng sát nhau. Tra cứu giờ, vé và quy định chụp ảnh tại kênh chính thức trước chuyến đi.",
                "",
                new String[]{"dinh_kt", "dinh_tong", "dinh_tin"},
                new Bai(
                        1,
                        "Phòng Nội các",
                        "Đây là nơi diễn ra các phiên họp của nội các chính quyền Việt Nam Cộng hòa. Bài giới thiệu của di tích ghi nhận có năm nội các trong thời kỳ 1967–1975.",
                        "Nội các cuối cùng do Vũ Văn Mẫu làm Thủ tướng thành lập ngày 29/4/1975, chưa kịp ra mắt trước sự kiện 30/4. Phòng cho thấy vai trò của Dinh như trung tâm điều hành chính quyền.",
                        "Đọc bảng chú thích và quan sát cách bố trí chỗ họp; đối chiếu với Phòng Hội đồng An ninh Quốc gia để phân biệt họp nội các với họp quân sự.",
                        "",
                        new String[]{"dinh_1"}),
                new Bai(
                        2,
                        "Phòng Đại yến",
                        "Phòng Đại yến phục vụ các cuộc chiêu đãi chính thức, có sức chứa hơn một trăm khách theo giới thiệu của di tích.",
                        "Không gian gắn với hoạt động ngoại giao, nghi lễ. Bữa tiệc nhân lễ nhậm chức của Nguyễn Văn Thiệu và Nguyễn Cao Kỳ diễn ra tại đây ngày 31/10/1967.",
                        "Quan sát bố cục bàn tiệc, màu sắc nội thất và các tác phẩm trang trí từ lối đi cho phép. Không chạm đồ bày; đọc nhãn để xác định tác phẩm và niên đại.",
                        "",
                        new String[]{"dinh_2"}),
                new Bai(
                        3,
                        "Cầu thang trung tâm",
                        "Cầu thang trung tâm tổ chức lối lưu thông từ đại sảnh lên tầng trên. Bài nghiên cứu về tranh Việt Nam Quốc Tổ của di tích mô tả mối liên hệ giữa đại sảnh, cầu thang và các phòng nghi lễ.",
                        "Bức tranh thủy mặc Việt Nam Quốc Tổ do họa sĩ Trọng Nội thực hiện là một điểm quan sát gắn với không gian đại sảnh.",
                        "Dừng nghe ở vị trí không cản dòng người. Quan sát trục nhìn giữa sảnh, cầu thang và phòng phía sau trước khi chuyển tầng.",
                        "",
                        new String[]{"dinh_thuy_mac"}),
                new Bai(
                        4,
                        "Phòng Khánh tiết",
                        "Phòng Khánh tiết là không gian hội họp, tiếp tân lớn với sức chứa khoảng năm trăm người theo trang giới thiệu của di tích.",
                        "Tại đây Nguyễn Văn Thiệu tuyên bố từ chức ngày 21/4/1975. Tháng 11/1975, phòng được dùng cho Hội nghị hiệp thương chính trị bàn việc thống nhất đất nước.",
                        "Đọc các mốc sự kiện để phân biệt chức năng trước và sau năm 1975. Quan sát quy mô phòng từ khu vực được phép, tránh đi vào phần đang tổ chức sự kiện.",
                        "",
                        new String[]{"dinh_4"}),
                new Bai(
                        5,
                        "Phòng Hội đồng An ninh Quốc gia",
                        "Phòng dùng cho các cuộc họp giữa tổng thống, tướng lĩnh và cố vấn về tình hình quân sự, an ninh.",
                        "Các bản đồ là thành phần quan trọng của không gian chỉ huy. Nguồn di tích ghi nhận cuộc họp sáng 25/3/1975 liên quan đến quyết định rút quân khỏi Huế về Đà Nẵng.",
                        "Phân biệt bản đồ tư liệu theo thời kỳ với bản đồ hành chính hiện nay. Quan sát ký hiệu và nhãn trưng bày thay vì suy đoán diễn biến chỉ từ vị trí trên bản đồ.",
                        "",
                        new String[]{"dinh_5"}),
                new Bai(
                        6,
                        "Phòng làm việc của Tổng thống",
                        "Đây là điểm tham quan về không gian làm việc của tổng thống Việt Nam Cộng hòa. Trang giới thiệu chính thức dành phần nội dung cho tiểu sử Nguyễn Văn Thiệu.",
                        "Nguyễn Văn Thiệu giữ chức tổng thống từ năm 1967 và tuyên bố từ chức ngày 21/4/1975. Có thể liên hệ điểm này với bài Phòng Khánh tiết để hiểu trình tự sự kiện.",
                        "Quan sát phòng từ ranh giới tham quan; chỉ xác định chủ sở hữu hoặc công dụng của hiện vật khi có nhãn. Không mặc định tất cả đồ dùng đều thuộc cùng một nhiệm kỳ.",
                        "",
                        new String[]{"dinh_6"}),
                new Bai(
                        7,
                        "Phòng khách của Tổng thống",
                        "Phòng tiếp khách ở tầng 2 có hai chiếc tủ sơn mài trang trí bằng nét vẽ vàng theo chủ đề mai, lan, cúc, trúc.",
                        "Nghiên cứu của di tích dẫn phiếu đặt hàng ngày 4/1/1967, xác định người thực hiện phần vẽ là Nguyễn Văn Triêm. Đây là ví dụ về mỹ thuật trang trí trong nội thất Dinh.",
                        "Tìm họa tiết trên cánh tủ và so sánh cách thể hiện cây cỏ với những chi tiết trang trí khác. Giữ khoảng cách để bảo vệ bề mặt sơn mài.",
                        "",
                        new String[]{"dinh_tu"}),
                new Bai(
                        8,
                        "Phòng khách của Phó Tổng thống",
                        "Điểm này nằm trong nhóm không gian tiếp khách của Dinh, được đánh dấu trên sơ đồ hiện có của ứng dụng.",
                        "Hãy so sánh diện tích cảm nhận, lối tiếp cận và cách bố trí chỗ ngồi với phòng khách Tổng thống; những quan sát này giúp đọc chức năng không gian.",
                        "Dùng bảng tên tại phòng để xác nhận điểm đang đứng. Muốn tìm hiểu một cuộc tiếp khách hoặc hiện vật cụ thể, cần đối chiếu hồ sơ trưng bày tại chỗ.",
                        "Nguồn được dẫn xác nhận bối cảnh chung của Dinh; chưa có bài chuyên biệt được đối chiếu cho phòng này. Không gán thêm sự kiện hoặc chủ sở hữu đồ vật.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        9,
                        "Phòng trình quốc thư",
                        "Phòng gắn với nghi lễ ngoại giao trình quốc thư. Tác phẩm sơn mài Bình Ngô Đại Cáo là điểm nổi bật được di tích giới thiệu trong không gian này.",
                        "Tác phẩm do Nguyễn Văn Minh thực hiện năm 1966. Việc đưa một văn bản lịch sử vào nghệ thuật sơn mài cho thấy sự kết hợp giữa nội dung dân tộc và không gian nghi lễ.",
                        "Quan sát bố cục, màu sắc từ khoảng cách cho phép. Không chụp đèn flash nếu có biển cấm; đọc chú thích để phân biệt tên tác phẩm, tác giả và nội dung văn bản.",
                        "",
                        new String[]{"dinh_tranh"}),
                new Bai(
                        10,
                        "Khu gia đình Tổng thống",
                        "Khu gia đình là phần không gian sinh hoạt được giới thiệu trong tuyến tham quan Dinh.",
                        "Nguồn hoạt động giáo dục của di tích xác nhận khu này cùng các phòng giải trí, chiếu phim là những điểm học sinh được tìm hiểu trong chuyến tham quan.",
                        "So sánh cách tổ chức không gian sinh hoạt với các phòng nghi lễ. Tôn trọng phần được khoanh giới hạn; không mở cửa, tủ hoặc sử dụng đồ trưng bày.",
                        "",
                        new String[]{"dinh_hoc"}),
                new Bai(
                        11,
                        "Thư viện",
                        "Thư viện là điểm được xác định trên sơ đồ tham quan tầng 3 của ứng dụng.",
                        "Có thể dùng điểm này để tìm hiểu vai trò của sách và không gian đọc trong một dinh thự có nhiều chức năng.",
                        "Quan sát nhãn sách hoặc bảng giới thiệu nếu được mở cho khách. Không rút sách khỏi giá; đừng suy luận thói quen đọc của nhân vật lịch sử từ cách trưng bày hiện nay.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        12,
                        "Phòng khách phu nhân Tổng thống",
                        "Điểm tiếp khách này nằm trong phần tầng 3 trên sơ đồ ứng dụng.",
                        "Gợi ý quan sát: lối vào, tương quan với khu gia đình và cách bố trí bàn ghế. Đây là các câu hỏi để đọc không gian khi tham quan.",
                        "Xác nhận tên phòng bằng biển tại chỗ. Chỉ dùng chú thích của di tích để xác định công năng và xuất xứ đồ trưng bày; không gán chuyện đời tư cho căn phòng.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        13,
                        "Phòng chiếu phim",
                        "Phòng chiếu phim thuộc các không gian được nhắc trong hoạt động tham quan giáo dục của di tích.",
                        "Điểm này bổ sung góc nhìn về chức năng sinh hoạt, giải trí, bên cạnh các phòng điều hành và nghi lễ.",
                        "Quan sát hướng ghế và khu vực trình chiếu từ lối cho phép. Phân biệt phòng chiếu phim lịch sử với chương trình phim tư liệu phục vụ du khách ở điểm khác.",
                        "",
                        new String[]{"dinh_hoc"}),
                new Bai(
                        14,
                        "Phòng giải trí",
                        "Phòng giải trí là một phần của tuyến tìm hiểu các không gian sinh hoạt được di tích giới thiệu cho học sinh.",
                        "Đặt điểm này cạnh phòng chiếu phim và khu gia đình để nhận ra sự phân chia giữa hoạt động riêng và hoạt động chính thức trong Dinh.",
                        "Xem bảng tên để nhận biết từng đồ trưng bày; không sử dụng dụng cụ giải trí. Nếu phòng đóng cửa, tiếp tục tuyến theo chỉ dẫn của nhân viên.",
                        "",
                        new String[]{"dinh_hoc"}),
                new Bai(
                        15,
                        "Tứ phương vô sự lầu",
                        "Tứ phương vô sự lầu nằm ở phần trên cùng, được bài giới thiệu kiến trúc của di tích nhắc đến trong cách tổ chức hình khối Dinh.",
                        "Nguồn kiến trúc lý giải tổng thể công trình bằng các hình tượng chữ Hán và quan niệm thiết kế Á Đông.",
                        "Quan sát từ vị trí được phép để nhận ra quan hệ giữa khối lầu, mái và mặt đứng. Không vượt rào để tìm góc nhìn hoặc lên khu vực không mở cho khách.",
                        "",
                        new String[]{"dinh_kt"}),
                new Bai(
                        16,
                        "Trực thăng UH-1",
                        "Sân đáp có trực thăng UH-1 và dấu tích liên quan cuộc ném bom Dinh ngày 8/4/1975 của phi công Nguyễn Thành Trung.",
                        "Trang di tích giới thiệu mảnh bom có chữ ký cùng hai vị trí bom rơi được đánh dấu trên sân đáp sau khi khôi phục.",
                        "Quan sát dấu sơn, chú thích và trực thăng như những thành phần trưng bày khác nhau. Không đồng nhất hiện vật đồng dạng với phương tiện nguyên gốc khi chưa đọc nhãn xác nhận.",
                        "",
                        new String[]{"dinh_bom"}),
                new Bai(
                        17,
                        "Tham mưu tác chiến",
                        "Điểm tham mưu tác chiến thuộc tuyến khu hầm trên sơ đồ ứng dụng.",
                        "Khu hầm là một bộ phận chức năng của Dinh; có thể đặt câu hỏi về quan hệ giữa điều hành trên mặt đất và hoạt động trong không gian bảo vệ.",
                        "Quan sát bảng chú thích và bố trí phòng. Đừng coi các bản đồ trưng bày là dữ liệu đang vận hành hoặc tự suy ra mệnh lệnh lịch sử từ cách sắp đặt.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        18,
                        "Thông tin liên lạc",
                        "Thông tin liên lạc là một chức năng của khu hầm được giới thiệu trong tổng quan di tích.",
                        "Gợi ý quan sát: sự liên hệ giữa thiết bị, bàn làm việc và đường đi trong khu hầm. Mỗi thiết bị cần được nhận diện qua nhãn trưng bày.",
                        "Không chạm nút, dây hoặc máy. Khi đi theo nhóm, dừng ở vị trí không chắn hành lang; nghe bằng tai nghe để không ảnh hưởng người khác.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        19,
                        "Phòng ngủ Tổng thống",
                        "Đây là điểm được đánh dấu trong khu hầm trên sơ đồ ứng dụng, khác với khu gia đình ở tầng trên.",
                        "Có thể so sánh cách bố trí không gian nghỉ trong khu hầm với không gian sinh hoạt thường ngày để hiểu thiết kế đa chức năng.",
                        "Xem biển tên và giới hạn tham quan tại chỗ. Chưa có căn cứ để khẳng định thời gian lưu trú hoặc những đêm nghỉ cụ thể của nhân vật tại phòng.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        20,
                        "Phòng trực chiến của Tổng thống",
                        "Phòng trực chiến được đánh dấu ở khu hầm trong sơ đồ hiện có.",
                        "Điểm này nên được đọc cùng phòng tham mưu và thông tin liên lạc để đặt câu hỏi về cách các bộ phận liên hệ với nhau.",
                        "Đối chiếu chú thích trước khi gán công dụng cho điện thoại, bản đồ hoặc thiết bị. Không dùng tín hiệu GPS để nhận diện phòng trong hầm; chọn thủ công trên sơ đồ.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        21,
                        "Bếp",
                        "Khu bếp nằm ở tầng trệt theo tuyến đánh số của ứng dụng.",
                        "Điểm này gợi mở phần công việc hậu cần phía sau hoạt động sinh hoạt và tiếp tân trong Dinh.",
                        "Quan sát khu vực được mở, đọc nhãn dụng cụ và phân biệt vật thật với phần phục dựng nếu có. Không suy đoán thực đơn hoặc số suất ăn khi không có tư liệu xác nhận.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}),
                new Bai(
                        22,
                        "Xe Jeep",
                        "Theo bài ảnh của di tích, xe Jeep M151A2 trưng bày ở lối ra khu hầm từng đưa Dương Văn Minh đến Đài phát thanh Sài Gòn trưa 30/4/1975.",
                        "Nguồn liên hệ phương tiện với việc đọc tuyên bố đầu hàng tại đài phát thanh. Cần phân biệt câu chuyện xe này với những phương tiện khác trong khuôn viên.",
                        "Đọc biển hiện vật để xác nhận mẫu xe và hồ sơ trưng bày. Không lên xe hoặc chạm bộ phận; đối chiếu mốc thời gian với phần giới thiệu sự kiện ngày 30/4.",
                        "",
                        new String[]{"dinh_anh"}),
                new Bai(
                        23,
                        "Xe Mercedes",
                        "Bài ảnh chính thức của di tích giới thiệu xe Mercedes gắn với việc sử dụng của Nguyễn Văn Thiệu.",
                        "Hiện vật phương tiện bổ sung góc nhìn về sinh hoạt và di chuyển, bên cạnh hồ sơ về các phòng làm việc.",
                        "Đọc nhãn để nhận diện đúng chiếc xe; không lên xe hoặc chạm vào thân xe. So sánh câu chuyện hiện vật với bối cảnh chung thay vì suy ra mọi chuyến đi của người sử dụng.",
                        "",
                        new String[]{"dinh_anh"}),
                new Bai(
                        24,
                        "Phim tư liệu",
                        "Đây là điểm xem tư liệu trong tuyến tham quan trên sơ đồ ứng dụng.",
                        "Nội dung phim, giờ chiếu và khả năng phục vụ phụ thuộc tổ chức tham quan tại thời điểm đến; danh mục trong ứng dụng không bảo đảm luôn có suất chiếu.",
                        "Hỏi nhân viên về chương trình đang phục vụ. Sau khi xem, đối chiếu mốc thời gian trong phim với bài giới thiệu tại phòng Khánh tiết và khu trưng bày.",
                        "Đây là hướng dẫn sử dụng điểm tham quan; chưa xác minh một phim hoặc lịch chiếu cố định.",
                        new String[]{"dinh_thamquan"}),
                new Bai(
                        25,
                        "Phòng tập bắn",
                        "Phòng tập bắn được đánh dấu trong tuyến tầng trệt trên sơ đồ ứng dụng.",
                        "Khi tham quan, hãy dựa vào biển giới thiệu để tìm hiểu cấu tạo không gian và công dụng của thiết bị được trưng bày.",
                        "Tuân thủ giới hạn lối đi, không chạm thiết bị hoặc diễn lại thao tác sử dụng. Không suy đoán loại vũ khí hay lịch tập khi chưa có hồ sơ hiện vật.",
                        "Tên và vị trí theo sơ đồ của ứng dụng. Nguồn chính thức hiện dẫn ở mức tổng quan; chưa đối chiếu được bài riêng về niên đại, số lượng hoặc người sử dụng từng hiện vật tại phòng.",
                        new String[]{"dinh_tong"}));
    }

    private static Bai baoTangChungTich() {
        return new Bai(
                1,
                "Bảo tàng Chứng tích Chiến tranh",
                "Bảo tàng tại 28 Võ Văn Tần, Thành phố Hồ Chí Minh, thành lập ngày 4/9/1975. Đơn vị sưu tầm, nghiên cứu và trưng bày tư liệu về chiến tranh ở Việt Nam, hậu quả đối với con người và thông điệp bảo vệ hòa bình.",
                "Các chuyên đề tiếp cận lịch sử qua tài liệu, hiện vật, phóng sự ảnh và câu chuyện của nạn nhân. Sơ đồ ứng dụng đánh dấu 13 điểm, bao gồm cả phòng chức năng; số điểm này không đồng nghĩa với số chuyên đề thường xuyên của bảo tàng.",
                "Chọn phòng thủ công theo tầng. Có hình ảnh về thương tích và mất mát: người lớn nên cân nhắc nội dung phù hợp cho trẻ em. Mở kênh chính thức để kiểm tra giờ, vé và phòng đang phục vụ.",
                "Khi đối chiếu nguồn ngày 24/10/2026, trang bảo tàng có thông báo tạm dừng các không gian Hồi niệm, Việt Nam – Chiến tranh và Hòa bình, Chất độc da cam trong chiến tranh Việt Nam. Cần kiểm tra thông báo mới nhất; ứng dụng không cập nhật trạng thái mở phòng trực tiếp.",
                new String[]{"bt_tong", "bt_ds"},
                new Bai(
                        1,
                        "Những sự thật lịch sử",
                        "Chuyên đề giới thiệu tiến trình chiến tranh ở Việt Nam thông qua tư liệu và hiện vật. Trang bảo tàng công bố bộ trưng bày gồm 66 ảnh, 20 tài liệu và 153 hiện vật.",
                        "Điểm quan trọng là đọc tài liệu trong bối cảnh thời gian và nguồn gốc của từng chứng tích.",
                        "Theo dõi mốc năm và nhãn xuất xứ; phân biệt ảnh ghi tại thời điểm sự kiện với tài liệu hồi cứu. Có thể ghi lại một hiện vật để tìm hiểu tiếp ở nguồn chính thức.",
                        "",
                        new String[]{"bt_1"}),
                new Bai(
                        2,
                        "Hồi niệm",
                        "Hồi niệm tưởng nhớ những phóng viên ảnh đã thiệt mạng khi làm nhiệm vụ tại Đông Dương. Bộ sưu tập do Tim Page và Horst Faas thực hiện với sự hỗ trợ của Thông tấn xã Việt Nam.",
                        "Nguồn bảo tàng giới thiệu 275 ảnh của 134 phóng viên thuộc 11 quốc tịch, cho thấy nhiều góc nhìn của người ghi lại chiến tranh.",
                        "Đọc tên tác giả, nơi chụp và chú thích; phân biệt người chụp ảnh với nhân vật trong ảnh. Dành thời gian cho câu chuyện về nghề báo và trách nhiệm lưu giữ ký ức.",
                        "Trước khi đến, mở thông báo của bảo tàng để kiểm tra phòng có phục vụ không.",
                        new String[]{"bt_2", "bt_tong"}),
                new Bai(
                        3,
                        "Việt Nam – Chiến tranh và Hòa bình",
                        "Chuyên đề là bộ ảnh phóng sự của nhiếp ảnh gia Nhật Bản Ishikawa Bunyo; bảo tàng giới thiệu 123 ảnh.",
                        "Nhan đề đặt chiến tranh và hòa bình trong cùng một hành trình nhìn lại Việt Nam qua ảnh.",
                        "Đọc niên đại từng ảnh trước khi so sánh hoàn cảnh đời sống. Tự đặt câu hỏi ảnh cho biết điều gì và phần nào cần thêm tư liệu để hiểu.",
                        "Trước khi đến, mở thông báo của bảo tàng để kiểm tra phòng có phục vụ không.",
                        new String[]{"bt_ds", "bt_tong"}),
                new Bai(
                        4,
                        "Chất độc da cam trong chiến tranh Việt Nam",
                        "Bộ ảnh gồm 42 tác phẩm của nhà nhiếp ảnh Nhật Bản Goro Nakamura về chiến tranh và hậu quả chất độc da cam.",
                        "Công việc ghi chép bằng ảnh của tác giả giúp người xem tiếp cận hậu quả kéo dài đối với con người qua nhiều thời kỳ.",
                        "Đọc câu chuyện và thời điểm chụp, giữ thái độ tôn trọng nhân vật. Không suy luận chẩn đoán bệnh của một cá nhân chỉ từ hình ảnh.",
                        "Trước khi đến, mở thông báo của bảo tàng để kiểm tra phòng có phục vụ không.",
                        new String[]{"bt_4", "bt_tong"}),
                new Bai(
                        5,
                        "Bồ câu trắng – Phòng giáo dục thiếu nhi",
                        "Đây là điểm phòng giáo dục thiếu nhi trên sơ đồ mà ứng dụng đang sử dụng.",
                        "Gợi ý cho gia đình: cùng trẻ trao đổi về hòa bình, sự đồng cảm và cách ứng xử trước hình ảnh lịch sử khó tiếp nhận.",
                        "Hỏi quầy thông tin về việc mở phòng, lịch hoạt động và nhóm tuổi phù hợp. Không mặc định mọi chương trình giáo dục đều diễn ra hằng ngày.",
                        "Tên và vị trí theo sơ đồ ứng dụng; nguồn tổng quan xác nhận định hướng giáo dục hòa bình, chưa có hồ sơ chính thức riêng được đối chiếu về lịch hoặc trang thiết bị phòng.",
                        new String[]{"bt_tong"}),
                new Bai(
                        6,
                        "Tội ác chiến tranh xâm lược",
                        "Chuyên đề tập hợp chứng tích về hậu quả của chiến tranh đối với đất nước và người dân Việt Nam.",
                        "Mỗi tư liệu cần được đọc cùng chú thích để hiểu bối cảnh và xuất xứ.",
                        "Nội dung có thể gây xúc động mạnh. Dừng nghỉ khi cần, cân nhắc trước khi cho trẻ xem và tránh chụp lại hình ảnh nếu nội quy không cho phép.",
                        "",
                        new String[]{"bt_ds"}),
                new Bai(
                        7,
                        "Hậu quả chất độc da cam ở Việt Nam",
                        "Chuyên đề giới thiệu tác động của chất độc hóa học và nghị lực vượt khó của các nạn nhân chất độc da cam/dioxin.",
                        "Trang danh mục công bố 100 ảnh, 10 tài liệu và 20 hiện vật; nội dung hướng đến câu chuyện con người trong hậu quả chiến tranh.",
                        "Đọc phần giới thiệu nạn nhân và hoạt động hỗ trợ. Không đồng nhất toàn bộ người khuyết tật với nạn nhân chất độc da cam khi chưa có hồ sơ xác định.",
                        "",
                        new String[]{"bt_ds"}),
                new Bai(
                        8,
                        "Hậu quả chất độc da cam qua tranh thiếu nhi",
                        "Điểm này được ghi trên sơ đồ ứng dụng với chủ đề tranh thiếu nhi.",
                        "Gợi ý quan sát: màu sắc, hình tượng và thông điệp do trẻ thể hiện. Cảm nhận nghệ thuật cần được phân biệt với chứng cứ xác định nguyên nhân y khoa.",
                        "Đọc tên tác giả, tuổi và tên tác phẩm nếu có nhãn. Hỏi nhân viên về bộ tranh đang trưng bày vì nội dung có thể thay đổi.",
                        "Nguồn danh mục xác nhận chủ đề hậu quả chất độc da cam ở mức chung; chưa xác minh riêng số lượng tranh, tác giả hoặc tình trạng mở điểm này.",
                        new String[]{"bt_ds"}),
                new Bai(
                        9,
                        "Phòng họp",
                        "Phòng họp là điểm chức năng được đánh số trên sơ đồ ứng dụng, không được mô tả thành một chuyên đề lịch sử.",
                        "Có thể sử dụng điểm này để nhận biết vị trí khi đi theo nhóm; các hoạt động chỉ tổ chức theo lịch của bảo tàng.",
                        "Không đi vào khi có cuộc họp hoặc biển hạn chế. Muốn tham dự buổi giao lưu, cần kiểm tra thông báo và hình thức đăng ký chính thức.",
                        "Chưa có bài giới thiệu riêng về phòng họp; không công bố sức chứa, lịch sử hoặc lịch sự kiện chưa được xác nhận.",
                        new String[]{"bt_tong"}),
                new Bai(
                        10,
                        "Thế giới ủng hộ Việt Nam kháng chiến",
                        "Chuyên đề nói về phong trào nhân dân thế giới ủng hộ Việt Nam trong giai đoạn 1954–1975, bao gồm cả nhân dân Mỹ.",
                        "Các tư liệu cho thấy sự đa dạng của hoạt động đoàn kết quốc tế trong thời kỳ này.",
                        "Đối chiếu địa danh, tổ chức và thời điểm trên chú thích để nhận ra sự đa dạng của phong trào. Không coi một hiện vật là đại diện cho quan điểm của mọi người trong một quốc gia.",
                        "",
                        new String[]{"bt_ds"}),
                new Bai(
                        11,
                        "Phòng triển lãm ngắn ngày",
                        "Đây là không gian phục vụ những trưng bày có thời hạn; chủ đề thay đổi theo chương trình của bảo tàng.",
                        "Danh mục trực tuyến công bố tên và thời gian của từng triển lãm, giúp phân biệt hoạt động đang diễn ra với tư liệu sự kiện đã kết thúc.",
                        "Xem ngày bắt đầu, kết thúc trước khi đến. Đọc bảng giới thiệu ngay lối vào để biết chủ đề và đơn vị phối hợp của lần trưng bày hiện tại.",
                        "Ứng dụng không gắn cố định một triển lãm đã hết hạn cho phòng này.",
                        new String[]{"bt_ngan"}),
                new Bai(
                        12,
                        "Chế độ lao tù trong chiến tranh",
                        "Chuyên đề giới thiệu hệ thống nhà tù và điều kiện giam giữ trong chiến tranh ở Việt Nam.",
                        "Nguồn bảo tàng nhắc đến các nhà tù như Côn Đảo, Phú Quốc, Chí Hòa và các hiện vật về chế độ giam cầm.",
                        "Phân biệt mô hình tái hiện với hiện vật gốc bằng chú thích. Có thể mở tour Côn Đảo trong ứng dụng để tìm hiểu thêm về di tích tại địa điểm thực tế.",
                        "",
                        new String[]{"bt_ds"}),
                new Bai(
                        13,
                        "Trưng bày ngoài trời",
                        "Đây là điểm trưng bày ngoài trời trên sơ đồ ứng dụng. Kênh chính thức của bảo tàng giới thiệu các nhóm sưu tập máy bay, xe tăng và bom mìn.",
                        "Quan sát quy mô phương tiện bên cạnh các hồ sơ và ảnh trong nhà sẽ giúp đặt thêm câu hỏi về vật chứng chiến tranh.",
                        "Đọc nhãn của từng phương tiện để biết loại, xuất xứ và hoàn cảnh sử dụng. Không trèo lên hiện vật hoặc tự kết luận một phương tiện đã tham gia trận đánh cụ thể.",
                        "Danh mục này không xác nhận mọi phương tiện trên sơ đồ vẫn được bày tại cùng vị trí; kiểm tra nhãn và hướng dẫn tại chỗ.",
                        new String[]{"bt_tong"}));
    }

    private static Bai daiNoiHue() {
        return new Bai(
                2,
                "Đại Nội Huế",
                "Đại Nội là không gian cung đình trọng yếu trong Quần thể di tích Cố đô Huế, gắn với triều Nguyễn. Kinh thành, Hoàng thành và Tử Cấm Thành là ba vòng thành có chức năng và phạm vi khác nhau; không nên dùng ba tên như đồng nghĩa.",
                "Trục cung điện liên kết các khu nghi lễ, nơi ở và công trình phục vụ hoàng gia. Quần thể di tích Cố đô Huế được UNESCO ghi danh Di sản thế giới ngày 11/12/1993.",
                "Đi theo lối đang mở, chú ý khu vực trùng tu. Sơ đồ ứng dụng gồm 10 điểm; AUTO chỉ xét các điểm đã cấu hình và vẫn cần GPS đủ tin cậy. Kiểm tra vé, giờ và biểu diễn tại đơn vị quản lý trước khi đến.",
                "",
                new String[]{"hue_tong", "hue_trungtu"},
                new Bai(
                        1,
                        "Ngọ Môn",
                        "Ngọ Môn được xây dựng năm 1833 dưới thời Minh Mạng, là cổng nghi lễ phía nam của Hoàng thành.",
                        "Phần đài và lầu Ngũ Phụng tạo hình ảnh tiêu biểu của kiến trúc cung đình Huế. Đây cũng là nơi diễn ra lễ thoái vị của Bảo Đại ngày 30/8/1945.",
                        "Quan sát sự cân xứng từ khoảng sân phía trước. Phân biệt vị trí nghi lễ, các lối đi và giới hạn tham quan hiện nay; không mặc định tất cả cửa đều mở cho khách.",
                        "",
                        new String[]{"hue_1"}),
                new Bai(
                        2,
                        "Điện Thái Hòa",
                        "Điện Thái Hòa khởi dựng năm 1805; công trình được di chuyển, bố trí lại vào năm 1833. Đây là nơi gắn với các đại lễ của triều đình.",
                        "Hệ cột gỗ, trang trí cung đình và tổ chức không gian làm nổi bật vai trò nghi lễ. Công trình hoàn thành đợt trùng tu và khánh thành ngày 23/11/2024.",
                        "Quan sát sân, lối vào và nội thất theo hướng dẫn bảo vệ di tích. Không chạm đồ thờ hoặc vị trí ngai; xem bảng giới thiệu để phân biệt cấu kiện bảo tồn và phần phục hồi.",
                        "",
                        new String[]{"hue_2", "hue_trungtu"}),
                new Bai(
                        3,
                        "Nền điện Cần Chánh",
                        "Cần Chánh thuộc trục cung điện chính. Tên Nền điện Cần Chánh trong ứng dụng giữ theo sơ đồ hiện có, phản ánh giai đoạn công trình còn dấu tích nền.",
                        "Sở Du lịch Huế đưa tin khởi công dự án phục hồi điện Cần Chánh ngày 23/11/2024. Vì vậy, mô tả đây mãi là một nền trống sẽ không phản ánh quá trình bảo tồn.",
                        "Kiểm tra biển công trường và hướng tuyến tại chỗ. Không đi vào khu đang thi công; xem tư liệu phục hồi để phân biệt dấu tích cũ với công trình đang được tái lập.",
                        "Bài không xác nhận tiến độ hoặc ngày hoàn thành hiện tại của dự án.",
                        new String[]{"hue_tong", "hue_trungtu"}),
                new Bai(
                        4,
                        "Điện Kiến Trung",
                        "Kiến Trung là công trình trên trục Tử Cấm Thành, gắn với thời Khải Định và Bảo Đại. Sở Du lịch giới thiệu việc phục hồi và chuẩn bị đón khách trở lại trong dịp Tết năm 2024.",
                        "Công trình giúp tìm hiểu sự thay đổi trong kiến trúc cung đình vào giai đoạn cuối triều Nguyễn và công tác phục hồi di sản.",
                        "Đọc bảng về lịch sử xây dựng, hư hại và trùng tu. Khi quan sát trang trí, phân biệt vật liệu còn lại với phần được phục hồi thay vì coi toàn bộ là nguyên trạng xưa.",
                        "",
                        new String[]{"hue_4"}),
                new Bai(
                        5,
                        "Duyệt Thị Đường",
                        "Duyệt Thị Đường là nhà hát cung đình xây dựng năm 1826 dưới triều Minh Mạng, phục vụ vua, hoàng tộc, đại thần và khách của triều đình.",
                        "Công trình gắn với âm nhạc, múa và sân khấu truyền thống trong hoàng cung, bổ sung góc nhìn về di sản biểu diễn bên cạnh kiến trúc.",
                        "Nếu muốn xem biểu diễn, kiểm tra lịch, vé và thời lượng tại đơn vị quản lý. Khi có chương trình, giữ yên lặng và tuân thủ quy định quay chụp.",
                        "",
                        new String[]{"hue_5"}),
                new Bai(
                        6,
                        "Thái Bình Lâu",
                        "Thái Bình Lâu được Sở Du lịch giới thiệu như không gian đọc sách, nghỉ ngơi của vua trong hoàng cung.",
                        "Kiến trúc hai tầng và các chi tiết trang trí khảm sành sứ là những nét đáng chú ý. Tư liệu kiến trúc cần được đọc cùng các giai đoạn xây dựng, tu bổ.",
                        "Quan sát trang trí từ lối mở; không chạm bề mặt khảm. Không gán cùng một niên đại cho mọi bộ phận khi bảng giới thiệu phân biệt nhiều lần sửa đổi.",
                        "",
                        new String[]{"hue_6"}),
                new Bai(
                        7,
                        "Vườn Cơ Hạ",
                        "Cơ Hạ là không gian vườn ngự trong khu vực Đại Nội. Sở Du lịch từng giới thiệu hoạt động trưng bày hoa lan tại vườn.",
                        "Cây xanh và cảnh quan vườn giúp bổ sung cách đọc hoàng cung ngoài các điện, cổng và nơi thờ tự.",
                        "Quan sát quan hệ giữa đường dạo, cây và công trình. Các cuộc trưng bày hoa là sự kiện theo kỳ; kiểm tra lịch trước khi đến thay vì mặc định luôn có triển lãm.",
                        "",
                        new String[]{"hue_7"}),
                new Bai(
                        8,
                        "Cung Diên Thọ",
                        "Cung Diên Thọ là nơi ở của các hoàng thái hậu, thái hoàng thái hậu trong hoàng cung triều Nguyễn.",
                        "Bài của Sở Du lịch giới thiệu các thành phần như chính điện Diên Thọ, điện Thọ Chỉ, Tạ Trường Du và Tịnh Minh Lâu cùng nghệ thuật trang trí.",
                        "Xem các bảng tên để phân biệt từng công trình trong cung. Quan sát từ xa chi tiết trang trí, giữ sự trang nghiêm và chỉ vào những khu được mở.",
                        "",
                        new String[]{"hue_8"}),
                new Bai(
                        9,
                        "Cung Trường Sanh",
                        "Trường Sanh nằm trong hệ thống cung điện phía tây Hoàng thành, được tài liệu của Sở Du lịch nhắc cùng khu Diên Thọ.",
                        "Gợi ý tham quan: so sánh tổ chức cảnh quan và lối tiếp cận giữa hai khu cung, đồng thời đọc bảng giới thiệu để xác định chức năng theo từng thời kỳ.",
                        "Không coi mọi phần kiến trúc nhìn thấy đều thuộc cùng giai đoạn. Ưu tiên tư liệu tại chỗ khi tìm hiểu người sử dụng cụ thể hoặc thay đổi công năng.",
                        "Nguồn được đối chiếu chủ yếu về phân bố công trình và giếng cổ; chưa dùng làm căn cứ cho một niên biểu đầy đủ của cung.",
                        new String[]{"hue_9"}),
                new Bai(
                        10,
                        "Hậu hồ",
                        "Hậu hồ là nhãn điểm cảnh quan trên sơ đồ hiện có của ứng dụng.",
                        "Có thể dùng điểm này để quan sát vai trò của mặt nước và cây xanh trong tổng thể cung đình. Tên trên sơ đồ cần được đối chiếu với biển tại di tích.",
                        "Đi theo đường dạo được mở, giữ khoảng cách với bờ nước. Nếu biển tại chỗ dùng tên khác, hỏi nhân viên để xác định đúng hồ trước khi tìm hiểu lịch sử.",
                        "Chưa xác minh được hồ cụ thể tương ứng nhãn Hậu hồ từ một hồ sơ chính thức riêng; không đồng nhất với hồ Tịnh Tâm, Học Hải hay công trình có tên khác.",
                        new String[]{"hue_tong"}));
    }

    private static Bai conDao() {
        return new Bai(
                3,
                "Hệ thống Nhà tù Côn Đảo",
                "Hệ thống Nhà tù Côn Đảo hình thành từ năm 1862, được mở rộng qua các thời kỳ và chấm dứt chức năng nhà tù năm 1975. Các di tích kể lại chế độ giam cầm, lao động khổ sai, quá trình học tập và đấu tranh của người tù.",
                "Tuyến trong ứng dụng gồm 19 điểm: trại giam, khu biệt lập, cơ sở phục vụ bộ máy nhà tù, bảo tàng và nghĩa trang. Đây là số điểm được ứng dụng giới thiệu, không phải cách đếm pháp lý các di tích thành phần được xếp hạng.",
                "Các điểm phân bố tách biệt; nên xem bảo tàng để nắm bối cảnh, sau đó chọn tuyến theo thời gian và điều kiện di chuyển. Đặt thuyết minh với đơn vị quản lý nếu cần. Khi viếng nghĩa trang, giữ trang nghiêm và thực hiện hướng dẫn tại chỗ.",
                "",
                new String[]{"cd_tong", "cd_1", "cd_18"},
                new Bai(
                        1,
                        "Trại Phú Hải",
                        "Phú Hải từng mang các tên Banh I, Lao I, Trại Cộng Hòa và Trại 2. Tên Phú Hải được đặt vào tháng 11/1974 theo Trung tâm Bảo tồn di tích.",
                        "Nguồn giới thiệu 10 phòng giam tập thể, phòng giam đặc biệt, 20 xà lim, khu đập đá và hầm xay lúa. Nơi đây còn gắn với việc tổ chức học tập của tù chính trị.",
                        "Phân biệt khu giam tập thể, xà lim và nơi lao động khi xem sơ đồ. Đọc chú thích để nhận ra công năng đã thay đổi qua các thời kỳ.",
                        "",
                        new String[]{"cd_1"}),
                new Bai(
                        2,
                        "Trại Phú Sơn",
                        "Trại Phú Sơn, còn gọi Banh II, được xây dựng từ năm 1908 và hoàn thành năm 1916. Sau Hiệp định Paris năm 1973, trại mang tên Phú Sơn.",
                        "Nguồn Ban quản lý du lịch giới thiệu 13 phòng giam lớn và 14 xà lim. Trại gắn với quá trình học tập, đoàn kết và đấu tranh của tù nhân.",
                        "Quan sát sự khác nhau giữa phòng tập thể và khu biệt giam. Khi đọc một câu chuyện nhân vật, đối chiếu đúng phòng và niên đại trên bảng giới thiệu.",
                        "",
                        new String[]{"cd_2"}),
                new Bai(
                        3,
                        "Trại Phú Tường",
                        "Phú Tường từng mang tên Banh III phụ, Lao III phụ và Trại 4. Trung tâm Bảo tồn ghi nhận công trình được xây dựng năm 1940.",
                        "Trại gồm tám phòng giam chia hai dãy cùng các công trình phụ. Cần phân biệt trại Phú Tường với khu Chuồng Cọp Pháp có lối liên hệ trong hệ thống giam cầm.",
                        "Xem cả sơ đồ liên kết giữa các khu để hiểu việc che giấu và kiểm soát không gian. Không coi hai điểm gần nhau là một bản ghi trùng lặp.",
                        "",
                        new String[]{"cd_3"}),
                new Bai(
                        4,
                        "Trại Phú Bình – Chuồng Cọp Mỹ",
                        "Trại 7 hoàn thành năm 1971, sau Hiệp định Paris mang tên Phú Bình. Công trình được giới thiệu như mô hình trại giam kiểu Mỹ.",
                        "Các phòng biệt lập, mái thấp và điều kiện giam cầm là nội dung chính. Tại đây tù nhân đã chủ động hành động trong cuộc giải phóng Côn Đảo rạng sáng 1/5/1975.",
                        "So sánh với Chuồng Cọp Pháp để nhận ra khác biệt cấu trúc; không gộp hai khu. Đọc mốc sự kiện giải phóng trên tư liệu tại chỗ.",
                        "",
                        new String[]{"cd_4"}),
                new Bai(
                        5,
                        "Trại Phú An",
                        "Trại 6 được đổi tên thành Phú An sau Hiệp định Paris. Nguồn Trung tâm Bảo tồn mô tả 20 phòng giam chia hai khu, cùng bốn xà lim.",
                        "Trại gắn với các cuộc đấu tranh của tù chính trị và việc thành lập Đảng bộ Lưu Chí Hiếu ngày 3/2/1972.",
                        "Khi đọc tư liệu, chú ý tên khu A, B có sự thay đổi theo giai đoạn. Dựa vào niên đại chú thích để tránh áp một cách đánh số cho mọi thời kỳ.",
                        "",
                        new String[]{"cd_5"}),
                new Bai(
                        6,
                        "Nhà Chúa Đảo",
                        "Dinh Chúa Đảo hình thành khoảng 1862–1876, là nơi ở và làm việc của người đứng đầu bộ máy cai trị trên đảo.",
                        "Theo Trung tâm Bảo tồn, nơi đây tập trung quyền lực hành chính, tư pháp và quân sự. Di tích được trùng tu, phục dựng nội thất năm 2019.",
                        "Đặt không gian này cạnh các trại giam để hiểu cách vận hành bộ máy nhà tù. Phân biệt nội thất phục dựng với hiện vật gốc theo nhãn.",
                        "",
                        new String[]{"cd_6"}),
                new Bai(
                        7,
                        "Bảo tàng Côn Đảo",
                        "Bảo tàng Côn Đảo lưu giữ, giới thiệu tư liệu và hiện vật về lịch sử, văn hóa, con người của đảo qua các thời kỳ.",
                        "Bài của Bộ Văn hóa, Thể thao và Du lịch ngày 20/7/2023 giới thiệu các phân khu về người tù, cuộc đấu tranh và những tấm gương yêu nước.",
                        "Đọc tên chuyên đề tại lối vào và hỏi nhân viên về tuyến đang phục vụ. Ghi lại thông tin xuất xứ của hiện vật cần tìm hiểu tiếp, thay vì suy ra nội dung chỉ từ ảnh trên sơ đồ.",
                        "Nguồn bài viết năm 2023 được dùng cho bối cảnh lịch sử; không xác nhận số hiện vật hay danh mục phòng đang mở tại thời điểm hiện nay.",
                        new String[]{"cd_7"}),
                new Bai(
                        8,
                        "Cầu tàu 914",
                        "Cầu tàu khởi công năm 1873, gắn với việc tù nhân khai thác, vận chuyển đá và xây dựng trong điều kiện khổ sai.",
                        "Trung tâm Bảo tồn giải thích 914 là con số mang tính ước lệ để tưởng nhớ người chết trong quá trình xây cầu, không phải một thống kê chính xác đã kiểm đếm.",
                        "Quan sát công trình từ lối được mở và giữ an toàn ven biển. Đọc thêm câu chuyện tù nhân trở về đất liền năm 1945 và năm 1975 để thấy nhiều lớp ký ức của nơi này.",
                        "",
                        new String[]{"cd_8"}),
                new Bai(
                        9,
                        "Trại Phú Thọ",
                        "Phú Thọ còn gọi Banh III, được xây dựng năm 1928. Nguồn du lịch Côn Đảo mô tả chức năng tiếp nhận, cách ly tù mới trước khi chuyển trại.",
                        "Cơ cấu giam giữ thay đổi qua các giai đoạn, có khu biệt lập được tù nhân gọi là chuồng gà.",
                        "Đọc sơ đồ theo từng thời kỳ; phân biệt Phú Thọ với Phú Tường và các khu biệt lập. Không suy ra tên trại chỉ từ một phòng giam tương tự.",
                        "",
                        new String[]{"cd_9"}),
                new Bai(
                        10,
                        "Trại Phú Phong",
                        "Trại 5 xây dựng năm 1962 và đổi tên Phú Phong sau Hiệp định Paris. Nguồn Trung tâm Bảo tồn giới thiệu 12 phòng giam chia ba dãy.",
                        "Trại gắn với cuộc đấu tranh của nữ tù chính trị trong những năm 1966–1968 và nằm phía trước khu Chuồng Cọp Pháp.",
                        "Đọc câu chuyện tập thể tù nhân thay vì chỉ nhìn kiến trúc. Đối chiếu vị trí trại với khu biệt lập để hiểu quan hệ giữa những phần trong hệ thống.",
                        "",
                        new String[]{"cd_10"}),
                new Bai(
                        11,
                        "Trại Phú Hưng",
                        "Trại 8, sau gọi Phú Hưng, thuộc đợt mở rộng hệ thống trại giam cuối thập niên 1960, đầu thập niên 1970.",
                        "Theo Trung tâm Bảo tồn, thiết kế có 20 phòng nhưng chỉ hoàn thành một phần; phần còn lại dừng xây dựng khi Hiệp định Paris được ký kết.",
                        "Nhận biết phần đã sử dụng và dấu tích xây dựng dở theo chú thích. Không suy ra số tù nhân thực tế từ số phòng trong thiết kế.",
                        "",
                        new String[]{"cd_11"}),
                new Bai(
                        12,
                        "Trại 9",
                        "Trại 9 là công trình xây dựng chưa hoàn thành; nguồn Cổng thông tin du lịch Côn Đảo liên hệ việc dừng thi công với Hiệp định Paris năm 1973.",
                        "Điểm này giúp nhận ra một dự án chưa đi vào hoàn chỉnh trong quá trình mở rộng nhà tù.",
                        "Quan sát dấu tích từ lối cho phép. Không mô tả trại như một khu giam đã vận hành đầy đủ hoặc gán số phòng sử dụng khi chưa có hồ sơ.",
                        "",
                        new String[]{"cd_tong"}),
                new Bai(
                        13,
                        "Khu điều tra",
                        "Công trình ban đầu được phía Pháp sử dụng làm nhà ở; từ năm 1960 được dùng làm khu điều tra theo Trung tâm Bảo tồn.",
                        "Đây là nơi hỏi cung, lưu trữ hồ sơ và phân loại tù nhân trước khi chuyển trại; các trường hợp bị nghi tổ chức đấu tranh cũng có thể bị đưa về đây.",
                        "Đặt điểm này trong mối liên hệ với hệ thống trại giam để hiểu quá trình kiểm soát tù nhân. Phân biệt chức năng điều tra với khu giam giữ lâu dài.",
                        "",
                        new String[]{"cd_13"}),
                new Bai(
                        14,
                        "Cầu Ma Thiên Lãnh",
                        "Di tích còn hai mố cầu của công trình xây dựng dang dở bằng sức lao động khổ sai của tù nhân.",
                        "Trung tâm Bảo tồn cho biết công trình bị bỏ dở sau Cách mạng tháng Tám năm 1945. Dấu tích hiện nay gắn với ký ức về người tù làm đường, xây cầu.",
                        "Kiểm tra lối tiếp cận và điều kiện đường trước khi đi. Không trèo lên mố cầu hoặc tự đi vào khu vực bị hạn chế; giữ nguyên đá và cấu kiện tại di tích.",
                        "",
                        new String[]{"cd_14"}),
                new Bai(
                        15,
                        "Khu biệt lập Chuồng Bò",
                        "Khu vực hình thành từ cuối thế kỷ XIX, ban đầu gắn với chăn nuôi và công việc phục vụ hệ thống nhà tù.",
                        "Công năng biến đổi thành khu giam giữ, biệt lập. Nguồn Trung tâm Bảo tồn nêu việc đưa tù chính trị bị suy kiệt về đây từ cuối năm 1969.",
                        "Phân biệt công năng chăn nuôi ban đầu với những hạng mục bị chuyển đổi. Đọc chú thích về từng giai đoạn thay vì xem mọi dấu tích cùng một thời.",
                        "",
                        new String[]{"cd_15"}),
                new Bai(
                        16,
                        "Sở Lò Vôi",
                        "Sở Lò Vôi được xây dựng từ năm 1864 và mở rộng trong giai đoạn 1920–1921 theo Trung tâm Bảo tồn.",
                        "Tù nhân phải lấy san hô và nung thành vôi cung cấp cho việc xây dựng. Đây là chứng tích lao động khổ sai bên ngoài các buồng giam.",
                        "Quan sát cấu trúc lò từ khu vực cho phép, đọc sơ đồ sản xuất. Không nhặt vật liệu hoặc mang mẫu đá, san hô khỏi di tích.",
                        "",
                        new String[]{"cd_16"}),
                new Bai(
                        17,
                        "Nhà Công Quán",
                        "Nhà Công Quán là ngôi nhà khách do Pháp xây dựng cuối thế kỷ XIX trong khu vực dinh thự Chúa Đảo, gần lối ra Cầu tàu 914.",
                        "Ban quản lý du lịch giới thiệu việc nhà soạn nhạc Camille Saint-Saëns lưu lại Côn Đảo trong khoảng tháng 3–4/1895.",
                        "Quan sát kiến trúc và tài liệu giới thiệu về chuyến lưu trú. Khi tìm hiểu tác phẩm âm nhạc, cần đối chiếu tên tác phẩm và lịch sử sáng tác từ hồ sơ chuyên biệt.",
                        "",
                        new String[]{"cd_17"}),
                new Bai(
                        18,
                        "Nghĩa trang Hàng Dương",
                        "Hàng Dương là nơi tưởng niệm những người đã hy sinh trong thời kỳ nhà tù Côn Đảo. Công trình được khởi công xây dựng, tôn tạo ngày 19/12/1992.",
                        "Nguồn quản lý chia nghĩa trang thành các khu A, B, C, D. Nơi đây có phần mộ Lê Hồng Phong, Nguyễn An Ninh, Võ Thị Sáu cùng nhiều chiến sĩ và đồng bào.",
                        "Thực hiện hướng dẫn thăm viếng, ăn mặc lịch sự, nói khẽ và giữ vệ sinh. Tra cứu phần mộ qua hệ thống chính thức hoặc nhờ nhân viên hỗ trợ.",
                        "",
                        new String[]{"cd_18"}),
                new Bai(
                        19,
                        "Chuồng Cọp Pháp",
                        "Khu kỷ luật được xây dựng năm 1940, do tù nhân gọi là Chuồng Cọp. Nguồn Trung tâm Bảo tồn mô tả phòng giam có song sắt phía trên và hành lang kiểm soát của cai ngục.",
                        "Khu giam được che giấu bằng các lối nhỏ liên hệ với những trại xung quanh. Cần phân biệt với mô hình Chuồng Cọp Mỹ tại Phú Bình.",
                        "Quan sát cấu trúc phòng, hành lang và khoảng không lộ thiên theo tuyến hướng dẫn. Đây là không gian tưởng niệm: không tạo dáng mô phỏng tra tấn hoặc vượt rào.",
                        "",
                        new String[]{"cd_19"}));
    }

    private static Bai vanMieu() {
        return new Bai(
                4,
                "Văn Miếu – Quốc Tử Giám",
                "Văn Miếu được lập năm 1070 dưới triều Lý Thánh Tông; Quốc Tử Giám thành lập năm 1076 dưới triều Lý Nhân Tông. Quần thể tại 58 Quốc Tử Giám, Hà Nội, gắn với lịch sử giáo dục và truyền thống tôn sư trọng đạo.",
                "Không gian gồm Nội tự, Vườn Giám và Hồ Văn. Các cổng, sân, khu bia và khu thờ tạo nhiều lớp kiến trúc. Công trình hiện nay chứa dấu tích và lần tu bổ của nhiều thời kỳ, không phải tất cả đều được dựng từ thời Lý.",
                "Sơ đồ ứng dụng có 16 điểm, bao gồm cả lối ra. Đọc bảng giới thiệu và chọn điểm thủ công khi đứng gần nhiều công trình. Giữ trang nghiêm, không chạm bia hoặc xoa đầu rùa; kiểm tra lịch hoạt động và vé trên kênh chính thức.",
                "",
                new String[]{"vm_ls", "vm_khach"},
                new Bai(
                        1,
                        "Phương Đình – Hồ Văn",
                        "Phương Đình nằm trên gò Kim Châu trong Hồ Văn, phía trước khu Nội tự. Công trình hiện nay hoàn thành năm 2022 theo trang kiến trúc của di tích.",
                        "Kiến trúc hai tầng, mái tám hướng tạo điểm nhấn trên mặt nước. Lịch sử gò và hồ cần được phân biệt với niên đại công trình phục hồi hiện nay.",
                        "Kiểm tra lối vào và hoạt động tại Hồ Văn trước khi qua. Quan sát từ bờ được mở, không vượt rào hoặc xuống sát mép nước.",
                        "",
                        new String[]{"vm_1"}),
                new Bai(
                        2,
                        "Bia Hạ Mã",
                        "Hai bia Hạ Mã thuộc khu Tiền Án phía trước Văn Miếu. Hạ mã nghĩa là xuống ngựa.",
                        "Nội dung bia nhắc người đi qua, từ quan lại đến dân thường, xuống ngựa để thể hiện sự tôn kính đối với nơi thờ học.",
                        "Quan sát chữ và vị trí hai bên lối vào; không chạm hoặc tô lên mặt bia. Có thể liên hệ tập quán xưa với việc giữ trật tự và sự kính trọng khi tham quan ngày nay.",
                        "",
                        new String[]{"vm_tienan"}),
                new Bai(
                        3,
                        "Nghi Môn",
                        "Nghi Môn là tên điểm trên sơ đồ ứng dụng tại khu vực trước cổng. Trang chính thức mô tả khu Tiền Án bắt đầu bằng bốn trụ lớn và hai bia Hạ Mã.",
                        "Hai trụ giữa cao hơn, trang trí nghê; các trụ ngoài có hình phượng. Đây là các chi tiết để nhận biết phần Tứ trụ trước lối vào.",
                        "Đối chiếu biển tại chỗ với nhãn Nghi Môn trên sơ đồ; không nhầm phần trụ ngoài với Cổng Văn Miếu có mái ở phía trong.",
                        "Nguồn dùng tên Tứ trụ trong khu Tiền Án; nhãn Nghi Môn được giữ theo sơ đồ hiện có của ứng dụng.",
                        new String[]{"vm_tienan"}),
                new Bai(
                        4,
                        "Cổng Văn Miếu",
                        "Cổng Văn Miếu dẫn vào khu Nội tự. Theo trang kiến trúc của di tích, công trình hiện thấy được xây dựng vào đầu thế kỷ XX.",
                        "Cổng có hai tầng, ba lối đi và hệ mái truyền thống; các chi tiết gỗ trang trí làm rõ vai trò của lối vào nghi lễ.",
                        "Quan sát từ ngoài trước khi đi qua để thấy tổng thể. Phân biệt năm thành lập Văn Miếu với niên đại của cổng hiện còn.",
                        "",
                        new String[]{"vm_4"}),
                new Bai(
                        5,
                        "Cổng Đại Trung",
                        "Đại Trung Môn nằm trong lớp không gian Nhập Đạo, trên trục chính sau Cổng Văn Miếu.",
                        "Công trình ba gian với kết cấu gỗ; hai lối bên mang tên Thành Đức và Đạt Tài. Tên gọi gợi liên hệ giữa học tập, phẩm chất và tài năng.",
                        "Đi chậm qua sân, đọc bảng tên của cổng chính và lối phụ. Quan sát kiến trúc từ lối đi, không dựa vào cột hoặc chặn đường khách khác.",
                        "",
                        new String[]{"vm_5"}),
                new Bai(
                        6,
                        "Khuê Văn Các",
                        "Khuê Văn Các được dựng năm 1805 dưới thời Tổng trấn Nguyễn Văn Thành. Đây là công trình gắn với biểu tượng văn hiến của Hà Nội.",
                        "Phần trên bằng gỗ đặt trên bốn trụ gạch, có các cửa tròn. Trang di tích lý giải tên gọi bằng hình tượng sao Khuê và ý nghĩa văn chương, tri thức.",
                        "Quan sát sự tương phản giữa trụ, gác và khoảng trống. Chụp ảnh từ vị trí không cản lối, giữ khoảng cách bảo vệ công trình.",
                        "",
                        new String[]{"vm_6"}),
                new Bai(
                        7,
                        "Nhà bia Tiến sĩ",
                        "Khu bia còn 82 tấm ghi danh 1.304 tiến sĩ của các khoa thi từ năm 1442 đến 1779. Những bia đầu tiên được dựng năm 1484 dưới triều Lê Thánh Tông.",
                        "Bia cung cấp tên người đỗ, quê quán và tư liệu về khoa cử. Các dãy nhà che bia hiện nay được xây dựng năm 1994 để bảo vệ di sản.",
                        "Đọc một bản dịch văn bia hoặc phần giới thiệu, phân biệt năm thi với năm dựng bia. Không sờ bia, xoa đầu rùa hoặc lấy bản dập.",
                        "",
                        new String[]{"vm_bia"}),
                new Bai(
                        8,
                        "Giếng Thiên Quang",
                        "Giếng Thiên Quang nằm ở trung tâm khu vườn bia, hai phía đông và tây là các dãy bia Tiến sĩ.",
                        "Trang di tích mô tả giếng hình vuông, mỗi cạnh khoảng ba mươi mét. Mặt nước tạo khoảng mở giữa các dãy kiến trúc.",
                        "Quan sát quan hệ đối xứng giữa giếng, bia và cổng. Giữ khoảng cách với lan can và bờ nước; không ném tiền, rác hoặc đồ vật xuống giếng.",
                        "",
                        new String[]{"vm_bia"}),
                new Bai(
                        9,
                        "Cổng Đại Thành",
                        "Đại Thành Môn dẫn vào khu thờ Đại Thành. Công trình có ba gian và hệ cửa gỗ trên trục của di tích.",
                        "Trang chính thức giới thiệu tấm biển có ghi mốc lập Văn Miếu năm 1070 và lần tu sửa năm 1888; hai lối bên mang tên Kim Thanh và Ngọc Chấn.",
                        "Đọc thông tin niên đại như dấu vết nhiều giai đoạn, không coi đó là chứng cứ mọi cấu kiện đều cùng tuổi. Giữ trật tự khi vào không gian thờ.",
                        "",
                        new String[]{"vm_9"}),
                new Bai(
                        10,
                        "Điện Đại Thành",
                        "Khu Đại Thành là trung tâm thờ tự trong Nội tự. Trang di tích giới thiệu tòa Bái đường với chín gian, bốn mươi cột gỗ.",
                        "Các đồ thờ, hoành phi và biểu tượng tại đây cần được tìm hiểu trong chức năng nghi lễ, tôn vinh đạo học.",
                        "Quan sát qua khu vực cho phép, ăn mặc phù hợp và nói khẽ. Không chạm đồ thờ, tự ý di chuyển vật phẩm hoặc bước vào khu hạn chế.",
                        "",
                        new String[]{"vm_10"}),
                new Bai(
                        11,
                        "Cổng Thái Học",
                        "Đây là điểm chuyển tiếp sang khu Thái Học trên sơ đồ tham quan của ứng dụng.",
                        "Khu Thái Học hiện nay được xây dựng lại năm 1999–2000 trên nền khu Quốc Tử Giám cũ; cần phân biệt lịch sử trường học với tuổi công trình đang thấy.",
                        "Xem bảng tên và giới thiệu khu trước khi tiếp tục tới Tiền đường, Hậu đường. Không mặc định cổng và mọi bộ phận xung quanh còn nguyên từ thế kỷ XI.",
                        "Nguồn đối chiếu là bài về Nhà Thái Học và bối cảnh khu vực; chưa xác minh riêng hồ sơ niên đại cổng.",
                        new String[]{"vm_12"}),
                new Bai(
                        12,
                        "Nhà Thái Học",
                        "Khu Quốc Tử Giám cũ bị phá hủy năm 1946. Công trình Thái Học hiện nay được xây dựng trong giai đoạn 1999–2000.",
                        "Không gian gồm Tiền đường và Hậu đường. Hậu đường thờ Chu Văn An cùng các vua Lý Thánh Tông, Lý Nhân Tông và Lê Thánh Tông ở những tầng tương ứng theo giới thiệu của di tích.",
                        "Đọc bảng để phân biệt vai trò của từng nhân vật đối với giáo dục. Coi công trình là nơi tưởng niệm và giới thiệu truyền thống, không gọi toàn bộ là kiến trúc nguyên gốc thời Lý.",
                        "",
                        new String[]{"vm_12"}),
                new Bai(
                        13,
                        "Lầu Trống",
                        "Lầu Trống là một trong hai công trình bố trí cân xứng hai bên khu Thái Học.",
                        "Trang di tích đặt lầu trong đợt xây dựng khu Thái Học hiện nay, khởi công năm 1999; đây không phải tháp nguyên trạng từ thời lập Văn Miếu.",
                        "Quan sát kết cấu gỗ và mái từ sân. Không tự đánh trống; việc sử dụng trong nghi thức thuộc chương trình do đơn vị quản lý tổ chức.",
                        "",
                        new String[]{"vm_lau"}),
                new Bai(
                        14,
                        "Lầu Chuông",
                        "Lầu Chuông cùng Lầu Trống tạo bố cục cân xứng tại khu Thái Học được xây dựng lại cuối thế kỷ XX.",
                        "Công trình là phần của tổ hợp tưởng niệm truyền thống giáo dục; kết cấu và hình thức theo ngôn ngữ kiến trúc truyền thống.",
                        "Đọc bảng giới thiệu chuông để xác định niên đại và nội dung minh văn. Không tự lên lầu hoặc gióng chuông khi chưa có hướng dẫn.",
                        "",
                        new String[]{"vm_lau"}),
                new Bai(
                        15,
                        "Cổng ra",
                        "Cổng ra là điểm điều hướng kết thúc tuyến đánh số trong ứng dụng.",
                        "Điểm này giúp người tham quan kiểm tra tuyến đã đi và tìm lối rời khu Nội tự, không được gán một niên đại hoặc sự kiện lịch sử riêng.",
                        "Ưu tiên biển chỉ dẫn và lối ra đang mở tại di tích. Kiểm tra quy định ra vào lại trước khi rời cổng; giữ vé nếu đơn vị quản lý yêu cầu.",
                        "Đây là hướng dẫn di chuyển của ứng dụng. Nguồn không được dùng để khẳng định một công trình lịch sử mang tên Cổng ra.",
                        new String[]{"vm_khach"}),
                new Bai(
                        16,
                        "Nhà Bát Giác – Vườn Giám",
                        "Nhà Bát Giác nằm trong Vườn Giám phía tây Văn Miếu. Trang di tích cho biết công trình được xây dựng khoảng năm 2000.",
                        "Mặt bằng tám cạnh, mái nhiều lớp và không gian mở tạo điểm dừng trong cảnh quan cây xanh. Vườn Giám được cải tạo qua các dự án từ năm 1996 đến 2004.",
                        "Quan sát hình khối và quan hệ với vườn; kiểm tra lịch nếu muốn tham gia hoạt động văn hóa. Không gọi nhà này là công trình còn nguyên từ thời Quốc Tử Giám trung đại.",
                        "",
                        new String[]{"vm_vuon"}));
    }

}
