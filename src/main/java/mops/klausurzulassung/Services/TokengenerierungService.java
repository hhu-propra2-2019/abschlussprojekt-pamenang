package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.QuittungDao;
import mops.klausurzulassung.Domain.QuittungDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Arrays;
import java.util.Base64;

@Service
public class TokengenerierungService {

    private Logger logger = LoggerFactory.getLogger(TokengenerierungService.class);
    private final QuittungService quittungService;

    @Autowired
    public TokengenerierungService(QuittungService quittungService) {
        this.quittungService = quittungService;
    }

    public String erstellenHashValue(String matr, String fach){
        return matr+fach;
    }

    public String erstellenToken(String matr, String fachID) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        String hashValue = erstellenHashValue(matr, fachID);
        KeyPair pair = KeyPaarGenerierung();
        PrivateKey privateKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withRSA");
        logger.debug("Sign SHA256 with RSA");

        sign.initSign(privateKey);
        byte[] hashValueBytes = hashValue.getBytes(StandardCharsets.UTF_8);
        sign.update(hashValueBytes);

        PublicKey publicKey = pair.getPublic();
        byte[] token = sign.sign();

        String quittung = Arrays.toString(token) + "#" + matr + "#" + fachID;

        String base64Quittung = Base64.getEncoder().encodeToString(quittung.getBytes());

        base64Quittung = base64Quittung.replaceAll("/", "@");


        QuittungDto quittungDto = new QuittungDto(matr, fachID,publicKey, base64Quittung);
        QuittungDao quittungDao = erstelleQuittungDao(quittungDto);

        quittungService.save(quittungDao);
        logger.debug("Speichere Quittung von  Student: "+quittungDao.getMatrikelnummer()+ " in Datenbank");

        return base64Quittung;
    }

    private KeyPair KeyPaarGenerierung() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(512);
        logger.debug("Generiere KeyPaar");
        return keyPairGen.generateKeyPair();
    }

    private QuittungDao erstelleQuittungDao(QuittungDto quittungDto){
        QuittungDao quittungDao = new QuittungDao();
        quittungDao.setModulId(quittungDto.getModulId());
        quittungDao.setMatrikelnummer(quittungDto.getMatrikelnummer());
        quittungDao.setPublicKey(quittungDto.getPublicKey());
        quittungDao.setQuittung(quittungDto.getQuittung());
        return quittungDao;

    }
}
