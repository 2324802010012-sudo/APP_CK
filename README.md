
## 1. Tổng quan và phạm vi

| Nội dung | Hiện trạng |
| --- | --- |
| Tên ứng dụng / phiên bản | `APP_CK` / `1.0`, versionCode `1` |
| Package / applicationId | `com.example.app_ck` |
| Nền tảng | Android, API 24 trở lên |
| Kiến trúc hiện tại | Activity xử lý giao diện/nghiệp vụ, `TaiKhoanDb` truy cập SQLite, các lớp dữ liệu Java cung cấp dữ liệu khởi tạo |
| Dữ liệu | SQLite cục bộ, chưa có backend hoặc cơ chế đồng bộ tài khoản/nội dung giữa các thiết bị |
| Bản đồ | OpenStreetMap qua osmdroid trong ứng dụng; Google Maps qua liên kết ngoài để xem vị trí/chỉ đường |
| Thuyết minh | Android TextToSpeech tiếng Việt, người dùng chủ động bấm phát/dừng |
| Tài khoản | Đăng ký, đăng nhập, khôi phục mật khẩu, yêu thích và đăng xuất từ màn hình yêu thích |
| Phần chưa có | Lịch sử tham quan, tiến độ lưu lâu dài, tự phát thuyết minh theo vị trí và quản trị |

### 3 địa điểm khởi tạo

Các giá trị sau là dữ liệu demo trong `TaiKhoanDb.themDuLieuDiaDiem()`; địa chỉ, tọa độ và nội dung cần tiếp tục đối chiếu nguồn chính thức trước khi dùng thực tế.

| ID | Địa điểm | Địa chỉ trong dữ liệu | Vĩ độ, kinh độ | Tên drawable |
| ---: | --- | --- | --- | --- |
| 0 | Dinh Độc Lập | 135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh | 10.7770, 106.6953 | `anh_dinh_doc_lap` |
| 1 | Bảo tàng Chứng tích Chiến tranh | 28 Võ Văn Tần, TP. Hồ Chí Minh | 10.7795, 106.6921 | `anh_bao_tang_chung_tich` |
| 2 | Bưu điện Trung tâm Sài Gòn | 2 Công xã Paris, TP. Hồ Chí Minh | 10.7799, 106.6999 | `anh_buu_dien_sai_gon` |

## 2. Đối chiếu chức năng và màn hình

| Nhóm | Phần đã hiện thực | Giới hạn hiện tại |
| --- | --- | --- |
| Chào mừng | Giao diện ảnh, giới thiệu tính năng; nút bắt đầu mở trang chủ và đóng Welcome | Nút đăng nhập vẫn chỉ hiện Toast “đang được xây dựng” |
| Trang chủ | Đọc tên/tọa độ từ SQLite; đồng bộ tên/ảnh cho 3 thẻ nổi bật và 2 thẻ gợi ý; mở chi tiết, bản đồ, yêu thích | Cố định 3 ID địa điểm; gợi ý là danh sách có sẵn |
| Tìm kiếm trang chủ | Chuẩn hóa chữ thường, dấu tiếng Việt, `đ`, khoảng trắng; tìm theo tên và chuỗi từ khóa TP. Hồ Chí Minh/Sài Gòn; hiển thị kết quả hoặc thông báo rỗng | Chưa tìm trong mô tả, địa chỉ hoặc hoạt động dù ô nhập có gợi ý “điểm đến hoặc hoạt động” |
| Danh mục | Đọc lại SQLite khi chọn bộ lọc, chuẩn hóa và khớp trường `danh_muc`, ẩn/hiện thẻ tương ứng; thông báo khi không có kết quả | Còn cố định 3 thẻ; lọc trong Java sau khi đọc danh sách, chưa lọc đồng thời các mục gợi ý/gần tôi |
| Địa điểm gần tôi | Xin quyền vị trí, lấy vị trí, tính khoảng cách và sắp từ gần đến xa; chọn dòng mở chi tiết | Khoảng cách đường chim bay; API 30+ lấy vị trí hiện tại, API thấp hơn dùng vị trí đã biết gần nhất; chưa theo dõi liên tục |
| Chi tiết | Tên, danh mục, địa chỉ, mô tả, ảnh; xem thêm/thu gọn; yêu thích; nghe/dừng; mở tham quan và các thẻ điểm nổi bật | Có dữ liệu Java dự phòng; thư viện chỉ có 1 ảnh mỗi địa điểm, chưa có phim tư liệu |
| Giờ mở cửa | Hiển thị lời nhắc kiểm tra kênh chính thức | Chưa lưu hoặc hiển thị lịch mở cửa thực tế |
| Bản đồ khu vực | MapView OpenStreetMap, 3 marker, thẻ thông tin, tìm kiếm, nút chọn nhanh, zoom và vị trí của tôi | Chưa có định tuyến trong ứng dụng; ảnh thẻ bản đồ vẫn chọn từ mảng resource Java |
| Chỉ đường | Mở Google Maps URL từ chi tiết hoặc bản đồ theo tọa độ đích | Phụ thuộc ứng dụng/trình duyệt ngoài; không tính quãng đường/thời gian đi trong app |
| Tham quan Dinh Độc Lập | Đọc 25 marker và nội dung thuyết minh từ SQLite; sơ đồ 6 tầng, marker chọn được, danh sách theo tầng, điểm trước/tiếp theo | Ảnh sơ đồ/tên tầng vẫn từ mảng Java; chưa lưu lịch sử/điểm đã tham quan |
| Tham quan hai địa điểm còn lại | Mỗi địa điểm có 3 khu vực, mô tả và thuyết minh thủ công | Danh sách/nội dung còn nằm trong Java, chưa có sơ đồ marker riêng |
| Tài khoản | Đăng ký, đăng nhập, đặt lại mật khẩu bằng mã khôi phục và lưu phiên | Chưa có email/OTP, vai trò, khóa tài khoản hoặc trang hồ sơ |
| Yêu thích | Cả nút tiện ích và tab yêu thích ở trang chủ mở đúng Activity; thêm/xóa/xem theo tài khoản; đăng xuất có xác nhận | Tên địa điểm trong danh sách yêu thích vẫn lấy từ mảng Java |
| Cá nhân và tiện ích | Có menu tài khoản, tab cá nhân/khám phá/gợi ý | Menu tài khoản và các tab này mới hiển thị Toast; chưa có màn hình lịch sử/cài đặt |

Nút **Thuyết minh**, **Lộ trình** và banner trải nghiệm ở trang chủ hiện chỉ hiện thông báo. Chức năng thuyết minh thực tế nằm trong màn hình **chi tiết** và **tham quan**. Giao diện quảng bá lộ trình chưa tương ứng với một chức năng lập hành trình riêng.

### Bản đồ OpenStreetMap

`BanDoActivity` sử dụng osmdroid `6.1.20`, tải tile HTTPS từ `https://tile.openstreetmap.org/`, có user agent riêng và nhãn `© OpenStreetMap contributors` trong layout. Cấu hình zoom từ 12 đến 19, ban đầu 15.5, cho phép kéo/phóng bằng cử chỉ và nút cộng/trừ.

