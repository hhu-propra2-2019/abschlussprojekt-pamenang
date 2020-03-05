package mops.klausurzulassung.Repositories;

import mops.klausurzulassung.Entities.Modul;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulRepository extends CrudRepository<Modul, Long> {

}

