package mops.klausurzulassung.repositories;

import mops.klausurzulassung.database_entity.ModulStatistiken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatistikRepository extends CrudRepository<ModulStatistiken, Long> {

  Iterable<ModulStatistiken> findModulStatistikensByModulId(Long id);
}
