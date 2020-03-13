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
import java.util.Base64;

@Service
public class TokenverifikationService {

    private final QuittungService quittungService;
    private Logger logger = LoggerFactory.getLogger(TokenverifikationService.class);

    @Autowired
    public TokenverifikationService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public boolean verifikationToken(String matr, String fachID, String token) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoPublicKeyInDatabaseException {

        if(token.length() != 88){
            return false;
        }

        token =  token.replaceAll("@", "/");

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
        byte[] tokenByte = Base64.getDecoder().decode(token);
        logger.debug("Token Verifiziert");
        return sign.verify(tokenByte);
    }
}
