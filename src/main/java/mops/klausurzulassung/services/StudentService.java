package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.repositories.StudentRepository;
import org.springframework.stereotype.Service;


@Service
public class StudentService {
  private final StudentRepository studentRepository;


  public StudentService(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  /**
   * This method finds a Modul By Id
   * @param id is the Modul-ID
   * @return List of Students
   */
  Iterable<Student> findByModulId(Long id) {
    return studentRepository.findByModulId(id);
  }


  /**
   * This method deletes a Student in the database
   * @param student contains the Student-Information
   *
   */
  void delete(Student student) {
    studentRepository.delete(student);
  }


  /**
   * This method saves a Student into the database
   * @param student contains the Student-Information
   */
  public void save(Student student) {
    studentRepository.save(student);
  }

}
