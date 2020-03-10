package mops.klausurzulassung.Controller;

import mops.klausurzulassung.Domain.Account;
import mops.klausurzulassung.Domain.Modul;
import mops.klausurzulassung.Domain.Student;
import mops.klausurzulassung.Services.*;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.SignatureException;
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
  private final EmailService emailService;

  private final TokengenerierungService tokengenerierungService;

  public ModulController(ModulService modulService, CsvService csvService, StudentService studentService, TokengenerierungService tokengenerierungService, EmailService emailService) {
    this.modulService = modulService;
    this.csvService = csvService;
    this.studentService = studentService;
    this.tokengenerierungService = tokengenerierungService;
    this.emailService = emailService;
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
  public String uploadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, @RequestParam("datei") MultipartFile file) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    model.addAttribute("account", createAccountFromPrincipal(token));
    if (file.isEmpty()) {
      setMessages("Datei ist leer oder es wurde keine Datei ausgewählt!", null);
    } else {
      List<Student> students = csvService.getStudentListFromInputFile(file, id);

      for (Student student : students) {
        System.out.println("Tokens werden generiert und verschickt!");
        String tokenString = tokengenerierungService.erstellenToken(student.getMatrikelnummer().toString(), id.toString());
        System.out.println("TEST "+tokenString);
        student.setToken(tokenString);
        //emailService.sendMail(student);
      }
      csvService.writeCsvFile(id, students);
      setMessages(null, "Zulassungsliste wurde erfolgreich verarbeitet.");

    }
    return "redirect:/zulassung1/modul" + "/" + id;
  }
  /*@RequestMapping(value = "/files/{file_name}", method = RequestMethod.GET)
  @ResponseBody
  public FileSystemResource getFile(@PathVariable("file_name") String fileName) {
    return new FileSystemResource(myService.getFileFor(fileName));
  }
}*/

  @Secured("ROLE_orga")
  @GetMapping(value ="/modul/{id}/klausurliste", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ResponseBody
  public void downloadListe(@PathVariable Long id, Model model, KeycloakAuthenticationToken token, HttpServletResponse response) throws IOException{
    model.addAttribute("account", createAccountFromPrincipal(token));
    setMessages(null, "Klausurliste wurde erfolgreich heruntergeladen.");



    response.setContentType("text/csv");
    response.setHeader("Content-Disposition", "attachment; filename=\"klausurliste.csv\"");
    try
    {
      OutputStream outputStream = response.getOutputStream();
      File klausurliste = new File("klausurliste_"+Long.toString(id)+".csv");
      outputStream.write(klausurliste.toString().getBytes());
      outputStream.flush();
      outputStream.close();
    }
    catch(Exception e)
    {
      System.out.println(e.toString());
    }
    //return new FileSystemResource(new File("klausurliste_"+Long.toString(id)+".csv"));
    //return "redirect:/zulassung1/modulHinzufuegen";
  }

  @Secured("ROLE_orga")
  @PostMapping("/{id}/altzulassungHinzufuegen")
  public String altzulassungHinzufuegen(@ModelAttribute @Valid Student student, Boolean fristAbgelaufen, @PathVariable Long id, Model model, KeycloakAuthenticationToken token) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    model.addAttribute("account", createAccountFromPrincipal(token));

    String email = student.getEmail();
    String vorname = student.getVorname();
    String nachname = student.getNachname();
    Long matnr = student.getMatrikelnummer();
    Long modulId = id;

    if (fristAbgelaufen == null) {

      System.out.println("Student:" + email + " " + vorname + " " + nachname + " " + matnr + " " + modulId);
      String tokenString = tokengenerierungService.erstellenToken(matnr.toString(), id.toString());
      student.setToken(tokenString);
      //emailService.sendMail(student);

      setMessages(null, "Quittung für Student " + matnr + " ist neu verschickt worden!");

    } else {
      System.out.println("Datenbank wird durchsucht");
      String tokenString = tokengenerierungService.erstellenToken(matnr.toString(), id.toString());

      if (tokenString != null){
        studentService.save(new Student(vorname, nachname, email, matnr, id,null, tokenString));
        setMessages(null, "Altzulassung des Students "+matnr+" wurde erfolgreich eingetragen.");
      }


      if (tokenString == null){
        setMessages("Student "+matnr+" wurde nicht gefunden.",null);
      }
    }
    return "redirect:/zulassung1/modul/" + id;
  }

  private void setMessages(String errorMessage, String successMessage) {
    this.errorMessage = errorMessage;
    this.successMessage = successMessage;
  }
}