- Chạm marker hoặc một trong ba nút chọn nhanh để cập nhật thẻ địa điểm và bật nút xem chi tiết/chỉ đường.
- Tìm bằng thao tác Search trên bàn phím; khớp tên sau khi đổi chữ thường, chọn kết quả đầu tiên. **Tìm kiếm bản đồ chưa loại bỏ dấu**, khác với trang chủ.
- Nút **Vị trí của tôi** xin quyền khi cần, dùng `MyLocationNewOverlay` để hiển thị vị trí và đưa bản đồ tới người dùng. Thẻ địa điểm có thể hiển thị khoảng cách đường chim bay khi đã có GPS.
- `onPause()` tắt lớp định vị; `onResume()` khôi phục nếu trước đó đã bật; `onDestroy()` đóng kết nối SQLite và giải phóng MapView.
- Bản đồ cần mạng để tải tile chưa có trong cache; chưa có chức năng tải vùng bản đồ ngoại tuyến. Cấu hình hiện tại không cần Google Maps API key.

## 3. Sơ đồ và thuyết minh Dinh Độc Lập

Marker được khởi tạo từ `DuLieuDinhDocLap`, lưu vào SQLite, sau đó `ThamQuanActivity` đọc lại để vẽ bằng `Canvas`. Tọa độ `x`, `y` nằm trong khoảng `[0, 1]`, được nhân với kích thước ảnh khi hiển thị. Khi nhiều marker ở gần nhau, thao tác chạm chọn marker gần nhất trong ngưỡng 26dp.

| ID điểm | Tầng / mã khu vực | Tên điểm |
| ---: | --- | --- |
| 1 | Tầng 1 / 1 | Phòng Nội các |
| 2 | Tầng 1 / 1 | Phòng Đại yến |
| 3 | Tầng 1 / 1 | Cầu thang trung tâm |
| 4 | Tầng 1 / 1 | Phòng Khánh tiết |
| 5 | Tầng 2 / 2 | Phòng Hội đồng an ninh Quốc gia |
| 6 | Tầng 2 / 2 | Phòng làm việc của Tổng thống |
| 7 | Tầng 2 / 2 | Phòng khách của Tổng thống |
| 8 | Tầng 2 / 2 | Phòng khách của Phó Tổng thống |
| 9 | Tầng 2 / 2 | Phòng trình quốc thư |
| 10 | Tầng 2 / 2 | Khu gia đình Tổng thống |
| 11 | Tầng 3 / 3 | Thư viện |
| 12 | Tầng 3 / 3 | Phòng khách phu nhân Tổng thống |
| 13 | Tầng 3 / 3 | Phòng chiếu phim |
| 14 | Tầng 3 / 3 | Phòng giải trí |
| 15 | Tầng 4 / 4 | Tứ phương vô sự lầu |
| 16 | Tầng 4 / 4 | Trực thăng UH1 |
| 17 | Tầng hầm / 5 | Tham mưu tác chiến |
| 18 | Tầng hầm / 5 | Thông tin liên lạc |
| 19 | Tầng hầm / 5 | Phòng ngủ Tổng thống |
| 20 | Tầng hầm / 5 | Phòng trực chiến của Tổng thống |
| 21 | Tầng trệt / 6 | Bếp |
| 22 | Tầng trệt / 6 | Xe Jeep |
| 23 | Tầng trệt / 6 | Xe Mercedes |
| 24 | Tầng trệt / 6 | Phim tư liệu |
| 25 | Tầng trệt / 6 | Phòng tập bắn |

Số điểm theo thứ tự 6 khu vực là **4, 6, 4, 2, 4, 5**. Tên “Phim tư liệu” là một điểm trên sơ đồ, không có nghĩa ứng dụng đã tích hợp phát video.

Khi mở tham quan, ứng dụng yêu cầu đủ 25 marker, ID tuần tự 1–25, mã tầng 1–6 và tọa độ hợp lệ. Nếu dữ liệu thiếu/sai, màn hình thông báo rồi đóng. Chuyển điểm sẽ dừng giọng đọc, đổi tầng nếu cần và cập nhật nội dung. Đổi tab tầng chỉ đổi sơ đồ/danh sách đang xem; điểm được chọn giữ nguyên đến khi người dùng chọn điểm khác.

Bốn thẻ cũ ở màn hình chi tiết được ánh xạ như sau:

| Thẻ chi tiết | Điểm mở trong sơ đồ |
| --- | --- |
| Cổng chính và khuôn viên | Điểm 1 – Phòng Nội các (điểm bắt đầu mặc định) |
| Phòng Khánh tiết | Điểm 4 |
| Phòng họp Hội đồng Nội các | Điểm 1 |
| Khu hầm | Điểm 17 |

Cổng và khuôn viên không có marker riêng. Nút bắt đầu/xem tất cả cũng mở từ điểm 1. Extra `viTriKhuVuc` từ chi tiết là chỉ số của bốn thẻ này, không phải ID marker trực tiếp; bên trong màn hình tham quan, ID marker được quy đổi thành chỉ số `id - 1`.

### Nguồn nội dung giọng đọc

- **Chi tiết địa điểm:** đọc mô tả tổng quan đang hiển thị, ưu tiên dữ liệu SQLite và dùng Java dự phòng khi thiếu.
- **25 điểm Dinh Độc Lập:** đọc bảng `thuyet_minh` theo ID điểm cho cả mô tả và giọng đọc; thiếu nội dung thì hiển thị thông báo hoặc không phát.
- **Bảo tàng/bưu điện:** ghép tên khu vực với mô tả trong mảng Java.
- Nội dung seed tại `NoiDungThuyetMinhDinhDocLap` chỉ có bài riêng cho điểm **1** và **4**. 23 điểm còn lại có nội dung giới thiệu vị trí/tầng và thông báo đang cập nhật tư liệu.
- TextToSpeech đặt ngôn ngữ `vi-VN`, dùng `QUEUE_FLUSH`; giọng đọc dừng khi rời màn hình và engine được giải phóng khi Activity bị hủy.
- Chưa có audio thu sẵn, tự phát theo GPS, điều chỉnh tốc độ trên giao diện hoặc ghi lịch sử nghe. Thanh tiến trình chỉ thể hiện **thứ tự điểm đang chọn**, chưa phản ánh số điểm đã thực sự tham quan.

## 4. SQLite và luồng dữ liệu

Tệp CSDL: **`du_lich_tai_khoan.db`**, phiên bản hiện tại: **6**. `TaiKhoanDb` kế thừa `SQLiteOpenHelper`. Cài mới khởi tạo 6 bảng, seed 3 địa điểm, 6 khu vực, 25 điểm và 25 nội dung thuyết minh. Số lượng này là dữ liệu dự kiến từ mã khởi tạo, chưa được xác nhận bằng truy vấn trên thiết bị trong lần đối chiếu này.

### Các bảng

| Bảng | Cột và ràng buộc chính |
| --- | --- |
| `tai_khoan` | `id INTEGER PRIMARY KEY AUTOINCREMENT`, `ten_dang_nhap TEXT NOT NULL UNIQUE`, `muoi TEXT NOT NULL`, `mat_khau_bam TEXT NOT NULL`, `ma_khoi_phuc_bam TEXT` |
| `yeu_thich` | `tai_khoan_id INTEGER NOT NULL`, `dia_diem_id INTEGER NOT NULL`; khóa chính ghép hai cột; khai báo FK tài khoản tới `tai_khoan(id)` |
| `dia_diem` | `id INTEGER PRIMARY KEY`, `ten TEXT NOT NULL`, `dia_chi TEXT`, `mo_ta TEXT`, `danh_muc TEXT`, `vi_do REAL NOT NULL`, `kinh_do REAL NOT NULL`, `ten_anh TEXT` |
| `khu_vuc` | `id INTEGER PRIMARY KEY`, `dia_diem_id INTEGER NOT NULL`, `ten TEXT NOT NULL`, `tang INTEGER`, `ten_anh_so_do TEXT`; FK tới `dia_diem(id)` |
| `diem_tham_quan` | `id INTEGER PRIMARY KEY`, `khu_vuc_id INTEGER NOT NULL`, `ten TEXT NOT NULL`, `mo_ta TEXT`, `toa_do_x REAL`, `toa_do_y REAL`, `ten_anh TEXT`; FK tới `khu_vuc(id)` |
| `thuyet_minh` | `id INTEGER PRIMARY KEY`, `diem_tham_quan_id INTEGER NOT NULL UNIQUE`, `noi_dung TEXT NOT NULL`; FK tới `diem_tham_quan(id)` |

