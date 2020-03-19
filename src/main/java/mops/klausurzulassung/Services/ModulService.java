package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.AltzulassungStudentDto;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
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
  private String errorMessage;
  private String successMessage;
  private Logger logger = LoggerFactory.getLogger(ModulService.class);


  public ModulService(ModulRepository modulRepository, CsvService csvService, StudentService studentService, TokengenerierungService tokengenerierungService, EmailService emailService, QuittungService quittungService) {
    this.csvService = csvService;
    this.studentService = studentService;
    this.tokengenerierungService = tokengenerierungService;
    this.emailService = emailService;
    this.quittungService = quittungService;
    this.modulRepository = modulRepository;
    this.errorMessage = null;
    this.successMessage = null;
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

  private void save(Modul modul) {
    modulRepository.save(modul);
  }

  public String[] verarbeiteUploadliste(Long id, MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    successMessage = null;
    errorMessage = null;
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").parse(new InputStreamReader(file.getInputStream()));

    boolean countColumns = true;

    for (CSVRecord record : records) {
      if (record.size() != 4) {
        countColumns = false;
        break;
      }
    }

    records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").withSkipHeaderRecord().parse(new InputStreamReader(file.getInputStream()));

    if (!countColumns) {
      errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    } else if (file.isEmpty()) {
      errorMessage = "Datei ist leer oder es wurde keine Datei ausgewählt!";
    } else {
      List<Student> students = csvService.getStudentListFromInputFile(records, id);

      String modulname = findById(id).get().getName();

      for (Student student : students) {
        student.setFachname(modulname);
        erstelleTokenUndSendeEmail(student, id, false);
      }
      csvService.writeCsvFile(id, students);
      successMessage = "Zulassungsliste wurde erfolgreich verarbeitet.";
    }
    return new String[]{errorMessage, successMessage};
  }

  public String[] deleteStudentsFromModul(Long id) {
    successMessage = null;
    errorMessage = null;
    Optional<Modul> modul = findById(id);
    if (modul.isPresent()) {
      String modulName = modul.get().getName();
      modul.get().setOwner(null);
      modul.get().setFrist(null);
      modul.get().setActive(false);
      save(modul.get());

      Iterable<Student> students = studentService.findByModulId(id);
      for (Student student : students) {
        studentService.delete(student);
      }

      successMessage = "Das Modul " + modulName + " wurde gelöscht!";
    } else {
      errorMessage = "Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.";
    }

    return new String[]{errorMessage, successMessage};
  }

  public  Object[] neuesModul(Modul modul, Principal principal) throws ParseException {
    errorMessage = null;
    successMessage = null;
    modul.setOwner(principal.getName());

    if (!missingAttributeInModul(modul)) {
      String frist = modul.getFrist();
      Date date = new SimpleDateFormat("dd.MM.yyyy hh:mm").parse(frist);
      LocalDateTime actualDate = LocalDateTime.now().withNano(0).withSecond(0);
      LocalDateTime localFrist = date.toInstant()
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();


      if (localFrist.isAfter(actualDate)) {
        if (findById(modul.getId()).isPresent()) {
          errorMessage = "Diese Modul-ID existiert schon, bitte eine andere ID eingeben!";
        } else {
          save(modul);
          successMessage = "Neues Modul wurde erfolgreich hinzugefügt!";
          modul = new Modul();
        }
      } else {
        errorMessage = "Frist liegt in der Vergangenheit, bitte eine andere Frist eingeben!";
      }
    } else {
      errorMessage = "Alle Felder im Formular müssen ausgefüllt sein!";
    }
    return new Object[]{modul, errorMessage, successMessage};
  }

  public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {

    byte[] bytes;
    File klausurliste;

    try {
      klausurliste = new File("klausurliste_" + id + ".csv");
      Path path = klausurliste.toPath();
      bytes = Files.readAllBytes(path);
    } catch (NoSuchFileException e) {
      List<Student> empty = new ArrayList<>();
      csvService.writeCsvFile(id, empty);
      klausurliste = new File("klausurliste_" + id + ".csv");
      Path path = klausurliste.toPath();
      bytes = Files.readAllBytes(path);
    }

    OutputStream outputStream = writeHeader(id, response);
    outputStream.write(bytes);
    outputStream.flush();
    outputStream.close();
    klausurliste.delete();

  }

  public String[] saveNewModul(Modul modul, String owner) throws ParseException {
    successMessage = null;
    errorMessage = null;
    String page;

    if (!missingAttributeInModul(modul)) {
      if (!isFristAbgelaufen(modul)) {
        modul.setOwner(owner);
        modul.setActive(true);
        save(modul);
        page = "modulAuswahl";
      } else {
        errorMessage = "Die Frist muss in der Zukunft liegen!";
        page = "redirect:/zulassung1/modulHinzufuegen";
      }
    } else {
      errorMessage = "Bitte beide Felder ausfüllen!";
      page = "redirect:/zulassung1/modulHinzufuegen";
    }
    return new String[]{errorMessage, successMessage, page};
  }

  public boolean isFristAbgelaufen(Modul zuPrüfendesModul) throws ParseException {

    String frist = zuPrüfendesModul.getFrist();
    Date date = new SimpleDateFormat("dd.MM.yyyy hh:mm").parse(frist);
    LocalDateTime actualDate = LocalDateTime.now().withNano(0).withSecond(0);
    LocalDateTime localFrist = date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    boolean result = localFrist.isBefore(actualDate);
    logger.debug("Frist ist abgelaufen: " + result);
    return result;
  }

  public String[] modulBearbeiten(Modul modul, Long id, Principal principal) throws ParseException {
    successMessage = null;
    errorMessage = null;
    String page;
    if (!missingAttributeInModul(modul)) {
      if (!isFristAbgelaufen(modul)) {
        Modul vorhandenesModul = findById(id).get();
        vorhandenesModul.setName(modul.getName());
        vorhandenesModul.setFrist(modul.getFrist());
        vorhandenesModul.setOwner(principal.getName());
        vorhandenesModul.setActive(true);
        save(vorhandenesModul);
        page = "redirect:/zulassung1/modulAuswahl";
      } else {
        errorMessage = "Die Frist muss in der Zukunft liegen!";
        page = "redirect:/zulassung1/modulBearbeiten/" + id;
      }
    } else {
      errorMessage = "Beide Felder müssen ausgefüllt sein!";
      page = "redirect:/zulassung1/modulBearbeiten/" + id;
    }
    return new String[]{errorMessage, successMessage, page};
  }

  private OutputStream writeHeader(Long id, HttpServletResponse response) throws IOException {
    String fachname = findById(id).get().getName();
    response.setContentType("text/csv");
    String newFilename = "\"klausurliste_" + fachname + ".csv\"";
    response.setHeader("Content-Disposition", "attachment; filename=" + newFilename);
    OutputStream outputStream = response.getOutputStream();
    String header = "Matrikelnummer,Nachname,Vorname\n";
    outputStream.write(header.getBytes());
    return outputStream;
  }

  public String[] altzulassungVerarbeiten(AltzulassungStudentDto studentDto, boolean papierZulassung, Long id) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    successMessage = null;
    errorMessage = null;

      String modulname = findById(id).get().getName();
      Student  student = Student.builder()
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
        successMessage = "Student "+student.getMatrikelnummer()+" wurde erfolgreich zur Altzulassungsliste hinzugefügt.";
        emailService.sendMail(student);

      } catch (NoTokenInDatabaseException e) {
        if (papierZulassung) {
          erstelleTokenUndSendeEmail(student, student.getModulId(), true);
          successMessage = "Student "+student.getMatrikelnummer()+" wurde erfolgreich zur Altzulassungsliste hinzugefügt und hat ein Token.";
        } else {
          errorMessage = "Student " + student.getMatrikelnummer() + " hat keine Zulassung in diesem Modul!";
        }
      }

    return new String[]{errorMessage, successMessage};
  }

  void erstelleTokenUndSendeEmail(Student student, Long id, boolean isAltzulassung) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    try {

      quittungService.findPublicKey(student.getMatrikelnummer().toString(), student.getModulId().toString());

      if (isAltzulassung){
        emailService.sendMail(student);
      }

    } catch (NoPublicKeyInDatabaseException e){

      String tokenString = tokengenerierungService.erstellenToken(student.getMatrikelnummer().toString(), id.toString());
      student.setToken(tokenString);
      if (isAltzulassung){
        studentService.save(student);
      }
      emailService.sendMail(student);
    }
  }

  boolean studentIsEmpty(Student student) {
    return student.getVorname().isEmpty() || student.getNachname().isEmpty() || student.getEmail().isEmpty() || student.getMatrikelnummer() == null;
  }

  private boolean missingAttributeInModul(Modul modul) {
    return modul.getName().isEmpty() || modul.getFrist().isEmpty();
  }
}
