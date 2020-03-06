package mops.klausurzulassung.Services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.organisatoren.Services.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

  private StudentService studentService;

    /*CsvImportService kümmert sich um ein Multipartfile welches ein .csv File repräsentiert. Aus diesem File werden Studenten-Objekte
    generiert die als Liste weitergegeben werden*/


  public List<Student> getStudentListFromInputFile(MultipartFile multipartFile, Long id) throws IOException {
    List<Student> studentList = new ArrayList<>();
    InputStream inputstream = multipartFile.getInputStream();

    BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
    String line;

    while ((line = br.readLine()) != null) {
      System.out.println("Content:" + line);
      studentList.add(createStudentFromInputStream(line, id));
    }
    System.out.println(studentList);
    return studentList;

  }

  public void putStudentOntoList(CSVWriter writer, Student student) {
    String[] list = {String.valueOf(student.getMatrikelnummer()), student.getNachname(), student.getVorname()};

    writer.writeNext(list, false);
  }

  public Student createStudentFromInputStream(String line, Long id) {

    String vorname, nachname, email, fachname, token;
    Long matrikelnummer, modulId;

    String[] temp = line.split(",");
    vorname = temp[0];
    nachname = temp[1];
    email = temp[2];
    matrikelnummer = Long.parseLong(temp[3]);
    modulId = id;
    fachname = null;
    token = null;
    return new Student(vorname, nachname, email, matrikelnummer, modulId, fachname, token);

  }

  public void writeCsvFile(Long id, List<Student> students) throws IOException {
    File outputFile = new File("./klausurliste.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    Iterable<Student> altzugelassene = studentService.findByModulId(id);

    for (Student student : altzugelassene) {
      students.add(student);
    }

    for (Student student : students) {
      putStudentOntoList(writer, student);
      writer.flush();
    }
    writer.close();
  }
}
