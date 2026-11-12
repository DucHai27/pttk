package Controller;

import DAO.DishCategoryDAO5;
import DAO.DishDetailDAO5;
import Model.Dish5;
import Model.DishCategory5;
import Model.DishDetail5;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Các thao tác dùng chung cho nhiều Servlet. */
final class ControllerSupport5 {

    private ControllerSupport5() {
    }

    static Integer positiveId(String value) {
        try {
            int id = Integer.parseInt(value);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Mã dữ liệu không hợp lệ.");
        }
        throw new IllegalArgumentException("Mã dữ liệu không hợp lệ.");
    }

    static String keyword(HttpServletRequest request) {
        String value = request.getParameter("keyword");
        if (value == null) {
            return "";
        }
        value = value.trim();
        if (value.length() > 255) {
            throw new IllegalArgumentException("Từ khóa tìm kiếm tối đa 255 ký tự.");
        }
        return value;
    }

    /** Bổ sung thể loại và giá thấp nhất cho danh sách món trước khi chuyển sang JSP. */
    static void enrich(HttpServletRequest request, List<Dish5> dishes) throws SQLException {
        Map<Integer, DishCategory5> categories = new HashMap<>();
        Map<Integer, DishDetail5> prices = new HashMap<>();
        DishCategoryDAO5 categoryDAO = new DishCategoryDAO5();
        DishDetailDAO5 detailDAO = new DishDetailDAO5();

        for (Dish5 dish : dishes) {
            DishCategory5 category = categoryDAO.getCategoryByDish(dish);
            DishDetail5 minPrice = detailDAO.getMinPriceByDish(dish);
            categories.put(dish.getId(), category);
            prices.put(dish.getId(), minPrice);
        }

        request.setAttribute("dishList", dishes);
        request.setAttribute("categoryMap", categories);
        request.setAttribute("minPrices", prices);
    }

    static void databaseError(HttpServletRequest request, HttpServletResponse response, SQLException ex)
            throws ServletException, IOException {
        request.getServletContext().log("Lỗi truy cập MySQL", ex);
        response.setStatus(503);
        request.setAttribute("error",
                "Không thể đọc hoặc lưu dữ liệu. Vui lòng kiểm tra kết nối MySQL và thử lại.");
        request.getRequestDispatcher("/ErrorUI5.jsp").forward(request, response);
    }
}
