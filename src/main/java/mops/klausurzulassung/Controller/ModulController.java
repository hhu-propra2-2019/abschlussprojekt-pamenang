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
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
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
  private final CsvService csvService;
  private final StudentService studentService;
  private final QuittungService quittungService;
  private final EmailService emailService;
  private final TokengenerierungService tokengenerierungService;
  private String errorMessage;
  private String successMessage;
  private Modul currentModul = new Modul();

  public ModulController(ModulService modulService, CsvService csvService, StudentService studentService, TokengenerierungService tokengenerierungService, EmailService emailService, QuittungService quittungService) {
    this.modulService = modulService;
    this.csvService = csvService;
    this.studentService = studentService;
    this.tokengenerierungService = tokengenerierungService;
    this.emailService = emailService;
    this.quittungService = quittungService;
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

  private void resetMessages() {
    setMessages(null, null);
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
    Optional<Modul> modul = modulService.findById(id);
    if (modul.isPresent()) {
      String modulName = modul.get().getName();
      modulService.delete(modul.get());

      Iterable<Student> students = studentService.findByModulId(id);
      for (Student student : students) {
        studentService.delete(student);
      }

      setMessages(null, "Das Modul " + modulName + " wurde gelöscht!");
    } else {
      setMessages(
          "Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.", null);
    }
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
    modulService.verarbeiteUploadliste(id, file);
    return "redirect:/zulassung1/modul" + "/" + id;
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}/download")
  public String downloadListe() {

    // Liste muss noch herunter geladen werden

    setMessages(null, "Klausurliste wurde erfolgreich heruntergeladen.");
    return "redirect:/zulassung1/modulHinzufuegen";
  }

  @Secured("ROLE_orga")
  @PostMapping("/{id}/altzulassungHinzufuegen")
  public String altzulassungHinzufuegen(@ModelAttribute @Valid Student student, boolean papierZulassung, @PathVariable Long id, Model model, KeycloakAuthenticationToken token) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoTokenInDatabaseException, NoPublicKeyInDatabaseException {
    model.addAttribute("account", createAccountFromPrincipal(token));

    String email = student.getEmail();
    String vorname = student.getVorname();
    String nachname = student.getNachname();
    Long matnr = student.getMatrikelnummer();
    student.setModulId(id);

    try {
      System.out.println("Papier 1: "+papierZulassung);
      String tokenString = quittungService.findTokenByQuittung(matnr.toString(), id.toString());

      student.setToken(tokenString);
      String modulname = modulService.findById(id).get().getName();
      student.setFachname(modulname);
      studentService.save(student);
      setMessages(null, "Student "+matnr+" wurde erfolgreich zur Altzulassungsliste hinzugefügt.");
      emailService.sendMail(student);
      
    } catch (NoTokenInDatabaseException e) {
      System.out.println("Exception: "+e);
      System.out.println("Catch");
      System.out.println("Papier: "+papierZulassung);
      if (papierZulassung == true) {
        erstelleTokenUndSendeEmail(student, student.getModulId());
      } else {
        setMessages("Student " + matnr + " hat keine Zulassung in diesem Modul!", null);
      }
    }
    return "redirect:/zulassung1/modul/" + id;
  }

  private void setMessages(String errorMessage, String successMessage) {
    this.errorMessage = errorMessage;
    this.successMessage = successMessage;
  }

  public void erstelleTokenUndSendeEmail(Student student, Long id) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoPublicKeyInDatabaseException {

    try {
      quittungService.findPublicKeyByQuittung(student.getMatrikelnummer().toString(), student.getModulId().toString());

      String modulname = modulService.findById(id).get().getName();
      student.setFachname(modulname);
      emailService.sendMail(student);

    } catch (NoPublicKeyInDatabaseException e){
      String tokenString = tokengenerierungService.erstellenToken(student.getMatrikelnummer().toString(), id.toString());
      student.setToken(tokenString);
      String modulname = modulService.findById(id).get().getName();
      student.setFachname(modulname);
      studentService.save(student);

      emailService.sendMail(student);
    }
  }

}