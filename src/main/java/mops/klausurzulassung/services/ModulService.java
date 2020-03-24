package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Modul;
import mops.klausurzulassung.database_entity.ModulStatistiken;
import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.AltzulassungStudentDto;
import mops.klausurzulassung.domain.FrontendMessage;
import mops.klausurzulassung.exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.repositories.ModulRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class ModulService {
  private final ModulRepository modulRepository;
  private final CsvService csvService;
  private final StudentService studentService;
  private final QuittungService quittungService;
  private final EmailService emailService;
  private final TokengenerierungService tokengenerierungService;
  private final StatistikService statistikService;

  private FrontendMessage message = new FrontendMessage();
  private Logger logger = LoggerFactory.getLogger(ModulService.class);


  public ModulService(ModulRepository modulRepository, CsvService csvService, StudentService studentService, TokengenerierungService tokengenerierungService, EmailService emailService, QuittungService quittungService, StatistikService statistikService) {
    this.csvService = csvService;
    this.studentService = studentService;
    this.tokengenerierungService = tokengenerierungService;
    this.emailService = emailService;
    this.quittungService = quittungService;
    this.modulRepository = modulRepository;
    this.statistikService = statistikService;
  }

  public Iterable<Modul> findByOwnerAndActive(String name, boolean active) {
    return modulRepository.findByOwnerAndActive(name, active);
  }

  public Optional<Modul> findById(Long id) {
    return modulRepository.findById(id);
  }

  public Iterable<Modul> findByActive(boolean active) {
    return modulRepository.findByActive(active);
  }

  public void save(Modul modul) {
    modulRepository.save(modul);
    logger.info("Das Modul " + modul + " wurde gespeichert.");
  }

  public FrontendMessage verarbeiteUploadliste(Long id, MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    message.resetMessage();
    Iterable<CSVRecord> records = null;
    try {
      records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").parse(new InputStreamReader(file.getInputStream()));
    } catch (IOException e) {
      logger.error("Fehler beim Einlesen der Datei!", e);
    }

    boolean countColumns = true;

    for (CSVRecord record : records) {
      if (record.size() != 4) {
        countColumns = false;
        break;
      }
    }

    logger.info("Die Liste wurde erfolgreich eingelesen.");

    try {
      records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").withSkipHeaderRecord().parse(new InputStreamReader(file.getInputStream()));
    } catch (IOException e) {
      logger.error("Fehler beim Einlesen der Datei!", e);
    }

    if (!countColumns) {
      message.setErrorMessage("Datei hat eine falsche Anzahl von Einträgen pro Zeile!");
    } else if (file.isEmpty()) {
      message.setErrorMessage("Datei ist leer oder es wurde keine Datei ausgewählt!");
    } else {
      try {
        List<Student> students = csvService.getStudentListFromInputFile(records, id);

        String modulname = findById(id).get().getName();

        for (Student student : students) {
          student.setFachname(modulname);
          erstelleTokenUndSendeEmail(student, id, false);
        }
        logger.info("Token wurden generiert und Emails versendet!");

        csvService.writeCsvFile(id, students);
        message.setSuccessMessage("Zulassungsliste wurde erfolgreich verarbeitet.");

      } catch (NumberFormatException e) {
        logger.error("Eine Matrikelnummer der hochgeladenen Liste enthält nicht nur Zahlen!");
        message.setErrorMessage("Eine Matrikelnummer der hochgeladenen Liste enthält nicht nur Zahlen!");
      }
    }

    return message;
  }

  public FrontendMessage deleteStudentsFromModul(Long id) {

    logger.info("ID: " + id);
    Optional<Modul> modul = findById(id);
    if (modul.isPresent()) {
      String modulName = modul.get().getName();
      modul.get().setOwner(null);
      modul.get().setFrist(null);
      modul.get().setActive(false);
      modul.get().setTeilnehmer(0L);
      save(modul.get());

      Iterable<Student> students = studentService.findByModulId(id);
      for (Student student : students) {
        studentService.delete(student);
      }
      message.setSuccessMessage("Das Modul " + modulName + " wurde gelöscht!");
    } else {
      message.setErrorMessage("Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.");
    }

    return message;
  }

  private LocalDateTime[] parseFrist(Modul modul) throws ParseException {
    String frist = modul.getFrist() + " 12:00";
    Date date = new SimpleDateFormat("MM/dd/yyyy hh:mm").parse(frist);
    LocalDateTime actualDate = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime localFrist = date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    return new LocalDateTime[]{actualDate, localFrist};
  }

  public boolean fristIsDate(String frist) {
    String[] elements = frist.split("/");
    if (elements.length == 3) {
      int month = Integer.parseInt(elements[0]);
      if ((month > 0) && (month < 13)) {
        int day = Integer.parseInt(elements[1]);
        return (day > 0) && (day < 32);
      }
    }
    return false;
  }

  public void download(@PathVariable Long id, HttpServletResponse response) {

    byte[] bytes = null;
    File klausurliste = null;

    try {
      try {
        klausurliste = new File("klausurliste_" + id + ".csv");
        Path path = klausurliste.toPath();
        bytes = Files.readAllBytes(path);
        logger.info("Klausurliste mit Neuzulassungen");
      } catch (NoSuchFileException e) {
        List<Student> empty = new ArrayList<>();
        csvService.writeCsvFile(id, empty);
        klausurliste = new File("klausurliste_" + id + ".csv");
        Path path = klausurliste.toPath();
        bytes = Files.readAllBytes(path);
        logger.info("Klausurliste ohne Neuzulassungen");
      }
    } catch (IOException e) {
      logger.error("Fehler in der zwischengespeicherten Liste.", e);
    }

    OutputStream outputStream = writeHeader(id, response);
    try {
      outputStream.write(bytes);
      outputStream.flush();
      outputStream.close();

      countLines(id, klausurliste);


      klausurliste.delete();
    } catch (IOException e) {
      logger.error("Outputstream fehlerhaft!", e);
    }

    logger.info("Klausurliste wurde erfolgreich heruntergeladen.");
  }

  void countLines(@PathVariable Long id, File klausurliste) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(klausurliste));
    Long lines = 0L;
    while (reader.readLine() != null) lines++;
    reader.close();
    Optional<Modul> modul = findById(id);
    Long einzigartigeID = statistikService.modulInDatabase(modul.get().getFrist(), id);
    Optional<ModulStatistiken> modulstat = statistikService.findById(einzigartigeID);
    modulstat.get().setZulassungsZahl(lines);
    statistikService.save(modulstat.get());
  }

  public boolean isFristAbgelaufen(Modul zuPruefendesModul) throws ParseException {
    LocalDateTime[] dates = parseFrist(zuPruefendesModul);
    LocalDateTime localFrist = dates[1];
    LocalDateTime actualDate = dates[0];

    boolean result = localFrist.isBefore(actualDate);
    logger.info("Frist ist abgelaufen: " + result);
    return result;
  }

  private OutputStream writeHeader(Long id, HttpServletResponse response) {
    String fachname = findById(id).get().getName();
    response.setContentType("text/csv");
    String newFilename = "\"klausurliste_" + fachname + ".csv\"";
    response.setHeader("Content-Disposition", "attachment; filename=" + newFilename);
    OutputStream outputStream = null;
    try {
      outputStream = response.getOutputStream();
      String header = "Matrikelnummer,Nachname,Vorname\n";
      outputStream.write(header.getBytes());
    } catch (IOException e) {
      logger.error("HTTPServletResponse liefert fehlerhaften Outputstream.", e);
    }

    return outputStream;
  }

  public FrontendMessage altzulassungVerarbeiten(AltzulassungStudentDto studentDto, boolean papierZulassung, Long id) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    message.resetMessage();
    String modulname = findById(id).get().getName();
    Student student = Student.builder()
        .email(studentDto.getEmail())
        .fachname(modulname)
        .vorname(studentDto.getVorname())
        .nachname(studentDto.getNachname())
        .matrikelnummer(studentDto.getMatrikelnummer())
        .modulId(id)
        .build();
    try {

      String token = quittungService.findQuittung(studentDto.getMatrikelnummer().toString(), id.toString());
      student.setToken(token);
      studentService.save(student);
      message.setSuccessMessage("Student " + student.getMatrikelnummer() + " wurde erfolgreich zur Altzulassungsliste hinzugefügt.");
      emailService.sendMail(student);

    } catch (NoTokenInDatabaseException e) {
      if (papierZulassung) {
        erstelleTokenUndSendeEmail(student, student.getModulId(), true);
        message.setSuccessMessage("Student " + student.getMatrikelnummer() + " wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.");
      } else {
        message.setErrorMessage("Student " + student.getMatrikelnummer() + " hat keine Zulassung in diesem Modul!");
      }
    }

    return message;
  }

  void erstelleTokenUndSendeEmail(Student student, Long id, boolean isAltzulassung) {

    try {

      quittungService.findPublicKey(student.getMatrikelnummer().toString(), student.getModulId().toString());

      if (isAltzulassung) {
        emailService.sendMail(student);
      }

    } catch (NoPublicKeyInDatabaseException e) {

      String tokenString = null;
      try {
        tokenString = tokengenerierungService.erstellenToken(student.getMatrikelnummer().toString(), id.toString());
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
        logger.error("Fehler bei Erstellung des Tokens!", ex);
      }
      student.setToken(tokenString);
      if (isAltzulassung) {
        studentService.save(student);
      }
      emailService.sendMail(student);
    }
  }

  boolean studentIsEmpty(Student student) {
    return student.getVorname().isEmpty() || student.getNachname().isEmpty() || student.getEmail().isEmpty() || student.getMatrikelnummer() == null;
  }

  public boolean missingAttributeInModul(Modul modul) {
    return modul.getName().isEmpty() || modul.getFrist().isEmpty();
  }

  public void saveGesamtTeilnehmerzahlForModul(Long id, Long teilnehmerAnzahl) {
    Modul modul = findById(id).get();
    modul.setTeilnehmer(teilnehmerAnzahl);
    save(modul);
  }
}
