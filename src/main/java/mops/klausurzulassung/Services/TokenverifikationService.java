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

  public long[] verifikationToken(String quittung) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoPublicKeyInDatabaseException {

    quittung =  quittung.replaceAll("@", "/");

    String[] splitArray = quittung.split("§", 3);
    if(splitArray.length < 3){
      logger.error("Token fehlerhaft");
      return new long[]{-1, -1};
    }
    String token = splitArray[0];
    String base64Matr = splitArray[1];
    String base64ModulID = splitArray[2];

    byte[] matrByte = Base64.getDecoder().decode(base64Matr);
    String matr = new String(matrByte);
    byte[] modulIDByte = Base64.getDecoder().decode(base64ModulID);
    String modulID = new String(modulIDByte);

    String hashValue = matr+modulID;
    PublicKey publicKey = quittungService.findPublicKey(matr, modulID);
    if(publicKey == null){
      logger.error("Public Key ist null");
      return new long[]{-1, -1};
    }

    Signature sign = Signature.getInstance("SHA256withRSA");
    sign.initVerify(publicKey);
    byte[] hashValueBytes = hashValue.getBytes(StandardCharsets.UTF_8);
    sign.update(hashValueBytes);
    byte[] tokenByte = Base64.getDecoder().decode(String.valueOf(token));

    if(sign.verify(tokenByte)){
      logger.debug("Token Verifiziert");
      return new long[]{Long.parseLong(matr), Long.parseLong(modulID)};
    }
    return new long[]{-1, -1};
  }
}
