package mops.klausurzulassung.Services;

import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Domain.QuittungDao;
import mops.klausurzulassung.Domain.QuittungDto;
import mops.klausurzulassung.Repositories.QuittungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
public class QuittungService {

    private final QuittungRepository quittungRepository;

    @Autowired
    public QuittungService(QuittungRepository quittungRepository) {
        this.quittungRepository = quittungRepository;
    }

    public PublicKey findPublicKeyByQuittung(String matr, String fachID) throws NoPublicKeyInDatabaseException {
        QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndFachID(matr,fachID);
        if(quittungDao==null) throw new NoPublicKeyInDatabaseException("kein Public Key in Database");
        else return loadQuittungDto(quittungDao).getPublicKey();

    }

    private QuittungDto loadQuittungDto(QuittungDao quittungDao) {
        QuittungDto quittungDto = new QuittungDto();
        quittungDto.setFachID(quittungDao.getFachID());
        quittungDto.setMatrikelnummer(quittungDao.getMatrikelnummer());
        quittungDto.setPublicKey(quittungDao.getPublicKey());
        quittungDto.setToken(quittungDao.getToken());
        return quittungDto;
    }

    public String findTokenByQuittung(String matr, String fachID) throws NoTokenInDatabaseException {

        QuittungDao quittungDao = quittungRepository.findByMatrikelnummerAndFachID(matr,fachID);
        if(quittungDao==null) throw new NoTokenInDatabaseException("Token wurde in der Datenbank nicht gefunden!");

        else return loadQuittungDto(quittungDao).getToken();

    }

    public void save(QuittungDao quittungDao) {
        quittungRepository.save(quittungDao);
    }
}

