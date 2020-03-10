package mops.klausurzulassung.Services;


import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.Student;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

  private StudentService studentService;

  public CsvService(StudentService studentService) {
    this.studentService = studentService;
  }

  /*CsvImportService kümmert sich um ein Multipartfile welches ein .csv File repräsentiert. Aus diesem File werden Studenten-Objekte
    generiert die als Liste weitergegeben werden*/

  // Reihenfolge im input.csv:
  // Vorname, Nachname, Email, Matrikelnummer

  // Reihenfolge im output.csv:
  // Matrikelnummer, Nachname, Vorname

  public List<Student> getStudentListFromInputFile(Iterable<CSVRecord> records, Long id) throws IOException {
    List<Student> studentList = new ArrayList<>();

    for (CSVRecord record : records) {
      studentList.add(createStudentFromInputStream(record, id));
    }
    return studentList;
  }

  public void putStudentOntoList(CSVWriter writer, Student student) {
    String[] list = {String.valueOf(student.getMatrikelnummer()), student.getNachname(), student.getVorname()};

    writer.writeNext(list, false);
  }

  public Student createStudentFromInputStream(CSVRecord record, Long id) {

    String vorname, nachname, email, fachname, token;
    Long matrikelnummer, modulId;

    vorname = record.get("Vorname");
    nachname = record.get("Nachname");
    email = record.get("Email");
    matrikelnummer = Long.parseLong(record.get("Matrikelnummer"));
    modulId = id;
    fachname = null;
    token = null;

    return new Student(vorname, nachname, email, matrikelnummer, modulId, fachname, token);
  }

  public void writeCsvFile(Long id, List<Student> students) throws IOException {
    File outputFile = new File("klausurliste_"+id+".csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    Iterable<Student> altzugelassene = studentService.findByModulId(id);

    if (altzugelassene != null) {
      for (Student student : altzugelassene) {
        students.add(student);
      }
    }

    for (Student student : students) {
      putStudentOntoList(writer, student);
      writer.flush();
    }
    writer.close();
  }
}
