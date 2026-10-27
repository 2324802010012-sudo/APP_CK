# APP_CK – Hỗ trợ tham quan bằng bản đồ tương tác và thuyết minh

Ứng dụng Android viết bằng Java và Android Views/XML, hỗ trợ tra cứu địa điểm, bản đồ OpenStreetMap, sơ đồ tham quan, thuyết minh tiếng Việt, tài khoản, yêu thích và lịch sử nghe.

**Đối chiếu ngày 27/10/2026** với mã nguồn hiện có trong workspace, kể cả thay đổi chưa commit, và tệp **Nhom15_BaoCao .docx** do người dùng cung cấp tại thư mục tải xuống. Tệp Word được đọc trực tiếp trong lần rà soát này; không nằm trong repository. Các yêu cầu trong Word được dùng làm căn cứ đối chiếu, không phải bằng chứng chức năng đã được triển khai.

Hiện có **5 địa điểm chính, 83 điểm thành phần, 82 marker trên sơ đồ, 18 Activity khai báo trong manifest và SQLite phiên bản 14**. Ứng dụng có luồng người dùng và AUTO chọn lọc; **chưa có quản trị UC08, vai trò USER/ADMIN hoặc khóa tài khoản**. 

## Mục lục

1. [Phạm vi và công nghệ](#1-phạm-vi-và-công-nghệ)
2. [Cài đặt và chạy](#2-cài-đặt-và-chạy)
3. [Kiến trúc và các Activity](#3-kiến-trúc-và-các-activity)
4. [Dữ liệu địa điểm và nguồn nội dung](#4-dữ-liệu-địa-điểm-và-nguồn-nội-dung)
5. [Trang chủ, tìm kiếm và điều hướng](#5-trang-chủ-tìm-kiếm-và-điều-hướng)
6. [Bản đồ và chi tiết địa điểm](#6-bản-đồ-và-chi-tiết-địa-điểm)
7. [Tham quan tương tác và thuyết minh](#7-tham-quan-tương-tác-và-thuyết-minh)
8. [GPS thật, AUTO và Demo](#8-gps-thật-auto-và-demo)
9. [Tài khoản, yêu thích và lịch sử](#9-tài-khoản-yêu-thích-và-lịch-sử)
10. [SQLite và nâng cấp dữ liệu](#10-sqlite-và-nâng-cấp-dữ-liệu)
11. [Đối chiếu use case với báo cáo Word](#11-đối-chiếu-use-case-với-báo-cáo-word)
12. [Lỗi, hạn chế và phần chưa hoàn thành](#12-lỗi-hạn-chế-và-phần-chưa-hoàn-thành)
13. [Thứ tự ưu tiên hoàn thiện](#13-thứ-tự-ưu-tiên-hoàn-thiện)
14. [Bản đồ mã nguồn](#14-bản-đồ-mã-nguồn)
15. [Kiểm thử và bằng chứng](#15-kiểm-thử-và-bằng-chứng)
16. [Kịch bản nghiệm thu và cập nhật tài liệu](#16-kịch-bản-nghiệm-thu-và-cập-nhật-tài-liệu)

## 1. Phạm vi và công nghệ

| Thành phần | Cấu hình trong repository |
| --- | --- |
| Tên / phiên bản | APP_CK / 1.0, versionCode 1 |
| Namespace / applicationId | com.example.app_ck |
| Giao diện | Java, Android Views/XML; một số thẻ và sơ đồ dựng bằng Java/Canvas |
| Android SDK | minSdk 24, targetSdk 36, compileSdk 36.1 |
| Java của mã nguồn | Source/target compatibility 11 |
| Gradle / Android Gradle Plugin | 9.3.1 / 9.1.1 |
| AndroidX | AppCompat 1.8.0, Activity 1.13.0, ConstraintLayout 2.2.2 |
| Material Components | 1.14.0 |
| Bản đồ | osmdroid 6.1.20, tile OpenStreetMap |
| Giọng đọc | Android TextToSpeech tiếng Việt; khung nghe tổng hợp WAV tạm và phát bằng MediaPlayer |
| Dữ liệu | SQLite, SharedPreferences cho phiên; dữ liệu khởi tạo và nội dung khai báo bằng Java |
| Kiểm thử | JUnit 4.13.2, AndroidX JUnit 1.3.0, Espresso 3.7.0 |

Căn cứ: [app/build.gradle.kts](app/build.gradle.kts), [libs.versions.toml](gradle/libs.versions.toml), [Gradle Wrapper](gradle/wrapper/gradle-wrapper.properties).

Phạm vi ứng dụng là dữ liệu cục bộ trên thiết bị. Chưa có backend, đồng bộ tài khoản giữa các máy, quản trị, điều hướng đường đi bên trong app. Báo cáo Word yêu cầu quản trị cục bộ và phân quyền; không bắt buộc backend, AI, Google Maps SDK hoặc gửi email khôi phục.


## 3. Kiến trúc và các Activity

Dự án có một module Android app. Activity trực tiếp xử lý phần lớn giao diện, SQLite và sự kiện; chưa tách theo Repository/ViewModel. Logic nhận diện vị trí, cấu hình AUTO, khung nghe và nội dung đã có lớp riêng.

~~~mermaid
flowchart TD
    W[Welcome: bản đồ tổng quan] --> M[Trang chủ]
    W --> DN[Đăng nhập]
    DN --> M
    M --> BD[Bản đồ OSM]
    M --> CT[Chi tiết địa điểm]
    M --> CN[Cá nhân]
    M --> YT[Yêu thích]
    M --> LS[Lịch sử nghe]
    M --> GY[Gợi ý]
    BD --> CT
    YT --> CT
    GY --> CT
    CT --> T[Sơ đồ của đúng địa điểm]
    CT --> K[Khung nghe tổng quan]
    T --> P[Chọn điểm thành phần]
    P --> K2[Khung nghe điểm thành phần]
    T --> A[AUTO GPS hoặc Demo ở tour hỗ trợ]
    LS --> CT
    LS --> T
~~~

18 Activity trong [AndroidManifest.xml](app/src/main/AndroidManifest.xml):

| Activity | Vai trò / layout chính |
| --- | --- |
| WelcomeActivity | Màn hình khởi chạy, bản đồ tổng quan; activity_welcome.xml |
| MainActivity | Trang chủ, tìm kiếm, danh mục, Gần tôi, hướng dẫn vị trí; activity_main.xml |
| KhamPhaActivity | Danh sách có tìm/lọc; chế độ Gợi ý; activity_kham_pha.xml |
| BanDoActivity | Bản đồ địa điểm; activity_ban_do.xml |
| ChiTietDiaDiemActivity | Chi tiết địa điểm lớn, khung nghe tổng quan, vào tour; activity_chi_tiet_dia_diem.xml |
| ThamQuanActivity | Dinh Độc Lập; activity_tham_quan.xml |
| BaoTangThamQuanActivity | Bảo tàng Chứng tích Chiến tranh; activity_tham_quan.xml |
| DaiNoiHueThamQuanActivity | Đại Nội Huế; activity_tham_quan.xml |
| ConDaoThamQuanActivity | Côn Đảo; activity_tham_quan.xml |
| VanMieuThamQuanActivity | Văn Miếu; activity_tham_quan.xml |
| DangNhapActivity | Xác thực cục bộ; activity_dang_nhap.xml |
| DangKyActivity | Tạo tài khoản và cấp mã khôi phục; activity_dang_ky.xml |
| QuenMatKhauActivity | Đặt lại mật khẩu bằng mã; activity_quen_mat_khau.xml |
| YeuThichActivity | Bộ sưu tập riêng; activity_yeu_thich.xml |
| CaNhanActivity | Hồ sơ, tài khoản và liên kết chức năng; activity_ca_nhan.xml |
| SuaHoSoActivity | Sửa họ tên/email; activity_bieu_mau_tai_khoan.xml |
| DoiMatKhauActivity | Đổi mật khẩu; activity_bieu_mau_tai_khoan.xml |
| LichSuActivity | Các lượt nghe riêng theo tài khoản; activity_lich_su.xml |

BaseActivity và BieuMauTaiKhoanActivity là lớp dùng chung, không phải Activity được khai báo độc lập trong manifest. WelcomeActivity là điểm vào exported; các Activity còn lại không exported.

Báo cáo liệt kê 27 màn hình/giao diện chức năng, bao gồm khung nghe, phần thông tin tích hợp và các màn hình quản trị dự kiến. Không thể lấy 18/27 làm tỷ lệ hoàn thành: một Activity có thể chứa nhiều giao diện chức năng.

## 4. Dữ liệu địa điểm và nguồn nội dung

ID địa điểm thống nhất giữa trang chủ, bản đồ, chi tiết, tour, yêu thích và lịch sử:

| ID | Địa điểm | Điểm thành phần | Marker vẽ trên sơ đồ | AUTO điểm thành phần |
| ---: | --- | ---: | ---: | --- |
| 0 | Dinh Độc Lập | 25 | 25 | Chưa bật; chọn phòng/tầng thủ công |
| 1 | Bảo tàng Chứng tích Chiến tranh | 13 | 13 | Chưa bật; chọn điểm/tầng thủ công |
| 2 | Đại Nội Huế | 10 | 10 | 7 điểm |
| 3 | Hệ thống Nhà tù Côn Đảo | 19 | 18 | 12 điểm |
| 4 | Văn Miếu – Quốc Tử Giám | 16 | 16 | 3 điểm |
| | **Tổng** | **83** | **82** | **22 điểm được cấu hình AUTO** |

Bảo tàng Côn Đảo là điểm thành phần ID 7 của địa điểm ID 3, có nội dung và chọn được trong danh sách nhưng chưa có ký hiệu riêng trên ảnh quy hoạch. Vì vậy có 83 điểm và 82 marker, không phải mất một bài thuyết minh.

Tọa độ WGS84 mặc định được đồng bộ bởi KhoToaDoThamQuan:

| ID | Vĩ độ | Kinh độ |
| ---: | ---: | ---: |
| 0 | 10.77702 | 106.69540 |
| 1 | 10.779475 | 106.692132 |
| 2 | 16.4689726 | 107.5781266 |
| 3 | 8.68907 | 106.61484 |
| 4 | 21.02861 | 105.83556 |

Đây là mốc đích tham chiếu trong dữ liệu, không phải tọa độ hiện tại của người dùng hoặc kết quả đo GPS tại thực địa. Một số giá trị seed/dự phòng cũ còn khác giá trị đồng bộ; màn hình ưu tiên đọc SQLite.

### Nội dung và nguồn

[DuLieuNoiDungThamQuan.java](app/src/main/java/com/example/app_ck/DuLieuNoiDungThamQuan.java) chứa 5 bài tổng quan, 83 bài thành phần và 63 mục nguồn. Phiên bản nội dung hiện tại là 1, ngày đối chiếu lưu trong dữ liệu là **24/10/2026**.

Mỗi bài có giới thiệu, điểm đáng chú ý, gợi ý tham quan, liên kết nguồn và ghi chú phạm vi xác minh. Nội dung được biên soạn lại từ các cơ quan quản lý di tích, bảo tàng và cơ quan nhà nước; không phải bản thuyết minh được các đơn vị này chứng nhận.

Danh mục từng điểm và nguồn: [docs/nguon_noi_dung.md](docs/nguon_noi_dung.md). Đây là bảng tham chiếu chi tiết cho toàn bộ 83 điểm; tránh duy trì thêm một danh sách tên/nguồn trùng lặp trong README.

KhoNoiDungThamQuan đưa nội dung Java vào SQLite theo phiên bản; NguonThongTin mở danh sách nguồn từ chi tiết, tour và khung nghe. URL không đưa vào giọng đọc. Một số phòng/nhãn điều hướng chỉ có nguồn tổng quan, đã ghi rõ giới hạn; có bài không đồng nghĩa đã có hồ sơ chuyên biệt cho từng hiện vật/phòng.

Nội dung ngoại tuyến không tự cập nhật từ website. Giá vé, giờ mở cửa, lịch diễn, tình trạng mở phòng và địa chỉ trang nguồn cần kiểm tra lại khi sử dụng. Lần viết README này đối chiếu tệp và mã nguồn, không tái xác minh từng website hoặc khảo sát GPS thực địa.

## 5. Trang chủ, tìm kiếm và điều hướng

Welcome hiển thị bản đồ OSM với 5 marker, kéo/chụm hai ngón và nút +/−. Mở lần đầu căn khung các điểm đến; xoay/tái tạo Welcome giữ tâm và zoom. Bản đồ Welcome không cần vị trí của người dùng.

**Bắt đầu khám phá** mở MainActivity kèm yêu cầu bật hướng dẫn vị trí; việc xin quyền diễn ra ở trang chủ. Liên kết Đăng nhập mở luồng tài khoản, hoặc vào trang chủ nếu đã có phiên.

Trang chủ có:

- Ô tìm kiếm không phân biệt dấu theo tên/địa chỉ, kết quả chọn để mở chi tiết.
- Các thẻ địa điểm, danh mục, danh sách gợi ý và Gần tôi.
- Bốn chức năng nhanh: **Thuyết minh, Bản đồ, Yêu thích, Lịch sử**.
- Khối hướng dẫn vị trí với GPS thật, Demo và Dừng.

| Nút ở thanh dưới | Hành vi thực tế |
| --- | --- |
| Khám phá | Cuộn lên ô tìm kiếm ở trang chủ; từ tab khác trở về MainActivity rồi cuộn lên, giữ từ khóa của MainActivity đang được tái sử dụng |
| Yêu thích | Mở bộ sưu tập; khách được mời đăng nhập |
| Trang chủ | Trở về MainActivity |
| Cá nhân | Mở hồ sơ và thao tác tài khoản |
| Gợi ý | Mở KhamPhaActivity ở chế độ Gợi ý |

KhamPhaActivity vẫn tồn tại cho danh sách và Gợi ý. Nút Khám phá ở thanh dưới và nút khám phá khi lịch sử trống cùng quay về ô tìm kiếm trên trang chủ.

Tìm kiếm trong KhamPhaActivity xét tên/địa chỉ/mô tả, kết hợp một danh mục được chọn. Tìm kiếm trang chủ và bản đồ chưa dùng chung toàn bộ tiêu chí.

GoiYDiaDiem loại các điểm đã lưu, ưu tiên điểm chưa lưu có chủ đề/danh mục giao với các điểm yêu thích; khi bằng điểm sắp theo ID. Đây là thuật toán xác định bằng quy tắc, không phải AI hay gợi ý từ lịch sử nghe.

**Gần tôi** lấy vị trí thiết bị, tính khoảng cách địa lý và sắp xếp 5 địa điểm gần đến xa. Đây là khoảng cách đường chim bay, không phải chiều dài đường đi; hiện chưa có giới hạn bán kính tìm kiếm.

## 6. Bản đồ và chi tiết địa điểm

BanDoActivity dùng OpenStreetMap qua osmdroid, có 5 marker, tìm tên, chọn nhanh địa điểm, hiển thị thẻ, mở chi tiết và chỉ đường. Mức zoom bản đồ là 4–19; marker và thẻ phải cùng ID địa điểm. Khi được cấp quyền, lớp MyLocationNewOverlay hỗ trợ vị trí của tôi.

Chỉ đường sử dụng URL Google Maps và ứng dụng/trình duyệt ngoài. App chưa tự tính tuyến, cung cấp chỉ dẫn từng chặng hoặc tải sẵn bản đồ toàn khu vực để dùng offline. Tile đã cache có thể tái sử dụng; không đồng nghĩa có gói bản đồ ngoại tuyến hoàn chỉnh.

Trang chi tiết hiển thị ảnh, tên, địa chỉ, danh mục, mô tả mở rộng/thu gọn, nguồn chính thức, yêu thích và các điểm thành phần. **Bắt đầu tham quan** mở đúng một trong 5 tour theo ID.

Hai nút **Thuyết minh** và **Nghe thuyết minh** cùng mở khung nghe tổng quan dùng chung với trang tham quan. Khung có ảnh, toàn bộ bài, nguồn, thời gian, tua, phát/tạm dừng/tiếp tục/dừng/nghe lại. Mở chi tiết thông thường không tự phát; mở từ lời mời vị trí đã xác nhận sẽ yêu cầu phát một lần.

Trong layout hiện có, bốn tiện ích bản đồ/chỉ đường/thuyết minh/yêu thích đang bố trí **hai hàng, mỗi hàng hai nút**. README mô tả tệp thực tế; yêu cầu bố trí bốn nút một hàng chưa hiện diện trong layout hiện tại.

Mỗi địa điểm hiện có ảnh tổng quan; chưa có bộ sưu tập ảnh/phim tư liệu đầy đủ. Khung nghe điểm thành phần dùng ảnh tổng quan địa điểm, chưa có ảnh chuyên biệt cho mọi điểm.

Thẻ **Nghe giới thiệu** của điểm thành phần ở chi tiết truyền đúng idMarker và mở tour tại điểm đó. Thẻ này chưa phát ngay; người dùng tiếp tục bấm mở/nghe thuyết minh trong tour.

## 7. Tham quan tương tác và thuyết minh

### Sơ đồ

- Dinh Độc Lập có 6 khu vực/tầng, lần lượt 4, 6, 4, 2, 4, 5 điểm; tổng 25.
- Bảo tàng có 3 mức: trệt/ngoài trời, lầu 1, lầu 2; danh sách thay đổi theo tầng.
- Huế, Côn Đảo và Văn Miếu dùng sơ đồ quần thể.
- Cả 5 tour hỗ trợ chọn điểm qua số trên ảnh/danh sách, Trước/Tiếp, kéo và thu phóng sơ đồ.
- Bốn tour dùng SoDoThuPhongLayout; Huế xử lý sơ đồ/zoom riêng. Mức phóng sơ đồ ảnh 1–5 khác mức zoom bản đồ địa lý.
- Tọa độ x/y trên ảnh chỉ phục vụ vẽ/bắt chạm marker. Tọa độ này tách biệt với vĩ độ/kinh độ GPS.

Chọn tầng là thao tác thủ công. App không dùng GPS để tự phân biệt các phòng sát nhau hoặc cùng vị trí mặt bằng nhưng khác tầng.

### Nghe thủ công

1. Chọn điểm thành phần trên sơ đồ hoặc trong danh sách.
2. App cập nhật tên, nội dung, marker và mở khung nghe; chọn điểm chưa tự bật loa.
3. Bấm nghe; TTS tổng hợp các đoạn văn ra WAV tạm.
4. MediaPlayer phát âm thanh, lấy thời lượng thật để hiển thị và tua.
5. Người dùng tạm dừng, tiếp tục, tua, dừng về đầu hoặc nghe lại.
6. Đóng khung/rời màn hình dừng âm thanh, giải phóng audio focus và dọn tệp tạm.

Khung chia bài dài theo giới hạn TTS, có trạng thái chuẩn bị/lỗi và giới hạn chờ chuẩn bị. Không có thanh tua giả theo số ký tự. Phụ thuộc giọng Việt và khả năng tổng hợp của engine trên từng máy.

### Chọn điểm khác khi AUTO đang phát

Áp dụng ở Huế, Côn Đảo và Văn Miếu cho marker, danh sách và Trước/Tiếp:

- Chạm đúng điểm đang phát: giữ nguyên bài.
- Chọn điểm khác: hiện **Chuyển điểm tham quan?**, chưa dừng âm thanh hoặc đổi điểm.
- **Tiếp tục nghe**, Back hoặc đóng hộp thoại: giữ bài và AUTO.
- **Chuyển điểm**: mới dừng AUTO/âm thanh cũ, cập nhật điểm và mở khung nghe thủ công của điểm đã chọn.
- Không mở chồng nhiều hộp xác nhận. Rời màn hình hủy hộp thoại; không giữ lệnh chuyển để chạy khi quay lại.

Khi không có bài AUTO đang phát, chọn điểm chuyển như trước. Nếu bài kết thúc tự nhiên trong lúc chờ xác nhận, callback AUTO vẫn được xử lý; hộp thoại không phải cơ chế tạm dừng âm thanh.

### Các bộ phát hiện tại

| Ngữ cảnh | Cách phát / điều khiển |
| --- | --- |
| Giới thiệu từ nút Thuyết minh ở trang chủ | TTS trực tiếp; nghe/dừng |
| Giới thiệu ở trang chi tiết, thủ công hoặc sau xác nhận vị trí | Khung nghe chung: WAV → MediaPlayer, tua/tạm dừng/tiếp tục/dừng/nghe lại |
| Điểm thành phần chọn thủ công | Khung nghe chung; không cần GPS |
| Điểm thành phần trong AUTO GPS/Demo | TTS trực tiếp, chờ callback đọc xong; không tự mở khung thủ công |

Điều khiển chưa đồng nhất ở tất cả bộ phát: AUTO điểm thành phần và giới thiệu trang chủ chưa có tua/tạm dừng như khung nghe. Chưa có chỉnh tốc độ đọc trên UI hoặc dịch vụ phát âm thanh nền.

## 8. GPS thật, AUTO và Demo

### 8.1. Địa điểm chính ở trang chủ

ThamQuanTuDong đăng ký vị trí GPS/network của thiết bị. Android 11 trở lên có thêm yêu cầu vị trí hiện tại; Android 7–10 dùng cập nhật vị trí mới. Màn hình hiển thị tọa độ, nguồn đo, độ mới, sai số, khoảng cách đến các điểm và địa điểm gần nhất.

Vị trí dùng nhận diện phải hợp lệ, không phải mock, có sai số tối đa 25 m, không cũ quá 15 giây và ổn định khoảng 2,5 giây. Điều kiện vào vùng xét cả khoảng cách và sai số; đứng cách mốc đúng 100 m chưa chắc đủ điều kiện kích hoạt.

Luồng:

1. Người dùng bật GPS thật; app kiểm tra quyền và dịch vụ vị trí.
2. Xét 5 địa điểm chính, chọn điểm gần nhất đủ điều kiện trong vùng kích hoạt 100 m.
3. Hiện lời mời **Bắt đầu/Bỏ qua**.
4. Chưa đồng ý thì không phát. Đồng ý mở đúng chi tiết và khung nghe tổng quan.
5. Cờ yêu cầu phát chỉ dùng một lần; xoay/quay lại không tự phát lại.
6. Mỗi địa điểm đã xử lý lời mời không bị hỏi liên tục trong cùng lượt theo dõi.
7. Dừng/rời trang chủ hủy theo dõi, yêu cầu vị trí còn chờ và lời mời.

Bán kính 100 m hiện là hằng dùng chung trong LoiMoiThuyetMinh, chưa là trường cấu hình riêng của từng địa điểm trong bảng dia_diem.

Khối GPS có thể hiển thị khoảng cách từ vị trí thật mới ngay cả khi độ chính xác chưa đủ cho AUTO. **Có khoảng cách không có nghĩa đã đủ điều kiện tự phát.** Khi chưa có mẫu thật hợp lệ, app chờ dữ liệu, không thay bằng tọa độ demo.

### 8.2. AUTO điểm thành phần trong tour

Chỉ theo dõi các điểm được cấu hình cho phép AUTO **thuộc tour đang mở**. Có tọa độ không tự đồng nghĩa được bật AUTO.

| Tour | Mốc được phép AUTO | Điểm còn lại |
| --- | --- | --- |
| Đại Nội Huế | ID 1, 2, 3, 4, 7, 8, 9: Ngọ Môn, Thái Hòa, Cần Chánh, Kiến Trung, Cơ Hạ, Diên Thọ, Trường Sanh; bán kính 35 m | Duyệt Thị Đường, Thái Bình Lâu, Hậu hồ: thủ công |
| Văn Miếu | ID 4, 6, 11: Cổng Văn Miếu, Khuê Văn Các, Cổng Thái Học; bán kính 15 m | 13 điểm thủ công |
| Côn Đảo | ID 1, 2, 3, 4, 6, 7, 8, 9, 10, 14, 15, 16 trong CauHinhAutoConDao; bán kính 35 m | 7 điểm thủ công; không lấy GPS để đoán các phòng |
| Dinh Độc Lập / Bảo tàng | Không bật AUTO thành phần | Toàn bộ nghe thủ công |

Bộ lọc kiểm tra tuổi mẫu, sai số, thời gian ổn định, bán kính và mẫu trùng. Nếu vùng sai số có thể khớp nhiều mốc, không tự đoán và hướng dẫn chọn trên sơ đồ. Điểm đã tự phát không lặp trong cùng lượt.

Văn Miếu có bán kính 15 m nên thực tế cần GPS tốt hơn ngưỡng chung 25 m: điều kiện khoảng cách cộng sai số phải nằm trong vùng. Cấu hình tham chiếu không thay thế thử nghiệm thực địa.

HuongDanTour điều phối Huế/Văn Miếu; HuongDanConDao điều phối Côn Đảo. Chuyển thủ công, tắt AUTO hoặc rời tour sẽ dừng bộ nhận diện và bài cũ, riêng trường hợp đang phát và chọn điểm khác phải qua xác nhận ở mục 7.

### 8.3. Demo

| Nơi chạy | Hành vi |
| --- | --- |
| Trang chủ | Chọn một trong 5 nơi; mô phỏng khoảng cách 500 → 300 → 150 → 90 m, chờ ổn định rồi hỏi Bắt đầu/Bỏ qua |
| Huế / Văn Miếu / Côn Đảo | Đi qua toàn bộ 10/16/19 điểm; điểm AUTO tự phát, điểm thủ công hỏi Nghe/Bỏ qua; chờ đọc xong rồi chuyển tiếp |

Demo không cần GPS/quyền vị trí và không ghi tọa độ mô phỏng vào dữ liệu GPS thật. Có thể ghi một lượt lịch sử chế độ Demo khi người dùng đã đăng nhập và âm thanh bắt đầu.

GPS thật không tự chạy toàn tuyến như Demo; chỉ phản ứng với vị trí thực của thiết bị.

## 9. Tài khoản, yêu thích và lịch sử

### Tài khoản

Đăng ký bằng tên đăng nhập và mật khẩu, kiểm tra dữ liệu/xác nhận, chống trùng tên và cấp mã khôi phục. Đăng nhập xác thực SQLite, lưu phiên trong SharedPreferences. Đăng xuất xóa phiên, giữ dữ liệu cá nhân.

Mật khẩu dùng PBKDF2WithHmacSHA1, 120.000 vòng, kết quả 256 bit và salt ngẫu nhiên 16 byte; mã khôi phục cũng được băm. Đây là xác thực cục bộ; không có tài khoản mẫu ADMIN hoặc tài khoản quản trị có quyền riêng.

Quên mật khẩu dùng **tên đăng nhập + mã khôi phục**, sau đó đặt lại mật khẩu và cấp mã mới. Đổi mật khẩu khi đã đăng nhập cần mật khẩu hiện tại. Email trong hồ sơ là thông tin liên hệ tùy chọn, chưa xác minh, không dùng để đăng nhập hoặc khôi phục.

Báo cáo Word được cung cấp lần này cũng quy định khôi phục bằng mã, **không yêu cầu email xác thực**. Hạn chế tài khoản so với Word là thiếu vai trò/trạng thái khóa/ngày tạo, không phải thiếu đăng nhập email.

### Yêu thích

- Chỉ lưu/xem/bỏ lưu theo tài khoản đang đăng nhập; khóa chính ghép chống trùng.
- Khách chọn lưu ở chi tiết được mời đăng nhập; đăng nhập thành công tiếp tục lưu địa điểm vừa chọn.
- Danh sách mở chi tiết đúng ID; bỏ lưu trong danh sách có xác nhận.
- Trạng thái trống hiển thị trực tiếp trên nền, căn giữa vùng nội dung; đã bỏ khung trắng, trái tim trang trí và nút Khám phá địa điểm.
- Nút Thử lại chỉ xuất hiện khi tải danh sách gặp lỗi.
- Đăng xuất không xóa yêu thích.

### Lịch sử nghe

Mở từ nút Lịch sử trên trang chủ hoặc mục **Lịch sử nghe** trong trang Cá nhân khi đã đăng nhập. Hai lối vào dùng cùng LichSuActivity và dữ liệu theo tài khoản. Khách mở từ trang chủ được mời đăng nhập; nếu hủy thì xem trạng thái dành cho khách. Nút khám phá ở lịch sử trống quay về ô tìm kiếm trang chủ.

Lịch sử lưu khi âm thanh **thực sự bắt đầu**, không phải khi mở trang, chọn marker hoặc bắt đầu tổng hợp. Khách không được ghi lịch sử rồi gán sang tài khoản đăng nhập sau đó.

Mỗi lượt có tài khoản, địa điểm lớn, điểm thành phần nếu có, tên chụp tại thời điểm ghi, thời gian, chế độ và mã lượt. Ghi một lần cho cùng lượt; tạm dừng/tiếp tục/tua không sinh lượt mới. Dừng/nghe lại sau đó có thể tạo lượt mới.

Danh sách mới nhất trước, có rỗng/lỗi/thử lại. Chạm lượt mở đúng trang chi tiết/tour/điểm, không tự phát lại. Xóa từng lượt hoặc tất cả có xác nhận, giới hạn theo tài khoản; yêu thích giữ nguyên. Chưa có phân trang hoặc lọc lịch sử.

| Mã trong ứng dụng | Ý nghĩa | Quan hệ với mã trong Word |
| --- | --- | --- |
| MANUAL | Nghe thủ công | MANUAL |
| AUTO_MAIN | Phát sau xác nhận vị trí ở địa điểm chính, diem_id NULL | AUTO_MAIN |
| AUTO_SUB | Tự phát theo GPS trong tour, diem_id là điểm thành phần | AUTO_SUB |
| DEMO | Phát trong luồng mô phỏng | Word chưa định nghĩa mã tương ứng |

SQLite v14 lưu đúng ba mã trong Word và thêm DEMO cho mô phỏng. Migration từ v13 chuyển THU_CONG → MANUAL, AUTO_GPS → AUTO_MAIN nếu là tổng quan hoặc AUTO_SUB nếu có điểm thành phần; giữ DEMO, ID, thời gian, tên đã lưu và mã lượt. Đầu vào cũ vẫn được chuẩn hóa trước ghi. Không gộp Demo vào lịch sử GPS thật; từ chối AUTO_MAIN có điểm thành phần hoặc AUTO_SUB không có điểm thành phần.

Lịch sử là lượt đã bắt đầu nghe, không chứng minh đã nghe hết bài, hoàn thành tour hoặc đã đứng tại địa điểm.

**Kiểm chứng đồng bộ lịch sử ngày 27/10/2026:** build debug và APK kiểm thử thành công; 37 unit test và 26 kiểm thử thiết bị trên emulator Android 14 đều đạt. Nhóm thiết bị gồm 4 ca KhoLichSuTest, 10 ca TaiKhoanDbMigrationTest và 12 ca LichSuFlowTest: giữ dữ liệu khi nâng v13→v14, lối vào từ Trang chủ/Cá nhân, nhãn chế độ, mở lại đúng điểm, xóa có xác nhận, cách ly tài khoản, chống ghi trùng và phát TTS/MediaPlayer thật. Chế độ GPS/Demo trong các ca nghe được đặt để kiểm tra luồng ghi, không thay thế khảo sát vị trí thực địa. Log cục bộ: build/hoan_thien_lich_su_build.log và build/hoan_thien_lich_su_thiet_bi.log. Chưa chạy lại toàn bộ các nhóm kiểm thử khác.

## 10. SQLite và nâng cấp dữ liệu

Helper chính: [TaiKhoanDb.java](app/src/main/java/com/example/app_ck/TaiKhoanDb.java). Tệp du_lich_tai_khoan.db, phiên bản **14**.

| Bảng thực tế | Nội dung |
| --- | --- |
| tai_khoan | id, tên đăng nhập, salt, băm mật khẩu/mã, họ tên và email |
| yeu_thich | Liên kết tài khoản–địa điểm, khóa chính ghép; chưa có thời gian thêm |
| dia_diem | 5 địa điểm, tên/địa chỉ/mô tả/danh mục/ảnh/WGS84 |
| khu_vuc | 6 khu vực của Dinh |
| diem_tham_quan | 25 điểm Dinh, liên kết khu vực, tọa độ ảnh |
| thuyet_minh | Bài các điểm Dinh |
| gps_diem_tham_quan | Dữ liệu GPS marker từ luồng cũ; không điều khiển AUTO hiện tại |
| phien_ban_noi_dung | Mốc đồng bộ nội dung Java; khóa -1 cho tổng quan, 0–4 cho tour |
| lich_su_nghe | Lượt nghe, tên chụp, thời gian, chế độ; UNIQUE(tài khoản, mã lượt) và chỉ mục thời gian |

Các bảng tour khác được tạo khi mở màn hình tương ứng: bao_tang_marker, dai_noi_hue_diem, dai_noi_hue_toa_do, con_dao_diem, con_dao_toa_do, van_mieu_diem, van_mieu_toa_do. Database Inspector có thể chưa thấy bảng tour nếu chưa mở tour đó.

Cấu hình WGS84/bán kính/cờ AUTO thành phần nằm trong các lớp cấu hình Java. Riêng Côn Đảo, constructor hiện suy ra cờ cho phép AUTO từ tọa độ hữu hạn và nguồn GPS không rỗng; chưa có trường bật/tắt độc lập do quản trị thay đổi. Tọa độ ảnh được lưu riêng. Không dùng chỉnh marker ảnh để suy ra GPS.

### Nâng cấp và đồng bộ

| Mốc | Xử lý |
| --- | --- |
| v2–v6 | Mã khôi phục, dữ liệu địa điểm, khu vực/điểm, thuyết minh, danh mục |
| v7–v9 | Bổ sung/dọn dữ liệu địa điểm cũ; có xóa dữ liệu Làng Văn hóa và liên kết của địa điểm đã bỏ |
| v10 | Dùng ID 2 cho Đại Nội Huế |
| v11 | Đồng bộ WGS84 địa điểm và thêm GPS marker |
| v12 | Thêm ho_ten/email, kiểm tra cột trước khi thêm |
| v13 | Thêm lịch sử và chỉ mục; nâng từ v12 không xóa tài khoản, yêu thích hoặc tọa độ tour |
| v14 | Chuyển lịch sử cũ sang MANUAL/AUTO_MAIN/AUTO_SUB/DEMO, giữ bản ghi và chỉ mục; kiểm tra chế độ đúng cấp địa điểm |

KhoNoiDungThamQuan đồng bộ bài theo PHIEN_BAN trong giao dịch, chỉ đánh dấu khi thành công; không ghi đè tài khoản/yêu thích/tọa độ để nhận bài mới. Khi sửa nội dung Java cần tăng phiên bản nội dung. Không yêu cầu xóa dữ liệu app để cập nhật bài.

Không suy rộng kết quả nâng cấp v12→v13 thành cam kết mọi phiên bản cũ đều giữ tất cả dữ liệu: các migration trước đây có chủ đích xóa địa điểm ngừng sử dụng.

### Khác biệt với mô hình 5 bảng trong Word

| Thiết kế Word | Hiện thực / khoảng thiếu |
| --- | --- |
| tblTaiKhoan | tai_khoan; thiếu vai_tro, trang_thai và ngay_tao; có thêm email |
| tblDiemDuLich | dia_diem; chưa có bán kính từng nơi, tham chiếu sơ đồ và nội dung tổng quan tách riêng như thiết kế |
| tblDiemThamQuan | Nhiều bảng/lớp riêng cho 5 tour; chưa có bảng thành phần thống nhất khóa ghép địa điểm–điểm và cấu hình AUTO trong cùng bảng |
| tblYeuThich | yeu_thich có quan hệ tài khoản–địa điểm; thiếu thoi_gian_them và khóa ngoại địa điểm |
| tblLichSuThamQuan | lich_su_nghe dùng thời gian epoch mili giây dạng INTEGER, cùng ba mã chế độ và thêm DEMO; thiếu khóa ngoại ghép tới điểm thành phần do mô hình tour phân tán, được kiểm tra trong xử lý ghi |

Helper chưa bật thực thi khóa ngoại bằng onConfigure/setForeignKeyConstraintsEnabled. Khai báo FOREIGN KEY không tự chứng minh mọi thao tác đã được SQLite kiểm soát. KhoLichSu có kiểm tra bản ghi liên quan trước ghi; datYeuThich chưa tự kiểm tra đầy đủ và chưa trả trạng thái ghi.

Database Inspector báo (closed) khi kết nối đã đóng không đồng nghĩa mất tệp DB. Chọn đúng tiến trình debug/thiết bị, mở chức năng truy cập dữ liệu rồi refresh.


### UC01 – Quản lý tài khoản

| Use case | Hiện trạng | Mã liên quan |
| --- | --- | --- |
| UC01.1 Đăng ký | Có tên đăng nhập duy nhất, mật khẩu và mã; chưa tạo vai trò/trạng thái/ngày tạo theo mô hình Word | DangKyActivity, TaiKhoanDb |
| UC01.2 Đăng nhập | Một phần: xác thực cục bộ; chưa kiểm tra tài khoản bị khóa hoặc phân quyền USER/ADMIN | DangNhapActivity, PhienDangNhap, TaiKhoanDb |
| UC01.3 Đăng xuất | Có kết thúc phiên và giữ dữ liệu đã lưu | CaNhanActivity, YeuThichActivity, PhienDangNhap |
| UC01.4 Quên mật khẩu | Có tên đăng nhập + mã khôi phục, đổi mật khẩu/cấp mã mới; phù hợp đặc tả Word hiện tại | QuenMatKhauActivity, TaiKhoanDb |
| UC01.5 Cập nhật hồ sơ | Có họ tên và email tùy chọn; email là phần bổ sung, không phải điều kiện xác thực trong Word | SuaHoSoActivity, HoSoTaiKhoan |
| UC01.6 Đổi mật khẩu | Có kiểm tra mật khẩu cũ, mật khẩu mới/xác nhận và cấp mã mới | DoiMatKhauActivity, TaiKhoanDb |

### UC02–UC05 – Tra cứu, chi tiết, bản đồ và vị trí

| Use case / yêu cầu | Hiện trạng | Mã liên quan |
| --- | --- | --- |
| UC02.1 Danh sách | Có 5 điểm; đường vào tab Khám phá hiện cuộn ô tìm kiếm, khác cách tách màn hình trong Word | MainActivity, KhamPhaActivity, ThanhDieuHuongDuoi |
| UC02.2 Tìm kiếm | Có không dấu và rỗng/không khớp; phạm vi tìm chưa thống nhất giữa màn hình | MainActivity, KhamPhaActivity, BanDoActivity, GoiYDiaDiem |
| UC02.3 Lọc | Có danh mục, kết hợp từ khóa trong KhamPhaActivity; trang chủ lọc riêng | MainActivity, KhamPhaActivity |
| UC02.4 Gần vị trí | Có tính/sắp xếp khoảng cách; chất lượng vị trí/ngoại lệ còn hạn chế. Word hiện không bắt buộc bán kính lọc cụ thể | MainActivity |
| UC03.1 Chi tiết | Có thông tin, ảnh, mô tả, nguồn; thư viện ảnh/phim còn hạn chế | ChiTietDiaDiemActivity, NguonThongTin |
| Bước Bắt đầu tham quan trong UC03 | Có mở đúng tour và đầy đủ danh sách thành phần | ChiTietDiaDiemActivity, 5 Activity tham quan |
| UC04.1 Bản đồ khu vực | Có OSM, marker, thông tin, tìm/chọn/zoom/vị trí và chỉ đường ngoài | BanDoActivity |
| UC04.2 Sơ đồ nội bộ | Có 5 sơ đồ, tầng/khu vực, marker, danh sách; chưa có cơ chế dự phòng sơ đồ thiếu thống nhất cho mọi tour | Các Activity tham quan, SoDoThuPhongLayout |
| UC05.1 Vị trí hiện tại | Có quyền/dịch vụ/cập nhật; bộ AUTO lọc mẫu, Gần tôi và thẻ bản đồ chưa đồng nhất độ mới/sai số | MainActivity, BanDoActivity, ThamQuanTuDong |
| UC05.2 Khoảng cách | Có đường chim bay m/km; không phải khoảng cách tuyến đường | MainActivity, BanDoActivity, ThamQuanTuDong |
| UC05.3 Chỉ đường | Có chuyển tọa độ tới ứng dụng/trình duyệt và xử lý không có nơi mở | BanDoActivity, ChiTietDiaDiemActivity |

### UC06 – Thuyết minh

| Use case | Hiện trạng | Mã liên quan |
| --- | --- | --- |
| UC06.1 Bật/tắt AUTO | Có GPS/Demo/Dừng ở trang chủ và 3 tour; xử lý quyền và dừng theo vòng đời | ThamQuanTuDong, HuongDanTour, HuongDanConDao |
| UC06.2 AUTO địa điểm chính | Có chọn gần nhất, sai số/ổn định/chống lặp, hỏi trước khi phát; bán kính 100 m dùng chung thay vì cấu hình mỗi nơi trong DB | BoNhanDienDiaDiem, LoiMoiThuyetMinh, ChiTietDiaDiemActivity |
| UC06.3 Nghe thủ công điểm chính | Có ở trang chủ và chi tiết; chi tiết dùng khung nghe chung | MainActivity, ChiTietDiaDiemActivity, KhungNgheThuyetMinh |
| UC06.4 AUTO chọn lọc thành phần | Có 7/12/3 mốc Huế/Côn Đảo/Văn Miếu, giới hạn trong tour; Dinh/Bảo tàng thủ công phù hợp điều kiện GPS | Các cấu hình AUTO, bộ nhận diện, controller tour |
| UC06.5 Nghe thủ công thành phần | Có mọi điểm có nội dung, không cần GPS; đang AUTO và chọn điểm khác có xác nhận trước khi chuyển | 5 Activity tham quan, BaseActivity |
| UC06.6 Điều khiển | Một phần: khung nghe có đầy đủ pause/resume/tua/dừng/nghe lại; AUTO thành phần và TTS trang chủ chưa có cùng bộ điều khiển | KhungNgheThuyetMinh, DoanAmThanh, MainActivity, controller tour |

AUTO thành phần có chọn lọc đã nằm trong **UC06.4 của Word hiện tại**, không còn là chức năng ngoài đặc tả. Word hiện liệt kê tạm dừng, tua và tiếp tục ở **UC06.6**; không dùng mã UC06.5 cho điều khiển như README cũ.

### UC07 – Hoạt động cá nhân

| Use case | Hiện trạng | Mã liên quan |
| --- | --- | --- |
| UC07.1 Thêm yêu thích | Có theo tài khoản, chống trùng; có tiếp tục lưu sau đăng nhập | ChiTietDiaDiemActivity, TaiKhoanDb |
| UC07.2 Xem yêu thích | Có danh sách/rỗng/khách/lỗi, mở chi tiết | YeuThichActivity |
| UC07.3 Xóa yêu thích | Có bỏ lưu và cập nhật đúng tài khoản | YeuThichActivity, ChiTietDiaDiemActivity |
| UC07.4 Lịch sử | Có ghi lúc bắt đầu nghe, danh sách riêng/mới nhất/mở lại/xóa; mã chế độ thống nhất Word, Demo riêng; truy cập từ Trang chủ và Cá nhân | LichSuActivity, KhoLichSu, LuotNgheLichSu, CaNhanActivity |

### UC08 – Quản trị hệ thống

**Chưa triển khai toàn bộ UC08.1–UC08.12.**

| Mã | Yêu cầu Word | Hiện trạng / nơi cần bổ sung |
| --- | --- | --- |
| UC08.1 | Xem danh sách địa điểm để quản lý | Chưa có màn hình quản trị; danh sách người dùng không thay thế quyền quản trị |
| UC08.2 | Thêm địa điểm | Chưa có form/ghi dữ liệu; cần mở rộng TaiKhoanDb và bỏ phụ thuộc danh sách ID cố định |
| UC08.3 | Sửa địa điểm | Chưa có UI hoặc xử lý quyền/cập nhật |
| UC08.4 | Xóa địa điểm | Chưa có xác nhận và xử lý đầy đủ dữ liệu liên kết |
| UC08.5 | Thêm điểm thành phần | Chưa có luồng quản trị; cần thống nhất mô hình tour |
| UC08.6 | Sửa điểm, marker và cấu hình AUTO | Chưa có form quản trị cho phép AUTO/WGS84/bán kính |
| UC08.7 | Xóa điểm thành phần | Chưa có xử lý liên kết, marker và lịch sử |
| UC08.8 | Cập nhật bài thuyết minh | Hiện sửa Java và tăng phiên bản nội dung; chưa có thao tác quản trị trong app |
| UC08.9 | Danh sách tài khoản | Chưa có Activity/truy vấn quản trị |
| UC08.10 | Tìm tài khoản theo tên/họ tên | Chưa có |
| UC08.11 | Xem tài khoản, vai trò/trạng thái/ngày tạo | Chưa có, schema cũng thiếu các trường này |
| UC08.12 | Khóa/mở khóa, không tự khóa ADMIN đang dùng | Chưa có schema, kiểm tra đăng nhập hoặc UI |

Nhấn giữ/chỉnh vị trí marker trong các công cụ sơ đồ hiện có không phải UC08.6 hoàn chỉnh: chưa có phân quyền, form nghiệp vụ hoặc cấu hình AUTO đầy đủ.

## 12. Lỗi, hạn chế và phần chưa hoàn thành

Các mục từ đọc mã là giới hạn hoặc nguy cơ cần kiểm thử, không phải tất cả đều đã tái hiện thành lỗi trên thiết bị.

| Mức / vấn đề | Ảnh hưởng và căn cứ | File liên quan |
| --- | --- | --- |
| Chưa có quản trị/phân quyền | Thiếu toàn bộ UC08, vai trò và trạng thái khóa của UC01 | TaiKhoanDb, PhienDangNhap, DangNhapActivity; chưa có Activity quản trị |
| Mô hình dữ liệu phân tán | Nhiều bảng seed trong Activity, ID cố định; khó thêm/xóa địa điểm tổng quát theo Word | TaiKhoanDb, 5 Activity tour, KhoDuLieuConDao |
| Khóa ngoại chưa được bật | Cần kiểm tra dữ liệu mồ côi và xử lý liên kết trước khi có CRUD | TaiKhoanDb, KhoLichSu |
| Gần tôi chưa lọc vị trí đầy đủ | API dưới 30 dùng lastKnownLocation chưa kiểm tra tuổi/sai số; ưu tiên GPS không có kết quả chưa chuyển network; chưa hủy yêu cầu theo vòng đời đầy đủ | MainActivity |
| Thẻ bản đồ chưa cập nhật khoảng cách liên tục | Vị trí hiển thị và khoảng cách trên thẻ có thể chưa đồng bộ khi di chuyển | BanDoActivity |
| Chờ callback AUTO chưa có timeout | Nếu TTS nhận lệnh nhưng không trả kết thúc, controller có thể kẹt đang đọc; đánh dấu đã phát trước khi biết hoàn tất | HuongDanTour, HuongDanConDao |
| Rút tai nghe lúc đang tổng hợp | Khung xử lý tạm dừng khi đã phát; có thể vẫn phát khi tổng hợp xong nếu rút tai nghe trước đó | KhungNgheThuyetMinh |
| Điều khiển âm thanh chưa đồng nhất | TTS trực tiếp chưa có tua/pause/resume hoặc cùng xử lý audio focus như khung MediaPlayer | MainActivity, controller tour, 3 Activity AUTO |
| Tái tạo màn hình tour | Chưa lưu đầy đủ điểm/tầng/zoom đang chọn; AUTO dừng là chủ đích nhưng lựa chọn có thể trở về ban đầu | 5 Activity tour |
| Chọn điểm đặt lại sơ đồ | Một số tour dựng lại sơ đồ/giải mã ảnh, có thể mất zoom hoặc tốn tài nguyên | ThamQuanActivity, BaoTangThamQuanActivity, ConDaoThamQuanActivity, VanMieuThamQuanActivity |
| Thanh tiến trình không phải hoàn thành | Tính theo chỉ số điểm đang chọn; chọn điểm cuối có thể hiện 100% dù chưa nghe các điểm trước | 5 Activity tour |
| Nhãn Nghe giới thiệu chưa phát ngay | Thẻ thành phần trong chi tiết mở tour, cần thêm thao tác mở/nghe | ChiTietDiaDiemActivity |
| Nội dung/ảnh chuyên biệt còn thiếu | Có đủ bài nhưng một số nguồn chỉ tổng quan; ảnh phần lớn dùng ảnh địa điểm lớn | DuLieuNoiDungThamQuan, docs/nguon_noi_dung.md, các tour |
| Bố cục 4 tiện ích ở chi tiết | Layout hiện vẫn 2×2; khác yêu cầu UI bốn nút một hàng đã trao đổi | activity_chi_tiet_dia_diem.xml |
| Tải và xử lý trên luồng UI | PBKDF2, SQLite/seed và giải mã ảnh có thể chậm trên máy yếu; chưa có đo hiệu năng mới | TaiKhoanDb, các Activity tài khoản/tour |
| Sao lưu chưa chốt chính sách | Manifest cho phép backup; các tệp rules còn mẫu, chưa có quy tắc riêng cho DB/phiên | AndroidManifest.xml, backup_rules.xml, data_extraction_rules.xml |
| Một kiểm thử lời mời trang chủ chưa qua | Espresso không cuộn được tới autoDemo; dừng trước bước mở chi tiết, không đủ căn cứ kết luận luồng tích hợp đã đạt | BanDoVaGpsThamQuanTest, activity_main.xml |

Chưa có đồng bộ đa thiết bị, dịch vụ GPS/âm thanh nền, định vị trong nhà, gói bản đồ offline hoặc thư viện phim tư liệu. Các phần này là hướng phát triển, không tự tính là thiếu use case bắt buộc nếu Word không yêu cầu.

## 13. Thứ tự ưu tiên hoàn thiện

| Ưu tiên | Công việc | Điều kiện nghiệm thu |
| --- | --- | --- |
| 1 | Hoàn thiện luồng đang dùng: lỗi kiểm thử lời mời, timeout AUTO, ngắt tai nghe khi chuẩn bị, kiểm soát GPS Gần tôi | Có test luồng chính/ngoại lệ; không đọc chồng, kẹt hoặc dùng vị trí cũ |
| 2 | Chốt các khác biệt app–Word: bảng/tọa độ/cấu hình AUTO, đường vào Khám phá và UI 4 tiện ích; lịch sử đã đồng bộ chế độ/lối vào | Một bảng quyết định rõ sửa app hay sửa tài liệu; không còn mô tả trái mã |
| 3 | Thống nhất mô hình địa điểm/điểm thành phần và migration | Thêm/sửa/xóa tổng quát; giữ dữ liệu cần giữ, xử lý liên kết và khóa ngoại; tránh ID mảng cố định |
| 4 | Bổ sung USER/ADMIN, trạng thái khóa và kiểm tra quyền nghiệp vụ | USER không được gọi thao tác quản trị; tài khoản khóa không đăng nhập; không tự khóa ADMIN |
| 5 | Triển khai UC08.1–UC08.8 | CRUD địa điểm/điểm/bài, WGS84/bán kính/AUTO, xác nhận xóa và cập nhật giao diện |
| 6 | Triển khai UC08.9–UC08.12 | Danh sách/tìm/xem/khóa–mở tài khoản; không lộ mật khẩu hoặc mã |
| 7 | Nghiệm thu GPS, marker và nội dung thực địa | Log độ chính xác/tuổi mẫu/vùng giao nhau; chỉ bật AUTO nơi đủ phân biệt; kiểm tra phát âm/nguồn/ảnh |
| 8 | Hồi quy và hoàn thiện báo cáo | Cài mới/nâng cấp/âm thanh/tài khoản/quản trị đạt; bổ sung ảnh và kết quả thật vào Word |

Backend không phải điều kiện bắt buộc để làm phần quản trị của đồ án. Nếu vẫn lưu cục bộ, cần mô tả đúng giới hạn quyền và dữ liệu trên từng thiết bị.

## 14. Bản đồ mã nguồn

Tất cả Java dưới [app/src/main/java/com/example/app_ck](app/src/main/java/com/example/app_ck); giao diện dưới [res/layout](app/src/main/res/layout). Hiện có 48 tệp Java chính và 23 layout XML.

| Nhóm | File chính / trách nhiệm |
| --- | --- |
| Chào và trang chủ | [WelcomeActivity](app/src/main/java/com/example/app_ck/WelcomeActivity.java), BanDoChaoMungView, [MainActivity](app/src/main/java/com/example/app_ck/MainActivity.java) |
| Điều hướng/giao diện chung | [BaseActivity](app/src/main/java/com/example/app_ck/BaseActivity.java), [ThanhDieuHuongDuoi](app/src/main/java/com/example/app_ck/ThanhDieuHuongDuoi.java), BieuMauTaiKhoanActivity |
| Tra cứu/gợi ý | [KhamPhaActivity](app/src/main/java/com/example/app_ck/KhamPhaActivity.java), GoiYDiaDiem, DiaDiem |
| Bản đồ/chi tiết | [BanDoActivity](app/src/main/java/com/example/app_ck/BanDoActivity.java), [ChiTietDiaDiemActivity](app/src/main/java/com/example/app_ck/ChiTietDiaDiemActivity.java) |
| Các tour | [ThamQuanActivity](app/src/main/java/com/example/app_ck/ThamQuanActivity.java), [BaoTangThamQuanActivity](app/src/main/java/com/example/app_ck/BaoTangThamQuanActivity.java), [DaiNoiHueThamQuanActivity](app/src/main/java/com/example/app_ck/DaiNoiHueThamQuanActivity.java), [ConDaoThamQuanActivity](app/src/main/java/com/example/app_ck/ConDaoThamQuanActivity.java), [VanMieuThamQuanActivity](app/src/main/java/com/example/app_ck/VanMieuThamQuanActivity.java) |
| Sơ đồ và dữ liệu nền | SoDoThuPhongLayout, DuLieuDinhDocLap, NoiDungThuyetMinhDinhDocLap, KhoDuLieuConDao, KhoToaDoThamQuan |
| Nội dung/nguồn | [DuLieuNoiDungThamQuan](app/src/main/java/com/example/app_ck/DuLieuNoiDungThamQuan.java), KhoNoiDungThamQuan, NguonThongTin, [bảng nguồn](docs/nguon_noi_dung.md) |
| Vị trí điểm chính | [ThamQuanTuDong](app/src/main/java/com/example/app_ck/ThamQuanTuDong.java), BoNhanDienDiaDiem, LoiMoiThuyetMinh |
| AUTO điểm thành phần | [HuongDanTour](app/src/main/java/com/example/app_ck/HuongDanTour.java), [HuongDanConDao](app/src/main/java/com/example/app_ck/HuongDanConDao.java), CauHinhAutoDiSan, CauHinhAutoConDao, BoNhanDienDiemThanhPhan, BoChonDiemTuDong |
| Khung nghe | [KhungNgheThuyetMinh](app/src/main/java/com/example/app_ck/KhungNgheThuyetMinh.java), DoanAmThanh, sheet_nghe_thuyet_minh.xml |
| Tài khoản | DangNhapActivity, DangKyActivity, QuenMatKhauActivity, CaNhanActivity, SuaHoSoActivity, DoiMatKhauActivity, PhienDangNhap, HoSoTaiKhoan |
| SQLite/yêu thích | [TaiKhoanDb](app/src/main/java/com/example/app_ck/TaiKhoanDb.java), [YeuThichActivity](app/src/main/java/com/example/app_ck/YeuThichActivity.java), activity_yeu_thich.xml, item_yeu_thich.xml |
| Lịch sử | [KhoLichSu](app/src/main/java/com/example/app_ck/KhoLichSu.java), LuotNgheLichSu, [LichSuActivity](app/src/main/java/com/example/app_ck/LichSuActivity.java), activity_lich_su.xml, item_lich_su.xml |
| Theme/hộp thoại | styles_hoi_thoai.xml, themes.xml; hộp cấp quyền vẫn do Android hiển thị |

