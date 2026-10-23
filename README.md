# APP_CK – Ứng dụng hỗ trợ tham quan du lịch

Ứng dụng Android hỗ trợ tra cứu điểm du lịch, xem bản đồ và sơ đồ tương tác, nghe thuyết minh tiếng Việt, lưu yêu thích và quản lý hồ sơ cá nhân.

**Cập nhật ngày 23/10/2026 theo mã nguồn trong workspace, bao gồm các thay đổi chưa commit.** Hiện có **5 địa điểm, 71 điểm tham quan thành phần, 17 Activity và SQLite v12**. Dữ liệu tài khoản và nội dung được lưu cục bộ; chưa có backend, lịch sử tham quan hoặc quản trị.

README mô tả chức năng đang có và đối chiếu với báo cáo **`Nhom15_BaoCao.docx`** do nhóm cung cấp. Báo cáo Word không nằm trong repository. Trạng thái “có luồng chính” bên dưới không đồng nghĩa đã hoàn thành toàn bộ đặc tả hoặc đã kiểm chứng thực địa.

**Tiến độ tổng thể ước tính: khoảng 60% so với toàn bộ yêu cầu trong báo cáo Word**, tính cả quản trị và phân quyền. Đây là ước lượng theo mức độ hiện thực chức năng, chưa phải tỷ lệ nghiệm thu chính thức. Căn cứ và phần còn thiếu được trình bày tại mục 13; các bước hoàn thiện ở mục 15.

## 1. Tổng quan và phạm vi

| Nội dung | Hiện trạng |
| --- | --- |
| Tên / phiên bản | APP_CK / `1.0`, versionCode `1` |
| Namespace / applicationId | `com.example.app_ck` |
| Giao diện | Android Java, XML, một số sơ đồ và thẻ dựng bằng Java/Canvas |
| Dữ liệu | SQLite trên thiết bị, dữ liệu khởi tạo từ mã nguồn |
| Bản đồ | OpenStreetMap qua osmdroid; chỉ đường bằng URL Google Maps |
| Giọng đọc | Android TextToSpeech tiếng Việt |
| Chế độ tham quan | Chọn thủ công, demo tự động, AUTO theo GPS khi màn hình tham quan đang mở |
| Tài khoản | Đăng ký, đăng nhập, mã khôi phục, hồ sơ, đổi mật khẩu, đăng xuất |
| Cá nhân hóa | Yêu thích theo tài khoản; gợi ý theo danh mục của các điểm đã lưu |

Ứng dụng cho phép khách khám phá mà chưa đăng nhập. Danh sách yêu thích và thao tác thay đổi hồ sơ yêu cầu đăng nhập. Dữ liệu chưa đồng bộ giữa các thiết bị.

## 2. Công nghệ và cấu hình

| Thành phần | Cấu hình trong dự án |
| --- | --- |
| Android SDK | minSdk `24`, targetSdk `36`, compileSdk `36.1` |
| Mã nguồn Java | sourceCompatibility và targetCompatibility `11` |
| Gradle / Android Gradle Plugin | `9.3.1` / `9.1.1` |
| AndroidX | AppCompat `1.8.0`, Activity `1.13.0`, ConstraintLayout `2.2.2` |
| Material Components | `1.14.0` |
| osmdroid | `6.1.20` |
| Kiểm thử | JUnit `4.13.2`, AndroidX JUnit `1.3.0`, Espresso `3.7.0` |

Cấu hình nằm tại [app/build.gradle.kts](app/build.gradle.kts), [libs.versions.toml](gradle/libs.versions.toml) và [Gradle Wrapper](gradle/wrapper/gradle-wrapper.properties). Mức Java 11 của mã nguồn khác với JDK chạy Gradle; môi trường build đã dùng JDK 21.

## 3. Mở dự án và chạy ứng dụng

1. Mở thư mục gốc bằng Android Studio; dùng JDK 21 cho Gradle.
2. Cài Android SDK tương ứng `compileSdk 36.1`, Platform Tools và đồng bộ Gradle.
3. Khai báo SDK trong `local.properties` theo máy đang dùng; không đưa đường dẫn riêng của máy vào repository.
4. Chạy module `app` trên thiết bị/emulator Android 7.0 trở lên.
5. Cài hoặc bật giọng đọc **Tiếng Việt** trong phần Text-to-speech của thiết bị để nghe thuyết minh.

Lệnh PowerShell tại thư mục gốc:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

Lệnh cuối cần thiết bị/emulator đang kết nối và được cấp quyền ADB. APK debug nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

Bản đồ hiện tại không cần Google Maps API key. Tile bản đồ chưa cache cần Internet. Demo không cần quyền vị trí; GPS thật cần bật dịch vụ vị trí và cấp **Vị trí chính xác**. Không yêu cầu quyền vị trí nền.

## 4. Năm địa điểm và nguồn tọa độ

Trang chủ, danh sách khám phá, bản đồ, chi tiết và yêu thích dùng ID địa điểm thống nhất.

