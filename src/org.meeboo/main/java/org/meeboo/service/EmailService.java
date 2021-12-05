package org.meeboo.service;

import com.sun.mail.smtp.SMTPTransport;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static org.meeboo.constant.EmailConstant.*;
import static javax.mail.Message.RecipientType.CC;

@Service
public class EmailService {
    public void sendNewPasswordEMail(String firstName, String password, String email, String confirmationToken) throws javax.mail.MessagingException {
        Message message = createEmail(firstName, password, email, confirmationToken);
        var smtpTransport = (SMTPTransport) getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);
        smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
        smtpTransport.sendMessage(message, message.getAllRecipients());
        smtpTransport.close();
    }

    private Message createEmail(String firstName, String password, String email, String confirmationToken) throws javax.mail.MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setRecipients(CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello " + firstName + ", \n \n Your new account password is: " + password + "\n \n The Support Team");
        message.setSentDate(new Date());
        message.setFrom(new InternetAddress("support@meeboo.org"));


        String htmlText =
                "<h1>Hello World</h1>" +
                "<span>To confirm your account, please click here : </span> <br>" +
                "<span>http://localhost:8082/api/confirm-account?token=" + confirmationToken + "</span>";

        message.setContent(htmlText, "text/html");

        message.saveChanges();

        return message;    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        properties.put(SMTP_SSL, true);
        return Session.getInstance(properties, null);
    }
}
