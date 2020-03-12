package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.Student;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

@RequestMapping("/zulassung1")
@SessionScope
@Controller
public class StudentenController {

  @Autowired
  TokenverifikationService tokenverifikation;
  @Autowired StudentRepository studentRepository;
  @Autowired StudentService studentService;

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured({"ROLE_studentin", "ROLE_orga"})
  @GetMapping("/student/{token}/{fach}/{matrikelnummer}/{vorname}/{nachname}/")
  public String studentansichtMitToken(@PathVariable String token, @PathVariable String fach, @PathVariable long matrikelnummer, @PathVariable String vorname, @PathVariable String nachname, Model model, KeycloakAuthenticationToken keyToken) {
    model.addAttribute("account", createAccountFromPrincipal(keyToken));
    model.addAttribute("meldung", false);
    model.addAttribute("token", token);
    model.addAttribute("matrikelnummer", matrikelnummer);
    model.addAttribute("fach", fach);
    model.addAttribute("vorname", vorname);
    model.addAttribute("nachname", nachname);
    return "student";
  }

  @GetMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String studentansicht(Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("meldung", false);
    model.addAttribute("student", false);
    model.addAttribute("token", "");
    model.addAttribute("matrikelnummer", "");
    model.addAttribute("fach", "");
    model.addAttribute("vorname", "");
    model.addAttribute("nachname", "");
    if (token.getAccount().getPrincipal().toString().equals("studentin")){
      model.addAttribute("student", true);
      System.out.println(token.getAccount().getPrincipal().toString());
    }

    return "student";
  }

  @PostMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String empfangeDaten(KeycloakAuthenticationToken keycloakAuthenticationToken, Model model, @Valid String matrikelnummer, @Valid String token, @Valid String fach, @Valid String vorname, @Valid String nachname, String email)
      throws SignatureException, NoSuchAlgorithmException, InvalidKeyException,
          NoPublicKeyInDatabaseException {

    boolean value = tokenverifikation.verifikationToken(matrikelnummer, fach, token) && !studentService.isFristAbgelaufen(Long.parseLong(fach));
    if (value) {
      Student student = new Student(vorname, nachname, email, Long.parseLong(matrikelnummer), Long.parseLong(fach), null, token);
      studentService.save(student);
    }
    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("success", value);
    model.addAttribute("fehlerText", "Altzulassung nicht erfolgreich möglicherweise Frist abgelaufen!");
    model.addAttribute("successText", "Altzulassung erfolgreich!");
    model.addAttribute("meldung", true);
    model.addAttribute("student", false);
    if (keycloakAuthenticationToken.getAccount().getPrincipal().toString().equals("studentin"))
      model.addAttribute("student", true);
    return "student";
  }
}
