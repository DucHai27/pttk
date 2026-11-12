package Controller;

import Util.Upload5;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/ImageServlet5")
public class ImageServlet5 extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        if (name == null || !name.matches("[a-f0-9-]+\\.(png|jpg|gif)")) {
            response.sendError(404);
            return;
        }

        Path directory = Upload5.directory();
        Path imagePath = directory.resolve(name).normalize();
        if (!imagePath.startsWith(directory) || !Files.isRegularFile(imagePath)) {
            response.sendError(404);
            return;
        }

        String contentType = "image/png";
        if (name.endsWith(".jpg")) {
            contentType = "image/jpeg";
        } else if (name.endsWith(".gif")) {
            contentType = "image/gif";
        }

        response.setContentType(contentType);
        response.setContentLengthLong(Files.size(imagePath));
        Files.copy(imagePath, response.getOutputStream());
    }
}
