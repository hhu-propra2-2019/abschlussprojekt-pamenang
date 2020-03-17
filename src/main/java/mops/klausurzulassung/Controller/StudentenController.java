package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.FrontendMessage;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Repositories.StudentRepository;
import mops.klausurzulassung.Services.StudentService;
import mops.klausurzulassung.Services.TokenverifikationService;
import mops.klausurzulassung.wrapper.Token;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;

@RequestMapping("/zulassung1")
@SessionScope
@Controller
public class StudentenController {

  @Autowired
  TokenverifikationService tokenverifikation;
  @Autowired
  StudentRepository studentRepository;
  @Autowired
  StudentService studentService;
  private FrontendMessage message = new FrontendMessage();
  private Logger logger = LoggerFactory.getLogger(TokenverifikationService.class);

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured({"ROLE_studentin", "ROLE_orga"})
  @GetMapping("/student/{tokenLink}/")
  public String studentansichtMitToken(@PathVariable String tokenLink, Model model, KeycloakAuthenticationToken keyToken) {
    Student student = studentService.findByToken(tokenLink).get();
    model.addAttribute("account", createAccountFromPrincipal(keyToken));
    model.addAttribute("token", new Token(tokenLink));
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();
    return "student";
  }

  @GetMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String studentansicht(Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("student", false);
    model.addAttribute("token", new Token(""));

    if (token.getAccount().getPrincipal().toString().equals("studentin")) {
      model.addAttribute("student", true);
      logger.debug(token.getAccount().getPrincipal().toString());
    }

    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();


    return "student";
  }

  @PostMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String empfangeDaten(@ModelAttribute("token") Token token, BindingResult bindingResult, KeycloakAuthenticationToken keycloakAuthenticationToken, Model model)
      throws SignatureException, NoSuchAlgorithmException, InvalidKeyException,
      NoPublicKeyInDatabaseException, ParseException {

    if (bindingResult.hasErrors()) {
      model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
      return "student";
    }
    logger.debug("Token: " + token.getToken());

    long[] verifizierungsErgebnis = tokenverifikation.verifikationToken(token.getToken());
    if (verifizierungsErgebnis[0] > 0 &&
        verifizierungsErgebnis[1] > 0) {

      Student student = Student.builder()
          .matrikelnummer(verifizierungsErgebnis[0])
          .modulId(verifizierungsErgebnis[1]).build();

      studentService.save(student);
      message.setSuccessMessage("Altzulassung erfolgreich!");
    } else {
      message.setErrorMessage("Token nicht Valide");
    }

    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("student", false);
    if (keycloakAuthenticationToken.getAccount().getPrincipal().toString().equals("studentin"))
      model.addAttribute("student", true);
    model.addAttribute("token", "");
    return "redirect:/zulassung1/student";
  }
}
