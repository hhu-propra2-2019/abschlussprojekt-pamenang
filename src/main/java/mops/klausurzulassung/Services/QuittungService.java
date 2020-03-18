package mops.klausurzulassung.Services;

import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Domain.QuittungDao;
import mops.klausurzulassung.Domain.QuittungDto;
import mops.klausurzulassung.Repositories.QuittungRepository;
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
      QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndModulId(matr,modulID);
      if(quittungDao==null){
        throw new NoPublicKeyInDatabaseException("kein Public Key in Database");
      }
      else{
        logger.debug("Public Key gefunden");
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

      QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndModulId(matr,modulID);
      if(quittungDao==null) throw new NoTokenInDatabaseException("Token wurde in der Datenbank nicht gefunden!");
      else{
        logger.debug("Quittung gefunden");
        return loadQuittungDto(quittungDao).getQuittung();
      }

    }

    void save(QuittungDao quittungDao) {
      quittungRepository.save(quittungDao);
    }
}

