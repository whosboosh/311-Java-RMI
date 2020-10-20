public class ImplRMI implements RMIService {

    public AuctionItem getSpec(int itemId, int clientId) {
        return new AuctionItem("Chair", "A good chair", itemId);
    }
    public void sendMsg(String string) {
        System.out.println(string);
    }
}