Quan hệ nội dung: `dia_diem → khu_vuc → diem_tham_quan → thuyet_minh`. `yeu_thich` lưu ID địa điểm nhưng **chưa khai báo FK tới `dia_diem`**. Code hiện chưa bật cưỡng chế khóa ngoại bằng `onConfigure()`/`setForeignKeyConstraintsEnabled(true)`; không nên xem khai báo FK là bằng chứng mọi liên kết đã được kiểm soát lúc chạy.

ID địa điểm cố định 0–2; ID khu vực 1–6 đồng thời được dùng làm mã tầng; ID điểm Dinh Độc Lập 1–25. Nhiều luồng vẫn phụ thuộc quy ước này, chưa hỗ trợ thêm địa điểm tùy ý chỉ bằng cách thêm hàng vào CSDL.

### Mức độ sử dụng SQLite

| Thành phần | Đọc từ SQLite | Phần còn cố định trong Java/XML |
| --- | --- | --- |
| Trang chủ | Tên, tọa độ, tên ảnh; có tải đối tượng chứa địa chỉ/mô tả/danh mục | Số thẻ, ID, nhãn phụ, các lựa chọn bộ lọc và gợi ý |
| Bản đồ | Tên, địa chỉ, tọa độ, tên ảnh | Ảnh hiển thị vẫn lấy `ANH_DIA_DIEM`; tên ảnh đọc từ CSDL chưa được áp dụng |
| Chi tiết | Tên, địa chỉ, mô tả, danh mục, tọa độ, tên drawable | Giá trị dự phòng và các thẻ điểm nổi bật |
| Sơ đồ Dinh Độc Lập | ID/tên/tầng/tọa độ marker; thuyết minh theo điểm | Tên/ảnh các tầng dùng mảng; `ten_anh_so_do` trong bảng `khu_vuc` chưa điều khiển ảnh hiển thị |
| Tham quan bảo tàng/bưu điện | Chưa đọc danh sách khu vực/thuyết minh từ CSDL | Tên, ảnh, 3 khu vực và mô tả mỗi địa điểm |
| Yêu thích | Danh sách ID theo tài khoản, thao tác thêm/xóa | Tên địa điểm trong danh sách |

`DuLieuDinhDocLap` và `NoiDungThuyetMinhDinhDocLap` vẫn là nguồn seed. Phần lớn thao tác seed dùng `CONFLICT_IGNORE`, nên sửa nội dung seed Java không tự cập nhật bản ghi đã tồn tại. `themMoTaVaDanhMuc()` chỉ điền trường trống/null để giữ nội dung đã có.

### Các mốc nâng cấp CSDL

| Phiên bản đích | Thay đổi trong `onUpgrade()` |
| ---: | --- |
| 2 | Thêm `ma_khoi_phuc_bam` vào `tai_khoan` |
| 3 | Tạo bảng du lịch và seed 3 địa điểm |
| 4 | Seed 6 khu vực và 25 marker |
| 5 | Tạo bổ sung bảng nếu thiếu và seed thuyết minh |
| 6 | Thêm cột `danh_muc` vào CSDL cũ, điền danh mục/mô tả trống |

**Lỗi nâng cấp đã xác nhận bằng mô phỏng SQL:** khi nâng trực tiếp từ phiên bản **1 hoặc 2** lên 6, nhánh `< 3` gọi `taoBangDuLich()` hiện đã tạo `dia_diem.danh_muc`, sau đó nhánh `< 6` tiếp tục `ALTER TABLE ... ADD COLUMN danh_muc`, gây `duplicate column name: danh_muc`. Đây là lỗi còn tồn tại trong mã nguồn, chưa được sửa trong lần cập nhật tài liệu. Cần kiểm tra sự tồn tại của cột hoặc tách schema theo từng phiên bản và kiểm thử migration trước khi dùng dữ liệu cũ. Các đường nâng cấp khác chưa được chạy thử trên thiết bị.

## 5. Tài khoản, phiên đăng nhập và yêu thích

- Đăng ký yêu cầu tên khớp `[A-Za-z0-9_]{3,30}`, mật khẩu từ 8 ký tự và xác nhận trùng khớp. Tên được trim/chuyển chữ thường khi lưu và tra cứu; có ràng buộc không trùng.
- PBKDF2 dùng `PBKDF2WithHmacSHA1`, 120.000 vòng lặp, khóa 256 bit và salt ngẫu nhiên 16 byte; mật khẩu và mã khôi phục lưu dạng băm Base64.
- Đăng ký mới cấp mã khôi phục ngẫu nhiên từ 12 byte, hiển thị để người dùng tự lưu. Khôi phục dùng tên đăng nhập + mã + mật khẩu mới; thành công tạo salt/mã mới, mã cũ hết hiệu lực. Tài khoản cũ không có mã khôi phục không dùng được luồng này.
- Luồng khôi phục không gửi email/SMS và không yêu cầu Internet. Chưa có backend xác thực, phân quyền hoặc chức năng khóa tài khoản.
- `SharedPreferences` tên `phien_dang_nhap` lưu `id`, `ten`; đăng nhập thành công đóng màn hình đăng nhập và quay lại màn hình trước.
- Yêu thích yêu cầu đăng nhập. Từ chi tiết, ứng dụng mở hộp thoại mời đăng nhập; quay lại cần bấm yêu thích để thực hiện thao tác. Từ trang chủ có thể vào danh sách yêu thích rồi bấm đăng nhập.
- Danh sách cho phép mở chi tiết, bỏ yêu thích với xác nhận và đăng xuất với xác nhận. Đăng xuất xóa phiên, không xóa tài khoản hoặc các địa điểm đã lưu.
- Ứng dụng bật `allowBackup`; các file backup/extraction hiện còn cấu hình mẫu, chưa có quy tắc loại trừ riêng cho CSDL/phiên. Chưa có chức năng đồng bộ do ứng dụng tự cung cấp.

## 6. Điều hướng và cấu trúc dự án

Manifest khai báo **9 Activity**, chỉ `WelcomeActivity` được export làm launcher. Có 8 layout màn hình XML; `YeuThichActivity` dựng giao diện bằng Java qua các hàm hỗ trợ trong `DangNhapActivity`.

```text
Welcome --Bắt đầu--> Main
Main --chọn địa điểm/tìm kiếm/gợi ý/gần tôi--> ChiTietDiaDiem
Main --Bản đồ--> BanDo --Xem chi tiết--> ChiTietDiaDiem
Main --Yêu thích (tiện ích hoặc tab)--> YeuThich
ChiTietDiaDiem --Bắt đầu/chọn thẻ--> ThamQuan
ChiTietDiaDiem --Yêu thích khi chưa đăng nhập--> DangNhap
YeuThich --chưa đăng nhập--> DangNhap
DangNhap --> DangKy / QuenMatKhau
YeuThich --Đăng xuất--> xóa phiên, cập nhật giao diện
ChiTietDiaDiem / BanDo --Chỉ đường--> ứng dụng ngoài
```

