# APP_CK – Ứng dụng hỗ trợ tham quan du lịch

**Cập nhật: 23/10/2026.** README mô tả working tree hiện tại, gồm các thay đổi chưa commit. Ứng dụng hiện có **5 địa điểm**, **13 Activity** và **SQLite phiên bản 8**. Nội dung từng địa điểm/tọa độ dưới đây được mô tả theo dữ liệu trong code, chưa phải xác minh thực địa. Phần đối chiếu file Word và lộ trình triển khai tiếp được giữ ở cuối tài liệu.

## 1. Tổng quan và phạm vi

| Nội dung | Hiện trạng |
| --- | --- |
| Tên / phiên bản / package | `APP_CK` / `1.0` (versionCode 1) / `com.example.app_ck` |
| Nền tảng | Android Java, giao diện XML và giao diện dựng bằng Java |
| Kiến trúc | Activity xử lý giao diện/nghiệp vụ; `TaiKhoanDb` truy cập SQLite; các lớp Java cung cấp dữ liệu seed |
| Dữ liệu | Cục bộ trên thiết bị, chưa có backend/đồng bộ |
| Bản đồ | OpenStreetMap qua osmdroid; chỉ đường dùng Google Maps URL |
| Thuyết minh | TextToSpeech tiếng Việt, phát/dừng thủ công từ trang chủ, chi tiết hoặc tham quan |
| Tài khoản | Tên đăng nhập, mật khẩu, mã khôi phục; phiên SharedPreferences; yêu thích/đăng xuất |
| Phần chưa có | AUTO theo vị trí, lịch sử, tiến độ tham quan lưu lâu dài, quản trị và phân quyền |

### 5 địa điểm khởi tạo trong SQLite v8

| ID | Địa điểm | Địa chỉ trong seed | Vĩ độ, kinh độ | Drawable |
| ---: | --- | --- | --- | --- |
| 0 | Dinh Độc Lập | 135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh | 10.7770, 106.6953 | `anh_dinh_doc_lap` |
| 1 | Bảo tàng Chứng tích Chiến tranh | 28 Võ Văn Tần, TP. Hồ Chí Minh | 10.7795, 106.6921 | `anh_bao_tang_chung_tich` |
| 2 | Làng Văn hóa – Du lịch các dân tộc Việt Nam | Đồng Mô, Sơn Tây, Hà Nội | 21.06452, 105.49827 | `anh_lang_van_hoa` |
| 3 | Hệ thống Nhà tù Côn Đảo | Côn Đảo, TP. Hồ Chí Minh | 8.68907, 106.61484 | `anh_nha_tu_con_dao` |
| 4 | Văn Miếu – Quốc Tử Giám | 58 Quốc Tử Giám, Hà Nội | 21.02930, 105.83575 | `anh_van_mieu` |

ID 2 hiện được dùng cho Làng Văn hóa. Migration v7 dọn dữ liệu địa điểm cũ trước khi migration v8 seed 3 địa điểm mới. Giá trị tọa độ Java dự phòng trong chi tiết có thể khác seed SQLite; thông tin SQLite được ưu tiên khi đọc thành công. Ảnh của các địa điểm mới đã có trong drawable, dù một số chú thích code còn mô tả ảnh thay thế.

## 2. Chức năng và màn hình hiện tại

| Nhóm | Phần đã hiện thực | Giới hạn |
| --- | --- | --- |
| Welcome | Hai ảnh địa điểm, giới thiệu tính năng, mở trang chủ | Đăng nhập tại Welcome vẫn chỉ Toast |
| Trang chủ | Đọc 5 địa điểm; đồng bộ 5 thẻ nổi bật và 2 thẻ gợi ý; mở chi tiết/bản đồ/yêu thích | Mảng/thẻ và điều hướng còn cố định theo ID 0–4 |
| Tìm kiếm | Trang chủ tìm tên + địa chỉ không dấu; bản đồ tìm tên không dấu | Chưa tìm mô tả/hoạt động; bản đồ chọn kết quả khớp đầu tiên |
| Danh mục | Đọc lại `danh_muc` từ SQLite, chuẩn hóa và lọc thẻ; báo khi rỗng | Lọc trong Java, chưa áp dụng đồng thời cho gợi ý/gần tôi |
| Gần tôi | Xin quyền, lấy vị trí, tính và sắp khoảng cách, mở chi tiết | Chưa giới hạn bán kính; kết quả chủ yếu tên/km; API dưới 30 dùng vị trí đã biết gần nhất |
| Thuyết minh trang chủ | Nút Thuyết minh và banner trải nghiệm mở danh sách 5 địa điểm, phát mô tả từ SQLite, có dừng đọc | Chưa có AUTO hoặc ghi lịch sử |
| Chi tiết | Tên, ảnh, danh mục, địa chỉ, mô tả, xem thêm/thu gọn, yêu thích, nghe/dừng và mở tham quan | Dữ liệu Java dự phòng; thư viện 1 ảnh/địa điểm; giờ mở cửa chỉ nhắc kiểm tra nguồn chính thức |
| Bản đồ | 5 marker, 5 nút chọn nhanh, ảnh lấy theo tên resource trong SQLite, vị trí của tôi, khoảng cách và chỉ đường | Chưa có routing nội bộ/offline riêng; một số nhãn XML còn cũ |
| Tham quan | Màn hình riêng cho cả 5 địa điểm, danh sách, điểm trước/tiếp theo, TTS; 4 địa điểm có ảnh sơ đồ | Làng Văn hóa mới có danh sách; dữ liệu chưa thống nhất trong một schema điểm thành phần |
| Yêu thích | Lấy ID và tên địa điểm từ SQLite, mở chi tiết, bỏ yêu thích, trạng thái rỗng, đăng xuất | Giới hạn ID 0–4; thiếu ảnh/địa chỉ/mô tả và thời gian thêm |
| Tài khoản/cá nhân | Đăng ký, đăng nhập, khôi phục bằng mã, phiên đăng nhập | Chưa có email/họ tên/vai trò/trạng thái; menu tài khoản, tab cá nhân/khám phá/gợi ý còn Toast |

