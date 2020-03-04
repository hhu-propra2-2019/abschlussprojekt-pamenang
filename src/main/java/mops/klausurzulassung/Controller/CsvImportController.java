package mops.klausurzulassung.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CsvImportController {

    @GetMapping("/csvimport")
    public String csvimport(Model model){

        return "csvImport";
    }

}
