package org.likelionhsu.roundandgo.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom("roundandgo.official@gmail.com"); // 꼭 작성!
            helper.setTo(toEmail);
            helper.setSubject("[ROUND & GO] 이메일 인증을 완료해 주세요");
            helper.setText("""
                    <html>
                      <body>
                        <p>안녕하세요,</p>
                        <p>아래 링크를 클릭하여 이메일 인증을 완료해 주세요:</p>
                        <a href="%s">%s</a>
                        <p><small>※ 이 링크는 30분 동안만 유효합니다.</small></p>
                      </body>
                    </html>
                    """.formatted(verificationLink, verificationLink), true); // HTML=true

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }
}