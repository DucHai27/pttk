package Controller;

import DAO.DishDAO5;
import Model.Dish5;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/DishSearchServlet5")
public class DishSearchServlet5 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String keyword = ControllerSupport5.keyword(request);
            request.setAttribute("keyword", keyword);

            if (keyword.isEmpty()) {
                request.setAttribute("error", "Vui lòng nhập tên món ăn cần tìm.");
                request.getRequestDispatcher("/DishSearchUI5.jsp").forward(request, response);
                return;
            }

            DishDAO5 dishDAO = new DishDAO5();
            List<Dish5> dishes = dishDAO.searchDishesByName(keyword);

            ControllerSupport5.enrich(request, dishes);
            request.getRequestDispatcher("/DishSearchResultUI5.jsp").forward(request, response);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/DishSearchUI5.jsp").forward(request, response);
        } catch (SQLException ex) {
            ControllerSupport5.databaseError(request, response, ex);
        }
    }
}
