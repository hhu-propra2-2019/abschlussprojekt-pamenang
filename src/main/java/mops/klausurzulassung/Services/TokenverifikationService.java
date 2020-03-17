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


        StringBuilder fachID = new StringBuilder();
        char[] charArray = quittung.toCharArray();
        char[] token = Arrays.copyOfRange(charArray, 0, 88);
        char[] matrChar = Arrays.copyOfRange(charArray, 88, 95);
        for(int i=95;i<charArray.length;i++){
            fachID.append(charArray[i]);

        }

        StringBuilder matr = new StringBuilder();
        for(int i = 0; i < matrChar.length; i++){
          matr.append(matrChar[i]);
        }
        logger.debug("Matrikelnummer: " + matr + " FachID: "+fachID);

        String HashValue = matr.toString()+fachID.toString();
        PublicKey publicKey = quittungService.findPublicKeyByQuittung(matr.toString(), fachID.toString());
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
