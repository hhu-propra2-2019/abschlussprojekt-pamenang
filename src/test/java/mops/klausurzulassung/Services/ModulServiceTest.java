package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.bouncycastle.math.raw.Mod;
import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ModulServiceTest {

  private ModulRepository modulRepository;
  private CsvService csvService;
  private StudentService studentService;
  private TokengenerierungService tokengenerierungService;
  private EmailService emailService;
  private QuittungService quittungService;
  private ModulService modulService;

  @BeforeEach
  public void initilize() {

    modulRepository = mock(ModulRepository.class);
    csvService = mock(CsvService.class);
    studentService = mock(StudentService.class);
    tokengenerierungService = mock(TokengenerierungService.class);
    emailService = mock(EmailService.class);
    quittungService = mock(QuittungService.class);
    modulService =
        new ModulService(
            modulRepository,
            csvService,
            studentService,
            tokengenerierungService,
            emailService,
            quittungService
        );
  }

  @Test
  void deleteExistingModulWithStudents() {
    long modulID = 1L;
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner"));
    when(modulRepository.findById(modulID)).thenReturn(modul);
    ArrayList<Student> students = new ArrayList<>();
    students.add(new Student());
    when(studentService.findByModulId(modulID)).thenReturn(students);

    String[] result = modulService.deleteStudentsFromModul(modulID);

    assertNull(result[0]);
    assertEquals("Das Modul fname wurde gelöscht!", result[1]);
  }

  @Test
  void deleteExistingModulWithoutStudents() {
    long modulID = 1L;
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner"));
    when(modulRepository.findById(modulID)).thenReturn(modul);

    String[] result = modulService.deleteStudentsFromModul(modulID);

    assertNull(result[0]);
    assertEquals("Das Modul fname wurde gelöscht!", result[1]);
  }

  @Test
  void deleteNonExistingModul() {
    long modulID = 1L;
    Optional<Modul> modul = Optional.empty();
    when(modulRepository.findById(modulID)).thenReturn(modul);

    String[] result = modulService.deleteStudentsFromModul(modulID);

    assertEquals("Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.", result[0]);
    assertNull(result[1]);
  }

  @Test
  void testStudentIsEmptyIsEmpty() {
    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("");
    student.setMatrikelnummer((long) 1231);
    boolean b = modulService.studentIsEmpty(student);
    assertTrue(b);
  }

  @Test
  void testStudentIsEmptyIsNotEmpty() {
    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua.müller@web.de");
    student.setMatrikelnummer((long) 1231);

    boolean b = modulService.studentIsEmpty(student);

    assertFalse(b);
  }

  @Test
  void verarbeiteRichtigeUploadliste() throws SignatureException, NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396\nRebecca,Fröhlich,refro100@hhu.de,2658447".getBytes());

    when(multipartFile.getInputStream()).thenReturn(input);

    String successMessage = "Zulassungsliste wurde erfolgreich verarbeitet.";
    String success = modulService.verarbeiteUploadliste(69L, multipartFile)[1];
    assertEquals(success, successMessage);
  }

  @Test
  void verarbeiteZuLangeUploadliste() throws SignatureException, NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396,zu viel".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);

    String errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    String message = modulService.verarbeiteUploadliste(69L, multipartFile)[0];
    assertEquals(message, errorMessage);
  }

  @Test
  void verarbeiteZuKurzeUploadliste() throws SignatureException, NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);

    String errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    String message = modulService.verarbeiteUploadliste(69L, multipartFile)[0];
    assertEquals(message, errorMessage);
  }

  @Test
  void verarbeiteFalscheUploadliste() throws SignatureException, NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);
    when(multipartFile.isEmpty()).thenReturn(true);
    String errorMessage = "Datei ist leer oder es wurde keine Datei ausgewählt!";
    String message = modulService.verarbeiteUploadliste(69L, multipartFile)[0];
    assertEquals(message, errorMessage);
  }

  @Test
  void altzulassungenVerarbeitenErrorMessage() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException {
    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("");
    student.setMatrikelnummer((long) 1231);

    String[] strings = modulService.altzulassungVerarbeiten(student, anyBoolean(), anyLong());
    String errorMessage = "Bitte Daten eingeben!";
    assertEquals(strings[0],errorMessage);
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageOhneTokenError() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    Optional<Modul> modul = Optional.of(new Modul((long)1,"name","owner"));

    when(quittungService.findTokenByQuittung("123","123")).thenReturn("132");
    when(modulRepository.findById((long) 1)).thenReturn(modul);
    modulService.altzulassungVerarbeiten(student, true, (long) 1);


    verify(studentService,times(1)).save(any());
    verify(emailService,times(1)).sendMail(any());
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorMitPapierzulassung() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    Optional<Modul> modul = Optional.of(new Modul((long)1,"name","owner"));

    when(quittungService.findTokenByQuittung("123","123")).thenThrow(new NoTokenInDatabaseException("ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);


    String[] strings = modulService.altzulassungVerarbeiten(student, true, (long) 1);
    String successMessage = "Student "+ "1231"+" wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.";
    assertEquals(successMessage,strings[1]);
  }
}