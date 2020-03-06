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

    public Optional<Quittung> findByID(String token){return quittungRepository.findById(token);}

    public void save(Quittung quittung) {
        quittungRepository.save(quittung);
    }
}

