package mops.klausurzulassung.Services.Token.Services;

import mops.klausurzulassung.Services.Token.Entities.Quittung;
import mops.klausurzulassung.Services.Token.Repositories.QuittungRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class QuittungService {

    private final QuittungRepository quittungRepository;

    public QuittungService(QuittungRepository quittungRepository) {
        this.quittungRepository = quittungRepository;
    }

    public Optional<Quittung> findQuittung(String matr, String fachID){
        for(Quittung quittung : quittungRepository.findAll()){
            if(quittung.getMatrikelnummer().equals(matr) && quittung.getFachID().equals(fachID)){
                return Optional.of(quittung);
            }
        }
        return Optional.empty();
    }

    public void save(Quittung quittung) {
        quittungRepository.save(quittung);
    }
}

