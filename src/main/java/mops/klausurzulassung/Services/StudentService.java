package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Repositories.ModulRepository;
import mops.klausurzulassung.Repositories.StudentRepository;
import org.springframework.stereotype.Service;


@Service
public class StudentService {
  private final StudentRepository studentRepository;

  public StudentService(StudentRepository studentRepository,ModulRepository modulRepository) {
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