Các luồng trên chỉ mô tả liên kết đã có trong code. Nút đăng nhập tại Welcome và mục đăng nhập/đăng ký của menu tài khoản trang chủ chưa được nối.

### Vai trò các tệp Java

Tất cả nằm trong [`app/src/main/java/com/example/app_ck/`](app/src/main/java/com/example/app_ck/).

| Tệp | Trách nhiệm |
| --- | --- |
| `BaseActivity.java` | Ẩn thanh điều hướng hệ thống khi tạo/tiếp tục màn hình; cho phép vuốt hiện tạm thời |
| `WelcomeActivity.java` | Màn hình chào và mở trang chủ |
| `MainActivity.java` | Tải 3 địa điểm, đồng bộ thẻ, tìm kiếm/lọc, gợi ý, gần tôi và điều hướng |
| `BanDoActivity.java` | OpenStreetMap, marker, tìm kiếm, vị trí, khoảng cách và liên kết chỉ đường |
| `ChiTietDiaDiemActivity.java` | Đồng bộ chi tiết từ CSDL, dữ liệu dự phòng, ảnh, thuyết minh, yêu thích và mở tham quan |
| `ThamQuanActivity.java` | Tải/kiểm tra marker, vẽ sơ đồ, chuyển điểm và đọc thuyết minh |
| `TaiKhoanDb.java` | Tạo/nâng cấp/seed CSDL, xử lý tài khoản, yêu thích và truy vấn nội dung |
| `DiaDiem.java` | Đối tượng dữ liệu địa điểm, gồm cả mô tả/danh mục/tên ảnh |
| `DuLieuDinhDocLap.java` | Model điểm, dữ liệu seed 25 marker, quy ước tầng và tên ảnh sơ đồ |
| `NoiDungThuyetMinhDinhDocLap.java` | Nội dung seed thuyết minh theo ID |
| `DangNhapActivity.java` | Đăng nhập, mở đăng ký/khôi phục và các hàm dựng UI dùng chung |
| `DangKyActivity.java` | Kiểm tra dữ liệu đăng ký và hiển thị mã khôi phục |
| `QuenMatKhauActivity.java` | Đổi mật khẩu bằng mã và hiển thị mã thay thế |
| `PhienDangNhap.java` | Đọc/lưu/xóa phiên SharedPreferences |
| `YeuThichActivity.java` | Danh sách riêng theo tài khoản, bỏ yêu thích và đăng xuất |

Các màn hình kế thừa `BaseActivity`, ngoại trừ `BanDoActivity` kế thừa trực tiếp `AppCompatActivity` và tự ẩn navigation bar.

### Tài nguyên và cấu hình

| Đường dẫn | Nội dung |
| --- | --- |
| `app/src/main/res/layout/` | 8 layout màn hình và `styles_chi_tiet_bo_sung.xml`; tệp style bổ sung hiện nằm trong `layout`, chưa được tham chiếu |
| `app/src/main/res/drawable/` | 52 tệp: 3 ảnh JPG địa điểm, 6 ảnh PNG sơ đồ, 43 XML nền/vector icon |
| `app/src/main/res/mipmap-*/` | Icon launcher theo mật độ màn hình và adaptive icon |
| `app/src/main/res/values/`, `values-night/` | App name, màu và theme `Theme.Material3.DayNight.NoActionBar`; nhiều màu/chuỗi vẫn viết trực tiếp trong layout/Java |
| `app/src/main/res/xml/` | Quy tắc backup/extraction mẫu |
| `app/src/main/AndroidManifest.xml` | Activity, quyền và cấu hình ứng dụng |
| `app/src/test/`, `app/src/androidTest/` | Hai lớp kiểm thử mẫu |
| `app/build.gradle.kts` | SDK, applicationId, Java và dependency module |
| `gradle/libs.versions.toml` | Phiên bản thư viện và plugin Android |
| `settings.gradle.kts` | Module `app`, Google/Maven Central/Plugin Portal và Foojay resolver |
| `gradle/wrapper/gradle-wrapper.properties` | Phiên bản và checksum Gradle Wrapper |
| `gradle/gradle-daemon-jvm.properties` | Toolchain JVM 21 |


## 7. Môi trường và quyền truy cập

| Thành phần | Cấu hình trong mã nguồn |
| --- | --- |
| Android tối thiểu / target | API 24 / API 36 |
| Compile SDK | API 36, minor API 1 (`36.1`) |
| Android Gradle Plugin / Gradle | 9.1.1 / 9.3.1 |
| JVM Gradle / mức ngôn ngữ ứng dụng | Java 21 / Java 11 |
| AppCompat / Material | 1.8.0 / 1.14.0 |
| AndroidX Activity / ConstraintLayout | 1.13.0 / 2.2.2 |
| osmdroid | 6.1.20 |
| JUnit / AndroidX JUnit / Espresso | 4.13.2 / 1.3.0 / 3.7.0 |
| Release | `isMinifyEnabled = false`; chưa cấu hình signing release riêng |

Manifest khai báo `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`. Quyền vị trí được xin lúc dùng tính năng gần tôi/vị trí của tôi. Chưa khai báo quyền vị trí nền hoặc service theo dõi nền. Nội dung lưu cục bộ có thể đọc khi không có mạng; việc nghe còn tùy TTS engine/giọng đã cài. Bản đồ và Google Maps URL phụ thuộc kết nối và ứng dụng bên ngoài.


## 11. Tồn đọng và ưu tiên hoàn thiện

1. **Sửa và kiểm thử migration SQLite**, trước hết trường hợp v1/v2 lên v6 bị trùng cột `danh_muc`; kiểm tra dữ liệu được giữ qua từng phiên bản.
2. **Hoàn thiện điều hướng** đăng nhập tại Welcome và menu tài khoản; triển khai cá nhân/lịch sử/cài đặt hoặc điều chỉnh các mục giới thiệu cho khớp chức năng.
3. **Thống nhất dữ liệu** giữa trang chủ, bản đồ, chi tiết, yêu thích và tham quan; bỏ phụ thuộc mảng 3 ID, đọc ảnh bản đồ/tên yêu thích/tên ảnh sơ đồ từ CSDL, truy vấn marker theo khu vực/địa điểm thay vì đọc toàn bộ bảng.
4. **Bổ sung nội dung và media** cho 23 điểm còn thiếu bài riêng, ảnh từng điểm, giờ tham quan; đối chiếu tư liệu lịch sử, địa chỉ, tọa độ. Báo cáo đã được đối chiếu ở mục 12; nội dung lịch sử của từng điểm vẫn cần kiểm chứng bằng tư liệu chính thức.
5. **Lưu hoạt động cá nhân**: thêm bảng/màn hình lịch sử nghe/tham quan và tiến độ thực tế. Hiện chưa có các bảng này.
6. **Hoàn thiện bản đồ và vị trí**: thống nhất tìm kiếm không dấu, xử lý mất mạng/GPS, kiểm tra khoảng cách theo cập nhật vị trí; xây dựng lộ trình nếu thuộc phạm vi đồ án. Chưa có routing hoặc chức năng tải bản đồ ngoại tuyến riêng.
7. **Thuyết minh theo vị trí**: bổ sung bán kính/cơ chế kích hoạt và chống lặp theo UC06.1–UC06.2; hiện chưa có geofencing hoặc phát tự động.
8. **Quản trị và tài khoản**: bổ sung vai trò/trạng thái và quản lý nội dung, tài khoản theo UC08 của báo cáo; hiện chưa có Activity quản trị, vai trò ADMIN hoặc chức năng khóa/mở tài khoản.
9. **Chuẩn hóa UI/tài nguyên**: đưa chuỗi/màu về resources, thống nhất xử lý thanh điều hướng và giao diện sáng/tối; chuyển style bổ sung về `values` nếu thực sự dùng.
10. **Bổ sung kiểm thử có ý nghĩa**, tập trung migration, tài khoản, yêu thích, truy vấn và tìm kiếm; chạy demo trên thiết bị cho bản đồ/GPS/TTS trước khi kết luận hoàn tất chức năng.

