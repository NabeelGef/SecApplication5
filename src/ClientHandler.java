import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    //a thread for each client

    //the class handle the received message
    //and send the messages to the same client
    Socket client;
    SecretKey KEY;
CSR clientcsr;
    byte[] dataSignature;
    public boolean isOn ;
    DataInputStream inputStream ;
    DataOutputStream outputStream ;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    String clientNumber ;
    String clientPassword ;
    PublicKey publicKeyClient;
    SecretKey sessionKey;
    //BufferedReader inputStream ;
    //PrintWriter outputStream ;
    String connectionNumber ;
    String name ;
    final ArrayList<String> receivedMessages = new ArrayList<>() ;

    /*void INITCRYPTO(String KEY1 , String KEY2 , String IV1 ,String IV2 ){
        this.KEY1 = KEY1;
        this.KEY2 = KEY2;
        this.IV1 = IV1;
        this.IV2 = IV2;
    }*/
    ClientHandler(Socket clientSocket)
    {
        client = clientSocket ;
        isOn=true ;
        clientNumber = "";
        clientPassword = "" ;


        try {
            inputStream = new DataInputStream(client.getInputStream()) ;
            outputStream = new DataOutputStream(client.getOutputStream()) ;
            objectInputStream = new ObjectInputStream(client.getInputStream());
            //inputStream = new BufferedReader(new InputStreamReader(client.getInputStream())) ;
            //outputStream = new PrintWriter((client.getOutputStream())) ;

            try {

                //read the information of the clients
           /* this.clientNumber = inputStream.readUTF() ;
            this.clientPassword = inputStream.readUTF() ;
            this.connectionNumber = inputStream.readUTF() ;*/
                String register ;
                int i =0 ;
                while (inputStream!=null)
                {
                    register = inputStream.readUTF() ;
                    if (i==0)
                    {
                        System.out.println("this is first message");
                        this.clientNumber = register ;
                        System.out.println("The client number : " + this.clientNumber);
                        i++ ;
                    }

                    else if (i==1)
                    {
                        System.out.println("this is second message");
                        this.clientPassword = register ;
                        System.out.println("The client password : " + this.clientPassword);
                        i++ ;
                    }

                    if (i==2)
                        break;
                }
                //System.out.println("this is public Key for Client : " + publicKeyClient);
                this.publicKeyClient = (PublicKey) objectInputStream.readObject();

            }catch (IOException | ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
        }catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public SecretKey getKEY(){return KEY;}
    public SecretKey receiveKey() throws Exception {
        this.KEY = Crypto.createAESKey(this.getClientNumber());
        return this.KEY;
    }
    @Override
    public void run() {
        try {
            String str ="";
            int i = 0;
            //Received the messages from the current client(in this class)
            //check Signature For Client
            while (isOn)
            {
                str = inputStream.readUTF() ;
                System.out.println("Server Come it : "+str);
                synchronized (receivedMessages)
                      {
                          receivedMessages.add(str);
                      }
            }
        }catch (Exception ex )
        {
            ex.printStackTrace();
        }
        /*if (outputStream!=null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (inputStream!=null)
            inputStream.close();

        }catch (IOException ex)
        {
            ex.printStackTrace();
        }

        try {
            if (client!=null)
            client.close();

        }catch (IOException ex)
        {
            ex.printStackTrace();
        }*/
    }

    public PublicKey getPublicKey() {
        return publicKeyClient;
    }

    public void sendMessage(String message)
    {
        try {
            if (isOn)
                outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void closeAll()
    {
        isOn = false ;
        receivedMessages.clear();
        try {inputStream.close();}catch (Exception e){}
        try {outputStream.close();}catch (Exception e){}
        try {client.close();}catch (Exception e){}
    }

    public String getClientNumber()
    {
        return clientNumber ;
    }
    public String getConnectionNumber()
    {
        return  connectionNumber ;
    }
    public String getClientPassword()
    {
        return clientPassword ;
    }
    public ArrayList<String> getReceivedMessages()
    {
        return receivedMessages;
    }



    public void makeConnectionWithAnotherClient(ClientHandler otherClient) throws IOException
    {


        Thread handleMessages = new Thread(){
            @Override
            public void run() {

                ArrayList<String> messages = new ArrayList<>();
                while (true) {
                    messages = getReceivedMessages() ;
                    if (!messages.isEmpty()) {
                        synchronized (messages) {
                            for (int i = 0; i < messages.size(); i++) {
                                //send the messages to the other client
                                try {
                                    //otherClient.sendMessage(messages.get(i));
                                    otherClient.sendMessage(messages.get(i));
                                    } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            messages.clear();
                        }
                    }


                    //Received the messages from the first client(clientHandler2)
                    messages = otherClient.getReceivedMessages();
                    if (!messages.isEmpty()) {
                        synchronized (messages) {
                            for (int i = 0; i < messages.size(); i++) {
                                //send the messages to the other client
                                try {
                                    sendMessage(messages.get(i));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            messages.clear();
                        }
                    }
                    try {
                        Thread.sleep(5);
                    }catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        };
        handleMessages.start();
    }



    //لاستقبال الرَّقْم المراد التواصل معه
    public String receiveConnectionNumber()
    {
        try {
            System.out.print("i want  : ");
            this.connectionNumber = inputStream.readUTF() ;
            System.out.println(this.connectionNumber);
            return this.connectionNumber ;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void sendOtherClientsNumbers(ArrayList<String> clientNumbers)
    {

        try {
            //إرسال عدد الإرقام المراد عرضها لدى المستخدم
            sendMessage(clientNumbers.size()+"");
            for(int i = 1 ; i <= clientNumbers.size() ; i++)
            {
                if (!getClientNumber().equals(clientNumbers.get(i-1)))
                    sendMessage(i+"- "+clientNumbers.get(i-1));
            }

        }catch (NullPointerException ex)
        {
            ex.printStackTrace();
        }

    }







    public void sendCSR(CSR csrserver) throws IOException {
        objectOutputStream = new ObjectOutputStream(client.getOutputStream());
    objectOutputStream.writeObject(csrserver);
    }

    public void sendPublicKeyOther(String connectionNumber) throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
    objectOutputStream = new ObjectOutputStream(client.getOutputStream());
    objectOutputStream.writeObject(javaDB.getClientKey(connectionNumber));
    }

    public void checkIfClient(PublicKey publicKeyCA) throws Exception {
     objectInputStream = new ObjectInputStream(client.getInputStream());
     clientcsr = (CSR) objectInputStream.readObject();
     String serversubject = clientcsr.getSubject();

        byte[] datasign = clientcsr.getSign();
        if(Crypto.VERIFYINGSIGN(datasign,serversubject,publicKeyCA)){
            System.out.println("\nYour Connection Is Secure...");
        }
        else{
            throw new Exception("Your Connection Isn't Secure!!!");
        }
    }
}
