package mops.klausurzulassung.Token;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import java.security.*;

public class Tokengenerierung {

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        signatur();
    }

    public static String hashing(String HashValue){

        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putString(HashValue, Charsets.UTF_8);
        HashCode token = hasher.hash();

        return token.toString();
    }

    public static String erstellenHashValue(String matr, String fach, String key){
        return matr+fach+key;
    }

    public static String erstellenQuittung(String matr, String fach, String token){
        return matr+fach+token;
    }

    public static String signatur() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        //Initializing the key pair generator
        keyPairGen.initialize(2048);

        //Generate the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();

        //Getting the privatekey from the key pair
        PrivateKey privKey = pair.getPrivate();

        //Creating a Signature object
        Signature sign = Signature.getInstance("SHA256withRSA");

        //Initializing the signature
        sign.initSign(privKey);
        byte[] bytes = "Hello how are you".getBytes();

        //Adding data to the signature
        sign.update(bytes);

        //Calculating the signature
        byte[] signature = sign.sign();


        String string = new String(signature);
        System.out.println(string);
        return string;

    }
}
