import java.io.Serializable;
import java.security.PublicKey;

public interface Client extends Serializable {

    public void authoriseServer(RMIService stub);
    public byte[] challengeClient(byte[] message);
    public Integer getId();
    public PublicKey getPublicKey();
    public boolean getIsServerAuthorised();

}
