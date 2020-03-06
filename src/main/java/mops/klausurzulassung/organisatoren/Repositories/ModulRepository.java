package mops.klausurzulassung.organisatoren.Repositories;

import mops.klausurzulassung.organisatoren.Entities.Modul;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulRepository extends CrudRepository<Modul, Long> {
  Iterable<Modul> findByOwner(String name);
}
