import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class Client {
    PublicKey publicKey;
    PrivateKey privateKey;

    boolean isOn;
    String clientCount ;
    Socket other , client;
    static Cipher cipher;
    static PublicKey publicKeyCA , publicKeyOther;
    DataInputStream otherReadSource ;
    DataOutputStream otherWriteSource ;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    SecretKey KEY ;
    CSR servercsr , clientcsr;
    String mobile;

    /*BufferedReader otherReadSource ;
    PrintWriter otherWriteSource ;
     */
    final ArrayList<String> otherClientNumbers = new ArrayList<>() ;
    Client(String clientNumber , String clientPassword) throws Exception {
        mobile = clientNumber;
        isOn = true ;
        Scanner myInput = new Scanner(System.in) ;
        clientCount = "";
        cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");

        try {
            InetAddress ip = InetAddress.getLocalHost() ;//if the client is the same laptop

            //if the ip for other client
            // InetAddress ip = InetAddress.getByName('the ip');
            // System.out.println("The ip " +ip);
            other = new Socket(ip , 22000); //the first parameter is my ip , the second is the server (other person is port) IP
            otherReadSource = new DataInputStream(other.getInputStream())  ;//قراءة ما وصل من المتصل الآخر

            //كتابة أي معلومات للطرف الآخر المتصل من خلال السوكيت
            otherWriteSource = new DataOutputStream(other.getOutputStream()) ;
            objectOutputStream = new ObjectOutputStream(other.getOutputStream());

            otherWriteSource.writeUTF(clientNumber);
            otherWriteSource.writeUTF(clientPassword);
            File f = new File(mobile+".txt");
            if(!f.exists()){
                KeyPair keyPair = Crypto.generateKeyPair();
                publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();
                SavePrivatPublicKeyInFile();
            }else{
                System.out.println("PUBLIC AND PRIVAT IS FOUND ALREADY");
                get_privatePublicKey();
            }
            //send my publicKey to server
            objectOutputStream.writeObject(publicKey);

            clientCount = otherReadSource.readUTF();

            //get the client numbers from the server
            //and printed it in the client console

            showClientsNumbers();



            //KEY = javaDB.getClientKey(clientNumber);

         //   KEY = Crypto.createAESKey(clientNumber);

            /*IV =getIVSecureRandom();*/



            //Choose the client to connect  with him

            CreateCSR(clientNumber);

            //send my publicKey to Server to save it



            System.out.print("\nChoose a client number :");
            String clientNumberOther =myInput.nextLine();
            //write number Other
            otherWriteSource.writeUTF(clientNumberOther);

            // Is It Server ???
            objectInputStream = new ObjectInputStream(other.getInputStream());
            servercsr = (CSR) objectInputStream.readObject();
            checkIfServer();
            // send my CSR
            objectOutputStream = new ObjectOutputStream(other.getOutputStream());
            objectOutputStream.writeObject(clientcsr);
            // read public Key Other

            objectInputStream = new ObjectInputStream(other.getInputStream());
            publicKeyOther = (PublicKey) objectInputStream.readObject();
            System.out.println("Public KEY OTHER : " + DatatypeConverter.printHexBinary(publicKeyOther.getEncoded()));
            Thread getFromOther = new Thread()
            {
                @Override
                public void run() {
                    try {
                        String serverResponse = "";
                        int i = 0 ;
                        while (isOn) {
                            //check Signature For Server
                            serverResponse = otherReadSource.readUTF();
                            //System.out.println("Client said : " + serverResponse);
                            serverResponse = Crypto.decryptPGP(DatatypeConverter.parseHexBinary(serverResponse),privateKey);
                            System.out.println("Your friend said : "+ serverResponse);

                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            };
            getFromOther.start();

            String serverResponse = "";
            while (true)
            {
                serverResponse = myInput.nextLine() ;
                if (serverResponse.equalsIgnoreCase("exit"))
                {
                    break;
                }
                // Send Crypto with public other
                byte[] data = Crypto.encryptPGP(serverResponse,publicKeyOther);
                otherWriteSource.writeUTF(DatatypeConverter.printHexBinary(data));
            }
            isOn = false ;
            otherWriteSource.close();
            otherReadSource.close();
            other.close();
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void checkIfServer() throws Exception {
        String serversubject = servercsr.getSubject();
        byte[] datasign = servercsr.getSign();
       if(Crypto.VERIFYINGSIGN(datasign,serversubject,publicKeyCA)){
           System.out.println("\nYour Connection Is Secure...");
       }
       else{
           throw new Exception("Your Connection Isn't Secure!!!");
       }
    }


    public void printClientNumbers()
    {
        for (int i = 1 ; i <= otherClientNumbers.size() ; i++ )
        {

            System.out.print(otherClientNumbers.get(i-1) + "      ");
            if (i%3==0)
                System.out.print("\n");
        }

    }
    public void handleMessages()
    {
        Thread clientThread = new Thread()
        {
            @Override
            public void run()
            {
                String serverResponse = "";
                try {

                    while (isOn)
                    {
                        serverResponse = otherReadSource.readUTF();
                        System.out.println("Other client said : " + serverResponse);
                    }

                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            }
        };
        clientThread.start();

    }
    public void getTheHandleMessages()
    {
        handleMessages();
    }

    public void showClientsNumbers()
    {
        try {

            //While there is a numbers are sets
            String numbers ;
            int k = 0 ;
            int p = Integer.parseInt(clientCount) ;
            System.out.println("numbers of clients : " + p);
            while (k <p-1)
            {
                numbers = otherReadSource.readUTF() ;
                synchronized (otherClientNumbers)
                {
                    //تعني الانتظار حتى الانتهاء من التعامل من المصفوفة في حال كان أحد يستخدمها لأننا نتعامل مع threads
                    otherClientNumbers.add(numbers);
                }
                k++ ;
            }
            printClientNumbers();
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }
    public void CreateCSR(String clientNumber) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String Subject = clientNumber;
         clientcsr = new CSR(Subject , publicKey,null,null);
        try {
            InetAddress ip = InetAddress.getLocalHost() ;
            client = new Socket(ip , 20000);
            ObjectOutputStream writeobjectToServerCA = new ObjectOutputStream(client.getOutputStream());
            writeobjectToServerCA.writeObject(clientcsr);
            // response Calling from CA and ensure public key is me ...
            ObjectInputStream readobjectFromServerCA = new ObjectInputStream(client.getInputStream());
            clientcsr = (CSR) readobjectFromServerCA.readObject();
            publicKeyCA = clientcsr.getPublicKeyCA();
            boolean isSign = Crypto.VERIFYINGSIGN(clientcsr.getSign(),clientcsr.getSubject(),publicKeyCA);
            if(isSign){
                System.out.println("\nClient Get CA");
                System.out.println("========================");
                System.out.println("CSR Client : " +"\n publicKey : " +
                        DatatypeConverter.printHexBinary(clientcsr.getPublicKeyCA().getEncoded()) + "\n My Subject is : " +
                        clientcsr.getSubject() + "\n The Signature is : " + DatatypeConverter.printHexBinary(clientcsr.getSign()));
                client.close();
            }else{
                try {
                    throw new Exception("Client Is not Secure");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public  void get_privatePublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pub,pr;
        BufferedReader brTest = new BufferedReader(new FileReader(mobile+".txt"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        pub = brTest .readLine();
        byte[]  buffer = DatatypeConverter.parseHexBinary(pub);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        publicKey = keyFactory.generatePublic(keySpec);
        //System.out.println("Public is : " + DatatypeConverter.printHexBinary(publicKey.getEncoded()));
        pr = brTest .readLine();
        byte[] clear = pr.getBytes();
        PKCS8EncodedKeySpec keySpec2 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pr));
        privateKey=keyFactory.generatePrivate(keySpec2);
        //System.out.println("Private is : " + DatatypeConverter.printHexBinary(privateKey.getEncoded()));
    }
    public void SavePrivatPublicKeyInFile()
    {
        try {
            FileWriter myWriter = new FileWriter(mobile + ".txt");
            myWriter.write(DatatypeConverter.printHexBinary(publicKey.getEncoded())+"\n");
            myWriter.write(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            myWriter.close();
            System.out.println("Successfully wrote to the file " + mobile);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }



}