### 12.2. Ma trận 37 use case

“Có luồng chính” nghĩa là tìm thấy xử lý tương ứng trong mã nguồn; không đồng nghĩa đã kiểm thử đạt toàn bộ luồng thay thế và yêu cầu phi chức năng. “Một phần/khác đặc tả” nghĩa là đã có chức năng liên quan nhưng còn thiếu yêu cầu được nêu trong báo cáo.

| Use case | Chức năng trong báo cáo | Đánh giá và phần cần làm tiếp |
| --- | --- | --- |
| UC01.1 | Đăng ký tài khoản | **Một phần/khác đặc tả:** code dùng tên đăng nhập, mật khẩu và mã khôi phục; báo cáo yêu cầu họ tên, email duy nhất; CSDL chưa có họ tên/email/ngày tạo |
| UC01.2 | Đăng nhập | **Một phần/khác đặc tả:** xác thực và lưu phiên đã có; chưa đăng nhập bằng email, xác định USER/ADMIN và chuyển màn hình theo quyền; chưa kiểm tra khóa tài khoản theo UC08.12 |
| UC01.3 | Đăng xuất | **Một phần/khác đặc tả:** có xác nhận và xóa phiên trong yêu thích; báo cáo yêu cầu chuyển về đăng nhập, hiện chỉ cập nhật màn hình yêu thích |
| UC01.4 | Quên mật khẩu | **Một phần/khác đặc tả:** đã đổi mật khẩu bằng mã khôi phục; báo cáo mô tả nhập email, không mô tả mã khôi phục. Cần thống nhất đặc tả với cơ chế xác minh hiện tại, không bỏ xác minh chỉ để khớp câu chữ |
| UC02.1 | Xem danh sách điểm du lịch | **Một phần:** đọc SQLite và chọn chi tiết được; còn cố định 3 thẻ/ID, thẻ không hiển thị đủ địa chỉ và mô tả ngắn như đặc tả; thiếu dữ liệu thì trang chủ đóng thay vì danh sách trống |
| UC02.2 | Tìm kiếm điểm du lịch | **Có luồng chính:** tìm theo tên/từ khóa, không phân biệt hoa thường và có xử lý không có kết quả; trang chủ hỗ trợ không dấu, bản đồ chưa đồng nhất |
| UC02.3 | Lọc điểm du lịch | **Có luồng chính:** đọc `danh_muc` từ SQLite, lọc thẻ và báo khi rỗng; còn giới hạn 3 thẻ và không lọc các khu vực gợi ý/gần tôi |
| UC02.4 | Xem địa điểm gần vị trí hiện tại | **Một phần:** có quyền, GPS, khoảng cách, sắp xếp và mở chi tiết; chưa giới hạn theo phạm vi tìm kiếm BR02.4-4, chưa bỏ qua tọa độ sai; kết quả chỉ có tên/km, thiếu ảnh/địa chỉ |
| UC03.1 | Xem chi tiết điểm du lịch chính | **Một phần:** có thông tin và các nút chính; khác nhánh lỗi báo cáo vì dùng Java dự phòng khi thiếu CSDL; chưa hiển thị tọa độ/khoảng cách ngay ở chi tiết như mô tả tổng quát UC03 |
| UC03.2 | Bắt đầu tham quan | **Có luồng chính:** mở tham quan và các điểm thành phần; Dinh Độc Lập dùng sơ đồ, hai nơi còn lại dùng danh sách; chưa truy vấn tổng quát theo PlaceID |
| UC04.1 | Xem bản đồ khu vực | **Có luồng chính:** OSM, 3 marker, kéo/zoom, có lớp vị trí; hiện phải bấm “Vị trí của tôi” để bật định vị, chưa tự bật chỉ vì đã có quyền |
| UC04.2 | Chọn marker điểm du lịch | **Có luồng chính:** chọn đúng địa điểm, hiện tên/địa chỉ/khoảng cách khi có GPS, mở chi tiết/chỉ đường |
| UC04.3 | Xem sơ đồ tham quan nội bộ | **Có luồng chính:** Dinh Độc Lập đọc marker/tọa độ từ SQLite và cho chọn; hai nơi còn lại dùng danh sách thay thế, được đặc tả cho phép; cần kiểm tra marker trên các kích thước màn hình |
| UC05.1 | Xác định vị trí hiện tại | **Có luồng chính:** xin quyền, lấy vị trí, hiển thị trên bản đồ khi bật; API 24–29 ở trang chủ chỉ dùng vị trí đã biết gần nhất, chưa đảm bảo độ mới |
| UC05.2 | Xem khoảng cách đến địa điểm | **Một phần:** tính/hiển thị đã có; chưa tự cập nhật khoảng cách khi vị trí thay đổi đáng kể theo yêu cầu phi chức năng; hiện tính lại khi lấy vị trí/chọn địa điểm |
| UC05.3 | Chỉ đường đến địa điểm | **Có luồng chính:** Intent Google Maps theo tọa độ và xử lý không có ứng dụng; báo cáo cho phép dùng ứng dụng bản đồ ngoài, không bắt buộc routing nội bộ |
| UC06.1 | Bật/tắt thuyết minh tự động | **Chưa có:** thiếu giao diện, lưu trạng thái AUTO và theo dõi vị trí theo trạng thái |
| UC06.2 | Nghe thuyết minh tự động tại điểm du lịch chính | **Chưa có:** thiếu bán kính, kiểm tra khoảng cách, chọn điểm gần nhất, chống phát lặp và lưu trạng thái/lịch sử |
| UC06.3 | Nghe thuyết minh thủ công tại điểm du lịch chính | **Một phần:** có phát TTS và báo khi giọng chưa sẵn sàng; đang đọc mô tả tổng quan thay vì trường thuyết minh riêng; chưa ghi lịch sử theo bước 5 |
| UC06.4 | Nghe thuyết minh tại điểm tham quan thành phần | **Một phần:** chọn điểm, đọc đúng nội dung từ SQLite cho Dinh Độc Lập đã có; chưa lưu lịch sử theo hậu điều kiện; 23/25 bài seed còn là giới thiệu vị trí tạm thời |
| UC06.5 | Điều khiển thuyết minh | **Có luồng chính:** phát, dừng, bấm nghe lại từ đầu và `QUEUE_FLUSH`; chưa quản lý vô hiệu hóa nút theo trạng thái đầy đủ. Tốc độ đọc là tùy chọn trong báo cáo, không tính là thiếu bắt buộc; báo cáo không bắt buộc pause/resume |
| UC07.1 | Thêm địa điểm yêu thích | **Một phần:** lưu theo tài khoản, ngăn trùng đã có; chưa lưu thời gian thêm; nút hiện là toggle, bấm khi đã thích sẽ xóa thay vì chỉ báo đã lưu như luồng thêm riêng |
| UC07.2 | Xem danh sách yêu thích | **Một phần:** truy vấn theo tài khoản, trạng thái rỗng, mở chi tiết đã có; chưa hiện ảnh/địa chỉ/mô tả và tên còn dùng mảng Java |
| UC07.3 | Xóa địa điểm yêu thích | **Có luồng chính:** xóa theo tài khoản/địa điểm và cập nhật; danh sách có xác nhận/hủy; ở chi tiết là toggle, chưa có nhánh thông báo riêng khi bản ghi không tồn tại |
| UC07.4 | Xem lịch sử tham quan | **Chưa có:** thiếu bảng, ghi lịch sử khi nghe, danh sách theo thời gian, AUTO/MANUAL và mở lại địa điểm/điểm thành phần |
| UC08.1 | Quản trị: xem danh sách điểm du lịch | **Chưa có:** danh sách người dùng ở Main không thay thế chức năng quản trị và kiểm tra quyền |
| UC08.2 | Quản trị: thêm điểm du lịch | **Chưa có:** thiếu form, kiểm tra tọa độ/bán kính, tạo ID và cập nhật danh sách/bản đồ |
| UC08.3 | Quản trị: sửa điểm du lịch | **Chưa có:** thiếu form sửa và kiểm tra dữ liệu/giữ nguyên ID |
| UC08.4 | Quản trị: xóa điểm du lịch | **Chưa có:** thiếu xác nhận, xử lý điểm thành phần và dữ liệu liên quan |
| UC08.5 | Quản trị: thêm điểm tham quan thành phần | **Chưa có:** thiếu form, chọn sơ đồ/vị trí, nội dung và thứ tự tham quan |
| UC08.6 | Quản trị: sửa điểm tham quan thành phần | **Chưa có:** thiếu sửa tên/nội dung/tọa độ/thứ tự và cập nhật sơ đồ |
| UC08.7 | Quản trị: xóa điểm tham quan thành phần | **Chưa có:** thiếu xác nhận, xóa và cập nhật marker/danh sách |
| UC08.8 | Quản trị: cập nhật nội dung thuyết minh | **Chưa có:** seed hoặc sửa bằng Database Inspector không phải giao diện quản trị có kiểm tra quyền |
| UC08.9 | Quản trị: xem danh sách tài khoản | **Chưa có:** thiếu danh sách USER, họ tên/email/trạng thái/ngày tạo và kiểm tra quyền ADMIN |
| UC08.10 | Quản trị: tìm kiếm tài khoản | **Chưa có:** thiếu tra cứu họ tên/email trong nhóm USER |
| UC08.11 | Quản trị: xem thông tin tài khoản | **Chưa có:** thiếu màn hình và các trường theo đặc tả |
| UC08.12 | Quản trị: khóa/mở khóa tài khoản | **Chưa có:** thiếu trạng thái, xác nhận, chặn đăng nhập khi khóa và ngăn tự khóa ADMIN đang sử dụng |

