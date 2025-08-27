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

            helper.setFrom("roundandgo.official@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("[ROUND & GO] 이메일 인증을 완료해 주세요");

            String htmlBody = """
                    <!DOCTYPE html>
                    <html lang="ko">
                    <head>
                      <meta charset="UTF-8" />
                      <title>이메일 인증</title>
                    </head>
                    <body style="margin: 0; padding: 0; background-color: #f6f6f6;">
                      <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0;">
                        <tr>
                          <td align="center">
                            <table width="400" cellpadding="0" cellspacing="0" style="background: #ffffff; border: 2px solid #d9d9d9; border-radius: 8px; padding: 40px; font-family: Arial, sans-serif;">
                              <tr>
                                <td align="center" style="padding-bottom: 20px;">
                                  <img src="https://roundandgo.onrender.com/images/rounandgo-logo.png" alt="ROUND & GO" width="60" />
                                </td>
                              </tr>
                              <tr>
                                <td align="center" style="color: #2c8c7d; font-size: 18px; font-weight: bold; padding-bottom: 10px;">
                                  이메일 인증 안내
                                </td>
                              </tr>
                              <tr>
                                <td align="center" style="color: #777; font-size: 14px; line-height: 1.6; padding-bottom: 20px;">
                                  안녕하세요.<br/>
                                  라운드앤고를 이용해주셔서 진심으로 감사드립니다.<br/>
                                  아래 <b style="color: #2c8c7d;">메일 인증</b> 버튼을 클릭하여 인증을 완료해주세요.
                                </td>
                              </tr>
                              <tr>
                                <td align="center" style="padding-bottom: 30px;">
                                  <a href="%s" style="display: inline-block; padding: 10px 20px; color: #2c8c7d; border: 1px solid #2c8c7d; border-radius: 30px; text-decoration: none; font-weight: 500;">
                                    메일 인증
                                  </a>
                                </td>
                              </tr>
                              <tr>
                                <td align="center" style="font-size: 10px; color: #aaa;">
                                  만약 버튼이 눌리지 않는다면<br/>웹사이트로 돌아가 <b>재전송</b> 버튼을 눌러주세요
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </body>
                    </html>
                    """.formatted(verificationLink);

            helper.setText(htmlBody, true); // HTML 설정

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }
}
