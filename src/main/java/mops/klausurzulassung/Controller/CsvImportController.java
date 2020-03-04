package mops.klausurzulassung.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CsvImportController {

    @GetMapping("/csvimport")
    public String csvimport(Model model){

        return "csvImport";
    }

    @PostMapping("/csvimport")
    public String csvimportPost(@RequestParam("datei")MultipartFile multipartFile, Model model){
        System.out.println(multipartFile.getOriginalFilename());
        return "csvImport";
    }

}
