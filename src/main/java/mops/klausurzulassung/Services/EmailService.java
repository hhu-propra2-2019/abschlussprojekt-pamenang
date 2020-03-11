package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

  private static final String FROM_EMAIL = "pamenang@web.de";
  private JavaMailSender javaMailSender;

  private Logger logger = LoggerFactory.getLogger(EmailService.class);

  @Autowired
  public EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  public void sendMail(Student student, long id) {
    try {
      String body =
          "<h1>Hallo, "
              + student.getVorname()
              + "</h1><br> Klicke auf den <a href='"
              + generateValidTokenLink(student, id)
              + "'>Link</a>, um dich zu zulassen.<br> Bitte verliere den Token nicht, sonst ist es nicht möglich sich für die Klausur zu zuzlassen.<br>Token: "
              + student.getToken();
      MimeMessage message = this.javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(FROM_EMAIL);
      helper.setTo(student.getEmail().toString());
      helper.setSubject("Klausurzulassungstoken " + id);
      helper.setText(body, true);
      this.javaMailSender.send(message);
    } catch (MessagingException e1) {
      e1.printStackTrace();
    }
    logger.debug("Email wurde an: " + student.getEmail() + " abgeschickt");
  }

  /*Generiert einen Link für den Studenten der das ganze Studentenformular zur Aktivierung des Tokens direkt ausfüllt*/
  public String generateValidTokenLink(Student student, long id) {
    String studentAddUri = "/zulassung1/student/";
    String token = student.getToken() + "/";
    String fachID = id + "/";
    String matrikelnr = Long.toString(student.getMatrikelnummer()) + "/";
    String studvorname = student.getVorname() + "/";
    String studnachname = student.getNachname() + "/";
    String email = student.getEmail();
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        .path(studentAddUri)
        .path(token)
        .path(fachID)
        .path(matrikelnr)
        .path(studvorname)
        .path(studnachname)
        .toUriString();
  }
}