Không có chức năng lập lộ trình chuyến đi riêng. Nút/banners thuyết minh trên trang chủ đã hoạt động, khác README cũ ghi chỉ hiện thông báo.

### Bản đồ OpenStreetMap

`BanDoActivity` sử dụng osmdroid `6.1.20`, tải tile từ `https://tile.openstreetmap.org/`, có user agent và nhãn nguồn OSM. Zoom từ **4 đến 19**, khởi đầu **5.5** để phù hợp danh sách địa điểm ở nhiều khu vực; chọn nhanh/GPS có thể đưa zoom về 17.

- Đọc tên, địa chỉ, tọa độ và tên ảnh từ SQLite; kiểm tra tọa độ hữu hạn, đúng giới hạn, không đồng thời bằng 0.
- Chạm marker/nút nhanh cập nhật thẻ thông tin và bật xem chi tiết/chỉ đường.
- Tìm tên không dấu qua Search trên bàn phím; chuẩn hóa chữ thường, dấu, `đ` và khoảng trắng.
- Vị trí của tôi dùng `MyLocationNewOverlay`; khoảng cách là đường chim bay, không phải quãng đường đi thực tế.
- `onPause()` tắt lớp định vị; `onResume()` khôi phục nếu đã bật; `onDestroy()` giải phóng MapView/helper.
- Không cần Google Maps API key cho cấu hình hiện tại; tile chưa cache cần mạng. Chỉ đường mở ứng dụng/trình duyệt ngoài.

## 3. Các màn hình tham quan và thuyết minh

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

Khi mở tham quan, ứng dụng yêu cầu đủ 25 marker, ID tuần tự 1–25, mã tầng 1–6 và tọa độ hợp lệ. Nếu dữ liệu thiếu/sai, màn hình thông báo rồi đóng. Chuyển điểm sẽ dừng giọng đọc, đổi tầng nếu cần và cập nhật nội dung. Đổi tầng đồng bộ điểm được chọn sang một điểm thuộc tầng mới, tránh nội dung và sơ đồ lệch tầng.

### Điều hướng vào màn hình tham quan

`ChiTietDiaDiemActivity.moManHinhThamQuan()` chọn Activity theo ID: 0 → `ThamQuanActivity`, 1 → `BaoTangThamQuanActivity`, 2 → `LangVanHoaThamQuanActivity`, 3 → `ConDaoThamQuanActivity`, 4 → `VanMieuThamQuanActivity`.

Với Dinh Độc Lập, extra `idMarker` hợp lệ được ưu tiên; nếu không có, bốn thẻ giới thiệu được tìm theo tên trong SQLite, có từ khóa dự phòng. Cổng/khuôn viên mở từ điểm đầu. Các Activity mới chủ yếu nhận `idMarker`, nhưng luồng mở từ chi tiết chưa truyền ID này cho mọi thẻ; cần kiểm tra để tránh các thẻ cùng mở điểm mặc định.

### Phạm vi các địa điểm còn lại

| Địa điểm | Nội dung seed | Sơ đồ và giới hạn |
| --- | --- | --- |
| Bảo tàng | 13 điểm trong `bao_tang_marker` | 3 tầng (0–2), marker có tọa độ; chọn tầng/điểm đồng bộ, có trước/tiếp và TTS |
| Làng Văn hóa | 5 mục: tổng quan và các cụm làng I–IV trong `lang_van_hoa_diem` | Danh sách và TTS; chưa có sơ đồ/marker, ảnh đầu màn hình còn dùng icon gallery dù drawable địa điểm đã có |
| Côn Đảo | 7 mục trong `con_dao_diem` | Ảnh `so_do_nha_tu_con_dao`; 6 tọa độ seed trong `con_dao_toa_do`, chỉ thêm khi ảnh đúng tỷ lệ gốc 1366×970; điểm Bảo tàng Côn Đảo chưa có tọa độ |
| Văn Miếu | 16 điểm trong `van_mieu_diem` | Ảnh `so_do_van_mieu_chinh_thuc`; 16 tọa độ seed trong `van_mieu_toa_do`; bản debug cho nhấn giữ để căn điểm và xác nhận lưu SQLite |

