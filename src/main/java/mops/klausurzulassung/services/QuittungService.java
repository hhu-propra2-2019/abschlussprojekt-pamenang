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

  public PublicKey findPublicKey(String matr, String modulID) throws NoPublicKeyInDatabaseException {
    QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndModulId(matr, modulID);
    if (quittungDao == null) {
      throw new NoPublicKeyInDatabaseException("kein Public Key in Database");
    } else {
      logger.info("Public Key gefunden");
      return loadQuittungDto(quittungDao).getPublicKey();
    }
  }

  private QuittungDto loadQuittungDto(QuittungDao quittungDao) {
    QuittungDto quittungDto = new QuittungDto();
    quittungDto.setModulId(quittungDao.getModulId());
    quittungDto.setMatrikelnummer(quittungDao.getMatrikelnummer());
    quittungDto.setPublicKey(quittungDao.getPublicKey());
    quittungDto.setQuittung(quittungDao.getQuittung());
    return quittungDto;
  }

  public String findQuittung(String matr, String modulID) throws NoTokenInDatabaseException {

    QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndModulId(matr, modulID);
    if (quittungDao == null) throw new NoTokenInDatabaseException("Token wurde in der Datenbank nicht gefunden!");
    else {
      logger.info("Quittung gefunden");
      return loadQuittungDto(quittungDao).getQuittung();
    }

  }

  void save(QuittungDao quittungDao) {
    quittungRepository.save(quittungDao);
  }
}

