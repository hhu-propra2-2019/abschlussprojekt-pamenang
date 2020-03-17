package mops.klausurzulassung.Services;

import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
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
import java.util.Arrays;
import java.util.Base64;

@Service
public class TokenverifikationService {

    private final QuittungService quittungService;
    private Logger logger = LoggerFactory.getLogger(TokenverifikationService.class);

    @Autowired
    public TokenverifikationService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public boolean verifikationToken(String quittung) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoPublicKeyInDatabaseException {

        quittung =  quittung.replaceAll("@", "/");

        String[] splitArray = quittung.split("#", 3);
        String token = splitArray[0];
        String matr = splitArray[1];
        String fachID = splitArray[2];

        logger.debug("Matrikelnummer: " + matr + " FachID: "+fachID);

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
        byte[] tokenByte = Base64.getDecoder().decode(String.valueOf(token));
        logger.debug("Token Verifiziert");
        return sign.verify(tokenByte);
    }
}
