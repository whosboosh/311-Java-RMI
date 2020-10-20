import java.io.Serializable;

public class AuctionItem implements Serializable {
    AuctionItem(String name1, String description1, Integer id1) {
        name = name1;
        description = description1;
        id = id1;
    }
    private String description;
    private String name;
    private Integer id;

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    public Integer getId() {
        return id;
    }
}
