package mops.klausurzulassung.Controller.student;


        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StundentenController {
    @GetMapping("/student")
    public String index(Model model) {
        return "student";
    }
}
