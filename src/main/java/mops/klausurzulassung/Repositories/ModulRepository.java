package mops.klausurzulassung.Repositories;

import mops.klausurzulassung.Domain.Modul;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulRepository extends CrudRepository<Modul, Long> {
  Iterable<Modul> findByOwner(String name);
}
