package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.repositories.StatistikRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatistikService {

  final StatistikRepository statistikRepository;

  @Autowired
  public StatistikService(StatistikRepository statistikRepository){
    this.statistikRepository = statistikRepository;
  }

  public Iterable<ModulStatistiken> findModulStatistikensByModulId(Long id){
    return statistikRepository.findModulStatistikensByModulId(id);
  }
}
