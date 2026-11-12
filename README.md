# RestMan – Hai module Servlet/JSP MVC

Triển khai theo `BTL_PTTK_SOLO_FINAL.docx`: khách hàng tìm thông tin món ăn và nhân viên quản lý lên menu dạng combo. Giao diện bám mẫu RestMan trắng–xám, bảng kết quả, form combo và popup kích cỡ/số lượng trong Hình 14–15.

## Môi trường

- NetBeans, Apache Ant, JDK 25 và Apache Tomcat 11 (Jakarta Servlet/JSP), theo cấu hình dự án hiện tại.
- Docker Desktop chạy Linux containers; MySQL 8.4 được tạo bằng Docker Compose.
- MySQL Connector/J đã được khai báo trong thư viện NetBeans. Khi chuyển máy, cập nhật đường dẫn JAR ở Project Properties → Libraries, đường dẫn Tomcat ở Run và Java Platform nếu cần.
- Không sử dụng Spring, Hibernate, frontend framework hoặc ORM.

## Chạy MySQL bằng Docker

Từ thư mục dự án:

```powershell
docker compose up -d --wait
docker compose ps
```

Lần chạy đầu với volume trống, MySQL tự tạo database `restman5`, chạy `database/01-schema.sql` và `database/02-seed.sql`. Các lần khởi động sau giữ dữ liệu trong volume `mysql_data`.

Kết nối từ Tomcat hoặc DBeaver:

| Thuộc tính | Giá trị mặc định |
| --- | --- |
| Host | `localhost` |
| Port | `3306` |
| Database | `restman5` |
| User | `restman` |
| Password | `RestmanDev@123` |

Đây là thông tin mẫu dành cho phát triển cục bộ. Để đổi, sao chép `.env.example` thành `.env` rồi chỉnh trước lần khởi tạo volume đầu tiên. Nếu thay cổng/tên database/user/password, cập nhật cấu hình JDBC tương ứng. Thay biến môi trường Compose sau khi database đã được khởi tạo không tự đổi tài khoản hay mật khẩu đang lưu trong MySQL.

Đọc log bằng `docker compose logs mysql`; dừng MySQL bằng `docker compose stop`. Không cần xóa volume để khởi động lại.

## Chạy ứng dụng

1. Kiểm tra `src/conf/db.properties`. Mặc định đã khớp Docker Compose. Các biến môi trường của tiến trình Tomcat `DB_URL`, `DB_USER`, `DB_PASSWORD` được ưu tiên nếu được đặt. Build sao chép file properties vào `WEB-INF/classes`.
2. Trong NetBeans chọn Tomcat 11, **Clean and Build**, rồi **Run**.
3. Mở `http://localhost:8080/pttk/` (thay cổng nếu Tomcat của bạn dùng cổng khác).

| Vai trò | Tên đăng nhập | Mật khẩu mẫu |
| --- | --- | --- |
| Khách hàng | `khachhang` | `Khach@123` |
| Nhân viên quản lý | `quanly` | `Quanly@123` |

Mật khẩu được lưu dưới dạng PBKDF2-HMAC-SHA256. Session lưu đúng vai trò; khách hàng không truy cập được chức năng quản lý. Đăng nhập được bổ sung để phục vụ điều kiện đầu vào của hai module trong tài liệu.

Có thể build và kiểm tra tất cả JSP bằng Ant:

```powershell
ant verify-jsps dist
```

Nếu `ant` chưa có trong PATH, dùng `ant.bat` trong thư mục `netbeans/extide/ant/bin`. Target `verify-jsps` dùng Jasper của Tomcat 11, phù hợp Jakarta/JDK 25; giữ tùy chọn `compile.jsps=false` của NetBeans vì bộ biên dịch JSP tích hợp của NetBeans có thể không nhận source level 25. WAR nằm ở `dist/pttk.war`.

## Luồng sử dụng

**Khách hàng:** Trang chủ → Tìm thông tin món ăn → nhập `Phở` → danh sách ba món kèm thể loại, ảnh đại diện và giá thấp nhất → chọn `Phở bò` → xem mô tả, trạng thái, các kích cỡ Nhỏ/Vừa/Lớn cùng giá 45.000/55.000/65.000đ. Quay lại giữ từ khóa tìm kiếm. Có xử lý từ khóa trống và không có kết quả.

**Quản lý:** Trang chủ → Quản lý combo → Thêm combo mới → nhập tên, giá bán, mô tả, ngày áp dụng, chọn ảnh → Tiếp tục → tìm theo tên/thể loại → Chọn món → popup chọn size và số lượng → Thêm vào combo → Xác nhận.

Ví dụ trong tài liệu: `Combo Phở 3 suất`, giá `129000`, từ `01/12/2026` đến `31/12/2026`, chọn Phở bò size Vừa số lượng 2 và Phở gà size Nhỏ số lượng 1. Giá combo do quản lý nhập, độc lập với tổng giá món lẻ. Ngày hiển thị theo định dạng ngày/tháng/năm; trình chọn ngày có thể theo ngôn ngữ trình duyệt.

Chọn lại cùng một món/cùng size sẽ cộng số lượng; chọn size khác tạo dòng riêng. Có thể xóa từng dòng, quay lại sửa thông tin mà vẫn giữ món đã chọn, hoặc Hủy để bỏ bản nháp. Các món ngừng phục vụ không được thêm vào combo. Trạng thái combo được hiển thị theo ngày hiện tại ở múi giờ Asia/Bangkok.