Côn Đảo và Văn Miếu đều có lớp hỗ trợ ảnh/sơ đồ riêng bên trong Activity. Tọa độ là tỷ lệ trên ảnh, không phải GPS. Khi không có ảnh hoặc tọa độ phù hợp, danh sách/thuyết minh vẫn là cách truy cập nội dung. Các bảng riêng được tạo và seed khi mở màn hình tương ứng, không đồng loạt trong `TaiKhoanDb.onCreate()`.

### Nguồn nội dung và giọng đọc

- Trang chủ/chi tiết đọc mô tả tổng quan; chưa có trường thuyết minh riêng cho điểm du lịch chính.
- Dinh Độc Lập đọc bảng `thuyet_minh`; seed chỉ có bài riêng cho điểm 1 và 4, 23 điểm còn lại giới thiệu vị trí/tầng và thông báo đang cập nhật.
- Bảo tàng đọc `noi_dung`; ba địa điểm mới đọc `mo_ta` từ các bảng riêng.
- TTS dùng tiếng Việt và `QUEUE_FLUSH`; các màn hình dừng/giải phóng giọng đọc theo vòng đời. Chưa có AUTO, audio thu sẵn, lưu lịch sử nghe hoặc tốc độ đọc trên UI.
- Thanh tiến trình thể hiện thứ tự điểm đang chọn, chưa phải phần trăm các điểm đã thực sự tham quan.

## 4. SQLite và luồng dữ liệu

Tệp CSDL: **`du_lich_tai_khoan.db`**, phiên bản hiện tại: **8**. `TaiKhoanDb` kế thừa `SQLiteOpenHelper`. Cài mới khởi tạo 6 bảng, seed 5 địa điểm, 6 khu vực, 25 điểm và 25 nội dung thuyết minh. Đây là số lượng theo mã khởi tạo v8, chưa truy vấn trên thiết bị trong lần cập nhật tài liệu này; các bảng tham quan riêng được khởi tạo khi mở từng Activity.

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

ID địa điểm cố định 0–4; ID khu vực 1–6 đồng thời được dùng làm mã tầng; ID điểm Dinh Độc Lập 1–25. Nhiều luồng vẫn phụ thuộc quy ước này, chưa hỗ trợ thêm địa điểm tùy ý chỉ bằng cách thêm hàng vào CSDL.

### Mức độ sử dụng SQLite

| Thành phần | Đọc từ SQLite | Phần còn cố định trong Java/XML |
| --- | --- | --- |
| Trang chủ | Tên, tọa độ, tên ảnh, địa chỉ/mô tả/danh mục; đọc mô tả cho TTS trang chủ | Số thẻ, ID, nhãn phụ, các lựa chọn bộ lọc và gợi ý |
| Bản đồ | Tên, địa chỉ, tọa độ và ảnh từ tên resource SQLite | Số địa điểm và điều hướng còn cố định 5 ID |
| Chi tiết | Tên, địa chỉ, mô tả, danh mục, tọa độ, tên drawable | Giá trị dự phòng và các thẻ điểm nổi bật |
| Sơ đồ Dinh Độc Lập | ID/tên/tầng/tọa độ marker; thuyết minh theo điểm | Tên/ảnh các tầng dùng mảng; `ten_anh_so_do` trong bảng `khu_vuc` chưa điều khiển ảnh hiển thị |
| Tham quan ngoài Dinh Độc Lập | Các bảng riêng theo Activity, xem bảng dưới | Ảnh/tên và nội dung seed vẫn khai báo trong Java |
| Yêu thích | ID theo tài khoản, tên địa điểm từ `dia_diem`, thêm/xóa | Chưa có thẻ ảnh/địa chỉ/mô tả; giới hạn 5 ID |

`DuLieuDinhDocLap` và `NoiDungThuyetMinhDinhDocLap` vẫn là nguồn seed. Phần lớn thao tác seed dùng `CONFLICT_IGNORE`, nên sửa nội dung seed Java không tự cập nhật bản ghi đã tồn tại. `themMoTaVaDanhMuc()` chỉ điền trường trống/null để giữ nội dung đã có.

### Các bảng riêng tạo khi mở màn hình

| Bảng | Cột | Seed dự kiến |
| --- | --- | --- |
| `bao_tang_marker` | `id`, `tang`, `x`, `y`, `ten`, `noi_dung` | 13 |
| `lang_van_hoa_diem` | `id`, `ten`, `mo_ta` | 5 |
| `con_dao_diem` | `id`, `ten`, `mo_ta` | 7 |
| `con_dao_toa_do` | `id`, `x_ty_le`, `y_ty_le` | 6, phụ thuộc ảnh gốc phù hợp |
| `van_mieu_diem` | `id`, `ten`, `mo_ta` | 16 |
| `van_mieu_toa_do` | `id`, `x_ty_le`, `y_ty_le` | 16, có thể căn lại trong debug |

Như vậy có 6 bảng lõi và tối đa 6 bảng riêng kể trên khi đã mở các màn hình tương ứng. Chúng chưa được liên kết về một bảng điểm thành phần chung bằng PlaceID/FK, cần chuẩn hóa trước khi mở rộng quản trị.

### Các mốc nâng cấp CSDL

