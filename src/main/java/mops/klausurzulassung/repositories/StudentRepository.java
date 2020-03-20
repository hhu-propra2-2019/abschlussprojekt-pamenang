package mops.klausurzulassung.repositories;

import mops.klausurzulassung.database_entity.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
  Iterable<Student> findByModulId(Long id);

}
