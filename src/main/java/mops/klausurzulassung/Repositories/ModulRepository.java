package mops.klausurzulassung.Repositories;

import mops.klausurzulassung.Domain.Modul;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModulRepository extends CrudRepository<Modul, Long> {
  Iterable<Modul> findByOwnerAndActive(String name, boolean active);
  Optional<Modul> findById(Long fachId);

  Iterable<Modul> findByActive(boolean active);
}
