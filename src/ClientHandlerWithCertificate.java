import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.PublicKey;

public class ClientHandlerWithCertificate {

    String Subject;
    PublicKey publicKey;
    Socket client;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
ClientHandlerWithCertificate(Socket clientSocket){

    client = clientSocket;
}
CSR recieveCSR() throws IOException, ClassNotFoundException {
    objectInputStream = new ObjectInputStream(client.getInputStream());
    return (CSR) objectInputStream.readObject();
}
    public void sendCSR(String subject , PublicKey publicKeyOther , byte[] sign , PublicKey publicKeyCA) throws IOException {
     objectOutputStream = new ObjectOutputStream(client.getOutputStream());
    CSR csr = new CSR(subject,publicKeyOther, publicKeyCA,sign);
    objectOutputStream.writeObject(csr);
}

}
