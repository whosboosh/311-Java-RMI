import javax.crypto.Cipher;
import java.io.Serializable;

public class ClientRequest implements Serializable {
    public ClientRequest(int clientId) {
        this.clientId = clientId;
    }
    public int getClientId() {
        return clientId;
    }
    private int clientId;
}
