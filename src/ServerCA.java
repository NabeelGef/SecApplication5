import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class ServerCA {

   public static PublicKey publicKeyCA;
   public static PrivateKey privateKeyCA;
   boolean serverIsOn;
   public static PublicKey publicKeyOther;
   public static String subjectOther;
   final ArrayList<ClientHandlerWithCertificate> clientHandlerWithCertificates = new ArrayList<>();
    ServerCA(){
        serverIsOn = true;
        try {
            KeyPair keyPair = Crypto.generateKeyPair();
            publicKeyCA = keyPair.getPublic();
            privateKeyCA = keyPair.getPrivate();
            ServerSocket serverSocket = new ServerSocket(20000);
            Thread handlingIncomingConnections = new Thread() {
                @Override
                public void run() {
                    try {
                        while (serverIsOn) {
                            System.out.println("serverCA is on");
                            Socket clientSocket = serverSocket.accept();
                            System.out.println("Client Accepted");
                            ClientHandlerWithCertificate clientHandler0 = new ClientHandlerWithCertificate(clientSocket);


                                synchronized (clientHandlerWithCertificates) {
                                    clientHandlerWithCertificates.add(clientHandler0) ;
                                }


                                 CSR csr = clientHandler0.recieveCSR();
                                 publicKeyOther = csr.getPublicKey();
                                 subjectOther = csr.getSubject();
                                      // Ringing it's Phone to check if Public Key Other is it ...
                                byte[] sign = Crypto.CalculationSignature(privateKeyCA ,subjectOther);
                                clientHandler0.sendCSR(subjectOther,publicKeyOther,sign , publicKeyCA);
                                
                                //هنا يتم التأكد هل الكلاينت الذي أريد التواصل معه في حالة اتصال مع السيرفر
                                //إذا كان موجود يتم التواصل معه و إلا انتظار دخول هذا العميل لإرسال الرسائل له

                            /*else{
                                Person person = new Person();
                                person.setNumber(Integer.parseInt(clientHandler0.clientNumber));
                                person.setPassword(clientHandler0.getClientPassword());
                                person.setClient_key(generateKey());
                                javaDB.insert(person);//End of if statement
                            }*/
                        }//End of while statement
                    } catch (Exception ex) {
                        ex.printStackTrace() ;
                    }
                }//End of run method
            }; //End of handlingIncomingConnections
            handlingIncomingConnections.start() ;



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static PublicKey GetPublicKeyCA(){
        return publicKeyCA;
    }
}
