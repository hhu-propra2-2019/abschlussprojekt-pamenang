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

  public Iterable<Student> findByModulId(Long id) {
    return studentRepository.findByModulId(id);
  }

  public void delete(Student student) {
    studentRepository.delete(student);
  }

  public void save(Student student) {
    studentRepository.save(student);
  }

  public boolean isFristAbgelaufen(Long fachId){
    Optional<Modul> modul = modulRepository.findById(fachId);
    LocalDate date = LocalDate.now();
    String dateString = modul.get().getFrist();
    int year = Integer.parseInt(dateString.substring(0,4));
    int month = Integer.parseInt(dateString.substring(5-7));
    int day = Integer.parseInt(dateString.substring(8-10));

    LocalDate frist = LocalDate.parse(modul.get().getFrist());

    LocalDate test = LocalDate.of(year,month,day);
    return test.isBefore(date);
  }

  public Optional<Student> findByToken(String token){
    return studentRepository.findByToken(token);
  }
}
