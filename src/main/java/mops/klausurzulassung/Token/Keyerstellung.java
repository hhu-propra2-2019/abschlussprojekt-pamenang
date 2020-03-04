package mops.klausurzulassung.Token;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
}
