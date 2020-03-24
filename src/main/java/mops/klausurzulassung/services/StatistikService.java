package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.repositories.StatistikRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StatistikService {

  final StatistikRepository statistikRepository;
  private Logger logger = LoggerFactory.getLogger(ModulService.class);

  @Autowired
  public StatistikService(StatistikRepository statistikRepository) {
    this.statistikRepository = statistikRepository;
  }

  public Iterable<ModulStatistiken> findModulStatistikensByModulId(Long id) {
    return statistikRepository.findModulStatistikensByModulId(id);
  }

  public Long modulInDatabase(String frist, Long modulId) {
    String date = frist.substring(0, frist.length() - 6);
    Optional<ModulStatistiken> modul = statistikRepository.findByFristAndModulId(date, modulId);
    if (modul.isPresent()) {
      return modul.get().getId();
    }
    return null;
  }

  public void save(ModulStatistiken modul) {
    String frist = modul.getFrist();
    String date = frist.substring(0, frist.length() - 6);
    modul.setFrist(date);
    statistikRepository.save(modul);
    logger.info("Neue Statistik zum Modul wurde gespeichert!");
  }

  public Optional<ModulStatistiken> findById(Long id) {
    return statistikRepository.findById(id);
  }
}
