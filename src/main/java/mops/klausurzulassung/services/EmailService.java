package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import mops.klausurzulassung.domain.EmailError;
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
  public static final String STYLESHEET_BOOTSTRAP = "<link href='https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css' rel='stylesheet'>";
  private JavaMailSender javaMailSender;
  private EmailErrorService emailErrorService;

  private Logger logger = LoggerFactory.getLogger(EmailService.class);

  @Autowired
  public EmailService(JavaMailSender javaMailSender, EmailErrorService emailErrorService) {
    this.javaMailSender = javaMailSender;
    this.emailErrorService = emailErrorService;
  }


  /**
   * This method sends an individual Email to the Student
   *
   * @param student contains Information for sending the Email
   */
  public void sendMail(Student student) {
    try {
      MimeMessage message = getEmailMessage(student);
      this.javaMailSender.send(message);
    } catch (Exception exception) {
      logger.error("Die mail konnte nicht versendet werden");
      logger.error(exception.getMessage());
      emailErrorService.addEmailErrorToList(new EmailError(student));
      return;
    }
    logger.debug("Email wurde an: " + student.getEmail() + " abgeschickt");
  }

  /**
   * This method builds the Content of the Email
   *
   * @param student contains Information for sending the Email
   * @return MimeMessage-Object which wraps the message
   */
  private MimeMessage getEmailMessage(Student student) throws MessagingException {
    String body = STYLESHEET_BOOTSTRAP
            + "<h3>Hallo, "
            + student.getVorname()
            + "!"
            + "</h3><br> Hier ist dein Token für das Modul "
            + student.getFachname()
            + " mit der ID " + student.getModulId()
            + "."
            + "<br>Klicke auf den <a href='"
            + generateValidTokenLink(student)
            + "'>Link</a>, um dich zu zulassen.<br> Bitte verliere den Token nicht, sonst ist es nicht möglich sich für die Klausur zu zulassen.<br>Token: "
            + student.getToken();
    MimeMessage message = this.javaMailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(FROM_EMAIL);
    helper.setTo(student.getEmail());
    helper.setSubject("Klausurzulassungstoken " + student.getFachname());
    helper.setText(body, true);
    return message;
  }

  /**
   * Generates a specific Activiationlink with Tokeninformation, which will be parsed directly into the form.
   *
   * @param student contains the Token which is part of the linkl
   * @return Link as String
   */
  public String generateValidTokenLink(Student student) {
    logger.debug("Link wird erstellt");
    String studentAddUri = "/zulassung1/student/";
    String quittung = student.getToken() + "/";
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        .path(studentAddUri)
        .path(quittung)
        .toUriString();
  }

  /**
   * This method resends an individual Email to the Student
   *
   * @param student contains Information for sending the Email
   * @return true if Email was sucessfully send. Else it returns false.
   */
  public boolean resendEmail(Student student) {
    try {
      MimeMessage message = getEmailMessage(student);
      javaMailSender.send(message);
    } catch (Exception exception) {
      logger.error("Email konnte nicht neu versendet werden.");
      logger.error(exception.getMessage());
      return false;
    }
    logger.debug("Email wurde erfolgreich an " + student.getMatrikelnummer() + " versendet.");
    return true;
  }
}