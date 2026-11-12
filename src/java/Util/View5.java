package Util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Các hàm định dạng dùng trong JSP; không truy vấn database. */
public final class View5 {

    private View5() {
    }

    /** h = HTML: đổi ký tự đặc biệt để dữ liệu được hiển thị an toàn. */
    public static String h(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString();
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        text = text.replace("\"", "&quot;");
        text = text.replace("'", "&#39;");
        return text;
    }

    /** q = query: mã hóa giá trị trước khi đưa vào tham số URL. */
    public static String q(String value) {
        if (value == null) {
            value = "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String money(double value) {
        Locale vietnamese = Locale.forLanguageTag("vi-VN");
        NumberFormat format = NumberFormat.getNumberInstance(vietnamese);
        format.setMaximumFractionDigits(2);
        return format.format(value) + "đ";
    }

    public static String date(Date value) {
        if (value == null) {
            return "";
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return value.toLocalDate().format(format);
    }

    public static String image(String context, String value) {
        if (value != null && value.matches("uploads/[a-f0-9-]+\\.(png|jpg|gif)")) {
            String fileName = value.substring("uploads/".length());
            return context + "/ImageServlet5?name=" + q(fileName);
        }
        if (value != null && value.matches("assets/images/[A-Za-z0-9_-]+\\.(svg|png|jpg)")) {
            return context + "/" + value;
        }
        return context + "/assets/images/dish.svg";
    }
}
