import DAO.*;
import Model.*;
import Util.Upload5;
import java.sql.*;

/** Kiểm tra dữ liệu ghi qua HTTP và rollback của transaction tạo combo. */
public class DatabaseIntegration5 extends DAO5 {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        String name = args[0];
        require(name.startsWith("__VERIFY_RESTMAN_"), "Chỉ kiểm tra và dọn dữ liệu có mã chạy riêng.");
        DatabaseIntegration5 test = new DatabaseIntegration5();
        String image = null;
        try (Connection con = test.getConnection()) {
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement("SELECT id,price,startDate,endDate,image FROM tblCombo5 WHERE name=?")) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        require(rs.next(), "Combo tạo qua HTTP phải được lưu.");
                        id = rs.getInt("id"); image = rs.getString("image");
                        require(rs.getBigDecimal("price").compareTo(new java.math.BigDecimal("129000")) == 0, "Giá combo do quản lý nhập.");
                        require("2026-12-01".equals(rs.getDate("startDate").toString()), "Ngày bắt đầu chính xác.");
                        require("2026-12-31".equals(rs.getDate("endDate").toString()), "Ngày kết thúc chính xác.");
                        require(!rs.next(), "Xác nhận nhiều lần không được lưu trùng combo.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT dishDetailId,quantity FROM tblComboDetail5 WHERE comboId=? ORDER BY dishDetailId")) {
                    ps.setInt(1,id);
                    try (ResultSet rs = ps.executeQuery()) {
                        require(rs.next() && rs.getInt(1) == 2 && rs.getInt(2) == 2, "Phở bò size Vừa số lượng 2.");
                        require(rs.next() && rs.getInt(1) == 4 && rs.getInt(2) == 1, "Phở gà size Nhỏ số lượng 1.");
                        require(!rs.next(), "Combo có đúng hai dòng.");
                    }
                }
                Combo5 invalid = new Combo5(); invalid.setName(name + "_rollback"); invalid.setPrice(100000);
                invalid.setStartDate(Date.valueOf("2026-12-01")); invalid.setEndDate(Date.valueOf("2026-12-31"));
                ComboDetail5 line = new ComboDetail5(); line.setDishDetail(new DishDetailDAO5().getDishDetail(2)); line.setQuantity(1);
                invalid.setComboDetails(new ComboDetail5[]{line,line});
                boolean failed = false;
                try { new ComboDAO5().createCombo(invalid); } catch (SQLException expected) { failed = true; }
                require(failed, "Ràng buộc dữ liệu phải chặn hai dòng trùng size.");
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM tblCombo5 WHERE name=?")) {
                    ps.setString(1, invalid.getName());
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); require(rs.getInt(1) == 0, "Rollback phải xóa cả combo cha khi lưu chi tiết lỗi."); }
                }
                System.out.println("PASS: database values, duplicate confirmation, complete transaction rollback");
            } finally {
                try (PreparedStatement ps = con.prepareStatement("DELETE cd FROM tblComboDetail5 cd JOIN tblCombo5 c ON cd.comboId=c.id WHERE c.name IN (?,?)")) {
                    ps.setString(1,name); ps.setString(2,name+"_rollback"); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM tblCombo5 WHERE name IN (?,?)")) {
                    ps.setString(1,name); ps.setString(2,name+"_rollback"); ps.executeUpdate();
                }
                Upload5.deleteDraft(image);
            }
        }
    }
}
