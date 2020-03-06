package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class EmailService {

  private static final String FROM_EMAIL = "pamenang@web.de";
  private JavaMailSender javaMailSender;

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
            + " dein Klausurzulassungtoken. Dieses lautet: \n"
            + student.getToken());
    javaMailSender.send(msg);
  }

  public String generateValidTokenLink(Student student){
    String link = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
    String studentAddUri = "/zulassung1/student/";
    String token = student.getToken()+"/";
    String fachName = student.getFachname()+ "/";
    String matrikelnr = Long.toString(student.getMatrikelnummer());
    link = link + studentAddUri + token  + fachName +matrikelnr;
    return link;

  }
}
