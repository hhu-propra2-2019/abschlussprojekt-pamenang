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

  public ModulService(
      ModulRepository modulRepository,
      CsvService csvService,
      StudentService studentService,
      TokengenerierungService tokengenerierungService,
      EmailService emailService,
      QuittungService quittungService,
      StatistikService statistikService) {
    this.csvService = csvService;
    this.studentService = studentService;
    this.tokengenerierungService = tokengenerierungService;
    this.emailService = emailService;
    this.quittungService = quittungService;
    this.modulRepository = modulRepository;
    this.statistikService = statistikService;
  }

  /**
   * This methods finds all active moduls corresponding to the given owner.
   *
   * @param name of the owner
   * @param active if the modul is active
   * @return Iterable of the moduls
   */
  public Iterable<Modul> findByOwnerAndActive(String name, boolean active) {
    return modulRepository.findByOwnerAndActive(name, active);
  }

  /**
   * This methods finds a modul corresponding to the given id.
   *
   * @param id of the modul
   * @return Optional of the modul
   */
  public Optional<Modul> findById(Long id) {
    return modulRepository.findById(id);
  }

  /**
   * This methods finds all active moduls.
   *
   * @param active if the modul is active
   * @return Iterable of the moduls
   */
  public Iterable<Modul> findByActive(boolean active) {
    return modulRepository.findByActive(active);
  }

  /**
   * This methods saves the modul.
   *
   * @param modul which contains about the modul
   */
  public void save(Modul modul) {
    modulRepository.save(modul);
    logger.info("Das Modul " + modul + " wurde gespeichert.");
  }

  /**
   * This method is called from ModulController.uploadListe.
   *
   * <p>Checks format of uploaded csv-file and processes students.
   *
   * @param id of the selected modul
   * @param file input csv file with header: name, surname, email, matriculationnumber
   * @return error or success message for ModulController
   */
  public FrontendMessage verarbeiteUploadliste(Long id, MultipartFile file) {
    message.resetMessage();
    Iterable<CSVRecord> records = getCsvRecords(file);

    boolean validInputFile = isValidInputFile(records);

    logger.info("Die Liste wurde erfolgreich eingelesen.");

    try {
      records =
          CSVFormat.DEFAULT
              .withHeader("Vorname", "Nachname", "Email", "Matrikelnummer")
              .withSkipHeaderRecord()
              .parse(new InputStreamReader(file.getInputStream()));
    } catch (IOException e) {
      logger.error("Fehler beim Einlesen der Datei!", e);
    }

    if (!validInputFile) {
      message.setErrorMessage("Datei hat eine falsche Anzahl von Einträgen pro Zeile!");
    } else if (file.isEmpty()) {
      message.setErrorMessage("Datei ist leer oder es wurde keine Datei ausgewählt!");
    } else {
      verarbeiteStudenten(id, records);
    }

    return message;
  }

  /**
   * This method is called from ModulService.verarbeiteUploadliste.
   *
   * <p>Creates student objects from records and generates token which are send to the student in an
   * email. Writes all students with a admission for this modul to download file.
   *
   * @param id of the selected modul
   * @param records contains the lines of the csv-file with header: name, surname, email,
   *     matriculationnumber
   */
  private void verarbeiteStudenten(Long id, Iterable<CSVRecord> records) {
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
      message.setErrorMessage(
          "Eine Matrikelnummer der hochgeladenen Liste enthält nicht nur Zahlen!");
    }
  }

  /**
   * This method is called from ModulService.verarbeiteUploadliste.
   *
   * <p>Checks if the input file contains 4 strings in each line.
   *
   * @param records contains the lines of the csv-file with header: name, surname, email,
   *     matriculationnumber
   * @return if the input file is valid
   */
  private boolean isValidInputFile(Iterable<CSVRecord> records) {
    boolean countColumns = true;

    for (CSVRecord record : records) {
      if (record.size() != 4) {
        countColumns = false;
        break;
      }
    }
    return countColumns;
  }

  /**
   * This method is called from ModulService.verarbeiteUploadliste.
   *
   * @param file input csv file with header: name, surname, email, matriculationnumber
   * @return records from input file
   */
  private Iterable<CSVRecord> getCsvRecords(MultipartFile file) {
    Iterable<CSVRecord> records = null;
    try {
      records =
          CSVFormat.DEFAULT
              .withHeader("Vorname", "Nachname", "Email", "Matrikelnummer")
              .parse(new InputStreamReader(file.getInputStream()));
    } catch (IOException e) {
      logger.error("Fehler beim Einlesen der Datei!", e);
    }
    return records;
  }

  /**
   * This method is called from ModulController.deleteModul.
   *
   * <p>Sets the modul to inactiv and deletes all students participating this modul.
   *
   * @param id of the selected modul
   * @return error or success message for ModulController
   */
  public FrontendMessage deleteStudentsFromModul(Long id, String user) {

    logger.info("ID: " + id);
    Optional<Modul> modul = findById(id);
    if (modul.isPresent()) {
      if (!checkOwner(modul.get(), user)) {
        message.setErrorMessage(
            "Modul konnte nicht gelöscht werden, da es einem anderen User gehört");
        return message;
      }

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
      message.setErrorMessage(
          "Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.");
    }

    return message;
  }

  /**
   * This method is called from ModulService.deleteStudentsFromModul
   *
   * <p>
   *
   * @param modul responding to the selected id
   * @param user who tries to modify the modul
   * @return Checks if the user is the corresponding owner
   */
  boolean checkOwner(Modul modul, String user) {
    return user.equals(modul.getOwner());
  }

  /**
   * This method is called from ModulService.istFristAbgelaufen.
   *
   * <p>Takes deadline from modul and sets time at 12PM.
   *
   * @param modul responding to the selected id
   * @return now and deadline as LocalDateTime
   */
  private LocalDateTime[] parseFrist(Modul modul) throws ParseException {
    String frist = modul.getFrist() + " 12:00";
    Date date = new SimpleDateFormat("MM/dd/yyyy hh:mm").parse(frist);
    LocalDateTime actualDate = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime localFrist = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    return new LocalDateTime[] {actualDate, localFrist};
  }

  /**
   * This method is called from ModulService.istFristAbgelaufen.
   *
   * @param frist deadline for modul
   * @return if deadline is valid date
   */
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

  /**
   * This method is called from ModulController.downloadListe.
   *
   * <p>Writes the download file.
   *
   * @param id of the selected modul
   * @param response contains download file
   */
  public void download(Long id, HttpServletResponse response) {

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

    writeOutputFile(id, response, bytes, klausurliste);

    logger.info("Klausurliste wurde erfolgreich heruntergeladen.");
  }

  /**
   * This method is called from ModulService.download.
   *
   * <p>Writes OutputStream in HttpServletResponse.
   *
   * @param id of the selected modul
   * @param response contains download file
   * @param bytes content of the output file
   * @param klausurliste file which should be deleted after download
   */
  private void writeOutputFile(
      Long id, HttpServletResponse response, byte[] bytes, File klausurliste) {
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
  }

  /**
   * This method is called from ModulService.download.
   *
   * <p>Counts number of admissions for this modul.
   *
   * @param id of the selected modul
   * @param klausurliste file which contains students with admission
   */
  void countLines(Long id, File klausurliste) throws IOException {
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

  /**
   * This method is called from ModulController.backToModulAuswahl and
   * ModulController.modulAbschicken.
   *
   * @param zuPruefendesModul which is selected
   * @return if deadline of the modul has expired
   */
  public boolean isFristAbgelaufen(Modul zuPruefendesModul) throws ParseException {
    LocalDateTime[] dates = parseFrist(zuPruefendesModul);
    LocalDateTime localFrist = dates[1];
    LocalDateTime actualDate = dates[0];

    boolean result = localFrist.isBefore(actualDate);
    logger.info("Frist ist abgelaufen: " + result);
    return result;
  }

  /**
   * This method is called from ModulService.writeOutputFile.
   *
   * @param id of the selected modul
   * @param response contains download file
   * @return outputstream with the header for the output file
   */
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

  /**
   * This method is called from ModulController.altzulassungHinzufuegen.
   *
   * <p>Checks if admission is saved in the database and sends token in email. If the student got a
   * paper admission then the token is generated and an email is send. In both cases the student is
   * written into the database for old admissions.
   *
   * @param studentDto represents student
   * @param papierZulassung if the student has an offline/paper admission
   * @param id of the selected modul
   * @return error or success message for ModulController
   */
  public FrontendMessage altzulassungVerarbeiten(
      AltzulassungStudentDto studentDto, boolean papierZulassung, Long id) {

    message.resetMessage();
    String modulname = findById(id).get().getName();
    Student student =
        Student.builder()
            .email(studentDto.getEmail())
            .fachname(modulname)
            .vorname(studentDto.getVorname())
            .nachname(studentDto.getNachname())
            .matrikelnummer(studentDto.getMatrikelnummer())
            .modulId(id)
            .build();
    try {

      String token =
          quittungService.findQuittung(studentDto.getMatrikelnummer().toString(), id.toString());
      student.setToken(token);
      studentService.save(student);
      message.setSuccessMessage(
          "Student "
              + student.getMatrikelnummer()
              + " wurde erfolgreich zur Altzulassungsliste hinzugefügt.");
      sendAltzulassungsEmail(student);

    } catch (NoTokenInDatabaseException e) {
      if (papierZulassung) {
        erstelleTokenUndSendeEmail(student, student.getModulId(), true);
        message.setSuccessMessage(
            "Student "
                + student.getMatrikelnummer()
                + " wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.");
      } else {
        message.setErrorMessage(
            "Student " + student.getMatrikelnummer() + " hat keine Zulassung in diesem Modul!");
      }
    }

    return message;
  }

  private void sendAltzulassungsEmail(Student student) {
    boolean statusEmail = emailService.resendEmail(student);
    if(!statusEmail){
      message.setErrorMessage("Email konnte nicht verschickt werden an: " + student.getEmail());
    }
  }

  /**
   * This method is called from ModulService.verarbeiteStudenten and
   * ModulService.altzulassungVerarbeiten.
   *
   * <p>Generates Token and sends email to student.
   *
   * @param student represents student
   * @param id of the selected modul
   * @param isAltzulassung if the student has an old admission
   */
  void erstelleTokenUndSendeEmail(Student student, Long id, boolean isAltzulassung) {

    try {

      quittungService.findPublicKey(
          student.getMatrikelnummer().toString(), student.getModulId().toString());

      if (isAltzulassung) {
        emailService.sendMail(student);
      }

    } catch (NoPublicKeyInDatabaseException e) {

      String tokenString = null;
      try {
        tokenString =
            tokengenerierungService.erstellenToken(
                student.getMatrikelnummer().toString(), id.toString());
      } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
        logger.error("Fehler bei Erstellung des Tokens!", ex);
      }
      student.setToken(tokenString);
      if (isAltzulassung) {
        studentService.save(student);
      }
      sendAltzulassungsEmail(student);
    }
  }

  /**
   * This method is called from ModulController.backToModulAuswahl and
   * ModulController.modulAbschicken.
   *
   * @param modul responding to the selected id
   * @return if modul has name and deadline
   */
  public boolean missingAttributeInModul(Modul modul) {
    return modul.getName().isEmpty() || modul.getFrist().isEmpty();
  }

  /**
   * This method is called from ModulController.modulTeilnehmerHinzufuegen.
   *
   * <p>Saves the number of participants for the selected modul.
   *
   * @param id of the selected modul
   * @param teilnehmerAnzahl number of participants
   */
  public void saveGesamtTeilnehmerzahlForModul(Long id, Long teilnehmerAnzahl) {
    Modul modul = findById(id).get();
    modul.setTeilnehmer(teilnehmerAnzahl);
    save(modul);
  }
}
