import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Crypto {
    static Cipher cipher;
    static String key = "aesEncryptionKey";
    static String iv = "encryptionIntVec";
    static Signature signature ;
    public static SecretKey  GenerateSessionKey() throws NoSuchAlgorithmException, UnsupportedEncodingException {
    //    return new SecretKeySpec(Arrays.copyOf(key.getBytes(StandardCharsets.UTF_8), 16), "AES");
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        return keygenerator.generateKey();
    }
    public static KeyPair generateKeyPair()
            throws Exception
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    public static SecretKey createAESKey(String mobile)
            throws Exception {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec passwordBasedEncryptionKeySpec = new PBEKeySpec(mobile.toCharArray() , key.getBytes() , 12288 , 256);
        SecretKey secretKeyFromPBKDF2 = secretKeyFactory.generateSecret(passwordBasedEncryptionKeySpec);
        return  new SecretKeySpec(secretKeyFromPBKDF2.getEncoded(), "AES");
    }

    public static byte[] encryptPGP(String plaintext , PublicKey KEY )
            throws Exception {
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, KEY);

        return cipher.doFinal(plaintext.getBytes());
    }
    public static String decryptPGP(byte[] plaintextcipher, PrivateKey KEY)
            throws Exception {
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,
                KEY);
        byte[] result
                = cipher.doFinal(plaintextcipher);
        return new String(result);
    }
    public static String encrypt(String plainText , PublicKey KEY)
            throws Exception {
        cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        byte[] plainTextByte = plainText.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, KEY , ivParameterSpec);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedText = encoder.encodeToString(encryptedByte);
        return encryptedText;
    }
    public static String decrypt(String encryptedText,PrivateKey KEY )
            throws Exception {
        cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        Base64.Decoder decoder = Base64.getDecoder();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, KEY , ivParameterSpec);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }
    public static SecretKey ConvertToSecretKey(String str){
        byte[] decodedKey = Base64.getDecoder().decode(str);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
    public static String ConvertToString(SecretKey secretKey){
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static byte[] CalculationSignature(PrivateKey privateKey , String data ) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] s = signature.sign();
        //System.out.println("BYIE IN Clac Signature : " + DatatypeConverter.printHexBinary(s));

//        signature.initVerify(publicKeytest);
//        signature.update(data.getBytes());
//        System.out.println("RESULT SIGN : " + signature.verify(s));
        return s;
}
    public static boolean VERIFYINGSIGN(byte[] sign, String data, PublicKey publicKeyClient) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //System.out.println("BYIE IN Verif Signature : " + DatatypeConverter.printHexBinary(sign));
        signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKeyClient);
        signature.update(data.getBytes());
        return signature.verify(sign);
    }



}
