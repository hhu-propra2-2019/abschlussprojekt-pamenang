package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Repositories.ModulRepository;
import mops.klausurzulassung.Repositories.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class StudentenServiceTests {


  private ModulRepository modulRepository;
  private StudentService studentService;
  private StudentRepository studentRepository;

  @BeforeEach
  void setUp(){
    this.modulRepository = mock(ModulRepository.class);
    this.studentRepository = mock(StudentRepository.class);
    this.studentService = new StudentService(studentRepository, modulRepository);
  }

  @Test
  void testFristAbgelaufen() throws ParseException {

    Modul propra1 = new Modul(1L, "ProPra1","orga","21.12.2012 20:00");
    Optional<Modul> modul = Optional.of(propra1);
    when(modulRepository.findById(1L)).thenReturn(modul);

    boolean abgelaufen = studentService.isFristAbgelaufen(1L);

    assertTrue(abgelaufen);
  }
}
