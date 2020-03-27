package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.QuittungDao;
import mops.klausurzulassung.domain.QuittungDto;
import mops.klausurzulassung.exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.repositories.QuittungRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
public class QuittungService {
  private final QuittungRepository quittungRepository;
  private Logger logger = LoggerFactory.getLogger(TokengenerierungService.class);

  @Autowired
  public QuittungService(QuittungRepository quittungRepository) {
    this.quittungRepository = quittungRepository;
  }

  /**
   * This method finds the specific PublicKey which depends on the ID-Number of the Student and the Modul-ID
   *
   * @param matr contains the Identificationnumber of the Student
   * @param modulID contains the Id of a individually created Modul from an Organizer
   * @return Publickey if key is found in Database
   */
  public PublicKey findPublicKey(String matr, String modulID) throws NoPublicKeyInDatabaseException {
    QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndModulId(matr, modulID);
    if (quittungDao == null) {
      throw new NoPublicKeyInDatabaseException("kein Public Key in Database");
    } else {
      logger.info("Public Key gefunden");
      return loadQuittungDto(quittungDao).getPublicKey();
    }
  }

  /**
   * This method loads an QuittingDto-Object as a data-transfer-object
   *
   * @param quittungDao is used to persist in Database
   * @return quittungDto
   */
  private QuittungDto loadQuittungDto(QuittungDao quittungDao) {
    QuittungDto quittungDto = new QuittungDto();
    quittungDto.setModulId(quittungDao.getModulId());
    quittungDto.setMatrikelnummer(quittungDao.getMatrikelnummer());
    quittungDto.setPublicKey(quittungDao.getPublicKey());
    quittungDto.setQuittung(quittungDao.getQuittung());
    return quittungDto;
  }


  /**
   * This method finds a Receipt which depends on ID-Number of the Student and Modul-ID
   *
   * @param matr is the ID-Number of the Student
   * @param modulID is the Modul-ID of a specific Modul created by an Organizer
   * @return Receipt in form of a String which contains the Token,ID-Number and Modul-ID
   */
  public String findQuittung(String matr, String modulID) throws NoTokenInDatabaseException {

    QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndModulId(matr, modulID);
    if (quittungDao == null) throw new NoTokenInDatabaseException("Token wurde in der Datenbank nicht gefunden!");
    else {
      logger.info("Quittung gefunden");
      return loadQuittungDto(quittungDao).getQuittung();
    }

  }

  /**
   * This method saves a QuittingDto into the Database with the help of a Repository
   *
   * @param quittungDao contains the Information of the Receipt, which will be persisted into the Database
   */
  void save(QuittungDao quittungDao) {
    quittungRepository.save(quittungDao);
  }
}