| Phiên bản đích | Thay đổi trong `onUpgrade()` |
| ---: | --- |
| 2 | Thêm `ma_khoi_phuc_bam` vào `tai_khoan` |
| 3 | Tạo bảng du lịch và seed 5 địa điểm |
| 4 | Seed 6 khu vực và 25 marker |
| 5 | Tạo bổ sung bảng nếu thiếu và seed thuyết minh |
| 6 | Thêm cột `danh_muc` nếu chưa có, điền danh mục/mô tả trống |
| 7 | Dọn ID 2 cũ và dữ liệu liên kết; giữ ID 0/1 và tài khoản |
| 8 | Seed thêm Làng Văn hóa (ID 2), Côn Đảo (ID 3), Văn Miếu (ID 4); điền mô tả/danh mục còn trống |

**Thứ tự nâng cấp:** kiểm tra cột `danh_muc` trước khi thêm; nếu từ bản dưới v7 thì dọn ID 2 cũ trước, sau đó nhánh `< 8` seed đủ 5 địa điểm. ID 2 hiện là Làng Văn hóa, không được xem mọi hàng ID 2 là dữ liệu cần xóa. Seed dùng `CONFLICT_IGNORE`; mô tả/danh mục chỉ điền trường trống nên không tự ghi đè nội dung đã sửa. Các tọa độ/nhãn cũ cũng có thể được giữ lại, cần kiểm tra dữ liệu thực tế khi nâng cấp.

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

Manifest khai báo **13 Activity**, chỉ `WelcomeActivity` được export làm launcher. Có 8 layout màn hình XML; `YeuThichActivity` dựng giao diện bằng Java qua các hàm hỗ trợ trong `DangNhapActivity`.

