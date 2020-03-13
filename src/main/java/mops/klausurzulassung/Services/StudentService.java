package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Repositories.ModulRepository;
import mops.klausurzulassung.Repositories.StudentRepository;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

  void delete(Student student) {
    studentRepository.delete(student);
  }

  public void save(Student student) {
    studentRepository.save(student);
  }

  public boolean isFristAbgelaufen(Long fachId) throws ParseException {
    Optional<Modul> modul = modulRepository.findById(fachId);

    String frist = modul.get().getFrist();
    Date date = new SimpleDateFormat("dd.mm.yyyy hh:mm").parse(frist);
    LocalDateTime actualDate = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime localFrist = date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    return localFrist.isBefore(actualDate);
  }

  public Optional<Student> findByToken(String token){
    return studentRepository.findByToken(token);
  }
}
