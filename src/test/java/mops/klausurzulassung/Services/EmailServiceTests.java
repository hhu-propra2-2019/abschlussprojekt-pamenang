package mops.klausurzulassung.Services;

import mops.klausurzulassung.Domain.Student;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.internet.MimeMessage;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceTests {

  static EmailService emailService;
  static JavaMailSender javaMailSender;
  static MimeMessage mimeMessage;

  @BeforeAll
  public static void beforeAllTests() {
    javaMailSender = mock(JavaMailSender.class);
    emailService = new EmailService(javaMailSender);
    mimeMessage = mock(MimeMessage.class);
  }

  @Test
  public void test_sendMail_checkForMethodCalls() {
    // Arrange
    long id = 5;
    Student student = new Student("t1", "t2", "t3", 1234L, 1L, "t4", "token");
    // Act
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    emailService.sendMail(student, id);
    // Assert
    verify(javaMailSender, times(1)).send((MimeMessage) any());
  }

  @Test
  public void test_generateValidToken_CheckIfSuccessfullyGeneratedLink() {
    long id = 5;
    Student student = new Student("t1", "t2", "t3", (long) 1234, (long) 1, "t4", "token");
    String link = emailService.generateValidTokenLink(student, id);
    System.out.println(link);
    Assertions.assertThat(link).contains("token/5/1234/t1/t2");
  }
}
