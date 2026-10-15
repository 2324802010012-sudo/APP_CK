# APP_CK - Báo cáo đối chiếu project

## Tổng quan

Project hiện tại là ứng dụng Android Java/Gradle hỗ trợ tham quan điểm du lịch. Nội dung trong `Nhom15_BaoCao.docx` được dùng làm tài liệu đối chiếu, không phải chỉ thị thực thi.

Kết luận nhanh: project đã bám khá tốt phần ứng dụng người dùng cơ bản, gồm màn hình chào, trang chủ, danh sách/chi tiết địa điểm, thuyết minh bằng giọng nói, tài khoản và yêu thích. Tuy nhiên, phạm vi báo cáo lớn hơn code hiện tại: bản đồ tương tác thật, thuyết minh tự động theo vị trí, lịch sử tham quan, quản trị viên và CSDL đầy đủ chưa được hiện thực hoàn chỉnh.

## Các phần đã làm

- Có các màn hình chính: Welcome, Trang chủ, Chi tiết địa điểm, Tham quan, Đăng nhập, Đăng ký, Quên mật khẩu, Yêu thích.
- Có khai báo các Activity trong `app/src/main/AndroidManifest.xml`.
- Trang chủ hiển thị 3 điểm du lịch chính: Dinh Độc Lập, Bảo tàng Chứng tích Chiến tranh, Bưu điện Trung tâm Sài Gòn.
- Có tìm kiếm địa điểm theo tên/từ khóa và mở màn hình chi tiết.
- Có lọc danh mục ở mức giao diện.
- Có xin quyền vị trí, lấy vị trí hiện tại, tính khoảng cách và sắp xếp địa điểm gần người dùng.
- Màn hình chi tiết địa điểm có ảnh, tên, danh mục, địa chỉ, mô tả, chỉ đường, nghe/dừng thuyết minh, yêu thích, thư viện ảnh đơn giản và danh sách điểm tham quan nổi bật.
- Màn hình tham quan có các khu vực/điểm tham quan thành phần, tiến trình tham quan, nút điểm trước/điểm tiếp theo và thuyết minh bằng giọng nói.
- Chức năng tài khoản có đăng ký, đăng nhập, đăng xuất, quên mật khẩu bằng mã khôi phục và lưu phiên đăng nhập bằng `SharedPreferences`.
- SQLite đã có bảng tài khoản và yêu thích.
- Mật khẩu được băm bằng PBKDF2 có salt.
- Chức năng yêu thích đã có thêm, xóa và xem danh sách theo tài khoản.
- Project build được APK debug và chạy unit test mặc định thành công.

## Các phần còn tồn đọng so với báo cáo

- Bản đồ tương tác chưa có thật. Hiện chưa thấy Google Maps, MapView hoặc thư viện bản đồ khác; nút bản đồ ở trang chủ mới hiện thông báo đang xây dựng.
- Chưa có sơ đồ/biểu đồ tương tác nội bộ đúng nghĩa theo báo cáo. Màn hình tham quan hiện là danh sách các khu vực, chưa có marker/tọa độ MapX, MapY trên sơ đồ.
- Thuyết minh tự động theo vị trí chưa được hiện thực. App có thuyết minh bằng giọng nói thủ công, nhưng chưa có bán kính kích hoạt, theo dõi vị trí và tự động phát khi vào phạm vi.
- CSDL chưa đầy đủ theo ERD trong báo cáo. Báo cáo nêu các bảng `tblTaiKhoan`, `tblDiemDuLich`, `tblDiemThamQuan`, `tblYeuThich`, `tblLichSuThamQuan`; code hiện có chủ yếu là `tai_khoan` và `yeu_thich`, còn dữ liệu địa điểm đang hard-code trong Java.
- Chưa có lịch sử tham quan. Chưa ghi lại địa điểm/điểm thành phần đã nghe thuyết minh.
- Chưa có module quản trị hệ thống du lịch. Các chức năng thêm/sửa/xóa địa điểm, điểm tham quan, nội dung thuyết minh, tài khoản người dùng và khóa/mở khóa tài khoản chưa được làm.
- Một số màn hình trong báo cáo chưa có layout/activity riêng, vì báo cáo liệt kê nhiều màn hình hơn project hiện tại.
- `YeuThichActivity` đã tồn tại nhưng một số nút trên trang chủ/nav còn hiện Toast, chưa điều hướng vào màn hình yêu thích.
- Nút đăng nhập ở màn hình Welcome hiện mới Toast thông báo đang xây dựng, chưa mở `DangNhapActivity`.
- Test nghiệp vụ còn thiếu. Hiện chỉ có test mặc định, chưa có test cho đăng ký, đăng nhập, yêu thích, tìm kiếm, vị trí và thuyết minh.

