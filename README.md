# APP_CK – Ứng dụng hỗ trợ tham quan du lịch

Ứng dụng Android viết bằng Java, hỗ trợ khám phá địa điểm tại TP. Hồ Chí Minh, xem thông tin, nghe thuyết minh tiếng Việt và tham quan theo sơ đồ. Dữ liệu hiện có gồm **Dinh Độc Lập**, **Bảo tàng Chứng tích Chiến tranh** và **Bưu điện Trung tâm Sài Gòn**.

README mô tả trạng thái mã nguồn hiện tại; các chức năng chưa hoàn thiện được liệt kê riêng bên dưới.

## Chức năng hiện có

- **Khám phá địa điểm:** danh sách 3 địa điểm, tìm kiếm có/không dấu theo tên và từ khóa, lọc danh mục cơ bản.
- **Địa điểm gần tôi:** xin quyền vị trí, lấy vị trí thiết bị, tính khoảng cách và sắp xếp địa điểm từ gần đến xa.
- **Chi tiết địa điểm:** ảnh, danh mục, địa chỉ, mô tả, thư viện ảnh đơn giản và các điểm tham quan nổi bật.
- **Bản đồ và chỉ đường:** từ màn hình chi tiết, mở Google Maps qua ứng dụng hoặc trình duyệt để xem vị trí và chỉ đường.
- **Thuyết minh:** nghe/dừng nội dung bằng Android `TextToSpeech` với ngôn ngữ tiếng Việt tại màn hình chi tiết và tham quan.
- **Tham quan:** chọn điểm trong danh sách, chuyển điểm trước/tiếp theo và hiển thị vị trí điểm đang chọn trên thanh tiến trình.
- **Tài khoản:** có màn hình đăng ký, đăng nhập và đặt lại mật khẩu bằng mã khôi phục; phiên đăng nhập lưu bằng `SharedPreferences`.
- **Yêu thích:** thêm/xóa từ màn hình chi tiết và có màn hình danh sách yêu thích theo tài khoản. Một số nút điều hướng đến màn hình này chưa được nối.

### Sơ đồ tương tác Dinh Độc Lập

Màn hình tham quan Dinh Độc Lập có **25 điểm**, phân bố trên **6 sơ đồ**:

| Sơ đồ | Số điểm | Ví dụ |
| --- | ---: | --- |
| Tầng 1 | 4 | Phòng Nội các, Phòng Đại yến, Phòng Khánh tiết |
| Tầng 2 | 6 | Phòng làm việc của Tổng thống, Phòng trình quốc thư |
| Tầng 3 | 4 | Thư viện, Phòng chiếu phim, Phòng giải trí |
| Tầng 4 | 2 | Tứ phương vô sự lầu, Trực thăng UH1 |
| Tầng hầm | 4 | Tham mưu tác chiến, Thông tin liên lạc |
| Tầng trệt | 5 | Bếp, Xe Jeep, Xe Mercedes, Phòng tập bắn |

Người dùng có thể đổi tầng, chạm vào số trên sơ đồ hoặc chọn tên trong danh sách để xem điểm tương ứng, sau đó bấm nghe thuyết minh. Marker được vẽ trên ảnh bằng `Canvas`, sử dụng tọa độ chuẩn hóa trong `DuLieuDinhDocLap`.

Nội dung riêng hiện có cho **Phòng Nội các (điểm 1)** và **Phòng Khánh tiết (điểm 4)**. Các điểm còn lại sử dụng phần giới thiệu tên, vị trí, tầng và thông báo nội dung lịch sử đang được cập nhật. Thanh tiến trình thể hiện thứ tự điểm đang chọn, chưa phải tỷ lệ các điểm đã thực sự tham quan.

## Công nghệ và cấu hình

| Thành phần | Cấu hình trong dự án |
| --- | --- |
| Ngôn ngữ / giao diện | Java, XML layouts |
| Android SDK tối thiểu | API 24 (Android 7.0) |
| Target SDK | API 36 |
| Compile SDK | API 36, minor API 1 (`36.1`) |
| Android Gradle Plugin | 9.1.1 |
| Gradle Wrapper | 9.3.1 |
| JVM chạy Gradle | Java 21 theo `gradle-daemon-jvm.properties` |
| Mức ngôn ngữ Java của ứng dụng | Java 11 |
| Thư viện giao diện | AndroidX AppCompat, Activity, ConstraintLayout, Material Components |
| Lưu trữ | SQLite, SharedPreferences |
| Vị trí / giọng đọc | Android LocationManager, TextToSpeech |

