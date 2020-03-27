package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.QuittungDao;
import mops.klausurzulassung.domain.QuittungDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

@Service
public class TokengenerierungService {

  private Logger logger = LoggerFactory.getLogger(TokengenerierungService.class);
  private final QuittungService quittungService;

  @Autowired
  public TokengenerierungService(QuittungService quittungService) {
    this.quittungService = quittungService;
  }

  /**
   * This method creates a Hashvalue containing the Modul-ID and the ID-Number
   *
   * @param matr contains the Identificationnumber of the Student
   * @param modulID contains the Id of a individually created Modul from an Organizer
   * @return Hashvalue as String
   */
  public String erstellenHashValue(String matr, String modulID) {
    return matr + modulID;
  }

  /**
   * This method generates a Receipt based on the ID-Number and Modul-ID.
   * The Token is encoded by the "SHA256 with RSA" Algorithm.
   * The Algorithm uses Keypairgeneration.
   * The Private-Key is used to encode the Receipt. And the Public-Key is saved into the database which
   * is later used to decode the Receipt.
   * The Receipt is compressed with Base64 for late usage in the controller.
   * Due to Restrictions the character '/' is replaced with an '@'
   *
   * @param matr contains the Identificationnumber of the Student
   * @param modulID contains the Id of a individually created Modul from an Organizer
   * @return Receipt as a String
   */
  public String erstellenToken(String matr, String modulID) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    String hashValue = erstellenHashValue(matr, modulID);
    KeyPair pair = KeyPaarGenerierung();
    PrivateKey privateKey = pair.getPrivate();
    Signature sign = Signature.getInstance("SHA256withRSA");

    sign.initSign(privateKey);
    byte[] hashValueBytes = hashValue.getBytes(StandardCharsets.UTF_8);
    sign.update(hashValueBytes);

    PublicKey publicKey = pair.getPublic();
    byte[] token = sign.sign();

    String base64Token = Base64.getEncoder().encodeToString(token);
    String base64Matr = Base64.getEncoder().encodeToString(matr.getBytes());
    String base64ModulID = Base64.getEncoder().encodeToString(modulID.getBytes());

    String quittung = base64Token + "§" + base64Matr + "§" + base64ModulID;

    // Slash wird ersetzt, da sonst Fehler bei Linkgenierierung auftreten
    quittung = quittung.replaceAll("/", "@");

    logger.debug("Quittung wurde erstellt und ist encoded:" + quittung);


    QuittungDto quittungDto = new QuittungDto(matr, modulID, publicKey, quittung);
    QuittungDao quittungDao = erstelleQuittungDao(quittungDto);

    quittungService.save(quittungDao);
    logger.debug("Speichere Quittung von  Student: " + quittungDao.getMatrikelnummer() + " in Datenbank");

    return quittung;
  }

  /**
   * This method generates random KeyPairs with RSA-Encoding
   *
   * @return Keypair-Object
   */
  private KeyPair KeyPaarGenerierung() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    keyPairGen.initialize(512);
    logger.debug("Generiere KeyPaar");
    return keyPairGen.generateKeyPair();
  }

  /**
   * This method generates a Receipt-Dao for Presistence
   *
   * @param quittungDto contains the Information of the Receipt
   * @return quittingDao-object
   */
  private QuittungDao erstelleQuittungDao(QuittungDto quittungDto) {
    QuittungDao quittungDao = new QuittungDao();
    quittungDao.setModulId(quittungDto.getModulId());
    quittungDao.setMatrikelnummer(quittungDto.getMatrikelnummer());
    quittungDao.setPublicKey(quittungDto.getPublicKey());
    quittungDao.setQuittung(quittungDto.getQuittung());
    return quittungDao;

  }
}
