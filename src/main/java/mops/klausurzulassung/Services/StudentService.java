package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Repositories.ModulRepository;
import mops.klausurzulassung.Repositories.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;


@Service
public class StudentService {
  private final StudentRepository studentRepository;
  private final ModulRepository modulRepository;

  public StudentService(StudentRepository studentRepository,ModulRepository modulRepository) {
    this.studentRepository = studentRepository;
    this.modulRepository = modulRepository;
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

  public boolean isFristAbgelaufen(Long fachId){
    Optional<Modul> modul = modulRepository.findById(fachId);
    LocalDate date = LocalDate.now();
    LocalDate frist = LocalDate.parse(modul.get().getFrist());
    return frist.isBefore(date);
  }
}
