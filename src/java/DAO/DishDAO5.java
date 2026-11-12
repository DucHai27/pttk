package DAO;

import Model.Dish5;
import Model.DishCategory5;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DishDAO5 extends DAO5 {

    public List<Dish5> searchDishesByName(String keyword) throws SQLException {
        return findDishes(keyword, null);
    }

    public List<Dish5> getAllDishes() throws SQLException {
        return findDishes("", null);
    }

    public List<Dish5> getDishesByCategory(DishCategory5 category) throws SQLException {
        return findDishes("", category.getId());
    }

    /** Dùng chung truy vấn cho tìm theo tên, theo thể loại hoặc cả hai. */
    public List<Dish5> findDishes(String keyword, Integer categoryId) throws SQLException {
        String sql = "SELECT id, name, description, status, image "
                + "FROM tblDish5 WHERE name LIKE ? ESCAPE '!'";
        if (categoryId != null) {
            sql += " AND dishCategoryId = ?";
        }
        sql += " ORDER BY id";

        if (keyword == null) {
            keyword = "";
        }
        // %, _ có ý nghĩa riêng trong LIKE, nên cần đổi thành ký tự tìm kiếm thông thường.
        String escapedKeyword = keyword.replace("!", "!!");
        escapedKeyword = escapedKeyword.replace("%", "!%");
        escapedKeyword = escapedKeyword.replace("_", "!_");

        List<Dish5> dishes = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + escapedKeyword + "%");
            if (categoryId != null) {
                statement.setInt(2, categoryId);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Dish5 dish = map(result);
                    dishes.add(dish);
                }
            }
        }
        return dishes;
    }

    public Dish5 getDishInformation(Integer dishId) throws SQLException {
        String sql = "SELECT id, name, description, status, image FROM tblDish5 WHERE id = ?";

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dishId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
                return null;
            }
        }
    }

    /** Chuyển một dòng kết quả SQL thành một đối tượng Dish5. */
    static Dish5 map(ResultSet result) throws SQLException {
        Dish5 dish = new Dish5();
        dish.setId(result.getInt("id"));
        dish.setName(result.getString("name"));
        dish.setDescription(result.getString("description"));
        dish.setStatus(result.getString("status"));
        dish.setImage(result.getString("image"));
        return dish;
    }
}