| ID | Địa điểm | Địa chỉ trong dữ liệu khởi tạo | Vĩ độ | Kinh độ |
| ---: | --- | --- | ---: | ---: |
| 0 | Dinh Độc Lập | 135 Nam Kỳ Khởi Nghĩa, TP. Hồ Chí Minh | 10.77702 | 106.69540 |
| 1 | Bảo tàng Chứng tích Chiến tranh | 28 Võ Văn Tần, TP. Hồ Chí Minh | 10.779475 | 106.692132 |
| 2 | Đại Nội Huế | Đường 23 Tháng 8, Huế | 16.4689726 | 107.5781266 |
| 3 | Hệ thống Nhà tù Côn Đảo | Côn Đảo, TP. Hồ Chí Minh | 8.68907 | 106.61484 |
| 4 | Văn Miếu – Quốc Tử Giám | 58 Quốc Tử Giám, Hà Nội | 21.02861 | 105.83556 |

Tọa độ tập trung ở [KhoToaDoThamQuan.java](app/src/main/java/com/example/app_ck/KhoToaDoThamQuan.java), được cập nhật vào SQLite khi cài mới/nâng cấp từ trước v11. Đây là **mốc đại diện địa điểm**, chưa chứng minh là cổng vào và không phải GPS từng phòng.

