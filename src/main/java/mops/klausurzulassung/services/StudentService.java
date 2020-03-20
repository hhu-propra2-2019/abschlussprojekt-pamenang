package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.repositories.ModulRepository;
import mops.klausurzulassung.repositories.StudentRepository;
import org.springframework.stereotype.Service;


@Service
public class StudentService {
  private final StudentRepository studentRepository;

  public StudentService(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  Iterable<Student> findByModulId(Long id) {
    return studentRepository.findByModulId(id);
  }

  void delete(Student student) {
    studentRepository.delete(student);
  }

  public void save(Student student) {
    studentRepository.save(student);
  }

}
