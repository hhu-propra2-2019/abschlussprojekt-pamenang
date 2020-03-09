package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ModulService {
  private final ModulRepository modulRepository;

  public ModulService(ModulRepository modulRepository) {
    this.modulRepository = modulRepository;
  }

  public Iterable<Modul> allModuls() {
    return modulRepository.findAll();
  }

  public Iterable<Modul> findByOwner(String name) {
    return modulRepository.findByOwner(name);
  }

  public Optional<Modul> findById(Long id) {
    return modulRepository.findById(id);
  }

  public void delete(Modul modul) {
    modulRepository.delete(modul);
  }

  public void save(Modul modul) {
    modulRepository.save(modul);
  }
}