Nguồn đã dùng để đối chiếu tọa độ: [Dinh Độc Lập trên OpenStreetMap](https://www.openstreetmap.org/way/39598493), [Bảo tàng Chứng tích Chiến tranh](https://en.wikipedia.org/wiki/War_Remnants_Museum), [Hoàng Thành Huế trên OpenStreetMap](https://www.openstreetmap.org/node/5182931945), [Nhà tù Côn Đảo](https://mapcarta.com/W715077456), [Văn Miếu](https://en.wikipedia.org/wiki/Temple_of_Literature,_Hanoi). Các nguồn này không thay thế khảo sát GPS tại chỗ.

Đại Nội Huế dùng ID `2` từ SQLite v10. Bưu điện và Làng Văn hóa dân tộc không còn trong danh sách địa điểm hiện hành; mã migration xử lý dữ liệu cũ vẫn được giữ để nâng cấp an toàn.

## 5. Trang chủ, khám phá và gợi ý

| Khu vực | Hành vi hiện tại |
| --- | --- |
| Điểm đến nổi bật | Hiển thị đủ 5 địa điểm, bấm thẻ mở đúng chi tiết |
| Gợi ý trên trang chủ | Hai thẻ Dinh Độc Lập và Bảo tàng, cùng kích thước trong nhóm |
| Tìm kiếm trang chủ | Tìm theo tên và địa chỉ, hỗ trợ nhập không dấu |
| Danh mục trang chủ | Lọc nhóm điểm nổi bật; chưa kết hợp đồng thời với mọi nhóm gợi ý/gần tôi |
| Gần tôi | Xin quyền, lấy vị trí, tính khoảng cách và sắp xếp từ gần đến xa |
| Thuyết minh trang chủ | Chọn trong 5 địa điểm, đọc mô tả từ SQLite và dừng đọc |
| Khám phá | Danh sách từ SQLite; tìm không dấu theo tên/địa chỉ/mô tả, lọc danh mục, mở chi tiết |
| Tab Gợi ý | Loại địa điểm đã yêu thích, ưu tiên điểm có chủ đề danh mục tương tự |

Gợi ý do [GoiYDiaDiem.java](app/src/main/java/com/example/app_ck/GoiYDiaDiem.java) xếp hạng theo danh mục. Khi chưa có yêu thích, danh sách theo thứ tự ID; khi đã lưu hết, có trạng thái hết gợi ý và chuyển sang xem tất cả. Dữ liệu được đọc lại khi quay về màn hình. Chưa có gợi ý bằng AI hoặc dựa trên lịch sử hành vi.

Kích thước dùng chung tại [dimens.xml](app/src/main/res/values/dimens.xml): thẻ nổi bật rộng **205dp**, ảnh cao **125dp**; thẻ gợi ý trang chủ rộng **172dp**, ảnh cao **104dp**. Giới hạn dòng tiêu đề/phụ đề giúp các thẻ trong cùng nhóm có chiều cao đồng đều.

**Giới hạn Gần tôi:** chưa lọc theo bán kính tìm kiếm như Word; API dưới 30 dùng vị trí đã biết gần nhất, cần kiểm tra thêm độ mới của vị trí. Khoảng cách hiển thị là đường chim bay.

## 6. Bản đồ và chi tiết địa điểm

[BanDoActivity.java](app/src/main/java/com/example/app_ck/BanDoActivity.java) đọc tên, ảnh và tọa độ từ SQLite, hiển thị **5 marker và 5 nút chọn nhanh**. Tìm kiếm tên không dấu chọn kết quả khớp đầu tiên; chạm marker cập nhật thẻ thông tin rồi có thể mở chi tiết/chỉ đường.

Bản đồ dùng tile `https://tile.openstreetmap.org/`, có nhãn nguồn OpenStreetMap và user agent. Mức zoom 4–19, khởi đầu 5.5 để thấy các điểm ở nhiều vùng; chọn điểm có thể phóng tới mức 17. Vị trí của tôi dùng lớp định vị osmdroid; lớp này dừng khi màn hình tạm ngưng và giải phóng khi đóng.

Chỉ đường mở Google Maps bằng URL qua ứng dụng/trình duyệt ngoài. Chưa có bộ tính tuyến đường hoặc tải bản đồ offline riêng trong app.

[ChiTietDiaDiemActivity.java](app/src/main/java/com/example/app_ck/ChiTietDiaDiemActivity.java) hiển thị ảnh, tên, danh mục, địa chỉ, mô tả có mở rộng/thu gọn; hỗ trợ yêu thích, nghe/dừng, bản đồ và bắt đầu tham quan. Mỗi địa điểm hiện có một ảnh chính, chưa có bộ sưu tập ảnh đầy đủ. Giờ mở cửa chưa lấy từ nguồn cập nhật chính thức.

## 7. Sơ đồ tham quan và nội dung thuyết minh

| Địa điểm | Activity tham quan | Số điểm | Phân khu |
| --- | --- | ---: | --- |
| Dinh Độc Lập | `ThamQuanActivity` | 25 | Tầng 1, 2, 3, 4, hầm, trệt |
| Bảo tàng Chứng tích Chiến tranh | `BaoTangThamQuanActivity` | 13 | 3 tầng/mức sơ đồ |
| Đại Nội Huế | `DaiNoiHueThamQuanActivity` | 10 | Sơ đồ quần thể |
| Hệ thống Nhà tù Côn Đảo | `ConDaoThamQuanActivity` | 7 | Các điểm trong quần thể |
| Văn Miếu – Quốc Tử Giám | `VanMieuThamQuanActivity` | 16 | Sơ đồ di tích |
| **Tổng** | | **71** | |

Các màn hình có chọn điểm, trước/tiếp, nội dung thuyết minh và nghe/dừng bằng TTS tiếng Việt. Dinh Độc Lập có đủ ID 1–25; số điểm ở 6 khu vực lần lượt là **4, 6, 4, 2, 4, 5**. Chuyển điểm đồng bộ tầng và nội dung.

Cả 5 sơ đồ hỗ trợ kéo, chụm hai ngón và nút thu/phóng/đặt lại, mức phóng 1–5 lần. Bốn sơ đồ dùng [SoDoThuPhongLayout.java](app/src/main/java/com/example/app_ck/SoDoThuPhongLayout.java); Đại Nội Huế dùng xử lý thu phóng riêng. Tọa độ chạm được quy đổi để tiếp tục chọn marker sau khi phóng.

Tọa độ `x/y` tương đối trên ảnh **khác tọa độ GPS WGS84**. Chức năng GPS chọn/highlight marker, chưa chiếu vị trí thật thành chấm di chuyển chính xác trên mọi ảnh sơ đồ. Văn Miếu có công cụ chỉnh tọa độ ảnh trong bản debug, không thay thế luồng ghi GPS thật.

**Nội dung còn cần hoàn thiện:** [NoiDungThuyetMinhDinhDocLap.java](app/src/main/java/com/example/app_ck/NoiDungThuyetMinhDinhDocLap.java) mới có bài riêng cho ID 1 và 4; 23 điểm còn lại trả về mẫu giới thiệu kèm thông báo đang cập nhật. Có đủ marker và phát được TTS chưa đồng nghĩa đủ bài thuyết minh đã kiểm chứng. Nội dung các địa điểm còn lại cũng cần đối chiếu tư liệu, kiểm tra phát âm và ghi nguồn.

## 8. Demo tự động và GPS thật

[ThamQuanTuDong.java](app/src/main/java/com/example/app_ck/ThamQuanTuDong.java) dùng chung cho 5 màn hình thông qua `NguonThamQuanTuDong`. Bảng **Hướng dẫn theo vị trí** có hai luồng riêng.

### Demo

1. Mở chi tiết → bắt đầu tham quan → bấm **Chạy demo**.
2. Nhãn hiển thị **DEMO · Vị trí mô phỏng**. Ứng dụng mô phỏng đến marker, chờ ổn định rồi đọc bài tương ứng.
3. Đọc xong chờ khoảng 1,8 giây rồi chuyển sang điểm kế tiếp; có nút chuyển mốc demo để bỏ qua bài đang đọc.
4. Chọn điểm/tầng thủ công, dừng AUTO hoặc rời màn hình sẽ dừng demo.

Demo dùng tọa độ mô phỏng trong bộ nhớ, không xin quyền vị trí và không lưu các tọa độ này làm GPS thật. Tuyến demo lần lượt đi qua các điểm của từng địa điểm; cần TTS tiếng Việt để nghe và tự chuyển bài sau khi đọc xong.

### GPS thật

1. Bấm **GPS thật**, bật dịch vụ vị trí và cấp quyền vị trí chính xác. Giao diện hướng dẫn mở cài đặt nếu bị từ chối quyền hoặc tắt định vị.
2. Theo dõi vĩ độ, kinh độ, sai số và khoảng cách tới địa điểm. Chế độ này không chấp nhận vị trí được đánh dấu giả lập.
3. Có sẵn mốc giới thiệu chung, bán kính **250 m** quanh tọa độ đại diện của từng địa điểm. Vào vùng hợp lệ và đủ ổn định sẽ phát giới thiệu chung.
4. Muốn AUTO theo marker: đến đúng vị trí thực, chọn marker/tầng trên sơ đồ → chờ GPS tốt → chọn bán kính → **Ghi GPS tại mốc đang chọn** → kiểm tra và xác nhận lưu.
5. Lần sau bật GPS thật, chọn đúng tầng và tới gần mốc đã ghi để tự chọn marker/phát bài. Mốc cụ thể được ưu tiên hơn giới thiệu chung.

Marker chưa được khảo sát không có sẵn GPS riêng. Các lựa chọn bán kính là **20/30/40/60/80 m**, mặc định **40 m**; bảng GPS chấp nhận bán kính 15–100 m. Chỉ ghi khi còn gần địa điểm: tối đa 1,5 km, riêng quần thể Côn Đảo 8 km. Ghi lại cùng marker cập nhật tọa độ cũ.

### Điều kiện và giới hạn

[BoChonDiemTuDong.java](app/src/main/java/com/example/app_ck/BoChonDiemTuDong.java) chỉ nhận vị trí hợp lệ, sai số **≤25 m**, tuổi **≤15 giây**, thời điểm tăng dần; cần ổn định trong vùng ít nhất **2,5 giây**. Cả khoảng cách cộng sai số phải nằm trong bán kính phát. Nếu hai mốc quá gần để phân biệt bằng sai số, bộ chọn không tự đoán điểm.

Mỗi marker chỉ tự phát một lần trong một lượt AUTO; có thể nghe lại thủ công hoặc bắt đầu lượt mới. GPS không xác định tầng, vì vậy Dinh/Bảo tàng cần chọn tầng. Rời màn hình dừng TTS, callback và cập nhật vị trí; quay lại cần bật chế độ lại. Chưa có dịch vụ AUTO chạy nền hoặc tiến độ tham quan lưu lâu dài.

**Chưa khảo sát thực địa toàn bộ 71 marker.** Demo và kiểm thử logic không chứng minh GPS trong nhà phân biệt chính xác từng phòng.

## 9. Đăng ký, đăng nhập và khôi phục

| Luồng | Hành vi |
| --- | --- |
| Welcome → Bắt đầu | Vào trang chủ với trạng thái khách |
| Welcome → Đăng nhập | Đăng nhập thành công vào trang chủ, hủy quay lại Welcome |
| Đăng ký | Tạo tài khoản cục bộ, cấp mã khôi phục; quay lại đăng nhập với tên đã điền |
| Đăng nhập từ chức năng | Trả kết quả về màn hình gọi và tiếp tục thao tác phù hợp |
| Quên mật khẩu | Dùng tên đăng nhập + mã khôi phục + mật khẩu mới; đổi mã khôi phục sau thành công |
| Đăng xuất | Xác nhận rồi xóa phiên, giữ hồ sơ và yêu thích trong SQLite |

Tên đăng nhập gồm 3–30 ký tự chữ ASCII, số hoặc dấu gạch dưới, được chuẩn hóa chữ thường/bỏ khoảng trắng đầu cuối. Mật khẩu tối thiểu 8 ký tự, cần khớp xác nhận.

Mật khẩu và mã khôi phục được băm bằng `PBKDF2WithHmacSHA1`, 120.000 vòng, đầu ra 256 bit, salt ngẫu nhiên 16 byte. Phiên đăng nhập dùng `SharedPreferences` qua `PhienDangNhap`, không lưu mật khẩu rõ. Mã khôi phục chỉ hiển thị để người dùng lưu; mã cũ hết hiệu lực sau khôi phục/đổi mật khẩu.

Đăng nhập hiện dùng **tên đăng nhập**, chưa dùng email như Word. Email hồ sơ là thông tin liên hệ tùy chọn, chưa xác minh, không duy nhất và không dùng gửi mã khôi phục. Tài khoản cũ thiếu mã có thể đổi mật khẩu sau đăng nhập để nhận mã mới.

## 10. Yêu thích và hồ sơ cá nhân

**Yêu thích:** khách mở tab được chuyển sang đăng nhập; nếu hủy sẽ thấy giao diện yêu cầu đăng nhập. Tài khoản đã đăng nhập xem thẻ ảnh/tên/danh mục/địa chỉ của các điểm mình lưu, mở chi tiết hoặc bỏ lưu. Khóa ghép tài khoản–địa điểm ngăn lưu trùng. Đăng nhập để tiếp tục lưu từ chi tiết không vô tình xóa một điểm đã lưu trước đó. Chưa lưu thời điểm thêm yêu thích.

**Cá nhân:** khách có nút đăng nhập/đăng ký; sau đăng nhập hiển thị tên, `@tên_đăng_nhập`, avatar chữ cái, số yêu thích thực và tổng địa điểm. Chưa có ảnh đại diện tải lên hoặc số lượt tham quan từ lịch sử.

| Chức năng | Xử lý |
| --- | --- |
| Thông tin cá nhân | Sửa họ tên tối đa 80 ký tự, email tùy chọn tối đa 254 ký tự và kiểm tra định dạng; tên đăng nhập giữ nguyên |
| Đổi mật khẩu | Kiểm tra mật khẩu hiện tại, mật khẩu mới khác cũ, đủ độ dài và khớp xác nhận; đổi salt/hash và mã khôi phục |
| Sau đổi mật khẩu | Giữ phiên hiện tại, hiển thị mã khôi phục mới để sao lưu |
| Đăng xuất | Có xác nhận; chuyển giao diện khách, không xóa dữ liệu tài khoản |
| Thông tin ứng dụng | Giải thích dữ liệu cục bộ và mã khôi phục |

Biểu mẫu dùng `BieuMauTaiKhoanActivity`, ghi nhớ ID tài khoản đang sửa và kiểm tra lại phiên trước khi lưu. Hồ sơ được lưu trong SQLite v12, đọc lại khi mở màn hình.

## 11. Thanh điều hướng dùng chung

[view_thanh_dieu_huong_duoi.xml](app/src/main/res/layout/view_thanh_dieu_huong_duoi.xml) và [ThanhDieuHuongDuoi.java](app/src/main/java/com/example/app_ck/ThanhDieuHuongDuoi.java) dùng cho Trang chủ, Yêu thích, Cá nhân và Khám phá/Gợi ý.

| Thứ tự | Tab | Đích đến |
| ---: | --- | --- |
| 1 | Khám phá | Danh sách tìm kiếm/lọc địa điểm |
| 2 | Yêu thích | Bộ sưu tập của tài khoản hoặc yêu cầu đăng nhập |
| 3 | Trang chủ | Nút nổi ở giữa, trở về `MainActivity` |
| 4 | Cá nhân | Hồ sơ và thao tác tài khoản |
| 5 | Gợi ý | Chế độ gợi ý trong `KhamPhaActivity` |

Tab đang chọn có màu/chữ đậm và trạng thái hỗ trợ đọc màn hình. Điều hướng tái sử dụng màn hình phù hợp, tránh xếp chồng nhiều bản sao khi đổi tab. `BaseActivity` xử lý khoảng đệm thanh trạng thái/điều hướng hệ thống; nút tài khoản đầu trang chủ cũng mở Cá nhân.

## 12. SQLite và nâng cấp dữ liệu

Helper chính: [TaiKhoanDb.java](app/src/main/java/com/example/app_ck/TaiKhoanDb.java). Tên tệp **`du_lich_tai_khoan.db`**, phiên bản hiện tại **12**.

| Bảng | Mục đích |
| --- | --- |
| `tai_khoan` | Tên đăng nhập, salt/hash mật khẩu, hash mã khôi phục, họ tên và email |
| `yeu_thich` | Liên kết tài khoản–địa điểm, khóa chính ghép |
| `dia_diem` | Năm địa điểm, mô tả, danh mục, ảnh và tọa độ |
| `khu_vuc` | Sáu khu vực của Dinh Độc Lập |
| `diem_tham_quan` | 25 marker Dinh Độc Lập, tọa độ ảnh và liên kết khu vực |
| `thuyet_minh` | Nội dung thuyết minh các điểm Dinh Độc Lập |
| `gps_diem_tham_quan` | GPS marker theo `(dia_diem_id, marker_id)`, tầng, bán kính, sai số, thời điểm ghi |

Bảng riêng được tạo khi mở màn hình tương ứng: `bao_tang_marker`; `dai_noi_hue_diem`/`dai_noi_hue_toa_do`; `con_dao_diem`/`con_dao_toa_do`; `van_mieu_diem`/`van_mieu_toa_do`. Vì vậy chưa mở các tour thì Database Inspector có thể chưa thấy các bảng này.

Các mốc nâng cấp đáng chú ý:

| Phiên bản | Thay đổi |
| --- | --- |
| v2–v6 | Mã khôi phục, địa điểm, khu vực/marker, thuyết minh, danh mục |
| v7–v9 | Bổ sung dữ liệu và dọn địa điểm đã ngừng sử dụng; v9 loại dữ liệu Làng Văn hóa |
| v10 | Bổ sung Đại Nội Huế với ID 2 |
| v11 | Đồng bộ tọa độ 5 địa điểm, thêm bảng GPS marker |
| v12 | Thêm `ho_ten`, `email`; kiểm tra cột trước khi thêm, giữ dữ liệu tài khoản/yêu thích/GPS |

Dữ liệu seed dùng cách chèn tránh ghi đè dữ liệu đã tồn tại ở các phần tương ứng. Do đó sửa bài thuyết minh trong Java không bảo đảm tự cập nhật bài đã lưu trên máy cũ; cần migration cập nhật nội dung có chủ đích. Không xóa dữ liệu ứng dụng khi cần giữ tài khoản, yêu thích và mốc GPS đã ghi.

Database Inspector báo **`(closed)` / No Databases Available** khi kết nối SQLite không còn mở trong tiến trình đang quan sát. Chạy bản debug, chọn đúng thiết bị/tiến trình, mở màn hình có truy cập CSDL và refresh; trạng thái này không tự chứng minh tệp DB đã bị mất.

## 13. Đối chiếu báo cáo Word

Báo cáo mô tả **8 nhóm, 37 use case con**. Bảng dưới đánh giá theo mã nguồn hiện tại; các yêu cầu về lịch sử, tài khoản và phạm vi AUTO được xét riêng, không tính tỷ lệ hoàn thành từ số màn hình hoặc số test.

### Ước lượng tiến độ tổng thể

**Toàn bộ dự án hiện đạt khoảng 60% so với Word; phần còn lại ước tính khoảng 40%.** Nếu chỉ xét luồng người dùng chính, không tính quản trị, mức độ hiện thực khoảng 75–80%; con số này không thay thế tỷ lệ tổng thể.

Căn cứ đánh giá:

- Trong 37 use case con, **12 use case quản trị (UC08.1–UC08.12)** và **lịch sử tham quan (UC07.4)** chưa được triển khai.
- **24 use case còn lại đã có phần hiện thực hoặc luồng chính**, tương đương khoảng 65% số use case có chức năng liên quan. Đây không phải 24 use case đã hoàn thành đầy đủ.
- Các phần đã có vẫn còn khác biệt hoặc thiếu so với Word: tài khoản theo email, phạm vi tìm kiếm Gần tôi, AUTO cấp địa điểm chính và ghi lịch sử khi nghe. Ngoài ra cần hoàn thiện nội dung thuyết minh và kiểm chứng GPS thực địa.
- Vì vậy dùng mốc **khoảng 60%** để theo dõi tiến độ hiện tại. Mức này là đánh giá định tính, không phải phép tính có trọng số khối lượng cho từng use case; cần cập nhật sau mỗi đợt triển khai và nghiệm thu.

### Trạng thái từng use case

| Use case | Trạng thái và phần cần bổ sung |
| --- | --- |
| UC01.1 – Đăng ký | Có luồng thật; chưa đăng ký họ tên/email bắt buộc và email duy nhất theo Word |
| UC01.2 – Đăng nhập | Có xác thực cục bộ; chưa đăng nhập email, kiểm tra vai trò/trạng thái khóa |
| UC01.3 – Đăng xuất | Có xác nhận, xóa phiên và giữ yêu thích; chưa có lịch sử để kiểm chứng bảo toàn |
| UC01.4 – Quên mật khẩu | Có khôi phục bằng mã; khác luồng nhận diện tài khoản bằng email trong Word |
| UC02.1 – Danh sách | Có danh sách 5 địa điểm và mở chi tiết |
| UC02.2 – Tìm kiếm | Có tìm không dấu; phạm vi tìm giữa trang chủ/bản đồ/Khám phá chưa hoàn toàn giống nhau |
| UC02.3 – Lọc | Có lọc danh mục; cần thống nhất tổ hợp tìm kiếm, danh mục và trạng thái rỗng |
| UC02.4 – Gần vị trí | Có khoảng cách/sắp xếp; thiếu giới hạn phạm vi tìm kiếm theo BR02.4-4 |
| UC03.1 – Chi tiết | Có luồng chính; cần hoàn thiện nội dung, ảnh và nguồn thông tin |
| UC03.2 – Bắt đầu tham quan | Có mở đúng sơ đồ của cả 5 địa điểm |
| UC04.1 – Bản đồ khu vực | Có OSM, 5 marker, thu phóng và vị trí |
| UC04.2 – Chọn marker | Có thẻ thông tin và mở chi tiết |
| UC04.3 – Sơ đồ nội bộ | Có 5 sơ đồ, chọn marker, tầng và thu phóng; cần nghiệm thu vị trí từng marker |
| UC05.1 – Vị trí hiện tại | Có xin quyền/định vị; cần kiểm thử thêm mất quyền, vị trí cũ, GPS yếu |
| UC05.2 – Khoảng cách | Có đường chim bay; cần kiểm soát độ mới vị trí nhất quán giữa các màn hình |
| UC05.3 – Chỉ đường | Có mở Google Maps ngoài; cần kiểm thử thiết bị không có ứng dụng bản đồ |
| UC06.1 – Bật/tắt AUTO | Có trong màn hình tham quan; chưa có điều phối AUTO theo dõi toàn bộ địa điểm ở cấp ứng dụng |
| UC06.2 – AUTO tại điểm chính | Có vùng phát giới thiệu cho từng tour đang mở; thiếu lịch sử, bán kính theo dữ liệu từng địa điểm và lựa chọn giữa nhiều địa điểm chính |
| UC06.3 – Nghe tại điểm chính | Có nghe/dừng/phát lại; chưa ghi lịch sử MANUAL |
| UC06.4 – Nghe điểm thành phần | Có chọn điểm và TTS; chưa ghi lịch sử, còn nội dung mẫu |
| UC06.5 – Điều khiển | Có phát/dừng/phát lại; cần nghiệm thu tránh phát chồng giữa các màn hình và vòng đời |
| UC07.1 – Thêm yêu thích | Có theo tài khoản, chống trùng, tiếp tục lưu sau đăng nhập |
| UC07.2 – Danh sách yêu thích | Có chặn khách, thẻ dữ liệu thật, rỗng và mở chi tiết |
| UC07.3 – Xóa yêu thích | Có bỏ lưu theo tài khoản và cập nhật danh sách |
| UC07.4 – Lịch sử | Chưa có bảng dữ liệu, ghi nhận lượt nghe hoặc màn hình lịch sử |
| UC08.1–UC08.4 | Chưa có quản trị xem/thêm/sửa/xóa địa điểm |
| UC08.5–UC08.7 | Chưa có quản trị thêm/sửa/xóa điểm thành phần |
| UC08.8 | Chưa có màn hình quản trị cập nhật thuyết minh |
| UC08.9–UC08.12 | Chưa có quản trị danh sách/tìm/chi tiết/khóa–mở khóa tài khoản |

**Phạm vi cần chốt theo Word:** BR06 giới hạn AUTO ở điểm du lịch chính; BR10 yêu cầu chọn thủ công điểm thành phần. GPS theo marker là phần mở rộng đã làm thêm, không phải điều kiện bắt buộc để đạt BR10. Cần thống nhất lại đặc tả và UI nếu giữ phần mở rộng; không lấy demo marker làm bằng chứng đã đạt AUTO cấp địa điểm chính.

Word cho phép phát/dừng/phát lại, điều chỉnh tốc độ nếu được hỗ trợ; không mặc định bắt buộc pause/resume. Các tính năng AI, lập chuyến đi, backend hoặc Google Maps SDK không tự trở thành yêu cầu bắt buộc của đồ án.

## 15. Các bước tiếp theo để hoàn thành luồng chính

Thứ tự dưới đây ưu tiên hoàn thiện luồng người dùng theo Word, sau đó hoàn thành phạm vi quản trị. Mỗi bước cần có dữ liệu thật và tiêu chí nghiệm thu trước khi đánh dấu xong.

| Bước | Công việc | Điều kiện hoàn thành |
| ---: | --- | --- |
| 1 | Chốt bảng ánh xạ Word–mã nguồn: tài khoản email, AUTO điểm chính, GPS marker mở rộng, cách khôi phục | Mỗi khác biệt có quyết định rõ: sửa app hoặc cập nhật đặc tả được nhóm thống nhất |
| 2 | Hoàn thiện 71 bài thuyết minh và dữ liệu địa điểm | Không còn bài mẫu; đúng tên/ID/ảnh/tầng, có nguồn; thiết kế migration cho máy đã cài |
| 3 | Thêm lịch sử và điểm ghi nhận TTS dùng chung | Lưu tài khoản, PlaceID, PointID tùy chọn, thời gian, AUTO/MANUAL khi lượt phát thực sự bắt đầu; tránh ghi trùng callback |
| 4 | Xây màn hình Lịch sử từ Cá nhân | Danh sách mới nhất, rỗng, chặn khách, chỉ dữ liệu đúng tài khoản; bấm mở đúng địa điểm/điểm thành phần |
| 5 | Hoàn thiện AUTO cấp địa điểm chính | Bán kính lưu theo địa điểm, xét các địa điểm hợp lệ, chọn gần nhất, không lặp/phát chồng, có lịch sử và dừng đúng vòng đời |
| 6 | Hoàn thiện Gần tôi và bộ lọc | Có bán kính tìm kiếm, kiểm soát vị trí cũ, kết hợp từ khóa/danh mục, xử lý từ chối quyền và không có kết quả |
| 7 | Đồng bộ tài khoản theo đặc tả đã chốt | Nếu giữ yêu cầu Word: email bắt buộc/duy nhất, họ tên khi đăng ký, đăng nhập email; khôi phục vẫn phải có cơ chế xác minh |
| 8 | Hoàn thiện trạng thái UI và TTS | Loading/rỗng/lỗi/retry rõ ràng; nghe/dừng/phát lại nhất quán; đổi màn hình/xoay màn hình không phát chồng |
| 9 | Nghiệm thu GPS và sơ đồ trên thiết bị | Thử vào/ra vùng, GPS yếu/cũ, nhiều vùng giao nhau, dừng AUTO; ghi rõ phần chưa khảo sát; chọn tầng thủ công |
| 10 | Bổ sung vai trò và trạng thái tài khoản | Migration giữ dữ liệu, kiểm tra quyền tại nơi đọc/ghi; tài khoản khóa không đăng nhập; không tự khóa ADMIN đang dùng |
| 11 | Thực hiện đủ quản trị UC08.1–UC08.12 | CRUD địa điểm/điểm/bài đọc; tìm/xem/khóa tài khoản; xác nhận xóa và xử lý liên kết để không có dữ liệu mồ côi |
| 12 | Kiểm thử hồi quy, hoàn thiện báo cáo và bàn giao | Chạy build/unit/thiết bị, kiểm tra cài mới và nâng cấp; cập nhật Word, ảnh minh chứng và trạng thái từng UC theo kết quả thật |

Phần lịch sử nên định danh điểm bằng **cả PlaceID và PointID**, vì ID marker có thể trùng giữa các địa điểm. Không tự gán lịch sử nghe của khách cho tài khoản đăng nhập sau đó. Khóa quyền quản trị phải được kiểm tra trong xử lý nghiệp vụ, ngoài việc ẩn nút trên UI.

## 16. Kịch bản nghiệm thu đề xuất

Các kịch bản này là danh sách cần chạy khi hoàn thiện, không phải tuyên bố tất cả đã đạt.

| Mã | Kịch bản | Kết quả cần đạt |
| --- | --- | --- |
| LC01 | Welcome → Bắt đầu; Welcome → đăng nhập/hủy | Đúng đích đến và Back, không tạo phiên giả |
| LC02 | Đăng ký, trùng tên, mật khẩu sai, khôi phục và mã cũ | Kiểm tra đầu vào; mật khẩu/mã cũ mất hiệu lực sau đổi |
| LC03 | Đổi 5 tab, đăng nhập/đăng xuất từ Cá nhân | Tab chọn đúng, không chồng màn hình, yêu thích đúng tài khoản |
| LC04 | Tìm không dấu, lọc và Gần tôi khi có/không có GPS | Kết quả đúng, xử lý rỗng/lỗi và vị trí cũ; bán kính theo đặc tả sau hoàn thiện |
| LC05 | Mở cả 5 địa điểm từ thẻ, bản đồ, yêu thích | Tên/ảnh/ID/tọa độ và Activity tham quan thống nhất |
| LC06 | Chọn điểm/tầng và phóng–thu cả 5 sơ đồ | Marker vẫn bấm đúng, nội dung/TTS theo đúng điểm |
| LC07 | Demo từng địa điểm, chuyển mốc, dừng/rời màn hình | Không phát chồng, không ghi GPS mô phỏng, dừng đúng |
| LC08 | GPS thật vào/ra vùng, sai số lớn, hai mốc gần nhau | Chỉ phát khi đủ điều kiện; không đoán tầng/phòng hoặc lặp liên tục |
| LC09 | Ghi GPS đúng/sai vị trí, mở lại ứng dụng | Chặn dữ liệu cũ/giả lập/xa địa điểm; mốc hợp lệ lưu đúng site/marker/tầng |
| LC10 | Nghe AUTO/MANUAL rồi mở Lịch sử sau khi triển khai | Đúng tài khoản, thời gian, chế độ, ID; mở lại đúng nội dung |
| LC11 | Sửa hồ sơ, email sai, đổi mật khẩu, xoay màn hình | Chỉ ghi tài khoản sở hữu; lưu đúng hồ sơ và mã mới |
| LC12 | USER vào quản trị, ADMIN sửa/xóa/khóa sau khi triển khai | Chặn sai quyền, xử lý liên kết và trạng thái khóa đúng |
| LC13 | Cài mới và nâng cấp DB cũ có yêu thích/GPS/hồ sơ | Đủ dữ liệu mới, không mất dữ liệu cần giữ; các luồng trên vẫn hoạt động |

Mỗi lần nghiệm thu nên lưu phiên bản mã nguồn, thiết bị/API, dữ liệu thử, đạt/chưa đạt và ảnh/log minh chứng. Chỉ đánh dấu hoàn thành UC sau khi cả luồng chính và ngoại lệ liên quan đã được kiểm tra.

## 17. Cấu trúc mã nguồn và quy tắc cập nhật

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/app_ck/
│   ├── MainActivity, WelcomeActivity, KhamPhaActivity
│   ├── BanDoActivity, ChiTietDiaDiemActivity
│   ├── ThamQuanActivity và 4 Activity tham quan còn lại
│   ├── DangNhapActivity, DangKyActivity, QuenMatKhauActivity
│   ├── CaNhanActivity, SuaHoSoActivity, DoiMatKhauActivity
│   ├── YeuThichActivity, ThanhDieuHuongDuoi, BaseActivity
│   ├── TaiKhoanDb, PhienDangNhap, HoSoTaiKhoan, DiaDiem
│   ├── ThamQuanTuDong, NguonThamQuanTuDong, BoChonDiemTuDong
│   ├── KhoToaDoThamQuan, SoDoThuPhongLayout, GoiYDiaDiem
│   └── DuLieuDinhDocLap, NoiDungThuyetMinhDinhDocLap
└── res/
    ├── layout/       # Màn hình, thẻ và thành phần dùng chung
    ├── drawable/     # Ảnh địa điểm, sơ đồ, icon, nền giao diện
    └── values/       # Chuỗi, màu, theme, kích thước và ID
app/src/test/         # Unit test JVM
app/src/androidTest/  # Kiểm thử thiết bị, UI và migration
```

Khi thêm/sửa địa điểm, cần kiểm tra đồng thời ID trong SQLite, ánh xạ Activity, ảnh, sơ đồ, bản đồ, yêu thích và TTS. Khi thay đổi cấu trúc/nội dung đã lưu, bổ sung migration và kiểm thử nâng cấp. Tài nguyên ảnh có thể được nạp động qua tên drawable; không kết luận ảnh không dùng chỉ từ việc thiếu tham chiếu `@drawable` trong XML.

