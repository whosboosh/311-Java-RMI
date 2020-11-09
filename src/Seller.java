import java.io.Serializable;

public class Seller implements Serializable {
    public Seller(Integer id) {
        this.id = id;
    }
    private Integer id;
    public Integer getId() {return id;}

}
