package mops.klausurzulassung.Services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.Student;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    /*CsvImportService kümmert sich um ein Multipartfile welches ein .csv File repräsentiert. Aus diesem File werden Studenten-Objekte
    generiert die als Liste weitergegeben werden*/


  public List<Student> getStudentListFromInputFile(MultipartFile multipartFile, Long id) throws IOException {
    List<Student> studentList = new ArrayList<>();
    InputStream inputstream = multipartFile.getInputStream();

    BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
    String line;

    while ((line = br.readLine()) != null) {
      studentList.add(createStudentFromInputStream(line, id));
    }
    return studentList;

  }

  public void putStudentOntoList(CSVWriter writer, Student student) {
    ArrayList<String> list = new ArrayList<>();
    list.add(String.valueOf(student.getMatrikelnummer()));
    list.add(student.getNachname());
    list.add(student.getVorname());

    writer.writeNext((String[]) list.toArray());
  }

  private Student createStudentFromInputStream(String line, Long id) {

    String vorname, nachname, email, fachname, token;
    Long matrikelnummer, raumId;

    String[] temp = line.split(",");
    vorname = temp[0];
    nachname = temp[1];
    email = temp[2];
    matrikelnummer = Long.parseLong(temp[3]);
    raumId = id;
    fachname = null;
    token = null;
    return new Student(vorname, nachname, email, matrikelnummer, raumId, fachname, token);

  }
}
