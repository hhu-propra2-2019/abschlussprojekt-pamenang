package mops.klausurzulassung.Token;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

public class Keyerstellung {

    KeyGenerator keyGenerator;

    private SecretKey erstelleKey() throws NoSuchAlgorithmException {
        keyGenerator = KeyGenerator.getInstance("AES");

        //Initialisieren des KeyGenerators
        SecureRandom secureRandom = new SecureRandom();
        int keyBitSize = 256;
        keyGenerator.init(keyBitSize, secureRandom);

        //Generieren des Keys
        return keyGenerator.generateKey();
    }

    private void erstelleKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] keyStorePassword = "123abc".toCharArray();
        try(InputStream keyStoreData = new FileInputStream("keystore.ks")){
            keyStore.load(keyStoreData, keyStorePassword);
        }

    }
}