Bằng chứng triển khai: `MainActivity` (tra cứu/lọc/gần tôi), `BanDoActivity` (bản đồ/GPS/khoảng cách/Intent), `ChiTietDiaDiemActivity` và `ThamQuanActivity` (chi tiết/sơ đồ/TTS), các Activity tài khoản, `PhienDangNhap`, `YeuThichActivity` và schema/phương thức trong `TaiKhoanDb`. Manifest hiện có 9 Activity, không có Activity quản trị hoặc lịch sử. Việc đánh giá không chỉ dựa vào tên màn hình mà còn đối chiếu các bước xử lý và trường dữ liệu.

### 12.3. CSDL hiện tại so với ERD trong báo cáo

Số bảng hoặc tên bảng khác nhau không tự động có nghĩa sai thiết kế: tách `khu_vuc` và `thuyet_minh` có thể hợp lý nếu bảo toàn quan hệ và nghiệp vụ. Các khoảng trống thực tế là:

| Bảng trong báo cáo | Hiện thực tương ứng | Còn thiếu/khác |
| --- | --- | --- |
| `tblTaiKhoan` | `tai_khoan` và `PhienDangNhap` | `ho_ten`, `email`, `vai_tro`, `trang_thai`, `ngay_tao`; tên đăng nhập hiện thay email. Giữ mật khẩu băm, không chuyển về văn bản rõ để khớp tên trường `mat_khau` |
| `tblDiemDuLich` | `dia_diem`, ảnh trong drawable, sơ đồ qua `khu_vuc` | `ban_kinh_kich_hoat`, nội dung thuyết minh tổng quan riêng; sơ đồ đã lưu qua bảng con nhưng UI chưa dùng tên ảnh từ bảng này |
| `tblDiemThamQuan` | `diem_tham_quan → khu_vuc → dia_diem`, nội dung qua `thuyet_minh` | Chưa có `thu_tu_tham_quan` riêng (đang dùng ID); truy vấn marker chưa lọc theo PlaceID; nội dung/ảnh riêng phần lớn chưa hoàn thiện |
| `tblYeuThich` | `yeu_thich` với khóa ghép chống trùng | `thoi_gian_them`, FK tới địa điểm; không nhất thiết thêm ID tự tăng nếu khóa ghép vẫn đáp ứng nghiệp vụ |
| `tblLichSuThamQuan` | Chưa có | Toàn bộ dữ liệu liên kết tài khoản/địa điểm/điểm thành phần, thời gian và chế độ AUTO/MANUAL |

Ngoài đối chiếu ERD, lỗi migration v1/v2 → v6 ở mục 4 cần xử lý trước khi nâng cấp dữ liệu đang có. Các khai báo FK cũng cần cơ chế thực thi và cách xử lý dữ liệu liên quan khi triển khai xóa trong UC08.

### 12.4. Màn hình và phần báo cáo còn thiếu

Báo cáo liệt kê 21 màn hình, còn dự án có 9 Activity. Không dùng phép chia 9/21 để tính tiến độ: chi tiết điểm nhỏ và thuyết minh đang ghép trong `ThamQuanActivity`; chỉ đường mở ứng dụng ngoài đúng UC05.3; khám phá nằm ở trang chủ. Những màn hình ghép vẫn có thể đáp ứng yêu cầu nếu đủ luồng xử lý.

Các phần thực sự thiếu là lịch sử, hồ sơ cá nhân, cài đặt AUTO/bán kính, trang quản trị, quản lý tài khoản, quản lý địa điểm/điểm thành phần, form thêm/sửa và cập nhật nội dung. Mục đăng nhập tại Welcome/menu trang chủ vẫn chỉ Toast, làm luồng demo chưa liền mạch.


Các mục sau không nên tính thành thiếu sót bắt buộc theo báo cáo này: Google Maps SDK (OSM vẫn đáp ứng bản đồ), routing trong app (Intent ngoài được cho phép), GPS cho từng phòng (BR10 yêu cầu chọn thủ công), audio thu sẵn (báo cáo chọn TTS), tải bản đồ offline hoặc thuật toán lập lịch trình. Báo cáo nêu rõ chức năng lọc không lập kế hoạch chuyến đi; các hướng phát triển trong README cần phân biệt với tiêu chí phải hoàn thành.

## 13. Lộ trình 14 bước hoàn thành chức năng theo file Word

Thực hiện theo thứ tự dưới đây vì tài khoản, dữ liệu và lịch sử là nền tảng cho AUTO và quản trị. Mỗi bước cần hoàn thành **giao diện → xử lý → lưu dữ liệu → kiểm thử** trước khi đánh dấu đạt. Đây là kế hoạch tiếp theo, không phải danh sách chức năng đã hoàn thành; hiện trạng được đối chiếu trong ma trận 37 use case phía trên.

### Bước 1. Chốt danh sách yêu cầu và tiêu chí nghiệm thu

