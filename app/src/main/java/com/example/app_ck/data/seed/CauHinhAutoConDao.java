package com.example.app_ck.data.seed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Danh mục 19 điểm Côn Đảo; tách ID nội bộ, số/pixel trên ảnh và GPS WGS84.
 * Tọa độ GPS từ OSM, chưa thay thế khảo sát thực địa. Nội dung có nguồn theo từng điểm.
 */
public final class CauHinhAutoConDao {
    private CauHinhAutoConDao() {}
    public static final class Diem {
        public final int id, soSoDo;
        public final String ten, noiDung, nguonNoiDung, nguon;
        public final float xAnh, yAnh, banKinh;
        public final boolean choPhepAuto;
        public final double lat, lon;
        private Diem(int id, String ten, int soSoDo, float x, float y, double lat, double lon,
                     String nguonGps, String nguonNoiDung, String noiDung) {
            this.id=id; this.ten=ten; this.soSoDo=soSoDo;
            xAnh=x/1366f; yAnh=y/970f;
            this.lat=lat; this.lon=lon; nguon=nguonGps;
            this.nguonNoiDung=nguonNoiDung; this.noiDung=noiDung;
            choPhepAuto=Double.isFinite(lat) && Double.isFinite(lon) && !nguonGps.isEmpty();
            banKinh=35;
        }
        public boolean coMarker() { return soSoDo>0 && Float.isFinite(xAnh) && Float.isFinite(yAnh); }
    }

