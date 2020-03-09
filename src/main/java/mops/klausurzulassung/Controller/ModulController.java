package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.CsvService;
import mops.klausurzulassung.organisatoren.Entities.Modul;
import mops.klausurzulassung.organisatoren.Services.ModulService;
import mops.klausurzulassung.organisatoren.Services.StudentService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/zulassung1")
public class ModulController {

  private final ModulService modulService;
  private final CsvService csvService;
  private final StudentService studentService;
  private String errorMessage;
  private String successMessage;
  private Modul currentModul = new Modul();

  public ModulController(ModulService modulService, CsvService csvService, StudentService studentService) {
    this.modulService = modulService;
    this.csvService = csvService;
    this.studentService = studentService;
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
    return "modulAuswahl";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modulHinzufuegen")
  public String newModul(
      @ModelAttribute @Valid Modul modul, Model model, KeycloakAuthenticationToken token, Principal principal) {
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
      setMessages("Modul konnte nicht gelöscht werden, da es in der Datenbank nicht vorhanden ist.", null);
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
    model.addAttribute("fristAbgelaufen", false);
    model.addAttribute("error", errorMessage);
    model.addAttribute("success", successMessage);

    return "modulAnsicht";
  }

  @Secured("ROLE_orga")
  @PostMapping("/modul/{id}")
  public String uploadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, @RequestParam("datei") MultipartFile file) throws IOException {
    model.addAttribute("account", createAccountFromPrincipal(token));
    if (file.isEmpty()) {
      setMessages("Datei ist leer oder es wurde keine Datei ausgewählt!", null);
    } else {
      List<Student> students = csvService.getStudentListFromInputFile(file, id);

      for (Student student : students) {
        System.out.println("Tokens werden generiert und verschickt!");
        // Student wird zu Token-Generierung geschickt (Email, Vorname, Nachname, MatrNr, Fach)
        // Token des Students wird zum Mailsender geschickt
      }
      csvService.writeCsvFile(id, students);
      setMessages(null, "Zulassungsliste wurde erfolgreich verarbeitet.");

    }
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
  public String altzulassungHinzufuegen(@ModelAttribute @Valid Student student, Boolean fristAbgelaufen, @PathVariable Long id, Model model, KeycloakAuthenticationToken token) {
    model.addAttribute("account", createAccountFromPrincipal(token));

    String email = student.getEmail();
    String vorname = student.getVorname();
    String nachname = student.getNachname();
    Long matnr = student.getMatrikelnummer();
    Long modulId = id;

    if (fristAbgelaufen == null) {

      System.out.println("Student:" + email + " " + vorname + " " + nachname + " " + matnr + " " + modulId);
      // Student zur Tokengenierung schicken
      // Token per Mail verschicken

      setMessages(null, "Quittung für Student " + matnr + " ist neu verschickt worden!");

    } else {
      System.out.println("Datenbank wird durchsucht");
      // wenn in DB ein Token für modulId und MatrNr gespeichert ist, Student in Altzulassung-DB schreiben
      //setMessages(null, "Altzulassung des Students "+matnr+" wurde erfolgreich eingetragen.");

      // wenn Student nicht in DB gefunden werden kann, gibt Error Message aus
      //setMessages("Student "+matnr+" wurde nicht gefunden.",null);
    }
    return "redirect:/zulassung1/modul/" + id;
  }

  private void setMessages(String errorMessage, String successMessage) {
    this.errorMessage = errorMessage;
    this.successMessage = successMessage;
  }
}
