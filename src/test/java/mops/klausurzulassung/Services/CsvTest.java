package mops.klausurzulassung.Services;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.CsvService;
import mops.klausurzulassung.Services.StudentService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CsvTest {

  private CsvService csvService;
  private MultipartFile multipartFile;
  private CSVRecord record;
  private StudentService studentService;

  @Test
  public void getStudentListFromInputFileTest() throws IOException {
    csvService = new CsvService(studentService);
    this.multipartFile = mock(MultipartFile.class);

    List<Student> students = new ArrayList<>();
    students.add(new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null));
    students.add(new Student("Rebecca", "Fröhlich", "refro100@hhu.de", 2658447L, 1L, null, null));

    File outputFile = new File("liste.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] cara = {"Cara", "Überschär", "caueb100@hhu.de", "2659396"};
    String[] rebecca = {"Rebecca", "Fröhlich", "refro100@hhu.de", "2658447"};
    writer.writeNext(cara, false);
    writer.writeNext(rebecca, false);
    writer.flush();
    writer.close();

    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396\nRebecca,Fröhlich,refro100@hhu.de,2658447".getBytes());

    when(multipartFile.getInputStream()).thenReturn(input);
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Vorname","Nachname","Email","Matrikelnummer").parse(new InputStreamReader(input));

    List<Student> studentList = csvService.getStudentListFromInputFile(records, 1L);

    assertEquals(students, studentList);

    // Löschen des Test-Files
    File file = new File("liste.csv");
    if (file.exists()) {
      file.delete();
    }
  }


  @Test
  public void createStudentFromInputStreamTest() throws IOException {
    csvService = new CsvService(studentService);

    Student student = new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, "ProPra2", "123Ldnd");

    File outputFile = new File("studenten.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] header = {"Vorname","Nachname","Email","Matrikelnummer"};
    writer.writeNext(header, false);
    String[] list = {student.getVorname(), student.getNachname(), student.getEmail(), String.valueOf(student.getMatrikelnummer())};
    writer.writeNext(list, false);
    writer.flush();
    writer.close();

    Reader input = new FileReader("studenten.csv");
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("Vorname","Nachname","Email","Matrikelnummer").withSkipHeaderRecord().parse(input);


    Student cara = null;
    for (CSVRecord record : records) {
      System.out.println("Record: "+record);
      cara = csvService.createStudentFromInputStream(record, 1L);
    }

    Student expected = new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null);

    assertEquals(expected, cara);

    // Löschen des Test-Files
    File file = new File("studenten.csv");
    if (file.exists()) {
      file.delete();
    }
  }

  @Test
  public void putStudentOntoListTest() throws IOException {
    this.csvService = new CsvService(studentService);

    ArrayList<Student> students = new ArrayList<>();
    students.add(new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, "ProPra2", "123Ldnd"));
    students.add(new Student("Rebecca", "Fröhlich", "refro100@hhu.de", 2658447L, 1L, "ProPra2", "4493Lsksi"));

    File outputFile = new File("klausurliste.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);


    for (Student student : students) {
      csvService.putStudentOntoList(writer, student);
      writer.flush();
    }
    writer.close();

    BufferedReader br = new BufferedReader(new FileReader("klausurliste.csv"));
    String line = br.readLine();

    assertNotNull(line);
    assertEquals(line, "2659396,Überschär,Cara");
    line = br.readLine();
    assertEquals(line, "2658447,Fröhlich,Rebecca");

    // Löschen des Test-Files
    File file = new File("klausurliste.csv");
    if (file.exists()) {
      file.delete();
    }
  }
}
