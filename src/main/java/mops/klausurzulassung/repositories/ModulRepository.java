package mops.klausurzulassung.repositories;

import mops.klausurzulassung.database_entity.Modul;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModulRepository extends CrudRepository<Modul, Long> {
  Iterable<Modul> findByOwnerAndActive(String name, boolean active);

  Optional<Modul> findById(Long fachId);

  Iterable<Modul> findByActive(boolean active);
}
