package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.CsvImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CsvImportController {


    @Autowired
    CsvImportService csvImportService;

    private boolean imported =false;

    private List<Student> studentList =  new ArrayList<>();;

    @GetMapping("/csvImport")
    public String csvimport(Model model){
        model.addAttribute("studentList", studentList);
        model.addAttribute("imported", imported);
        return "csvImport";
    }

    @PostMapping("/csvImport")
    public String csvimportPost(@RequestParam("datei")MultipartFile multipartFile, Model model) throws IOException {

        studentList = csvImportService.getStudentListFromInputFile(multipartFile);
        if(!studentList.isEmpty())imported=true;

        return "redirect:/csvImport";
    }

}
