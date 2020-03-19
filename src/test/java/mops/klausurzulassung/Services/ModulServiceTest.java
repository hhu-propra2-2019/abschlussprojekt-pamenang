package mops.klausurzulassung.Services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.AltzulassungStudentDto;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
  void saveNewModulwithoutMissingAttribute() throws ParseException {
    String frist = fristInZukunft();
    Modul propra = new Modul(null, "ProPra1", null, frist, null);
    String owner = "orga";

    String[] messages = modulService.saveNewModul(propra, owner);

    assertNull(messages[0]);
    assertNull(messages[1]);
    assertEquals("orga", propra.getOwner());
    assertEquals(true, propra.getActive());
    assertEquals(frist, propra.getFrist());
    assertEquals("ProPra1", propra.getName());
  }

  @Test
  void saveNewModulwithMissingAttribute() throws ParseException {
    String frist = fristInZukunft();
    Modul propra = new Modul(null, "", null, frist, null);
    String owner = "orga";

    String[] messages = modulService.saveNewModul(propra, owner);

    assertEquals("Bitte beide Felder ausfüllen!", messages[0]);
    assertNull(messages[1]);
    assertNull(propra.getOwner());
    assertNull(propra.getActive());
    assertEquals(frist, propra.getFrist());
    assertEquals("", propra.getName());
  }

  @Test
  void testFristAbgelaufen() throws ParseException {

    Modul propra1 = new Modul(1L, "ProPra1", "orga", "12/21/2012", true);
    Optional<Modul> modul = Optional.of(propra1);

    boolean abgelaufen = modulService.isFristAbgelaufen(propra1);

    assertTrue(abgelaufen);
  }

  @Test
  void saveNewModulwithFristIsFalse() throws ParseException {
    String frist = "12/20/2000";
    Modul propra = new Modul(null, "ProPra1", null, frist, null);
    String owner = "orga";

    String[] messages = modulService.saveNewModul(propra, owner);

    assertNull(propra.getActive());
    assertEquals("Die Frist muss in der Zukunft liegen!", messages[0]);
    assertNull(messages[1]);
    assertNull(propra.getOwner());
    assertEquals(frist, propra.getFrist());
    assertEquals("ProPra1", propra.getName());
  }

  @Test
  void modulBearbeitenWithoutMissingAttribute() throws ParseException {
    String frist = fristInZukunft();
    Modul propra = new Modul(null, "ProPra1", null, frist, null);
    Modul vorhandenesModul = new Modul(1L, "ProPra", null, "", false);
    Optional modul = Optional.of(vorhandenesModul);
    when(principal.getName()).thenReturn("orga");
    when(modulService.findById(1L)).thenReturn(modul);

    String[] messages = modulService.modulBearbeiten(propra, 1L, principal);
    assertNull(messages[0]);
    assertNull(messages[1]);
    assertEquals("ProPra1", vorhandenesModul.getName());
    assertEquals("orga", vorhandenesModul.getOwner());
    assertEquals(frist, vorhandenesModul.getFrist());
    assertEquals(1L, vorhandenesModul.getId());
    assertTrue(vorhandenesModul.getActive());
  }

  @Test
  void modulBearbeitenMitAbgelaufenerFrist() throws ParseException {
    String frist = "12/20/2000";
    Modul propra = new Modul(null, "ProPra1", null, frist, null);

    String[] messages = modulService.modulBearbeiten(propra, 23L, principal);

    assertNull(propra.getActive());
    assertEquals("Die Frist muss in der Zukunft liegen!", messages[0]);
    assertNull(messages[1]);
    assertNull(propra.getOwner());
    assertEquals(frist, propra.getFrist());
    assertEquals("ProPra1", propra.getName());
  }

  @Test
  void modulBearbeitenWithMissingAttribute() throws ParseException {
    Modul propra = new Modul(null, "ProPra2", null, "", null);

    String[] messages = modulService.modulBearbeiten(propra, 7L, principal);

    assertNull(propra.getActive());
    assertEquals("Beide Felder müssen ausgefüllt sein!", messages[0]);
    assertNull(messages[1]);
    assertNull(propra.getOwner());
    assertEquals("", propra.getFrist());
    assertEquals("ProPra2", propra.getName());
  }

  @Test
  void deleteExistingModulWithStudents() {
    long modulID = 1L;
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner", "12/12/2000", true));
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
    Optional<Modul> modul = Optional.of(new Modul(modulID, "fname", "owner", "12/12/2000", true));
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
  void verarbeiteRichtigeUploadliste() throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396\nRebecca,Fröhlich,refro100@hhu.de,2658447".getBytes());

    List<Student> students = new ArrayList<>();
    students.add(new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null));
    students.add(new Student("Rebecca", "Fröhlich", "refro100@hhu.de", 2658447L, 1L, null, null));

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));

    when(multipartFile.getInputStream()).thenReturn(input);
    when(csvService.getStudentListFromInputFile(any(), any())).thenReturn(students);
    when(modulRepository.findById(anyLong())).thenReturn(modul);

    String successMessage = "Zulassungsliste wurde erfolgreich verarbeitet.";
    String success = modulService.verarbeiteUploadliste(69L, multipartFile)[1];
    assertEquals(success, successMessage);
  }

  @Test
  void verarbeiteZuLangeUploadliste() throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396,zu viel".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);

    String errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    String message = modulService.verarbeiteUploadliste(69L, multipartFile)[0];
    assertEquals(message, errorMessage);
  }

  @Test
  void verarbeiteZuKurzeUploadliste() throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);

    String errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    String message = modulService.verarbeiteUploadliste(69L, multipartFile)[0];
    assertEquals(message, errorMessage);
  }

  @Test
  void verarbeiteFalscheUploadliste() throws SignatureException, NoSuchAlgorithmException, IOException, InvalidKeyException {
    MultipartFile multipartFile = mock(MultipartFile.class);

    InputStream input = new ByteArrayInputStream("".getBytes());
    when(multipartFile.getInputStream()).thenReturn(input);
    when(multipartFile.isEmpty()).thenReturn(true);
    String errorMessage = "Datei ist leer oder es wurde keine Datei ausgewählt!";
    String message = modulService.verarbeiteUploadliste(69L, multipartFile)[0];
    assertEquals(message, errorMessage);
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageOhneTokenError() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
            .vorname("Joshua")
            .nachname("Müller")
            .email("joshua@gmail.com")
            .matrikelnummer((long)1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));

    when(quittungService.findQuittung("123","123")).thenReturn("132");
    when(modulRepository.findById((long) 1)).thenReturn(modul);
    modulService.altzulassungVerarbeiten(student, true, (long) 1);


    verify(studentService,times(1)).save(any());
    verify(emailService,times(1)).sendMail(any());
  }

  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorMitPapierzulassung() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
            .vorname("Joshua")
            .nachname("Müller")
            .email("joshua@gmail.com")
            .matrikelnummer((long)1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));

    when(quittungService.findQuittung(anyString(), anyString())).thenThrow(new NoTokenInDatabaseException(
            "ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);


    String[] strings = modulService.altzulassungVerarbeiten(student, true, (long) 1);
    String successMessage = "Student " + "1231" + " wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.";
    assertEquals(successMessage, strings[1]);
  }

  
  @Test
  void altzulassungenVerarbeitenSuccessMessageMitTokenErrorOhnePapierzulassung() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoTokenInDatabaseException {
    AltzulassungStudentDto student = AltzulassungStudentDto.builder()
            .vorname("Joshua")
            .nachname("Müller")
            .email("joshua@gmail.com")
            .matrikelnummer((long)1231).build();
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));

    when(quittungService.findQuittung(anyString(), anyString())).thenThrow(new NoTokenInDatabaseException(
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
    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));


    when(quittungService.findPublicKey(anyString(),anyString())).thenReturn(any());
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

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));

    when(quittungService.findPublicKey(anyString(),anyString())).thenThrow(new NoPublicKeyInDatabaseException("ERROR"));
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

    Optional<Modul> modul = Optional.of(new Modul((long) 1, "name", "owner", "01/01/2000", true));

    when(quittungService.findPublicKey(anyString(),anyString())).thenThrow(new NoPublicKeyInDatabaseException("ERROR"));
    when(modulRepository.findById((long) 1)).thenReturn(modul);

    modulService.erstelleTokenUndSendeEmail(student, (long) 1,true);

    verify(studentService,times(1)).save(student);
    verify(emailService,times(1)).sendMail(student);

  }
  
  @Test
  void downloadMitVorhandenerListe() throws IOException {
    File outputFile = new File("klausurliste_1.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] cara = {"2659396","Überschär","Cara"};
    String[] rebecca = {"2658447","Fröhlich","Rebecca"};
    writer.writeNext(cara, false);
    writer.writeNext(rebecca, false);
    writer.flush();
    writer.close();

    Modul propra2 = new Modul(1L, "ProPra2", "orga", "12/12/2000", true);
    Optional<Modul> modul = Optional.of(propra2);

    when(modulService.findById(1L)).thenReturn(modul);

    when(response.getOutputStream()).thenReturn(outputStream);

    modulService.download(1L, response);

    assertFalse(outputFile.exists());
  }

  @Test
  void downloadOhneVorhandeneListe() throws IOException {
    csvService = new CsvService(studentService);
    modulService = new ModulService(modulRepository, csvService, studentService, tokengenerierungService, emailService, quittungService);

    Modul propra2 = new Modul(2L, "ProPra2", "orga", "12/12/2000", true);
    Optional<Modul> modul = Optional.of(propra2);

    when(modulService.findById(2L)).thenReturn(modul);

    when(response.getOutputStream()).thenReturn(outputStream);
    modulService.download(2L, response);

    File outputFile = new File("klausurliste_2.csv");
    assertFalse(outputFile.exists());
  }

  @Test
  void neuesModulErstellenFristInDerZukunftIdIsPresent() throws ParseException {
    String frist = fristInZukunft();

    Modul propra = new Modul(1L, "ProPra2", "orga", frist, true);
    Optional<Modul> modul = Optional.of(propra);

    when(principal.getName()).thenReturn("orga");
    when(modulService.findById(1L)).thenReturn(modul);

    Object[] returnValues = modulService.neuesModul(propra, principal);

    assertEquals(propra, returnValues[0]);
    assertNull(returnValues[2]);
    assertEquals("Diese Modul-ID existiert schon, bitte eine andere ID eingeben!", returnValues[1]);

  }

  private String fristInZukunft() {
    LocalDateTime now = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime future = now.plusYears(1);
    int year = future.getYear();
    int month = future.getMonthValue();
    int day = future.getDayOfMonth();
    return month + "/" + day + "/" + year;
  }

  @Test
  void neuesModulErstellenFristInDerZukunftIdIsNotPresent() {
    String frist = fristInZukunft();

    Modul propra = new Modul(1L, "ProPra2", "orga", frist, true);
    Optional<Modul> modul = Optional.empty();

    when(principal.getName()).thenReturn("orga");
    when(modulService.findById(1L)).thenReturn(modul);

    Object[] returnValues = modulService.neuesModul(propra, principal);

    assertNotEquals(propra, returnValues[0]);
    assertNull(returnValues[1]);
    assertEquals("Neues Modul wurde erfolgreich hinzugefügt!", returnValues[2]);

  }

  @Test
  void neuesModulErstellenFristAbgelaufen() {
    Modul propra = new Modul(1L, "ProPra2", "orga", "12/12/2000", true);
    Optional<Modul> modul = Optional.of(propra);

    when(principal.getName()).thenReturn("orga");
    when(modulService.findById(1L)).thenReturn(modul);

    Object[] returnValues = modulService.neuesModul(propra, principal);

    assertEquals(propra, returnValues[0]);
    assertEquals("Frist liegt in der Vergangenheit, bitte eine andere Frist eingeben!", returnValues[1]);
    assertNull(returnValues[2]);

  }

  @Test
  void neuesModulFristLeer() {
    Modul propra = new Modul(1L, "ProPra1", "orga", "", true);
    Optional<Modul> modul = Optional.of(propra);

    Object[] returnValues = modulService.neuesModul(propra, principal);

    assertEquals(propra, returnValues[0]);
    assertEquals("Alle Felder im Formular müssen ausgefüllt sein!", returnValues[1]);
    assertNull(returnValues[2]);
  }
}