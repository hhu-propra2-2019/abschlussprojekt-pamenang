package mops.klausurzulassung.services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.database_entity.Student;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

  private StudentService studentService;
  private Logger logger = LoggerFactory.getLogger(CsvService.class);

  public CsvService(StudentService studentService) {
    this.studentService = studentService;
  }

  /**
   * This method is called from ModulService.verarbeiteUploadliste.
   *
   * @param records contains the lines of the csv-file with header: name, surname, email, matriculationnumber
   * @param id      of the selected modul
   * @return List of students from input file
   */
  public List<Student> getStudentListFromInputFile(Iterable<CSVRecord> records, Long id) throws NumberFormatException {
    List<Student> studentList = new ArrayList<>();

    for (CSVRecord record : records) {
      studentList.add(createStudentFromInputStream(record, id));
    }

    logger.info("Studentenliste wurde aus Csv-Datei erstellt!");

    return studentList;
  }

  /**
   * This method is called from CsvService.writeCsvFile.
   *
   * Writes student on output file.
   *
   * @param writer   writer for output csv files
   * @param student  student which should be written onto list
   */
  public void putStudentOntoList(CSVWriter writer, Student student) {
    String[] list = {String.valueOf(student.getMatrikelnummer()), student.getNachname(), student.getVorname()};
    writer.writeNext(list, false);
    logger.info("Studenten in Csv-Liste geschrieben");
  }

  /**
   * This method is called from CsvService.getStudentListFromInputFile.
   *
   * @param record   contains one line of the csv-file
   * @param id       of the selected modul
   * @return Student object created from record
   */
  public Student createStudentFromInputStream(CSVRecord record, Long id) throws NumberFormatException {

    String vorname, nachname, email, fachname, token;
    long matrikelnummer, modulId;

    vorname = record.get("Vorname");
    nachname = record.get("Nachname");
    email = record.get("Email");
    matrikelnummer = Long.parseLong(record.get("Matrikelnummer"));

    modulId = id;
    fachname = null;
    token = null;

    return new Student(vorname, nachname, email, matrikelnummer, modulId, fachname, token);
  }

  /**
   * This method is called from ModulService.verarbeiteUploadliste and ModulService.download.
   *
   * Writes students from input file and students with an old license onto the output file with header: matriculationnumber, surname, name.
   *
   * @param id       of the selected modul
   * @param students List of from the input file
   */
  public void writeCsvFile(Long id, List<Student> students) {
    File outputFile = new File("klausurliste_" + id + ".csv");
    FileWriter fileWriter;

    try {
      fileWriter = new FileWriter(outputFile);
      CSVWriter writer = new CSVWriter(fileWriter);
      Iterable<Student> altzugelassene = studentService.findByModulId(id);

      boolean bereitsEnthalten;
      if (altzugelassene != null) {
        for (Student altStudent : altzugelassene) {
          bereitsEnthalten = false;
          for (Student student : students) {
            if (student.getMatrikelnummer().equals(altStudent.getMatrikelnummer())) {
              bereitsEnthalten = true;
              break;
            }
          }
          if (!bereitsEnthalten) {
            students.add(altStudent);
          }
        }
      }

      for (Student student : students) {
        putStudentOntoList(writer, student);
        writer.flush();
      }
      writer.close();
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }
}
