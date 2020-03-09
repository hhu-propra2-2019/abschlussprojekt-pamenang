package mops.klausurzulassung.Services.Token.Services;

import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Services.Token.Entities.QuittungDao;
import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
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
        for(QuittungDao quittungDao : quittungRepository.findAll()){
            if(quittungDao.getMatrikelnummer().equals(matr) && quittungDao.getFachID().equals(fachID)){
                return quittungDao.getPublicKey();
            }
        }
        throw new NoPublicKeyInDatabaseException("Public Key wurde nicht Datenbank gefunden!");
    }

    public String findTokenByQuittung(String matr, String fachID) throws NoTokenInDatabaseException {
        for(QuittungDao quittungDao : quittungRepository.findAll()){
            if(quittungDao.getMatrikelnummer().equals(matr) && quittungDao.getFachID().equals(fachID)){
                return quittungDao.getToken();
            }
        }
        throw new NoTokenInDatabaseException("Token wurde in der Datenbank nicht gefunden!");
    }

    public void save(QuittungDao quittungDao) {
        quittungRepository.save(quittungDao);
    }
}

