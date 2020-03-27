package mops.klausurzulassung.repositories;

import mops.klausurzulassung.database_entity.QuittungDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuittungRepository extends JpaRepository<QuittungDao, Long> {

  QuittungDao findByMatrikelnummerAndModulId(String matrikelnummer, String fachId);

}
