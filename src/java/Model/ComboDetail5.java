package Model;

import java.io.Serializable;

/** Một dòng món trong combo, gồm kích cỡ đã chọn và số lượng. */
public class ComboDetail5 implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer quantity;
    private DishDetail5 dishDetail;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public DishDetail5 getDishDetail() {
        return dishDetail;
    }
    public void setDishDetail(DishDetail5 dishDetail) {
        this.dishDetail = dishDetail;
    }
}
