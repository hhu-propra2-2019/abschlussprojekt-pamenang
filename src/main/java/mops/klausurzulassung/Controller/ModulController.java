package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Exceptions.NoPublicKeyInDatabaseException;
import mops.klausurzulassung.Exceptions.NoTokenInDatabaseException;
import mops.klausurzulassung.Services.CsvService;
import mops.klausurzulassung.Services.EmailService;
import mops.klausurzulassung.Services.QuittungService;
import mops.klausurzulassung.Services.TokengenerierungService;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Services.ModulService;
import mops.klausurzulassung.Services.StudentService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
import java.util.List;
import java.util.Optional;

@Controller
@SessionScope
@RequestMapping("/zulassung1")
public class ModulController {

  private final ModulService modulService;
  private String errorMessage;
  private String successMessage;
  private Modul currentModul = new Modul();

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
  @GetMapping("/modulHinzufuegen")
  public String index(Model model, KeycloakAuthenticationToken token, Principal principal) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Iterable<Modul> moduls = modulService.findByOwner(principal.getName());
    model.addAttribute("moduls", moduls);
    model.addAttribute("modul", currentModul);
    model.addAttribute("error", errorMessage);
    model.addAttribute("success", successMessage);
    resetMessages();
    return "modulAuswahl";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modulHinzufuegen")
  public String newModul(
      @ModelAttribute @Valid Modul modul,
      Model model,
      KeycloakAuthenticationToken token,
      Principal principal) {

    model.addAttribute("account", createAccountFromPrincipal(token));
    modul.setOwner(principal.getName());
    this.currentModul = modul;

    if (modulService.findById(modul.getId()).isPresent()) {
      setMessages("Diese Modul-ID existiert schon, bitte eine andere ID eingeben!", null);
    } else {
      modulService.save(modul);
      setMessages(null, "Neues Modul wurde erfolgreich hinzugefügt!");
      this.currentModul = new Modul();
    }

    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}/delete")
  public String deleteModul(Model model, @PathVariable Long id, KeycloakAuthenticationToken token) {

    model.addAttribute("account", createAccountFromPrincipal(token));
    String [] messages = modulService.deleteStudentsFromModul(id);
    setMessages(messages[0],messages[1]);
    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @Secured("ROLE_orga")
  @GetMapping("/modul/{id}")
  public String selectModul(@PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));
    Modul modul = modulService.findById(id).get();
    String name = modul.getName();
    model.addAttribute("modul", name);
    model.addAttribute("id", id);
    model.addAttribute("student", new Student("", "", "", null, id, null, null));
    model.addAttribute("papierZulassung", false);
    model.addAttribute("error", errorMessage);
    model.addAttribute("success", successMessage);
    resetMessages();
    return "modulAnsicht";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}")
  public String uploadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, @RequestParam("datei") MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoPublicKeyInDatabaseException {
    model.addAttribute("account", createAccountFromPrincipal(token));
    String [] messages = modulService.verarbeiteUploadliste(id, file);
    setMessages(messages[0],messages[1]);
    return "redirect:/zulassung1/modul" + "/" + id;
  }

  @Secured("ROLE_orga")
  @GetMapping(value ="/modul/{id}/klausurliste", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public String downloadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, HttpServletResponse response) throws IOException{
    model.addAttribute("account", createAccountFromPrincipal(token));
    String[] messages = modulService.download(id, response);
    setMessages(messages[0],messages[1]);

    return "redirect:/zulassung1/modul/" + id;
  }

  @Secured("ROLE_orga")
  @PostMapping("/{id}/altzulassungHinzufuegen")
  public String altzulassungHinzufuegen(@ModelAttribute @Valid Student student, boolean papierZulassung, @PathVariable Long id, Model model, KeycloakAuthenticationToken token) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoTokenInDatabaseException, NoPublicKeyInDatabaseException {
    model.addAttribute("account", createAccountFromPrincipal(token));

    String[] messages = modulService.altzulassungVerarbeiten(student, papierZulassung, id);
    setMessages(messages[0],messages[1]);
    return "redirect:/zulassung1/modul/" + id;
  }

  private void resetMessages() {
    setMessages(null, null);
  }

  private void setMessages(String errorMessage, String successMessage) {
    this.errorMessage = errorMessage;
    this.successMessage = successMessage;
  }

}