Ứng dụng chưa tích hợp SDK bản đồ và không cần Google Maps API key cho thao tác mở liên kết bản đồ hiện tại.

## Cài đặt và chạy

1. Mở thư mục dự án bằng Android Studio có hỗ trợ cấu hình AGP của dự án.
2. Chuẩn bị JDK 21 và Android SDK tương ứng với compile SDK `36.1`. Gradle có cấu hình tải JVM qua Foojay nếu môi trường cho phép.
3. Đặt đường dẫn Android SDK trong SDK Manager hoặc `local.properties` với thuộc tính `sdk.dir` phù hợp máy đang dùng; không sao chép đường dẫn SDK của máy khác.
4. Đồng bộ Gradle. Lần đầu cần Internet để tải Gradle và các dependency.
5. Chọn thiết bị thật hoặc máy ảo Android API 24 trở lên, rồi chạy module `app`.

Các lệnh bên dưới chạy tại thư mục gốc bằng PowerShell:

```powershell
# Tạo APK debug
.\gradlew.bat assembleDebug

# Chạy unit test
.\gradlew.bat testDebugUnitTest

# Cài bản debug lên thiết bị/máy ảo đang kết nối
.\gradlew.bat installDebug

# Chạy kiểm thử trên thiết bị/máy ảo
.\gradlew.bat connectedDebugAndroidTest
```

APK debug sau khi build thành công nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

Trên macOS/Linux, dùng `./gradlew` thay cho `.\gradlew.bat` (cấp quyền thực thi cho wrapper nếu cần).

### Luồng dùng thử

1. Mở ứng dụng, chọn **Bắt đầu** để vào trang chủ.
2. Chọn một địa điểm hoặc tìm bằng từ khóa, ví dụ `dinh doc lap`.
3. Tại màn hình chi tiết, thử xem bản đồ, chỉ đường và nghe/dừng thuyết minh.
4. Với Dinh Độc Lập, mở màn hình tham quan, đổi tầng và chạm các marker để chọn điểm.
5. Nhấn yêu thích khi chưa đăng nhập để mở luồng đăng nhập, rồi chuyển sang đăng ký nếu chưa có tài khoản. Lưu mã khôi phục được cấp sau khi đăng ký.
6. Để dùng **Địa điểm gần tôi**, bật dịch vụ vị trí và cấp quyền khi được yêu cầu. Trên máy ảo, cần thiết lập vị trí mô phỏng.

Giọng đọc phụ thuộc vào TTS engine và dữ liệu tiếng Việt trên thiết bị. Nếu chưa nghe được, kiểm tra cài đặt chuyển văn bản thành giọng nói và cài dữ liệu giọng đọc tiếng Việt. Bản đồ/chỉ đường phụ thuộc ứng dụng hoặc trình duyệt bên ngoài và kết nối của thiết bị.

## Cấu trúc mã nguồn

Các lớp Java nằm trong `app/src/main/java/com/example/app_ck/`:

| Tệp | Vai trò |
| --- | --- |
| `WelcomeActivity.java` | Màn hình mở đầu |
| `MainActivity.java` | Trang chủ, tìm kiếm, danh mục và địa điểm gần tôi |
| `ChiTietDiaDiemActivity.java` | Chi tiết, bản đồ/chỉ đường, thuyết minh và yêu thích |
| `ThamQuanActivity.java` | Chuyển điểm, sơ đồ tương tác và nghe thuyết minh |
| `DuLieuDinhDocLap.java` | 25 điểm, thông tin tầng và tọa độ marker |
| `NoiDungThuyetMinhDinhDocLap.java` | Nội dung giới thiệu/thuyết minh theo ID điểm |
| `DangNhapActivity.java`, `DangKyActivity.java`, `QuenMatKhauActivity.java` | Các màn hình tài khoản |
| `TaiKhoanDb.java` | SQLite cho tài khoản và yêu thích |
| `PhienDangNhap.java` | Lưu, đọc và xóa phiên đăng nhập |
| `YeuThichActivity.java` | Hiển thị và quản lý danh sách yêu thích |

