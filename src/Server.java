import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;

public class Server {

    //room all clients enter
    //everytime a client enters, broadcast client name
    //client send name first (verify unused name)
    //each client has his name and it is added during broadcasting
    //creating client commands
    //creating server commands
    Socket server;
    CSR csrserver ;
    PublicKey publicKey;
    PrivateKey privateKey;
    PublicKey publicKeyClient;
    PublicKey publicKeyCA;
    SecretKey sessionKeyServer;
    ArrayList<String> clientNames = new ArrayList<>();
    ArrayList<String> clientNumbers = new ArrayList<>();
    final ArrayList<ClientHandler> clientHandlerArrayList = new ArrayList<>();
    boolean serverIsOn;

    Server() {
        serverIsOn = true;
        try {
           KeyPair kp = Crypto.generateKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
            CreateCSR();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Connection connection = javaDB.getConnection();

        //  if (connection!=null)
        clientNumbers = javaDB.getClientNumber() ;
        try {
            //Data type تقوم بعمل Socket من نوع استماع
            //حيث تستسمع للاتصالات وتجهز بيانات المتصل وتعيد socket فيهاالبيانات
            ServerSocket serverSocket = new ServerSocket(22000);

            /*
            كود سحب الأرقام من قاعدة البيانات
            وتخزينها في clientNumber
             */

            //for accepting the connection
            //اي اتصالات قادمة يتم قبولها هنا
            Thread handlingIncomingConnections = new Thread() {
                @Override
                public void run() {
                    try {
                        while (serverIsOn) {
                            System.out.println("server is on");
                            Socket clientSocket = serverSocket.accept();
                            System.out.println("Client Accepted");
                            ClientHandler clientHandler0 = new ClientHandler(clientSocket);
                            //The code for check the information in the database
                            if(javaDB.checkInformationInDataBase( clientHandler0.getClientNumber() , clientHandler0.getClientPassword()))
                            {
                                System.out.println("Login is done");

                                //إضافة رقم العميل لمصفوفة العملاء المرتبطين بالسيرفر
                                // فك التعليق سيكون بعد الإضافة من الداتا بيز
                                //  clientNumbers.add(clientHandler0.getClientNumber()) ;
                                javaDB.UpdatePublicKey(DatatypeConverter.printHexBinary(clientHandler0.getPublicKey().getEncoded()),
                                        clientHandler0.getClientNumber());
                                clientHandler0.sendOtherClientsNumbers(clientNumbers) ;
                                //publicKeyClient = clientHandler0.getPublicKey();
                                //إدخال العميل
                                synchronized (clientHandlerArrayList) {
                                    clientHandlerArrayList.add(clientHandler0) ;
                                }

                                //clientHandler0.sendPublicKey(publicKey);

                                //هنا يتم التأكد هل الكلاينت الذي أريد التواصل معه في حالة اتصال مع السيرفر
                                //إذا كان موجود يتم التواصل معه و إلا انتظار دخول هذا العميل لإرسال الرسائل له
                                String connectionNumber = clientHandler0.receiveConnectionNumber() ;
                                clientHandler0.sendCSR(csrserver);

                                //if is it Client???
                                clientHandler0.checkIfClient(publicKeyCA);


                                clientHandler0.sendPublicKeyOther(connectionNumber);
                                //SecretKey KEY = clientHandler0.receiveKey();
                                for (int i = 0 ; i < clientHandlerArrayList.size() ; i++)
                                {
                                    if (clientHandler0.getConnectionNumber().equals(clientHandlerArrayList.get(i).getClientNumber()))
                                    {
                                        System.out.println("hhhhhhhhhhhhhhhhhhaaaaaaaaaaaaaaaaaa");
                                        clientHandler0.start();
                                        clientHandlerArrayList.get(i).start();
                                        clientHandler0.makeConnectionWithAnotherClient(clientHandlerArrayList.get(i) ) ;
                                    }
                                }
        }
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



        }catch(IOException e){
            e.printStackTrace();
        }
    }


    public void CreateCSR() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String Subject = "https://Server.com";
        csrserver = new CSR(Subject , publicKey,null,null);
        try {
            InetAddress ip = InetAddress.getLocalHost() ;
            server = new Socket(ip , 20000);
            ObjectOutputStream writeobjectToServerCA = new ObjectOutputStream(server.getOutputStream());
            writeobjectToServerCA.writeObject(csrserver);
            // response Calling from CA and ensure public key is me ...
            ObjectInputStream readobjectFromServerCA = new ObjectInputStream(server.getInputStream());
            csrserver = (CSR) readobjectFromServerCA.readObject();
            publicKeyCA = csrserver.getPublicKeyCA();
            boolean isSign = Crypto.VERIFYINGSIGN(csrserver.getSign(),csrserver.getSubject(),publicKeyCA);
            if(isSign){
                System.out.println("ServerGetItCA");
            }else{
                try {
                    throw new Exception("Server Is not Secure");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        }
     }


    public void printAllUserNames()
    {
        System.out.println(clientNames);
    }

    public void printUserCount()
    {
        System.out.println(clientNames.size());
    }

}

