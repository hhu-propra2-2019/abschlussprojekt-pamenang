package mops.klausurzulassung.Repositories;

import mops.klausurzulassung.Domain.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
  Iterable<Student> findByModulId(Long id);
}