- Dùng đủ 37 use case trong báo cáo để lập danh sách theo dõi: luồng chính, luồng thay thế, điều kiện nghiệp vụ, kiểm thử và kết quả.
- Giữ đúng thiết kế hai cấp: GPS/AUTO cho điểm du lịch chính; chọn thủ công trên sơ đồ cho điểm thành phần.
- Giữ phạm vi bắt buộc: tài khoản, tra cứu, chi tiết, bản đồ, định vị/chỉ đường, TTS thủ công/tự động, yêu thích/lịch sử và quản trị.
- Chỉ đường được phép mở ứng dụng ngoài; không bắt buộc thay OSM, thu âm sẵn hoặc xây thuật toán lập lịch trình.

**Điều kiện đạt:** mỗi UC có tiêu chí kiểm tra cụ thể, phân biệt rõ yêu cầu bắt buộc và hướng phát triển. Chưa đánh dấu hoàn thành chỉ vì đã có màn hình.

### Bước 2. Sửa migration SQLite trước khi mở rộng CSDL

- Sửa lỗi v1/v2 lên v6 thêm trùng cột `danh_muc` trong `TaiKhoanDb`; kiểm tra cột trước khi thêm hoặc tổ chức lại migration theo từng phiên bản.
- Thiết kế migration lên phiên bản mới cho các trường/bảng sắp bổ sung, không xóa CSDL để thay thế việc nâng cấp.
- Bật thực thi khóa ngoại; xác định cách xử lý dữ liệu liên quan khi xóa địa điểm, điểm thành phần hoặc tài khoản.
- Kiểm thử cài mới và nâng cấp từ các phiên bản cũ có dữ liệu.

**Điều kiện đạt:** mở CSDL không lỗi, không thêm trùng cột, giữ nguyên tài khoản/yêu thích và dữ liệu nội dung đã có.

### Bước 3. Bổ sung schema theo ERD

| Nhóm | Dữ liệu cần hoàn thiện |
| --- | --- |
| Tài khoản | Họ tên, email duy nhất, vai trò USER/ADMIN, trạng thái, ngày tạo |
| Điểm du lịch | Bán kính kích hoạt, nội dung thuyết minh tổng quan riêng |
| Điểm thành phần | Thứ tự tham quan riêng, nội dung, ảnh và liên kết với địa điểm |
| Yêu thích | Thời gian thêm và liên kết địa điểm hợp lệ |
| Lịch sử | Tài khoản, địa điểm, điểm thành phần có thể rỗng, thời gian, AUTO/MANUAL |

- Có thể giữ các bảng `khu_vuc` và `thuyet_minh` riêng nếu bảo toàn quan hệ/nghiệp vụ của ERD.
- Giữ mật khẩu và mã khôi phục dạng băm; kiểm tra ràng buộc email, vai trò, trạng thái, bán kính dương và tọa độ hợp lệ.
- Xác định cách bổ sung email/họ tên cho tài khoản cũ, không tự tạo email giả rồi xem như người dùng đã xác nhận.

**Điều kiện đạt:** cài mới/nâng cấp tạo đủ cấu trúc, dữ liệu cũ có lộ trình chuyển đổi rõ ràng và các truy vấn trả đúng liên kết.

### Bước 4. Chuyển các màn hình sang dữ liệu động

- Bỏ mảng cố định 3 địa điểm; đọc danh sách, tên, ảnh, danh mục và tọa độ từ SQLite.
- Truy vấn khu vực/điểm thành phần theo ID địa điểm, thay vì đọc toàn bộ bảng marker.
- Truyền ID thực giữa các màn hình; bỏ giả định vị trí mảng luôn bằng ID.
- Bỏ kiểm tra bắt buộc đúng 25 điểm hoặc ID liên tiếp; dùng `thu_tu_tham_quan` để sắp xếp.
- Đọc tên ảnh sơ đồ từ CSDL; thống nhất dữ liệu giữa trang chủ, bản đồ, chi tiết, yêu thích và tham quan.

**Điều kiện đạt:** thêm địa điểm thứ tư, thêm/xóa một marker hoặc dùng ID không liên tiếp vẫn hiển thị/chuyển màn hình đúng. Các luồng quản trị sau này không cần sửa Java mỗi khi thay nội dung.

### Bước 5. Hoàn thành tài khoản và điều hướng — UC01.1–UC01.4

- Đăng ký bằng họ tên, email, mật khẩu và xác nhận; kiểm tra email duy nhất.
- Đăng nhập bằng email; kiểm tra trạng thái khóa, vai trò USER/ADMIN và mở giao diện phù hợp.
- Đăng xuất xóa phiên rồi chuyển về đăng nhập, giữ dữ liệu cá nhân đã lưu.
- Khôi phục dùng email để tìm tài khoản và giữ bước xác minh bằng mã khôi phục; bổ sung mô tả xác minh này vào Word cho khớp hiện thực.
- Nối đăng nhập tại Welcome, menu tài khoản, hồ sơ cá nhân và các nút quay lại.
- Tạo cách khởi tạo tài khoản ADMIN phục vụ demo; đăng ký người dùng thông thường không được tự chọn quyền ADMIN.

**Điều kiện đạt:** kiểm tra được dữ liệu thiếu/sai, email trùng, mật khẩu sai, tài khoản khóa, phân quyền, đăng xuất và khôi phục. USER không vào được quản trị bằng cách điều hướng trực tiếp.

### Bước 6. Hoàn thiện danh sách, tìm kiếm, lọc và gần tôi — UC02.1–UC02.4

- Danh sách động hiển thị ảnh, tên, địa chỉ, loại hình và mô tả ngắn.
- Tìm kiếm/lọc dựa trên dữ liệu CSDL; thống nhất xử lý hoa thường và dấu giữa các màn hình.
- Có trạng thái danh sách trống và không có kết quả; không đóng trang chủ chỉ vì chưa có địa điểm.
- Gần tôi phải giới hạn theo phạm vi tìm kiếm đã quy định, bỏ qua tọa độ sai và sắp xếp từ gần đến xa.
- Kết quả gần tôi có ảnh, địa chỉ, khoảng cách và mở đúng chi tiết; từ chối vị trí vẫn dùng tra cứu thường được.

**Điều kiện đạt:** kết quả chính xác với danh sách thay đổi, không có kết quả, ngoài phạm vi, tọa độ sai, GPS tắt hoặc quyền bị từ chối. Phân biệt **bán kính tìm gần tôi** với **bán kính kích hoạt AUTO**.

### Bước 7. Hoàn thiện chi tiết và tham quan — UC03.1–UC03.2, UC04.3

- Hiển thị đủ thông tin theo ID, bao gồm tọa độ/khoảng cách khi có vị trí và các nút chỉ đường, nghe, yêu thích, tham quan.
- Thống nhất xử lý địa điểm không tồn tại theo đặc tả; tránh hiển thị dữ liệu dự phòng của một địa điểm đã bị xóa.
- Bắt đầu tham quan tải đúng các điểm thuộc địa điểm đã chọn; có sơ đồ thì vẽ marker, không có sơ đồ có thể dùng danh sách thay thế.
- Chọn điểm hiển thị đúng nội dung, ảnh và thứ tự; có thông báo khi chưa có điểm thành phần.
- Kiểm tra bố cục, tọa độ tương đối và khả năng chọn marker trên nhiều kích thước màn hình.

**Điều kiện đạt:** dữ liệu/marker đúng sau khi thêm, sửa, xóa; các ID không liên tiếp không gây nhầm điểm hoặc lỗi chuyển điểm.