- `app/src/main/res/layout/`: bố cục giao diện XML.
- `app/src/main/res/drawable/`: ảnh địa điểm, ảnh sơ đồ và tài nguyên giao diện.
- `app/src/main/res/values/`: chuỗi, màu sắc và theme.
- `app/src/main/AndroidManifest.xml`: khai báo Activity và quyền vị trí.
- `app/src/test/`, `app/src/androidTest/`: unit test và kiểm thử trên thiết bị; hiện chủ yếu là test mẫu mặc định.

## Dữ liệu và tài khoản

SQLite sử dụng tệp `du_lich_tai_khoan.db`, phiên bản 2, với hai bảng:

- `tai_khoan`: tên đăng nhập duy nhất, salt, mật khẩu băm và mã khôi phục băm.
- `yeu_thich`: liên kết ID tài khoản với ID địa điểm; mỗi cặp chỉ lưu một lần.

Mật khẩu và mã khôi phục được băm bằng PBKDF2 với salt. Tên đăng nhập khi đăng ký gồm 3–30 ký tự chữ Latin, số hoặc dấu gạch dưới; mật khẩu tối thiểu 8 ký tự. Đặt lại mật khẩu thành công sẽ cấp mã khôi phục mới và vô hiệu mã cũ.

Tài khoản và yêu thích lưu cục bộ trên thiết bị, chưa có backend hoặc đồng bộ giữa các máy. Dữ liệu địa điểm, tọa độ và nội dung tham quan hiện được khai báo trực tiếp trong các lớp Java, chưa lưu thành bảng du lịch trong SQLite.

## Phần còn cần hoàn thiện

- Nút **Đăng nhập** ở Welcome mới hiển thị thông báo; các mục trong menu tài khoản ở trang chủ chưa mở màn hình tương ứng.
- Nút yêu thích và một số mục điều hướng ở trang chủ mới hiển thị Toast; cần nối vào Activity đã có.
- Chưa có luồng đăng xuất hoàn chỉnh trên giao diện, dù `PhienDangNhap` đã có hàm xóa phiên.
- Chưa có bản đồ khu vực tích hợp trong ứng dụng. Nút bản đồ ở trang chủ còn thông báo đang xây dựng; sơ đồ tương tác hiện chỉ áp dụng cho Dinh Độc Lập.
- Chưa có thuyết minh tự động theo vị trí hoặc bán kính kích hoạt. Thuyết minh hiện được phát theo thao tác người dùng.
- Chưa có lịch sử tham quan, lưu tiến độ thực tế hoặc khôi phục hành trình từ CSDL.
- Nội dung thuyết minh chi tiết và ảnh riêng cho từng điểm cần tiếp tục bổ sung, đối chiếu tư liệu.
- Chưa có bảng dữ liệu điểm du lịch/điểm tham quan, phân quyền quản trị hoặc chức năng quản lý nội dung và tài khoản.
- Chưa có bộ kiểm thử nghiệp vụ cho tài khoản, yêu thích, tìm kiếm, marker và thuyết minh.

## Hướng phát triển

1. Hoàn thiện điều hướng tài khoản, yêu thích và đăng xuất.
2. Chuẩn hóa dữ liệu địa điểm/điểm tham quan trong SQLite và bổ sung lịch sử tham quan.
3. Hoàn thiện nội dung, ảnh và kiểm tra tọa độ các marker trên thiết bị.
4. Mở rộng bản đồ khu vực, sơ đồ cho các địa điểm còn lại và thuyết minh theo vị trí.
5. Bổ sung chức năng quản trị và kiểm thử nghiệp vụ theo phạm vi đồ án.

Các lệnh kiểm thử ở trên là hướng dẫn thực hiện, không phải xác nhận kết quả build của lần cập nhật tài liệu này. Khi lập báo cáo hoặc demo, cần chạy lại kiểm thử và mô tả chức năng theo đúng trạng thái ứng dụng.
