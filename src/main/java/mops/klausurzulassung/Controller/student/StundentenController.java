package mops.klausurzulassung.Controller.student;


        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class StundentenController {
    @GetMapping("/student")
    public String index(Model model) {
        return "student";
    }
    @PostMapping("/student")
    public String empfangeDaten(Model model,String name, String token){
    System.out.println(name + " " + token);
        return "student";
    }
}
