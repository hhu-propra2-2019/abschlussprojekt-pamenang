package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}