```text
Welcome --Bắt đầu--> Main
Main --chọn địa điểm/tìm kiếm/gợi ý/gần tôi--> ChiTietDiaDiem
Main --Bản đồ--> BanDo --Xem chi tiết--> ChiTietDiaDiem
Main --Yêu thích (tiện ích hoặc tab)--> YeuThich
ChiTietDiaDiem --ID 0--> ThamQuan
ChiTietDiaDiem --ID 1--> BaoTangThamQuan
ChiTietDiaDiem --ID 2--> LangVanHoaThamQuan
ChiTietDiaDiem --ID 3--> ConDaoThamQuan
ChiTietDiaDiem --ID 4--> VanMieuThamQuan
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
| `MainActivity.java` | Tải 5 địa điểm, đồng bộ thẻ, tìm kiếm/lọc, gợi ý, gần tôi và điều hướng |
| `BanDoActivity.java` | OpenStreetMap, marker, tìm kiếm, vị trí, khoảng cách và liên kết chỉ đường |
| `ChiTietDiaDiemActivity.java` | Đồng bộ chi tiết từ CSDL, dữ liệu dự phòng, ảnh, thuyết minh, yêu thích và mở tham quan |
| `ThamQuanActivity.java` | Tải/kiểm tra marker Dinh Độc Lập, vẽ sơ đồ, chuyển điểm và đọc thuyết minh |
| `BaoTangThamQuanActivity.java` | Sơ đồ bảo tàng 13 marker trên 3 tầng và thuyết minh từ bảng `bao_tang_marker` |
| `LangVanHoaThamQuanActivity.java` | Danh sách 5 khu vực, tạo/đọc bảng riêng và TTS |
| `ConDaoThamQuanActivity.java` | Danh sách 7 điểm, sơ đồ/tọa độ Côn Đảo và TTS |
| `VanMieuThamQuanActivity.java` | 16 điểm, sơ đồ và căn tọa độ trong debug, TTS |
| `TaiKhoanDb.java` | Tạo/nâng cấp/seed CSDL, xử lý tài khoản, yêu thích và truy vấn nội dung |
| `DiaDiem.java` | Đối tượng dữ liệu địa điểm, gồm cả mô tả/danh mục/tên ảnh |
| `DuLieuDinhDocLap.java` | Model điểm, dữ liệu seed 25 marker, quy ước tầng và tên ảnh sơ đồ |
| `NoiDungThuyetMinhDinhDocLap.java` | Nội dung seed thuyết minh theo ID |
| `DangNhapActivity.java` | Đăng nhập, mở đăng ký/khôi phục và các hàm dựng UI dùng chung |
| `DangKyActivity.java` | Kiểm tra dữ liệu đăng ký và hiển thị mã khôi phục |
| `QuenMatKhauActivity.java` | Đổi mật khẩu bằng mã và hiển thị mã thay thế |
| `PhienDangNhap.java` | Đọc/lưu/xóa phiên SharedPreferences |
| `YeuThichActivity.java` | Danh sách riêng theo tài khoản, bỏ yêu thích và đăng xuất |

Các màn hình kế thừa `BaseActivity`, ngoại trừ `BanDoActivity` kế thừa trực tiếp `AppCompatActivity` và tự xử lý thanh điều hướng.

### Tài nguyên và cấu hình

| Đường dẫn | Nội dung |
| --- | --- |
| `app/src/main/res/layout/` | 8 layout màn hình và `styles_chi_tiet_bo_sung.xml`; tệp style bổ sung hiện nằm trong `layout`, chưa được tham chiếu |
| `app/src/main/res/drawable/` | 61 tệp: 2 JPG, 14 PNG (ảnh địa điểm và sơ đồ), 45 XML nền/vector icon |
| `app/src/main/res/mipmap-*/` | Icon launcher theo mật độ màn hình và adaptive icon |
| `app/src/main/res/values/`, `values-night/` | App name, màu và theme `Theme.Material3.DayNight.NoActionBar`; nhiều màu/chuỗi vẫn viết trực tiếp trong layout/Java |
| `app/src/main/res/xml/` | Quy tắc backup/extraction mẫu |
| `app/src/main/AndroidManifest.xml` | Activity, quyền và cấu hình ứng dụng |
| `app/src/test/`, `app/src/androidTest/` | Unit test mẫu, test package và 4 test migration viết cho v7 (chưa cập nhật v8) |
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

1. **Cập nhật kiểm thử migration lên v8**, kiểm tra 5 địa điểm, đúng ý nghĩa ID 2 qua từng phiên bản, giữ tài khoản/yêu thích và các bảng tham quan riêng.
2. **Hoàn thiện điều hướng** đăng nhập tại Welcome và menu tài khoản; triển khai cá nhân/lịch sử/cài đặt hoặc điều chỉnh các mục giới thiệu cho khớp chức năng.
3. **Thống nhất dữ liệu** giữa trang chủ, bản đồ, chi tiết, yêu thích và tham quan; bỏ phụ thuộc mảng 5 ID, giữ ảnh bản đồ/tên yêu thích đã đọc CSDL và chuyển tên ảnh sơ đồ/danh sách sang dữ liệu chung, truy vấn marker theo khu vực/địa điểm thay vì đọc toàn bộ bảng.
4. **Bổ sung nội dung và media** cho 23 điểm còn thiếu bài riêng, ảnh từng điểm, giờ tham quan; đối chiếu tư liệu lịch sử, địa chỉ, tọa độ. Báo cáo đã được đối chiếu ở mục 12; nội dung lịch sử của từng điểm vẫn cần kiểm chứng bằng tư liệu chính thức.
5. **Lưu hoạt động cá nhân**: thêm bảng/màn hình lịch sử nghe/tham quan và tiến độ thực tế. Hiện chưa có các bảng này.
6. **Hoàn thiện bản đồ và vị trí**: giữ tìm kiếm không dấu đã có; sửa nhãn phạm vi/số điểm, xử lý mất mạng/GPS, kiểm tra khoảng cách theo cập nhật vị trí và bán kính gần tôi. Routing/offline riêng là hướng phát triển, không bắt buộc theo đặc tả đã đối chiếu.
7. **Thuyết minh theo vị trí**: bổ sung bán kính/cơ chế kích hoạt và chống lặp theo UC06.1–UC06.2; hiện chưa có geofencing hoặc phát tự động.
8. **Quản trị và tài khoản**: bổ sung vai trò/trạng thái và quản lý nội dung, tài khoản theo UC08 của báo cáo; hiện chưa có Activity quản trị, vai trò ADMIN hoặc chức năng khóa/mở tài khoản.
9. **Chuẩn hóa UI/tài nguyên**: đưa chuỗi/màu về resources, thống nhất xử lý thanh điều hướng và giao diện sáng/tối; chuyển style bổ sung về `values` nếu thực sự dùng.
10. **Bổ sung kiểm thử có ý nghĩa**, tập trung migration, tài khoản, yêu thích, truy vấn và tìm kiếm; chạy demo trên thiết bị cho bản đồ/GPS/TTS trước khi kết luận hoàn tất chức năng.

## 12. Đối chiếu với báo cáo Word

Dựa trên đặc tả đã đọc từ `Nhom15_BaoCao.docx` và code hiện tại, vẫn có **15/37 UC chưa có luồng sử dụng**: UC06.1, UC06.2, UC07.4 và 12 UC quản trị. 22 UC còn lại có hiện thực liên quan nhưng nhiều mục chưa đủ đặc tả; phép đếm không phải tỷ lệ hoàn thành. Các sơ đồ/nội dung mới cải thiện phần tham quan, chưa thay thế AUTO, lịch sử hoặc quản trị.

### 12.2. Ma trận 37 use case

“Có luồng chính” nghĩa là tìm thấy xử lý tương ứng trong mã nguồn; không đồng nghĩa đã kiểm thử đạt toàn bộ luồng thay thế và yêu cầu phi chức năng. “Một phần/khác đặc tả” nghĩa là đã có chức năng liên quan nhưng còn thiếu yêu cầu được nêu trong báo cáo.

| Use case | Chức năng trong báo cáo | Đánh giá và phần cần làm tiếp |
| --- | --- | --- |
| UC01.1 | Đăng ký tài khoản | **Một phần/khác đặc tả:** code dùng tên đăng nhập, mật khẩu và mã khôi phục; báo cáo yêu cầu họ tên, email duy nhất; CSDL chưa có họ tên/email/ngày tạo |
| UC01.2 | Đăng nhập | **Một phần/khác đặc tả:** xác thực và lưu phiên đã có; chưa đăng nhập bằng email, xác định USER/ADMIN và chuyển màn hình theo quyền; chưa kiểm tra khóa tài khoản theo UC08.12 |
| UC01.3 | Đăng xuất | **Một phần/khác đặc tả:** có xác nhận và xóa phiên trong yêu thích; báo cáo yêu cầu chuyển về đăng nhập, hiện chỉ cập nhật màn hình yêu thích |
| UC01.4 | Quên mật khẩu | **Một phần/khác đặc tả:** đã đổi mật khẩu bằng mã khôi phục; báo cáo mô tả nhập email, không mô tả mã khôi phục. Cần thống nhất đặc tả với cơ chế xác minh hiện tại, không bỏ xác minh chỉ để khớp câu chữ |
| UC02.1 | Xem danh sách điểm du lịch | **Một phần:** đọc SQLite và chọn chi tiết được; còn cố định 5 thẻ/ID, thẻ không hiển thị đủ địa chỉ và mô tả ngắn như đặc tả; thiếu dữ liệu thì trang chủ đóng thay vì danh sách trống |
| UC02.2 | Tìm kiếm điểm du lịch | **Có luồng chính:** trang chủ tìm tên/địa chỉ, bản đồ tìm tên; cả hai chuẩn hóa hoa thường và dấu, xử lý rỗng/không có kết quả |
| UC02.3 | Lọc điểm du lịch | **Có luồng chính:** đọc `danh_muc` từ SQLite, lọc thẻ và báo khi rỗng; còn giới hạn 5 thẻ và không lọc các khu vực gợi ý/gần tôi |
| UC02.4 | Xem địa điểm gần vị trí hiện tại | **Một phần:** có quyền, GPS, khoảng cách, sắp xếp và mở chi tiết; chưa giới hạn theo phạm vi tìm kiếm BR02.4-4, chưa bỏ qua tọa độ sai; kết quả chỉ có tên/km, thiếu ảnh/địa chỉ |
| UC03.1 | Xem chi tiết điểm du lịch chính | **Một phần:** đủ các nút chính cho 5 địa điểm; còn Java dự phòng, chưa hiện tọa độ/khoảng cách tại chi tiết; ID ngoài phạm vi đang quay về ID 0 |
| UC03.2 | Bắt đầu tham quan | **Có luồng chính:** mở đúng Activity cho 5 địa điểm; còn ánh xạ cố định, một số thẻ chưa truyền ID điểm để mở đúng điểm được chọn |
| UC04.1 | Xem bản đồ khu vực | **Có luồng chính:** OSM, 5 marker, 5 nút nhanh, zoom 4–19, ảnh từ SQLite, lớp vị trí khi bật; nhãn XML còn “2 điểm”/TP. Hồ Chí Minh |
| UC04.2 | Chọn marker điểm du lịch | **Có luồng chính:** chọn đúng địa điểm, hiện tên/địa chỉ/khoảng cách khi có GPS, mở chi tiết/chỉ đường |
| UC04.3 | Xem sơ đồ tham quan nội bộ | **Có luồng chính:** Dinh Độc Lập/Bảo tàng/Văn Miếu/Côn Đảo có sơ đồ; Làng Văn hóa dùng danh sách thay thế được đặc tả cho phép; cần kiểm chứng tọa độ/khả năng chọn trên thiết bị |
| UC05.1 | Xác định vị trí hiện tại | **Có luồng chính:** xin quyền, lấy vị trí, hiển thị trên bản đồ khi bật; API 24–29 ở trang chủ chỉ dùng vị trí đã biết gần nhất, chưa đảm bảo độ mới |
| UC05.2 | Xem khoảng cách đến địa điểm | **Một phần:** tính/hiển thị đã có; chưa tự cập nhật khoảng cách khi vị trí thay đổi đáng kể theo yêu cầu phi chức năng; hiện tính lại khi lấy vị trí/chọn địa điểm |
| UC05.3 | Chỉ đường đến địa điểm | **Có luồng chính:** Intent Google Maps theo tọa độ và xử lý không có ứng dụng; báo cáo cho phép dùng ứng dụng bản đồ ngoài, không bắt buộc routing nội bộ |
| UC06.1 | Bật/tắt thuyết minh tự động | **Chưa có:** thiếu giao diện, lưu trạng thái AUTO và theo dõi vị trí theo trạng thái |
| UC06.2 | Nghe thuyết minh tự động tại điểm du lịch chính | **Chưa có:** thiếu bán kính, kiểm tra khoảng cách, chọn điểm gần nhất, chống phát lặp và lưu trạng thái/lịch sử |
| UC06.3 | Nghe thuyết minh thủ công tại điểm du lịch chính | **Một phần:** phát mô tả từ SQLite ở trang chủ/chi tiết; chưa có nội dung tổng quan riêng và ghi lịch sử theo bước 5 |
| UC06.4 | Nghe thuyết minh tại điểm tham quan thành phần | **Một phần:** cả 5 nơi có TTS theo điểm, đọc SQLite ở các bảng tương ứng; chưa ghi lịch sử, nội dung 23/25 điểm Dinh Độc Lập còn tạm |
| UC06.5 | Điều khiển thuyết minh | **Có luồng chính:** phát, dừng, bấm nghe lại từ đầu và `QUEUE_FLUSH`; chưa quản lý vô hiệu hóa nút theo trạng thái đầy đủ. Tốc độ đọc là tùy chọn trong báo cáo, không tính là thiếu bắt buộc; báo cáo không bắt buộc pause/resume |
| UC07.1 | Thêm địa điểm yêu thích | **Một phần:** lưu theo tài khoản, ngăn trùng đã có; chưa lưu thời gian thêm; nút hiện là toggle, bấm khi đã thích sẽ xóa thay vì chỉ báo đã lưu như luồng thêm riêng |
| UC07.2 | Xem danh sách yêu thích | **Một phần:** truy vấn ID/tên theo CSDL và tài khoản, xử lý rỗng/mở chi tiết; chưa có ảnh/địa chỉ/mô tả, giới hạn 5 ID |
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

Bằng chứng triển khai: `MainActivity` (tra cứu/lọc/gần tôi), `BanDoActivity` (bản đồ/GPS/khoảng cách/Intent), `ChiTietDiaDiemActivity` và `ThamQuanActivity` (chi tiết/sơ đồ/TTS), các Activity tài khoản, `PhienDangNhap`, `YeuThichActivity` và schema/phương thức trong `TaiKhoanDb`. Manifest hiện có 13 Activity, không có Activity quản trị hoặc lịch sử. Việc đánh giá không chỉ dựa vào tên màn hình mà còn đối chiếu các bước xử lý và trường dữ liệu.

### 12.3. CSDL hiện tại so với ERD trong báo cáo

Số bảng hoặc tên bảng khác nhau không tự động có nghĩa sai thiết kế: tách `khu_vuc` và `thuyet_minh` có thể hợp lý nếu bảo toàn quan hệ và nghiệp vụ. Các khoảng trống thực tế là:

| Bảng trong báo cáo | Hiện thực tương ứng | Còn thiếu/khác |
| --- | --- | --- |
| `tblTaiKhoan` | `tai_khoan` và `PhienDangNhap` | `ho_ten`, `email`, `vai_tro`, `trang_thai`, `ngay_tao`; tên đăng nhập hiện thay email. Giữ mật khẩu băm, không chuyển về văn bản rõ để khớp tên trường `mat_khau` |
| `tblDiemDuLich` | `dia_diem`, ảnh trong drawable, sơ đồ qua `khu_vuc` | `ban_kinh_kich_hoat`, nội dung thuyết minh tổng quan riêng; sơ đồ đã lưu qua bảng con nhưng UI chưa dùng tên ảnh từ bảng này |
| `tblDiemThamQuan` | `diem_tham_quan → khu_vuc → dia_diem`, nội dung qua `thuyet_minh` | Chưa có `thu_tu_tham_quan` riêng (đang dùng ID); truy vấn marker chưa lọc theo PlaceID; nội dung/ảnh riêng phần lớn chưa hoàn thiện |
| `tblYeuThich` | `yeu_thich` với khóa ghép chống trùng | `thoi_gian_them`, FK tới địa điểm; không nhất thiết thêm ID tự tăng nếu khóa ghép vẫn đáp ứng nghiệp vụ |
| `tblLichSuThamQuan` | Chưa có | Toàn bộ dữ liệu liên kết tài khoản/địa điểm/điểm thành phần, thời gian và chế độ AUTO/MANUAL |

Ngoài đối chiếu ERD, migration v8 ở mục 4 cần được kiểm thử lại với ID 2 hiện là Làng Văn hóa; các test v7 chưa phù hợp phạm vi 5 địa điểm. Các khai báo FK cũng cần cơ chế thực thi và cách xử lý dữ liệu liên quan khi triển khai xóa trong UC08.

### 12.4. Màn hình và phần báo cáo còn thiếu

Báo cáo liệt kê 21 màn hình, còn dự án có 13 Activity. Không dùng phép chia 13/21 để tính tiến độ: chi tiết điểm nhỏ và thuyết minh đang ghép trong `ThamQuanActivity`; chỉ đường mở ứng dụng ngoài đúng UC05.3; khám phá nằm ở trang chủ. Những màn hình ghép vẫn có thể đáp ứng yêu cầu nếu đủ luồng xử lý.

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

### Bước 2. Cập nhật kiểm thử migration trước khi mở rộng CSDL

- Cập nhật kỳ vọng kiểm thử lên v8/5 địa điểm; kiểm tra nâng v1/v2/v6/v7 và ý nghĩa ID 2 sau khi dọn dữ liệu cũ rồi seed Làng Văn hóa.
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

- Bỏ mảng cố định 5 địa điểm; đọc danh sách, tên, ảnh, danh mục và tọa độ từ SQLite.
- Truy vấn khu vực/điểm thành phần theo ID địa điểm, thay vì đọc toàn bộ bảng marker.
- Truyền ID thực giữa các màn hình; bỏ giả định vị trí mảng luôn bằng ID.
- Bỏ kiểm tra bắt buộc đúng 25 điểm hoặc ID liên tiếp; dùng `thu_tu_tham_quan` để sắp xếp.
- Đọc tên ảnh sơ đồ từ CSDL; thống nhất dữ liệu giữa trang chủ, bản đồ, chi tiết, yêu thích và tham quan.

**Điều kiện đạt:** thêm địa điểm thứ sáu, thêm/xóa một marker hoặc dùng ID không liên tiếp vẫn hiển thị/chuyển màn hình đúng. Các luồng quản trị sau này không cần sửa Java mỗi khi thay nội dung.

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



### Bước 14. Nghiệm thu chức năng và cập nhật báo cáo

- Chạy lại build, kiểm thử nghiệp vụ/migration và demo trên thiết bị với đủ 37 UC cùng các luồng thay thế.
- Hoàn thiện phần hiện thực, ảnh màn hình và kết quả kiểm thử trong Word; cập nhật ERD, màn hình, hạn chế và hướng phát triển theo bản ứng dụng cuối.
- Chỉ đánh dấu đạt khi có bằng chứng; không dùng các kết quả kiểm thử v7 để nghiệm thu v8.

**Điều kiện đạt:** dữ liệu, chức năng, demo và báo cáo thống nhất; các thiếu sót được ghi đúng hiện trạng.

### Thứ tự bắt đầu

**Kiểm thử migration v8 → hoàn thiện schema → bỏ dữ liệu cố định → hoàn thành tài khoản**, sau đó tiếp tục lần lượt các bước 6–14. Chưa đặt thời hạn hoặc phần trăm hoàn thành khi chưa có lịch làm việc và ước lượng khối lượng; cập nhật tiến độ bằng các điều kiện nghiệm thu đã đạt.


## 14. Build, kiểm thử và các điểm cần lưu ý — 23/10/2026

### Kết quả hiện tại

| Kiểm tra | Kết quả / phạm vi |
| --- | --- |
| `assembleDebug testDebugUnitTest` | **BUILD SUCCESSFUL** trong lần cập nhật này |
| Unit test | 1 test mẫu `addition_isCorrect`, không bao phủ nghiệp vụ |
| `TaiKhoanDbMigrationTest` | Có 4 test, nhưng vẫn kỳ vọng version 7 và 2 địa điểm; không còn phù hợp version 8/5 địa điểm |
| Kiểm thử thiết bị lần này | Chưa chạy; kết quả 4 test đạt ngày 22/10 là của v7, không chứng minh v8 đạt |
| UI/GPS/TTS và tọa độ các sơ đồ mới | Mô tả theo mã nguồn, chưa demo/xác minh trên thiết bị trong lần này |

### Sai lệch còn thấy trong mã nguồn

- Một số chú thích vẫn ghi SQLite v7/hai địa điểm/điểm mới chưa có sơ đồ dù code đã là v8 và có Activity riêng.
- `activity_ban_do.xml` vẫn ghi “2 điểm” và “Khám phá TP. Hồ Chí Minh”, trong khi dữ liệu đã có 5 địa điểm ở nhiều khu vực.
- Làng Văn hóa đã có ảnh drawable nhưng đầu màn hình tham quan vẫn dùng icon gallery.
- Ảnh/tọa độ seed có thể không tự cập nhật vì dùng `CONFLICT_IGNORE`; tọa độ đã căn tay được giữ lại. Cần kiểm tra dữ liệu thật sau khi thay sơ đồ.
- `ChiTietDiaDiemActivity` nhận ID không hợp lệ hiện quay về ID 0; một số thẻ giới thiệu chưa truyền `idMarker` cho màn hình tương ứng.
- Chưa có AUTO, lịch sử, phân quyền/quản trị; còn nhiều giới hạn cố định 5 ID và số lượng marker theo từng Activity.

### Cách chạy kiểm tra

Chuẩn bị JDK 21, Android SDK 36.1 và đồng bộ Gradle trong Android Studio. Cấu hình `sdk.dir` trong `local.properties` theo máy đang dùng; lần đầu cần mạng tải dependency. Chạy tại thư mục gốc:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
.\gradlew.bat installDebug
# Chỉ chạy bộ migration sau khi cập nhật kỳ vọng và dữ liệu kiểm thử cho v8:
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.app_ck.TaiKhoanDbMigrationTest'
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Báo cáo unit test: `app/build/reports/tests/testDebugUnitTest/index.html`. Không dùng build thành công để kết luận mọi chức năng/migration đã đạt.

### Đối chiếu CSDL thực tế

Mở Database Inspector, chọn đúng tiến trình và vào trang chủ/bản đồ để mở kết nối. Các bảng riêng chỉ xuất hiện sau khi đã mở màn hình tham quan tương ứng. Nếu kết nối đóng, bật Keep database connections open rồi mở lại màn hình có truy vấn.

```sql
PRAGMA user_version;
SELECT id, ten, danh_muc, vi_do, kinh_do, ten_anh FROM dia_diem ORDER BY id;
SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;
```

Cài mới dự kiến `user_version = 8`, `dia_diem` có 5 hàng ID 0–4; số lượng điểm và các bảng riêng được mô tả ở mục 3–4. Việc cập nhật README lần này chỉ sửa tài liệu, không sửa các lỗi hoặc test cũ nêu trên.
