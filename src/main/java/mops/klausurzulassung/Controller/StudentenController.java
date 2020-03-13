package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.FrontendMessage;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Domain.StudentDto;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Repositories.StudentRepository;
import mops.klausurzulassung.Services.StudentService;
import mops.klausurzulassung.Services.TokenverifikationService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Optional;

@RequestMapping("/zulassung1")
@SessionScope
@Controller
public class StudentenController {

  @Autowired
  TokenverifikationService tokenverifikation;
  @Autowired StudentRepository studentRepository;
  @Autowired StudentService studentService;
  private FrontendMessage message = new FrontendMessage();

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured({"ROLE_studentin", "ROLE_orga"})
  @GetMapping("/student/{tokenLink}/{fachLink}/{matrikelnummerLink}/{vornameLink}/{nachnameLink}/")
  public String studentansichtMitToken(@PathVariable String tokenLink, @PathVariable Long fachLink, @PathVariable Long matrikelnummerLink, @PathVariable String vornameLink, @PathVariable String nachnameLink, Model model, KeycloakAuthenticationToken keyToken) {
    Student student = studentService.findByToken(tokenLink).get();
    StudentDto studentDto = StudentDto.builder()
            .email(student.getEmail())
            .fachname(student.getFachname())
            .matrikelnummer(student.getMatrikelnummer())
            .modulId(student.getModulId())
            .nachname(student.getNachname())
            .token(student.getToken())
            .vorname(student.getVorname()).build();
    model.addAttribute("account", createAccountFromPrincipal(keyToken));
    model.addAttribute("studentDto", studentDto);

    model.addAttribute("errorMessage",message.getErrorMessage());
    model.addAttribute("successMessage",message.getSuccessMessage());
    message.resetMessage();
    return "student";
  }

  @GetMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String studentansicht(Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("student", false);
    model.addAttribute("studentDto", new StudentDto());


    if (token.getAccount().getPrincipal().toString().equals("studentin")){
      model.addAttribute("student", true);
      System.out.println(token.getAccount().getPrincipal().toString());
    }

    model.addAttribute("errorMessage",message.getErrorMessage());
    model.addAttribute("successMessage",message.getSuccessMessage());
    message.resetMessage();



    return "student";
  }

  @PostMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String empfangeDaten(@ModelAttribute("studentDto") @Valid StudentDto studentDto, BindingResult bindingResult, KeycloakAuthenticationToken keycloakAuthenticationToken, Model model)
      throws SignatureException, NoSuchAlgorithmException, InvalidKeyException,
          NoPublicKeyInDatabaseException {

    if(bindingResult.hasErrors()){
      model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
      return "student";
    }


    boolean tokenValid = tokenverifikation.verifikationToken(studentDto.getMatrikelnummer().toString(), studentDto.getModulId().toString(), studentDto.getToken());
    /*
    if(studentService.isFristAbgelaufen(Long.parseLong(studentDto.getModulId().toString()))){

      message.setErrorMessage("Frist ist abgelaufen");
    }*/
     if (tokenValid) {

      Student student = Student.builder()
              .token(studentDto.getToken())
              .modulId(studentDto.getModulId())
              .matrikelnummer(studentDto.getMatrikelnummer())
              .fachname(studentDto.getFachname())
              .email(studentDto.getEmail())
              .nachname(studentDto.getNachname())
              .vorname(studentDto.getVorname())
              .build();

      studentService.save(student);
      message.setSuccessMessage("Altzulassung erfolgreich!");
    }else{
      message.setErrorMessage("Token nicht Valide");
    }

    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("student", false);
    if (keycloakAuthenticationToken.getAccount().getPrincipal().toString().equals("studentin"))
      model.addAttribute("student", true);
    return "redirect:/zulassung1/student";
  }
}
