package Controller;

import DAO.DishCategoryDAO5;
import DAO.DishDAO5;
import DAO.DishDetailDAO5;
import Model.Dish5;
import Model.DishCategory5;
import Model.DishDetail5;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/DishDetailServlet5")
public class DishDetailServlet5 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int dishId = ControllerSupport5.positiveId(request.getParameter("id"));
            DishDAO5 dishDAO = new DishDAO5();
            Dish5 dish = dishDAO.getDishInformation(dishId);

            if (dish == null) {
                response.sendError(404);
                return;
            }

            DishCategoryDAO5 categoryDAO = new DishCategoryDAO5();
            DishCategory5 category = categoryDAO.getCategoryByDish(dish);
            DishDetailDAO5 detailDAO = new DishDetailDAO5();
            List<DishDetail5> details = detailDAO.getDishDetailsByDish(dish);
            dish.setDishDetails(details.toArray(new DishDetail5[0]));

            request.setAttribute("dish", dish);
            request.setAttribute("category", category);
            request.setAttribute("dishDetails", details);
            request.setAttribute("keyword", ControllerSupport5.keyword(request));
            request.getRequestDispatcher("/DishDetailUI5.jsp").forward(request, response);
        } catch (IllegalArgumentException ex) {
            response.sendError(400);
        } catch (SQLException ex) {
            ControllerSupport5.databaseError(request, response, ex);
        }
    }
}