## Đánh giá mức độ bám sát báo cáo

- Bám sát tốt: luồng người dùng cơ bản, giao diện tham quan, chi tiết địa điểm, thuyết minh thủ công, tài khoản và yêu thích.
- Bám sát một phần: tra cứu, lọc, định vị và hỗ trợ di chuyển.
- Chưa bám sát/chủ yếu còn thiếu: bản đồ tương tác, thuyết minh tự động theo vị trí, lịch sử tham quan, quản trị hệ thống, CSDL du lịch đầy đủ.

## Kết quả kiểm tra

- `./gradlew.bat testDebugUnitTest`: BUILD SUCCESSFUL.
- `./gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

## Gợi ý ưu tiên nếu cần hoàn thiện theo báo cáo

1. Nối nút đăng nhập ở Welcome và nút yêu thích/nav về đúng Activity đã có.
2. Bổ sung bảng CSDL cho điểm du lịch, điểm tham quan và lịch sử tham quan.
3. Chuyển dữ liệu địa điểm đang hard-code sang SQLite.
4. Làm màn hình lịch sử tham quan.
5. Làm bản đồ/sơ đồ tương tác và marker điểm du lịch/điểm tham quan.
6. Bổ sung thuyết minh tự động theo vị trí và bán kính kích hoạt.
7. Làm các màn hình quản trị nếu muốn bám sát đầy đủ UC08 trong báo cáo.

## Từng bước làm để đúng với báo cáo

Phần này là lộ trình thực hiện để đưa project hiện tại bám sát hơn với nội dung trong báo cáo. Nên làm theo thứ tự vì các bước sau phụ thuộc vào CSDL và điều hướng của các bước trước.

### Bước 1: Hoàn thiện điều hướng các màn hình người dùng

- Nối nút `Đăng nhập` ở màn hình Welcome vào `DangNhapActivity`.
- Nối nút `Điểm yêu thích` và thanh điều hướng `Yêu thích` ở trang chủ vào `YeuThichActivity`.
- Nối mục `Thông tin cá nhân` trong menu tài khoản vào một màn hình hoặc hộp thoại hiển thị tài khoản đang đăng nhập.
- Nối mục `Đăng nhập`, `Đăng ký`, `Đăng xuất` trong menu tài khoản theo đúng trạng thái đăng nhập.
- Kiểm tra lại luồng chuyển màn hình: Welcome -> Trang chủ -> Chi tiết địa điểm -> Tham quan -> Yêu thích.

Kết quả cần đạt: người dùng có thể di chuyển giữa các màn hình chính đúng như sơ đồ liên kết màn hình trong báo cáo.

### Bước 2: Chuẩn hóa CSDL theo ERD trong báo cáo

- Mở rộng `TaiKhoanDb` hoặc tạo lớp CSDL riêng cho du lịch.
- Bổ sung bảng tài khoản theo báo cáo: `id_tai_khoan`, `ho_ten`, `email` hoặc `ten_dang_nhap`, `mat_khau_bam`, `vai_tro`, `trang_thai`, `ngay_tao`.
- Bổ sung bảng điểm du lịch: `id_diem_du_lich`, `ten_diem_du_lich`, `dia_chi`, `loai_hinh`, `mo_ta`, `hinh_anh`, `vi_do`, `kinh_do`, `ban_kinh_kich_hoat`, `noi_dung_thuyet_minh`, `so_do_tham_quan`.
- Bổ sung bảng điểm tham quan thành phần: `id_diem_tham_quan`, `id_diem_du_lich`, `ten_diem_tham_quan`, `mo_ta`, `hinh_anh`, `noi_dung_thuyet_minh`, `toa_do_x`, `toa_do_y`, `thu_tu_tham_quan`.
- Bổ sung bảng yêu thích có thời gian thêm.
- Bổ sung bảng lịch sử tham quan.

Kết quả cần đạt: CSDL trong code có đủ các bảng chính đã nêu trong báo cáo.

### Bước 3: Chuyển dữ liệu hard-code sang CSDL

- Đưa 3 điểm du lịch hiện có vào bảng điểm du lịch.
- Đưa các khu vực như Cổng chính, Phòng Khánh tiết, Khu hầm... vào bảng điểm tham quan.
- Sửa `MainActivity` lấy danh sách điểm du lịch từ SQLite thay vì mảng `TEN`, `MO_TA`, `TOA_DO`.
- Sửa `ChiTietDiaDiemActivity` lấy chi tiết địa điểm từ CSDL theo `id_diem_du_lich`.
- Sửa `ThamQuanActivity` lấy danh sách điểm tham quan thành phần từ CSDL theo `id_diem_du_lich`.

Kết quả cần đạt: nội dung hiển thị vẫn như hiện tại, nhưng dữ liệu được lấy từ CSDL đúng với báo cáo.

### Bước 4: Hoàn thiện UC01 - Quản lý tài khoản

- Đăng ký tài khoản với thông tin rõ ràng hơn, nếu báo cáo yêu cầu thì bổ sung họ tên/email.
- Đăng nhập kiểm tra trạng thái tài khoản. Tài khoản bị khóa không được đăng nhập.
- Đăng xuất rõ ràng trong màn hình cá nhân hoặc menu tài khoản.
- Quên mật khẩu giữ cách dùng mã khôi phục hiện có hoặc điều chỉnh theo nội dung báo cáo.
- Lưu vai trò `USER` và `ADMIN` để phục vụ màn hình quản trị.

Kết quả cần đạt: UC01.1, UC01.2, UC01.3, UC01.4 trong báo cáo có luồng xử lý tương ứng trong app.

### Bước 5: Hoàn thiện UC02 - Tra cứu điểm du lịch

- Hiển thị danh sách điểm du lịch từ CSDL.
- Tìm kiếm theo tên địa điểm và từ khóa không dấu.
- Lọc theo loại hình du lịch lấy từ trường `loai_hinh`.
- Khi không có kết quả, hiện thông báo tiếng Việt rõ ràng.
- Nếu có quyền vị trí, sắp xếp hoặc hiện thêm khoảng cách đến từng địa điểm.

Kết quả cần đạt: người dùng xem, tìm kiếm, lọc và chọn điểm du lịch đúng như báo cáo.

### Bước 6: Hoàn thiện UC03 - Xem thông tin điểm du lịch

- Màn hình chi tiết hiện đầy đủ: tên địa điểm, hình ảnh, địa chỉ, loại hình, mô tả, tọa độ/khoảng cách nếu có vị trí người dùng và nội dung thuyết minh.
- Nút chỉ đường mở ứng dụng bản đồ hoặc trình duyệt với tọa độ đúng.
- Nút yêu thích thêm/xóa theo tài khoản đang đăng nhập.
- Nút bắt đầu tham quan mở danh sách điểm tham quan thành phần.

Kết quả cần đạt: màn hình chi tiết đáp ứng UC03.1 và UC03.2.

### Bước 7: Hoàn thiện UC04 - Bản đồ tương tác

- Chọn cách làm bản đồ: Google Maps nếu có API key, OpenStreetMap/OSMDroid nếu muốn tránh phụ thuộc Google Maps, hoặc sơ đồ nội bộ bằng `FrameLayout`/ảnh nền và các nút marker nếu chỉ cần demo.
- Màn hình bản đồ khu vực hiện các điểm du lịch chính bằng marker.
- Marker hiện tên địa điểm, địa chỉ ngắn và nút mở chi tiết.
- Màn hình sơ đồ nội bộ hiện các điểm tham quan thành phần theo `toa_do_x`, `toa_do_y`.
- Cho phép người dùng chọn marker điểm thành phần để xem thông tin và nghe thuyết minh.

Kết quả cần đạt: báo cáo nói "bản đồ tương tác" thì app phải có màn hình bản đồ/sơ đồ có marker bấm được, không chỉ là Toast.

### Bước 8: Hoàn thiện UC05 - Định vị và hỗ trợ di chuyển

- Giữ quyền `ACCESS_FINE_LOCATION` và `ACCESS_COARSE_LOCATION`.
- Lấy vị trí hiện tại của người dùng.
- Tính khoảng cách đến từng điểm du lịch bằng tọa độ trong CSDL.
- Hiện khoảng cách trên danh sách hoặc chi tiết địa điểm.
- Nút chỉ đường dùng tọa độ của địa điểm để mở Google Maps URL.
- Nếu chưa cấp quyền hoặc tắt GPS, hiện thông báo hướng dẫn bằng tiếng Việt.

Kết quả cần đạt: người dùng biết địa điểm nào gần mình và có thể mở chỉ đường.

### Bước 9: Hoàn thiện UC06 - Thuyết minh bằng giọng nói

- Giữ chức năng nghe/dừng thuyết minh thủ công hiện có.
- Đọc nội dung từ trường `noi_dung_thuyet_minh` trong CSDL.
- Ghi nhận điểm đã nghe vào bảng lịch sử tham quan.
- Bổ sung bán kính kích hoạt cho điểm du lịch chính.
- Khi người dùng ở trong bán kính kích hoạt, hiện gợi ý nghe thuyết minh hoặc tự động phát nếu thiết kế báo cáo yêu cầu.
- Tránh phát lặp lại liên tục: cần lưu trạng thái đã kích hoạt trong phiên hiện tại.

Kết quả cần đạt: có thuyết minh thủ công và có cơ chế thuyết minh theo vị trí như báo cáo.

### Bước 10: Hoàn thiện UC07 - Quản lý hoạt động cá nhân

- Yêu thích: thêm địa điểm yêu thích, xem danh sách yêu thích, xóa địa điểm yêu thích và lưu theo tài khoản.
- Lịch sử tham quan: tạo màn hình lịch sử, lưu địa điểm đã tham quan hoặc đã nghe thuyết minh, lưu thời gian tham quan, lưu chế độ thuyết minh thủ công hoặc tự động.
- Màn hình cá nhân nên có các mục: thông tin tài khoản, địa điểm yêu thích, lịch sử tham quan và đăng xuất.

Kết quả cần đạt: UC07 trong báo cáo có đủ yêu thích và lịch sử.

### Bước 11: Hoàn thiện UC08 - Quản trị hệ thống du lịch

- Tạo tài khoản có vai trò `ADMIN`.
- Tạo màn hình quản trị riêng chỉ cho admin.
- Quản lý điểm du lịch chính: xem danh sách, thêm, sửa, xóa.
- Quản lý điểm tham quan thành phần: xem danh sách theo điểm du lịch, thêm, sửa, xóa, cập nhật tọa độ trên sơ đồ nội bộ.
- Quản lý nội dung thuyết minh cho điểm du lịch chính và điểm tham quan thành phần.
- Quản lý tài khoản người dùng: xem danh sách, tìm kiếm, xem chi tiết, khóa/mở khóa tài khoản.

Kết quả cần đạt: các use case UC08.1 đến UC08.12 trong báo cáo có màn hình và thao tác tương ứng.

### Bước 12: Hoàn thiện UI đúng với đề tài

- Toàn bộ chữ hiển thị dùng tiếng Việt.
- Tên nút, thông báo lỗi, Toast và hộp thoại cần thống nhất cách gọi: "Điểm du lịch", "Điểm tham quan", "Thuyết minh", "Yêu thích", "Lịch sử tham quan".
- Giao diện nên giữ phong cách du lịch, lịch sử, bản đồ, hành trình.
- Không hiện các chữ tạm thời như "đang xây dựng" nếu chức năng đã có màn hình riêng.
- Các icon, ảnh nền, ảnh địa điểm cần có mô tả tiếng Việt.

Kết quả cần đạt: app nhìn hoàn chỉnh, đúng chủ đề và không còn chữ tiếng Anh trên giao diện người dùng.

### Bước 13: Bổ sung kiểm thử

- Test đăng ký tài khoản thành công.
- Test không đăng ký trùng tên/email.
- Test đăng nhập đúng/sai mật khẩu.
- Test thêm/xóa yêu thích.
- Test lấy danh sách điểm du lịch từ CSDL.
- Test tìm kiếm không dấu.
- Test tính khoảng cách.
- Test ghi lịch sử khi nghe thuyết minh.
- Chạy lại `./gradlew.bat testDebugUnitTest` và `./gradlew.bat assembleDebug`.

Kết quả cần đạt: build thành công và các chức năng chính có test tối thiểu.

### Bước 14: Cập nhật báo cáo và demo

- Cập nhật chương hiện thực chức năng theo đúng màn hình đã làm.
- Thêm ảnh chụp các màn hình: Welcome, Trang chủ, Đăng nhập/đăng ký, Danh sách điểm du lịch, Chi tiết điểm du lịch, Bản đồ/sơ đồ tương tác, Tham quan và thuyết minh, Yêu thích, Lịch sử tham quan, Quản trị nếu có.
- Cập nhật phần hạn chế nếu một số chức năng chỉ demo ở mức cơ bản.
- Cập nhật hướng phát triển nếu chưa làm đầy đủ bản đồ hoặc thuyết minh tự động.

Kết quả cần đạt: nội dung báo cáo và ứng dụng demo khớp nhau, tránh việc báo cáo có chức năng nhưng app không có.
