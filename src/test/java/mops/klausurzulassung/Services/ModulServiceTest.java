package mops.klausurzulassung.Services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.AltzulassungStudentDto;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ModulServiceTest {

  private ModulRepository modulRepository;
  private CsvService csvService;
  private StudentService studentService;
  private TokengenerierungService tokengenerierungService;
  private EmailService emailService;
  private QuittungService quittungService;
  private ModulService modulService;
  private HttpServletResponse response;

  @BeforeEach
  public void initilize() {

    modulRepository = mock(ModulRepository.class);
    csvService = mock(CsvService.class);
    studentService = mock(StudentService.class);
    tokengenerierungService = mock(TokengenerierungService.class);
    emailService = mock(EmailService.class);
    quittungService = mock(QuittungService.class);
    response = mock(HttpServletResponse.class);
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
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner", "2000-12-12"));
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
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner", "2000-12-12"));
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

    List<Student> students = new ArrayList<>();
    students.add(new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null));
    students.add(new Student("Rebecca", "Fröhlich", "refro100@hhu.de", 2658447L, 1L, null, null));

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "2000-01-01"));

    when(multipartFile.getInputStream()).thenReturn(input);
    when(csvService.getStudentListFromInputFile(any(), any())).thenReturn(students);
    when(modulRepository.findById(anyLong())).thenReturn(modul);

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
  void altzulassungenVerarbeitenSuccessMessageOhneTokenError() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
            .vorname("Joshua")
            .nachname("Müller")
            .email("joshua@gmail.com")
            .matrikelnummer((long)1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long)1,"name","owner","2000-01-01"));

    when(quittungService.findTokenByQuittung("123","123")).thenReturn("132");
    when(modulRepository.findById((long) 1)).thenReturn(modul);
    modulService.altzulassungVerarbeiten(student, true, (long) 1);


    verify(studentService,times(1)).save(any());
    verify(emailService,times(1)).sendMail(any());
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorMitPapierzulassung() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
            .vorname("Joshua")
            .nachname("Müller")
            .email("joshua@gmail.com")
            .matrikelnummer((long)1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "2000-01-01"));

    when(quittungService.findTokenByQuittung(anyString(), anyString())).thenThrow(new NoTokenInDatabaseException(
            "ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);


    String[] strings = modulService.altzulassungVerarbeiten(student, true, (long) 1);
    String successMessage = "Student " + "1231" + " wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.";
    assertEquals(successMessage, strings[1]);
  }
  
  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorOhnePapierzulassung() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
            .vorname("Joshua")
            .nachname("Müller")
            .email("joshua@gmail.com")
            .matrikelnummer((long)1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "2000-01-01"));

    when(quittungService.findTokenByQuittung(anyString(), anyString())).thenThrow(new NoTokenInDatabaseException(
            "ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);


    String[] strings = modulService.altzulassungVerarbeiten(student, false, (long) 1);
    String errorMessage = "Student " + "1231" + " hat keine Zulassung in diesem Modul!";
    assertEquals(errorMessage, strings[0]);
  }

  @Test
  void erstelleTokenUndSendeMail() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException {

    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    student.setModulId((long) 1);
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "2000-01-01"));


    when(quittungService.findPublicKeyByQuittung(anyString(),anyString())).thenReturn(any());
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1,true);

    verify(emailService,times(1)).sendMail(student);

  }

  @Test
  void erstelleTokenUndSendeMailWithExceptionMitAltzulassung() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException {

    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    student.setModulId((long) 1);

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "2000-01-01"));

    when(quittungService.findPublicKeyByQuittung(anyString(),anyString())).thenThrow(new NoPublicKeyInDatabaseException("ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1,false);

    verify(studentService,times(0)).save(student);
    verify(emailService,times(1)).sendMail(student);

  }

  @Test
  void erstelleTokenUndSendeMailWithExceptionOhneAltzulassung() throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException {

    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    student.setModulId((long) 1);

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "2000-01-01"));

    when(quittungService.findPublicKeyByQuittung(anyString(),anyString())).thenThrow(new NoPublicKeyInDatabaseException("ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1,true);

    verify(studentService,times(1)).save(student);
    verify(emailService,times(1)).sendMail(student);

  }
  
  @Test
  void downloadMitVorhandenerListe() throws IOException {
    ServletOutputStream outputStream = mock(ServletOutputStream.class);

    File outputFile = new File("klausurliste_1.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] cara = {"2659396","Überschär","Cara"};
    String[] rebecca = {"2658447","Fröhlich","Rebecca"};
    writer.writeNext(cara, false);
    writer.writeNext(rebecca, false);
    writer.flush();
    writer.close();

    Modul propra2 = new Modul(1L, "ProPra2","orga","2000-12-12");
    Optional<Modul> modul = Optional.of(propra2);

    when(modulService.findById(1L)).thenReturn(modul);

    when(response.getOutputStream()).thenReturn(outputStream);

    String[] messages = modulService.download(1L, response);

    String[] expectedMessages = {null,null};

    assertEquals(expectedMessages[0],messages[0]);
    assertEquals(expectedMessages[1],messages[1]);
    assertEquals(false,outputFile.exists());
  }

  @Test
  void downloadOhneListe() throws IOException {

    String[] messages = modulService.download(1L, response);

    String[] expectedMessages = {"Bitte erst eine Zulassungsliste hochladen!",null};
    assertEquals(expectedMessages[0],messages[0]);
    assertEquals(expectedMessages[1],messages[1]);
  }
}