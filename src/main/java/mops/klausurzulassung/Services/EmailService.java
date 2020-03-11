package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Service
public class EmailService {

  private static final String FROM_EMAIL = "pamenang@web.de";
  private JavaMailSender javaMailSender;

  private Logger logger = LoggerFactory.getLogger(EmailService.class);

  @Autowired
  public EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  public void sendMail(Student student) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(FROM_EMAIL);
    msg.setTo(student.getEmail());
    msg.setSubject("Klausurzulassungstoken " + student.getFachname());
    msg.setText(
        "Grüezi "
            + student.getVorname()
            + " "
            + student.getNachname()
            + ",\n hiermit erhälst du"
            + " dein Klausurzulassungtoken für das Modul "
            + student.getFachname() +" mit der Modul-ID "
            + student.getModulId() +
            ".\n Dieses lautet: \n"
            + student.getToken());
    javaMailSender.send(msg);
    logger.debug("Email wurde an: "+student.getEmail() +" abgeschickt");
  }

  /*Generiert einen Link für den Studenten der das ganze Studentenformular zur Aktivierung des Tokens direkt ausfüllt*/
  public String generateValidTokenLink(Student student){
    String studentAddUri = "/zulassung1/student/";
    String token = student.getToken()+"/";
    String fachName = student.getFachname()+ "/";
    String matrikelnr = Long.toString(student.getMatrikelnummer());
    return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(studentAddUri)
            .path(token)
            .path(fachName)
            .path(matrikelnr)
            .toUriString();
  }

}
