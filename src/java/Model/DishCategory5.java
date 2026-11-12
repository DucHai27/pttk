package Model;

import java.io.Serializable;

/** Thể loại món ăn. */
public class DishCategory5 implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String description;
    private Dish5[] dishes = new Dish5[0];

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

    public Dish5[] getDishes() {
        return dishes;
    }
    public void setDishes(Dish5[] dishes) {
        this.dishes = dishes;
    }
}
