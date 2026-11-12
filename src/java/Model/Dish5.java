package Model;

import java.io.Serializable;

/** Món ăn và danh sách kích cỡ của món đó. */
public class Dish5 implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String description;
    private String status;
    private String image;
    private DishDetail5[] dishDetails = new DishDetail5[0];

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

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public DishDetail5[] getDishDetails() {
        return dishDetails;
    }
    public void setDishDetails(DishDetail5[] dishDetails) {
        this.dishDetails = dishDetails;
    }

    public String getCode() {
        return String.format("MA%03d", id);
    }
    public boolean isAvailable() {
        return "Đang phục vụ".equals(status);
    }
}