### Bước 8. Hoàn thiện bản đồ, định vị và khoảng cách — UC04.1–UC04.2, UC05.1–UC05.3

- Đọc marker động từ SQLite; chọn marker mở đúng thẻ thông tin, chi tiết và chỉ đường.
- Khi đã có quyền, hiển thị vị trí theo luồng bản đồ mô tả trong Word; khi chưa có quyền vẫn xem địa điểm được.
- Nhận vị trí đủ mới và cập nhật khoảng cách khi người dùng di chuyển đáng kể; quản lý đăng ký/hủy cập nhật theo vòng đời màn hình.
- Xử lý GPS tắt, chưa có tọa độ, mất mạng/tải tile thất bại, quyền bị thu hồi và không có ứng dụng mở Intent.
- Giữ chỉ đường bằng Google Maps URL theo đúng tọa độ đích; không bắt buộc routing nội bộ.

**Điều kiện đạt:** vị trí/khoảng cách cập nhật đúng, không hiển thị kết quả cũ như vị trí mới và ứng dụng không dừng bất thường ở các trường hợp lỗi.

### Bước 9. Hoàn thiện TTS thủ công — UC06.3–UC06.5

- Địa điểm chính đọc nội dung thuyết minh tổng quan riêng; điểm thành phần đọc nội dung theo ID từ CSDL.
- Hoàn thiện phát, dừng, phát lại và trạng thái nút; xử lý thiếu nội dung hoặc giọng tiếng Việt.
- Quản lý phát giữa các màn hình, dừng khi chuyển điểm/rời màn hình và giải phóng engine đúng vòng đời.
- Bổ sung, đối chiếu bài thuyết minh riêng cho 23 điểm còn dùng nội dung tạm; bổ sung ảnh phù hợp.
- Chuẩn bị callback khi bắt đầu phát thành công để bước 10 ghi lịch sử.

**Điều kiện đạt:** nghe đúng nội dung đã chọn, không phát chồng, dừng/phát lại rõ ràng và lỗi TTS không gây treo ứng dụng. Tốc độ đọc là tùy chọn trong Word; pause/resume không phải yêu cầu bắt buộc.

### Bước 10. Ghi và xem lịch sử — UC07.4, hậu điều kiện UC06.3–UC06.4

- Ghi lịch sử cho tài khoản khi TTS bắt đầu phát thành công; không ghi chỉ vì mở màn hình hoặc nhấn nút nhưng phát thất bại.
- Lưu đúng địa điểm, điểm thành phần nếu có, thời gian và chế độ MANUAL; thiết kế dùng chung cho AUTO ở bước 11.
- Tạo màn hình lịch sử theo tài khoản, sắp mới nhất trước, hiện đủ tên/thời gian/chế độ.
- Chọn mục lịch sử mở đúng nội dung; xử lý danh sách rỗng và nội dung đã bị xóa.
- Quy định cách xử lý khi chưa đăng nhập và khi thay tài khoản để tránh gán lịch sử sai người.

**Điều kiện đạt:** lịch sử được lưu sau khi phát, vẫn còn khi mở lại ứng dụng và tài khoản khác không đọc được lịch sử riêng.

### Bước 11. Hoàn thành AUTO — UC06.1–UC06.2

1. Tạo công tắc bật/tắt và lưu trạng thái AUTO.
2. Khi bật, kiểm tra quyền và dịch vụ vị trí; thể hiện rõ nếu chưa thể hoạt động.
3. Nhận cập nhật vị trí và tính khoảng cách tới các điểm du lịch chính có dữ liệu hợp lệ.
4. Lọc điều kiện `khoảng cách ≤ bán kính kích hoạt`; nhiều điểm hợp lệ thì chọn gần nhất.
5. Kiểm tra trạng thái đã phát để không phát lặp liên tục cùng địa điểm; xác định rõ phạm vi lưu trạng thái này.
6. Phát nội dung đúng địa điểm, ghi trạng thái khi phát thành công và lưu lịch sử AUTO.
7. Khi tắt, ngừng kiểm tra tự động; vẫn cho phép nghe thủ công. Điều phối AUTO/MANUAL để không phát chồng.

**Điều kiện đạt:** thử vị trí mô phỏng ngoài/vào/đúng ranh giới bán kính, hai phạm vi giao nhau, đứng yên, mất GPS, thiếu nội dung, lỗi TTS và bật/tắt AUTO. Điểm thành phần trong nhà không tự kích hoạt bằng GPS.

### Bước 12. Hoàn thiện yêu thích — UC07.1–UC07.3

- Lưu thời gian thêm và ngăn trùng theo tài khoản/địa điểm.
- Danh sách đọc thông tin từ CSDL, có ảnh, tên, địa chỉ, mô tả và mở đúng chi tiết.
- Phân biệt luồng thêm với xóa; khi đã lưu thì xử lý theo đặc tả thêm, không vô tình xóa do cùng một thao tác.
- Hoàn thiện thông báo, xác nhận/hủy và trường hợp bản ghi/địa điểm không còn tồn tại.
- Đồng bộ trạng thái giữa chi tiết và danh sách sau khi quay lại.

**Điều kiện đạt:** hai tài khoản có dữ liệu độc lập; thêm trùng không tạo dòng mới; hủy xóa không đổi dữ liệu; đăng xuất/đăng nhập lại vẫn còn danh sách.

### Bước 13. Xây dựng quản trị — UC08.1–UC08.12

Thực hiện theo thứ tự:

1. Trang quản trị và kiểm tra quyền ở cả điều hướng lẫn thao tác dữ liệu.
2. Xem/thêm/sửa/xóa điểm du lịch (UC08.1–UC08.4), gồm form tọa độ, bán kính và nội dung.
3. Thêm/sửa/xóa điểm thành phần (UC08.5–UC08.7), gồm địa điểm chứa điểm, tọa độ sơ đồ, ảnh và thứ tự.
4. Cập nhật nội dung thuyết minh hai cấp (UC08.8), kiểm tra dữ liệu và dùng nội dung mới khi nghe tiếp.
5. Danh sách/tìm kiếm/chi tiết tài khoản USER (UC08.9–UC08.11), gồm họ tên, email, trạng thái, vai trò và ngày tạo theo từng màn hình.
6. Khóa/mở khóa có xác nhận (UC08.12); chặn tài khoản khóa đăng nhập và ngăn ADMIN tự khóa tài khoản đang sử dụng.

- Form phải kiểm tra dữ liệu bắt buộc, tọa độ, bán kính dương và vị trí marker hợp lệ.
- Sửa giữ nguyên ID; hủy giữ nguyên dữ liệu. Xóa có xác nhận và xử lý liên kết điểm thành phần, thuyết minh, yêu thích/lịch sử theo chính sách đã chọn.
- Danh sách, bản đồ và sơ đồ phải phản ánh dữ liệu sau thao tác quản trị.

**Điều kiện đạt:** ADMIN thực hiện đủ 12 UC, USER không được gọi thao tác quản trị; dữ liệu thay đổi đúng, không có liên kết mồ côi và không cần sửa mã nguồn để thêm nội dung.



### Thứ tự bắt đầu

**Sửa migration → hoàn thiện schema → bỏ dữ liệu cố định → hoàn thành tài khoản**, sau đó tiếp tục lần lượt các bước 6–14. Chưa đặt thời hạn hoặc phần trăm hoàn thành khi chưa có lịch làm việc và ước lượng khối lượng; cập nhật tiến độ bằng các điều kiện nghiệm thu đã đạt.
