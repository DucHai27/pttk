package Controller;

import Model.Customer5;
import Model.Manager5;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

/** Kiểm tra đăng nhập, vai trò và mã xác nhận form trước khi gọi Servlet. */
@WebFilter("/*")
public class SessionFilter5 implements Filter {

    @Override
    public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) input;
        HttpServletResponse response = (HttpServletResponse) output;
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();
        if (path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");

        HttpSession session = request.getSession();
        if (session.getAttribute("csrf") == null) {
            session.setAttribute("csrf", UUID.randomUUID().toString());
        }

        Object member = session.getAttribute("member");
        boolean managerPath = path.startsWith("/Combo")
                || path.equals("/ManagerMainUI5.jsp")
                || path.equals("/DishSizeSelectionUI5.jsp");
        boolean customerPath = path.startsWith("/DishSearch")
                || path.startsWith("/DishDetail")
                || path.equals("/CustomerMainUI5.jsp");
        boolean requiresLogin = managerPath || customerPath || path.equals("/ImageServlet5");

        if (requiresLogin) {
            if (member == null) {
                response.sendRedirect(request.getContextPath() + "/LoginServlet5");
                return;
            }
            if (managerPath && !(member instanceof Manager5)) {
                response.sendError(403);
                return;
            }
            if (customerPath && !(member instanceof Customer5)) {
                response.sendError(403);
                return;
            }
        }

        if ("POST".equals(request.getMethod())) {
            String token;
            try {
                token = request.getParameter("csrf");
            } catch (IllegalStateException ex) {
                request.setAttribute("error",
                        "Ảnh quá lớn. Vui lòng chọn ảnh PNG/JPG/GIF tối đa 5 MB.");
                response.setStatus(413);
                request.getRequestDispatcher("/ErrorUI5.jsp").forward(request, response);
                return;
            }

            // CSRF đảm bảo form gửi lên có mã xác nhận khớp với phiên hiện tại.
            if (!session.getAttribute("csrf").equals(token)) {
                response.sendError(400);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
