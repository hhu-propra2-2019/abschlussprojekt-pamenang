package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.AltzulassungStudentDto;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
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

  public Iterable<Modul> allModuls() {
    return modulRepository.findAll();
  }

  public Iterable<Modul> findByOwner(String name) {
    return modulRepository.findByOwner(name);
  }

  public Optional<Modul> findById(Long id) {
    return modulRepository.findById(id);
  }

  private void delete(Modul modul) {
    modulRepository.delete(modul);
  }

  public void save(Modul modul) {
    modulRepository.save(modul);
  }

  public String[] verarbeiteUploadliste(Long id, MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoPublicKeyInDatabaseException {
    successMessage = null;
    errorMessage = null;
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").parse(new InputStreamReader(file.getInputStream()));

    boolean countColumns = true;

    for (CSVRecord record : records) {
      if (record.size() != 4) {
        countColumns = false;
      }
    }

    records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").withSkipHeaderRecord().parse(new InputStreamReader(file.getInputStream()));


    if (!countColumns) {
      errorMessage = "Datei hat eine falsche Anzahl von Einträgen pro Zeile!";
    } else if (file.isEmpty()) {
      errorMessage = "Datei ist leer oder es wurde keine Datei ausgewählt!";
    } else {
      List<Student> students = csvService.getStudentListFromInputFile(records, id);


      for (Student student : students) {
        erstelleTokenUndSendeEmail(student, id, false);
      }
      csvService.writeCsvFile(id, students);
      successMessage = "Zulassungsliste wurde erfolgreich verarbeitet.";
    }
    String[] messages = {errorMessage, successMessage};
    return messages;
  }

  public String[] deleteStudentsFromModul(Long id) {
    successMessage = null;
    errorMessage = null;
    Optional<Modul> modul = findById(id);
    if (modul.isPresent()) {
      String modulName = modul.get().getName();
      delete(modul.get());

      Iterable<Student> students = studentService.findByModulId(id);
      for (Student student : students) {
        studentService.delete(student);
      }

      successMessage = "Das Modul " + modulName + " wurde gelöscht!";
    } else {
      errorMessage = "Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.";
    }

    String[] messages = {errorMessage, successMessage};
    return messages;
  }

  public String[] download(@PathVariable Long id, HttpServletResponse response) throws IOException {
    errorMessage = null;
    successMessage = null;

    try {
      File klausurliste = new File("klausurliste_"+Long.toString(id)+".csv");
      Path path = klausurliste.toPath();
      byte[] bytes = Files.readAllBytes(path);

      String fachname = findById(id).get().getName();
      response.setContentType("text/csv");
      String newFilename = "\"klausurliste_"+fachname+".csv\"";
      response.setHeader("Content-Disposition", "attachment; filename="+newFilename);
      OutputStream outputStream = response.getOutputStream();
      String header = "Matrikelnummer,Nachname,Vorname\n";
      outputStream.write(header.getBytes());
      outputStream.write(bytes);
      outputStream.flush();
      outputStream.close();
      klausurliste.delete();

    } catch (NoSuchFileException e){
      errorMessage = "Bitte erst eine Zulassungsliste hochladen!";
      response.sendRedirect("/zulassung1/modul" + "/" + id);
    }
    String[] messages = {errorMessage, successMessage};
    return messages;
  }

  public String[] altzulassungVerarbeiten(AltzulassungStudentDto studentDto, boolean papierZulassung, Long id) throws NoSuchAlgorithmException, NoPublicKeyInDatabaseException, InvalidKeyException, SignatureException {
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

        String token = quittungService.findTokenByQuittung(studentDto.getMatrikelnummer().toString(), id.toString());
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


    String[] messages = {errorMessage, successMessage};
    return messages;
  }

  private void erstelleTokenUndSendeEmail(Student student, Long id, boolean isAltzulassung) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoPublicKeyInDatabaseException {

    try {

      quittungService.findPublicKeyByQuittung(student.getMatrikelnummer().toString(), student.getModulId().toString());

      if (isAltzulassung){
        //emailService.sendMail(student);
      }

    } catch (NoPublicKeyInDatabaseException e){

      String tokenString = tokengenerierungService.erstellenToken(student.getMatrikelnummer().toString(), id.toString());
      student.setToken(tokenString);
      if (isAltzulassung){
        studentService.save(student);
      }
      //emailService.sendMail(student);
    }
  }

  private boolean studentIsEmpty(Student student) {
    if(student.getVorname().isEmpty() || student.getNachname().isEmpty() || student.getEmail().isEmpty() ||student.getMatrikelnummer() == null){
      return true;
    }
    return false;
  }
}
