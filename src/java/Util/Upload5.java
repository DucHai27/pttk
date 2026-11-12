package Util;

import jakarta.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** Lưu ảnh ngoài WAR để ảnh không bị mất khi triển khai lại ứng dụng. */
public final class Upload5 {

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_PIXELS = 20000000;

    private Upload5() {
    }

    public static Path directory() {
        String configured = System.getenv("RESTMAN_UPLOAD_DIR");
        Path folder;

        if (configured == null || configured.isBlank()) {
            folder = Paths.get(System.getProperty("user.home"), ".restman5", "uploads");
        } else {
            folder = Paths.get(configured);
        }
        return folder.toAbsolutePath().normalize();
    }

    public static String save(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ảnh tối đa 5 MB.");
        }

        byte[] bytes;
        try (InputStream input = part.getInputStream()) {
            bytes = input.readNBytes(MAX_FILE_SIZE + 1);
        }
        if (bytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ảnh tối đa 5 MB.");
        }

        String extension = validateImage(bytes);
        String fileName = UUID.randomUUID() + "." + extension;
        Path folder = directory();
        Files.createDirectories(folder);
        Files.write(folder.resolve(fileName), bytes, StandardOpenOption.CREATE_NEW);

        return "uploads/" + fileName;
    }

    /** Kiểm tra nội dung ảnh thực tế, không chỉ dựa vào đuôi tên file. */
    private static String validateImage(byte[] bytes) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Vui lòng chọn ảnh PNG, JPG hoặc GIF hợp lệ.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                String extension;

                if (format.equals("jpeg") || format.equals("jpg")) {
                    extension = "jpg";
                } else if (format.equals("png")) {
                    extension = "png";
                } else if (format.equals("gif")) {
                    extension = "gif";
                } else {
                    throw new IllegalArgumentException("Chỉ hỗ trợ ảnh PNG, JPG hoặc GIF.");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixelCount = (long) width * height;
                if (width < 1 || height < 1 || pixelCount > MAX_PIXELS) {
                    throw new IllegalArgumentException("Ảnh tối đa 20 triệu điểm ảnh.");
                }

                reader.read(0);
                return extension;
            } finally {
                reader.dispose();
            }
        }
    }

    public static void deleteDraft(String image) {
        if (image == null || !image.matches("uploads/[a-f0-9-]+\\.(png|jpg|gif)")) {
            return;
        }

        String fileName = image.substring("uploads/".length());
        try {
            Files.deleteIfExists(directory().resolve(fileName));
        } catch (IOException ex) {
            Logger logger = Logger.getLogger(Upload5.class.getName());
            logger.log(Level.WARNING, "Không xóa được ảnh nháp", ex);
        }
    }
}
