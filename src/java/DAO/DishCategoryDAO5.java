package DAO;

import Model.Dish5;
import Model.DishCategory5;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DishCategoryDAO5 extends DAO5 {

    public DishCategory5 getCategoryByDish(Dish5 dish) throws SQLException {
        String sql = "SELECT c.id, c.name, c.description FROM tblDishCategory5 c "
                + "JOIN tblDish5 d ON d.dishCategoryId = c.id WHERE d.id = ?";

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, dish.getId());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
                return null;
            }
        }
    }

    public List<DishCategory5> getAllCategories() throws SQLException {
        String sql = "SELECT id, name, description FROM tblDishCategory5 ORDER BY id";
        List<DishCategory5> categories = new ArrayList<>();

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                categories.add(map(result));
            }
        }
        return categories;
    }

    private DishCategory5 map(ResultSet result) throws SQLException {
        DishCategory5 category = new DishCategory5();
        category.setId(result.getInt("id"));
        category.setName(result.getString("name"));
        category.setDescription(result.getString("description"));
        return category;
    }
}
