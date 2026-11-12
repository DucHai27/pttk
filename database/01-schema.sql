-- Phần CSDL phục vụ hai module và điều kiện đăng nhập, theo Hình 13 của tài liệu.
-- Docker tự chọn MYSQL_DATABASE khi chạy script lần đầu với volume trống.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS tblMember5 (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userName VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    fullName VARCHAR(255) NOT NULL,
    dateOfBirth DATE,
    gender VARCHAR(20),
    address VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phoneNumber VARCHAR(20) UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblEmployee5 (
    memberId INT PRIMARY KEY,
    position VARCHAR(100),
    FOREIGN KEY (memberId) REFERENCES tblMember5(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblManager5 (
    employeeId INT PRIMARY KEY,
    FOREIGN KEY (employeeId) REFERENCES tblEmployee5(memberId)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblCustomer5 (
    memberId INT PRIMARY KEY,
    FOREIGN KEY (memberId) REFERENCES tblMember5(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDishCategory5 (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDish5 (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'Đang phục vụ',
    image VARCHAR(255),
    dishCategoryId INT NOT NULL,
    FOREIGN KEY (dishCategoryId) REFERENCES tblDishCategory5(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDishDetail5 (
    id INT PRIMARY KEY AUTO_INCREMENT,
    size VARCHAR(30) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    dishId INT NOT NULL,
    CONSTRAINT uq_dish_size UNIQUE (dishId,size),
    CONSTRAINT ck_dish_price CHECK (price > 0),
    FOREIGN KEY (dishId) REFERENCES tblDish5(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblCombo5 (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    startDate DATE NOT NULL,
    endDate DATE NOT NULL,
    image VARCHAR(255),
    CONSTRAINT ck_combo_price CHECK (price > 0),
    CONSTRAINT ck_combo_dates CHECK (endDate >= startDate)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblComboDetail5 (
    id INT PRIMARY KEY AUTO_INCREMENT,
    quantity INT NOT NULL,
    comboId INT NOT NULL,
    dishDetailId INT NOT NULL,
    CONSTRAINT uq_combo_dish_detail UNIQUE (comboId,dishDetailId),
    CONSTRAINT ck_combo_quantity CHECK (quantity BETWEEN 1 AND 999),
    FOREIGN KEY (comboId) REFERENCES tblCombo5(id),
    FOREIGN KEY (dishDetailId) REFERENCES tblDishDetail5(id)
) ENGINE=InnoDB;
