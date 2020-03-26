package mops.klausurzulassung.controller;

import mops.klausurzulassung.domain.Account;
import mops.klausurzulassung.domain.FrontendMessage;
import mops.klausurzulassung.exceptions.InvalidFrist;
import mops.klausurzulassung.repositories.StudentRepository;
import mops.klausurzulassung.services.StudentService;
import mops.klausurzulassung.services.TokenverifikationService;
import mops.klausurzulassung.wrapper.Token;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.annotation.SessionScope;

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

  /**
   * This method is called for a GET request to /zulassung1/student/{tokenLink}.
   *
   * @param tokenLink link for token
   * @param model     Spring object that is used as a container to supply the variables
   * @param keyToken  contains role data
   * @return view student
   */
  @Secured({"ROLE_studentin", "ROLE_orga"})
  @GetMapping("/student/{tokenLink}/")
  public String studentansichtMitToken(@PathVariable String tokenLink, Model model, KeycloakAuthenticationToken keyToken) {
    model.addAttribute("account", createAccountFromPrincipal(keyToken));
    model.addAttribute("token", new Token(tokenLink));
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();
    return "student";
  }

  /**
   * This method is called for a GET request to /zulassung1/student
   *
   * @param model Spring object that is used as a container to supply the variables
   * @param token contains role data
   * @return view student
   */
  @GetMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String studentansicht(Model model, KeycloakAuthenticationToken token) {
    logger.debug("Rolle: " + token.getAccount().getPrincipal().toString());
    model.addAttribute("account", createAccountFromPrincipal(token));
    model.addAttribute("token", new Token(""));
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();
    return "student";
  }

  /**
   * This method is called for a POST request to /zulassung1/student.
   *
   * @param token                       form data input
   * @param keycloakAuthenticationToken contains role data
   * @param model                       Spring object that is used as a container to supply the variables
   * @return Redirect of view zulassung1/student
   */
  @PostMapping("/student")
  @Secured({"ROLE_studentin", "ROLE_orga"})
  public String empfangeDaten(@ModelAttribute("token") Token token, KeycloakAuthenticationToken keycloakAuthenticationToken, Model model) {
    logger.debug("Token: " + token.getToken());
    try {
      tokenverifikation.verifikationToken(token.getToken());
    } catch (InvalidFrist ex) {
      logger.error("Die Tokenverifikation ist fehlgeschlagen!");
      logger.error(ex.getMessage());
      message.setErrorMessage("Die Frist zur Einreichung von Altzulassung ist bereits abgelaufen!");
      return "redirect:/zulassung1/student";
    } catch (Exception e) {
      logger.error("Die Tokenverifikation ist fehlgeschlagen!");
      logger.error(e.getMessage());
      message.setErrorMessage("Token nicht valide!");
      return "redirect:/zulassung1/student";
    }

    message.setSuccessMessage("Altzulassung erfolgreich!");

    model.addAttribute("account", createAccountFromPrincipal(keycloakAuthenticationToken));
    model.addAttribute("token", "");
    return "redirect:/zulassung1/student";
  }
}
