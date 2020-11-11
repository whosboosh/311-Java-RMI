import java.io.Serializable;
public class Buyer implements Serializable {

    public Buyer(String name, String email, Integer id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }
    private String name;
    private String email;
    private Integer id;

    private Boolean hasSold = false;

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public Integer getId() {
        return id;
    }

    public void pollServerIfWon(RMIService stub, Integer itemId) {
        new Thread( new Runnable() {
            boolean hasSold = false;
            public void run() {
                while (!hasSold) {
                    try {
                        AuctionItem item = stub.getAuctionItem(itemId);
                        hasSold = item.isSold();
                        if (hasSold) {
                            if (item.getWinningBuyerId() == null) System.out.println("You've didn't win... ID: " + item.getId() + " " + item.getName() + " " + item.getDescription());
                            else System.out.println("You've won! ID: " + item.getId() + " " + item.getName() + " " + item.getDescription());
                            break;
                        }
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {
                        System.err.println("Client Exception "+e.toString());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}