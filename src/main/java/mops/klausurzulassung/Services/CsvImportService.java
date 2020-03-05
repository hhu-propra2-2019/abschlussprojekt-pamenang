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

    /*CsvImportService kümmert sich um ein Multipartfile welches ein .csv File repräsentiert. Aus diesem File werden Studenten-Objekte
    generiert die als Liste weitergegeben werden*/


    public List<Student> getStudentListFromInputFile(MultipartFile multipartFile) throws IOException {
        List<Student> studentList = new ArrayList<>();
        InputStream inputstream = multipartFile.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(inputstream));
        String line;

        while ((line = br.readLine()) != null) {
            studentList.add(createStudentFromInputStream(line));
        }
        return studentList;

    }

    private Student createStudentFromInputStream(String line){

        String vorname,nachname,email,fachname,token;
        long matrikelnummer;
        int raumId;

        String [] temp = line.split(",");
        vorname = temp[0];
        nachname = temp[1];
        email = temp[2];
        matrikelnummer = Long.parseLong(temp[3]);
        raumId = 0;
        fachname = null;
        token = null;
        return new Student(vorname,nachname,email,matrikelnummer,raumId,fachname,token);

    }
}
