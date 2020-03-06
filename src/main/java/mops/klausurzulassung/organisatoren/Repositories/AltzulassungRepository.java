package mops.klausurzulassung.organisatoren.Repositories;

import mops.klausurzulassung.organisatoren.Entities.Altzulassung;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AltzulassungRepository extends CrudRepository<Altzulassung, Long> {
  Iterable<Altzulassung> findByModulId(Long id);
}
