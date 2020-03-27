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

  /**
   * This method finds a ModulStatisticsObject by the Modul-ID
   * @param id is the Modul-ID
   * @return List of ModulStatistics
   */
  public Iterable<ModulStatistiken> findModulStatistikensByModulId(Long id) {
    return statistikRepository.findModulStatistikensByModulId(id);
  }

  /**
   * This method checks if the database contains a ModulStasticsObject
   * @param frist represents the deadline
   * @param modulId represents the Modul-Id
   * @return the Id of the present ModulStastic if it exists, else null
   */
  public Long modulInDatabase(String frist, Long modulId) {
    String date = frist.substring(0, frist.length() - 6);
    Optional<ModulStatistiken> modul = statistikRepository.findByFristAndModulId(date, modulId);
    if (modul.isPresent()) {
      return modul.get().getId();
    }
    return null;
  }

  /**
   * This method saves a ModulstatisticsObject
   * @param modul which contains Information about the Modulstatistic
   */
  public void save(ModulStatistiken modul) {

    statistikRepository.save(modul);
    logger.info("Neue Statistik zum Modul wurde gespeichert!");
  }

  /**
   * This method finds a ModulStatisticObject by Id
   * @param id is the Modulstatistics-ID
   * @return Optional ModulStatisticObject
   */
  public Optional<ModulStatistiken> findById(Long id) {
    return statistikRepository.findById(id);
  }
}
