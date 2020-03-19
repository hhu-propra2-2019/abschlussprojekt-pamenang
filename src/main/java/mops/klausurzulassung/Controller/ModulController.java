package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.AltzulassungStudentDto;
import mops.klausurzulassung.Domain.FrontendMessage;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.ModulService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;

@Controller
@SessionScope
@RequestMapping("/zulassung1")
public class ModulController {

  private final ModulService modulService;
  private Modul currentModul = new Modul();

  private FrontendMessage message = new FrontendMessage();

  public ModulController(ModulService modulService) {
    this.modulService = modulService;
  }

  private Account createAccountFromPrincipal(KeycloakAuthenticationToken token) {
    KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();
    return new Account(
        principal.getName(),
        principal.getKeycloakSecurityContext().getIdToken().getEmail(),
        null,
        token.getAccount().getRoles());
  }

  @Secured("ROLE_orga")
  @GetMapping("/modulAuswahl")
  public String index(Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Iterable<Modul> moduls = modulService.findByOwnerAndActive(principal.getName(), true);
    model.addAttribute("moduls", moduls);

    model.addAttribute("errorMessage",message.getErrorMessage());
    model.addAttribute("successMessage",message.getSuccessMessage());
    message.resetMessage();

    return "modulAuswahl";
  }

  @Secured("ROLE_orga")
  @PostMapping("/neuesModulHinzufuegen")
  public String backToModulAuswahl(@ModelAttribute @Valid Modul modul, Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    String orga = principal.getName();
    String[] messageArray = modulService.saveNewModul(modul, orga);
    Iterable<Modul> moduls = modulService.findByOwnerAndActive(orga, true);
    model.addAttribute("moduls", moduls);
    message.setErrorMessage(messageArray[0]);
    message.setSuccessMessage(messageArray[1]);
    return messageArray[2];
  }

  @Secured("ROLE_orga")
  @GetMapping("/modulBearbeiten/{id}")
  public String modulBearbeiten(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Modul modul = modulService.findById(id).get();
    model.addAttribute("id", id);
    model.addAttribute("modul", modul);
    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    message.resetMessage();

    return "modulBearbeiten";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modulBearbeiten/{id}")
  public String modulAbschicken(@ModelAttribute @Valid Modul modul, @PathVariable Long id, Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    String[] messageArray = modulService.modulBearbeiten(modul, id, principal);
    message.setErrorMessage(messageArray[0]);
    message.setSuccessMessage(messageArray[1]);
    return messageArray[2];
  }

  @Secured("ROLE_orga")
  @GetMapping("/modulHinzufuegen")
  public String newModul(
      Model model,
      KeycloakAuthenticationToken token) {

    model.addAttribute("account", createAccountFromPrincipal(token));

    Iterable<Modul> moduls = modulService.findByActive(false);
    model.addAttribute("moduls", moduls);


    model.addAttribute("modul", currentModul);
    model.addAttribute("bearbeitetesModul", currentModul);

    model.addAttribute("errorMessage", message.getErrorMessage());
    model.addAttribute("successMessage", message.getSuccessMessage());
    return "modulHinzufuegen";

  }


  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}/delete")
  public String deleteModul(Model model, @PathVariable Long id, KeycloakAuthenticationToken token) {

    model.addAttribute("account", createAccountFromPrincipal(token));

    String[] messageArray = modulService.deleteStudentsFromModul(id);
    message.setErrorMessage(messageArray[0]);
    message.setSuccessMessage(messageArray[1]);
    return "redirect:/zulassung1/modulAuswahl";
  }

  @Secured("ROLE_orga")
  @GetMapping("/modul/{id}")
  public String selectModul(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Modul modul = modulService.findById(id).get();
    String name = modul.getName();
    String frist = modul.getFrist();
    model.addAttribute("modul", name);
    model.addAttribute("id", id);
    model.addAttribute("frist", frist);
    model.addAttribute("student", new Student("", "", "", null, id, null, null));
    model.addAttribute("papierZulassung", false);

    model.addAttribute("errorMessage",message.getErrorMessage());
    model.addAttribute("successMessage",message.getSuccessMessage());
    message.resetMessage();

    return "modulAnsicht";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}")
  public String uploadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, @RequestParam("datei") MultipartFile file) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    model.addAttribute("account", createAccountFromPrincipal(token));
    String[] messageArray = modulService.verarbeiteUploadliste(id, file);
    message.setErrorMessage(messageArray[0]);
    message.setSuccessMessage(messageArray[1]);
    return "redirect:/zulassung1/modul" + "/" + id;
  }

  @Secured("ROLE_orga")
  @GetMapping(value ="/modul/{id}/klausurliste", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ResponseBody
  public void downloadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, HttpServletResponse response) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    modulService.download(id, response);
  }


  @Secured("ROLE_orga")
  @PostMapping("/{id}/altzulassungHinzufuegen")
  public String altzulassungHinzufuegen(@ModelAttribute("studentDto") @Valid AltzulassungStudentDto studentDto, BindingResult bindingResult, boolean papierZulassung, @PathVariable Long id, Model model, KeycloakAuthenticationToken token) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    
    if(bindingResult.hasErrors()){
      message.setErrorMessage("Alle Felder im Formular müssen befüllt werden!");
      return "redirect:/zulassung1/modul/" + id;
    }
    model.addAttribute("account", createAccountFromPrincipal(token));

    String[] messageArray = modulService.altzulassungVerarbeiten(studentDto, papierZulassung, id);
    message.setErrorMessage(messageArray[0]);
    message.setSuccessMessage(messageArray[1]);
    return "redirect:/zulassung1/modul/" + id;
  }

}