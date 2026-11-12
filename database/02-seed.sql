SET NAMES utf8mb4;
START TRANSACTION;

-- Mật khẩu PBKDF2-HMAC-SHA256: khachhang / Khach@123; quanly / Quanly@123.
INSERT INTO tblMember5(id,userName,password,fullName,email,phoneNumber) VALUES
(1,'khachhang','pbkdf2_sha256$210000$O+ZXjeC8J9bVA4V4yvcOJw==$zZ4oRuNOuVkK+2eub3U2OGK4PxB5wOwRuuPiOigyscQ=','Khách hàng A','khachhang@example.test','0900000001'),
(2,'quanly','pbkdf2_sha256$210000$6Wapqj0w861Tdq0uwjaciw==$EvffXFDWXT3qSoxM6HTT28M04xmHTtT+P0gClucL2bM=','Nhân viên quản lý A','quanly@example.test','0900000002')
ON DUPLICATE KEY UPDATE id=id;
INSERT IGNORE INTO tblCustomer5(memberId) VALUES (1);
INSERT IGNORE INTO tblEmployee5(memberId,position) VALUES (2,'Quản lý');
INSERT IGNORE INTO tblManager5(employeeId) VALUES (2);

INSERT INTO tblDishCategory5(id,name,description) VALUES
(1,'Món chính','Các món ăn chính của nhà hàng'),
(2,'Khai vị','Các món khai vị'),
(3,'Đồ uống','Nước uống và đồ giải khát'),
(4,'Tráng miệng','Các món tráng miệng')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tblDish5(id,name,description,status,image,dishCategoryId) VALUES
(1,'Phở bò','Phở bò truyền thống với nước dùng đậm đà, thịt bò tươi và bánh phở mềm.','Đang phục vụ','assets/images/dish.svg',1),
(2,'Phở gà','Phở gà với nước dùng thanh ngọt, thịt gà ta và rau thơm.','Đang phục vụ','assets/images/dish.svg',1),
(3,'Phở đặc biệt','Phở bò đặc biệt với thịt tái, nạm, gầu và bò viên.','Đang phục vụ','assets/images/dish.svg',1),
(4,'Nem cuốn','Nem cuốn tôm thịt, rau sống và nước chấm chua ngọt.','Đang phục vụ','assets/images/dish.svg',2),
(5,'Nước chanh','Nước chanh tươi, có thể điều chỉnh lượng đường và đá.','Đang phục vụ','assets/images/dish.svg',3),
(6,'Chè sen','Chè hạt sen thanh mát. Món hiện tạm ngừng phục vụ.','Ngừng phục vụ','assets/images/dish.svg',4)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tblDishDetail5(id,size,price,dishId) VALUES
(1,'Nhỏ',45000,1),(2,'Vừa',55000,1),(3,'Lớn',65000,1),
(4,'Nhỏ',40000,2),(5,'Nhỏ',60000,3),(6,'Vừa',70000,3),(7,'Lớn',80000,3),
(8,'Phần',35000,4),(9,'Vừa',20000,5),(10,'Lớn',25000,5),(11,'Chén',30000,6)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO tblCombo5(id,name,price,description,status,startDate,endDate,image) VALUES
(1,'Combo Cơm Trưa',89000,'Combo bữa trưa với phở bò và nem cuốn.','Đã kết thúc','2026-10-01','2026-10-31','assets/images/dish.svg'),
(2,'Combo Gia Đình',249000,'Combo dành cho cả gia đình.','Sắp áp dụng','2026-12-01','2026-12-31','assets/images/dish.svg')
ON DUPLICATE KEY UPDATE id=id;
INSERT INTO tblComboDetail5(id,quantity,comboId,dishDetailId) VALUES
(1,1,1,2),(2,1,1,8),(3,3,2,2),(4,2,2,4),(5,1,2,8)
ON DUPLICATE KEY UPDATE id=id;

COMMIT;
