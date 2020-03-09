package mops.klausurzulassung.Services.Token.Repositories;

import mops.klausurzulassung.Services.Token.Entities.QuittungDao;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuittungRepository extends CrudRepository<QuittungDao,String> {

}
