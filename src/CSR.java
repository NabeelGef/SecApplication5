import java.io.Serializable;
import java.security.PublicKey;

public class CSR implements Serializable {
    String subject;
    PublicKey publicKey;
    PublicKey publicKeyCA;
    byte[] sign;

    public PublicKey getPublicKeyCA() {
        return publicKeyCA;
    }

    public CSR(String subject, PublicKey publicKey, PublicKey publicKeyCA, byte[] sign) {
        this.subject = subject;
        this.publicKey = publicKey;
        this.publicKeyCA = publicKeyCA;
        this.sign = sign;
    }

    public String getSubject() {
        return subject;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] getSign() {
        return sign;
    }
}
