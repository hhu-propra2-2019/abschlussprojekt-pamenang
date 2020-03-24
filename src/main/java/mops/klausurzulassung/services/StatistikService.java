package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.repositories.StatistikRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatistikService {

  final StatistikRepository statistikRepository;
  private Logger logger = LoggerFactory.getLogger(ModulService.class);

  @Autowired
  public StatistikService(StatistikRepository statistikRepository){
    this.statistikRepository = statistikRepository;
  }

  public Iterable<ModulStatistiken> findModulStatistikensByModulId(Long id){
    return statistikRepository.findModulStatistikensByModulId(id);
  }

  public void save(ModulStatistiken modul) {
    statistikRepository.save(modul);
    logger.info("Neue Statistik zum Modul wurde gespeichert!");
  }
}
