package mops.klausurzulassung.Services.Token.Services;

import mops.klausurzulassung.Services.Token.Entities.Quittung;
import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Optional;

@Component
public class QuittungService {

    private final QuittungRepository quittungRepository;

    @Autowired
    public QuittungService(QuittungRepository quittungRepository) {
        this.quittungRepository = quittungRepository;
    }

    public PublicKey findPublicKeyByQuittung(String matr, String fachID){
        for(Quittung quittung : quittungRepository.findAll()){
            if(quittung.getMatrikelnummer().equals(matr) && quittung.getFachID().equals(fachID)){
                return quittung.getPublicKey();
            }
        }
        return null;
    }

    public void save(Quittung quittung) {
        quittungRepository.save(quittung);
    }
}

