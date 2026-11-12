package Model;

import java.io.Serializable;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

/** Thông tin combo và các món thành phần. */
public class Combo5 implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private double price;
    private String description;
    private String status;
    private Date startDate;
    private Date endDate;
    private String image;
    private ComboDetail5[] comboDetails = new ComboDetail5[0];

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public ComboDetail5[] getComboDetails() {
        return comboDetails;
    }
    public void setComboDetails(ComboDetail5[] comboDetails) {
        this.comboDetails = comboDetails;
    }

    public String getCode() {
        return String.format("CB%03d", id);
    }
    public String getDisplayStatus() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Bangkok"));
        if (startDate != null && today.isBefore(startDate.toLocalDate())) {
            return "Sắp áp dụng";
        }
        if (endDate != null && today.isAfter(endDate.toLocalDate())) {
            return "Đã kết thúc";
        }
        return "Đang áp dụng";
    }
}
