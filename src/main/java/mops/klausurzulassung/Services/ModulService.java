package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Repositories.ModulRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

@Component
public class ModulService {
  private final ModulRepository modulRepository;

  public ModulService(ModulRepository modulRepository) {
    this.modulRepository = modulRepository;
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

  public void delete(Modul modul) {
    modulRepository.delete(modul);
  }

  public void save(Modul modul) {
    modulRepository.save(modul);
  }

  public void verarbeiteUploadliste(@PathVariable Long id, @RequestParam("datei") MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoPublicKeyInDatabaseException {
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").parse(new InputStreamReader(file.getInputStream()));

    boolean countColumns = true;

    for (CSVRecord record : records) {
      if (record.size() != 4) {
        countColumns = false;
      }
    }

    records = CSVFormat.DEFAULT.withHeader("Vorname", "Nachname", "Email", "Matrikelnummer").withSkipHeaderRecord().parse(new InputStreamReader(file.getInputStream()));

    System.out.println();

    if (!countColumns) {
      setMessages("Datei hat eine falsche Anzahl von Einträgen pro Zeile!", null);
    } else if (file.isEmpty()) {
      setMessages("Datei ist leer oder es wurde keine Datei ausgewählt!", null);
    } else {
      List<Student> students = csvService.getStudentListFromInputFile(records, id);

      System.out.println(students.toString());

      for (Student student : students) {
        System.out.println("students :"+student);
        erstelleTokenUndSendeEmail(student, id);
      }
      csvService.writeCsvFile(id, students);
      setMessages(null, "Zulassungsliste wurde erfolgreich verarbeitet.");
    }
  }
}
