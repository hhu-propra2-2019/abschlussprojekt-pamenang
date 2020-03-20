package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.exceptions.InvalidToken;
import mops.klausurzulassung.exceptions.NoPublicKeyInDatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

@Service
public class TokenverifikationService {

  private final QuittungService quittungService;
  private final StudentService studentService;
  private Logger logger = LoggerFactory.getLogger(TokenverifikationService.class);

  @Autowired
  public TokenverifikationService(QuittungService quittungService, StudentService studentService) {
    this.quittungService = quittungService;
    this.studentService = studentService;
  }

  public void verifikationToken(String quittung) throws NoSuchAlgorithmException, SignatureException,
      NoPublicKeyInDatabaseException, InvalidKeyException, InvalidToken {

    quittung =  quittung.replaceAll("@", "/");

    String[] splitArray = quittung.split("§", 3);
    if(splitArray.length < 3){
      logger.error("Token fehlerhaft");
      throw new InvalidToken("Token ist fehlerhaft");
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
      return;
    }

    Signature sign = Signature.getInstance("SHA256withRSA");
    sign.initVerify(publicKey);
    byte[] hashValueBytes = hashValue.getBytes(StandardCharsets.UTF_8);
    sign.update(hashValueBytes);
    byte[] tokenByte = Base64.getDecoder().decode(String.valueOf(token));

    if(sign.verify(tokenByte)){
      logger.debug("Token Verifiziert");
      logger.debug("ModulID: " + modulID);
      logger.debug("Matrikelnummer : " + matr);
      Student student = Student.builder()
          .matrikelnummer(Long.parseLong(matr))
          .modulId(Long.parseLong(modulID)).build();
      studentService.save(student);
      logger.debug("Altzulassung erfolgreich!");
    }
  }
}
