package mops.klausurzulassung.organisatoren.Services;

import mops.klausurzulassung.organisatoren.Entities.Altzulassung;
import mops.klausurzulassung.organisatoren.Repositories.AltzulassungRepository;
import org.springframework.stereotype.Service;

@Service
public class AltzulassungService {
  private final AltzulassungRepository altzulassungRepository;

  public AltzulassungService(AltzulassungRepository altzulassungRepository) {
    this.altzulassungRepository = altzulassungRepository;
  }

  public Iterable<Altzulassung> findByModulId(Long id) {
    return altzulassungRepository.findByModulId(id);
  }
}
