package mops;

import com.opencsv.CSVWriter;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.CsvService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class CsvTest {

  private CsvService csvService;
  private MultipartFile multipartFile;

  @Test
  public void getStudentListFromInputFileTest() throws IOException {
    csvService = new CsvService();

    this.multipartFile = mock(MultipartFile.class);

    List<Student> students = new ArrayList<>();
    students.add(new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null));
    students.add(new Student("Rebecca", "Fröhlich", "refro100@hhu.de", 2658447L, 1L, null, null));

    File outputFile = new File("liste.csv");
    FileWriter fileWriter = new FileWriter(outputFile);
    CSVWriter writer = new CSVWriter(fileWriter);

    String[] cara = {"Cara", "Überschär", "caueb100@hhu.de", "2659396", "1"};
    String[] rebecca = {"Rebecca", "Fröhlich", "refro100@hhu.de", "2658447", "1"};
    writer.writeNext(cara, false);
    writer.writeNext(rebecca, false);
    writer.flush();
    writer.close();


    InputStream input = new ByteArrayInputStream("Cara,Überschär,caueb100@hhu.de,2659396,1,\nRebecca,Fröhlich,refro100@hhu.de,2658447,1".getBytes());


    when(multipartFile.getInputStream()).thenReturn(input);
    System.out.println(multipartFile.getInputStream());
    List<Student> studentList = csvService.getStudentListFromInputFile(multipartFile, 1L);

    assertEquals(students, studentList);
  }


  @Test
  public void createStudentFromInputStreamTest() {
    csvService = new CsvService();
    String cara = "Cara,Überschär,caueb100@hhu.de,2659396";
    Student expected = new Student("Cara", "Überschär", "caueb100@hhu.de", 2659396L, 1L, null, null);
    Long id = 1L;

    Student rebecca = csvService.createStudentFromInputStream(cara, id);

    assertEquals(expected, rebecca);
  }

  @Test
  public void putStudentOntoListTest() throws IOException {
    this.csvService = new CsvService();

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

    File file = new File("klausurliste.csv");
    if (file.exists()) {
      file.delete();
    }
  }
}
