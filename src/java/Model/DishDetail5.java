package Model;

import java.io.Serializable;

/** Một kích cỡ và giá bán cụ thể của món ăn. */
public class DishDetail5 implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String size;
    private double price;
    private Dish5 dish;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public Dish5 getDish() {
        return dish;
    }
    public void setDish(Dish5 dish) {
        this.dish = dish;
    }
}
