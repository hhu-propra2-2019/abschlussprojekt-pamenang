package mops.klausurzulassung.organisatoren.Services;

import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.organisatoren.Repositories.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
  private final StudentRepository studentRepository;

  public StudentService(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  public Iterable<Student> findByModulId(Long id) {
    return studentRepository.findByModulId(id);
  }
}
