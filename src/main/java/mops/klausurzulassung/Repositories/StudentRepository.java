package mops.klausurzulassung.Repositories;

import mops.klausurzulassung.Domain.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
  Iterable<Student> findByModulId(Long id);

    Optional<Student> findByToken(String token);
}