    // Đối chiếu ảnh 1366×970 và OSM ngày 24/10/2026. Way dùng tâm bounding box.
    public static final List<Diem> CAC_DIEM=Collections.unmodifiableList(java.util.Arrays.asList(
            new Diem(1, "Trại Phú Hải", 5, 780f, 594f, 8.68421825, 106.61003025,
                    "https://www.openstreetmap.org/way/751056578",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-hai-trai-2/",
                    "Trại Phú Hải, còn được biết đến với tên Banh Một và Trại Hai, là một khu giam lâu đời của hệ thống Nhà tù Côn Đảo. Nhiều thế hệ người yêu nước và chiến sĩ cách mạng từng bị giam tại đây. Trong điều kiện khắc nghiệt, người tù vẫn tổ chức học tập, giúp đỡ nhau và đấu tranh để giữ vững tinh thần. Khi tham quan, hãy quan sát các dãy phòng giam, xà lim và không gian lao động khổ sai để hiểu mối liên hệ giữa kiến trúc trại giam với đời sống tù nhân. Trên sơ đồ quy hoạch, di tích mang số năm."),
            new Diem(2, "Trại Phú Sơn", 6, 809f, 573f, 8.68473455, 106.61086745,
                    "https://www.openstreetmap.org/way/751056577",
                    "https://condao.com.vn/vi/news/di-tich-lich-su/diem-tham-quan-di-tich-trai-phu-son-con-dao-75.html",
                    "Trại Phú Sơn, còn gọi là Banh Hai và Trại Ba, được xây dựng từ năm 1908 và hoàn thành năm 1916. Khu trại có các phòng giam tập thể, xà lim và những công trình phụ phục vụ việc quản lý tù nhân. Di tích ghi lại hoạt động học tập, sinh hoạt văn hóa và đấu tranh của nhiều thế hệ tù chính trị. Nhìn vào cách bố trí phòng và sân trại, người tham quan có thể hình dung sự kiểm soát đối với đời sống trong tù. Phú Sơn nằm gần Phú Hải nhưng là một di tích riêng, mang số sáu trên sơ đồ."),
            new Diem(3, "Trại Phú Tường", 25, 924f, 442f, 8.6885971, 106.6142303,
                    "https://www.openstreetmap.org/node/7023329195",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-tuong-trai-4/",
                    "Trại Phú Tường là Trại Bốn, từng được gọi là Banh Ba phụ, được xây dựng năm 1940. Khu trại gồm các dãy phòng giam cùng nhà bếp, nhà kho, bệnh xá và sân vườn. Nơi đây ghi dấu sự giam cầm và những cuộc đấu tranh của tù chính trị. Phú Tường có liên hệ với khu biệt lập Chuồng Cọp Pháp, nhưng hai hạng mục cần được phân biệt khi tham quan: trại mang số hai mươi lăm, còn Chuồng Cọp Pháp mang số hai mươi sáu. Hãy chọn đúng ký hiệu để nghe bài giới thiệu tương ứng."),
            new Diem(4, "Trại Phú Bình – Chuồng Cọp Mỹ", 29, 1114f, 243f, 8.69364895, 106.61934005,
                    "https://www.openstreetmap.org/way/469802902",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-binh-trai-7/",
                    "Trại Phú Bình, hay Trại Bảy, được xây dựng năm 1971 và thường được gọi là Chuồng Cọp Mỹ. Khác với các phòng giam tập thể, khu trại được tổ chức thành nhiều phòng biệt lập nhỏ. Mái thấp và điều kiện sinh hoạt khắc nghiệt khiến người tù chịu nhiều đày đọa. Đây cũng là nơi gắn với cuộc nổi dậy của tù nhân, tiến tới làm chủ Côn Đảo vào ngày một tháng năm năm 1975. Trên sơ đồ quy hoạch, hãy tìm số hai mươi chín. Bài giới thiệu này dành cho toàn khu trại; từng phòng bên trong được khám phá bằng thao tác thủ công."),
            new Diem(5, "Trại Phú An", 28, 1050f, 291f, Double.NaN, Double.NaN,
                    "",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-an-trai-6/",
                    "Trại Phú An, còn gọi là Trại Sáu, thuộc giai đoạn mở rộng hệ thống nhà tù trong những năm 1967 đến 1968. Khu trại gồm hai khu A và B với các dãy phòng giam đối diện nhau cùng khu biệt lập. Nhà bếp, bệnh xá và kho được bố trí phía trước, còn phòng giam nằm sâu phía trong. Tù chính trị tại đây đã tổ chức nhiều cuộc đấu tranh bảo vệ khí tiết và đòi cải thiện đời sống. Di tích mang số hai mươi tám trên sơ đồ. Hãy chọn marker để nghe; ứng dụng chưa dùng GPS để tự phân biệt các khu và phòng của trại."),
            new Diem(6, "Nhà Chúa Đảo", 3, 724f, 645f, 8.68293335, 106.6083764,
                    "https://www.openstreetmap.org/way/751056576",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-nha-chua-dao/",
                    "Nhà Chúa Đảo, còn được gọi là Dinh Chúa Đảo hoặc Dinh Ông Lớn, từng là nơi ở và làm việc của người đứng đầu bộ máy quản lý nhà tù. Công trình gồm nhà chính, nhà phụ và sân vườn, thể hiện một phần cơ cấu cai trị trên đảo. Không gian sinh hoạt tại dinh đối lập với cuộc sống bị giam cầm và lao động khổ sai của tù nhân. Di tích còn gắn với những thời điểm hình thành chính quyền cách mạng ở Côn Đảo. Trên sơ đồ, Nhà Chúa Đảo mang số ba; các gian phòng bên trong được lựa chọn thủ công."),
            new Diem(7, "Bảo tàng Côn Đảo", 0, Float.NaN, Float.NaN, 8.68738105, 106.61377465,
                    "https://www.openstreetmap.org/way/751056607",
                    "https://bvhttdl.gov.vn/Pages/chi-tiet.aspx?url=%2Fchu-tich-nuoc-vo-van-thuong-tham-bao-tang-con-dao-va-di-tich-lich-su-trai-phu-hai-20230720081030742.htm",
                    "Bảo tàng Côn Đảo lưu giữ và giới thiệu tư liệu, hình ảnh, hiện vật về lịch sử, văn hóa và con người trên đảo. Những bộ sưu tập giúp kết nối câu chuyện của các trại giam, hoạt động đấu tranh và sự thay đổi của Côn Đảo qua nhiều thời kỳ. Khi xem một hiện vật, hãy đọc nhãn chú thích để hiểu xuất xứ và bối cảnh sử dụng thay vì chỉ quan sát hình dáng. Bài thuyết minh này giới thiệu chung về bảo tàng. Ảnh sơ đồ quy hoạch đang dùng chưa xác định một ký hiệu riêng cho bảo tàng, nên điểm này được chọn từ danh sách."),
            new Diem(8, "Cầu tàu 914", 1, 763f, 704f, 8.6811899, 106.60920715,
                    "https://www.openstreetmap.org/way/751056510",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-cau-tau-914/",
                    "Cầu tàu 914 nằm phía trước Nhà Chúa Đảo, hướng ra vịnh Côn Sơn. Công trình được khởi công từ năm 1873 và trải qua nhiều lần mở rộng. Tù nhân bị buộc khai thác, vận chuyển đá và xây cầu trong điều kiện khổ sai. Con số chín trăm mười bốn được dùng để tưởng nhớ sự hy sinh của người tù, không nên hiểu là một thống kê đã xác định đầy đủ. Cầu tàu vừa là nơi nhiều người bắt đầu cuộc lưu đày, vừa chứng kiến những đoàn tù được tự do trở về đất liền. Trên sơ đồ, cầu mang số một."),
            new Diem(9, "Trại Phú Thọ", 24, 951f, 396f, 8.6897054, 106.6151959,
                    "https://www.openstreetmap.org/node/7023329193",
                    "https://condao.com.vn/en/news/historical-sites/phu-tho-prison-relic-con-dao-607.html",
                    "Trại Phú Thọ, còn gọi là Banh Ba và Trại Một, được xây dựng năm 1928. Khu trại từng dùng để giam giữ, cách ly tù nhân mới đưa ra đảo hoặc trước khi chuyển sang trại khác. Bố cục ban đầu gồm các dãy giam tập thể, khu biệt lập, nhà bếp và bệnh xá; các giai đoạn sau tiếp tục có thay đổi. Vì tên gọi và số thứ tự trại thay đổi theo thời kỳ, hãy đối chiếu tên di tích thay vì suy ra vị trí từ số trại. Trên ảnh quy hoạch của ứng dụng, Phú Thọ mang số hai mươi bốn."),
            new Diem(10, "Trại Phú Phong", 27, 950f, 435f, 8.6888132, 106.6151637,
                    "https://www.openstreetmap.org/node/7023329194",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-phong-trai-5/",
                    "Trại Phú Phong là Trại Năm, được xây dựng năm 1962. Trại có mười hai phòng giam chia thành ba dãy cùng các hạng mục phụ. Công trình nằm phía trước khu Chuồng Cọp Pháp, vì vậy cần phân biệt hai điểm khi đi theo sơ đồ. Di tích gắn với những cuộc đấu tranh của tù nhân, trong đó có tập thể nữ tù chính trị trong các năm 1966 đến 1968. Không gian phòng giam và cách tổ chức trại giúp người xem hiểu thêm về điều kiện giam giữ. Ký hiệu của Phú Phong trên sơ đồ là số hai mươi bảy."),
            new Diem(11, "Trại Phú Hưng", 30, 1097f, 214f, Double.NaN, Double.NaN,
                    "",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-trai-phu-hung-trai-8/",
                    "Trại Phú Hưng là Trại Tám, được xây dựng từ cuối những năm 1960. Thiết kế gồm hai khu phòng giam đối diện nhau và các xà lim, nhưng một phần công trình còn dang dở khi Hiệp định Paris được ký kết. Những hạng mục hoàn thành và chưa hoàn thành phản ánh quá trình mở rộng hệ thống nhà tù ở giai đoạn cuối. Di tích cũng ghi lại các cuộc đấu tranh của tù nhân nhằm đòi cải thiện đời sống và thực hiện việc trao trả. Trên sơ đồ quy hoạch, Phú Hưng mang số ba mươi. Hiện điểm này được nghe bằng lựa chọn thủ công."),
            new Diem(12, "Trại 9", 31, 1021f, 249f, Double.NaN, Double.NaN,
                    "",
                    "https://www.condao.com.vn/vi/news/tai-nguyen-du-lich/nha-tu-con-dao-2750.html",
                    "Trại Chín là hạng mục xây dựng còn dang dở trong hệ thống Nhà tù Côn Đảo. Di tích giúp nhận biết quá trình mở rộng các khu giam giữ nhưng chưa được hoàn thành. Khi tham quan, hãy phân biệt dấu tích công trình với phòng giam từng được sử dụng; không suy diễn chức năng chỉ từ hình dáng còn lại. Trên sơ đồ, điểm mang số ba mươi mốt."),
            new Diem(13, "Khu điều tra – Phòng điều tra", 4, 812f, 626f, Double.NaN, Double.NaN,
                    "",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-khu-dieu-tra/",
                    "Khu điều tra là một bộ phận trong cơ cấu quản lý nhà tù, gắn với việc hỏi cung và xử lý hồ sơ tù nhân. Đây là không gian khác với các trại giam, cho thấy sự liên kết giữa hoạt động điều tra và việc phân loại, quản lý người bị giam giữ. Khi xem di tích, hãy đọc các chú thích về từng phòng và hiện vật để hiểu chức năng cụ thể. Trên sơ đồ quy hoạch, khu điều tra xét hỏi được đánh số bốn. Các phòng gần nhau được lựa chọn thủ công, không dùng GPS để tự nhận diện."),
            new Diem(14, "Cầu Ma Thiên Lãnh", 21, 350f, 89f, 8.6989322, 106.5965559,
                    "https://www.openstreetmap.org/node/7028421382",
                    "https://www.condao.com.vn/vi/news/tai-nguyen-du-lich/nha-tu-con-dao-2750.html",
                    "Cầu Ma Thiên Lãnh là dấu tích công trình gắn với lao động khổ sai của tù nhân. Cây cầu chưa được hoàn thành, hiện còn các mố cầu giúp nhận biết phần xây dựng dang dở. Điểm dừng này nhắc người tham quan về những công việc cưỡng bức bên ngoài trại giam. Hãy quan sát theo lối tham quan được hướng dẫn. Trên sơ đồ quy hoạch, cầu mang số hai mươi mốt."),
            new Diem(15, "Khu biệt lập Chuồng Bò", 18, 685f, 412f, 8.6892808, 106.60752445,
                    "https://www.openstreetmap.org/way/751056520",
                    "https://condao.com.vn/vi/news/Tin-tuc/khu-di-tich-chuong-bo-con-dao-979.html",
                    "Khu biệt lập Chuồng Bò bắt nguồn từ một sở tù gắn với hoạt động chăn nuôi và tạm giam. Về sau, nơi đây được sử dụng để giam giữ và điều tra tù chính trị trong những điều kiện rất khắc nghiệt. Các hạng mục chăn nuôi và phòng giam nằm gần nhau cho thấy một cơ sở có thể bị thay đổi chức năng qua nhiều giai đoạn. Di tích giúp mở rộng câu chuyện từ các trại giam chính sang hệ thống sở tù. Trên sơ đồ, Chuồng Bò mang số mười tám; các phòng bên trong vẫn được lựa chọn thủ công."),
            new Diem(16, "Sở Lò Vôi", 35, 1188f, 285f, 8.6929849, 106.6226088,
                    "https://www.openstreetmap.org/node/7024795791",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-so-lo-voi/",
                    "Sở Lò Vôi là cơ sở sản xuất vật liệu xây dựng bằng sức lao động cưỡng bức của tù nhân. Người tù phải khai thác nguyên liệu từ biển, vận chuyển rồi đưa vào lò nung để làm vôi phục vụ công trình trên đảo. Chuỗi công việc này cho thấy lao động khổ sai không chỉ diễn ra trong tường trại. Khi xem di tích, hãy liên hệ giữa vị trí gần biển, khu lò và hoạt động xây dựng của hệ thống nhà tù. Trên sơ đồ quy hoạch, Sở Lò Vôi được đánh số ba mươi lăm."),
            new Diem(17, "Nhà Công Quán", 2, 754f, 667f, Double.NaN, Double.NaN,
                    "",
                    "https://condao.com.vn/vi/news/di-tich-lich-su/diem-tham-quan-di-tich-nha-cong-quan-con-dao-74.html",
                    "Nhà Công Quán là ngôi nhà khách được xây dựng từ cuối thế kỷ mười chín, thuộc không gian kiến trúc gần Nhà Chúa Đảo và Cầu tàu 914. Công trình từng phục vụ những người đến đảo làm việc. Nơi đây còn gắn với thời gian lưu trú của nhạc sĩ người Pháp Camille Saint-Saëns vào năm 1895. Câu chuyện về nhà khách giúp nhận biết một phương diện khác của bộ máy trên đảo, bên cạnh các trại giam. Trên sơ đồ, Nhà Công Quán mang số hai. Hãy chọn marker hoặc tên trong danh sách để nghe bài giới thiệu."),
            new Diem(18, "Nghĩa trang Hàng Dương", 32, 978f, 213f, Double.NaN, Double.NaN,
                    "",
                    "https://www.condao.com.vn/vi/news/tai-nguyen-du-lich/nha-tu-con-dao-2750.html",
                    "Nghĩa trang Hàng Dương là nơi tưởng niệm các chiến sĩ cách mạng, người yêu nước và những người đã mất trong hệ thống Nhà tù Côn Đảo. Đây là không gian để tri ân và tìm hiểu ký ức của các thế hệ tù nhân. Khi thăm viếng, hãy giữ sự trang nghiêm và đọc thông tin tại từng khu mộ. Trên sơ đồ quy hoạch, khu nghĩa trang mang số ba mươi hai."),
            new Diem(19, "Chuồng Cọp Pháp", 26, 934f, 421f, Double.NaN, Double.NaN,
                    "",
                    "https://trungtambaotonditichquocgiacondao.vn/di-tich-chuong-cop-phap/",
                    "Chuồng Cọp Pháp là khu biệt lập được xây dựng năm 1940, có các phòng giam nhỏ với song sắt phía trên và lối kiểm soát của cai ngục. Khu này có những lối thông với các trại lân cận, nhưng là một hạng mục cần nghe bài giới thiệu riêng. Di tích phản ánh điều kiện giam cầm hà khắc và sự đấu tranh bền bỉ của tù nhân, trong đó có nữ tù chính trị. Trên sơ đồ, Chuồng Cọp Pháp mang số hai mươi sáu, nằm gần Trại Phú Tường số hai mươi lăm. Hai điểm ở sát nhau nên phần này luôn được chọn nghe thủ công.")
    ));

    public static Diem tim(int id) {
        for (Diem d:CAC_DIEM) if (d.id==id) return d;
        return null;
    }
    public static List<Diem> cacDiemAuto() {
        List<Diem> ds=new ArrayList<>();
        for (Diem d:CAC_DIEM) if (d.choPhepAuto) ds.add(d);
        return ds;
    }
}
