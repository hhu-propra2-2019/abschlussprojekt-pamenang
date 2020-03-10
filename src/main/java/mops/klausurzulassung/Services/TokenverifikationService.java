package mops.klausurzulassung.Services;

import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Services.QuittungService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.PublicKey;
import java.security.Signature;

@Service
public class TokenverifikationService {

    private final QuittungService quittungService;
    private Logger logger = LoggerFactory.getLogger(TokenverifikationService.class);

    @Autowired
    public TokenverifikationService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public boolean verifikationToken(String matr, String fachID, String token) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoPublicKeyInDatabaseException {

        String HashValue = matr+fachID;
        PublicKey publicKey = quittungService.findPublicKeyByQuittung(matr, fachID);
        if(publicKey == null){
            logger.error("Public Key ist null");
            return false;
        }

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        byte[] hashValueBytes = HashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);
        byte[] tokenByte = hexStringToByteArray(token);
        logger.debug("Token Verifiziert");
        return sign.verify(tokenByte);
    }

    byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