Ảnh PNG/JPG/GIF tối đa 5 MB được kiểm tra nội dung và lưu ngoài WAR ở `%USERPROFILE%/.restman5/uploads`. Có thể đổi thư mục bằng biến `RESTMAN_UPLOAD_DIR` của Tomcat. Ảnh nháp được xóa khi Hủy/đăng xuất/hết session; ảnh của combo đã lưu được giữ lại. Dữ liệu mẫu dùng hình minh họa SVG, bạn có thể thay bằng ảnh thực tế.

## Cấu trúc và đối chiếu tài liệu

Code được trình bày theo cách tường minh để dễ học: mỗi câu lệnh một dòng, dùng vòng `for` và `if/else`, khai báo import rõ ràng và thêm chú thích ở các bước nghiệp vụ. Các constructor rỗng và comment mẫu NetBeans đã được bỏ. Session chỉ lưu một đối tượng `member` cho người đăng nhập; vai trò được xác định từ kiểu `Customer5` hoặc `Manager5`.

Khi đọc `ComboCreateServlet5`, bắt đầu từ `doGet()` và `doPost()`, rồi đọc hàm tương ứng với thao tác:

| Hàm | Nội dung tương ứng trong kịch bản |
| --- | --- |
| `showInformationForm()` | Chuẩn bị dữ liệu và mở form thông tin combo |
| `saveInformation()` | Kiểm tra thông tin, lưu bản nháp vào session |
| `selection()` | Lấy danh sách món/thể loại và dữ liệu popup kích cỡ |
| `addDish()` | Thêm món; cộng số lượng nếu đã có cùng món/cùng size |
| `removeDish()` | Xóa dòng món đã chọn |
| `confirmCombo()` | Gọi DAO lưu combo và báo thành công |

Trong `ComboDAO5.createCombo()`, đọc lần lượt `checkDishes()` → `insertCombo()` → `insertComboDetails()` → `commit()`. Các hàm cùng dùng một kết nối; nếu gặp lỗi thì `rollback()`. Với module khách hàng, bắt đầu từ `DishSearchServlet5.doGet()` rồi đến `DishDAO5.searchDishesByName()` và trang kết quả.

```text
src/java/Model       Thực thể có hậu tố 5, quan hệ kế thừa và mảng theo sơ đồ lớp
src/java/DAO         JDBC PreparedStatement, đóng kết nối sau mỗi thao tác
src/java/Controller  Servlet điều khiển luồng, kiểm tra dữ liệu và quyền truy cập
src/java/Util        Định dạng/escape HTML, xác thực mật khẩu, lưu ảnh
src/conf            Cấu hình JDBC
web                 JSP hiển thị, CSS/JS thuần
web/WEB-INF/jspf     Phần header/footer/thông báo dùng chung
database            CSDL và dữ liệu mẫu
compose.yaml        MySQL trong Docker
```

Giữ các hàm `searchDishesByName`, `getDishInformation`, `getAllDishes`, `getDishesByCategory`, `getCategoryByDish`, `getAllCategories`, `getMinPriceByDish`, `getDishDetailsByDish`, `getAllCombos`, `createCombo` theo thiết kế. Combo chứa `ComboDetail5[]`; mỗi chi tiết tham chiếu `DishDetail5` để lưu chính xác size và số lượng. DAO mở kết nối theo từng thao tác để Servlet phục vụ nhiều request an toàn, thay vì chia sẻ một connection.

Tên trang kết quả đúng tài liệu là `DishSearchResultUI5.jsp`; `DishSearchResult5.jsp` được giữ làm đường dẫn tương thích với file ban đầu. JSP chỉ hiển thị dữ liệu, không truy vấn CSDL; các trang cần dữ liệu chuyển qua Servlet khi truy cập trực tiếp.

Script tạo phần CSDL cần cho hai module và đăng nhập (9 bảng theo tên/cột trong Hình 13). Các nghiệp vụ đặt bàn, bán hàng, kho và thống kê toàn hệ thống nằm ngoài phạm vi hai module này.

## Kiểm thử tích hợp

Sau khi build và khởi động ứng dụng với dữ liệu mẫu, có thể chạy bộ kiểm tra HTTP/DAO bằng Node.js và JDK:

```powershell
$env:RESTMAN_BASE_URL = 'http://localhost:8080/pttk'
node test/integration/http-smoke.mjs
```

Bộ kiểm tra dùng tài khoản mẫu, kiểm tra tìm kiếm tiếng Việt, phân quyền, kiểm tra form/ảnh, chọn size/số lượng, lưu và chống xác nhận trùng. Phần JDBC đối chiếu dữ liệu đã lưu và cố ý gây lỗi lưu chi tiết để xác minh rollback toàn bộ transaction. Các combo/ảnh được tạo riêng bởi lần kiểm thử thành công sẽ được dọn; mã ID tự tăng có thể có khoảng trống do tạo/xóa dữ liệu kiểm thử và rollback.

File `test/integration/browser-check.mjs` là kiểm tra giao diện tùy chọn, dùng Edge/Chrome mở sẵn với remote debugging cổng `19225`. Kết quả chụp màn hình nằm trong `build/verification`.
