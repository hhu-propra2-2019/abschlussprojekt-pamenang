package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvImportService {



    public List<Student> getStudentListFromInputFile(MultipartFile multipartFile) throws IOException {
        List<Student> studentList = new ArrayList<>();
        InputStream inputstream = multipartFile.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
        String line;
        String vorname,nachname;
        long matrikelnummer;
        boolean zulassung;
        while ((line = br.readLine()) != null) {
            String [] temp = line.split(",");
            vorname = temp[0];
            nachname = temp[1];
            matrikelnummer = Long.parseLong(temp[2]);
            zulassung = temp[3].equals("ja");

            Student student = new Student(vorname,nachname,matrikelnummer,zulassung);
            studentList.add(student);
        }
        return studentList;

    }
}
