package DAO;

import Model.Dish5;
import Model.DishDetail5;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DishDetailDAO5 extends DAO5 {

    public DishDetail5 getMinPriceByDish(Dish5 dish) throws SQLException {
        List<DishDetail5> details = getDishDetailsByDish(dish);
        if (details.isEmpty()) {
            return null;
        }
        // Truy vấn đã sắp xếp giá tăng dần nên phần tử đầu có giá thấp nhất.
        return details.get(0);
    }

    public List<DishDetail5> getDishDetailsByDish(Dish5 dish) throws SQLException {
        String sql = "SELECT id, size, price FROM tblDishDetail5 "
                + "WHERE dishId = ? ORDER BY price, id";
        List<DishDetail5> details = new ArrayList<>();

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dish.getId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    DishDetail5 detail = new DishDetail5();
                    detail.setId(result.getInt("id"));
                    detail.setSize(result.getString("size"));
                    detail.setPrice(result.getBigDecimal("price").doubleValue());
                    detail.setDish(dish);
                    details.add(detail);
                }
            }
        }
        return details;
    }

    public DishDetail5 getDishDetail(Integer detailId) throws SQLException {
        String sql = "SELECT d.id, d.name, d.description, d.status, d.image, "
                + "dd.id AS detailId, dd.size, dd.price "
                + "FROM tblDishDetail5 dd JOIN tblDish5 d ON dd.dishId = d.id "
                + "WHERE dd.id = ?";

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, detailId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                DishDetail5 detail = new DishDetail5();
                detail.setId(result.getInt("detailId"));
                detail.setSize(result.getString("size"));
                detail.setPrice(result.getBigDecimal("price").doubleValue());
                detail.setDish(DishDAO5.map(result));
                return detail;
            }
        }
    }
}
