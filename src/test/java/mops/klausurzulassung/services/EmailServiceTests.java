package mops.klausurzulassung.services;

import mops.klausurzulassung.database_entity.Student;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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
class EmailServiceTests {

  private static EmailService emailService;
  private static JavaMailSender javaMailSender;
  private static MimeMessage mimeMessage;

  @BeforeAll
  static void beforeAllTests() {
    javaMailSender = mock(JavaMailSender.class);
    emailService = new EmailService(javaMailSender);
    mimeMessage = mock(MimeMessage.class);
  }

  @Test
  void test_sendMail_checkForMethodCalls() {
    // Arrange
    Student student = new Student("t1", "t2", "t3", 1234L, 1L, "t4", "token");
    // Act
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    emailService.sendMail(student);
    // Assert
    verify(javaMailSender, times(1)).send((MimeMessage) any());
  }

  @Test
  void test_generateValidToken_CheckIfSuccessfullyGeneratedLink() {
    Student student = new Student("t1", "t2", "t3", (long) 1234, (long) 1, "t4", "token");
    String link = emailService.generateValidTokenLink(student);
    Assertions.assertThat(link).contains("token/");
  }
}
