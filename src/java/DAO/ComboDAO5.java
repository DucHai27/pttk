package DAO;

import Model.Combo5;
import Model.ComboDetail5;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ComboDAO5 extends DAO5 {

    public List<Combo5> getAllCombos() throws SQLException {
        String sql = "SELECT id, name, price, description, status, startDate, endDate, image "
                + "FROM tblCombo5 ORDER BY id";
        List<Combo5> combos = new ArrayList<>();

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Combo5 combo = new Combo5();
                combo.setId(result.getInt("id"));
                combo.setName(result.getString("name"));
                combo.setPrice(result.getBigDecimal("price").doubleValue());
                combo.setDescription(result.getString("description"));
                combo.setStatus(result.getString("status"));
                combo.setStartDate(result.getDate("startDate"));
                combo.setEndDate(result.getDate("endDate"));
                combo.setImage(result.getString("image"));
                combos.add(combo);
            }
        }
        return combos;
    }

    /** Cùng một transaction lưu cả combo cha và các dòng chi tiết. */
    public boolean createCombo(Combo5 combo) throws SQLException {
        if (combo.getComboDetails() == null || combo.getComboDetails().length == 0) {
            throw new IllegalArgumentException("Combo phải có ít nhất một món ăn.");
        }

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try {
                checkDishes(connection, combo.getComboDetails());
                int comboId = insertCombo(connection, combo);
                insertComboDetails(connection, comboId, combo.getComboDetails());

                connection.commit();
                combo.setId(comboId);
                return true;
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;
            }
        }
    }

    private void checkDishes(Connection connection, ComboDetail5[] details) throws SQLException {
        // FOR UPDATE giữ trạng thái món ổn định cho đến khi lưu xong combo.
        String sql = "SELECT d.status FROM tblDishDetail5 dd "
                + "JOIN tblDish5 d ON d.id = dd.dishId "
                + "WHERE dd.id = ? FOR UPDATE";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ComboDetail5 line : details) {
                Integer quantity = line.getQuantity();
                if (quantity == null || quantity < 1 || quantity > 999) {
                    throw new IllegalArgumentException("Số lượng phải từ 1 đến 999.");
                }

                statement.setInt(1, line.getDishDetail().getId());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || !"Đang phục vụ".equals(result.getString("status"))) {
                        throw new IllegalArgumentException(
                                "Có món đã ngừng phục vụ. Vui lòng xóa món đó khỏi combo.");
                    }
                }
            }
        }
    }

    private int insertCombo(Connection connection, Combo5 combo) throws SQLException {
        String sql = "INSERT INTO tblCombo5 "
                + "(name, price, description, status, startDate, endDate, image) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, combo.getName());
            statement.setBigDecimal(2, BigDecimal.valueOf(combo.getPrice()));
            statement.setString(3, combo.getDescription());
            statement.setString(4, combo.getDisplayStatus());
            statement.setDate(5, combo.getStartDate());
            statement.setDate(6, combo.getEndDate());
            statement.setString(7, combo.getImage());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không lưu được combo.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không nhận được mã combo.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertComboDetails(Connection connection, int comboId, ComboDetail5[] details)
            throws SQLException {
        String sql = "INSERT INTO tblComboDetail5 (quantity, comboId, dishDetailId) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ComboDetail5 line : details) {
                statement.setInt(1, line.getQuantity());
                statement.setInt(2, comboId);
                statement.setInt(3, line.getDishDetail().getId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
