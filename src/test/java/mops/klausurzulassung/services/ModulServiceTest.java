package mops.klausurzulassung.services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.AltzulassungStudentDto;
import mops.klausurzulassung.domain.FrontendMessage;
import mops.klausurzulassung.exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.repositories.ModulRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModulServiceTest {

  private ModulRepository modulRepository;
  private CsvService csvService;
  private StudentService studentService;
  private TokengenerierungService tokengenerierungService;
  private EmailService emailService;
  private QuittungService quittungService;
  private ModulService modulService;
  private HttpServletResponse response;
  private Principal principal;
  private ServletOutputStream outputStream;
  private StatistikService statistikService;


  @BeforeEach
  void initilize() {

    modulRepository = mock(ModulRepository.class);
    csvService = mock(CsvService.class);
    studentService = mock(StudentService.class);
    tokengenerierungService = mock(TokengenerierungService.class);
    emailService = mock(EmailService.class);
    quittungService = mock(QuittungService.class);
    response = mock(HttpServletResponse.class);
    principal = mock(Principal.class);
    outputStream = mock(ServletOutputStream.class);
    statistikService = mock(StatistikService.class);

    modulService =
        new ModulService(
            modulRepository,
            csvService,
            studentService,
            tokengenerierungService,
            emailService,
            quittungService,
            statistikService
        );
  }

  @Test
  void testFristAbgelaufen() throws ParseException {

    Modul propra1 = new Modul(1L, "ProPra1", "orga", "12/21/2012", true, 0L);

    boolean abgelaufen = modulService.isFristAbgelaufen(propra1);

    assertTrue(abgelaufen);
  }

  @Test
  void fristIsNoDate() {
    String frist = "38/53/2020";

    boolean result = modulService.fristIsDate(frist);
    assertFalse(result);
  }

  @Test
  void fristIsDate() {
    String frist = "03/12/2020";

    boolean result = modulService.fristIsDate(frist);
    assertTrue(result);
  }

  @Test
  void deleteExistingModulWithStudents() {
    long modulID = 1L;
    FrontendMessage message;
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner", "12/12/2000", true, 0L));
    when(modulRepository.findById(modulID)).thenReturn(modul);
    ArrayList<Student> students = new ArrayList<>();
    students.add(new Student());
    when(studentService.findByModulId(modulID)).thenReturn(students);

    message = modulService.deleteStudentsFromModul(modulID);

    assertEquals("Das Modul fname wurde gelöscht!", message.getSuccessMessage());
  }

  @Test
  void deleteExistingModulWithoutStudents() {
    long modulID = 1L;
    FrontendMessage message;
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner", "12/12/2000", true, 0L));
    when(modulRepository.findById(modulID)).thenReturn(modul);

    message = modulService.deleteStudentsFromModul(modulID);

    assertEquals("Das Modul fname wurde gelöscht!", message.getSuccessMessage());
  }

  @Test
  void deleteNonExistingModul() {
    long modulID = 1L;
    FrontendMessage message;
    Optional<Modul> modul = Optional.empty();
    when(modulRepository.findById(modulID)).thenReturn(modul);

    message = modulService.deleteStudentsFromModul(modulID);

    assertEquals("Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.", message.getErrorMessage());
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
  void verarbeiteRichtigeUploadliste() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    MultipartFile multipartFile = mock(MultipartFile.class);
    FrontendMessage message;
    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396\nRebecca,Fröhlich,refro100@hhu.de,2658447".getBytes());

    List<Student> students = new ArrayList<>();
    students.add(new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null));
    students.add(new Student("Rebecca", "Fröhlich", "refro100@hhu.de", 2658447L, 1L, null, null));

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));

    when(multipartFile.getInputStream()).thenReturn(input);
    when(csvService.getStudentListFromInputFile(any(), any())).thenReturn(students);
    when(modulRepository.findById(anyLong())).thenReturn(modul);

    String successMessage = "Zulassungsliste wurde erfolgreich verarbeitet.";
    message = modulService.verarbeiteUploadliste(69L, multipartFile);
    assertEquals(message.getSuccessMessage(), successMessage);
  }

  @Test
  void verarbeiteZuLangeUploadliste() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    MultipartFile multipartFile = mock(MultipartFile.class);
    FrontendMessage message;

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396,zu viel".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);

    String errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    message = modulService.verarbeiteUploadliste(69L, multipartFile);
    assertEquals(message.getErrorMessage(), errorMessage);
  }

  @Test
  void verarbeiteZuKurzeUploadliste() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    MultipartFile multipartFile = mock(MultipartFile.class);
    FrontendMessage message;
    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);

    String errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    message = modulService.verarbeiteUploadliste(69L, multipartFile);
    assertEquals(message.getErrorMessage(), errorMessage);
  }

  @Test
  void verarbeiteFalscheUploadliste() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    MultipartFile multipartFile = mock(MultipartFile.class);
    FrontendMessage message;
    InputStream input = new ByteArrayInputStream("".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);
    when(multipartFile.isEmpty()).thenReturn(true);
    String errorMessage = "Datei ist leer oder es wurde keine Datei ausgewählt!";
    message = modulService.verarbeiteUploadliste(69L, multipartFile);
    assertEquals(message.getErrorMessage(), errorMessage);
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageOhneTokenError() throws NoTokenInDatabaseException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
        .vorname("Joshua")
        .nachname("Müller")
        .email("joshua@gmail.com")
        .matrikelnummer((long) 1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));
    Optional<ModulStatistiken> modulstat = Optional.of(new ModulStatistiken(1L, 1L, "03/28/2020", 120L, 120L));
    when(statistikService.findById(any())).thenReturn(modulstat);
    when(quittungService.findQuittung("123", "123")).thenReturn("132");
    when(modulRepository.findById((long) 1)).thenReturn(modul);
    modulService.altzulassungVerarbeiten(student, true, (long) 1);


    verify(studentService, times(1)).save(any());
    verify(emailService, times(1)).sendMail(any());
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorMitPapierzulassung() throws NoTokenInDatabaseException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    FrontendMessage message;
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
        .vorname("Joshua")
        .nachname("Müller")
        .email("joshua@gmail.com")
        .matrikelnummer((long) 1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));

    when(quittungService.findQuittung(anyString(), anyString())).thenThrow(new NoTokenInDatabaseException(
        "ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);


    message = modulService.altzulassungVerarbeiten(student, true, (long) 1);
    String successMessage = "Student " + "1231" + " wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.";
    assertEquals(successMessage, message.getSuccessMessage());
  }


  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorOhnePapierzulassung() throws NoTokenInDatabaseException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    FrontendMessage message;
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
        .vorname("Joshua")
        .nachname("Müller")
        .email("joshua@gmail.com")
        .matrikelnummer((long) 1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));

    when(quittungService.findQuittung(anyString(), anyString())).thenThrow(new NoTokenInDatabaseException(
        "ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);


    message = modulService.altzulassungVerarbeiten(student, false, (long) 1);
    String errorMessage = "Student " + "1231" + " hat keine Zulassung in diesem Modul!";
    assertEquals(errorMessage, message.getErrorMessage());
  }

  @Test
  void erstelleTokenUndSendeMail() throws NoPublicKeyInDatabaseException {

    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    student.setModulId((long) 1);
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));


    when(quittungService.findPublicKey(anyString(), anyString())).thenReturn(any());
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1, true);

    verify(emailService, times(1)).sendMail(student);

  }

  @Test
  void erstelleTokenUndSendeMailWithExceptionMitAltzulassung() throws NoPublicKeyInDatabaseException {

    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    student.setModulId((long) 1);

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));

    when(quittungService.findPublicKey(anyString(), anyString())).thenThrow(new NoPublicKeyInDatabaseException("ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1, false);

    verify(studentService, times(0)).save(student);
    verify(emailService, times(1)).sendMail(student);

  }

  @Test
  void erstelleTokenUndSendeMailWithExceptionOhneAltzulassung() throws NoPublicKeyInDatabaseException {

    Student student = new Student();
    student.setVorname("Joshua");
    student.setNachname("Müller");
    student.setEmail("joshua@gmail.com");
    student.setMatrikelnummer((long) 1231);
    student.setModulId((long) 1);

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true, 0L));

    when(quittungService.findPublicKey(anyString(), anyString())).thenThrow(new NoPublicKeyInDatabaseException("ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1, true);

    verify(studentService, times(1)).save(student);
    verify(emailService, times(1)).sendMail(student);

  }

  @Test
  void downloadMitVorhandenerListe() throws IOException {
    File outputFile = new File("klausurliste_1.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] cara = {"2659396", "Überschär", "Cara"};
    String[] rebecca = {"2658447", "Fröhlich", "Rebecca"};
    writer.writeNext(cara, false);
    writer.writeNext(rebecca, false);
    writer.flush();
    writer.close();

    Modul propra2 = new Modul(1L, "ProPra2", "orga", "12/12/2000", true, 0L);
    Optional<Modul> modul = Optional.of(propra2);

    when(modulService.findById(1L)).thenReturn(modul);

    when(response.getOutputStream()).thenReturn(outputStream);
    Optional<ModulStatistiken> modulstat = Optional.of(new ModulStatistiken(1L, 1L, "03/28/2020", 120L, 120L));
    when(statistikService.findById(any())).thenReturn(modulstat);
    modulService.download(1L, response);

    assertFalse(outputFile.exists());
  }

  @Test
  void downloadOhneVorhandeneListe() throws IOException {
    csvService = new CsvService(studentService);
    modulService = new ModulService(modulRepository, csvService, studentService, tokengenerierungService, emailService, quittungService, statistikService);

    Modul propra2 = new Modul(2L, "ProPra2", "orga", "12/12/2000", true, 0L);
    Optional<Modul> modul = Optional.of(propra2);

    when(modulService.findById(2L)).thenReturn(modul);
    Optional<ModulStatistiken> modulstat = Optional.of(new ModulStatistiken(1L, 1L, "03/28/2020", 120L, 120L));
    when(statistikService.findById(any())).thenReturn(modulstat);
    when(response.getOutputStream()).thenReturn(outputStream);
    modulService.download(2L, response);

    File outputFile = new File("klausurliste_2.csv");
    assertFalse(outputFile.exists());
  }

  @Test
  void countlines() throws IOException {
    File outputFile = new File("klausurliste_1.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] cara = {"2659396", "Überschär", "Cara"};
    String[] rebecca = {"2658447", "Fröhlich", "Rebecca"};
    writer.writeNext(cara, false);
    writer.writeNext(rebecca, false);
    writer.flush();
    writer.close();

    Modul propra2 = new Modul(1L, "ProPra2", "orga", "03/28/2020", true, 0L);
    Optional<Modul> modul = Optional.of(propra2);
    Optional<ModulStatistiken> modulstat = Optional.of(new ModulStatistiken(1L, 1L, "03/28/2020", 120L, 120L));
    when(statistikService.findById(any())).thenReturn(modulstat);
    when(statistikService.modulInDatabase(any(), any())).thenReturn(1L);
    when(modulService.findById(any())).thenReturn(modul);
    modulService.countLines(3L, outputFile);
    verify(statistikService, times(1)).save(any());
    assertEquals(2, modulstat.get().getZulassungsZahl());


  }

  private String fristInZukunft() {
    LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime future = now.plusYears(1);
    int year = future.getYear();
    int month = future.getMonthValue();
    int day = future.getDayOfMonth();
    return month + "/" + day + "/" + year;
  }
}