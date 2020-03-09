package mops.klausurzulassung.Services.Token;

import mops.klausurzulassung.Services.Token.Entities.QuittungDao;
import mops.klausurzulassung.Services.Token.Entities.QuittungDto;
import mops.klausurzulassung.Services.Token.Services.QuittungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

@Service
public class TokengenerierungService {

    private final QuittungService quittungService;

    @Autowired
    public TokengenerierungService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public String erstellenHashValue(String matr, String fach){
        return matr+fach;
    }

    public String erstellenQuittung(String matr, String fach, String token){
        return matr+fach+token;
    }

    public String erstellenToken(String matr, String fachID) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        String HashValue = erstellenHashValue(matr, fachID);

        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privateKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        byte[] hashValueBytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        PublicKey publicKey = pair.getPublic();
        byte[] token = sign.sign();

        QuittungDto quittungDto = new QuittungDto(matr, fachID, publicKey, bytesToHex(token));
        QuittungDao quittungDao = erstelleQuittungDao(quittungDto);

        quittungService.save(quittungDao);

        return bytesToHex(token);
    }

    private KeyPair KeyPaarGenerierung() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
    }

    String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private QuittungDao erstelleQuittungDao(QuittungDto quittungDto){
        QuittungDao quittungDao = new QuittungDao();
        quittungDao.setFachID(quittungDto.getFachID());
        quittungDao.setMatrikelnummer(quittungDto.getMatrikelnummer());
        quittungDao.setPublicKey(quittungDto.getPublicKey());
        quittungDao.setToken(quittungDto.getToken());
        return quittungDao;

    }
}
